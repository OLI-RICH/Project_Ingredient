
CREATE TABLE IF NOT EXISTS Ingredient_New (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    price NUMERIC(10, 2) NOT NULL,
    category category_enum NOT NULL
);

CREATE TYPE unit_enum AS ENUM ('PIECE', 'KG', 'L', 'ML', 'G');

CREATE TABLE IF NOT EXISTS DishIngredient (
    id_dish INTEGER NOT NULL,
    id_ingredient INTEGER NOT NULL,
    required_quantity NUMERIC(10, 2) NOT NULL,
    unit unit_enum NOT NULL,
    PRIMARY KEY (id_dish, id_ingredient),
    FOREIGN KEY (id_dish) REFERENCES Dish(id) ON DELETE CASCADE,
    FOREIGN KEY (id_ingredient) REFERENCES Ingredient_New(id) ON DELETE CASCADE
);

INSERT INTO Ingredient_New (name, price, category)
SELECT DISTINCT ON (name) name, price, category
FROM Ingredient
ON CONFLICT (name) DO NOTHING;

INSERT INTO DishIngredient (id_dish, id_ingredient, required_quantity, unit)

SELECT
    i.id_dish,
    in2.id,
    COALESCE(i.required_quantity, 1.0),
    'PIECE'::unit_enum
FROM Ingredient i
JOIN Ingredient_New in2 ON i.name = in2.name
WHERE i.id_dish IS NOT NULL
ON CONFLICT (id_dish, id_ingredient) DO NOTHING;

DROP TABLE IF EXISTS Ingredient CASCADE;

ALTER TABLE Ingredient_New RENAME TO Ingredient;

ALTER TABLE Dish
ADD COLUMN IF NOT EXISTS selling_price NUMERIC(10, 2);