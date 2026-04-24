package com.bytemenu.dao;

import com.bytemenu.model.User;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.*;

public class UserDAO {

    private final Connection conn = DatabaseManager.getInstance().getConnection();

    public User register(String fullName, String email, String password) throws Exception {
        if (findByEmail(email) != null)
            throw new Exception("An account with this email already exists.");
        String hashed = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        String sql = "INSERT INTO users (full_name, email, password, wallet_balance) VALUES (?,?,?,1000.0)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName); ps.setString(2, email); ps.setString(3, hashed);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return new User(keys.getInt(1), fullName, email, "student", 1000.0);
        }
        throw new Exception("Registration failed.");
    }

    public User login(String email, String password) throws Exception {
        User user = findByEmail(email);
        if (user == null) throw new Exception("No account found with this email address.");
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT password FROM users WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BCrypt.Result r = BCrypt.verifyer().verify(password.toCharArray(), rs.getString("password"));
                if (!r.verified) throw new Exception("Incorrect email or password.");
                return user;
            }
        }
        throw new Exception("Login failed.");
    }

    /**
     * Deduct amount from wallet. Throws if insufficient funds.
     * Uses a fresh connection so it always sees the latest balance.
     */
    public void deductWallet(int userId, double amount) throws Exception {
        try (Connection c = DatabaseManager.getInstance().getNewConnection()) {
            PreparedStatement check = c.prepareStatement(
                "SELECT wallet_balance FROM users WHERE id = ?");
            check.setInt(1, userId);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) throw new Exception("User not found.");
            double balance = rs.getDouble(1);
            if (balance < amount)
                throw new Exception(
                    "Insufficient wallet balance. Balance: Rs. " + (int)balance +
                    ", Order total: Rs. " + (int)amount + ".");
            PreparedStatement upd = c.prepareStatement(
                "UPDATE users SET wallet_balance = wallet_balance - ? WHERE id = ?");
            upd.setDouble(1, amount); upd.setInt(2, userId);
            upd.executeUpdate();
        }
    }

    /** Fetch current wallet balance fresh from DB. */
    public double getWalletBalance(int userId) throws Exception {
        try (Connection c = DatabaseManager.getInstance().getNewConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT wallet_balance FROM users WHERE id = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    public User findByEmail(String email) throws Exception {
        // Read wallet_balance; if column doesn't exist yet (old DB), default to 1000
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, full_name, email, role, wallet_balance FROM users WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"), rs.getString("full_name"),
                    rs.getString("email"), rs.getString("role"),
                    rs.getDouble("wallet_balance")
                );
            }
        } catch (SQLException e) {
            // wallet_balance column not yet migrated — fall back to basic query
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, full_name, email, role FROM users WHERE email = ?")) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"), rs.getString("full_name"),
                        rs.getString("email"), rs.getString("role"),
                        1000.0   // default for existing accounts
                    );
                }
            }
        }
        return null;
    }
}
