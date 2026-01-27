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
        String sql = "SELECT id, name, price, category FROM Ingredient ORDER BY id LIMIT ? OFFSET ?";
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, size);
            preparedStatement.setInt(2, (page - 1) * size);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ingredients.add(mapResultSetToIngredient(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.attemptCloseConnection(connection);
        }

        return ingredients;
    }

    public Ingredient findById(Integer id) {
        String sql = "SELECT id, name, price, category FROM Ingredient WHERE id = ?";
        Connection connection = dbConnection.getDBConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToIngredient(resultSet);
            }

            throw new RuntimeException("Ingrédient avec id " + id + " introuvable");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.attemptCloseConnection(connection);
        }
    }

    public Ingredient findByName(String name) {
        String sql = "SELECT id, name, price, category FROM Ingredient WHERE name = ?";
        Connection connection = dbConnection.getDBConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToIngredient(resultSet);
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.attemptCloseConnection(connection);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> createdIngredients = new ArrayList<>();

        try {
            connection.setAutoCommit(false);

            String checkSql = "SELECT COUNT(*) FROM Ingredient WHERE name = ?";
            String insertSql = "INSERT INTO Ingredient (name, price, category) VALUES (?, ?, ?::category_enum) RETURNING id";

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
        } finally {
            dbConnection.attemptCloseConnection(connection);
        }

        return createdIngredients;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category,
                                                      String dishName, int page, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT i.id, i.name, i.price, i.category " +
                        "FROM Ingredient i "
        );

        if (dishName != null && !dishName.isEmpty()) {
            sql.append("JOIN DishIngredient di ON i.id = di.id_ingredient ");
            sql.append("JOIN Dish d ON di.id_dish = d.id ");
        }

        sql.append("WHERE 1=1 ");

        List<Object> parameters = new ArrayList<>();

        if (ingredientName != null && !ingredientName.isEmpty()) {
            sql.append("AND LOWER(i.name) LIKE LOWER(?) ");
            parameters.add("%" + ingredientName + "%");
        }

        if (category != null) {
            sql.append("AND i.category = ?::category_enum ");
            parameters.add(category.name());
        }

        if (dishName != null && !dishName.isEmpty()) {
            sql.append("AND LOWER(d.name) LIKE LOWER(?) ");
            parameters.add("%" + dishName + "%");
        }

        sql.append("ORDER BY i.id LIMIT ? OFFSET ?");

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
                ingredients.add(mapResultSetToIngredient(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.attemptCloseConnection(connection);
        }

        return ingredients;
    }

    private Ingredient mapResultSetToIngredient(ResultSet resultSet) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(resultSet.getInt("id"));
        ingredient.setName(resultSet.getString("name"));
        ingredient.setPrice(resultSet.getDouble("price"));
        ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

        return ingredient;
    }
}