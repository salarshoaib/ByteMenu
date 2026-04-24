package com.bytemenu.dao;

import com.bytemenu.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    private final Connection conn = DatabaseManager.getInstance().getConnection();

    public List<MenuItem> getAllAvailable() throws Exception {
        return query("SELECT * FROM menu_items WHERE available = 1 ORDER BY category, name");
    }

    public List<MenuItem> getByCategory(String category) throws Exception {
        String sql = "SELECT * FROM menu_items WHERE available = 1 AND category = ? ORDER BY name";
        List<MenuItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<MenuItem> search(String keyword) throws Exception {
        String sql = "SELECT * FROM menu_items WHERE available = 1 AND name LIKE ? ORDER BY category, name";
        List<MenuItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private List<MenuItem> query(String sql) throws Exception {
        List<MenuItem> list = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private MenuItem map(ResultSet rs) throws SQLException {
        return new MenuItem(
                rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                rs.getDouble("price"), rs.getString("description"), rs.getInt("available") == 1);
    }
}
