package com.groupon.orders.api;

import java.util.List;

public class OrderRequest {
    private String orderId;
    private List<String> items;
    private Double amount;
    private String currency;
    private String userId;

    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
