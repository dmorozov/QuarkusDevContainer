# Implementation Plan: BRIC Document Processing Pipeline

**Branch**: `001-bric-document-pipeline` | **Date**: 2026-03-27 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-bric-document-pipeline/spec.md`

## Summary

Build an asynchronous document processing pipeline consisting of four Gradle modules: a shared domain library (`bric-core-domain`) and three Quarkus microservices (`bric-documents`, `bric-documents-generate`, `bric-documents-dispatch`). The pipeline accepts payment receipt requests via REST API, persists a Document entity, and progresses it through PENDING → GENERATED → DISPATCHED via RabbitMQ message queues. Generation and dispatch stages are stub implementations in this version. Infrastructure additions include RabbitMQ in the devcontainer and a database sequence for document IDs.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Quarkus 3.34.1, Hibernate Reactive Panache, SmallRye Reactive Messaging (RabbitMQ), JAXB, Bean Validation (Hibernate Validator)
**Storage**: PostgreSQL 18 (shared instance, already in devcontainer)
**Testing**: JUnit 5, REST Assured, TestContainers (for integration tests)
**Target Platform**: Linux server (devcontainer)
**Project Type**: Multi-module microservices (4 standalone Gradle projects)
**Performance Goals**: <2s API response time, <30s end-to-end pipeline, 50+ concurrent requests
**Constraints**: Non-blocking reactive I/O only, at-least-once message delivery, strict forward-only status transitions
**Scale/Scope**: Single-instance services, shared database, development/MVP stage

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evidence |
|-----------|--------|----------|
| I. Reactive-First | ✅ PASS | All endpoints return `Uni<Response>`. DB via Hibernate Reactive Panache. Messaging via SmallRye Reactive Messaging. JAXB serialization (blocking) isolated before reactive chain. |
| II. Single Source of Truth | ✅ PASS | Messages carry only `documentId`. Consumers look up `Document` from PostgreSQL. Status persisted before downstream publish. |
| III. Shared Domain as Library | ✅ PASS | `bric-core-domain` is a `java-library` project with no Quarkus runtime deps. Only API-level annotations (JPA, Bean Validation, JAXB). Consumed via `includeBuild`. |
| IV. Modular Independence | ✅ PASS | Four standalone Gradle projects with own `build.gradle`, `settings.gradle`, Gradle wrapper. No root project. `bric-documents` owns DDL (`update`); others use `validate`. |

**Gate result**: All principles satisfied. No violations to justify.

## Project Structure

### Documentation (this feature)

```text
specs/001-bric-document-pipeline/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── rest-api.md      # REST API contract
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
bric-core-domain/
├── build.gradle
├── settings.gradle
├── gradle/wrapper/
├── gradlew, gradlew.bat
└── src/main/java/com/bric/core/domain/
    ├── entity/
    │   ├── UpdatableEntity.java
    │   └── Document.java
    ├── enums/
    │   ├── DocumentType.java
    │   ├── CorrespondenceType.java
    │   └── DataStatus.java
    ├── converter/
    │   └── EnumListConverter.java
    └── dto/
        ├── DocumentMetadata.java
        └── GenerateDocumentMessage.java

bric-documents/
├── build.gradle
├── settings.gradle
├── gradle/wrapper/
├── gradlew, gradlew.bat
└── src/
    ├── main/
    │   ├── java/com/bric/documents/
    │   │   ├── rest/
    │   │   │   └── DocumentsResource.java
    │   │   ├── service/
    │   │   │   └── DocumentsService.java
    │   │   ├── dto/
    │   │   │   └── PaymentReceiptRequest.java
    │   │   └── validation/
    │   │       ├── ValidPaymentReceiptRequest.java
    │   │       └── PaymentReceiptRequestValidator.java
    │   └── resources/
    │       └── application.yml
    └── test/java/com/bric/documents/

bric-documents-generate/
├── build.gradle
├── settings.gradle
├── gradle/wrapper/
├── gradlew, gradlew.bat
└── src/
    ├── main/
    │   ├── java/com/bric/documents/generate/
    │   │   └── consumer/
    │   │       └── DocumentRequestConsumer.java
    │   └── resources/
    │       └── application.yml
    └── test/java/

bric-documents-dispatch/
├── build.gradle
├── settings.gradle
├── gradle/wrapper/
├── gradlew, gradlew.bat
└── src/
    ├── main/
    │   ├── java/com/bric/documents/dispatch/
    │   │   └── consumer/
    │   │       └── DocumentSendRequestConsumer.java
    │   └── resources/
    │       └── application.yml
    └── test/java/
```

**Structure Decision**: Four sibling Gradle projects at the repository root. No parent/root project. Cross-project dependency on `bric-core-domain` via Gradle composite builds (`includeBuild` + `dependencySubstitution`). This matches Constitution Principle IV (Modular Independence).

## Complexity Tracking

> No violations detected. Constitution Check passed on all principles.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| JAXB blocking serialization | Required for XML metadata format per FR-008 | Isolated before reactive chain; executed synchronously during request thread before `persist()` call returns `Uni`. Acceptable tradeoff — serialization is CPU-bound and fast (<1ms). |
