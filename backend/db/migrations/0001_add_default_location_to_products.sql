-- Migration: add default_location column to products and set default for existing rows
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS default_location VARCHAR(50) DEFAULT 'FRIDGE';

-- Ensure existing rows have a value
UPDATE products SET default_location = 'FRIDGE' WHERE default_location IS NULL;

-- Optional: remove server default to keep it nullable for new inserts
ALTER TABLE products ALTER COLUMN default_location DROP DEFAULT;
