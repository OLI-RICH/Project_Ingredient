package model;

public class DishIngredient {
    private Integer id;
    private Dish dish;
    private Ingredient ingredient;
    private Double requiredQuantity;
    private String unit;

    public DishIngredient() {}

    public DishIngredient(Dish dish,
                          Ingredient ingredient,
                          Double requiredQuantity,
                          String unit) {
        this.dish = dish;
        this.ingredient = ingredient;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(Double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", dish=" + (dish != null ? dish.getId() : null) +
                ", ingredient=" + (ingredient != null ? ingredient.getId() : null) +
                ", requiredQuantity=" + requiredQuantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}
