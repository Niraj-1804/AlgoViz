CREATE DATABASE IF NOT EXISTS campus_eats;
USE campus_eats;

CREATE TABLE IF NOT EXISTS dishes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  price DECIMAL(8,2) NOT NULL,
  available BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO dishes (name, price, available)
SELECT 'Masala Dosa', 80.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM dishes WHERE name = 'Masala Dosa');

INSERT INTO dishes (name, price, available)
SELECT 'Veg Sandwich', 60.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM dishes WHERE name = 'Veg Sandwich');

INSERT INTO dishes (name, price, available)
SELECT 'Cold Coffee', 70.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM dishes WHERE name = 'Cold Coffee');

INSERT INTO dishes (name, price, available)
SELECT 'Veg Burger', 95.00, FALSE
WHERE NOT EXISTS (SELECT 1 FROM dishes WHERE name = 'Veg Burger');