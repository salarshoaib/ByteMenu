package com.bytemenu.model;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String role;
    private double walletBalance;

    public User(int id, String fullName, String email, String role, double walletBalance) {
        this.id = id; this.fullName = fullName; this.email = email;
        this.role = role; this.walletBalance = walletBalance;
    }

    public int getId()               { return id; }
    public String getFullName()      { return fullName; }
    public String getEmail()         { return email; }
    public String getRole()          { return role; }
    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double b) { this.walletBalance = b; }
}
