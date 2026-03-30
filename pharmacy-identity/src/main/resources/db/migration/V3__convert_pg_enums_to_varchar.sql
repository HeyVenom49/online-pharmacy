-- Native PostgreSQL enums caused fragile JDBC/Hibernate binding for inserts (e.g. signup).
-- VARCHAR + @Enumerated(STRING) is reliable and preserves values.

ALTER TABLE users
    ALTER COLUMN role DROP DEFAULT,
    ALTER COLUMN status DROP DEFAULT;

ALTER TABLE users
    ALTER COLUMN role TYPE VARCHAR(32) USING role::text,
    ALTER COLUMN status TYPE VARCHAR(32) USING status::text;

ALTER TABLE users
    ALTER COLUMN role SET DEFAULT 'CUSTOMER',
    ALTER COLUMN status SET DEFAULT 'ACTIVE';

ALTER TABLE notifications
    ALTER COLUMN "type" TYPE VARCHAR(64) USING "type"::text;

DROP TYPE IF EXISTS user_role;
DROP TYPE IF EXISTS user_status;
DROP TYPE IF EXISTS notification_type;
