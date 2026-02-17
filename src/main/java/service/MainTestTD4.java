package service;

import model.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class MainTestTD4 {

    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        // ────────────────────────────────────────────────────────────────
        // PARTIE TD4 : Test niveau de stock
        // ────────────────────────────────────────────────────────────────
        List<Ingredient> ingredients = dataRetriever.getAllIngredients();

        if (ingredients.isEmpty()) {
            System.out.println("Aucun ingrédient trouvé dans la base. Vérifie tes données SQL.");
            return;
        }

        Instant t = LocalDateTime.of(2024, 1, 6, 12, 0, 0)
                .toInstant(ZoneOffset.UTC);

        System.out.println("=== TD4 - Niveau de stock au " + t + " ===\n");

        for (Ingredient ing : ingredients) {
            StockValue stockAtT = ing.getStockValueAt(t);
            // Database-side (push-down) calculation
            StockValue sqlStockAtT = dataRetriever.getStockValueAt(t, ing.getId());
            StockValue stockNow = ing.getStockValueAt(Instant.now());

            System.out.printf("%-12s | Stock à %s : %.2f %s%n",
                    ing.getName(), t, stockAtT.getQuantity(), stockAtT.getUnit());

            System.out.printf("           | Stock actuel : %.2f %s%n",
                    stockNow.getQuantity(), stockNow.getUnit());

                System.out.printf("           | Stock (SQL) à %s : %.2f %s%n",
                    t, sqlStockAtT.getQuantity(), sqlStockAtT.getUnit());

                double diff = stockAtT.getQuantity() - sqlStockAtT.getQuantity();
                System.out.printf("           | Diff OO vs SQL : %.4f %s%n", diff, stockAtT.getUnit());

            System.out.printf("           | Mouvements : %d%n%n",
                    ing.getStockMovementList().size());
        }

        // ────────────────────────────────────────────────────────────────
        // Test ajout mouvement + saveIngredient (TD4)
        // ────────────────────────────────────────────────────────────────
        System.out.println("=== Test ajout mouvement + saveIngredient ===\n");

        Ingredient laitue = findIngredientByName(ingredients, "Laitue");
        if (laitue != null) {
            StockMovement nouveau = new StockMovement();
            nouveau.setValue(new StockValue(1.5, UnitEnum.KG));
            nouveau.setType(MovementTypeEnum.IN);
            nouveau.setCreationDatetime(Instant.now());

            laitue.addStockMovement(nouveau);

            Ingredient saved = dataRetriever.saveIngredient(laitue);
            if (saved != null) {
                System.out.println("Sauvegarde OK pour " + saved.getName());
                System.out.println("Nouveau stock maintenant : " + saved.getStockValueAt(Instant.now()));
            } else {
                System.out.println("Échec saveIngredient");
            }
        } else {
            System.out.println("Ingrédient 'Laitue' non trouvé.");
        }

        // ────────────────────────────────────────────────────────────────
        // ANNEXE 2 : Test création et récupération commande
        // ────────────────────────────────────────────────────────────────
        System.out.println("\n=== ANNEXE 2 - Test création et récupération commande ===\n");

        Order commandeTest = new Order();
        commandeTest.setCreationDatetime(Instant.now());

        // ⚠️ CHANGE LES ID SELON TES PLATS RÉELS (ex. SELECT id, name FROM Dish;)
        DishOrder ligne1 = new DishOrder();
        ligne1.setIdDish(1); // ex. Salade ou Pizza
        ligne1.setQuantity(2);

        DishOrder ligne2 = new DishOrder();
        ligne2.setIdDish(2); // ex. Burger ou autre
        ligne2.setQuantity(1);

        commandeTest.addDishOrder(ligne1);
        commandeTest.addDishOrder(ligne2);

        try {
            Order commandeSauvee = dataRetriever.saveOrder(commandeTest);
            System.out.println("Commande créée avec succès !");
            System.out.println("Référence : " + commandeSauvee.getReference());
            System.out.println("Total HT : " + commandeSauvee.getTotalHT() + " €");
            System.out.println("Total TTC : " + commandeSauvee.getTotalTTC() + " €");
            System.out.println("Statut paiement : " + commandeSauvee.getPaymentStatus());
            System.out.println("Nombre de plats : " + commandeSauvee.getDishOrderList().size());

            // Test récupération
            Order retrouvee = dataRetriever.findOrderByReference(commandeSauvee.getReference());
            System.out.println("\nCommande retrouvée :");
            System.out.println("Référence : " + retrouvee.getReference());
            System.out.println("Total HT : " + retrouvee.getTotalHT());
            System.out.println("Total TTC : " + retrouvee.getTotalTTC());
            System.out.println("Statut paiement : " + retrouvee.getPaymentStatus());
            System.out.println("Lignes : " + retrouvee.getDishOrderList().size());

            // ────────────────────────────────────────────────────────────────
            // K2 : Test transformation commande → vente (après paiement)
            // ────────────────────────────────────────────────────────────────
            System.out.println("\n=== K2 - Test transformation commande → vente ===\n");

            // Simulation paiement (à faire après une commande réussie)
            commandeSauvee.setPaymentStatus(PaymentStatus.PAID);
            dataRetriever.saveOrder(commandeSauvee); // met à jour le statut en base

            Sale vente = dataRetriever.createSaleFromOrder(commandeSauvee.getId());
            System.out.println("Vente créée avec succès !");
            System.out.println("ID vente : " + vente.getId());
            System.out.println("ID commande : " + vente.getOrderId());
            System.out.println("Date vente : " + vente.getSaleDatetime());
            System.out.println("Montant total : " + vente.getTotalAmount() + " €");

        } catch (IllegalStateException e) {
            System.out.println("❌ Stock insuffisant : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur logique : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }

        // ────────────────────────────────────────────────────────────────
        // Test commande avec statut NON-PAID (PENDING)
        // ────────────────────────────────────────────────────────────────
        System.out.println("\n=== Test commande avec statut PENDING (non payée) ===\n");

        Order commandePending = new Order();
        commandePending.setCreationDatetime(Instant.now());

        DishOrder ligne3 = new DishOrder();
        ligne3.setIdDish(3);
        ligne3.setQuantity(1);

        commandePending.addDishOrder(ligne3);

        try {
            Order commandePendingSaved = dataRetriever.saveOrder(commandePending);
            System.out.println("Commande créée avec statut PENDING !");
            System.out.println("Référence : " + commandePendingSaved.getReference());
            System.out.println("Total HT : " + commandePendingSaved.getTotalHT() + " €");
            System.out.println("Total TTC : " + commandePendingSaved.getTotalTTC() + " €");
            System.out.println("Statut paiement : " + commandePendingSaved.getPaymentStatus());
            System.out.println("Nombre de plats : " + commandePendingSaved.getDishOrderList().size());

            // Vérifier qu'on ne peut pas créer une vente si la commande n'est pas payée
            System.out.println("\n--- Tentative création vente avec statut PENDING ---");
            if (commandePendingSaved.getPaymentStatus() != PaymentStatus.PAID) {
                System.out.println("❌ Impossible : La commande doit être PAID pour créer une vente (statut actuel: " 
                    + commandePendingSaved.getPaymentStatus() + ")");
            } else {
                Sale vente = dataRetriever.createSaleFromOrder(commandePendingSaved.getId());
                System.out.println("✅ Vente créée avec succès (montant: " + vente.getTotalAmount() + " €)");
            }

        } catch (IllegalStateException e) {
            System.out.println("❌ Stock insuffisant : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur logique : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Ingredient findIngredientByName(List<Ingredient> list, String name) {
        for (Ingredient ing : list) {
            if (ing.getName().equalsIgnoreCase(name)) {
                return ing;
            }
        }
        return null;
    }
}