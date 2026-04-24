package com.bytemenu.controller;

import com.bytemenu.dao.OrderDAO;
import com.bytemenu.model.Order;
import com.bytemenu.util.ChefVetoManager;
import com.bytemenu.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;

public class MyOrdersController {

    @FXML private VBox ordersList;

    @FXML public void initialize() { loadOrders(); }

    /**
     * Auto-advance skips any order that the chef has personally set via the portal.
     * Chef-locked orders are read-only from the student side.
     */
    @FXML
    void refresh() {
        try {
            OrderDAO dao = new OrderDAO();
            List<Order> orders = dao.getOrdersByUser(SessionManager.getCurrentUser().getId());
            for (Order o : orders) {
                // Chef veto: if chef ever touched this order, leave it alone
                if (ChefVetoManager.isLocked(o.getId())) continue;

                String next = switch (o.getStatus()) {
                    case "Preparing"     -> "Ready to Pick";
                    case "Ready to Pick" -> "Completed";
                    default              -> null;
                };
                if (next != null) dao.updateStatus(o.getId(), next);
            }
        } catch (Exception e) {
            System.err.println("Failed to advance statuses: " + e.getMessage());
        }
        loadOrders();
    }

    private void loadOrders() {
        ordersList.getChildren().clear();
        try {
            List<Order> orders = new OrderDAO()
                    .getOrdersByUser(SessionManager.getCurrentUser().getId());

            if (orders.isEmpty()) {
                Label none = new Label("You haven't placed any orders yet.");
                none.setStyle("-fx-text-fill:#888;-fx-font-size:14px;-fx-padding:30 0 0 0;");
                ordersList.getChildren().add(none);
                return;
            }
            for (Order o : orders) ordersList.getChildren().add(buildOrderCard(o));

        } catch (Exception e) {
            Label err = new Label("Failed to load orders: " + e.getMessage());
            err.setStyle("-fx-text-fill:#C8372D;-fx-font-size:13px;");
            ordersList.getChildren().add(err);
        }
    }

    private HBox buildOrderCard(Order order) {
        HBox card = new HBox(16);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-padding:14 18 14 18;-fx-background-radius:6;");

        VBox left = new VBox(4);
        HBox.setHgrow(left, Priority.ALWAYS);
        Label id = new Label("Order #" + order.getId());
        id.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:#1F4E79;");
        Label time = new Label(order.getCreatedAt() != null ? order.getCreatedAt() : "");
        time.setStyle("-fx-font-size:11px;-fx-text-fill:#999;");
        left.getChildren().addAll(id, time);
        if (order.getNotes() != null && !order.getNotes().isBlank()) {
            Label notes = new Label("Note: " + order.getNotes());
            notes.setStyle("-fx-font-size:11px;-fx-text-fill:#666;-fx-font-style:italic;");
            left.getChildren().add(notes);
        }

        Label total = new Label("Rs. " + (int) order.getTotal());
        total.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:#333;-fx-padding:0 12 0 0;");

        Label status = new Label(order.getStatus());
        String sc = switch (order.getStatus()) {
            case "Ready to Pick" -> "status-ready";
            case "Completed"     -> "status-ready";
            case "Cancelled"     -> "status-cancelled";
            default              -> "status-preparing";
        };
        status.getStyleClass().add(sc);

        card.getChildren().addAll(left, total, status);
        return card;
    }
}
