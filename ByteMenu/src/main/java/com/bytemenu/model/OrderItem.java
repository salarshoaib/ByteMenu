package com.bytemenu.model;

public class OrderItem {
    private int menuItemId;
    private String name;
    private int quantity;
    private String customization;
    private double unitPrice;

    public OrderItem(int menuItemId, String name, int quantity, String customization, double unitPrice) {
        this.menuItemId = menuItemId; this.name = name; this.quantity = quantity;
        this.customization = customization; this.unitPrice = unitPrice;
    }

    public int getMenuItemId()         { return menuItemId; }
    public String getName()            { return name; }
    public int getQuantity()           { return quantity; }
    public void setQuantity(int q)     { this.quantity = q; }
    public String getCustomization()   { return customization; }
    public void setCustomization(String c){ this.customization = c; }
    public double getUnitPrice()       { return unitPrice; }
    public double getSubtotal()        { return unitPrice * quantity; }
}
