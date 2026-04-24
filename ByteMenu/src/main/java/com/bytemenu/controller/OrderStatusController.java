package com.bytemenu.controller;

import com.bytemenu.dao.OrderDAO;
import com.bytemenu.model.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

/**
 * Sprint 3 — Public order status lookup screen.
 * No authentication required.
 * Reachable from the Login screen quick-lookup bar or by direct navigation.
 */
public class OrderStatusController {

    @FXML private TextField txtOrderId;
    @FXML private Label lblResult;
    @FXML private Label lblOrderInfo;

    private final OrderDAO orderDAO = new OrderDAO();

    /** Called by LoginController after navigating — pre-fills and auto-runs the lookup. */
    public void prefillAndLookup(String orderId) {
        if (txtOrderId != null) txtOrderId.setText(orderId);
        handleLookup();
    }

    @FXML
    void handleLookup() {
        if (lblResult != null) lblResult.setText("");
        if (lblOrderInfo != null) lblOrderInfo.setText("");

        String raw = (txtOrderId != null) ? txtOrderId.getText().trim() : "";
        if (raw.isEmpty()) {
            setResult("Please enter an order number.", "error"); return;
        }
        int id;
        try { id = Integer.parseInt(raw); }
        catch (NumberFormatException e) {
            setResult("Order number must be a number.", "error"); return;
        }

        try {
            List<Order> all = orderDAO.getAllOrdersAdmin();
            Order found = all.stream().filter(o -> o.getId() == id).findFirst().orElse(null);

            if (found == null) {
                setResult("Order #" + id + " not found.", "error"); return;
            }

            String display = switch (found.getStatus()) {
                case "Preparing"     -> "⏳  Preparing — your order is being made";
                case "Ready to Pick" -> "✅  Ready to Pick — head to the counter!";
                case "Completed"     -> "✔  Completed — enjoy your meal!";
                case "Cancelled"     -> "✖  Cancelled — please contact the counter";
                default              -> found.getStatus();
            };
            String mood = switch (found.getStatus()) {
                case "Ready to Pick", "Completed" -> "success";
                case "Cancelled"                  -> "error";
                default                           -> "info";
            };
            setResult(display, mood);

            if (lblOrderInfo != null) {
                lblOrderInfo.setText(
                    "Order #" + found.getId()
                    + "   ·   Rs. " + (int) found.getTotal()
                    + (found.getCreatedAt() != null ? "   ·   " + found.getCreatedAt() : "")
                );
                lblOrderInfo.setStyle("-fx-font-size:12px;-fx-text-fill:#888;-fx-font-family:Arial;");
            }

        } catch (Exception e) {
            setResult("Error: " + e.getMessage(), "error");
        }
    }

    private void setResult(String text, String mood) {
        if (lblResult == null) return;
        lblResult.setText(text);
        lblResult.setStyle(
            "-fx-font-size:14px;-fx-font-weight:bold;-fx-font-family:Arial;" +
            switch (mood) {
                case "success" -> "-fx-text-fill:#0A5233;";
                case "error"   -> "-fx-text-fill:#C8372D;";
                default        -> "-fx-text-fill:#1F4E79;";
            }
        );
    }

    @FXML
    void goBack() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/com/bytemenu/fxml/Login.fxml"));
            Stage stage = (Stage) txtOrderId.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 620));
            stage.setTitle("ByteMenu — Login");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
