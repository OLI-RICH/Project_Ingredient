-- Type enum pour payment_status (K2)
CREATE TYPE payment_status_enum AS ENUM ('PENDING', 'PAID', 'CANCELLED', 'REFUNDED');

-- Table Order (avec payment_status obligatoire)
CREATE TABLE IF NOT EXISTS "Order" (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(8) UNIQUE NOT NULL CHECK (reference ~ '^ORD[0-9]{5}$'),
    total_ht NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (total_ht >= 0),
    total_ttc NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (total_ttc >= total_ht),
    creation_datetime TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    payment_status payment_status_enum NOT NULL DEFAULT 'PENDING'
);

-- Table DishOrder (lien commande ↔ plat)
CREATE TABLE IF NOT EXISTS DishOrder (
    id SERIAL PRIMARY KEY,
    id_order INTEGER NOT NULL REFERENCES "Order"(id) ON DELETE CASCADE,
    id_dish INTEGER NOT NULL REFERENCES Dish(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    UNIQUE (id_order, id_dish) -- pas de doublon plat dans une commande
);

-- Index pour accélérer les recherches (optionnel mais recommandé)
CREATE INDEX idx_order_reference ON "Order"(reference);
CREATE INDEX idx_dishorder_order ON DishOrder(id_order);