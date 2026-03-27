-- Initialize DemoDB
-- This script runs automatically on first container startup.
-- The database "DemoDB" is already created by POSTGRES_DB env var.

-- Ensure the demo user has full privileges on the database
GRANT ALL PRIVILEGES ON DATABASE "DemoDB" TO demo;

-- Create the public schema (if not exists) and grant usage
GRANT ALL ON SCHEMA public TO demo;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO demo;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO demo;

-- Document ID sequence for bric-core-domain Document entity
CREATE SEQUENCE IF NOT EXISTS "DOCUMENT_SEQ" START WITH 1 INCREMENT BY 1;
