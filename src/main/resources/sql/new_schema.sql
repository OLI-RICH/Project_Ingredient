-- TD3: Normalisation de données et relation ManyToMany
-- Date: 15 janvier 2026

-- Étape 1: Créer les types ENUM
CREATE TYPE category_enum AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE dish_type_enum AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TYPE unit_enum AS ENUM ('PIECE', 'KG', 'L', 'ML', 'G');

-- Étape 2: Créer la table Dish
CREATE TABLE Dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dish_type dish_type_enum NOT NULL,
    selling_price NUMERIC(10, 2)
);

-- Étape 3: Créer la table Ingredient (normalisée, sans id_dish)
CREATE TABLE Ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    price NUMERIC(10, 2) NOT NULL,
    category category_enum NOT NULL
);

-- Étape 4: Créer la table de jointure DishIngredient
CREATE TABLE DishIngredient (
    id_dish INTEGER NOT NULL,
    id_ingredient INTEGER NOT NULL,
    required_quantity NUMERIC(10, 2) NOT NULL,
    unit unit_enum NOT NULL,
    PRIMARY KEY (id_dish, id_ingredient),
    FOREIGN KEY (id_dish) REFERENCES Dish(id) ON DELETE CASCADE,
    FOREIGN KEY (id_ingredient) REFERENCES Ingredient(id) ON DELETE CASCADE
);