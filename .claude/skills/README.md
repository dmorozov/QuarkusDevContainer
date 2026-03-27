# Skills

Skills are reusable prompts that teach Claude specific patterns for Java development.

## Structure Convention

Each skill folder contains:

| File | Purpose | Audience |
|------|---------|----------|
| `SKILL.md` | Instructions for Claude | AI (loaded with `view`) 

## Available Skills

### Workflow
| Skill | Description |
|-------|-------------|
| [git-commit](git-commit/) | Conventional commit messages for Java projects |
| [changelog-generator](changelog-generator/) | Generate changelogs from git commits |
| [issue-triage](issue-triage/) | GitHub issue triage and categorization |

### Code Quality
| Skill | Description |
|-------|-------------|
| [java-code-review](java-code-review/) | Systematic Java code review checklist |
| [api-contract-review](api-contract-review/) | REST API audit: HTTP semantics, versioning, compatibility |
| [concurrency-review](concurrency-review/) | Thread safety, race conditions, @Async, Virtual Threads |
| [performance-smell-detection](performance-smell-detection/) | Code-level performance smells (streams, boxing, regex) |
| [test-quality](test-quality/) | JUnit 5 + AssertJ testing patterns |
| [maven-dependency-audit](maven-dependency-audit/) | Audit dependencies for updates and vulnerabilities |
| [security-audit](security-audit/) | OWASP Top 10, input validation, injection prevention |

### Architecture & Design
| Skill | Description |
|-------|-------------|
| [architecture-review](architecture-review/) | Macro-level review: packages, modules, layers, boundaries |
| [solid-principles](solid-principles/) | S.O.L.I.D. principles with Java examples |
| [design-patterns](design-patterns/) | Factory, Builder, Strategy, Observer, Decorator, etc. |
| [clean-code](clean-code/) | DRY, KISS, YAGNI, naming, refactoring |

### Framework & Data
| Skill | Description |
|-------|-------------|
| [spring-boot-patterns](spring-boot-patterns/) | Spring Boot best practices |
| [java-migration](java-migration/) | Java version upgrade guide (8→11→17→21) |
| [jpa-patterns](jpa-patterns/) | JPA/Hibernate patterns (N+1, lazy loading, transactions) |
| [logging-patterns](logging-patterns/) | Structured logging (JSON), SLF4J, MDC, AI-friendly formats |

## Extras

### Planning & Design

These skills help you think through problems before writing code.

- **write-a-prd** — Create a PRD through an interactive interview, codebase exploration, and module design. Filed as a GitHub issue.

  ```
  npx skills@latest add mattpocock/skills/write-a-prd
  ```

- **prd-to-plan** — Turn a PRD into a multi-phase implementation plan using tracer-bullet vertical slices.

  ```
  npx skills@latest add mattpocock/skills/prd-to-plan
  ```

- **prd-to-issues** — Break a PRD into independently-grabbable GitHub issues using vertical slices.

  ```
  npx skills@latest add mattpocock/skills/prd-to-issues
  ```

- **grill-me** — Get relentlessly interviewed about a plan or design until every branch of the decision tree is resolved.

  ```
  npx skills@latest add mattpocock/skills/grill-me
  ```

- **request-refactor-plan** — Create a detailed refactor plan with tiny commits via user interview, then file it as a GitHub issue.

  ```
  npx skills@latest add mattpocock/skills/request-refactor-plan
  ```

### Development

These skills help you write, refactor, and fix code.

- **tdd** — Test-driven development with a red-green-refactor loop. Builds features or fixes bugs one vertical slice at a time.

  ```
  npx skills@latest add mattpocock/skills/tdd
  ```

- **triage-issue** — Investigate a bug by exploring the codebase, identify the root cause, and file a GitHub issue with a TDD-based fix plan.

  ```
  npx skills@latest add mattpocock/skills/triage-issue
  ```

- **improve-codebase-architecture** — Explore a codebase for architectural improvement opportunities, focusing on deepening shallow modules and improving testability.

  ```
  npx skills@latest add mattpocock/skills/improve-codebase-architecture
  ```

### Tooling & Setup

- **git-guardrails-claude-code** — Set up Claude Code hooks to block dangerous git commands (push, reset --hard, clean, etc.) before they execute.

  ```
  npx skills@latest add mattpocock/skills/git-guardrails-claude-code
  ```

## Learn More

- [Claude Code Skills Documentation](https://code.claude.com/docs/en/skills) - Official guide on creating and using skills
