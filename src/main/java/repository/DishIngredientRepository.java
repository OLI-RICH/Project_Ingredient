package repository;

import model.Dish;
import model.DishIngredient;
import model.Ingredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishIngredientRepository {

    private final IngredientRepository ingredientRepository;

    public DishIngredientRepository() {
        this.ingredientRepository = new IngredientRepository();
    }

    public List<DishIngredient> findByDishId(Integer dishId, Connection connection) throws SQLException {
        String sql = "SELECT id_dish, id_ingredient, required_quantity, unit " +
                "FROM DishIngredient WHERE id_dish = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, dishId);
        ResultSet rs = ps.executeQuery();

        List<DishIngredient> result = new ArrayList<>();

        while (rs.next()) {
            DishIngredient di = new DishIngredient();

            Dish dish = new Dish();
            dish.setId(rs.getInt("id_dish"));
            di.setDish(dish);

            Integer ingredientId = rs.getInt("id_ingredient");
            Ingredient ingredient = ingredientRepository.findById(ingredientId);
            di.setIngredient(ingredient);

            di.setRequiredQuantity(rs.getDouble("required_quantity"));
            di.setUnit(rs.getString("unit"));

            result.add(di);
        }

        return result;
    }

    public void deleteByDishId(Integer dishId, Connection connection) throws SQLException {
        String sql = "DELETE FROM DishIngredient WHERE id_dish = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, dishId);
        ps.executeUpdate();
    }

    public void saveForDish(Dish dish, Connection connection) throws SQLException {
        if (dish.getDishIngredients() == null) {
            return;
        }

        String sql = "INSERT INTO DishIngredient (id_dish, id_ingredient, required_quantity, unit) " +
                "VALUES (?, ?, ?, ?::unit_enum)";
        PreparedStatement ps = connection.prepareStatement(sql);

        for (DishIngredient di : dish.getDishIngredients()) {
            ps.setInt(1, dish.getId());
            ps.setInt(2, di.getIngredient().getId());
            ps.setDouble(3, di.getRequiredQuantity());
            ps.setString(4, di.getUnit());
            ps.executeUpdate();
        }
    }
}
