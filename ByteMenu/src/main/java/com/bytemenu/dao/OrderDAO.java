package com.bytemenu.dao;

import com.bytemenu.model.Order;
import com.bytemenu.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> getOrdersByUser(int userId) throws Exception {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY id DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getNewConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapOrder(rs));
        }
        return list;
    }

    public Order placeOrder(Order order) throws Exception {
        Connection conn = DatabaseManager.getInstance().getConnection();
        String sql = "INSERT INTO orders (user_id, total, status, notes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUserId());
            ps.setDouble(2, order.getTotal());
            ps.setString(3, "Preparing");
            ps.setString(4, order.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) order.setId(keys.getInt(1));
        }
        String itemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, customization, unit_price) VALUES (?,?,?,?,?)";
        for (OrderItem item : order.getItems()) {
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                ps.setInt(1, order.getId());
                ps.setInt(2, item.getMenuItemId());
                ps.setInt(3, item.getQuantity());
                ps.setString(4, item.getCustomization());
                ps.setDouble(5, item.getUnitPrice());
                ps.executeUpdate();
            }
        }
        return order;
    }

    public void updateStatus(int orderId, String status) throws Exception {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    public List<Order> getRecentFeedOrders(int limit) throws Exception {
        String sql = "SELECT * FROM orders ORDER BY id DESC LIMIT ?";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getNewConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapOrder(rs));
        }
        return list;
    }

    public List<Order> getAllOrdersAdmin() throws Exception {
        String sql = "SELECT * FROM orders ORDER BY id DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getNewConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapOrder(rs));
        }
        return list;
    }

    /** Get all orders with their items fully loaded — used by admin portal. */
    public List<Order> getAllOrdersWithItems() throws Exception {
        String orderSql = "SELECT * FROM orders ORDER BY id DESC";
        String itemSql  = "SELECT oi.*, mi.name FROM order_items oi " +
                          "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                          "WHERE oi.order_id = ?";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getNewConnection();
             PreparedStatement ps = conn.prepareStatement(orderSql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = mapOrder(rs);
                // Load items for this order
                try (PreparedStatement ips = conn.prepareStatement(itemSql)) {
                    ips.setInt(1, order.getId());
                    ResultSet irs = ips.executeQuery();
                    while (irs.next()) {
                        OrderItem oi = new OrderItem(
                            irs.getInt("menu_item_id"),
                            irs.getString("name"),
                            irs.getInt("quantity"),
                            irs.getString("customization"),
                            irs.getDouble("unit_price")
                        );
                        order.getItems().add(oi);
                    }
                }
                list.add(order);
            }
        }
        return list;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        return new Order(rs.getInt("id"), rs.getInt("user_id"), rs.getDouble("total"),
                rs.getString("status"), rs.getString("notes"), rs.getString("created_at"));
    }
}
