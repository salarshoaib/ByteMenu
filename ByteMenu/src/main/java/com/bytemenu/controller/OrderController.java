package com.bytemenu.controller;

import com.bytemenu.dao.OrderDAO;
import com.bytemenu.dao.UserDAO;
import com.bytemenu.model.Order;
import com.bytemenu.model.OrderItem;
import com.bytemenu.util.CartManager;
import com.bytemenu.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OrderController {

    @FXML private VBox cartList, emptyState;
    @FXML private Label lblItemCount, lblSubtotal, lblTotal, orderMsg;
    @FXML private TextArea orderNotes;

    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO  userDAO  = new UserDAO();

    @FXML public void initialize() { refreshCart(); }

    private void refreshCart() {
        cartList.getChildren().clear();
        var items = CartManager.getItems();
        boolean empty = items.isEmpty();
        emptyState.setVisible(empty);  emptyState.setManaged(empty);
        cartList.setVisible(!empty);   cartList.setManaged(!empty);
        for (OrderItem oi : items) cartList.getChildren().add(buildCartRow(oi));
        int count = items.stream().mapToInt(OrderItem::getQuantity).sum();
        lblItemCount.setText(String.valueOf(count));
        lblSubtotal.setText("Rs. " + (int) CartManager.getTotal());
        lblTotal.setText("Rs. " + (int) CartManager.getTotal());
        orderMsg.setText("");
    }

    private HBox buildCartRow(OrderItem oi) {
        HBox row = new HBox(10);
        row.getStyleClass().add("card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding:12 16 12 16;-fx-background-radius:6;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        info.setMinWidth(0);

        Label name = new Label(oi.getName());
        name.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#222;");
        name.setWrapText(true); name.setMaxWidth(Double.MAX_VALUE);

        String raw = oi.getCustomization();
        boolean has = raw != null && !raw.isBlank();
        Label custom = new Label(has ? raw : "No customization");
        custom.setStyle("-fx-font-size:11px;-fx-text-fill:" + (has ? "#2E75B6" : "#AAAAAA") + ";");
        custom.setWrapText(true); custom.setMaxWidth(Double.MAX_VALUE);
        info.getChildren().addAll(name, custom);

        Button customBtn = new Button("Customize");
        customBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#2E75B6;-fx-font-size:11px;" +
                "-fx-border-color:#2E75B6;-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;" +
                "-fx-padding:4 10 4 10;-fx-cursor:hand;");
        customBtn.setOnAction(e -> { openCustomizeDialog(oi); refreshCart(); });

        Button minus = new Button("−");
        minus.setStyle("-fx-background-color:#EBF3FB;-fx-text-fill:#1F4E79;-fx-font-weight:bold;" +
                "-fx-background-radius:4;-fx-padding:4 10 4 10;-fx-cursor:hand;");

        Label qty = new Label(oi.getQuantity() + "x");
        qty.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#1F4E79;-fx-padding:0 6 0 6;");

        Button plus = new Button("+");
        plus.setStyle("-fx-background-color:#EBF3FB;-fx-text-fill:#1F4E79;-fx-font-weight:bold;" +
                "-fx-background-radius:4;-fx-padding:4 10 4 10;-fx-cursor:hand;");

        minus.setOnAction(e -> {
            if (oi.getQuantity() > 1) oi.setQuantity(oi.getQuantity() - 1);
            else CartManager.getItems().remove(oi);
            refreshCart();
        });
        plus.setOnAction(e -> { oi.setQuantity(oi.getQuantity() + 1); refreshCart(); });

        Label price = new Label("Rs. " + (int)(oi.getUnitPrice() * oi.getQuantity()));
        price.setStyle("-fx-font-weight:bold;-fx-text-fill:#1F4E79;-fx-font-size:13px;-fx-padding:0 0 0 8;");
        price.setMinWidth(70);
        row.getChildren().addAll(info, customBtn, minus, qty, plus, price);
        return row;
    }

    private void openCustomizeDialog(OrderItem oi) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Customize — " + oi.getName());
        dialog.setResizable(false);

        VBox root = new VBox(14);
        root.setStyle("-fx-padding:24;-fx-background-color:white;");
        root.setPrefWidth(360);

        Label title = new Label("Customize: " + oi.getName());
        title.setStyle("-fx-font-weight:bold;-fx-font-size:15px;-fx-text-fill:#1F4E79;");
        Label hint = new Label("Common options (click to add):");
        hint.setStyle("-fx-text-fill:#888;-fx-font-size:12px;");

        FlowPane chips = new FlowPane(8, 8);
        String[] options = {"No spice","Extra spice","No onion","No mayo",
                "Extra sauce","Less salt","No ice","Extra cheese","Gluten-free","No pickles"};
        String existing = (oi.getCustomization() == null) ? "" : oi.getCustomization();
        TextArea customField = new TextArea(existing);
        customField.setPromptText("Type or click options above...");
        customField.setPrefHeight(70); customField.setWrapText(true);
        customField.setStyle("-fx-font-size:12px;-fx-border-color:#D0D0D5;-fx-border-radius:4;");

        for (String opt : options) {
            Button chip = new Button(opt);
            chip.setStyle("-fx-background-color:#EBF3FB;-fx-text-fill:#1F4E79;-fx-font-size:11px;" +
                    "-fx-background-radius:20;-fx-padding:4 12 4 12;-fx-cursor:hand;");
            chip.setOnAction(e -> {
                String cur = customField.getText().trim();
                customField.setText(cur.isEmpty() ? opt : cur + ", " + opt);
            });
            chips.getChildren().add(chip);
        }

        HBox btnRow = new HBox(10); btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color:transparent;-fx-text-fill:#555;-fx-border-color:#D0D0D5;" +
                "-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;" +
                "-fx-padding:8 18 8 18;-fx-cursor:hand;");
        cancel.setOnAction(e -> dialog.close());
        Button save = new Button("Save");
        save.setStyle("-fx-background-color:#1F4E79;-fx-text-fill:white;-fx-background-radius:4;" +
                "-fx-padding:8 18 8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        save.setOnAction(e -> { oi.setCustomization(customField.getText().trim()); dialog.close(); });
        btnRow.getChildren().addAll(cancel, save);
        root.getChildren().addAll(title, hint, chips, customField, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    @FXML
    void handlePlaceOrder() {
        if (CartManager.size() == 0) {
            orderMsg.setStyle("-fx-text-fill:#C8372D;-fx-font-size:12px;");
            orderMsg.setText("Your cart is empty. Add items from the menu first.");
            return;
        }
        openPaymentDialog();
    }

    private void openPaymentDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Checkout & Payment");
        dialog.setResizable(false);

        // ── Scrollable content area (payment options) ──
        VBox content = new VBox(12);
        content.setStyle("-fx-padding:24 28 8 28;-fx-background-color:white;");
        content.setPrefWidth(440);

        Label title = new Label("Checkout & Payment");
        title.setStyle("-fx-font-weight:bold;-fx-font-size:17px;-fx-text-fill:#1F4E79;");

        Label amountLbl = new Label("Amount Due:  Rs. " + (int) CartManager.getTotal());
        amountLbl.setStyle("-fx-font-size:15px;-fx-text-fill:#0A5233;-fx-font-weight:bold;");

        Separator sep = new Separator();

        Label methodLabel = new Label("Select Payment Method:");
        methodLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#555;-fx-font-weight:bold;");

        ToggleGroup group = new ToggleGroup();
        RadioButton rbCard   = new RadioButton("Credit / Debit Card");
        RadioButton rbWallet = new RadioButton("University Wallet");
        RadioButton rbCash   = new RadioButton("Pay at Counter (Cash)");
        for (RadioButton rb : new RadioButton[]{rbCard, rbWallet, rbCash}) {
            rb.setToggleGroup(group);
            rb.setStyle("-fx-font-size:13px;-fx-text-fill:#222;-fx-padding:3 0 3 0;");
        }
        rbWallet.setSelected(true);

        // Card fields
        VBox cardFields = new VBox(8);
        cardFields.setVisible(false); cardFields.setManaged(false);
        TextField cardNum = new TextField();
        cardNum.setPromptText("Card number  (e.g. 1234 5678 9012 3456)");
        cardNum.setMaxWidth(Double.MAX_VALUE);
        HBox expCvvRow = new HBox(8);
        TextField cardExp = new TextField(); cardExp.setPromptText("MM/YY"); cardExp.setPrefWidth(110);
        TextField cardCvv = new TextField(); cardCvv.setPromptText("CVV");   cardCvv.setPrefWidth(80);
        expCvvRow.getChildren().addAll(cardExp, cardCvv);
        cardFields.getChildren().addAll(cardNum, expCvvRow);

        // Wallet info
        double currentBal = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser().getWalletBalance() : 0;
        VBox walletInfo = new VBox(4);
        walletInfo.setVisible(true); walletInfo.setManaged(true);
        Label walletBalLbl = new Label("Current balance:  Rs. " + (int) currentBal);
        walletBalLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#1F4E79;-fx-font-weight:bold;");
        walletInfo.getChildren().add(walletBalLbl);

        Label payError = new Label("");
        payError.setStyle("-fx-text-fill:#C8372D;-fx-font-size:12px;");
        payError.setWrapText(true);

        rbCard.setOnAction(e -> {
            cardFields.setVisible(true);  cardFields.setManaged(true);
            walletInfo.setVisible(false); walletInfo.setManaged(false);
        });
        rbWallet.setOnAction(e -> {
            cardFields.setVisible(false); cardFields.setManaged(false);
            walletInfo.setVisible(true);  walletInfo.setManaged(true);
        });
        rbCash.setOnAction(e -> {
            cardFields.setVisible(false); cardFields.setManaged(false);
            walletInfo.setVisible(false); walletInfo.setManaged(false);
        });

        content.getChildren().addAll(
            title, amountLbl, sep,
            methodLabel, rbCard, rbWallet, rbCash,
            cardFields, walletInfo,
            payError
        );

        // ── Fixed bottom bar — always visible, never scrolls ──
        final boolean[] confirmed = {false};

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#555;-fx-border-color:#D0D0D5;" +
                "-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;" +
                "-fx-padding:10 22 10 22;-fx-cursor:hand;-fx-font-size:13px;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button payBtn = new Button("Confirm & Place Order  →");
        payBtn.setStyle("-fx-background-color:#0A5233;-fx-text-fill:white;-fx-background-radius:4;" +
                "-fx-padding:10 22 10 22;-fx-cursor:hand;-fx-font-weight:bold;-fx-font-size:13px;");
        payBtn.setOnAction(e -> {
            payError.setText("");
            if (rbCard.isSelected()) {
                String num = cardNum.getText().replaceAll("\\s", "");
                if (num.length() != 16 || !num.matches("\\d+")) {
                    payError.setText("Enter a valid 16-digit card number."); return;
                }
                if (!cardExp.getText().matches("\\d{2}/\\d{2}")) {
                    payError.setText("Enter expiry as MM/YY  (e.g. 11/27)."); return;
                }
                if (!cardCvv.getText().matches("\\d{3,4}")) {
                    payError.setText("Enter a valid CVV (3 or 4 digits)."); return;
                }
            }
            if (rbWallet.isSelected()) {
                double total = CartManager.getTotal();
                try {
                    userDAO.deductWallet(SessionManager.getCurrentUser().getId(), total);
                    SessionManager.getCurrentUser().setWalletBalance(
                        SessionManager.getCurrentUser().getWalletBalance() - total);
                } catch (Exception ex) {
                    payError.setText(ex.getMessage()); return;
                }
            }
            confirmed[0] = true;
            dialog.close();
        });

        HBox btnBar = new HBox(10, cancelBtn, payBtn);
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        btnBar.setStyle("-fx-padding:12 28 16 28;-fx-background-color:white;" +
                "-fx-border-color:#E8E8E8;-fx-border-width:1 0 0 0;");

        // ── Root layout: content scrolls, button bar stays at bottom ──
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:white;");
        root.setCenter(content);
        root.setBottom(btnBar);

        dialog.setScene(new Scene(root, 460, 480));
        dialog.showAndWait();

        if (confirmed[0]) {
            submitOrder();
            updateNavBalance();
        }
    }

    private void updateNavBalance() {
        try {
            var scene = cartList.getScene();
            if (scene == null) return;
            var balLbl = (Label) scene.lookup("#lblBalance");
            if (balLbl != null && SessionManager.getCurrentUser() != null)
                balLbl.setText("Wallet: Rs. " +
                    (int) SessionManager.getCurrentUser().getWalletBalance());
        } catch (Exception ignored) {}
    }

    private void submitOrder() {
        Order order = new Order();
        order.setUserId(SessionManager.getCurrentUser().getId());
        order.setNotes(orderNotes.getText().trim());
        CartManager.getItems().forEach(order::addItem);
        try {
            Order saved = orderDAO.placeOrder(order);
            CartManager.clear(); orderNotes.clear(); refreshCart();
            orderMsg.setStyle("-fx-text-fill:#0A5233;-fx-font-size:13px;-fx-font-weight:bold;");
            orderMsg.setText("✓ Payment confirmed. Order #" + saved.getId() +
                    " placed!\nTrack it in 'My Orders'.");
        } catch (Exception e) {
            orderMsg.setStyle("-fx-text-fill:#C8372D;-fx-font-size:12px;");
            orderMsg.setText("Failed to place order: " + e.getMessage());
        }
    }

    @FXML void handleClearCart() { CartManager.clear(); refreshCart(); }

    @FXML void goToMenu() {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/com/bytemenu/fxml/Menu.fxml"));
            ((StackPane) cartList.getScene().lookup("#contentArea")).getChildren().setAll(view);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
