package model;

import java.time.Instant;

public class Sale {

    private Integer id;
    private Integer orderId;
    private Instant saleDatetime;
    private Double totalAmount;

    public Sale() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Instant getSaleDatetime() { return saleDatetime; }
    public void setSaleDatetime(Instant saleDatetime) { this.saleDatetime = saleDatetime; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", saleDatetime=" + saleDatetime +
                ", totalAmount=" + totalAmount +
                '}';
    }
}