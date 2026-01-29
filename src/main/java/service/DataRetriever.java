package service;

import model.*;

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
    // Méthodes TD4 (pour que MainTestTD4 compile et affiche les stocks)
    // ────────────────────────────────────────────────────────────────

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sqlIngredients = "SELECT id, name, price, category FROM Ingredient ORDER BY id";
        String sqlMovements = "SELECT id, quantity, type, unit, creation_datetime " +
                "FROM StockMovement WHERE id_ingredient = ? ORDER BY creation_datetime ASC";

        try (Connection conn = getConnection();
             PreparedStatement psIng = conn.prepareStatement(sqlIngredients);
             ResultSet rsIng = psIng.executeQuery()) {

            while (rsIng.next()) {
                Ingredient ing = new Ingredient(
                        rsIng.getInt("id"),
                        rsIng.getString("name"),
                        rsIng.getDouble("price"),
                        CategoryEnum.valueOf(rsIng.getString("category"))
                );

                // Chargement des mouvements (essentiel pour ne plus avoir 0 partout)
                try (PreparedStatement psMvt = conn.prepareStatement(sqlMovements)) {
                    psMvt.setInt(1, ing.getId());
                    try (ResultSet rsMvt = psMvt.executeQuery()) {
                        while (rsMvt.next()) {
                            StockValue value = new StockValue(
                                    rsMvt.getDouble("quantity"),
                                    UnitEnum.valueOf(rsMvt.getString("unit"))
                            );
                            StockMovement mvt = new StockMovement();
                            mvt.setId(rsMvt.getInt("id"));
                            mvt.setValue(value);
                            mvt.setType(MovementTypeEnum.valueOf(rsMvt.getString("type")));
                            mvt.setCreationDatetime(rsMvt.getTimestamp("creation_datetime").toInstant());
                            ing.addStockMovement(mvt);
                        }
                    }
                }
                ingredients.add(ing);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    public Ingredient saveIngredient(Ingredient toSave) {
        if (toSave == null) return null;

        String sqlInsertIngredient = "INSERT INTO Ingredient (name, price, category) " +
                "VALUES (?, ?, ?::category_enum) RETURNING id";

        String sqlUpdateIngredient = "UPDATE Ingredient SET name = ?, price = ?, category = ?::category_enum " +
                "WHERE id = ?";

        String sqlInsertMovementWithId = "INSERT INTO StockMovement (id, id_ingredient, quantity, type, unit, creation_datetime) " +
                "VALUES (?, ?, ?, ?::movement_type_enum, ?::unit_enum, ?) " +
                "ON CONFLICT (id) DO NOTHING";

        String sqlInsertMovementNoId = "INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime) " +
                "VALUES (?, ?, ?::movement_type_enum, ?::unit_enum, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            if (toSave.getId() == null) {
                try (PreparedStatement ps = conn.prepareStatement(sqlInsertIngredient)) {
                    ps.setString(1, toSave.getName());
                    ps.setDouble(2, toSave.getPrice());
                    ps.setString(3, toSave.getCategory().name());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) toSave.setId(rs.getInt("id"));
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateIngredient)) {
                    ps.setString(1, toSave.getName());
                    ps.setDouble(2, toSave.getPrice());
                    ps.setString(3, toSave.getCategory().name());
                    ps.setInt(4, toSave.getId());
                    ps.executeUpdate();
                }
            }

            List<StockMovement> movements = toSave.getStockMovementList();
            if (movements != null && !movements.isEmpty()) {
                try (PreparedStatement psWithId = conn.prepareStatement(sqlInsertMovementWithId);
                     PreparedStatement psNoId = conn.prepareStatement(sqlInsertMovementNoId)) {

                    for (StockMovement mvt : movements) {
                        if (mvt.getId() != null && mvt.getId() > 0) {
                            psWithId.setInt(1, mvt.getId());
                            psWithId.setInt(2, toSave.getId());
                            psWithId.setDouble(3, mvt.getValue().getQuantity());
                            psWithId.setString(4, mvt.getType().name());
                            psWithId.setString(5, mvt.getValue().getUnit().name());
                            psWithId.setTimestamp(6, Timestamp.from(mvt.getCreationDatetime()));
                            psWithId.addBatch();
                        } else {
                            psNoId.setInt(1, toSave.getId());
                            psNoId.setDouble(2, mvt.getValue().getQuantity());
                            psNoId.setString(3, mvt.getType().name());
                            psNoId.setString(4, mvt.getValue().getUnit().name());
                            psNoId.setTimestamp(5, Timestamp.from(mvt.getCreationDatetime()));
                            psNoId.addBatch();
                        }
                    }
                    psWithId.executeBatch();
                    psNoId.executeBatch();
                }
            }

            conn.commit();
            return toSave;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ────────────────────────────────────────────────────────────────
    // ANNEXE 2 + K2 : Commandes et transformation en vente
    // ────────────────────────────────────────────────────────────────

    public Order saveOrder(Order orderToSave) {
        if (orderToSave == null || orderToSave.getDishOrderList() == null || orderToSave.getDishOrderList().isEmpty()) {
            throw new IllegalArgumentException("Commande invalide ou sans plats");
        }

        Instant now = Instant.now();
        orderToSave.setCreationDatetime(now);

        // Vérification stock
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

        String reference = generateNextOrderReference();
        orderToSave.setReference(reference);

        double totalHT = 0.0;
        for (DishOrder dishOrderLine : orderToSave.getDishOrderList()) {
            Dish dish = findDishById(dishOrderLine.getIdDish());
            totalHT += dish.getPrice() * dishOrderLine.getQuantity();
        }
        orderToSave.setTotalHT(totalHT);
        orderToSave.setTotalTTC(totalHT * 1.2);

        String sqlInsertOrder = "INSERT INTO \"Order\" (reference, total_ht, total_ttc, creation_datetime, payment_status) " +
                "VALUES (?, ?, ?, ?, ?::payment_status_enum) RETURNING id";

        String sqlInsertDishOrder = "INSERT INTO DishOrder (id_order, id_dish, quantity) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlInsertOrder)) {
                ps.setString(1, orderToSave.getReference());
                ps.setDouble(2, orderToSave.getTotalHT());
                ps.setDouble(3, orderToSave.getTotalTTC());
                ps.setTimestamp(4, Timestamp.from(orderToSave.getCreationDatetime()));
                ps.setString(5, orderToSave.getPaymentStatus().name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderToSave.setId(rs.getInt("id"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlInsertDishOrder)) {
                for (DishOrder dishOrderLine : orderToSave.getDishOrderList()) {
                    ps.setInt(1, orderToSave.getId());
                    ps.setInt(2, dishOrderLine.getIdDish());
                    ps.setInt(3, dishOrderLine.getQuantity());
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
        String sqlOrder = "SELECT id, reference, total_ht, total_ttc, creation_datetime, payment_status " +
                "FROM \"Order\" WHERE reference = ?";

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
                    order.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));

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
            throw new RuntimeException("Erreur recherche commande");
        }
    }

    // ────────────────────────────────────────────────────────────────
    // K2 : Transformation commande → vente
    // ────────────────────────────────────────────────────────────────

    public Sale createSaleFromOrder(Integer orderId) {
        Order order = findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Commande introuvable : id = " + orderId);
        }

        // Vérification paiement (exigence K2)
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("La commande n'est pas payée (statut = " + order.getPaymentStatus() + ")");
        }

        // Création de la vente
        Sale sale = new Sale();
        sale.setOrderId(order.getId());
        sale.setSaleDatetime(Instant.now());
        sale.setTotalAmount(order.getTotalTTC());

        // Création des mouvements OUT pour diminuer le stock
        Instant now = Instant.now();
        for (DishOrder dishOrder : order.getDishOrderList()) {
            Dish dish = findDishById(dishOrder.getIdDish());
            if (dish == null) continue;

            for (DishIngredient di : dish.getDishIngredients()) {
                double quantityUsed = dishOrder.getQuantity() * di.getRequiredQuantity();

                StockMovement outMovement = new StockMovement();
                outMovement.setValue(new StockValue(quantityUsed, UnitEnum.KG));
                outMovement.setType(MovementTypeEnum.OUT);
                outMovement.setCreationDatetime(now);

                Ingredient ing = findIngredientById(di.getIdIngredient());
                if (ing != null) {
                    ing.addStockMovement(outMovement);
                    saveIngredient(ing); // persiste le mouvement OUT
                }
            }
        }

        // Sauvegarde de la vente (à adapter selon ta table Sale)
        return saveSale(sale);
    }

    // Méthode utilitaire : récupérer une commande par ID
    private Order findOrderById(Integer orderId) {
        String sqlOrder = "SELECT id, reference, total_ht, total_ttc, creation_datetime, payment_status " +
                "FROM \"Order\" WHERE id = ?";

        String sqlDishOrder = "SELECT id_dish, quantity FROM DishOrder WHERE id_order = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOrder)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setReference(rs.getString("reference"));
                    order.setTotalHT(rs.getDouble("total_ht"));
                    order.setTotalTTC(rs.getDouble("total_ttc"));
                    order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
                    order.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));

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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthode à implémenter pour sauvegarder une vente (exemple minimal)
    private Sale saveSale(Sale sale) {
        String sql = "INSERT INTO Sale (id_order, sale_datetime, total_amount) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sale.getOrderId());
            ps.setTimestamp(2, Timestamp.from(sale.getSaleDatetime()));
            ps.setDouble(3, sale.getTotalAmount());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sale.setId(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sale;
    }

    // Méthodes utilitaires
    private Dish findDishById(Integer idDish) {
        String sql = "SELECT id, name, selling_price, dish_type FROM Dish WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDish);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dish dish = new Dish();
                    dish.setId(rs.getInt("id"));
                    dish.setName(rs.getString("name"));
                    dish.setPrice(rs.getDouble("selling_price"));
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                    
                    // Charger les ingrédients du plat
                    String sqlDishIng = "SELECT id_dish, id_ingredient, required_quantity, unit FROM DishIngredient WHERE id_dish = ?";
                    try (PreparedStatement psDi = conn.prepareStatement(sqlDishIng)) {
                        psDi.setInt(1, idDish);
                        try (ResultSet rsDi = psDi.executeQuery()) {
                            while (rsDi.next()) {
                                DishIngredient di = new DishIngredient();
                                di.setIdDish(rsDi.getInt("id_dish"));
                                di.setIdIngredient(rsDi.getInt("id_ingredient"));
                                di.setRequiredQuantity(rsDi.getDouble("required_quantity"));
                                dish.addDishIngredient(di);
                            }
                        }
                    }
                    return dish;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Ingredient findIngredientById(Integer idIngredient) {
        String sql = "SELECT id, name, price, category FROM Ingredient WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idIngredient);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ingredient ing = new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            CategoryEnum.valueOf(rs.getString("category"))
                    );
                    
                    // Charger les mouvements de stock
                    String sqlMovements = "SELECT id, quantity, type, unit, creation_datetime " +
                            "FROM StockMovement WHERE id_ingredient = ? ORDER BY creation_datetime ASC";
                    try (PreparedStatement psMvt = conn.prepareStatement(sqlMovements)) {
                        psMvt.setInt(1, idIngredient);
                        try (ResultSet rsMvt = psMvt.executeQuery()) {
                            while (rsMvt.next()) {
                                StockValue value = new StockValue(
                                        rsMvt.getDouble("quantity"),
                                        UnitEnum.valueOf(rsMvt.getString("unit"))
                                );
                                StockMovement mvt = new StockMovement();
                                mvt.setId(rsMvt.getInt("id"));
                                mvt.setValue(value);
                                mvt.setType(MovementTypeEnum.valueOf(rsMvt.getString("type")));
                                mvt.setCreationDatetime(rsMvt.getTimestamp("creation_datetime").toInstant());
                                ing.addStockMovement(mvt);
                            }
                        }
                    }
                    return ing;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateNextOrderReference() {
        String sql = "SELECT COUNT(*) + 1 as next_id FROM \"Order\"";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int nextId = rs.getInt("next_id");
                    return String.format("ORD%05d", nextId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "ORD00001";
    }
}