package repository;

import database.DBConnection;
import model.Dish;
import model.DishIngredient;
import model.DishTypeEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishRepository {

    private final DBConnection dbConnection;
    private final DishIngredientRepository dishIngredientRepository;

    public DishRepository() {
        this.dbConnection = new DBConnection();
        this.dishIngredientRepository = new DishIngredientRepository();
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

            List<DishIngredient> dishIngredients =
                    dishIngredientRepository.findByDishId(id, connection);
            dish.setDishIngredients(dishIngredients);

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
                String insertSql = "INSERT INTO Dish (name, dish_type, selling_price) " +
                        "VALUES (?, ?::dish_type_enum, ?) RETURNING id";
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

            // supprimer les anciens liens DishIngredient
            dishIngredientRepository.deleteByDishId(dishToSave.getId(), connection);

            // insérer les nouveaux liens DishIngredient
            dishIngredientRepository.saveForDish(dishToSave, connection);

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
        String sql =
                "SELECT DISTINCT d.id, d.name, d.dish_type, d.selling_price " +
                        "FROM Dish d " +
                        "JOIN DishIngredient di ON d.id = di.id_dish " +
                        "JOIN Ingredient i ON di.id_ingredient = i.id " +
                        "WHERE LOWER(i.name) LIKE LOWER(?)";

        Connection connection = dbConnection.getDBConnection();
        List<Dish> dishes = new ArrayList<>();

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

                List<DishIngredient> dishIngredients =
                        dishIngredientRepository.findByDishId(dish.getId(), connection);
                dish.setDishIngredients(dishIngredients);

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
