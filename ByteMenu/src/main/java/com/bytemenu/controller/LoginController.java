package com.bytemenu.controller;

import com.bytemenu.dao.OrderDAO;
import com.bytemenu.dao.UserDAO;
import com.bytemenu.model.Order;
import com.bytemenu.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoginController {

    @FXML private VBox loginForm, registerForm;
    @FXML private Button btnLoginTab, btnRegisterTab;
    @FXML private TextField loginEmail, regName, regEmail;
    @FXML private PasswordField loginPassword, regPassword, regConfirm;
    @FXML private Label loginError, regError;
    @FXML private TextField trackOrderField;
    @FXML private VBox trackerBar;
    @FXML private VBox feedList;
    @FXML private ScrollPane feedScroll;

    private final UserDAO  userDAO  = new UserDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private static final String ADMIN_USERNAME = "Admin";
    private static final String ADMIN_KEY      = "1234";

    // ── Persistent feed cache (static — survives screen transitions) ──
    private static final List<String[]> FEED_CACHE = new ArrayList<>();
    private static final String[] FEED_ITEMS = {
        "Chicken Burger","Beef Burger","Veggie Wrap","Chicken Biryani",
        "Pasta Alfredo","French Fries","Chicken Nuggets","Spring Rolls",
        "Pepsi","Mango Juice","Chocolate Cake","Gulab Jamun","Ice Cream"
    };
    private static final String[] STATUSES = {"Ready to Pick ✓","Ready to Pick ✓","Cancelled ✗"};
    private static final String[] NAMES    = {"SA","MH","AR","ZK","FN","IM","RB","YA"};
    private static final Random RNG = new Random();
    private static int simNum = 1000;

    static {
        for (int i = 0; i < 8; i++) addToCache();
    }

    private static void addToCache() {
        simNum++;
        String t = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        FEED_CACHE.add(0, new String[]{
            "#" + simNum + " · " + NAMES[RNG.nextInt(NAMES.length)],
            FEED_ITEMS[RNG.nextInt(FEED_ITEMS.length)],
            STATUSES[RNG.nextInt(STATUSES.length)], t
        });
        if (FEED_CACHE.size() > 30) FEED_CACHE.remove(FEED_CACHE.size() - 1);
    }

    @FXML
    public void initialize() {
        renderFeed();
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(4), e ->
            Platform.runLater(() -> { addToCache(); renderFeed(); })
        ));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    private void renderFeed() {
        feedList.getChildren().clear();
        for (String[] entry : FEED_CACHE) {
            HBox row = new HBox(10); row.getStyleClass().add("feed-item");
            VBox info = new VBox(2);
            Label idLbl = new Label(entry[0]); idLbl.getStyleClass().add("feed-order-id");
            Label nm    = new Label(entry[1]); nm.getStyleClass().add("feed-item-name");
            info.getChildren().addAll(idLbl, nm);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            VBox sb = new VBox(2); sb.setAlignment(Pos.CENTER_RIGHT);
            Label sl = new Label(entry[2]);
            sl.getStyleClass().add(entry[2].startsWith("Ready") ? "status-ready" : "status-cancelled");
            Label tl2 = new Label(entry[3]); tl2.getStyleClass().add("feed-order-id");
            sb.getChildren().addAll(sl, tl2);
            row.getChildren().addAll(info, sp, sb);
            feedList.getChildren().add(row);
        }
    }

    // ── Tab switching ──────────────────────────────────────────────────
    @FXML void showLogin() {
        loginForm.setVisible(true);    loginForm.setManaged(true);
        registerForm.setVisible(false); registerForm.setManaged(false);
        btnLoginTab.getStyleClass().setAll("tab-toggle-active");
        btnRegisterTab.getStyleClass().setAll("tab-toggle");
        if (trackerBar != null) { trackerBar.setVisible(true); trackerBar.setManaged(true); }
        loginError.setText("");
    }

    @FXML void showRegister() {
        registerForm.setVisible(true);  registerForm.setManaged(true);
        loginForm.setVisible(false);    loginForm.setManaged(false);
        btnRegisterTab.getStyleClass().setAll("tab-toggle-active");
        btnLoginTab.getStyleClass().setAll("tab-toggle");
        // Hide tracker — it breaks register layout
        if (trackerBar != null) { trackerBar.setVisible(false); trackerBar.setManaged(false); }
        regError.setText("");
    }

    // ── Unified login — routes Admin → portal, student → main ─────────
    @FXML void handleLogin() {
        loginError.setText("");
        String id   = loginEmail.getText().trim();
        String pass = loginPassword.getText();
        if (id.isEmpty() || pass.isEmpty()) {
            loginError.setText("Please fill in all fields."); return;
        }
        if (id.equals(ADMIN_USERNAME)) {
            if (pass.equals(ADMIN_KEY)) loadAdminPortal();
            else loginError.setText("Invalid admin key.");
            return;
        }
        try {
            var user = userDAO.login(id, pass);
            SessionManager.setCurrentUser(user);
            loadMain();
        } catch (Exception e) { loginError.setText(e.getMessage()); }
    }

    @FXML void handleRegister() {
        regError.setText("");
        String name    = regName.getText().trim();
        String email   = regEmail.getText().trim();
        String pass    = regPassword.getText();
        String confirm = regConfirm.getText();
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            regError.setText("Please fill in all fields."); return;
        }
        if (pass.length() < 8) { regError.setText("Password must be at least 8 characters."); return; }
        if (!pass.equals(confirm)) { regError.setText("Passwords do not match."); return; }
        try {
            var user = userDAO.register(name, email, pass);
            SessionManager.setCurrentUser(user);
            loadMain();
        } catch (Exception e) { regError.setText(e.getMessage()); }
    }

    // ── Order status lookup — modal popup, no screen resize ───────────
    @FXML void handleTrackOrder() {
        String raw = (trackOrderField != null) ? trackOrderField.getText().trim() : "";
        int orderId;
        try { orderId = Integer.parseInt(raw); }
        catch (NumberFormatException e) { return; }   // non-numeric — ignore silently
        showStatusPopup(orderId);
    }

    private void showStatusPopup(int orderId) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(trackOrderField != null
                ? trackOrderField.getScene().getWindow() : null);
        dialog.setTitle("Order Status");
        dialog.setResizable(false);

        VBox root = new VBox(14);
        root.setStyle("-fx-padding:28;-fx-background-color:white;");
        root.setPrefWidth(420);
        root.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Order Status");
        title.setStyle("-fx-font-weight:bold;-fx-font-size:17px;-fx-text-fill:#1F4E79;");

        Label resultLbl = new Label();
        resultLbl.setWrapText(true); resultLbl.setMaxWidth(360);
        Label infoLbl = new Label();
        infoLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#888;"); infoLbl.setWrapText(true);

        try {
            List<Order> all = orderDAO.getAllOrdersAdmin();
            Order found = all.stream().filter(o -> o.getId() == orderId).findFirst().orElse(null);
            if (found == null) {
                resultLbl.setText("Order #" + orderId + " not found.");
                resultLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#C8372D;");
            } else {
                String display = switch (found.getStatus()) {
                    case "Preparing"     -> "⏳  Preparing — your order is being made";
                    case "Ready to Pick" -> "✅  Ready to Pick — head to the counter!";
                    case "Completed"     -> "✔  Completed — enjoy your meal!";
                    case "Cancelled"     -> "✖  Cancelled — please contact the counter";
                    default              -> found.getStatus();
                };
                String color = switch (found.getStatus()) {
                    case "Ready to Pick","Completed" -> "#0A5233";
                    case "Cancelled"                 -> "#C8372D";
                    default                          -> "#1F4E79";
                };
                resultLbl.setText(display);
                resultLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
                infoLbl.setText("Order #" + found.getId()
                        + "   ·   Rs. " + (int) found.getTotal()
                        + (found.getCreatedAt() != null ? "   ·   " + found.getCreatedAt() : ""));
            }
        } catch (Exception e) {
            resultLbl.setText("Error: " + e.getMessage());
            resultLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#C8372D;");
        }

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color:#1F4E79;-fx-text-fill:white;-fx-background-radius:4;" +
                "-fx-padding:8 20 8 20;-fx-cursor:hand;-fx-font-weight:bold;");
        closeBtn.setOnAction(e -> dialog.close());
        HBox btnRow = new HBox(closeBtn); btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, resultLbl, infoLbl, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void loadAdminPortal() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/bytemenu/fxml/Admin.fxml"));
            Stage stage = (Stage) loginEmail.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 620));
            stage.setTitle("ByteMenu — Admin / Chef Portal");
        } catch (Exception e) { loginError.setText("Portal load failed: " + e.getMessage()); }
    }

    private void loadMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/bytemenu/fxml/Main.fxml"));
            Stage stage = (Stage) loginEmail.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 620));
            stage.setTitle("ByteMenu — Cafeteria Management System");
        } catch (Exception e) { loginError.setText("Load failed: " + e.getMessage()); }
    }
}
