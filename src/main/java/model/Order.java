package model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Integer id;
    private String reference;
    private double totalHT;
    private double totalTTC;
    private Instant creationDatetime;

    private List<DishOrder> dishOrderList = new ArrayList<>();

    public Order() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public double getTotalHT() { return totalHT; }
    public void setTotalHT(double totalHT) { this.totalHT = totalHT; }

    public double getTotalTTC() { return totalTTC; }
    public void setTotalTTC(double totalTTC) { this.totalTTC = totalTTC; }

    public Instant getCreationDatetime() { return creationDatetime; }
    public void setCreationDatetime(Instant creationDatetime) { this.creationDatetime = creationDatetime; }

    public List<DishOrder> getDishOrderList() {
        return new ArrayList<>(dishOrderList);
    }

    public void setDishOrderList(List<DishOrder> dishOrderList) {
        this.dishOrderList = (dishOrderList != null) ? new ArrayList<>(dishOrderList) : new ArrayList<>();
    }

    public void addDishOrder(DishOrder dishOrder) {
        if (dishOrder != null) {
            dishOrderList.add(dishOrder);
        }
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", reference='" + reference + "', totalHT=" + totalHT +
                ", totalTTC=" + totalTTC + ", plats=" + dishOrderList.size() + '}';
    }
}