-- TD4: Gestion de stocks
-- 1. Créer l'ENUM pour les types de mouvement
CREATE TYPE movement_type_enum AS ENUM ('IN', 'OUT');

-- 2. Créer la table StockMovement
CREATE TABLE StockMovement (
    id SERIAL PRIMARY KEY,
    id_ingredient INTEGER NOT NULL REFERENCES Ingredient(id) ON DELETE CASCADE,
    quantity NUMERIC(10, 4) NOT NULL,
    type movement_type_enum NOT NULL,
    unit unit_enum NOT NULL,
    creation_datetime TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 3. Stocks initiaux (mouvements IN à t=0)
INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime) VALUES
(1, 5.0, 'IN', 'KG', '2024-01-01 00:00:00+00'),  -- Laitue
(2, 4.0, 'IN', 'KG', '2024-01-01 00:00:00+00'),  -- Tomate
(3, 10.0, 'IN', 'KG', '2024-01-01 00:00:00+00'), -- Poulet
(4, 3.0, 'IN', 'KG', '2024-01-01 00:00:00+00'),  -- Chocolat
(5, 2.5, 'IN', 'KG', '2024-01-01 00:00:00+00'); -- Beurre

-- 4. Mouvements OUT de test (2024-01-06 12:00)
INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime) VALUES
(1, 0.2, 'OUT', 'KG', '2024-01-06 12:00:00+00'),  -- Laitue
(2, 0.15, 'OUT', 'KG', '2024-01-06 12:00:00+00'), -- Tomate
(3, 1.0, 'OUT', 'KG', '2024-01-06 12:00:00+00'),  -- Poulet
(4, 0.3, 'OUT', 'KG', '2024-01-06 12:00:00+00'),  -- Chocolat
(5, 0.2, 'OUT', 'KG', '2024-01-06 12:00:00+00'); -- Beurre
