-- Realign BIGSERIAL sequences with current MAX(id).
-- Prevents duplicate-key failures on INSERT when the sequence drifts (e.g. after bulk deletes,
-- manual fixes, or restores). Next nextval() becomes max(id)+1 (or 1 for empty tables).

SELECT setval(
    pg_get_serial_sequence('carts', 'id'),
    COALESCE((SELECT MAX(id) FROM carts), 0) + 1,
    false
);
SELECT setval(
    pg_get_serial_sequence('cart_items', 'id'),
    COALESCE((SELECT MAX(id) FROM cart_items), 0) + 1,
    false
);
SELECT setval(
    pg_get_serial_sequence('orders', 'id'),
    COALESCE((SELECT MAX(id) FROM orders), 0) + 1,
    false
);
SELECT setval(
    pg_get_serial_sequence('order_items', 'id'),
    COALESCE((SELECT MAX(id) FROM order_items), 0) + 1,
    false
);
SELECT setval(
    pg_get_serial_sequence('payments', 'id'),
    COALESCE((SELECT MAX(id) FROM payments), 0) + 1,
    false
);
SELECT setval(
    pg_get_serial_sequence('outbox_events', 'id'),
    COALESCE((SELECT MAX(id) FROM outbox_events), 0) + 1,
    false
);
