// HoneyBot: parses a Honeypot bug report (GitHub issue form) and posts a triage comment.
// Runs on Node 20+ with no dependencies (uses built-in fetch).
//
// Env:
//   GITHUB_EVENT_PATH  - set by Actions; issue payload is read from here (never interpolated into shell)
//   GITHUB_REPOSITORY  - set by Actions
//   GITHUB_TOKEN       - token with issues:write
//   MANIFEST_JSON      - path to version_manifest.yml converted to JSON
//   DRY_RUN=1          - print the comment instead of posting it

import fs from 'node:fs';

// ---------- Config ----------
const FIELDS = {
  honeypot: 'what version of honeypot are you using?',
  mc: 'mc server version',
};
const NO_RESPONSE = '_No response_';
const MAX_COMMENT_CHARS = 65000; // GitHub hard limit is 65,536
const MAX_LOGS = 3;
const MCLOGS_RE = /https?:\/\/(?:www\.)?mclo\.gs\/([A-Za-z0-9]+)/g;

const MIN_KEYS = ['min', 'minimum', 'min-version', 'min_version', 'minVersion',
  'min-mc-version', 'min_mc_version', 'minMcVersion', 'min-supported', 'min_supported'];
const MAX_KEYS = ['max', 'maximum', 'max-version', 'max_version', 'maxVersion',
  'max-mc-version', 'max_mc_version', 'maxMcVersion', 'max-supported', 'max_supported'];


// ---------- Issue form parsing ----------
export function parseIssueForm(markdown) {
  const md = (markdown ?? '').replace(/\r\n/g, '\n');
  const heads = [...md.matchAll(/^###\s+(.+?)\s*$/gm)];
  const sections = {};
  heads.forEach((m, i) => {
    const start = m.index + m[0].length;
    const end = i + 1 < heads.length ? heads[i + 1].index : md.length;
    const value = md.slice(start, end).trim();
    sections[m[1].trim().toLowerCase()] = value === '' || value === NO_RESPONSE ? null : value;
  });
  return sections;
}

// ---------- Version helpers ----------
// Pulls the first dotted version out of free text: "v5.1.3" -> "5.1.3", "1.21.4-R0.1" -> "1.21.4"
export function parseVersion(text) {
  if (text == null) return null;
  const m = String(text).match(/\d+(?:\.\d+)+/);
  return m ? m[0] : null;
}

const toParts = (v) => String(v).split('.').map((n) => parseInt(n, 10) || 0);

export function compareVersions(a, b) {
  const pa = toParts(a), pb = toParts(b);
  const len = Math.max(pa.length, pb.length);
  for (let i = 0; i < len; i++) {
    const d = (pa[i] ?? 0) - (pb[i] ?? 0);
    if (d !== 0) return d < 0 ? -1 : 1;
  }
  return 0;
}

// Both bounds inclusive and optional. A max with fewer segments acts as a wildcard: max "26.3" allows 26.3.x.
export function isInRange(version, min, max) {
  if (min && compareVersions(version, min) < 0) return false;
  if (!max) return true;
  const maxLen = toParts(max).length;
  const trimmed = toParts(version).slice(0, maxLen).join('.');
  return compareVersions(trimmed, max) <= 0;
}

// ---------- Manifest ----------
const pick = (obj, keys) => {
  for (const k of keys) if (obj[k] != null) return String(obj[k]).trim();
  return null;
};

// Accepts any of:
//   versions: { "5.1.3": { min: "1.21", max: "26.3" } }
//   { "5.1.3": { min: "1.21", max: "26.3" } }
//   versions: [ { version: "5.1.3", min: "1.21", max: "26.3" } ]
// Nested { mc: { min, max } } / { minecraft: { min, max } } also works.
export function normalizeManifest(raw) {
  const map = new Map();
  if (!raw || typeof raw !== 'object') return map;
  const entries = raw.versions ?? raw;

  const add = (ver, info) => {
    if (ver == null || !info || typeof info !== 'object') return;
    const nested = info['supported-server-versions'] ?? info.mc ?? info.minecraft;
    const src = nested && typeof nested === 'object' ? nested : info;
    map.set(String(ver).trim().replace(/^v/i, ''), { min: pick(src, MIN_KEYS), max: pick(src, MAX_KEYS) });
  };

  if (Array.isArray(entries)) entries.forEach((e) => add(e?.version ?? e?.honeypot ?? e?.name, e));
  else Object.entries(entries).forEach(([k, v]) => add(k, v));
  return map;
}

export function findManifestEntry(manifest, version) {
  if (manifest.has(version)) return manifest.get(version);
  for (const [k, v] of manifest) if (compareVersions(k, version) === 0) return v; // 5.1 == 5.1.0
  return null;
}

function loadManifest(path) {
  try {
    if (!path || !fs.existsSync(path)) return new Map();
    return normalizeManifest(JSON.parse(fs.readFileSync(path, 'utf8')));
  } catch (err) {
    console.warn(`Could not load manifest: ${err.message}`);
    return new Map();
  }
}

// ---------- Support check ----------
export function checkSupport(sections, manifest) {
  const find = (label, fallback) =>
    sections[label] ?? Object.entries(sections).find(([k]) => fallback(k))?.[1] ?? null;

  const hpRaw = find(FIELDS.honeypot, (k) => k.includes('honeypot') && k.includes('version'));
  const mcRaw = find(FIELDS.mc, (k) => (k.includes('mc') || k.includes('minecraft')) && k.includes('version'));

  const honeypot = parseVersion(hpRaw);
  const server = parseVersion(mcRaw);

  // result: 'unparsed' | 'unknown' (not in manifest / no bounds) | 'supported' | 'unsupported'
  if (!honeypot || !server) return { honeypot, server, result: 'unparsed' };
  const entry = findManifestEntry(manifest, honeypot);
  if (!entry || (!entry.min && !entry.max)) return { honeypot, server, result: 'unknown' };
  return { honeypot, server, result: isInRange(server, entry.min, entry.max) ? 'supported' : 'unsupported' };
}

// GitHub only renders an alert when [!TYPE] is alone on its first line, so the title goes on the next line in bold.
const alert = (type, title, text) => `> [!${type}]\n> **${title}**\n> ${text}`;

// Only problems get an alert; a supported (or unverifiable) setup adds nothing to the comment.
export function renderSupportAlert({ honeypot, server, result }) {
  if (result === 'unparsed') {
    if (!honeypot && !server) {
      return alert('WARNING', "Couldn't read your Honeypot or server version",
        'Could not determine the Honeypot version or the Minecraft server version from your report. Please double-check what you entered (e.g. `5.1.3` and `26.3`) so compatibility can be checked.');
    }
    if (!honeypot) {
      return alert('WARNING', "Couldn't read your Honeypot version",
        'Could not determine the Honeypot version from your report. Please double-check what you entered (e.g. `5.1.3`) so compatibility can be checked.');
    }
    return alert('WARNING', "Couldn't read your server version",
      'Could not determine the Minecraft server version from your report. Please double-check what you entered (e.g. `26.3`) so compatibility can be checked.');
  }
  if (result === 'unsupported') {
    return alert('CAUTION', 'Unsupported Configuration',
      'The version of Honeypot running on this server is not guaranteed to be compatible. Support may be limited. Please try updating Honeypot first, if possible, and see if the issue is resolved.');
  }
  return null;
}

// ---------- mclo.gs ----------
export function findLogIds(text) {
  return [...new Set([...(text ?? '').matchAll(MCLOGS_RE)].map((m) => m[1]))].slice(0, MAX_LOGS);
}

async function fetchLog(id) {
  const res = await fetch(`https://api.mclo.gs/1/log/${encodeURIComponent(id)}?get=true&insights=true`, {
    headers: { Accept: 'application/json', 'User-Agent': 'HoneyBot GitHub Action' },
    signal: AbortSignal.timeout(20000),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const data = await res.json();
  if (!data.success) throw new Error(data.error ?? 'API returned success: false');
  return data;
}

// ---------- Markdown rendering ----------
// Neutralise @mentions and stray HTML from untrusted text rendered outside code blocks.
const safe = (s) => String(s ?? '').replace(/</g, '&lt;').replace(/@/g, '@\u200b');

function fence(text, lang = '') {
  const longest = Math.max(2, ...[...text.matchAll(/`+/g)].map((m) => m[0].length));
  const f = '`'.repeat(longest + 1);
  return `${f}${lang}\n${text}\n${f}`;
}

function renderProblems(problems) {
  if (!Array.isArray(problems) || problems.length === 0) return '_None detected._';
  return problems.map((p) => {
    const msg = p?.message ?? JSON.stringify(p);
    const count = p?.counter > 1 ? ` (×${p.counter})` : '';
    const sols = (p?.solutions ?? [])
      .map((s) => `  - 💡 ${safe(s?.message ?? JSON.stringify(s))}`).join('\n');
    return `- **${safe(msg)}**${count}${sols ? `\n${sols}` : ''}`;
  }).join('\n');
}

function renderInformation(info) {
  if (!Array.isArray(info) || info.length === 0) return '_None detected._';
  return info.map((i) => {
    if (i?.label != null && i?.value != null) return `- **${safe(i.label)}:** ${safe(i.value)}`;
    return `- ${safe(i?.message ?? JSON.stringify(i))}`;
  }).join('\n');
}

// Keep the tail of the log (crashes and stack traces are usually at the end).
function truncateTail(text, budget) {
  if (text.length <= budget) return { text, cut: 0 };
  const lines = text.split('\n');
  const kept = [];
  let size = 0;
  for (let i = lines.length - 1; i >= 0; i--) {
    if (size + lines[i].length + 1 > budget) break;
    kept.unshift(lines[i]);
    size += lines[i].length + 1;
  }
  return { text: kept.join('\n'), cut: lines.length - kept.length };
}

function renderLog(log, rawBudget) {
  const url = log.data?.url ?? `https://mclo.gs/${log.id}`;
  if (log.error) {
    return `> ⚠️ I couldn't load [${url}](${url}) (${safe(log.error)}). It may have expired or been deleted.`;
  }
  const d = log.data;
  const meta = [d.lines != null && `${d.lines} lines`, d.errors != null && `${d.errors} errors`].filter(Boolean).join(', ');
  const raw = String(d.content?.raw ?? '');
  const { text, cut } = truncateTail(raw, Math.max(0, rawBudget));
  const rawNote = cut > 0
    ? `**Raw log** (first ${cut} lines omitted to fit GitHub's comment limit; [view full log](${url}))`
    : `**Raw log**`;
  const rawBlock = text ? fence(text, 'log') : `_Log too large to include here; [view full log](${url})._`;

  return [
    '<details>',
    `<summary>📄 ${url}${meta ? ` (${meta})` : ''}</summary>`,
    '',
    '**Detected problems**',
    renderProblems(d.content?.insights?.problems),
    '',
    '**Detected information**',
    renderInformation(d.content?.insights?.information),
    '',
    rawNote,
    '',
    rawBlock,
    '',
    '</details>',
  ].join('\n');
}

export function buildComment({ support, logs }) {
  const intro = [
    "# Hey there, I'm HoneyBot 🍯",
    "Thanks for reporting an issue, and sorry you're having issues!",
    '',
    "If you haven't yet, please change `logging.enable-logging` to `true` and `logging.minimum-log-level` to `DEBUG` or `VERBOSE` in Honeypot's config.yml. Once done, run `/honeypot reload` or restart your server and reproduce the issue. Please provide the results of those logs in a comment! ",
  ];
  if (logs.length === 0) {
    intro.push('', "If this is related to a crash, please upload your server logs to [https://mclo.gs](https://mclo.gs), then paste the link in a comment. I'll try to parse them!");
  }

  const supportAlert = renderSupportAlert(support);
  const info = [...(supportAlert ? ['', supportAlert] : []), '', '# Logs'];

  const head = [...intro, ...info].join('\n');
  if (logs.length === 0) return `${head}\n_No logs provided yet._`;
  return withLogs(head, logs);
}

// Comment posted when the issue author later replies with an mclo.gs link.
export function buildLogsComment({ logs }) {
  const head = [
    '# HoneyBot here again 🍯',
    "Thanks for sharing your logs! Here's what I was able to pull from them.",
    '',
    '# Logs',
  ].join('\n');
  return withLogs(head, logs);
}

// Render everything without raw logs first, then share the remaining space between them.
function withLogs(head, logs) {
  const skeleton = logs.map((l) => renderLog(l, 0)).join('\n\n');
  const loaded = logs.filter((l) => !l.error).length || 1;
  const perLog = Math.floor((MAX_COMMENT_CHARS - head.length - skeleton.length - 500) / loaded);
  return `${head}\n${logs.map((l) => renderLog(l, perLog)).join('\n\n')}`;
}

// ---------- GitHub / orchestration ----------
async function fetchLogs(text) {
  return Promise.all(findLogIds(text).map(async (id) => {
    try { return { id, data: await fetchLog(id) }; }
    catch (err) { console.warn(`mclo.gs ${id}: ${err.message}`); return { id, error: err.message }; }
  }));
}

async function postComment(issueNumber, comment) {
  if (process.env.DRY_RUN) return console.log(comment);
  const api = process.env.GITHUB_API_URL ?? 'https://api.github.com';
  const res = await fetch(`${api}/repos/${process.env.GITHUB_REPOSITORY}/issues/${issueNumber}/comments`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${process.env.GITHUB_TOKEN}`,
      Accept: 'application/vnd.github+json',
      'X-GitHub-Api-Version': '2022-11-28',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ body: comment }),
  });
  if (!res.ok) throw new Error(`Failed to post comment: HTTP ${res.status} ${await res.text()}`);
  console.log(`Posted comment on #${issueNumber}`);
}

async function handleIssueOpened(issue) {
  const sections = parseIssueForm(issue.body);
  if (!(FIELDS.honeypot in sections) && !(FIELDS.mc in sections)) {
    return console.log('Issue does not use the bug report form; skipping.');
  }
  const support = checkSupport(sections, loadManifest(process.env.MANIFEST_JSON));
  console.log('Support check:', support);
  const logs = await fetchLogs(issue.body);
  await postComment(issue.number, buildComment({ support, logs }));
}

// True when the issue's type is "Bug", or (fallback) it carries a "bug" label.
export function isBugIssue(issue) {
  const isBug = (name) => String(name ?? '').trim().toLowerCase() === 'bug';
  return isBug(issue?.type?.name) || (issue?.labels ?? []).some((l) => isBug(typeof l === 'string' ? l : l?.name));
}

// Only reacts to the issue author's own comments on Bug issues, never to bots (prevents HoneyBot replying to itself).
export function shouldHandleComment(issue, comment) {
  if (!issue || !comment) return 'missing issue or comment';
  if (issue.pull_request) return 'comment is on a pull request';
  if (!isBugIssue(issue)) return 'issue is not of type Bug';
  if (comment.user?.type === 'Bot') return 'comment is from a bot';
  if (comment.user?.login !== issue.user?.login) return 'commenter is not the issue author';
  if (findLogIds(comment.body).length === 0) return 'no mclo.gs link in comment';
  return null;
}

// ---------- @honeypot-bot commands ----------
const BOT_MENTION = 'honeypot-bot';
const COMMANDS = ['logs', 'recheck', 'help'];
const TRUSTED_ASSOCIATIONS = ['OWNER', 'MEMBER', 'COLLABORATOR'];

// Removes quoted replies and code so quoting someone's command (or the help text) doesn't re-run it.
export function stripQuotesAndCode(markdown) {
  return (markdown ?? '').replace(/\r\n/g, '\n')
    .replace(/^(`{3,}|~{3,})[^\n]*\n[\s\S]*?^\1[^\n]*$/gm, '')
    .replace(/`[^`\n]*`/g, '')
    .split('\n').filter((line) => !/^\s*>/.test(line)).join('\n');
}

// Returns 'logs' | 'recheck' | 'help', or null if the bot wasn't mentioned.
// The first known command after the mention wins ("@honeypot-bot can you recheck?" -> recheck).
// A bare mention means "logs" if the comment has mclo.gs links, otherwise "help".
export function parseCommand(body) {
  const text = stripQuotesAndCode(body);
  const m = text.match(new RegExp(`(?:^|[^\\w@/.-])@${BOT_MENTION}(?![\\w-])([^\\n]*)`, 'i'));
  if (!m) return null;
  const words = m[1].toLowerCase().match(/[a-z]+/g) ?? [];
  return words.find((w) => COMMANDS.includes(w)) ?? (findLogIds(body).length ? 'logs' : 'help');
}

export function isTrusted(issue, comment) {
  return comment?.user?.login === issue?.user?.login || TRUSTED_ASSOCIATIONS.includes(comment?.author_association);
}

export function buildHelpComment() {
  return [
    '# HoneyBot commands 🍯',
    'Mention me in a comment, followed by one of these commands:',
    '',
    '| Command | What it does |',
    '|---|---|',
    '| `@honeypot-bot logs` | Pulls in the mclo.gs logs linked in your comment, or from the issue description if your comment has none. |',
    '| `@honeypot-bot recheck` | Re-checks your Honeypot and Minecraft versions, for example after you edit the issue. |',
    '| `@honeypot-bot help` | Shows this message. |',
    '',
    'Commands can be used by the person who opened the issue and by the Honeypot team.',
  ].join('\n');
}

export function buildRecheckComment(issue, manifest) {
  const sections = parseIssueForm(issue.body);
  if (!(FIELDS.honeypot in sections) && !(FIELDS.mc in sections)) {
    return "# Version recheck 🍯\nThis issue doesn't use the bug report form, so there are no versions for me to check.";
  }
  const support = checkSupport(sections, manifest);
  const parsed = `Honeypot \`${support.honeypot ?? 'unknown'}\` on Minecraft \`${support.server ?? 'unknown'}\``;
  const result = renderSupportAlert(support)
    ?? (support.result === 'supported'
      ? '✅ These versions are compatible. Thanks for confirming!'
      : "I couldn't find this Honeypot version's supported server versions, so I can't confirm compatibility.");
  return `# Version recheck 🍯\nI read: ${parsed}\n\n${result}`;
}

async function runCommand(command, issue, comment) {
  if (command === 'help') return postComment(issue.number, buildHelpComment());
  if (command === 'recheck') {
    return postComment(issue.number, buildRecheckComment(issue, loadManifest(process.env.MANIFEST_JSON)));
  }
  // logs: links in the comment first, falling back to the issue description.
  const source = findLogIds(comment.body).length ? comment.body : issue.body;
  if (findLogIds(source).length === 0) {
    return postComment(issue.number, [
      '# HoneyBot here again 🍯',
      "I couldn't find any mclo.gs links in your comment or the issue description.",
      '',
      'Upload your server logs to [https://mclo.gs](https://mclo.gs), then include the link with the command, like `@honeypot-bot logs https://mclo.gs/abc123`.',
    ].join('\n'));
  }
  return postComment(issue.number, buildLogsComment({ logs: await fetchLogs(source) }));
}

async function handleComment(issue, comment) {
  if (!issue || !comment) return console.log('Skipping: missing issue or comment.');
  if (issue.pull_request) return console.log('Skipping: comment is on a pull request.');
  if (comment.user?.type === 'Bot') return console.log('Skipping: comment is from a bot.');

  // A mention is always treated as a command, so it never also triggers the automatic log reply.
  const command = parseCommand(comment.body);
  if (command) {
    if (!isTrusted(issue, comment)) {
      return console.log(`Skipping: ${comment.user?.login} (${comment.author_association}) may not run commands.`);
    }
    console.log(`Running command: ${command}`);
    return runCommand(command, issue, comment);
  }

  const skip = shouldHandleComment(issue, comment);
  if (skip) return console.log(`Skipping: ${skip}.`);
  await postComment(issue.number, buildLogsComment({ logs: await fetchLogs(comment.body) }));
}

async function main() {
  const event = JSON.parse(fs.readFileSync(process.env.GITHUB_EVENT_PATH, 'utf8'));
  if (!event.issue) return console.log('No issue in event payload; exiting.');
  if (process.env.GITHUB_EVENT_NAME === 'issue_comment') return handleComment(event.issue, event.comment);
  return handleIssueOpened(event.issue);
}

if (import.meta.url === `file://${process.argv[1]}`) {
  main().catch((err) => { console.error(err); process.exit(1); });
}
