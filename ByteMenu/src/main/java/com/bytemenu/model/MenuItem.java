package com.bytemenu.model;

public class MenuItem {
    private int id;
    private String name;
    private String category;
    private double price;
    private String description;
    private boolean available;

    public MenuItem(int id, String name, String category, double price, String description, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.available = available;
    }

    public int getId()           { return id; }
    public String getName()      { return name; }
    public String getCategory()  { return category; }
    public double getPrice()     { return price; }
    public String getDescription(){ return description; }
    public boolean isAvailable() { return available; }

    @Override
    public String toString() { return name; }
}
