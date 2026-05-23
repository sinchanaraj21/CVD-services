-- Run in psql to clean up all removed tables:
-- psql -d cvd
-- then paste below:

DROP TABLE IF EXISTS doctor_reviews CASCADE;
DROP TABLE IF EXISTS doctors        CASCADE;
DROP TABLE IF EXISTS admins         CASCADE;

-- Expected result after running:
--  public | patient_login  | table
--  public | patients       | table
--  public | predictions    | table
