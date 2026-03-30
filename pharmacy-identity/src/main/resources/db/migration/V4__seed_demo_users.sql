-- V4__seed_demo_users.sql
--
-- Demo seed users for local/dev environments.
-- Idempotent: uses ON CONFLICT (email) DO NOTHING.
--
-- Passwords (BCrypt):
-- - admin@pharmacy.com / admin123
-- - demo.customer@example.com / password123

INSERT INTO users (name, email, password_hash, mobile, role, status)
VALUES
  ('Admin User', 'admin@pharmacy.com', '$2a$10$lvGgxlwaJDVlzwuLg1jlkeTypyTVG6Bfkb3ErvIG3hkNM2kH.q2dm', '5550000', 'ADMIN', 'ACTIVE'),
  ('Demo Customer', 'demo.customer@example.com', '$2a$10$1jRIbPM1nREplHbLtuo4KebwD5DlkXgR82YzIXew5ZUqZPVBjnJRi', '5550101', 'CUSTOMER', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

