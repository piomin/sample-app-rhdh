#  Demo Project [![Twitter](https://img.shields.io/twitter/follow/piotr_minkowski.svg?style=social&logo=twitter&label=Follow%20Me)](https://twitter.com/piotr_minkowski)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/dashboard?id=piomin_sample-app-rhdh)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-app-rhdh&metric=bugs)](https://sonarcloud.io/dashboard?id=piomin_sample-app-rhdh)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-app-rhdh&metric=coverage)](https://sonarcloud.io/dashboard?id=piomin_sample-app-rhdh)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-app-rhdh&metric=ncloc)](https://sonarcloud.io/dashboard?id=piomin_sample-app-rhdh)

In this project I'm demonstrating you the most interesting features of [Backstage](https://backstage.io/) for generating app skeletons. \
This skeleton was generated automatically from the following [template](https://github.com/piomin/backstage-templates/blob/master/templates/spring-boot-basic/template.yaml).

## AI-assisted reviews with Claude

This repository is integrated with the [Claude GitHub app](https://github.com/anthropics/claude-code-action) so that Claude can analyze issues and pull requests automatically. The integration is driven by two GitHub Actions workflows that live in [`.github/workflows`](.github/workflows).

### What the app is doing right now

| Workflow | File | Trigger | What it does |
| --- | --- | --- | --- |
| **Claude Code** | [`claude.yml`](.github/workflows/claude.yml) | An issue, issue comment, PR review or PR review comment that contains `@claude` | Claude reads the request together with the repository code, then answers questions, performs code reviews or implements changes on a dedicated branch and opens a pull request. |
| **Claude Code Review** | [`claude-code-review.yml`](.github/workflows/claude-code-review.yml) | A pull request is `opened`, `reopened`, `synchronize`d or marked `ready_for_review` | Claude automatically runs the `code-review` plugin against the pull request and posts review feedback — no manual mention required. |

In short: mention `@claude` anywhere in an issue or PR to ask for help on demand, and every new/updated pull request additionally receives an automatic code review.

### How to install and enable the Claude app

1. **Install the GitHub app.** Visit [github.com/apps/claude](https://github.com/apps/claude) (or run `/install-github-app` from the [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI) and grant it access to this repository. The app needs read access to the repository contents, issues and pull requests.
2. **Add your Anthropic API key.** Create a repository secret named `ANTHROPIC_API_KEY` under **Settings → Secrets and variables → Actions → New repository secret**. You can generate a key from the [Anthropic Console](https://console.anthropic.com/). Both workflows read the key from `${{ secrets.ANTHROPIC_API_KEY }}`.
3. **Add the workflows.** Make sure the two workflow files under [`.github/workflows`](.github/workflows) are present on your default branch (they already are in this repository). GitHub Actions must be enabled for the repository under **Settings → Actions → General**.
4. **Verify the setup.** Open a test issue containing `@claude say hello` — the **Claude Code** workflow should start and Claude will reply in a comment. Opening a pull request should trigger the **Claude Code Review** workflow automatically.

### How to use the Claude plugin with this app

The automatic review workflow uses Claude Code's plugin mechanism instead of a hand-written prompt. In [`claude-code-review.yml`](.github/workflows/claude-code-review.yml) the relevant inputs are:

```yaml
- name: Run Claude Code Review
  uses: anthropics/claude-code-action@v1
  with:
    anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
    plugin_marketplaces: 'https://github.com/anthropics/claude-code.git'
    plugins: 'code-review@claude-code-plugins'
    prompt: '/code-review:code-review ${{ github.repository }}/pull/${{ github.event.pull_request.number }}'
```

- `plugin_marketplaces` points to the plugin marketplace to load — here the official [`anthropics/claude-code`](https://github.com/anthropics/claude-code) repository.
- `plugins` selects which plugin to enable, using the `<plugin>@<marketplace>` syntax (`code-review@claude-code-plugins`).
- `prompt` invokes the slash command exposed by the plugin (`/code-review:code-review`) and passes the pull request reference so Claude knows what to review.

To use a different plugin, change the `plugins` value (and `plugin_marketplaces` if it lives in another marketplace) and invoke its slash command from the `prompt`. The same `claude_args`/`plugins` inputs can also be added to [`claude.yml`](.github/workflows/claude.yml) if you want the on-demand `@claude` workflow to load plugins as well. See the [claude-code-action usage docs](https://github.com/anthropics/claude-code-action/blob/main/docs/usage.md) for the full list of available options.