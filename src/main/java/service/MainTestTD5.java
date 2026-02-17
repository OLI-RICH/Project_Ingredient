package service;

import model.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class MainTestTD5 {

    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        List<Ingredient> ingredients = dataRetriever.getAllIngredients();

        if (ingredients.isEmpty()) {
            System.out.println("Aucun ingrédient trouvé. Exécute le script SQL fourni avant de lancer ce test.");
            return;
        }

        Instant t = LocalDateTime.of(2024, 1, 6, 12, 0, 0)
                .toInstant(ZoneOffset.UTC);

        System.out.println("=== TD5 - Vérification OO vs SQL (push-down) au " + t + " ===\n");

        for (Ingredient ing : ingredients) {
            StockValue stockOO = ing.getStockValueAt(t);
            StockValue stockSQL = dataRetriever.getStockValueAt(t, ing.getId());

            System.out.printf("%-12s | Stock OO à %s : %.4f %s%n",
                    ing.getName(), t, stockOO.getQuantity(), stockOO.getUnit());

            System.out.printf("           | Stock SQL à %s : %.4f %s%n",
                    t, stockSQL.getQuantity(), stockSQL.getUnit());

            double diff = stockOO.getQuantity() - stockSQL.getQuantity();
            System.out.printf("           | Diff OO - SQL : %.4f %s%n%n",
                    diff, stockOO.getUnit());
        }

        // ────────────────────────────────────────────────────────────────
        // Test getDishCost et getGrossMargin (exercices 1 & 2)
        // ────────────────────────────────────────────────────────────────
        System.out.println("\n=== Test partie 2 : Coût de plats et marge brute ===\n");

        int dishId = 1;
        Double cost = dataRetriever.getDishCost(dishId);
        Double margin = dataRetriever.getGrossMargin(dishId);

        System.out.println("Plat " + dishId + " → Coût ingrédients : " + cost);
        System.out.println("Plat " + dishId + " → Marge brute : " + margin);

        if (cost.isNaN() || cost == 0.0) {
            System.out.println("   (Note : valeur 0 → vérifier que le plat " + dishId + " existe et a des ingrédients)");
        }
    }
}
