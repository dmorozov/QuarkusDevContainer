<!--
Sync Impact Report
===================
- Version change: 0.0.0 → 1.0.0 (MAJOR — initial ratification)
- Added principles:
  - I. Reactive-First
  - II. Single Source of Truth
  - III. Shared Domain as Library
  - IV. Modular Independence
- Added sections:
  - Technology Stack Constraints
- Removed sections:
  - [SECTION_3_NAME] (not needed per user input)
- Templates requiring updates:
  - .specify/templates/plan-template.md — ✅ no updates needed
    (Constitution Check section is generic; principles are
    evaluated dynamically)
  - .specify/templates/spec-template.md — ✅ no updates needed
    (template is technology-agnostic by design)
  - .specify/templates/tasks-template.md — ✅ no updates needed
    (task phases are generic; no principle-specific task types)
  - .specify/templates/commands/*.md — ✅ no commands directory exists
- Follow-up TODOs: none
-->

# BRIC Documents Constitution

## Core Principles

### I. Reactive-First

All I/O operations — database access, HTTP calls, and message
publishing — MUST use non-blocking reactive patterns (Mutiny `Uni`
and `Multi`). No blocking calls are permitted in service code.

- All REST endpoints MUST return `Uni<Response>` or `Multi<T>`.
- Database access MUST use Hibernate Reactive Panache.
- Message publishing MUST use SmallRye Reactive Messaging.
- Blocking operations (e.g., JAXB serialization) MUST be
  isolated and executed on a worker thread if unavoidable.

**Rationale**: The system uses Vert.x event-loop threads. A single
blocking call can stall the entire event loop, degrading throughput
for all concurrent requests.

### II. Single Source of Truth

The `Document` entity in PostgreSQL is the canonical state for all
document lifecycle data. Inter-service messages MUST carry only
the `documentId`; consumers MUST look up current state from the
database.

- Messages MUST use the `GenerateDocumentMessage` DTO containing
  only `documentId`.
- Services MUST NOT cache or duplicate document state locally
  beyond the scope of a single transaction.
- Status transitions (PENDING → GENERATED → DISPATCHED / FAILED)
  MUST be persisted to the `Document` entity before publishing
  downstream messages.

**Rationale**: Carrying full state in messages creates divergence
risk. A single database record eliminates reconciliation problems
and provides a reliable audit trail.

### III. Shared Domain as Library

All shared entities, DTOs, enums, converters, and base classes
MUST reside in `bric-core-domain`. This module is a plain Java
library — it MUST NOT contain service logic, configuration, or
a runnable main class.

- `bric-core-domain` MUST NOT depend on any Quarkus runtime
  extensions beyond API-level annotations (JPA, Bean Validation,
  JAXB).
- Services consume `bric-core-domain` via Gradle composite
  builds (`includeBuild` + `dependencySubstitution`).
- Changes to shared domain entities MUST be coordinated across
  all consuming services before merge.

**Rationale**: A shared domain library prevents entity drift
between services while keeping each service independently
buildable.

### IV. Modular Independence

Each service (`bric-documents`, `bric-documents-generate`,
`bric-documents-dispatch`) is a standalone Gradle project with
its own `build.gradle`, `settings.gradle`, and Gradle wrapper.
There is no root/parent project.

- Each service MUST be independently buildable and runnable.
- No service may directly depend on another service's source
  code — only on `bric-core-domain`.
- Each service owns its own `application.yml` and port
  assignment.
- Exactly one service (`bric-documents`) owns DDL via Hibernate
  `update` mode; all others MUST use `validate` mode.

**Rationale**: Independent projects allow services to evolve,
build, and deploy on separate timelines without coupling.

## Technology Stack Constraints

| Component          | Requirement                              |
|--------------------|------------------------------------------|
| Language           | Java 25                                  |
| Framework          | Quarkus 3.17+                            |
| Build tool         | Gradle (Groovy DSL), standalone projects |
| Database           | PostgreSQL 18, shared instance           |
| ORM                | Hibernate Reactive Panache               |
| Messaging          | SmallRye Reactive Messaging + RabbitMQ   |
| Validation         | Bean Validation (Hibernate Validator)    |
| XML serialization  | JAXB (`jakarta.xml.bind`)                |
| Config format      | YAML (`application.yml`)                 |
| Testing            | JUnit 5, REST Assured, TestContainers    |

- Dependencies MUST be managed via the Quarkus BOM
  (`io.quarkus.platform:quarkus-bom`).
- New dependencies MUST NOT introduce blocking I/O drivers
  unless explicitly justified in a complexity tracking record.

## Governance

This constitution defines the non-negotiable principles for the
BRIC Documents project. It will be reviewed and updated as the
project evolves.

- All code changes MUST comply with the principles above.
- Amendments to this constitution MUST be documented with a
  version bump, rationale, and migration plan for affected code.
- Versioning follows semantic versioning: MAJOR for principle
  removals/redefinitions, MINOR for additions/expansions,
  PATCH for clarifications.

**Version**: 1.0.1 | **Ratified**: 2026-03-27 | **Last Amended**: 2026-03-27
