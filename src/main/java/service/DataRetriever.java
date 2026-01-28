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

    public Ingredient saveIngredient(Ingredient toSave) {
        if (toSave == null) {
            throw new IllegalArgumentException("L'ingrédient à sauvegarder ne peut pas être null");
        }

        String sqlInsertIngredient =
                "INSERT INTO Ingredient (name, price, category) " +
                        "VALUES (?, ?, ?::category_enum) RETURNING id";

        String sqlUpdateIngredient =
                "UPDATE Ingredient SET name = ?, price = ?, category = ?::category_enum " +
                        "WHERE id = ?";

        String sqlInsertMovementWithId =
                "INSERT INTO StockMovement (id, id_ingredient, quantity, type, unit, creation_datetime) " +
                        "VALUES (?, ?, ?, ?::movement_type_enum, ?::unit_enum, ?) " +
                        "ON CONFLICT (id) DO NOTHING";

        String sqlInsertMovementNoId =
                "INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime) " +
                        "VALUES (?, ?, ?::movement_type_enum, ?::unit_enum, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // 1. Sauvegarde ou mise à jour de l'ingrédient
            if (toSave.getId() == null) {
                // INSERT nouvel ingrédient
                try (PreparedStatement ps = conn.prepareStatement(sqlInsertIngredient)) {
                    ps.setString(1, toSave.getName());
                    ps.setDouble(2, toSave.getPrice());
                    ps.setString(3, toSave.getCategory().name());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            toSave.setId(rs.getInt("id"));
                        } else {
                            throw new SQLException("Impossible de récupérer l'ID après insertion");
                        }
                    }
                }
            } else {
                // UPDATE ingrédient existant
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateIngredient)) {
                    ps.setString(1, toSave.getName());
                    ps.setDouble(2, toSave.getPrice());
                    ps.setString(3, toSave.getCategory().name());
                    ps.setInt(4, toSave.getId());
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected == 0) {
                        System.err.println("Aucun ingrédient trouvé pour l'ID " + toSave.getId());
                    }
                }
            }

            // 2. Sauvegarde des mouvements
            List<StockMovement> movements = toSave.getStockMovementList();
            if (movements != null && !movements.isEmpty()) {
                try (PreparedStatement psWithId = conn.prepareStatement(sqlInsertMovementWithId);
                     PreparedStatement psNoId = conn.prepareStatement(sqlInsertMovementNoId)) {

                    for (StockMovement mvt : movements) {
                        if (mvt.getId() != null && mvt.getId() > 0) {
                            // Mouvement avec ID existant → ON CONFLICT DO NOTHING
                            psWithId.setInt(1, mvt.getId());
                            psWithId.setInt(2, toSave.getId());
                            psWithId.setDouble(3, mvt.getValue().getQuantity());
                            psWithId.setString(4, mvt.getType().name());
                            psWithId.setString(5, mvt.getValue().getUnit().name());
                            psWithId.setTimestamp(6, Timestamp.from(mvt.getCreationDatetime()));
                            psWithId.addBatch();
                        } else {
                            // Nouveau mouvement (pas d'ID) → SERIAL automatique
                            psNoId.setInt(1, toSave.getId());
                            psNoId.setDouble(2, mvt.getValue().getQuantity());
                            psNoId.setString(3, mvt.getType().name());
                            psNoId.setString(4, mvt.getValue().getUnit().name());
                            psNoId.setTimestamp(5, Timestamp.from(mvt.getCreationDatetime()));
                            psNoId.addBatch();
                        }
                    }

                    // Exécute les batches
                    psWithId.executeBatch();
                    psNoId.executeBatch();
                }
            }

            conn.commit();
            return toSave;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de saveIngredient : " + e.getMessage());
            return null;
        }
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();

        String sqlIngredients = "SELECT id, name, price, category FROM Ingredient ORDER BY id";
        String sqlMovements =
                "SELECT id, quantity, type, unit, creation_datetime " +
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

                try (PreparedStatement psMvt = conn.prepareStatement(sqlMovements)) {
                    psMvt.setInt(1, ing.getId());
                    try (ResultSet rsMvt = psMvt.executeQuery()) {
                        while (rsMvt.next()) {
                            StockValue value = new StockValue(
                                    rsMvt.getDouble("quantity"),
                                    UnitEnum.valueOf(rsMvt.getString("unit"))
                            );
                            StockMovement mvt = new StockMovement(
                                    rsMvt.getInt("id"),
                                    value,
                                    MovementTypeEnum.valueOf(rsMvt.getString("type")),
                                    rsMvt.getTimestamp("creation_datetime").toInstant()
                            );
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
}