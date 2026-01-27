-- Données de test pour TD3

-- Insertion des plats
INSERT INTO Dish (name, dish_type, selling_price) VALUES 
('Salade Fraîche', 'START', 3000.0),
('Poulet Rôti', 'MAIN', 12000.0),
('Gâteau au chocolat', 'DESSERT', 5000.0);

-- Insertion des ingrédients (uniques, normalisés)
INSERT INTO Ingredient (name, price, category) VALUES 
('Laitue', 800.0, 'VEGETABLE'),
('Tomate', 600.0, 'VEGETABLE'),
('Poulet', 3500.0, 'ANIMAL'),
('Chocolat', 2500.0, 'OTHER'),
('Beurre', 1800.0, 'DAIRY');

-- Insertion des relations DishIngredient avec quantités et unités
-- Salade Fraîche (id=1): 1 pièce de laitue, 0.25 KG de tomate
INSERT INTO DishIngredient (id_dish, id_ingredient, required_quantity, unit) VALUES 
(1, 1, 1.0, 'PIECE'),   -- Laitue
(1, 2, 0.25, 'KG');     -- Tomate

-- Poulet Rôti (id=2): 0.5 KG de poulet
INSERT INTO DishIngredient (id_dish, id_ingredient, required_quantity, unit) VALUES 
(2, 3, 0.5, 'KG');      -- Poulet

-- Gâteau au chocolat (id=3): 0.2 KG de chocolat, 0.1 KG de beurre
INSERT INTO DishIngredient (id_dish, id_ingredient, required_quantity, unit) VALUES 
(3, 4, 0.2, 'KG'),      -- Chocolat
(3, 5, 0.1, 'KG');      -- Beurre