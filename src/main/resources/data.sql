-- ============================================================================
-- SwapEd — Reference Seed Script (data.sql)
-- Categories are inserted idempotently; full relational sample data (8 verified
-- Thapar students with individual BCrypt password hashes, 16 academic listings,
-- reservations, and completed trades) is seeded via DataInitializerConfig on
-- first startup when the tables are empty.
-- ============================================================================

INSERT IGNORE INTO categories (category_id, category_name) VALUES
(1, 'Textbooks'),
(2, 'Lab Coats'),
(3, 'Drawing & Drafting Tools'),
(4, 'Engineering Equipment'),
(5, 'Calculators'),
(6, 'Other Academic Essentials');
