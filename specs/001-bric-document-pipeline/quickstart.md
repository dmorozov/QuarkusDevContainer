# Quickstart: BRIC Document Processing Pipeline

**Branch**: `001-bric-document-pipeline` | **Date**: 2026-03-27

## Prerequisites

- Devcontainer running (PostgreSQL 18 + RabbitMQ 4)
- Java 25 available at `/opt/java/openjdk`
- Ports available: 8080, 8081, 8082, 5672, 15672

## 1. Rebuild Devcontainer

After adding RabbitMQ to `docker-compose.yml`, rebuild the devcontainer:

```bash
# From VS Code: Ctrl+Shift+P → "Dev Containers: Rebuild Container"
# Or from CLI: devcontainer rebuild
```

## 2. Build the Shared Domain Library

```bash
cd /workspaces/bric-core-domain
./gradlew build
```

Expected: `BUILD SUCCESSFUL` — compiles entities, enums, DTOs, converters.

## 3. Start the Services

Open three terminal windows and start each service:

**Terminal 1 — bric-documents (port 8080)**:
```bash
cd /workspaces/bric-documents
./gradlew quarkusDev
```

**Terminal 2 — bric-documents-generate (port 8081)**:
```bash
cd /workspaces/bric-documents-generate
./gradlew quarkusDev
```

**Terminal 3 — bric-documents-dispatch (port 8082)**:
```bash
cd /workspaces/bric-documents-dispatch
./gradlew quarkusDev
```

Wait for all three services to report `Listening on: http://0.0.0.0:808X`.

## 4. Submit a Payment Receipt Request

```bash
curl -s -X POST http://localhost:8080/documents/payment-receipt \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1001,
    "paymentId": 5001
  }' | jq .
```

Expected response:
```json
{
  "documentId": 1
}
```

## 5. Verify the Pipeline

### Check Document Status in Database

```bash
psql -h localhost -U demo -d DemoDB -c \
  "SELECT document_id, document_status, document_type FROM documents;"
```

Expected: Document with status `DISPATCHED` (after pipeline completes).

### Check RabbitMQ Management UI

Open http://localhost:15672 (guest/guest) and verify:
- `DOCUMENT_REQUEST` queue received and consumed messages
- `DOCUMENT_SEND_REQUEST` queue received and consumed messages

### Check Service Logs

In each terminal, look for log entries:
- **bric-documents**: Document persisted and message published
- **bric-documents-generate**: `Received DOCUMENT_REQUEST`, status updated to GENERATED
- **bric-documents-dispatch**: `Received DOCUMENT_SEND_REQUEST`, status updated to DISPATCHED

## 6. Test Validation Errors

```bash
# Missing accountId
curl -s -X POST http://localhost:8080/documents/payment-receipt \
  -H "Content-Type: application/json" \
  -d '{}' | jq .

# Missing both paymentId and paymentMethodId
curl -s -X POST http://localhost:8080/documents/payment-receipt \
  -H "Content-Type: application/json" \
  -d '{"accountId": 1001}' | jq .
```

Both should return `400 Bad Request` with validation error details.

## 7. Run Tests

```bash
# Unit tests for each module
cd /workspaces/bric-core-domain && ./gradlew test
cd /workspaces/bric-documents && ./gradlew test
cd /workspaces/bric-documents-generate && ./gradlew test
cd /workspaces/bric-documents-dispatch && ./gradlew test
```

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Connection refused` on 5672 | RabbitMQ not running | Rebuild devcontainer; check `docker-compose.yml` |
| Schema validation error on startup | `bric-documents` not started first | Start `bric-documents` first (owns DDL with `update` mode) |
| Document stuck in PENDING | Message broker was unavailable during publish | Restart service; check RabbitMQ connectivity |
| `BUILD FAILED` on service | `bric-core-domain` not built | Build `bric-core-domain` first: `cd bric-core-domain && ./gradlew build` |
