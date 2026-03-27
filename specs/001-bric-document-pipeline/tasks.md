# Tasks: BRIC Document Processing Pipeline

**Input**: Design documents from `/specs/001-bric-document-pipeline/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Not explicitly requested in the feature specification. Test tasks are omitted.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Multi-module microservices**: Four standalone Gradle projects at repository root
  - `bric-core-domain/` — shared domain library
  - `bric-documents/` — REST API service (port 8080)
  - `bric-documents-generate/` — generation consumer service (port 8081)
  - `bric-documents-dispatch/` — dispatch consumer service (port 8082)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Devcontainer infrastructure changes and Gradle project scaffolding for all four modules

- [ ] T001 Add RabbitMQ service to `.devcontainer/docker-compose.yml`. Add a `quarkus-demo-rabbitmq` service using image `rabbitmq:4-management-alpine`, exposing ports 5672 (AMQP) and 15672 (Management UI), with environment variables `RABBITMQ_DEFAULT_USER=guest` and `RABBITMQ_DEFAULT_PASS=guest`, and a health check running `rabbitmq-diagnostics -q ping` (interval 5s, timeout 5s, retries 5). Add `depends_on` with `condition: service_healthy` to the `quarkus-demo-app` service so it waits for RabbitMQ to be ready. Also add RabbitMQ environment variables (`RABBITMQ_HOST: quarkus-demo-rabbitmq`, `RABBITMQ_PORT: 5672`, `RABBITMQ_USERNAME: guest`, `RABBITMQ_PASSWORD: guest`) to the `quarkus-demo-app` service's environment section.

- [ ] T002 Update `.devcontainer/devcontainer.json` to add forwarded ports for RabbitMQ AMQP (5672), RabbitMQ Management UI (15672), bric-documents-generate (8081), and bric-documents-dispatch (8082). Add `portsAttributes` entries with labels: "RabbitMQ AMQP" (5672, silent), "RabbitMQ Management" (15672, notify), "Quarkus Generate" (8081, notify), "Quarkus Dispatch" (8082, notify).

- [ ] T003 Add `DOCUMENT_SEQ` sequence to `.devcontainer/init.sql`. Append the statement `CREATE SEQUENCE IF NOT EXISTS "DOCUMENT_SEQ" START WITH 1 INCREMENT BY 1;` to the existing init.sql file. This sequence is used by the `Document` entity's `@SequenceGenerator` for primary key generation.

- [ ] T004 Create `bric-core-domain/` project skeleton. Create `bric-core-domain/settings.gradle` with `rootProject.name = 'bric-core-domain'`. Create `bric-core-domain/build.gradle` as a `java-library` plugin project: group `com.bric`, version `1.0.0-SNAPSHOT`, Java 25 source/target compatibility. Add dependencies managed by `io.quarkus.platform:quarkus-bom:3.34.1`: `io.quarkus:quarkus-hibernate-reactive-panache`, `jakarta.validation:jakarta.validation-api`, `jakarta.xml.bind:jakarta.xml.bind-api`, `org.glassfish.jaxb:jaxb-runtime`, `jakarta.persistence:jakarta.persistence-api`. Initialize Gradle wrapper by running `gradle wrapper` inside `bric-core-domain/` (or copy wrapper files from a Quarkus project). Create the source directory tree: `src/main/java/com/bric/core/domain/{entity,enums,converter,dto}/`.

- [ ] T005 Create `bric-documents/` project skeleton. Create `bric-documents/settings.gradle` with `pluginManagement` block (repos: mavenCentral, gradlePluginPortal; plugin `io.quarkus` version `3.34.1`), `rootProject.name = 'bric-documents'`, and an `includeBuild('../bric-core-domain')` block with `dependencySubstitution { substitute module('com.bric:bric-core-domain') using project(':') }`. Create `bric-documents/build.gradle` with plugins `java` and `io.quarkus`, group `com.bric`, version `1.0.0-SNAPSHOT`, Java 25, repos mavenCentral + mavenLocal, dependencies: quarkus-bom 3.34.1 platform, `com.bric:bric-core-domain:1.0.0-SNAPSHOT`, `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-reactive-panache`, `quarkus-reactive-pg-client`, `quarkus-messaging-rabbitmq`, `quarkus-hibernate-validator`, `jakarta.xml.bind:jakarta.xml.bind-api`, `org.glassfish.jaxb:jaxb-runtime`, test: `quarkus-junit5`, `io.rest-assured:rest-assured`. Initialize Gradle wrapper. Create source directory tree: `src/main/java/com/bric/documents/{rest,service,dto,validation}/` and `src/main/resources/` and `src/test/java/com/bric/documents/`.

- [ ] T006 [P] Create `bric-documents-generate/` project skeleton. Create `bric-documents-generate/settings.gradle` with `pluginManagement` block (repos: mavenCentral, gradlePluginPortal; plugin `io.quarkus` version `3.34.1`), `rootProject.name = 'bric-documents-generate'`, and an `includeBuild('../bric-core-domain')` block with `dependencySubstitution`. Create `bric-documents-generate/build.gradle` with plugins `java` and `io.quarkus`, group `com.bric`, version `1.0.0-SNAPSHOT`, Java 25, dependencies: quarkus-bom 3.34.1, `com.bric:bric-core-domain:1.0.0-SNAPSHOT`, `quarkus-rest`, `quarkus-hibernate-reactive-panache`, `quarkus-reactive-pg-client`, `quarkus-messaging-rabbitmq`, test: `quarkus-junit5`. Initialize Gradle wrapper. Create source directory tree: `src/main/java/com/bric/documents/generate/consumer/` and `src/main/resources/` and `src/test/java/`.

- [ ] T007 [P] Create `bric-documents-dispatch/` project skeleton. Create `bric-documents-dispatch/settings.gradle` with `pluginManagement` block (repos: mavenCentral, gradlePluginPortal; plugin `io.quarkus` version `3.34.1`), `rootProject.name = 'bric-documents-dispatch'`, and an `includeBuild('../bric-core-domain')` block with `dependencySubstitution`. Create `bric-documents-dispatch/build.gradle` with plugins `java` and `io.quarkus`, group `com.bric`, version `1.0.0-SNAPSHOT`, Java 25, dependencies: quarkus-bom 3.34.1, `com.bric:bric-core-domain:1.0.0-SNAPSHOT`, `quarkus-rest`, `quarkus-hibernate-reactive-panache`, `quarkus-reactive-pg-client`, `quarkus-messaging-rabbitmq`, test: `quarkus-junit5`. Initialize Gradle wrapper. Create source directory tree: `src/main/java/com/bric/documents/dispatch/consumer/` and `src/main/resources/` and `src/test/java/`.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared domain library (`bric-core-domain`) entities, enums, DTOs, and converters that ALL user stories depend on. Also application configuration for all three services.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T008 [P] Create `DocumentType` enum in `bric-core-domain/src/main/java/com/bric/core/domain/enums/DocumentType.java`. Package: `com.bric.core.domain.enums`. Values: `PAYMENT_RECEIPT`, `ENROLLMENT_CONFIRMATION`. `ENROLLMENT_CONFIRMATION` is reserved for future use but MUST be defined now so the enum is complete.

- [ ] T009 [P] Create `CorrespondenceType` enum in `bric-core-domain/src/main/java/com/bric/core/domain/enums/CorrespondenceType.java`. Package: `com.bric.core.domain.enums`. Values: `PRINT`, `EMAIL`, `EMAIL_WITH_ATTACHMENT`, `SMS`, `MAILHOUSE`.

- [ ] T010 [P] Create `DataStatus` enum in `bric-core-domain/src/main/java/com/bric/core/domain/enums/DataStatus.java`. Package: `com.bric.core.domain.enums`. Values in ordinal order: `PENDING` (0), `GENERATED` (1), `DISPATCHED` (2), `FAILED` (3). The ordinal values are significant — they are used for "at or past" status comparison in duplicate detection logic (per research R6). Consumers will compare `document.status.ordinal() >= targetStatus.ordinal()` to detect already-processed documents.

- [ ] T011 Create `EnumListConverter` in `bric-core-domain/src/main/java/com/bric/core/domain/converter/EnumListConverter.java`. Package: `com.bric.core.domain.converter`. Implements `AttributeConverter<List<CorrespondenceType>, String>` with `@Converter` annotation. `convertToDatabaseColumn`: joins enum names with comma separator. `convertToEntityAttribute`: splits comma-separated string and maps each to `CorrespondenceType.valueOf()`. Handle null/empty inputs gracefully (return empty string / empty list). This converter enables storing the correspondence type list as a comma-separated string in the `CORRESPONDENCE_LIST` column instead of a join table.

- [ ] T012 Create `UpdatableEntity` abstract base class in `bric-core-domain/src/main/java/com/bric/core/domain/entity/UpdatableEntity.java`. Package: `com.bric.core.domain.entity`. Extends `PanacheEntityBase` with `@MappedSuperclass`. Public fields: `createdTime` (`ZonedDateTime`, column `CREATED_ON`, not null, not updatable), `createdBy` (`UUID`, column `CREATED_BY`, not null, not updatable), `modifiedTime` (`ZonedDateTime`, column `UPDATED_ON`, not null, annotated with `@Version` for optimistic locking), `modifiedBy` (`UUID`, column `UPDATED_BY`, not null). Methods: `prePersistAudit()` sets all four fields using dummy UUID `00000000-0000-0000-0000-000000000000` and `ZonedDateTime.now()`. `preUpdateAudit()` sets only `modifiedBy` to the dummy UUID (modifiedTime is handled by `@Version`). The placeholder UUID is used because authentication is out of scope for v1 per spec assumptions.

- [ ] T013 Create `Document` entity in `bric-core-domain/src/main/java/com/bric/core/domain/entity/Document.java`. Package: `com.bric.core.domain.entity`. Extends `UpdatableEntity`. Annotated with `@Entity` and `@Table(name = "DOCUMENTS")`. Public fields: `id` (`Long`, column `DOCUMENT_ID`, PK with `@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_seq")` and `@SequenceGenerator(name = "document_seq", sequenceName = "DOCUMENT_SEQ", allocationSize = 1)`), `status` (`DataStatus`, column `DOCUMENT_STATUS`, not null, `@Enumerated(EnumType.STRING)`, default `DataStatus.PENDING`), `type` (`DocumentType`, column `DOCUMENT_TYPE`, not null, `@Enumerated(EnumType.STRING)`), `correspondenceTypes` (`List<CorrespondenceType>`, column `CORRESPONDENCE_LIST`, not null, `@Convert(converter = EnumListConverter.class)`), `storageId` (`String`, column `STORAGE_ID`, length 36, nullable — reserved for future document generation, stays null in v1), `metadata` (`String`, column `METADATA`, length 4096, not null — stores XML-serialized `DocumentMetadata`).

- [ ] T014 [P] Create `DocumentMetadata` DTO in `bric-core-domain/src/main/java/com/bric/core/domain/dto/DocumentMetadata.java`. Package: `com.bric.core.domain.dto`. JAXB-annotated class with `@XmlRootElement(name = "documentMetadata")` and `@XmlAccessorType(XmlAccessType.FIELD)`. Fields: `type` (`DocumentType`, `@XmlElement`), `correspondenceTypes` (`List<CorrespondenceType>`, wrapped with `@XmlElementWrapper(name = "correspondenceTypes")` and `@XmlElement(name = "correspondenceType")`), `dataEntries` (`List<DataEntry>`, wrapped with `@XmlElementWrapper(name = "data")` and `@XmlElement(name = "entry")`). Inner static class `DataEntry` with `@XmlAccessorType(XmlAccessType.FIELD)`, fields: `key` (String), `value` (String), both with `@XmlElement`. Constructor: no-arg (JAXB default) + `(DocumentType, List<CorrespondenceType>, Map<String, String>)` that converts map entries to `DataEntry` list. Getters: `getType()`, `getCorrespondenceTypes()`, `getData()` (converts `DataEntry` list back to `Map<String, String>`). Static helper methods: `toXml()` — creates `JAXBContext.newInstance(DocumentMetadata.class)`, marshals with `JAXB_FORMATTED_OUTPUT=true` to `StringWriter`, returns string. IMPORTANT: This is a blocking call per Constitution Principle I — callers MUST execute it before entering the reactive Mutiny chain. `fromXml(String xml)` — creates `JAXBContext`, unmarshals from `StringReader`, returns `DocumentMetadata`.

- [ ] T015 [P] Create `GenerateDocumentMessage` DTO in `bric-core-domain/src/main/java/com/bric/core/domain/dto/GenerateDocumentMessage.java`. Package: `com.bric.core.domain.dto`. Simple POJO with field `documentId` (`Long`). No-arg constructor (for JSON deserialization) + `(Long documentId)` constructor. Getter `getDocumentId()` and setter `setDocumentId(Long)`. This class is used for both `DOCUMENT_REQUEST` and `DOCUMENT_SEND_REQUEST` queue messages — the payload is identical per Constitution Principle II (Single Source of Truth: messages carry only the document ID).

- [ ] T016 Build and verify `bric-core-domain`. Run `cd /workspaces/bric-core-domain && ./gradlew build` and ensure `BUILD SUCCESSFUL`. This validates all entities, enums, DTOs, and converters compile correctly with the Quarkus BOM dependencies.

- [ ] T017 Create `application.yml` for bric-documents in `bric-documents/src/main/resources/application.yml`. Configure: `quarkus.http.port: 8080`. Datasource: `quarkus.datasource.db-kind: postgresql`, `quarkus.datasource.reactive.url: vertx-reactive:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:DemoDB}`, username `${DB_USER:demo}`, password `${DB_PASSWORD:demo}`. Hibernate: `quarkus.hibernate-orm.database.generation: update` (this service OWNS DDL — per Constitution Principle IV, it's the only service with `update` mode), `quarkus.hibernate-orm.log.sql: true`. RabbitMQ: `quarkus.rabbitmq.host: ${RABBITMQ_HOST:localhost}`, port `${RABBITMQ_PORT:5672}`, username `${RABBITMQ_USERNAME:guest}`, password `${RABBITMQ_PASSWORD:guest}`. SmallRye outgoing channel: `mp.messaging.outgoing.document-request-out.connector: smallrye-rabbitmq`, exchange name `DOCUMENT_REQUEST`, queue name `DOCUMENT_REQUEST`.

- [ ] T018 [P] Create `application.yml` for bric-documents-generate in `bric-documents-generate/src/main/resources/application.yml`. Configure: `quarkus.http.port: 8081`. Same datasource config as T017 but with `quarkus.hibernate-orm.database.generation: validate` (NOT `update` — this service does NOT own DDL). Same RabbitMQ connection config. SmallRye incoming channel: `mp.messaging.incoming.document-request-in.connector: smallrye-rabbitmq`, queue name `DOCUMENT_REQUEST`, exchange name `DOCUMENT_REQUEST`. SmallRye outgoing channel: `mp.messaging.outgoing.document-send-request-out.connector: smallrye-rabbitmq`, exchange name `DOCUMENT_SEND_REQUEST`, queue name `DOCUMENT_SEND_REQUEST`.

- [ ] T019 [P] Create `application.yml` for bric-documents-dispatch in `bric-documents-dispatch/src/main/resources/application.yml`. Configure: `quarkus.http.port: 8082`. Same datasource config as T017 but with `quarkus.hibernate-orm.database.generation: validate` (NOT `update`). Same RabbitMQ connection config. SmallRye incoming channel: `mp.messaging.incoming.document-send-request-in.connector: smallrye-rabbitmq`, queue name `DOCUMENT_SEND_REQUEST`, exchange name `DOCUMENT_SEND_REQUEST`.

**Checkpoint**: All four Gradle projects exist with correct build files, the shared domain library compiles, all service configs are in place, and devcontainer infrastructure includes RabbitMQ. User story implementation can now begin.

---

## Phase 3: User Story 1 — Request a Payment Receipt Document (Priority: P1) 🎯 MVP

**Goal**: API consumer can POST a payment receipt request, the system validates it, persists a Document entity in PENDING status with XML-serialized metadata, publishes a `GenerateDocumentMessage` to the `DOCUMENT_REQUEST` queue, and returns HTTP 202 with the document ID. Implements FR-001, FR-002, FR-003, FR-004, FR-008, FR-010.

**Independent Test**: Send `curl -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{"accountId":1001,"paymentId":5001}'` and verify: HTTP 202 response with `{"documentId": N}`, and a row in the `DOCUMENTS` table with `DOCUMENT_STATUS='PENDING'`, `DOCUMENT_TYPE='PAYMENT_RECEIPT'`, `CORRESPONDENCE_LIST='EMAIL_WITH_ATTACHMENT'`, and valid XML in `METADATA`.

### Implementation for User Story 1

- [ ] T020 [P] [US1] Create `PaymentReceiptRequest` record in `bric-documents/src/main/java/com/bric/documents/dto/PaymentReceiptRequest.java`. Package: `com.bric.documents.dto`. Java record with fields: `accountId` (`@NotNull @Positive Long`), `paymentId` (`Long`, nullable), `paymentMethodId` (`Long`, nullable), `contactId` (`@Positive Long`, nullable — `@Positive` only validates when non-null). Annotate the record with `@ValidPaymentReceiptRequest` (custom class-level constraint). Add a compact constructor `PaymentReceiptRequest(Long accountId)` that delegates to the canonical constructor with nulls for the other three fields. This satisfies FR-002 (field-level validation) and the cross-field constraint is handled by the custom annotation.

- [ ] T021 [P] [US1] Create `@ValidPaymentReceiptRequest` constraint annotation in `bric-documents/src/main/java/com/bric/documents/validation/ValidPaymentReceiptRequest.java`. Package: `com.bric.documents.validation`. Annotated with `@Documented`, `@Constraint(validatedBy = PaymentReceiptRequestValidator.class)`, `@Target({ElementType.TYPE})`, `@Retention(RetentionPolicy.RUNTIME)`. Default message: `"Either paymentId or paymentMethodId must be provided"`. Standard `groups()` and `payload()` attributes.

- [ ] T022 [P] [US1] Create `PaymentReceiptRequestValidator` in `bric-documents/src/main/java/com/bric/documents/validation/PaymentReceiptRequestValidator.java`. Package: `com.bric.documents.validation`. Implements `ConstraintValidator<ValidPaymentReceiptRequest, PaymentReceiptRequest>`. `isValid()` method: if request is null, return true (let `@NotNull` handle that separately). Otherwise return `request.paymentId() != null || request.paymentMethodId() != null`. This implements the cross-field validation rule from FR-002.

- [ ] T023 [US1] Create `DocumentsService` in `bric-documents/src/main/java/com/bric/documents/service/DocumentsService.java`. Package: `com.bric.documents.service`. `@ApplicationScoped` class. Inject `@Channel("document-request-out") Emitter<GenerateDocumentMessage> documentRequestEmitter`. Implement `triggerPaymentReceipt(PaymentReceiptRequest request)` returning `Uni<Long>` with `@WithTransaction`. Method body: (1) Build a `Map<String, String>` data map from request fields — always include `accountId`, conditionally include `paymentId` and `paymentMethodId` if non-null. (2) Create `correspondenceTypes = List.of(CorrespondenceType.EMAIL_WITH_ATTACHMENT)` (hardcoded default per spec assumptions). (3) Create `DocumentMetadata` with `DocumentType.PAYMENT_RECEIPT`, the correspondence types list, and the data map. (4) Call `metadata.toXml()` to serialize to XML string — wrap in try/catch for `JAXBException`, on failure return `Uni.createFrom().failure(new RuntimeException("Failed to serialize document metadata", e))`. IMPORTANT: this blocking JAXB call is intentionally performed before the reactive chain per Constitution Principle I and research R5. (5) Create new `Document` entity: set `type = DocumentType.PAYMENT_RECEIPT`, `correspondenceTypes = correspondenceTypes`, `metadata = metadataXml`, `status = DataStatus.PENDING`, call `document.prePersistAudit()`. (6) Call `document.persist()` (reactive), `.map()` to cast result to `Document`, extract `doc.id`, call `documentRequestEmitter.send(new GenerateDocumentMessage(doc.id))` to publish to the DOCUMENT_REQUEST queue (FR-004), return `doc.id`.

- [ ] T024 [US1] Create `DocumentsResource` REST endpoint in `bric-documents/src/main/java/com/bric/documents/rest/DocumentsResource.java`. Package: `com.bric.documents.rest`. `@Path("/documents")`, `@Produces(MediaType.APPLICATION_JSON)`, `@Consumes(MediaType.APPLICATION_JSON)`. Inject `DocumentsService`. Method: `@POST @Path("/payment-receipt") public Uni<Response> paymentReceipt(@Valid PaymentReceiptRequest request)` — delegates to `documentsService.triggerPaymentReceipt(request)`, maps result to `Response.accepted().entity(Map.of("documentId", documentId)).build()`. The `@Valid` annotation triggers Bean Validation on the request body, which runs both field-level (`@NotNull`, `@Positive`) and class-level (`@ValidPaymentReceiptRequest`) validators before the method executes. Returns HTTP 202 per FR-001 and the REST API contract.

- [ ] T025 [US1] Build and verify bric-documents. Run `cd /workspaces/bric-documents && ./gradlew build`. Ensure `BUILD SUCCESSFUL`. Then start with `./gradlew quarkusDev` and verify: (1) Service starts on port 8080 without errors. (2) Hibernate creates the `DOCUMENTS` table (check logs for DDL statements). (3) Test valid request: `curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{"accountId":1001,"paymentId":5001}'` returns `{"documentId":1}` with HTTP 202. (4) Test invalid request (missing both IDs): `curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{"accountId":1001}'` returns 400. (5) Test invalid request (missing accountId): `curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{}'` returns 400. (6) Query DB to verify document: `psql -h localhost -U demo -d DemoDB -c "SELECT document_id, document_status, document_type, correspondence_list, metadata FROM documents;"` — should show PENDING status, PAYMENT_RECEIPT type, EMAIL_WITH_ATTACHMENT correspondence, and valid XML metadata. (7) Test boundary value for accountId: `curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{"accountId":9223372036854775807,"paymentId":1}'` — should return HTTP 202 (Long.MAX_VALUE is a valid positive value per spec edge case).

**Checkpoint**: User Story 1 is complete. The REST API accepts payment receipt requests, validates input (field-level + cross-field), persists a Document entity with PENDING status and XML metadata, publishes to the DOCUMENT_REQUEST queue, and returns HTTP 202. The service can be tested independently — no dependency on generation or dispatch services.

---

## Phase 4: User Story 2 — Generate a Document from a Queued Request (Priority: P2)

**Goal**: The `bric-documents-generate` service consumes messages from the `DOCUMENT_REQUEST` queue, looks up the Document from the database, checks that its status is PENDING (duplicate guard per FR-011), updates status to GENERATED, and publishes a `GenerateDocumentMessage` to the `DOCUMENT_SEND_REQUEST` queue. If the document is not found, logs a warning (FR-009). If the document status is already at or past GENERATED, logs and discards (FR-011). If any exception occurs during processing, sets status to FAILED (FR-007). Implements FR-005, FR-007, FR-009, FR-011.

**Independent Test**: With `bric-documents` running, create a document via the API (it goes to PENDING). Then start `bric-documents-generate`. The consumer picks up the message, and the document status changes to GENERATED in the database. A new message appears on the `DOCUMENT_SEND_REQUEST` queue.

### Implementation for User Story 2

- [ ] T026 [US2] Create `DocumentRequestConsumer` in `bric-documents-generate/src/main/java/com/bric/documents/generate/consumer/DocumentRequestConsumer.java`. Package: `com.bric.documents.generate.consumer`. `@ApplicationScoped` class. Inject `@Channel("document-send-request-out") Emitter<GenerateDocumentMessage> sendRequestEmitter`. Use `org.jboss.logging.Logger` for logging: `private static final Logger LOG = Logger.getLogger(DocumentRequestConsumer.class)`. Method: `@Incoming("document-request-in") @WithTransaction public Uni<Void> consume(GenerateDocumentMessage message)`. Implementation: (1) Log receipt: `LOG.infof("Received DOCUMENT_REQUEST for documentId=%d", message.getDocumentId())`. (2) Call `Document.<Document>findById(message.getDocumentId())`. (3) If document is null: log warning `"Document not found for documentId=%d"` and return void (FR-009). (4) **Duplicate guard (FR-011)**: Check `document.status.ordinal() >= DataStatus.GENERATED.ordinal()`. If true, log `"Skipping already-processed document documentId=%d, status=%s"` and return void — do NOT publish downstream. (5) **Stub generation logic**: Log `"TODO: Generate document for documentId=%d"` (actual PDF generation is out of scope per spec assumptions). (6) Set `document.status = DataStatus.GENERATED` and call `document.preUpdateAudit()`. (7) After status update persists successfully, publish to dispatch queue: `sendRequestEmitter.send(new GenerateDocumentMessage(document.id))` and log `"Published DOCUMENT_SEND_REQUEST for documentId=%d"`. (8) **Error handling (FR-007)**: Chain `.onFailure()` handler on the Mutiny pipeline. In the failure handler: attempt to load the document again, set `status = DataStatus.FAILED`, persist, log the error with full stack trace. Do NOT rethrow — acknowledge the message to prevent infinite retry loops per research R7. Return `Uni<Void>` via `.replaceWithVoid()`.

- [ ] T027 [US2] Build and verify bric-documents-generate. Run `cd /workspaces/bric-documents-generate && ./gradlew build`. Ensure `BUILD SUCCESSFUL`. Start `bric-documents` first (port 8080, owns DDL). Then start `bric-documents-generate` with `./gradlew quarkusDev`. Verify: (1) Service starts on port 8081 without errors. (2) Hibernate validates the schema against the DOCUMENTS table (logs should show `validate` mode). (3) Create a document via the API: `curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{"accountId":1001,"paymentId":5001}'`. (4) Check bric-documents-generate logs for `"Received DOCUMENT_REQUEST"` and `"Published DOCUMENT_SEND_REQUEST"`. (5) Query DB: `psql -h localhost -U demo -d DemoDB -c "SELECT document_id, document_status FROM documents;"` — status should be `GENERATED`. (6) Check RabbitMQ Management UI (http://localhost:15672) — `DOCUMENT_REQUEST` queue should show consumed messages, `DOCUMENT_SEND_REQUEST` queue should show published messages.

**Checkpoint**: User Stories 1 AND 2 are complete. Documents progress from PENDING to GENERATED automatically via the message queue. Duplicate messages are detected and skipped. Missing documents are logged without blocking.

---

## Phase 5: User Story 3 — Dispatch a Generated Document (Priority: P3)

**Goal**: The `bric-documents-dispatch` service consumes messages from the `DOCUMENT_SEND_REQUEST` queue, looks up the Document, checks that its status is GENERATED (duplicate guard per FR-011), updates status to DISPATCHED. If the document is not found, logs a warning (FR-009). If the document status is already DISPATCHED, logs and discards (FR-011). If any exception occurs, sets status to FAILED (FR-007). Implements FR-006, FR-007, FR-009, FR-011.

**Independent Test**: With a document in GENERATED status in the database, start `bric-documents-dispatch`. Place a message on the `DOCUMENT_SEND_REQUEST` queue (or let it consume from the existing queue). The document status changes to DISPATCHED.

### Implementation for User Story 3

- [ ] T028 [US3] Create `DocumentSendRequestConsumer` in `bric-documents-dispatch/src/main/java/com/bric/documents/dispatch/consumer/DocumentSendRequestConsumer.java`. Package: `com.bric.documents.dispatch.consumer`. `@ApplicationScoped` class. Use `org.jboss.logging.Logger`: `private static final Logger LOG = Logger.getLogger(DocumentSendRequestConsumer.class)`. Method: `@Incoming("document-send-request-in") @WithTransaction public Uni<Void> consume(GenerateDocumentMessage message)`. Implementation: (1) Log receipt: `LOG.infof("Received DOCUMENT_SEND_REQUEST for documentId=%d", message.getDocumentId())`. (2) Call `Document.<Document>findById(message.getDocumentId())`. (3) If document is null: log warning `"Document not found for documentId=%d"` and return void (FR-009). (4) **Duplicate guard (FR-011)**: Check `document.status.ordinal() >= DataStatus.DISPATCHED.ordinal()`. If true, log `"Skipping already-dispatched document documentId=%d, status=%s"` and return void. (5) **Stub dispatch logic**: Log `"TODO: Dispatch correspondence for documentId=%d, types=%s"` with `document.correspondenceTypes` (actual email/print/SMS dispatch is out of scope per spec assumptions). (6) Set `document.status = DataStatus.DISPATCHED` and call `document.preUpdateAudit()`. (7) **Error handling (FR-007)**: Chain `.onFailure()` handler. Load document, set `status = DataStatus.FAILED`, persist, log error. Do NOT rethrow. No downstream message publishing — this is the last stage. Return `Uni<Void>` via `.replaceWithVoid()`.

- [ ] T029 [US3] Build and verify bric-documents-dispatch. Run `cd /workspaces/bric-documents-dispatch && ./gradlew build`. Ensure `BUILD SUCCESSFUL`. Start all three services in order: `bric-documents` (8080), `bric-documents-generate` (8081), `bric-documents-dispatch` (8082). Verify: (1) Service starts on port 8082 without errors. (2) Hibernate validates the schema (validate mode). (3) Create a new document: `curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{"accountId":1001,"paymentId":5001}'`. (4) Check bric-documents-dispatch logs for `"Received DOCUMENT_SEND_REQUEST"` and `"TODO: Dispatch correspondence"`. (5) Query DB: `psql -h localhost -U demo -d DemoDB -c "SELECT document_id, document_status FROM documents;"` — status should be `DISPATCHED`.

**Checkpoint**: User Stories 1, 2, AND 3 are all complete. The full pipeline works: PENDING → GENERATED → DISPATCHED.

---

## Phase 6: User Story 4 — End-to-End Document Lifecycle (Priority: P4)

**Goal**: Validate the complete pipeline works as a cohesive system. A single API request triggers the full lifecycle: PENDING → GENERATED → DISPATCHED. This is a verification/integration phase, not new code.

**Independent Test**: Submit a payment receipt request and poll the database until status reaches DISPATCHED (within 30 seconds per SC-002).

### Implementation for User Story 4

- [ ] T030 [US4] End-to-end pipeline verification. With all three services running (`bric-documents` on 8080, `bric-documents-generate` on 8081, `bric-documents-dispatch` on 8082) and RabbitMQ available: (1) Submit request: `curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{"accountId":1001,"paymentId":5001}'` — note the returned `documentId`. (2) Wait a few seconds for async processing. (3) Query final state: `psql -h localhost -U demo -d DemoDB -c "SELECT document_id, document_status, document_type, correspondence_list, metadata, created_on, updated_on FROM documents WHERE document_id = <ID>;"` — verify DISPATCHED status, PAYMENT_RECEIPT type, EMAIL_WITH_ATTACHMENT correspondence, valid XML metadata, and audit timestamps. (4) Verify RabbitMQ: open http://localhost:15672, confirm both queues (`DOCUMENT_REQUEST`, `DOCUMENT_SEND_REQUEST`) show published and consumed messages. (5) Review all three service logs end-to-end to confirm the full flow executed without errors. (6) Test with payment method ID variant: `curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{"accountId":2002,"paymentMethodId":200}'` — verify same DISPATCHED lifecycle. (7) Submit 50 concurrent requests to validate SC-004: `for i in $(seq 1 50); do curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d "{\"accountId\":$i,\"paymentId\":$i}" & done; wait` — then query `SELECT count(*), document_status FROM documents GROUP BY document_status;` — all 50 should reach DISPATCHED with no message loss or status inconsistency.

**Checkpoint**: Full pipeline validated end-to-end. All acceptance scenarios from US4 verified.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final verification, cleanup, and documentation

- [ ] T031 [P] Verify duplicate message handling. With all services running: (1) Create a document. (2) Manually publish a duplicate message to the `DOCUMENT_REQUEST` queue using RabbitMQ Management UI (Publish Message tab on the queue, body: `{"documentId": <existing_id>}`). (3) Check bric-documents-generate logs for the skip message: `"Skipping already-processed document"`. (4) Verify the document status did NOT change and no message was published to `DOCUMENT_SEND_REQUEST`. (5) Repeat for dispatch queue: publish duplicate to `DOCUMENT_SEND_REQUEST` and verify bric-documents-dispatch logs the skip.

- [ ] T032 [P] Verify independent service restart (SC-006). (1) Stop bric-documents-generate (Ctrl+C). (2) Submit a new document request — it should succeed (HTTP 202) and the document should be in PENDING status. (3) Restart bric-documents-generate. (4) It should pick up the pending message from the queue and process it. (5) Verify document reaches DISPATCHED status. (6) Repeat: stop bric-documents-dispatch, let a document get to GENERATED, restart dispatch, verify it reaches DISPATCHED.

- [ ] T033 Run quickstart.md validation. Follow every step in `specs/001-bric-document-pipeline/quickstart.md` from scratch and verify each expected outcome matches actual behavior. Fix any discrepancies in quickstart.md.

- [ ] T034 [P] Verify FAILED status path (FR-007). With all services running: (1) To simulate a failure in the generation consumer, temporarily modify `DocumentRequestConsumer.consume()` to throw a `RuntimeException("Simulated generation failure")` before the status update. (2) Submit a document request: `curl -s -X POST http://localhost:8080/documents/payment-receipt -H "Content-Type: application/json" -d '{"accountId":9999,"paymentId":9999}'`. (3) Check bric-documents-generate logs for the error being caught by the `onFailure()` handler. (4) Query DB: `psql -h localhost -U demo -d DemoDB -c "SELECT document_id, document_status FROM documents WHERE document_id = (SELECT max(document_id) FROM documents);"` — status should be `FAILED`. (5) Verify the message was acknowledged (not stuck in queue) by checking RabbitMQ Management UI — no unacked messages on `DOCUMENT_REQUEST`. (6) Verify no message was published to `DOCUMENT_SEND_REQUEST` for the failed document. (7) Revert the temporary exception and rebuild.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Phase 2 — can start after foundational is complete
- **User Story 2 (Phase 4)**: Depends on Phase 2 + needs documents in queue (US1 must produce messages, but US2 code can be written independently)
- **User Story 3 (Phase 5)**: Depends on Phase 2 + needs documents in GENERATED status (US2 must run, but US3 code can be written independently)
- **User Story 4 (Phase 6)**: Depends on US1 + US2 + US3 all being complete (integration verification)
- **Polish (Phase 7)**: Depends on US4 completion

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2 — no dependencies on other stories
- **User Story 2 (P2)**: Code can be written after Phase 2 — but testing requires US1 to produce messages
- **User Story 3 (P3)**: Code can be written after Phase 2 — but testing requires US2 to produce messages
- **User Story 4 (P4)**: Verification only — requires all three stories to be complete

### Within Each User Story

- DTOs and validation before service logic
- Service logic before REST endpoints
- REST endpoint before integration verification
- Build and verify as final step

### Parallel Opportunities

- T006 and T007 can run in parallel (independent project skeletons)
- T008, T009, T010 can run in parallel (independent enum files)
- T014 and T015 can run in parallel (independent DTO files)
- T017, T018, T019 can run in parallel (independent application.yml files, but T18/T19 can start as soon as T17 pattern is established)
- T020, T021, T022 can run in parallel (independent files within bric-documents)
- T031, T032, and T034 can run in parallel (independent verification scenarios)
- US2 and US3 code (T026, T028) can be written in parallel after Phase 2 if two developers are available

---

## Parallel Example: Phase 2 Foundation

```bash
# Launch all enum files together:
Task: "Create DocumentType enum in bric-core-domain/.../enums/DocumentType.java"
Task: "Create CorrespondenceType enum in bric-core-domain/.../enums/CorrespondenceType.java"
Task: "Create DataStatus enum in bric-core-domain/.../enums/DataStatus.java"

# Launch all DTO files together:
Task: "Create DocumentMetadata DTO in bric-core-domain/.../dto/DocumentMetadata.java"
Task: "Create GenerateDocumentMessage DTO in bric-core-domain/.../dto/GenerateDocumentMessage.java"

# Launch all application.yml files together:
Task: "Create application.yml for bric-documents"
Task: "Create application.yml for bric-documents-generate"
Task: "Create application.yml for bric-documents-dispatch"
```

## Parallel Example: User Story 1

```bash
# Launch all validation files together:
Task: "Create PaymentReceiptRequest record in bric-documents/.../dto/"
Task: "Create @ValidPaymentReceiptRequest annotation in bric-documents/.../validation/"
Task: "Create PaymentReceiptRequestValidator in bric-documents/.../validation/"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T007)
2. Complete Phase 2: Foundational (T008–T019)
3. Complete Phase 3: User Story 1 (T020–T025)
4. **STOP and VALIDATE**: Test US1 independently — REST API accepts requests, validates, persists, publishes
5. Deploy/demo if ready — you have a working document intake API

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo (documents auto-generate)
4. Add User Story 3 → Test independently → Deploy/Demo (documents auto-dispatch)
5. Add User Story 4 → Full pipeline verification
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (REST API + service)
   - Developer B: User Story 2 (generation consumer) — can write code in parallel, test after US1 deploys
   - Developer C: User Story 3 (dispatch consumer) — can write code in parallel, test after US2 deploys
3. All rejoin for User Story 4 (E2E verification) and Phase 7 (Polish)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- `bric-documents` MUST be started first — it owns DDL via Hibernate `update` mode
- All three services share the same PostgreSQL instance and RabbitMQ instance
- Queue auto-declaration by SmallRye means no manual RabbitMQ setup is needed
- JAXB serialization is blocking — always execute before entering Mutiny reactive chain
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
