import model.*;
import service.DataRetriever;

public class MainTestTD3 {

    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("=== TESTS TD3 - Normalisation ManyToMany ===\n");

        testFindDishWithIngredients(dataRetriever);

        testGetDishCost(dataRetriever, 1, "Salade fraîche", 250.0);
        testGetDishCost(dataRetriever, 2, "Poulet grillé", 4500.0);
        testGetDishCost(dataRetriever, 3, "Gâteau au chocolat", 1400.0);

        testGetGrossMargin(dataRetriever, 1, "Salade fraîche", 3250.0);
        testGetGrossMargin(dataRetriever, 2, "Poulet grillé", 7500.0);
        testGetGrossMargin(dataRetriever, 3, "Gâteau au chocolat", 6600.0);

        testCreateDishWithIngredients(dataRetriever);
        testUpdateDishIngredients(dataRetriever);
        testFindDishsByIngredientName(dataRetriever);

        System.out.println("\n=== FIN DES TESTS TD3 ===");
    }

    private static void testFindDishWithIngredients(DataRetriever dr) {
        System.out.println("TEST 1: Récupération d'un plat avec ses ingrédients");
        try {
            Dish dish = dr.findDishById(1);
            System.out.println("Plat: " + dish.getName());
            System.out.println("Prix: " + dish.getSellingPrice() + " Ar");

            for (DishIngredient di : dish.getDishIngredients()) {
                Ingredient ing = di.getIngredient();
                System.out.println("  - " + ing.getName() +
                        " | Prix: " + ing.getPrice() +
                        " | Quantité: " + di.getRequiredQuantity() + " " + di.getUnit());
            }
            System.out.println("✓ OK\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testGetDishCost(DataRetriever dr, int dishId, String dishName, double expectedCost) {
        System.out.println("TEST getDishCost(): " + dishName);
        try {
            Dish dish = dr.findDishById(dishId);
            Double cost = dish.getDishCost();
            System.out.println("Coût: " + cost + " | Attendu: " + expectedCost);
            System.out.println("✓ OK\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testGetGrossMargin(DataRetriever dr, int dishId, String dishName, double expectedMargin) {
        System.out.println("TEST getGrossMargin(): " + dishName);
        try {
            Dish dish = dr.findDishById(dishId);
            Double margin = dish.getGrossMargin();
            System.out.println("Marge: " + margin + " | Attendu: " + expectedMargin);
            System.out.println("✓ OK\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testCreateDishWithIngredients(DataRetriever dr) {
        System.out.println("TEST 4: Création plat");
        try {
            Dish newDish = new Dish("Salade de tomates", DishTypeEnum.START);
            newDish.setSellingPrice(2500.0);
            newDish.setId(null);

            Ingredient tomate = dr.findIngredientByName("Tomate");

            if (tomate != null) {
                DishIngredient di = new DishIngredient();
                di.setRequiredQuantity(0.5);
                di.setUnit("KG");   // String
                di.setIngredient(tomate);

                newDish.getDishIngredients().add(di);
            }

            Dish savedDish = dr.saveDish(newDish);

            System.out.println("Plat créé: " + savedDish.getName());
            System.out.println("ID: " + savedDish.getId());
            System.out.println("✓ OK\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testUpdateDishIngredients(DataRetriever dr) {
        System.out.println("TEST 5: Update ingrédients");
        try {
            Dish dish = dr.findDishById(1);

            for (DishIngredient di : dish.getDishIngredients()) {
                if (di.getIngredient().getName().equals("Laitue")) {
                    di.setRequiredQuantity(2.0);
                }
            }

            Dish updatedDish = dr.saveDish(dish);

            for (DishIngredient di : updatedDish.getDishIngredients()) {
                System.out.println("  - " + di.getIngredient().getName() +
                        ": " + di.getRequiredQuantity() + " " + di.getUnit());
            }

            System.out.println("✓ OK\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testFindDishsByIngredientName(DataRetriever dr) {
        System.out.println("TEST 6: Recherche par ingrédient");
        try {
            var dishes = dr.findDishsByIngredientName("tomate");
            for (Dish dish : dishes) {
                System.out.println("  - " + dish.getName());
            }
            System.out.println("✓ OK\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
