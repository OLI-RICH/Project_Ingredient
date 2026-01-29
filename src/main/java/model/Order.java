package model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private Integer id;
    private String reference;           // ORDXXXXX
    private Double totalHT;
    private Double totalTTC;
    private Instant creationDatetime;
    private PaymentStatus paymentStatus; // ← obligatoire pour K2

    private List<DishOrder> dishOrderList = new ArrayList<>();

    public Order() {
        this.paymentStatus = PaymentStatus.PENDING; // valeur par défaut
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Double getTotalHT() { return totalHT; }
    public void setTotalHT(Double totalHT) { this.totalHT = totalHT; }

    public Double getTotalTTC() { return totalTTC; }
    public void setTotalTTC(Double totalTTC) { this.totalTTC = totalTTC; }

    public Instant getCreationDatetime() { return creationDatetime; }
    public void setCreationDatetime(Instant creationDatetime) { this.creationDatetime = creationDatetime; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public List<DishOrder> getDishOrderList() { return new ArrayList<>(dishOrderList); }
    public void setDishOrderList(List<DishOrder> dishOrderList) {
        this.dishOrderList = (dishOrderList != null) ? new ArrayList<>(dishOrderList) : new ArrayList<>();
    }

    public void addDishOrder(DishOrder dishOrder) {
        if (dishOrder != null) dishOrderList.add(dishOrder);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", totalHT=" + totalHT +
                ", totalTTC=" + totalTTC +
                ", paymentStatus=" + paymentStatus +
                ", creationDatetime=" + creationDatetime +
                ", plats=" + dishOrderList.size() +
                '}';
    }
}