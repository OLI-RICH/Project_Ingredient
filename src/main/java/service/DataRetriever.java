package service;

import model.CategoryEnum;
import model.Dish;
import model.Ingredient;
import repository.DishRepository;
import repository.IngredientRepository;

import java.util.List;

public class DataRetriever {

    private DishRepository dishRepository;
    private IngredientRepository ingredientRepository;

    public DataRetriever() {
        this.dishRepository = new DishRepository();
        this.ingredientRepository = new IngredientRepository();
    }

    public Dish findDishById(Integer id) {
        return dishRepository.findDishById(id);
    }

    public Dish saveDish(Dish dishToSave) {
        return dishRepository.saveDish(dishToSave);
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        return dishRepository.findDishsByIngredientName(ingredientName);
    }

    public List<Ingredient> findIngredients(int page, int size) {
        return ingredientRepository.findIngredients(page, size);
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        return ingredientRepository.createIngredients(newIngredients);
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category,
                                                      String dishName, int page, int size) {
        return ingredientRepository.findIngredientsByCriteria(ingredientName, category, dishName, page, size);
    }
}