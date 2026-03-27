# Template Quarkus project


## SpecKit

Spec-Driven Development flips the script on traditional software development. For decades, code has been king — specifications were just scaffolding we built and discarded once the "real work" of coding began. Spec-Driven Development changes this: specifications become executable, directly generating working implementations rather than just guiding them.

Note: the GitHub SpecKit works with any Cli AI tools (Claude Code, Gemini CLI, OpenAI Codex etc.)

### Installation

```bash
Install uv tool:
curl -LsSf https://astral.sh/uv/install.sh | sh

Install SpecKit:
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git

or upgrade:
uv tool install specify-cli --force --from git+https://github.com/github/spec-kit.git
```

then:

```bash
specify init .
specify check
```

Documentation:
https://github.com/github/spec-kit

Specify commands available:

1. /speckit.constitution - Establish project principles
2. /speckit.specify - Create baseline specification
3. /speckit.clarify (optional) - Ask structured questions to de-risk ambiguous areas before planning (run before /speckit.plan if used)
4. /speckit.plan - Create implementation plan
5. /speckit.checklist (optional) - Generate quality checklists to validate requirements completeness, clarity, and consistency (after /speckit.plan)
6. /speckit.tasks - Generate actionable tasks182572
7. /speckit.analyze (optional) - Cross-artifact consistency & alignment report (after /speckit.tasks, before /speckit.implement)
8. /speckit.implement - Execute implementation


## Claude Code

### Installation:

```bash
$ curl -fsSL https://claude.ai/install.sh | bash
```

To use:

```bash
$ claude
or
$ claude --dangerously-skip-permissions
```

### Add Quarkus MCP server
Add quarkus dev MCP server
claude mcp add --scope project quarkus-agent -- jbang quarkus-agent-mcp@quarkusio

### Work on some plan

@examples/skeleton-plan.md /grill-me I'd like to think about implementing this.

Note: "grill-me" is pretty useful skill will ask you a LOT of questions to prepare a comprehensive plan.
At the end you can ask it to store the plan into a file and use it file with Claude's plan mode to implement.

### MCP Servers

Install uv tool (it is installed for others):
```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
uv venv
```

### PostgreSQL Pro MCP

Install the PostgreSQL mcp server
```bash
uv pip install postgres-mcp
```
Configure in the project (already done)

```json
{
  "mcpServers": {
    "postgres": {
      "command": "uvx",
      "args": [
        "postgres-mcp",
        "--access-mode=unrestricted"
      ],
      "env": {
        "DATABASE_URI": "postgresql://username:password@localhost:5432/dbname"
      }
    }
  }
}
```

### Context7 - up to date documentation

claude mcp add --transport http context7 https://mcp.context7.com/mcp \
  --header "CONTEXT7_API_KEY: YOUR_KEY"
