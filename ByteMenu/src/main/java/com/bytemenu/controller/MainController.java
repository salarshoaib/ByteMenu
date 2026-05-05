package com.bytemenu.controller;

import com.bytemenu.dao.UserDAO;
import com.bytemenu.util.CartManager;
import com.bytemenu.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUser;
    @FXML private Label lblBalance;
    @FXML private Button btnMenu, btnOrders, btnSeats, btnMyOrders;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        refreshUserInfo();
        showMenu();
    }

    public void refreshUserInfo() {
        if (SessionManager.getCurrentUser() == null) return;
        String first = SessionManager.getCurrentUser().getFullName().split(" ")[0];
        if (lblUser != null) lblUser.setText("Welcome, " + first);
        try {
            double bal = userDAO.getWalletBalance(SessionManager.getCurrentUser().getId());
            SessionManager.getCurrentUser().setWalletBalance(bal);
            if (lblBalance != null) {
                lblBalance.setText("Wallet: Rs. " + (int) bal);
                lblBalance.setStyle("-fx-text-fill:#AAFFCC;-fx-font-size:11px;");
            }
        } catch (Exception e) {
            if (lblBalance != null) lblBalance.setText("Wallet: —");
        }
    }

    @FXML public void showMenu()     { loadView("Menu.fxml");     setActive(btnMenu); }
    @FXML public void showOrders()   { loadView("Order.fxml");    setActive(btnOrders); }
    @FXML public void showSeats()    { loadView("Seat.fxml");     setActive(btnSeats); }
    @FXML public void showMyOrders() { loadView("MyOrders.fxml"); setActive(btnMyOrders); }

    @FXML
    public void handleLogout() {
        CartManager.clear();
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/bytemenu/fxml/Login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 620));
            stage.setTitle("ByteMenu — Login");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadView(String fxml) {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/com/bytemenu/fxml/" + fxml));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setActive(Button active) {
        for (Button b : new Button[]{btnMenu, btnOrders, btnSeats, btnMyOrders})
            b.getStyleClass().setAll(b == active ? "nav-button-active" : "nav-button");
    }
}
