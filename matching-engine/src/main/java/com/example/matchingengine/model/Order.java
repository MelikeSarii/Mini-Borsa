package com.example.matchingengine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "matching_orders")
public class Order {

    @Id
    private String orderId;

    private String symbol;
    private int qty;
    private String side;
    private String customerName;
    private Double price;
    private String status;

    public Order() {
    }

    public Order(
            String orderId,
            String symbol,
            int qty,
            String side,
            String customerName,
            Double price) {

        this.orderId = orderId;
        this.symbol = symbol;
        this.qty = qty;
        this.side = side;
        this.customerName = customerName;
        this.price = price;
        this.status = "NEW";
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}