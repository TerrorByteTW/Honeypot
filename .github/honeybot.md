# HoneyBot 🍯

HoneyBot is a GitHub Actions bot that helps triage bug reports for Honeypot. When someone opens an issue, it asks them for debug logs, checks whether their Honeypot and Minecraft versions are compatible, and pulls in any server logs they've shared on [mclo.gs](https://mclo.gs). Afterward, the reporter and the Honeypot team can ask it for more help by mentioning `@honeypot-bot` in a comment.

HoneyBot needs no server or hosting. It runs entirely on GitHub Actions and has no npm dependencies.

## Features

### New issue triage

When an issue is opened using the bug report form, HoneyBot posts a comment that:

- Thanks the reporter and explains how to turn on Honeypot's debug logging (`logging.enable-logging` and `logging.minimum-log-level`), then reproduce the issue and share the results.
- Asks crash reporters to upload their server logs to mclo.gs. This request is left out if the issue already includes an mclo.gs link.
- Adds a compatibility alert when there's a problem with the reported versions (see below).
- Includes a **Logs** section with any mclo.gs logs linked in the issue description.

Issues that don't use the bug report form are ignored.

### Version compatibility check

HoneyBot reads two fields from the bug report form: **What version of Honeypot are you using?** and **MC Server Version**. Each field can contain extra text, so `5.1.3`, `v5.1.3`, `Minecraft 26.3`, and `26.3.1` all work. HoneyBot takes the first dotted version number it finds.

It then looks up the Honeypot version in `version_manifest.yml` and compares the server version against that release's `supported-server-versions`. Both `min` and `max` are inclusive and optional. A `max` is compared only as far as its own number of segments goes, so `"26.3"` covers every 26.3.x hotfix.

| Result | What HoneyBot adds |
|---|---|
| The versions are compatible | Nothing |
| A version couldn't be read | A **WARNING** alert naming which version (Honeypot, the server, or both) |
| The versions aren't compatible | A **CAUTION** alert: "Unsupported Configuration" |
| The Honeypot version isn't in the manifest, or has no supported server versions (such as a pulled release) | Nothing |

### mclo.gs log parsing

For each mclo.gs link it finds (up to 3 per comment), HoneyBot fetches the log from the mclo.gs API and adds a section under **Logs** containing:

- **Detected problems:** a table listing each problem mclo.gs found and its suggested solutions.
- **Detected information:** the server software and its version, such as Paper 26.2.
- **The raw log**, in a collapsed section so it doesn't take over the comment.

If mclo.gs can't identify the type of log (it reports `unknown/unknown` or `Unknown Log`), HoneyBot says so and shows only the raw log.

GitHub limits comments to about 65,000 characters. When a log is too long to fit, HoneyBot keeps the *end* of the log, where crashes and stack traces usually appear, and links to the full log. If a log can't be loaded, for example because it expired or was deleted, HoneyBot says so instead of failing.

### Automatic log replies

When the issue author later posts a comment containing an mclo.gs link on a **Bug** issue, HoneyBot replies with the parsed logs automatically. No mention is needed. An issue counts as a Bug issue if its GitHub issue type is "Bug" or it has a `bug` label.

### Commands

Mention `@honeypot-bot` in a comment, followed by a command:

| Command | What it does |
|---|---|
| `@honeypot-bot logs` | Parses the mclo.gs links in the comment. If the comment has none, it uses the links in the issue description. |
| `@honeypot-bot recheck` | Re-runs the version check against the issue as it is now, for example after the reporter fixes a typo. |
| `@honeypot-bot help` | Lists the available commands. |

Commands can be written naturally: in "@honeypot-bot can you recheck this?", the first command word after the mention is used. A bare mention runs `logs` if the comment contains an mclo.gs link, and `help` otherwise. Any other text after the mention also gets the help message.

**Who can use commands:** the person who opened the issue, plus anyone GitHub lists as an `OWNER`, `MEMBER`, or `COLLABORATOR` of the repository. Mentions from anyone else are ignored without a reply.

Commands work on any issue, but not on pull requests. A comment that mentions the bot is always handled as a command, so it never also triggers the automatic log reply.

### Safety

- **No shell injection:** issue and comment text is read from the event file and never inserted into shell commands.
- **No unwanted pings:** `@mentions` and HTML inside log insights are neutralized, so the bot can't notify anyone.
- **No self-replies:** comments from bots are ignored, so HoneyBot never answers itself.
- **No accidental reruns:** mentions inside quoted replies (`> ...`) and code blocks are ignored, so quoting a command doesn't run it again.

## Setup

### 1. Add the files

| File | Purpose |
|---|---|
| `.github/workflows/honeybot.yml` | The workflow. It runs when an issue is opened and when a comment is created. |
| `.github/scripts/honeybot.mjs` | The bot's logic. |
| `version_manifest.yml` (repo root) | The release manifest used for the compatibility check. |

The workflow converts the manifest to JSON using `yq`, which is preinstalled on GitHub's Ubuntu runners. If the manifest is missing or invalid, HoneyBot still runs and skips the compatibility check.

### 2. Tag bug reports in the issue template (recommended)

Automatic log replies only run on Bug issues. To have bug reports tagged automatically, add `type: Bug` (or `labels: ["bug"]`) at the top level of your bug report form, for example `.github/ISSUE_TEMPLATE/bug_report.yml`.

### 3. Post as the Honeypot app (optional)

By default, comments appear as `github-actions`. To post under your own app's name and avatar:

1. **Create a GitHub App** with Issues set to *Read and write*. A webhook isn't needed.
2. **Install the app** on the repository and generate a private key.
3. **Add a variable and a secret** under the repository's **Settings → Secrets and variables → Actions**:
   - Variable: `HONEYBOT_APP_ID`, set to the app's ID.
   - Secret: `HONEYBOT_APP_PRIVATE_KEY`, set to the full contents of the `.pem` key file.

If `HONEYBOT_APP_ID` isn't set, the workflow falls back to the default `github-actions` token.

## Configuration

### Version manifest

HoneyBot reads these fields from each entry in `versions`:

```yaml
versions:
  - version: "5.1.3"
    supported-server-versions:
      min: "26.1"
      max: "26.3"
```

All other fields are ignored. Always quote version numbers: YAML turns an unquoted `1.20` into the number `1.2`.

### Script settings

These settings are constants near the top of `honeybot.mjs` and in its command section.

| Setting | Default | Description |
|---|---|---|
| `FIELDS` | Honeypot and MC version labels | The bug report form headings HoneyBot reads, in lowercase. Update these if you rename those form fields. |
| `MAX_LOGS` | `3` | The maximum number of mclo.gs links parsed per issue or comment. |
| `MAX_COMMENT_CHARS` | `65000` | The size budget for a comment. GitHub's limit is 65,536. |
| `BOT_MENTION` | `honeypot-bot` | The handle that triggers commands, without the `@`. |
| `COMMANDS` | `logs`, `recheck`, `help` | The recognized command words. |
| `TRUSTED_ASSOCIATIONS` | `OWNER`, `MEMBER`, `COLLABORATOR` | The repository relationships, besides the issue author, that may run commands. |

If you change `BOT_MENTION`, also update the `contains(github.event.comment.body, '@honeypot-bot')` check in `honeybot.yml`.

### Comment text

Each type of comment is built by its own function in `honeybot.mjs`, so its wording can be edited in one place:

| Function | Builds |
|---|---|
| `buildComment` | The new issue comment |
| `renderSupportAlert` | The WARNING and CAUTION alerts |
| `buildLogsComment` | Log replies |
| `buildRecheckComment` | `recheck` replies |
| `buildHelpComment` | `help` replies |

## Testing locally

Set `DRY_RUN=1` to print a comment instead of posting it:

```bash
DRY_RUN=1 \
GITHUB_EVENT_NAME=issues \
GITHUB_EVENT_PATH=./sample-event.json \
MANIFEST_JSON=./manifest.json \
node .github/scripts/honeybot.mjs
```

- For `GITHUB_EVENT_PATH`, use a saved webhook payload. You can copy one from a workflow run, or from **Settings → Webhooks → Recent Deliveries**.
- To test comments and commands, set `GITHUB_EVENT_NAME=issue_comment`.
- To create the manifest JSON, run `yq -o=json '.' version_manifest.yml > manifest.json`.

## Troubleshooting

When HoneyBot doesn't reply, its workflow run log in the **Actions** tab usually explains why. Each skipped run prints a reason, such as "commenter is not the issue author" or "may not run commands."

| Problem | What to check |
|---|---|
| No workflow run appears at all | The comment didn't pass the workflow's `if` filter. It might be on a pull request, from a bot, or neither a mention nor an author log comment on a Bug issue. |
| Automatic log replies never run on Bug issues | Make sure the issue actually has the Bug type or a `bug` label. HoneyBot reads the type from `github.event.issue.type.name`, so confirm that field appears in the event payload for your repository. |
| The version check always says it can't read the version | Make sure the form headings still match `FIELDS`. |
| The compatibility result seems wrong | Make sure the manifest's version numbers are quoted. |
| A log is missing | The mclo.gs log may have expired or been deleted. HoneyBot notes this in its comment. |
