package com.bytemenu.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private double total;
    private String status;
    private String notes;
    private String createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(int id, int userId, double total, String status, String notes, String createdAt) {
        this.id = id; this.userId = userId; this.total = total;
        this.status = status; this.notes = notes; this.createdAt = createdAt;
    }

    public int getId()           { return id; }
    public void setId(int id)    { this.id = id; }
    public int getUserId()       { return userId; }
    public void setUserId(int u) { this.userId = u; }
    public double getTotal()     { return total; }
    public void setTotal(double t){ this.total = t; }
    public String getStatus()    { return status; }
    public void setStatus(String s){ this.status = s; }
    public String getNotes()     { return notes; }
    public void setNotes(String n){ this.notes = n; }
    public String getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems(){ return items; }
    public void addItem(OrderItem item){ items.add(item); total += item.getUnitPrice() * item.getQuantity(); }
}
