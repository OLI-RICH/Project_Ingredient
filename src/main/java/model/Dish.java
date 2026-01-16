package model;

import java.util.ArrayList;
import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private List<DishIngredient> dishIngredients;
    private Double sellingPrice;

    public Dish() {
        this.dishIngredients = new ArrayList<>();
    }

    public Dish(String name, DishTypeEnum dishType) {
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = new ArrayList<>();
    }

    public Dish(Integer id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = new ArrayList<>();
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

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Double getDishCost() {
        if (dishIngredients == null || dishIngredients.isEmpty()) {
            return 0.0;
        }

        double totalCost = 0.0;
        for (DishIngredient di : dishIngredients) {
            if (di.getRequiredQuantity() == null) {
                throw new RuntimeException(
                        "Quantité requise manquante pour l’ingrédient '" +
                                (di.getIngredient() != null ? di.getIngredient().getName() : "inconnu") + "'"
                );
            }
            if (di.getIngredient() == null || di.getIngredient().getPrice() == null) {
                throw new RuntimeException("Prix de l’ingrédient manquant pour le calcul du coût");
            }
            totalCost += di.getIngredient().getPrice() * di.getRequiredQuantity();
        }
        return totalCost;
    }

    public Double getGrossMargin() {
        if (sellingPrice == null) {
            throw new RuntimeException("Le prix de vente du plat est nul, impossible de calculer la marge brute");
        }
        double cost = getDishCost();
        return sellingPrice - cost;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", sellingPrice=" + sellingPrice +
                ", dishIngredients=" + dishIngredients +
                '}';
    }
}
