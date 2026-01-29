package model;

public class DishIngredient {

    private Integer id;

    // objets liés (utilisés par repository et service)
    private Dish dish;
    private Ingredient ingredient;

    // quantité requise pour la recette
    private Double requiredQuantity;
    private String unit;

    public DishIngredient() {
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

    // Compatibility helpers for older code that used idDish/idIngredient/quantity
    public void setIdDish(Integer idDish) {
        if (this.dish == null) this.dish = new Dish();
        this.dish.setId(idDish);
    }

    public Integer getIdDish() {
        return (this.dish != null) ? this.dish.getId() : null;
    }

    public void setIdIngredient(Integer idIngredient) {
        if (this.ingredient == null) this.ingredient = new Ingredient();
        this.ingredient.setId(idIngredient);
    }

    public Integer getIdIngredient() {
        return (this.ingredient != null) ? this.ingredient.getId() : null;
    }

    // backward-compatible aliases for 'requiredQuantity'
    public Double getQuantity() {
        return getRequiredQuantity();
    }

    public void setQuantity(Double q) {
        setRequiredQuantity(q);
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "ingredient=" + (ingredient != null ? ingredient.getId() : null) +
                ", requiredQuantity=" + requiredQuantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}