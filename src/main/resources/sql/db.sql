-- Création de la base de données
CREATE DATABASE mini_dish_db;

-- Création de l'utilisateur
CREATE USER mini_dish_db_manager WITH PASSWORD 'your_password_here';

-- Attribution des privilèges
GRANT ALL PRIVILEGES ON DATABASE mini_dish_db TO mini_dish_db_manager;

-- Se connecter à la base de données mini_dish_db puis exécuter:
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mini_dish_db_manager;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mini_dish_db_manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO mini_dish_db_manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO mini_dish_db_manager;