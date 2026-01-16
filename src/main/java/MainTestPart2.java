import model.Dish;
import model.DishTypeEnum;
import model.Ingredient;
import model.CategoryEnum;

import java.util.Arrays;

public class MainTestPart2 {

    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("=== TESTS PARTIE 2 (7 janvier 2026) ===\n");

        testGetDishCostWithKnownQuantities(dataRetriever);

        testGetDishCostWithUnknownQuantities(dataRetriever);

        testCreateDishWithSellingPrice(dataRetriever);

        testUpdateDishSellingPrice(dataRetriever);

        System.out.println("\n=== FIN DES TESTS ===");
    }

    private static void testGetDishCostWithKnownQuantities(DataRetriever dr) {
        System.out.println("TEST 1: Calcul du coût d'un plat avec quantités connues");
        System.out.println("--------------------------------------------------------");
        try {
            Dish dish = dr.findDishById(1); // Salade Fraîche
            System.out.println("Plat récupéré: " + dish.getName());
            System.out.println("Ingrédients:");
            for (Ingredient ing : dish.getIngredients()) {
                System.out.println("  - " + ing.getName() +
                        " | Prix: " + ing.getPrice() +
                        " | Quantité: " + ing.getRequiredQuantity());
            }

            try {
                Double cost = dish.getDishCost();
                System.out.println("✓ Coût total calculé: " + cost + " Ar");

                System.out.println("  (Attendu: 2000.0 Ar)");
            } catch (RuntimeException e) {
                System.out.println("✗ Exception inattendue: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testGetDishCostWithUnknownQuantities(DataRetriever dr) {
        System.out.println("TEST 2: Calcul du coût avec quantités inconnues (devrait échouer)");
        System.out.println("--------------------------------------------------------------------");
        try {
            Dish dish = dr.findDishById(3); // Gâteau au chocolat
            System.out.println("Plat récupéré: " + dish.getName());
            System.out.println("Ingrédients:");
            for (Ingredient ing : dish.getIngredients()) {
                System.out.println("  - " + ing.getName() +
                        " | Prix: " + ing.getPrice() +
                        " | Quantité: " + ing.getRequiredQuantity());
            }

            try {
                Double cost = dish.getDishCost();
                System.out.println("✗ Erreur: Aucune exception levée (coût calculé: " + cost + ")");
            } catch (RuntimeException e) {
                System.out.println("✓ Exception correctement levée: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testCreateDishWithSellingPrice(DataRetriever dr) {
        System.out.println("TEST 3: Création d'un nouveau plat avec prix de vente");
        System.out.println("-------------------------------------------------------");
        try {
            Dish newDish = new Dish("Salade César", DishTypeEnum.START);
            newDish.setSellingPrice(5000.0);

            Ingredient laitue = new Ingredient("Laitue", 800.0, CategoryEnum.VEGETABLE);
            newDish.setIngredients(Arrays.asList(laitue));

            Dish savedDish = dr.saveDish(newDish);

            System.out.println("✓ Plat créé avec succès:");
            System.out.println("  ID: " + savedDish.getId());
            System.out.println("  Nom: " + savedDish.getName());
            System.out.println("  Type: " + savedDish.getDishType());
            System.out.println("  Prix de vente: " + savedDish.getSellingPrice() + " Ar");
            System.out.println("  Nombre d'ingrédients: " + savedDish.getIngredients().size());

            try {
                Double cost = savedDish.getDishCost();
                System.out.println("  Coût des ingrédients: " + cost + " Ar");
                System.out.println("  Marge: " + (savedDish.getSellingPrice() - cost) + " Ar");
            } catch (RuntimeException e) {
                System.out.println("  Coût: Impossible à calculer (" + e.getMessage() + ")");
            }

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testUpdateDishSellingPrice(DataRetriever dr) {
        System.out.println("TEST 4: Mise à jour du prix de vente d'un plat existant");
        System.out.println("--------------------------------------------------------");
        try {
            Dish dish = dr.findDishById(1);
            System.out.println("Plat avant mise à jour:");
            System.out.println("  Nom: " + dish.getName());
            System.out.println("  Prix de vente actuel: " + dish.getSellingPrice());

            dish.setSellingPrice(4500.0);

            Dish updatedDish = dr.saveDish(dish);

            System.out.println("\n Plat mis à jour avec succès:");
            System.out.println("  Nom: " + updatedDish.getName());
            System.out.println("  Nouveau prix de vente: " + updatedDish.getSellingPrice() + " Ar");

            try {
                Double cost = updatedDish.getDishCost();
                System.out.println("  Coût des ingrédients: " + cost + " Ar");
                System.out.println("  Marge: " + (updatedDish.getSellingPrice() - cost) + " Ar");
                double marginPercent = ((updatedDish.getSellingPrice() - cost) / cost) * 100;
                System.out.println("  Marge %: " + String.format("%.2f", marginPercent) + "%");
            } catch (RuntimeException e) {
                System.out.println("  Coût: Impossible à calculer (" + e.getMessage() + ")");
            }

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }
}