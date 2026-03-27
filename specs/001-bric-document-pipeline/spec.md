# Feature Specification: BRIC Document Processing Pipeline

**Feature Branch**: `001-bric-document-pipeline`
**Created**: 2026-03-27
**Status**: Draft
**Input**: User description: "Create baseline specification based on the examples/implementation_plan.md preliminary implementation plan."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Request a Payment Receipt Document (Priority: P1)

An API consumer submits a payment receipt request with account and payment details. The system validates the request, creates a document record in PENDING status, and acknowledges the request asynchronously. This is the entry point for the entire document lifecycle and must work before any downstream processing can occur.

**Why this priority**: Without the ability to accept and persist document requests, no other part of the pipeline has work to do. This is the foundational capability that all other stories depend on.

**Independent Test**: Can be fully tested by sending a POST request to the document creation endpoint and verifying: a valid response with a document identifier is returned, and a document record exists in the database with PENDING status and correct metadata.

**Acceptance Scenarios**:

1. **Given** the system is running and the database is available, **When** an API consumer submits a valid payment receipt request with an account ID and a payment ID, **Then** the system returns an accepted (202) response containing the new document identifier, and a document record is persisted with PENDING status, PAYMENT_RECEIPT type, and the submitted data serialized as metadata.

2. **Given** the system is running, **When** an API consumer submits a valid payment receipt request with an account ID and a payment method ID (instead of payment ID), **Then** the system accepts the request identically, using the payment method ID in the metadata.

3. **Given** the system is running, **When** an API consumer submits a request missing both payment ID and payment method ID, **Then** the system rejects the request with a validation error indicating that at least one of these identifiers is required.

4. **Given** the system is running, **When** an API consumer submits a request with a missing or non-positive account ID, **Then** the system rejects the request with a validation error.

---

### User Story 2 - Generate a Document from a Queued Request (Priority: P2)

After a document request is accepted (US1), the system asynchronously picks up the request from a message queue, processes the document generation step, updates the document status to GENERATED, and forwards the document to the dispatch stage. In this initial version, actual file generation is a placeholder — the key value is establishing the message-driven pipeline and status progression.

**Why this priority**: This story proves the asynchronous processing pattern works end-to-end from the first queue to the second, and establishes the status transition from PENDING to GENERATED. It depends on US1 producing messages.

**Independent Test**: Can be tested by placing a message with a valid document ID on the generation queue and verifying: the document status changes to GENERATED in the database, and a new message appears on the dispatch queue.

**Acceptance Scenarios**:

1. **Given** a document record exists in PENDING status, **When** a generation request message containing the document's identifier arrives on the generation queue, **Then** the system updates the document status to GENERATED and publishes a dispatch request message containing the same document identifier.

2. **Given** a generation request message arrives with a document identifier that does not exist in the database, **When** the system processes the message, **Then** the system logs a warning and does not publish any downstream message.

---

### User Story 3 - Dispatch a Generated Document (Priority: P3)

After a document has been generated (US2), the system picks up the dispatch request from a message queue, performs the dispatch step (placeholder in this version), and updates the document status to DISPATCHED. This completes the full document lifecycle: PENDING -> GENERATED -> DISPATCHED.

**Why this priority**: This is the final stage of the pipeline. It completes the lifecycle but depends on both US1 and US2 being functional. In this initial version, actual dispatch (email, print, SMS) is a placeholder.

**Independent Test**: Can be tested by placing a message with a valid document ID (in GENERATED status) on the dispatch queue and verifying: the document status changes to DISPATCHED in the database.

**Acceptance Scenarios**:

1. **Given** a document record exists in GENERATED status, **When** a dispatch request message containing the document's identifier arrives on the dispatch queue, **Then** the system updates the document status to DISPATCHED.

2. **Given** a dispatch request message arrives with a document identifier that does not exist in the database, **When** the system processes the message, **Then** the system logs a warning and takes no further action.

---

### User Story 4 - End-to-End Document Lifecycle (Priority: P4)

An API consumer submits a payment receipt request and the document progresses through the entire pipeline automatically: creation (PENDING) -> generation (GENERATED) -> dispatch (DISPATCHED). This story validates that all three stages work together as a cohesive system.

**Why this priority**: Integration story that validates the full pipeline. Only meaningful after US1, US2, and US3 are individually working.

**Independent Test**: Can be tested by submitting a payment receipt request via the API and then polling the database until the document reaches DISPATCHED status (or a reasonable timeout expires).

**Acceptance Scenarios**:

1. **Given** all three services and the message broker are running, **When** an API consumer submits a valid payment receipt request, **Then** the document progresses through PENDING -> GENERATED -> DISPATCHED without manual intervention, and the final database record reflects DISPATCHED status with the original metadata intact.

---

### Edge Cases

- What happens when the database is temporarily unavailable during document creation? The system MUST return an appropriate error response rather than silently failing.
- What happens when a generation or dispatch consumer receives a message for a document that has already been processed (duplicate message)? The system MUST check the document's current status before processing; if the document is already at or past the target status, the system MUST log the skip and discard the message without further action.
- What happens when the message broker is unavailable at the time of publishing after document creation? The document is persisted but the message is not sent, leaving it stuck in PENDING status. No automated recovery is provided in this version; operators can manually query the database to identify stuck documents.
- What happens when metadata serialization fails for a valid request? The system MUST return an internal error rather than persisting a document with corrupt metadata.
- What happens when the account ID is at the boundary of the positive range (e.g., maximum integer value)? The system MUST accept it as valid.

## Clarifications

### Session 2026-03-27

- Q: How should duplicate messages be handled — skip, idempotent re-processing, or status-guarded progression? → A: Skip duplicates: check current status before processing; if already at or past target status, log and discard the message.
- Q: What triggers the FAILED status? → A: Any exception during generation or dispatch processing sets the document to FAILED.
- Q: Should this version include recovery for stuck PENDING documents? → A: No recovery in this version — accept the risk; operators can manually query the database.
- Q: Are status transitions strictly ordered? → A: Strict forward-only: PENDING -> GENERATED -> DISPATCHED. No skipping, no backward transitions. Any status can transition to FAILED.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST accept payment receipt requests via a REST API endpoint and return an asynchronous acknowledgment (HTTP 202) with the created document's identifier.
- **FR-002**: System MUST validate that every payment receipt request contains a positive account ID and at least one of: payment ID or payment method ID.
- **FR-003**: System MUST persist a document record with PENDING status, document type, correspondence type list, and serialized metadata upon successful request validation.
- **FR-004**: System MUST publish a message containing only the document identifier to the generation queue after persisting the document.
- **FR-005**: System MUST consume generation request messages, update the document status to GENERATED, and publish a dispatch request message containing the document identifier.
- **FR-006**: System MUST consume dispatch request messages and update the document status to DISPATCHED.
- **FR-007**: System MUST track document status through the lifecycle: PENDING -> GENERATED -> DISPATCHED, with FAILED as a terminal error state. Transitions MUST be strictly forward-only; no step may be skipped and no backward transitions are allowed. Any status may transition to FAILED. Any exception during generation or dispatch processing MUST set the document status to FAILED and log the error details.
- **FR-008**: System MUST store document metadata as a structured format that can be reliably serialized and deserialized.
- **FR-009**: System MUST log warnings when processing messages that reference non-existent documents, without throwing errors that would block the message queue.
- **FR-010**: System MUST maintain audit fields (created timestamp, created by, modified timestamp, modified by) on every document record. In this initial version, the "by" fields use a placeholder identity until authentication is implemented.
- **FR-011**: System MUST check the document's current status before processing a queue message; if the document is already at or past the target status, the system MUST log the duplicate and discard the message without further action or downstream publishing.

### Key Entities

- **Document**: The central entity representing a document request throughout its lifecycle. Key attributes: unique identifier, status (PENDING/GENERATED/DISPATCHED/FAILED), document type (e.g., PAYMENT_RECEIPT), correspondence types (list of delivery channels such as EMAIL, PRINT, SMS), serialized metadata containing the request-specific data, optional storage reference for the generated file, and audit timestamps.

- **Document Metadata**: Structured data associated with a document that captures the request-specific information (account ID, payment ID, payment method ID, etc.), the document type, and the correspondence types. Stored as a serialized string within the Document entity.

- **Generation Request Message**: A lightweight message placed on the generation queue containing only the document identifier. The consumer retrieves full document state from the database.

- **Dispatch Request Message**: Same structure as the generation request message, placed on the dispatch queue after generation completes.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A valid payment receipt request receives an accepted response within 2 seconds under normal load.
- **SC-002**: Documents progress from PENDING to DISPATCHED status within 30 seconds of initial request submission (under normal load with all services running).
- **SC-003**: 100% of invalid requests (missing account ID, missing both payment identifiers, non-positive values) are rejected with clear validation error messages before any database write occurs.
- **SC-004**: The system processes at least 50 concurrent document requests without message loss or status inconsistency.
- **SC-005**: Non-existent document references in queue messages are logged and handled gracefully — no queue blockage, no unhandled errors, no cascading failures.
- **SC-006**: Each service can be started, stopped, and restarted independently without corrupting document state or losing in-flight messages (within message broker durability guarantees).

## Assumptions

- Authentication and authorization are out of scope for this initial version. Audit "created by" and "modified by" fields will use a placeholder identity.
- Actual document generation (e.g., PDF rendering) is out of scope. The generation stage is a status-update placeholder that will be filled in later.
- Actual correspondence dispatch (email sending, print routing, SMS delivery) is out of scope. The dispatch stage is a status-update placeholder.
- The correspondence type for payment receipts defaults to EMAIL_WITH_ATTACHMENT. Configuration of correspondence types per request is not required in this version.
- A shared database instance is acceptable for all services in this initial version. Service-specific database isolation may be considered in future iterations.
- The message broker provides at-least-once delivery. Exactly-once processing guarantees are not required for this version, but consumers MUST be resilient to duplicate messages.
- The `storageId` field on documents will remain null until document generation is implemented. It is reserved for a future storage reference.
- Only the PAYMENT_RECEIPT document type is supported in this version. ENROLLMENT_CONFIRMATION and other types will be added later.
- The contact ID field on payment receipt requests is optional and not used in processing for this version. It is captured for future use.
- No automated recovery mechanism exists for documents stuck in PENDING status due to message broker failures. This is an accepted risk for v1; a scheduled sweep or status query endpoint may be added in a future iteration.
