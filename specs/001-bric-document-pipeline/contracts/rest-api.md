# REST API Contract: BRIC Documents Service

**Branch**: `001-bric-document-pipeline` | **Date**: 2026-03-27
**Service**: bric-documents | **Port**: 8080

## POST /documents/payment-receipt

Create a payment receipt document request. The document is persisted and queued for asynchronous generation and dispatch.

### Request

**Content-Type**: `application/json`

```json
{
  "accountId": 1001,
  "paymentId": 5001,
  "paymentMethodId": null,
  "contactId": null
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| accountId | Long | Yes | Must be positive (> 0) |
| paymentId | Long | No | At least one of paymentId or paymentMethodId required |
| paymentMethodId | Long | No | At least one of paymentId or paymentMethodId required |
| contactId | Long | No | Must be positive when provided |

### Responses

#### 202 Accepted

Document created and queued for processing.

```json
{
  "documentId": 42
}
```

| Field | Type | Description |
|-------|------|-------------|
| documentId | Long | Unique identifier of the created document |

#### 400 Bad Request

Validation failed.

```json
{
  "title": "Constraint Violation",
  "status": 400,
  "violations": [
    {
      "field": "accountId",
      "message": "must not be null"
    }
  ]
}
```

Common validation errors:
- `accountId` is null or non-positive
- Both `paymentId` and `paymentMethodId` are null
- `contactId` is non-positive (when provided)

#### 500 Internal Server Error

Unexpected error (e.g., metadata serialization failure, database unavailable).

```json
{
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Failed to serialize document metadata"
}
```

### Example: Valid Request with Payment ID

```bash
curl -X POST http://localhost:8080/documents/payment-receipt \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1001,
    "paymentId": 5001
  }'
```

Response:
```json
{"documentId": 1}
```

### Example: Valid Request with Payment Method ID

```bash
curl -X POST http://localhost:8080/documents/payment-receipt \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1001,
    "paymentMethodId": 200
  }'
```

Response:
```json
{"documentId": 2}
```

### Example: Invalid Request (Missing Both IDs)

```bash
curl -X POST http://localhost:8080/documents/payment-receipt \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1001
  }'
```

Response: `400 Bad Request` with violation message.
