# Data Model: BRIC Document Processing Pipeline

**Branch**: `001-bric-document-pipeline` | **Date**: 2026-03-27

## Entities

### Document

The central entity representing a document request throughout its lifecycle.

**Table**: `DOCUMENTS`

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| DOCUMENT_ID | BIGINT | No | PK, sequence-generated (`DOCUMENT_SEQ`) | Unique document identifier |
| DOCUMENT_STATUS | VARCHAR | No | Enum: PENDING, GENERATED, DISPATCHED, FAILED | Current lifecycle status |
| DOCUMENT_TYPE | VARCHAR | No | Enum: PAYMENT_RECEIPT | Type of document |
| CORRESPONDENCE_LIST | VARCHAR | No | Comma-separated enum values | Delivery channels (EMAIL, PRINT, SMS, etc.) |
| STORAGE_ID | VARCHAR(36) | Yes | | Reference to generated file (null until generation implemented) |
| METADATA | VARCHAR(4096) | No | | XML-serialized DocumentMetadata |
| CREATED_ON | TIMESTAMP WITH TIME ZONE | No | Immutable after insert | Creation timestamp |
| CREATED_BY | UUID | No | Immutable after insert | Creator identity (placeholder UUID in v1) |
| UPDATED_ON | TIMESTAMP WITH TIME ZONE | No | `@Version` — optimistic locking | Last modification timestamp |
| UPDATED_BY | UUID | No | | Last modifier identity (placeholder UUID in v1) |

**Sequence**: `DOCUMENT_SEQ` — `START WITH 1 INCREMENT BY 1`

### State Machine

```text
PENDING ──→ GENERATED ──→ DISPATCHED
   │             │              │
   └─── FAILED ←─┘──── FAILED ←┘
```

**Transition rules** (strict forward-only, per clarification):
- PENDING → GENERATED: Generation consumer processes successfully
- GENERATED → DISPATCHED: Dispatch consumer processes successfully
- Any → FAILED: Exception during generation or dispatch processing
- No backward transitions allowed
- No step skipping allowed

**Duplicate handling**: Consumers MUST check current status before processing. If status is already at or past the target, log and discard.

## Enums

### DocumentType

| Value | Description |
|-------|-------------|
| PAYMENT_RECEIPT | Payment receipt document (only type in v1) |
| ENROLLMENT_CONFIRMATION | Reserved for future use |

### CorrespondenceType

| Value | Description |
|-------|-------------|
| PRINT | Physical print delivery |
| EMAIL | Email delivery |
| EMAIL_WITH_ATTACHMENT | Email with document attached (default for payment receipts) |
| SMS | SMS notification |
| MAILHOUSE | External mail house routing |

### DataStatus

| Value | Ordinal | Description |
|-------|---------|-------------|
| PENDING | 0 | Document created, awaiting generation |
| GENERATED | 1 | Document generated, awaiting dispatch |
| DISPATCHED | 2 | Document dispatched to correspondence channels |
| FAILED | 3 | Terminal error state |

**Note**: Ordinal values are used for "at or past" status comparison in duplicate detection logic.

## DTOs

### DocumentMetadata (XML-serializable)

Structured metadata stored as XML in `Document.metadata`.

| Field | Type | Description |
|-------|------|-------------|
| type | DocumentType | Document type |
| correspondenceTypes | List\<CorrespondenceType\> | Delivery channels |
| dataEntries | List\<DataEntry\> | Key-value pairs from the request |

**DataEntry** (nested):

| Field | Type | Description |
|-------|------|-------------|
| key | String | Data field name (e.g., "accountId") |
| value | String | Data field value (e.g., "1001") |

### GenerateDocumentMessage (queue message)

| Field | Type | Description |
|-------|------|-------------|
| documentId | Long | Reference to Document entity |

Used for both `DOCUMENT_REQUEST` and `DOCUMENT_SEND_REQUEST` queues (same payload structure per Constitution Principle II — Single Source of Truth).

### PaymentReceiptRequest (REST input)

| Field | Type | Nullable | Validation |
|-------|------|----------|------------|
| accountId | Long | No | `@NotNull @Positive` |
| paymentId | Long | Yes | None (cross-field validated) |
| paymentMethodId | Long | Yes | None (cross-field validated) |
| contactId | Long | Yes | `@Positive` (when present) |

**Cross-field validation**: At least one of `paymentId` or `paymentMethodId` MUST be provided (custom `@ValidPaymentReceiptRequest` annotation).

## Relationships

```text
Document 1──contains──1 metadata (serialized XML string)
Document 1──has──N correspondenceTypes (comma-separated in single column)
```

No foreign key relationships to external tables in v1. The Document entity is self-contained.

## Infrastructure

### Message Queues (RabbitMQ)

| Queue/Exchange | Producer | Consumer | Payload |
|----------------|----------|----------|---------|
| DOCUMENT_REQUEST | bric-documents | bric-documents-generate | `{ "documentId": <Long> }` |
| DOCUMENT_SEND_REQUEST | bric-documents-generate | bric-documents-dispatch | `{ "documentId": <Long> }` |

Auto-declared by SmallRye Reactive Messaging on service startup.

### Database

Single shared PostgreSQL 18 instance (`DemoDB`). Schema owned by `bric-documents` (Hibernate `update` mode). Other services use `validate` mode.
