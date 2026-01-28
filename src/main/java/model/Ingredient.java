package model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList = new ArrayList<>();

    public Ingredient() {
    }

    public Ingredient(String name, Double price, CategoryEnum category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Ingredient(Integer id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
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

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public List<StockMovement> getStockMovementList() {
        return Collections.unmodifiableList(stockMovementList);
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = (stockMovementList != null) ? new ArrayList<>(stockMovementList) : new ArrayList<>();
    }

    public void addStockMovement(StockMovement movement) {
        if (movement != null) {
            movement.setIdIngredient(this.id);
            stockMovementList.add(movement);
        }
    }

    public StockValue getStockValueAt(Instant t) {
        double quantity = 0.0;
        UnitEnum unit = UnitEnum.KG;

        if (stockMovementList == null || stockMovementList.isEmpty()) {
            return new StockValue(quantity, unit);
        }

        for (StockMovement movement : stockMovementList) {
            Instant movementTime = movement.getCreationDatetime();
            if (movementTime != null && !movementTime.isAfter(t)) {
                double q = movement.getValue().getQuantity();
                if (movement.getType() == MovementTypeEnum.IN) {
                    quantity += q;
                } else if (movement.getType() == MovementTypeEnum.OUT) {
                    quantity -= q;
                }
            }
        }

        return new StockValue(quantity, unit);
    }

    @Override
    public String toString() {
        return "Ingredient{id=" + id + ", name='" + name + "', price=" + price + ", category=" + category + "}";
    }
}