package repository;

import database.DBConnection;
import model.Dish;
import model.DishTypeEnum;
import model.Ingredient;

import java.sql.*;
import java.util.List;

public class DishRepository {

    private DBConnection dbConnection;
    private IngredientRepository ingredientRepository;

    public DishRepository() {
        this.dbConnection = new DBConnection();
        this.ingredientRepository = new IngredientRepository();
    }

    public Dish findDishById(Integer id) {
        String sql = "SELECT d.id, d.name, d.dish_type, d.selling_price FROM Dish d WHERE d.id = ?";
        Connection connection = dbConnection.getDBConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new RuntimeException("Plat avec id " + id + " introuvable");
            }

            Dish dish = new Dish();
            dish.setId(resultSet.getInt("id"));
            dish.setName(resultSet.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));

            double sellingPrice = resultSet.getDouble("selling_price");
            dish.setSellingPrice(resultSet.wasNull() ? null : sellingPrice);

            dish.setIngredients(ingredientRepository.findIngredientsByDishId(id, connection));

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.attemptCloseConnection(connection);
        }
    }

    public Dish saveDish(Dish dishToSave) {
        Connection connection = dbConnection.getDBConnection();

        try {
            connection.setAutoCommit(false);

            if (dishToSave.getId() == null) {
                String insertSql = "INSERT INTO Dish (name, dish_type, selling_price) VALUES (?, ?::dish_type_enum, ?) RETURNING id";
                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                insertStatement.setString(1, dishToSave.getName());
                insertStatement.setString(2, dishToSave.getDishType().name());

                if (dishToSave.getSellingPrice() != null) {
                    insertStatement.setDouble(3, dishToSave.getSellingPrice());
                } else {
                    insertStatement.setNull(3, Types.NUMERIC);
                }

                ResultSet resultSet = insertStatement.executeQuery();
                if (resultSet.next()) {
                    dishToSave.setId(resultSet.getInt("id"));
                }
            } else {
                String updateSql = "UPDATE Dish SET name = ?, dish_type = ?::dish_type_enum, selling_price = ? WHERE id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, dishToSave.getName());
                updateStatement.setString(2, dishToSave.getDishType().name());

                if (dishToSave.getSellingPrice() != null) {
                    updateStatement.setDouble(3, dishToSave.getSellingPrice());
                } else {
                    updateStatement.setNull(3, Types.NUMERIC);
                }

                updateStatement.setInt(4, dishToSave.getId());
                updateStatement.executeUpdate();
            }

            String dissociateSql = "UPDATE Ingredient SET id_dish = NULL WHERE id_dish = ?";
            PreparedStatement dissociateStatement = connection.prepareStatement(dissociateSql);
            dissociateStatement.setInt(1, dishToSave.getId());
            dissociateStatement.executeUpdate();

            if (dishToSave.getIngredients() != null && !dishToSave.getIngredients().isEmpty()) {
                String associateSql = "UPDATE Ingredient SET id_dish = ? WHERE name = ?";
                PreparedStatement associateStatement = connection.prepareStatement(associateSql);

                for (Ingredient ingredient : dishToSave.getIngredients()) {
                    associateStatement.setInt(1, dishToSave.getId());
                    associateStatement.setString(2, ingredient.getName());
                    associateStatement.executeUpdate();
                }
            }

            connection.commit();
            connection.setAutoCommit(true);

            return findDishById(dishToSave.getId());

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } finally {
            dbConnection.attemptCloseConnection(connection);
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        String sql = "SELECT DISTINCT d.id, d.name, d.dish_type, d.selling_price " +
                "FROM Dish d " +
                "JOIN Ingredient i ON d.id = i.id_dish " +
                "WHERE LOWER(i.name) LIKE LOWER(?)";

        Connection connection = dbConnection.getDBConnection();
        List<Dish> dishes = new java.util.ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "%" + ingredientName + "%");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("id"));
                dish.setName(resultSet.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));

                double sellingPrice = resultSet.getDouble("selling_price");
                dish.setSellingPrice(resultSet.wasNull() ? null : sellingPrice);

                dish.setIngredients(ingredientRepository.findIngredientsByDishId(dish.getId(), connection));
                dishes.add(dish);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.attemptCloseConnection(connection);
        }

        return dishes;
    }
}