-- Align with Hibernate enum binding (string parameters); native PG enum broke parameterized enum comparisons.
ALTER TABLE prescriptions ALTER COLUMN status DROP DEFAULT;
ALTER TABLE prescriptions
    ALTER COLUMN status TYPE VARCHAR(32) USING status::text;
ALTER TABLE prescriptions
    ALTER COLUMN status SET DEFAULT 'PENDING';

DROP TYPE IF EXISTS prescription_status;
