# Research: BRIC Document Processing Pipeline

**Branch**: `001-bric-document-pipeline` | **Date**: 2026-03-27

## R1: Quarkus 3.34.1 with Java 25 Compatibility

**Decision**: Use Quarkus BOM `3.34.1` with Java 25 source/target compatibility.

**Rationale**: The devcontainer runtime is Java 25. Quarkus 3.34.x supports Java 25. Using the matching version avoids bytecode compatibility issues and leverages the latest language features.

**Alternatives considered**:
- Java 21 with Quarkus 3.17.x (original reference plan): Rejected — older than devcontainer runtime, no benefit since we're greenfield.

## R2: Gradle Composite Builds for Multi-Module

**Decision**: Use `includeBuild('../bric-core-domain')` with `dependencySubstitution` in each service's `settings.gradle`.

**Rationale**: Gradle composite builds allow each project to remain independently buildable while resolving the shared domain library from local source. No need for `publishToMavenLocal` during development. Each service declares `implementation 'com.bric:bric-core-domain:1.0.0-SNAPSHOT'` and Gradle transparently substitutes the local project.

**Alternatives considered**:
- Single multi-project build with root `settings.gradle`: Rejected — violates Constitution Principle IV (Modular Independence).
- `publishToMavenLocal` + `mavenLocal()`: Rejected — fragile, requires manual publish step, stale artifacts risk.

## R3: SmallRye Reactive Messaging with RabbitMQ

**Decision**: Use `quarkus-messaging-rabbitmq` extension for message-driven communication between services.

**Rationale**: SmallRye Reactive Messaging provides annotation-driven `@Incoming`/`@Outgoing` with auto-declare for queues/exchanges. RabbitMQ is a mature, well-supported broker. The extension handles connection management, serialization, and acknowledgment automatically.

**Alternatives considered**:
- Kafka via `quarkus-messaging-kafka`: Rejected — heavier infrastructure for this use case; RabbitMQ is sufficient for point-to-point queue pattern.
- Direct AMQP client: Rejected — too low-level; SmallRye provides the reactive integration for free.

## R4: Hibernate Reactive Panache Entity Pattern

**Decision**: Use `PanacheEntityBase` (active record pattern) with public fields for the `Document` entity.

**Rationale**: Panache active record reduces boilerplate. `PanacheEntityBase` (not `PanacheEntity`) allows custom ID generation via `@SequenceGenerator`. Public fields are the Panache convention — no getters/setters needed.

**Alternatives considered**:
- Repository pattern (`PanacheRepository`): Rejected — adds unnecessary abstraction layer for this simple CRUD use case.
- `PanacheEntity` with auto-generated Long ID: Rejected — we need `@SequenceGenerator` for database sequence control.

## R5: JAXB for Metadata Serialization

**Decision**: Use JAXB (`jakarta.xml.bind`) to serialize `DocumentMetadata` to XML, stored as a string in the `Document.metadata` column.

**Rationale**: XML is the required format per the reference plan. JAXB is the standard Jakarta EE approach. Serialization is CPU-bound and fast — no reactive concern for typical metadata sizes.

**Alternatives considered**:
- JSON serialization (Jackson): Would be simpler but the reference plan explicitly uses XML format.
- Store metadata as JSON column type: Would require schema change and different serialization approach.

**Constitution note**: JAXB `marshal`/`unmarshal` are blocking calls. Per Principle I (Reactive-First), these MUST be executed before entering the reactive chain (i.e., serialize to string synchronously, then pass the string into the reactive `persist()` pipeline).

## R6: Duplicate Message Handling (Status Guard)

**Decision**: Check document status before processing; skip if already at or past target status.

**Rationale**: Per clarification session — with at-least-once delivery, consumers may receive duplicate messages. Status guard prevents re-processing and avoids publishing duplicate downstream messages.

**Implementation approach**:
- Generation consumer: Check `document.status != PENDING` → log skip, return.
- Dispatch consumer: Check `document.status != GENERATED` → log skip, return.
- Use ordinal comparison on `DataStatus` enum for "at or past" logic.

## R7: Error Handling and FAILED Status

**Decision**: Any exception during generation or dispatch processing sets `Document.status = FAILED`.

**Rationale**: Per clarification session — simple and predictable. In this version where generation and dispatch are stubs, exceptions would indicate infrastructure issues (DB failure, serialization errors). Wrapping consumer processing in try-catch and persisting FAILED status ensures the document lifecycle is terminal and visible.

**Implementation approach**:
- Wrap consumer logic in `onFailure()` handler on the Mutiny chain.
- In the failure handler: load document, set `status = FAILED`, persist, log error.
- Do NOT rethrow — acknowledge the message to prevent infinite retry loops.

## R8: RabbitMQ Infrastructure

**Decision**: Add RabbitMQ 4 (management-alpine) to devcontainer `docker-compose.yml`.

**Rationale**: Required for inter-service messaging. Management UI on port 15672 provides operational visibility during development.

**Configuration**:
- Ports: 5672 (AMQP), 15672 (Management UI)
- Credentials: guest/guest (dev only)
- Health check: `rabbitmq-diagnostics -q ping`
- Queue auto-declaration via SmallRye (no manual setup needed)
