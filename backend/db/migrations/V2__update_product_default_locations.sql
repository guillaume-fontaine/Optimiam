-- Migration: Update product default locations with intelligent assignment based on category
-- This migration ensures all products have a default location and assigns them intelligently based on their category

-- Update products in "Produits Laitiers & Œufs" category to FRIDGE
UPDATE products 
SET default_location = 'FRIDGE'
WHERE category_id IN (SELECT id FROM categories WHERE name LIKE '%Produits Laitiers%')
  AND (default_location IS NULL OR default_location = '');

-- Update products in "Viandes & Poissons" category to FRIDGE
UPDATE products 
SET default_location = 'FRIDGE'
WHERE category_id IN (SELECT id FROM categories WHERE name LIKE '%Viandes%')
  AND (default_location IS NULL OR default_location = '');

-- Update products in "Fruits & Légumes" category to FRIDGE
UPDATE products 
SET default_location = 'FRIDGE'
WHERE category_id IN (SELECT id FROM categories WHERE name LIKE '%Fruits%Légumes%' OR name LIKE '%Légumes%Fruits%')
  AND (default_location IS NULL OR default_location = '');

-- Update products in "Féculents & Céréales" category to PANTRY
UPDATE products 
SET default_location = 'PANTRY'
WHERE category_id IN (SELECT id FROM categories WHERE name LIKE '%Féculents%' OR name LIKE '%Céréales%')
  AND (default_location IS NULL OR default_location = '');

-- Update products in "Épicerie & Condiments" category to PANTRY
UPDATE products 
SET default_location = 'PANTRY'
WHERE category_id IN (SELECT id FROM categories WHERE name LIKE '%Épicerie%' OR name LIKE '%Condiments%')
  AND (default_location IS NULL OR default_location = '');

-- Update products in "Boissons" category to PANTRY
UPDATE products 
SET default_location = 'PANTRY'
WHERE category_id IN (SELECT id FROM categories WHERE name LIKE '%Boissons%')
  AND (default_location IS NULL OR default_location = '');

-- Handle any products without a category or unmatched categories - default to FRIDGE
UPDATE products 
SET default_location = 'FRIDGE'
WHERE default_location IS NULL OR default_location = '';

-- Verify the migration: show products and their assigned locations
SELECT id, name, category_id, default_location 
FROM products 
WHERE deleted = false
ORDER BY category_id, name;
