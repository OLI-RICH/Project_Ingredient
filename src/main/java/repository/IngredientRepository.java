package repository;

import database.DBConnection;
import model.CategoryEnum;
import model.Ingredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientRepository {

    private DBConnection dbConnection;

    public IngredientRepository() {
        this.dbConnection = new DBConnection();
    }

    public List<Ingredient> findIngredients(int page, int size) {
        String sql = "SELECT id, name, price, category, id_dish, required_quantity FROM Ingredient ORDER BY id LIMIT ? OFFSET ?";
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, size);
            preparedStatement.setInt(2, (page - 1) * size);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

                int idDish = resultSet.getInt("id_dish");
                ingredient.setIdDish(resultSet.wasNull() ? null : idDish);

                double requiredQty = resultSet.getDouble("required_quantity");
                ingredient.setRequiredQuantity(resultSet.wasNull() ? null : requiredQty);

                ingredients.add(ingredient);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        dbConnection.attemptCloseConnection(connection);
        return ingredients;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> createdIngredients = new ArrayList<>();

        try {
            connection.setAutoCommit(false);

            String checkSql = "SELECT COUNT(*) FROM Ingredient WHERE name = ?";
            String insertSql = "INSERT INTO Ingredient (name, price, category, id_dish, required_quantity) VALUES (?, ?, ?::category_enum, ?, ?) RETURNING id";

            for (Ingredient ingredient : newIngredients) {
                PreparedStatement checkStatement = connection.prepareStatement(checkSql);
                checkStatement.setString(1, ingredient.getName());
                ResultSet checkResult = checkStatement.executeQuery();
                checkResult.next();

                if (checkResult.getInt(1) > 0) {
                    connection.rollback();
                    throw new RuntimeException("L'ingrédient '" + ingredient.getName() + "' existe déjà");
                }

                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                insertStatement.setString(1, ingredient.getName());
                insertStatement.setDouble(2, ingredient.getPrice());
                insertStatement.setString(3, ingredient.getCategory().name());

                if (ingredient.getIdDish() != null) {
                    insertStatement.setInt(4, ingredient.getIdDish());
                } else {
                    insertStatement.setNull(4, Types.INTEGER);
                }

                if (ingredient.getRequiredQuantity() != null) {
                    insertStatement.setDouble(5, ingredient.getRequiredQuantity());
                } else {
                    insertStatement.setNull(5, Types.NUMERIC);
                }

                ResultSet resultSet = insertStatement.executeQuery();
                if (resultSet.next()) {
                    ingredient.setId(resultSet.getInt("id"));
                    createdIngredients.add(ingredient);
                }
            }

            connection.commit();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw e;
        }

        dbConnection.attemptCloseConnection(connection);
        return createdIngredients;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category,
                                                      String dishName, int page, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT i.id, i.name, i.price, i.category, i.id_dish, i.required_quantity " +
                        "FROM Ingredient i " +
                        "LEFT JOIN Dish d ON i.id_dish = d.id " +
                        "WHERE 1=1"
        );

        List<Object> parameters = new ArrayList<>();

        if (ingredientName != null && !ingredientName.isEmpty()) {
            sql.append(" AND LOWER(i.name) LIKE LOWER(?)");
            parameters.add("%" + ingredientName + "%");
        }

        if (category != null) {
            sql.append(" AND i.category = ?::category_enum");
            parameters.add(category.name());
        }

        if (dishName != null && !dishName.isEmpty()) {
            sql.append(" AND LOWER(d.name) LIKE LOWER(?)");
            parameters.add("%" + dishName + "%");
        }

        sql.append(" ORDER BY i.id LIMIT ? OFFSET ?");

        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());

            int paramIndex = 1;
            for (Object param : parameters) {
                preparedStatement.setString(paramIndex++, param.toString());
            }
            preparedStatement.setInt(paramIndex++, size);
            preparedStatement.setInt(paramIndex, (page - 1) * size);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

                int idDish = resultSet.getInt("id_dish");
                ingredient.setIdDish(resultSet.wasNull() ? null : idDish);

                double requiredQty = resultSet.getDouble("required_quantity");
                ingredient.setRequiredQuantity(resultSet.wasNull() ? null : requiredQty);

                ingredients.add(ingredient);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        dbConnection.attemptCloseConnection(connection);
        return ingredients;
    }

    List<Ingredient> findIngredientsByDishId(Integer dishId, Connection connection) throws SQLException {
        String sql = "SELECT id, name, price, category, id_dish, required_quantity FROM Ingredient WHERE id_dish = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, dishId);
        ResultSet resultSet = preparedStatement.executeQuery();

        List<Ingredient> ingredients = new ArrayList<>();
        while (resultSet.next()) {
            Ingredient ingredient = new Ingredient();
            ingredient.setId(resultSet.getInt("id"));
            ingredient.setName(resultSet.getString("name"));
            ingredient.setPrice(resultSet.getDouble("price"));
            ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

            int idDish = resultSet.getInt("id_dish");
            ingredient.setIdDish(resultSet.wasNull() ? null : idDish);

            double requiredQty = resultSet.getDouble("required_quantity");
            ingredient.setRequiredQuantity(resultSet.wasNull() ? null : requiredQty);

            ingredients.add(ingredient);
        }

        return ingredients;
    }
}