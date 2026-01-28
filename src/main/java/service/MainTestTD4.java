package service;

import model.*;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class MainTestTD4 {

    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();


        List<Ingredient> ingredients = dataRetriever.getAllIngredients();

        if (ingredients.isEmpty()) {
            System.out.println("Aucun ingrédient trouvé. Vérifie ta base de données et la méthode getAllIngredients().");
            return;
        }


        Instant t = LocalDateTime.of(2024, 1, 6, 12, 0, 0)
                .toInstant(ZoneOffset.UTC);

        System.out.println("=== TD4 - Niveau de stock au " + t + " ===\n");

        for (Ingredient ing : ingredients) {
            StockValue stock = ing.getStockValueAt(t);

            System.out.printf("%-12s | Stock à %s : %.2f %s%n",
                    ing.getName(),
                    t,
                    stock.getQuantity(),
                    stock.getUnit());


            StockValue now = ing.getStockValueAt(Instant.now());
            System.out.printf("           | Stock actuel : %.2f %s%n", now.getQuantity(), now.getUnit());
            System.out.printf("           | Mouvements   : %d%n%n", ing.getStockMovementList().size());
        }


        System.out.println("=== Test ajout mouvement + saveIngredient ===\n");

        Ingredient exemple = findIngredientByName(ingredients, "Laitue");
        if (exemple != null) {
            StockMovement nouveau = new StockMovement();
            nouveau.setValue(new StockValue(1.5, UnitEnum.KG));
            nouveau.setType(MovementTypeEnum.IN);
            nouveau.setCreationDatetime(Instant.now());

            exemple.addStockMovement(nouveau);

            Ingredient saved = dataRetriever.saveIngredient(exemple);
            if (saved != null) {
                System.out.println("Sauvegarde OK pour " + saved.getName());
                System.out.println("Nouveau stock maintenant : " + saved.getStockValueAt(Instant.now()));
            } else {
                System.out.println("Échec saveIngredient");
            }
        } else {
            System.out.println("Ingrédient 'Laitue' non trouvé.");
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