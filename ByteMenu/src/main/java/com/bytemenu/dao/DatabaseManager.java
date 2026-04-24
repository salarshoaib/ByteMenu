package com.bytemenu.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:bytemenu.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            initSchema();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /** Returns the shared connection (for writes/inserts). */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Opens a brand-new connection for every read query.
     * This guarantees SQLite returns fresh data, not cached results.
     * Caller is responsible for closing it.
     */
    public Connection getNewConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initSchema() throws SQLException {
        Statement s = connection.createStatement();

        s.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name      TEXT    NOT NULL,
                email          TEXT    NOT NULL UNIQUE,
                password       TEXT    NOT NULL,
                role           TEXT    NOT NULL DEFAULT 'student',
                wallet_balance REAL    NOT NULL DEFAULT 1000.0,
                created_at     TEXT    DEFAULT (datetime('now'))
            )
            """);

        s.execute("""
            CREATE TABLE IF NOT EXISTS menu_items (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                name        TEXT    NOT NULL,
                category    TEXT    NOT NULL,
                price       REAL    NOT NULL,
                description TEXT,
                available   INTEGER NOT NULL DEFAULT 1,
                UNIQUE(name, category)
            )
            """);

        s.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id     INTEGER NOT NULL,
                total       REAL    NOT NULL DEFAULT 0,
                status      TEXT    NOT NULL DEFAULT 'Preparing',
                notes       TEXT,
                created_at  TEXT    DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """);

        s.execute("""
            CREATE TABLE IF NOT EXISTS order_items (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id        INTEGER NOT NULL,
                menu_item_id    INTEGER NOT NULL,
                quantity        INTEGER NOT NULL DEFAULT 1,
                customization   TEXT,
                unit_price      REAL    NOT NULL,
                FOREIGN KEY (order_id)      REFERENCES orders(id),
                FOREIGN KEY (menu_item_id)  REFERENCES menu_items(id)
            )
            """);

        s.execute("""
            CREATE TABLE IF NOT EXISTS seat_reservations (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id     INTEGER NOT NULL,
                seat_number TEXT    NOT NULL,
                date        TEXT    NOT NULL,
                time_slot   TEXT    NOT NULL,
                status      TEXT    NOT NULL DEFAULT 'Confirmed',
                created_at  TEXT    DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """);

        // Only seed if empty
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM menu_items");
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close();

        if (count == 0) {
            String[] seeds = {
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Chicken Burger','Main Course',350,'Crispy fried chicken with lettuce and mayo')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Beef Burger','Main Course',420,'Juicy beef patty with cheese and pickles')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Veggie Wrap','Main Course',280,'Grilled vegetables in a tortilla wrap')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Chicken Biryani','Main Course',300,'Spiced basmati rice with tender chicken')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Pasta Alfredo','Main Course',320,'Creamy white sauce pasta with garlic bread')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('French Fries','Snacks',150,'Crispy golden fries with ketchup')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Chicken Nuggets','Snacks',200,'6-piece crispy chicken nuggets')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Spring Rolls','Snacks',180,'4-piece vegetable spring rolls')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Pepsi','Drinks',80,'330ml chilled can')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('7UP','Drinks',80,'330ml chilled can')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Mineral Water','Drinks',50,'500ml bottle')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Mango Juice','Drinks',120,'Fresh mango juice 250ml')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Chocolate Cake','Desserts',200,'Slice of rich chocolate fudge cake')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Gulab Jamun','Desserts',120,'3-piece warm gulab jamun')",
                    "INSERT INTO menu_items (name,category,price,description) VALUES ('Ice Cream','Desserts',150,'Two scoops of vanilla ice cream')"
            };
            for (String sql : seeds) s.execute(sql);
            System.out.println("Menu items seeded.");
        }

        s.close();

        // Migration: add wallet_balance column to existing databases that predate this feature
        try {
            connection.createStatement().execute(
                "ALTER TABLE users ADD COLUMN wallet_balance REAL NOT NULL DEFAULT 1000.0"
            );
        } catch (Exception ignored) {
            // Column already exists — safe to ignore
        }

        System.out.println("Database initialised successfully.");
    }
}