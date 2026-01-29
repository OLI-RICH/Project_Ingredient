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
            StockValue stockNow = ing.getStockValueAt(Instant.now());

            System.out.printf("%-12s | Stock à %s : %.2f %s%n",
                    ing.getName(), t, stockAtT.getQuantity(), stockAtT.getUnit());

            System.out.printf("           | Stock actuel : %.2f %s%n",
                    stockNow.getQuantity(), stockNow.getUnit());

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
        // PARTIE ANNEXE 2 : Test création et récupération commande
        // ────────────────────────────────────────────────────────────────
        System.out.println("\n=== ANNEXE 2 - Test création et récupération commande ===\n");

        Order commandeTest = new Order();
        commandeTest.setCreationDatetime(Instant.now());

        // Exemple : 2 plats id=1 + 1 plat id=2
        // ⚠️ CHANGE LES ID SELON TES PLATS RÉELS DANS LA TABLE Dish
        DishOrder ligne1 = new DishOrder();
        ligne1.setIdDish(1);   // ← adapte avec un id réel (ex. Salade)
        ligne1.setQuantity(2);

        DishOrder ligne2 = new DishOrder();
        ligne2.setIdDish(2);   // ← adapte avec un id réel (ex. Burger)
        ligne2.setQuantity(1);

        commandeTest.addDishOrder(ligne1);
        commandeTest.addDishOrder(ligne2);

        try {
            Order commandeSauvee = dataRetriever.saveOrder(commandeTest);
            System.out.println("Commande créée avec succès !");
            System.out.println("Référence : " + commandeSauvee.getReference());
            System.out.println("Total HT : " + commandeSauvee.getTotalHT() + " €");
            System.out.println("Total TTC : " + commandeSauvee.getTotalTTC() + " €");
            System.out.println("Nombre de plats : " + commandeSauvee.getDishOrderList().size());

            // Test récupération
            Order retrouvee = dataRetriever.findOrderByReference(commandeSauvee.getReference());
            System.out.println("\nCommande retrouvée :");
            System.out.println("Référence : " + retrouvee.getReference());
            System.out.println("Total HT : " + retrouvee.getTotalHT());
            System.out.println("Total TTC : " + retrouvee.getTotalTTC());
            System.out.println("Lignes : " + retrouvee.getDishOrderList().size());

        } catch (IllegalStateException e) {
            System.out.println("❌ Stock insuffisant : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
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