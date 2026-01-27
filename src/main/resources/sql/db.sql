CREATE USER mini_dish_db_manager WITH PASSWORD 'test';

-- Création de la base de données
CREATE DATABASE mini_dish_db;

-- Attribution des privilèges
GRANT ALL PRIVILEGES ON DATABASE mini_dish_db TO mini_dish_db_manager;