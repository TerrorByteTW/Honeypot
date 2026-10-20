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

const MSG = {
  supported: 'This version of Honeypot is supported! That helps a lot, thanks!',
  noHoneypot: 'Could not determine the version of Honeypot',
  noServer: 'Could not determine the version of the server',
  unknownSupport: 'Could not determine if Honeypot supports this server version',
  unsupported: "Honeypot isn't confirmed to support this version of MC. Support may be limited.",
};

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

  let status;
  if (!honeypot) status = MSG.noHoneypot;
  else if (!server) status = MSG.noServer;
  else {
    const entry = findManifestEntry(manifest, honeypot);
    if (!entry || (!entry.min && !entry.max)) status = MSG.unknownSupport;
    else status = isInRange(server, entry.min, entry.max) ? MSG.supported : MSG.unsupported;
  }
  return { honeypot, server, status };
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

  const info = [
    '',
    '# Server Information',
    `Parsed Honeypot Version: ${support.honeypot ?? 'Unknown'}`,
    `Parsed Server Version: ${support.server ?? 'Unknown'}`,
    support.status,
    '',
    '# Logs',
  ];

  const head = [...intro, ...info].join('\n');
  if (logs.length === 0) return `${head}\n_No logs provided yet._`;

  // Render everything without raw logs first, then share the remaining space between them.
  const skeleton = logs.map((l) => renderLog(l, 0)).join('\n\n');
  const loaded = logs.filter((l) => !l.error).length || 1;
  const perLog = Math.floor((MAX_COMMENT_CHARS - head.length - skeleton.length - 500) / loaded);
  return `${head}\n${logs.map((l) => renderLog(l, perLog)).join('\n\n')}`;
}

// ---------- Main ----------
async function main() {
  const event = JSON.parse(fs.readFileSync(process.env.GITHUB_EVENT_PATH, 'utf8'));
  const issue = event.issue;
  if (!issue) return console.log('No issue in event payload; exiting.');

  const sections = parseIssueForm(issue.body);
  if (!(FIELDS.honeypot in sections) && !(FIELDS.mc in sections)) {
    return console.log('Issue does not use the bug report form; skipping.');
  }

  const support = checkSupport(sections, loadManifest(process.env.MANIFEST_JSON));
  console.log('Support check:', support);

  const logs = await Promise.all(findLogIds(issue.body).map(async (id) => {
    try { return { id, data: await fetchLog(id) }; }
    catch (err) { console.warn(`mclo.gs ${id}: ${err.message}`); return { id, error: err.message }; }
  }));

  const comment = buildComment({ support, logs });

  if (process.env.DRY_RUN) return console.log(comment);

  const api = process.env.GITHUB_API_URL ?? 'https://api.github.com';
  const res = await fetch(`${api}/repos/${process.env.GITHUB_REPOSITORY}/issues/${issue.number}/comments`, {
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
  console.log(`Posted comment on #${issue.number}`);
}

if (import.meta.url === `file://${process.argv[1]}`) {
  main().catch((err) => { console.error(err); process.exit(1); });
}
