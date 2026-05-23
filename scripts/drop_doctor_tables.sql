-- One-time migration: removes the Doctor role tables that were dropped in v1.1.
-- Run this manually against your existing database ONLY if you have a pre-v1.1 schema.
--
-- Usage:
--   psql -d cvd -U postgres -f scripts/drop_doctor_tables.sql
--
-- Safe to skip on fresh installs — Hibernate will not create these tables.

DROP TABLE IF EXISTS doctor_reviews CASCADE;
DROP TABLE IF EXISTS doctors        CASCADE;
DROP TABLE IF EXISTS admins         CASCADE;

-- Expected tables after migration:
--   public | patient_login  | table
--   public | patients       | table
--   public | predictions    | table
