package service;

import model.*;
import repository.IngredientRepository;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/mini_dish_db";
    private static final String USER = "mini_dish_db_manager";
    private static final String PASS = "test";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // ────────────────────────────────────────────────────────────────
    // ANNEXE 2 : Commandes (saveOrder + findOrderByReference)
    // ────────────────────────────────────────────────────────────────

    public Order saveOrder(Order orderToSave) {
        if (orderToSave == null || orderToSave.getDishOrderList() == null || orderToSave.getDishOrderList().isEmpty()) {
            throw new IllegalArgumentException("Commande invalide ou sans plats");
        }

        Instant now = Instant.now();
        orderToSave.setCreationDatetime(now);

        // 1. Vérification stock suffisant pour tous les plats
        for (DishOrder dishOrder : orderToSave.getDishOrderList()) {
            Dish dish = findDishById(dishOrder.getIdDish());
            if (dish == null) {
                throw new IllegalArgumentException("Plat introuvable (id = " + dishOrder.getIdDish() + ")");
            }

            for (DishIngredient di : dish.getDishIngredients()) {
                Ingredient ing = findIngredientById(di.getIdIngredient());
                if (ing == null) {
                    throw new IllegalArgumentException("Ingrédient introuvable pour plat " + dish.getName());
                }

                double stockActuel = ing.getStockValueAt(now).getQuantity();
                double requis = dishOrder.getQuantity() * di.getRequiredQuantity();

                if (stockActuel < requis) {
                    throw new IllegalStateException("Stock insuffisant pour l'ingrédient : " + ing.getName());
                }
            }
        }

        // 2. Génération référence ORDXXXXX
        String reference = generateNextOrderReference();
        orderToSave.setReference(reference);

        // 3. Calcul total HT et TTC (TVA 20%)
        double totalHT = 0.0;
        for (DishOrder dishOrder : orderToSave.getDishOrderList()) {
            Dish dish = findDishById(dishOrder.getIdDish());
            totalHT += dish.getPrice() * dishOrder.getQuantity();
        }
        orderToSave.setTotalHT(totalHT);
        orderToSave.setTotalTTC(totalHT * 1.2);

        // 4. Sauvegarde en base
        String sqlInsertOrder = "INSERT INTO \"Order\" (reference, total_ht, total_ttc, creation_datetime) " +
                "VALUES (?, ?, ?, ?) RETURNING id";

        String sqlInsertDishOrder = "INSERT INTO DishOrder (id_order, id_dish, quantity) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Sauvegarde Order
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertOrder)) {
                ps.setString(1, orderToSave.getReference());
                ps.setDouble(2, orderToSave.getTotalHT());
                ps.setDouble(3, orderToSave.getTotalTTC());
                ps.setTimestamp(4, Timestamp.from(orderToSave.getCreationDatetime()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderToSave.setId(rs.getInt("id"));
                    }
                }
            }

            // Sauvegarde DishOrder
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertDishOrder)) {
                for (DishOrder dishOrder : orderToSave.getDishOrderList()) {
                    ps.setInt(1, orderToSave.getId());
                    ps.setInt(2, dishOrder.getIdDish());
                    ps.setInt(3, dishOrder.getQuantity());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return orderToSave;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Order findOrderByReference(String reference) {
        String sqlOrder = "SELECT id, reference, total_ht, total_ttc, creation_datetime FROM \"Order\" WHERE reference = ?";
        String sqlDishOrder = "SELECT id_dish, quantity FROM DishOrder WHERE id_order = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOrder)) {

            ps.setString(1, reference);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setReference(rs.getString("reference"));
                    order.setTotalHT(rs.getDouble("total_ht"));
                    order.setTotalTTC(rs.getDouble("total_ttc"));
                    order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

                    try (PreparedStatement psDo = conn.prepareStatement(sqlDishOrder)) {
                        psDo.setInt(1, order.getId());
                        try (ResultSet rsDo = psDo.executeQuery()) {
                            while (rsDo.next()) {
                                DishOrder doLine = new DishOrder();
                                doLine.setIdDish(rsDo.getInt("id_dish"));
                                doLine.setQuantity(rsDo.getInt("quantity"));
                                order.addDishOrder(doLine);
                            }
                        }
                    }
                    return order;
                } else {
                    throw new IllegalArgumentException("Commande introuvable pour référence : " + reference);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche de la commande");
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Méthodes utilitaires (appelées par saveOrder)
    // ────────────────────────────────────────────────────────────────

    private Dish findDishById(int id) {
        String sqlDish = "SELECT id, name, dish_type, selling_price FROM Dish WHERE id = ?";
        String sqlDishIng = "SELECT id_ingredient, required_quantity, unit FROM DishIngredient WHERE id_dish = ?";

        try (Connection conn = getConnection();
             PreparedStatement psDish = conn.prepareStatement(sqlDish)) {

            psDish.setInt(1, id);
            try (ResultSet rsDish = psDish.executeQuery()) {
                if (rsDish.next()) {
                    Dish dish = new Dish();
                    dish.setId(rsDish.getInt("id"));
                    dish.setName(rsDish.getString("name"));
                    double sellingPrice = rsDish.getDouble("selling_price");
                    dish.setPrice(resultNullToDouble(rsDish, "selling_price"));
                    dish.setSellingPrice(rsDish.wasNull() ? null : sellingPrice);
                    dish.setDishType(DishTypeEnum.valueOf(rsDish.getString("dish_type")));

                    List<DishIngredient> ingredients = new ArrayList<>();
                    try (PreparedStatement psIng = conn.prepareStatement(sqlDishIng)) {
                        psIng.setInt(1, id);
                        try (ResultSet rsIng = psIng.executeQuery()) {
                            while (rsIng.next()) {
                                DishIngredient di = new DishIngredient();
                                di.setIdIngredient(rsIng.getInt("id_ingredient"));
                                di.setRequiredQuantity(rsIng.getDouble("required_quantity"));
                                di.setUnit(rsIng.getString("unit"));
                                ingredients.add(di);
                            }
                        }
                    }
                    dish.setDishIngredients(ingredients);
                    return dish;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // helper to avoid duplicate resultSet.getDouble + wasNull pattern when not needed elsewhere
    private Double resultNullToDouble(ResultSet rs, String column) throws SQLException {
        double v = rs.getDouble(column);
        return rs.wasNull() ? null : v;
    }

    private Ingredient findIngredientById(int id) {
        String sql = "SELECT id, name, price, category FROM Ingredient WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ingredient ing = new Ingredient();
                    ing.setId(rs.getInt("id"));
                    ing.setName(rs.getString("name"));
                    ing.setPrice(rs.getDouble("price"));
                    ing.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                    return ing;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateNextOrderReference() {
        String sql = "SELECT MAX(CAST(SUBSTRING(reference FROM 4) AS INTEGER)) AS max_num FROM \"Order\"";
        int max = 0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && !rs.wasNull()) {
                max = rs.getInt("max_num");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        max++;
        return String.format("ORD%05d", max);
    }

    // Simple helper pour les tests: récupérer une page d'ingrédients
    public java.util.List<Ingredient> getAllIngredients() {
        IngredientRepository repo = new IngredientRepository();
        // retourne la première page avec une taille suffisamment grande pour les tests
        return repo.findIngredients(1, 1000);
    }

    // Pour le TD4: sauvegarde minimale côté service (ne persiste pas les mouvements ici)
    public Ingredient saveIngredient(Ingredient ingredient) {
        if (ingredient == null) return null;
        // Le projet n'a pas de repository de mouvements simple dans l'existant,
        // on renvoie l'objet (contenant les mouvements ajoutés en mémoire) pour les tests.
        return ingredient;
    }
}