import model.CategoryEnum;
import model.Dish;
import model.DishTypeEnum;
import model.Ingredient;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("=== DÉBUT DES TESTS ===\n");

        testFindDishById(dataRetriever, 1);

        testFindDishByIdNotFound(dataRetriever, 999);

        testFindIngredients(dataRetriever, 2, 2);

        testFindIngredients(dataRetriever, 3, 5);

        testFindDishsByIngredientName(dataRetriever, "eur");

        testFindIngredientsByCriteria1(dataRetriever);

        testFindIngredientsByCriteria2(dataRetriever);

        testFindIngredientsByCriteria3(dataRetriever);

        testCreateIngredients(dataRetriever);

        testCreateIngredientsWithExisting(dataRetriever);

        testSaveNewDish(dataRetriever);

        testUpdateDishAddIngredients(dataRetriever);

        testUpdateDishRemoveIngredients(dataRetriever);

        System.out.println("\n=== FIN DES TESTS ===");
    }

    private static void testFindDishById(DataRetriever dr, int id) {
        System.out.println("TEST a) findDishById(id=" + id + ")");
        try {
            Dish dish = dr.findDishById(id);
            System.out.println("✓ Résultat: " + dish.getName() + " avec " + dish.getIngredients().size() + " ingrédients");
            for (Ingredient ing : dish.getIngredients()) {
                System.out.println("  - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testFindDishByIdNotFound(DataRetriever dr, int id) {
        System.out.println("TEST b) findDishById(id=" + id + ") - Devrait lever une exception");
        try {
            Dish dish = dr.findDishById(id);
            System.out.println("✗ Erreur: Aucune exception levée");
        } catch (RuntimeException e) {
            System.out.println("✓ Exception levée: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testFindIngredients(DataRetriever dr, int page, int size) {
        System.out.println("TEST findIngredients(page=" + page + ", size=" + size + ")");
        try {
            List<Ingredient> ingredients = dr.findIngredients(page, size);
            System.out.println("✓ Résultat: " + ingredients.size() + " ingrédient(s)");
            for (Ingredient ing : ingredients) {
                System.out.println("  - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testFindDishsByIngredientName(DataRetriever dr, String name) {
        System.out.println("TEST e) findDishsByIngredientName(\"" + name + "\")");
        try {
            List<Dish> dishes = dr.findDishsByIngredientName(name);
            System.out.println("✓ Résultat: " + dishes.size() + " plat(s)");
            for (Dish dish : dishes) {
                System.out.println("  - " + dish.getName());
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testFindIngredientsByCriteria1(DataRetriever dr) {
        System.out.println("TEST f) findIngredientsByCriteria(category=VEGETABLE)");
        try {
            List<Ingredient> ingredients = dr.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10);
            System.out.println("✓ Résultat: " + ingredients.size() + " ingrédient(s)");
            for (Ingredient ing : ingredients) {
                System.out.println("  - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testFindIngredientsByCriteria2(DataRetriever dr) {
        System.out.println("TEST g) findIngredientsByCriteria(ingredientName=\"cho\", dishName=\"Sal\")");
        try {
            List<Ingredient> ingredients = dr.findIngredientsByCriteria("cho", null, "Sal", 1, 10);
            System.out.println("✓ Résultat: " + ingredients.size() + " ingrédient(s) (attendu: 0)");
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testFindIngredientsByCriteria3(DataRetriever dr) {
        System.out.println("TEST h) findIngredientsByCriteria(ingredientName=\"cho\", dishName=\"gâteau\")");
        try {
            List<Ingredient> ingredients = dr.findIngredientsByCriteria("cho", null, "gâteau", 1, 10);
            System.out.println("✓ Résultat: " + ingredients.size() + " ingrédient(s)");
            for (Ingredient ing : ingredients) {
                System.out.println("  - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testCreateIngredients(DataRetriever dr) {
        System.out.println("TEST i) createIngredients - Fromage et Oignon");
        try {
            List<Ingredient> newIngredients = Arrays.asList(
                    new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY),
                    new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE)
            );
            List<Ingredient> created = dr.createIngredients(newIngredients);
            System.out.println("✓ Résultat: " + created.size() + " ingrédient(s) créé(s)");
            for (Ingredient ing : created) {
                System.out.println("  - " + ing.getName() + " (id=" + ing.getId() + ")");
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testCreateIngredientsWithExisting(DataRetriever dr) {
        System.out.println("TEST j) createIngredients - avec Laitue (existant)");
        try {
            List<Ingredient> newIngredients = Arrays.asList(
                    new Ingredient("Carotte", 2000.0, CategoryEnum.VEGETABLE),
                    new Ingredient("Laitue", 2000.0, CategoryEnum.VEGETABLE)
            );
            List<Ingredient> created = dr.createIngredients(newIngredients);
            System.out.println("✗ Erreur: Aucune exception levée");
        } catch (RuntimeException e) {
            System.out.println("✓ Exception levée: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testSaveNewDish(DataRetriever dr) {
        System.out.println("TEST k) saveDish - Nouveau plat (Soupe de légumes)");
        try {
            Dish newDish = new Dish("Soupe de légumes", DishTypeEnum.START);
            Ingredient oignon = new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE);
            newDish.setIngredients(Arrays.asList(oignon));

            Dish saved = dr.saveDish(newDish);
            System.out.println("✓ Résultat: " + saved.getName() + " créé (id=" + saved.getId() + ")");
            System.out.println("  Ingrédients: " + saved.getIngredients().size());
            for (Ingredient ing : saved.getIngredients()) {
                System.out.println("    - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testUpdateDishAddIngredients(DataRetriever dr) {
        System.out.println("TEST l) saveDish - Mise à jour Salade fraîche (ajout ingrédients)");
        try {
            Dish dish = new Dish(1, "Salade fraîche", DishTypeEnum.START);
            dish.setIngredients(Arrays.asList(
                    new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE),
                    new Ingredient("Laitue", 800.0, CategoryEnum.VEGETABLE),
                    new Ingredient("Tomate", 600.0, CategoryEnum.VEGETABLE),
                    new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY)
            ));

            Dish updated = dr.saveDish(dish);
            System.out.println("✓ Résultat: " + updated.getName() + " mis à jour");
            System.out.println("  Ingrédients: " + updated.getIngredients().size());
            for (Ingredient ing : updated.getIngredients()) {
                System.out.println("    - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testUpdateDishRemoveIngredients(DataRetriever dr) {
        System.out.println("TEST m) saveDish - Mise à jour Salade de fromage (suppression ingrédients)");
        try {
            Dish dish = new Dish(1, "Salade de fromage", DishTypeEnum.START);
            dish.setIngredients(Arrays.asList(
                    new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY)
            ));

            Dish updated = dr.saveDish(dish);
            System.out.println("✓ Résultat: " + updated.getName() + " mis à jour");
            System.out.println("  Ingrédients: " + updated.getIngredients().size());
            for (Ingredient ing : updated.getIngredients()) {
                System.out.println("    - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }
}