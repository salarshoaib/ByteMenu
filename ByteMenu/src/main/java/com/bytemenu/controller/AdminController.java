package com.bytemenu.controller;

import com.bytemenu.dao.OrderDAO;
import com.bytemenu.dao.UserDAO;
import com.bytemenu.model.Order;
import com.bytemenu.model.OrderItem;
import com.bytemenu.util.ChefVetoManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminController {

    @FXML private VBox ordersList;
    @FXML private Label lblStatus;

    private final OrderDAO orderDAO = new OrderDAO();
    private Timeline autoRefresh;

    // Cache user names to avoid repeated DB hits on every refresh
    private final Map<Integer, String> userNameCache = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            List<Integer> allIds = orderDAO.getAllOrdersAdmin()
                    .stream().map(Order::getId).collect(Collectors.toList());
            ChefVetoManager.lockAll(allIds);
        } catch (Exception e) {
            System.err.println("Veto seed error: " + e.getMessage());
        }
        loadOrders();
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(10), e -> loadOrders()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    @FXML void refresh() { loadOrders(); }

    private void loadOrders() {
        ordersList.getChildren().clear();
        try {
            List<Order> orders = orderDAO.getAllOrdersAdmin();
            // Also load items for each order
            List<Order> ordersWithItems = orderDAO.getAllOrdersWithItems();
            orders.forEach(o -> ChefVetoManager.lock(o.getId()));

            if (ordersWithItems.isEmpty()) {
                Label none = new Label("No orders yet.");
                none.setStyle("-fx-font-size:13px;-fx-padding:16 12 16 12;-fx-text-fill:#888;");
                ordersList.getChildren().add(none);
                return;
            }

            for (Order o : ordersWithItems) {
                String userName = getUserName(o.getUserId());
                ordersList.getChildren().add(buildCard(o, userName));
            }

            if (lblStatus != null)
                lblStatus.setText("Last refreshed: " +
                    java.time.LocalTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));

        } catch (Exception e) {
            Label err = new Label("Error loading orders: " + e.getMessage());
            err.setStyle("-fx-font-size:13px;-fx-text-fill:red;-fx-padding:12;");
            ordersList.getChildren().add(err);
            System.err.println("Admin load error: " + e.getMessage());
        }
    }

    /** Look up user's full name from DB, cache it. */
    private String getUserName(int userId) {
        if (userNameCache.containsKey(userId)) return userNameCache.get(userId);
        try (Connection conn = com.bytemenu.dao.DatabaseManager.getInstance().getNewConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT full_name FROM users WHERE id = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("full_name");
                userNameCache.put(userId, name);
                return name;
            }
        } catch (Exception e) {
            System.err.println("getUserName error: " + e.getMessage());
        }
        return "User #" + userId;
    }

    private VBox buildCard(Order order, String userName) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color:white;-fx-border-color:#E0E0E0;" +
                "-fx-border-radius:6;-fx-background-radius:6;" +
                "-fx-padding:14 18 14 18;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),4,0,0,1);");
        VBox.setMargin(card, new javafx.geometry.Insets(0, 20, 10, 20));

        // ── Top row: order id + amount + status pill ──
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label idLbl = new Label("Order #" + order.getId());
        idLbl.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:#1F4E79;");
        HBox.setHgrow(idLbl, Priority.ALWAYS);

        Label totalLbl = new Label("Rs. " + (int) order.getTotal());
        totalLbl.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#333;");

        Label statusLbl = new Label(order.getStatus().toUpperCase());
        String pillStyle = switch (order.getStatus()) {
            case "Ready to Pick", "Completed" -> "-fx-background-color:#D4EDDA;-fx-text-fill:#0A5233;";
            case "Cancelled"                  -> "-fx-background-color:#F8D7DA;-fx-text-fill:#C8372D;";
            default                           -> "-fx-background-color:#EBF3FB;-fx-text-fill:#1F4E79;";
        };
        statusLbl.setStyle(pillStyle +
                "-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:3 10 3 10;");

        topRow.getChildren().addAll(idLbl, totalLbl, statusLbl);

        // ── Student name ──
        Label nameLbl = new Label(userName);
        nameLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#1F4E79;-fx-font-weight:bold;");

        // ── Items list ──
        VBox itemsBox = new VBox(3);
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem oi : order.getItems()) {
                String line = oi.getQuantity() + "x  " + oi.getName();
                if (oi.getCustomization() != null && !oi.getCustomization().isBlank())
                    line += "  — " + oi.getCustomization();
                Label itemLbl = new Label("• " + line);
                itemLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#444;");
                itemsBox.getChildren().add(itemLbl);
            }
        } else {
            Label fallback = new Label("(no item details available)");
            fallback.setStyle("-fx-font-size:12px;-fx-text-fill:#AAA;");
            itemsBox.getChildren().add(fallback);
        }

        if (order.getNotes() != null && !order.getNotes().isBlank()) {
            Label note = new Label("📝  " + order.getNotes());
            note.setStyle("-fx-font-size:11px;-fx-text-fill:#888;-fx-font-style:italic;");
            itemsBox.getChildren().add(note);
        }

        // ── Action buttons ──
        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button bPrep = actionBtn("PREPARING", "#1F4E79", "#EBF3FB");
        Button bDone = actionBtn("DONE",      "#0A5233", "#D4EDDA");
        Button bCanc = actionBtn("CANCELLED", "#C8372D", "#F8D7DA");

        bPrep.setOnAction(e -> chefSet(order.getId(), "Preparing"));
        bDone.setOnAction(e -> chefSet(order.getId(), "Completed"));
        bCanc.setOnAction(e -> chefSet(order.getId(), "Cancelled"));

        btnRow.getChildren().addAll(bPrep, bDone, bCanc);

        card.getChildren().addAll(topRow, nameLbl, itemsBox, new Separator(), btnRow);
        return card;
    }

    private Button actionBtn(String text, String textColor, String bgColor) {
        Button b = new Button(text);
        b.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:6 14 6 14;" +
                "-fx-cursor:hand;-fx-background-radius:4;" +
                "-fx-text-fill:" + textColor + ";-fx-background-color:" + bgColor + ";");
        b.setMinWidth(90);
        return b;
    }

    private void chefSet(int orderId, String status) {
        try {
            orderDAO.updateStatus(orderId, status);
            ChefVetoManager.lock(orderId);
        } catch (Exception e) {
            System.err.println("Chef set error: " + e.getMessage());
        }
        loadOrders();
    }

    @FXML void handleLogout() {
        if (autoRefresh != null) autoRefresh.stop();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/bytemenu/fxml/Login.fxml"));
            Stage stage = (Stage) ordersList.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 620));
            stage.setTitle("ByteMenu — Login");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
