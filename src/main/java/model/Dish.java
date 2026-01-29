package model;

import java.util.ArrayList;
import java.util.List;

public class Dish {

    private Integer id;
    private String name;
    private Double price;
    private DishTypeEnum dishType;

    private List<DishIngredient> dishIngredientList = new ArrayList<>();

    public Dish() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    // Compatibility: some repositories use "sellingPrice" naming
    public Double getSellingPrice() {
        return this.price;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.price = sellingPrice;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    // Getter "sécure" qui renvoie une copie de la liste
    public List<DishIngredient> getDishIngredients() {
        return new ArrayList<>(dishIngredientList);
    }

    // Setter qui copie la liste passée en paramètre
    public void setDishIngredients(List<DishIngredient> list) {
        this.dishIngredientList = (list != null) ? new ArrayList<>(list) : new ArrayList<>();
    }

    // Anciennes méthodes avec le nom *_List, tu peux les garder ou les supprimer
    public List<DishIngredient> getDishIngredientList() {
        return new ArrayList<>(dishIngredientList);
    }

    public void setDishIngredientList(List<DishIngredient> dishIngredientList) {
        this.dishIngredientList = (dishIngredientList != null)
                ? new ArrayList<>(dishIngredientList)
                : new ArrayList<>();
    }

    public void addDishIngredient(DishIngredient di) {
        if (di != null) {
            dishIngredientList.add(di);
        }
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", dishType=" + dishType +
                ", nb ingrédients=" + dishIngredientList.size() +
                '}';
    }
}
