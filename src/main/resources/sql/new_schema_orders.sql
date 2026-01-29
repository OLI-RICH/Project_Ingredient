-- TD4 Annexe 2: Gestion des commandes et ventes

-- 1. Créer la table Order
CREATE TABLE "Order" (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(8) UNIQUE NOT NULL CHECK (reference ~ '^ORD[0-9]{5}$'),  -- Format ORDXXXXX
    total_ht NUMERIC(10, 2) NOT NULL CHECK (total_ht >= 0),
    total_ttc NUMERIC(10, 2) NOT NULL CHECK (total_ttc >= total_ht),
    creation_datetime TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 2. Créer la table DishOrder (lien many-to-many Order <-> Dish avec quantity)
CREATE TABLE DishOrder (
    id SERIAL PRIMARY KEY,
    id_order INTEGER NOT NULL REFERENCES "Order"(id) ON DELETE CASCADE,
    id_dish INTEGER NOT NULL REFERENCES Dish(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    UNIQUE (id_order, id_dish)  -- Pas de doublon plat dans commande
);

-- 3. Sequence pour référence ORDXXXXX (optionnel, mais pour auto-génération si besoin)
CREATE SEQUENCE order_ref_seq START 1;

-- Exemples d'inserts pour tests (optionnel, à adapter)
INSERT INTO "Order" (reference, total_ht, total_ttc, creation_datetime) VALUES ('ORD00001', 50.00, 60.00, '2024-01-07 10:00:00+00');
INSERT INTO DishOrder (id_order, id_dish, quantity) VALUES (1, 1, 2);  -- 2 plats id=1 pour order 1