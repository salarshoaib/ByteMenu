package com.bytemenu.controller;

import com.bytemenu.dao.MenuDAO;
import com.bytemenu.model.MenuItem;
import com.bytemenu.util.CartManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuController {

    @FXML private VBox menuGridContainer;
    @FXML private TextField searchField;
    @FXML private Button btnAll, btnMain, btnSnacks, btnDrinks, btnDesserts;

    private final MenuDAO menuDAO = new MenuDAO();
    private List<MenuItem> allItems;
    private Button activeTab;
    private final Map<Integer, Label> badgeMap = new HashMap<>();

    private GridPane menuGrid;

    @FXML
    public void initialize() {
        try { allItems = menuDAO.getAllAvailable(); }
        catch (Exception e) { allItems = List.of(); }
        activeTab = btnAll;

        // Build a GridPane with 4 equal % columns — guaranteed to fill width
        menuGrid = new GridPane();
        menuGrid.setHgap(10);
        menuGrid.setVgap(10);
        menuGrid.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(menuGrid, Priority.ALWAYS);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            menuGrid.getColumnConstraints().add(cc);
        }

        menuGridContainer.getChildren().add(menuGrid);

        renderItems(allItems);
    }

    @FXML void filterAll()      { setTab(btnAll);      renderItems(allItems); }
    @FXML void filterMain()     { setTab(btnMain);     filterBy("Main Course"); }
    @FXML void filterSnacks()   { setTab(btnSnacks);   filterBy("Snacks"); }
    @FXML void filterDrinks()   { setTab(btnDrinks);   filterBy("Drinks"); }
    @FXML void filterDesserts() { setTab(btnDesserts); filterBy("Desserts"); }

    @FXML
    void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) { renderItems(allItems); return; }
        renderItems(allItems.stream()
                .filter(i -> i.getName().toLowerCase().contains(kw)
                          || i.getCategory().toLowerCase().contains(kw))
                .toList());
    }

    private void filterBy(String category) {
        renderItems(allItems.stream().filter(i -> i.getCategory().equals(category)).toList());
    }

    private void renderItems(List<MenuItem> items) {
        menuGrid.getChildren().clear();
        badgeMap.clear();
        int col = 0, row = 0;
        for (MenuItem item : items) {
            VBox card = buildCard(item);
            menuGrid.add(card, col, row);
            col++;
            if (col == 4) { col = 0; row++; }
        }
    }

    private VBox buildCard(MenuItem item) {
        VBox card = new VBox(4);
        card.getStyleClass().add("menu-item-card");
        card.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(card, true);
        GridPane.setHgrow(card, Priority.ALWAYS);

        Label cat = new Label(item.getCategory().toUpperCase());
        cat.setStyle("-fx-text-fill:#AAAAAA;-fx-font-size:10px;-fx-font-weight:bold;");

        Label name = new Label(item.getName());
        name.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#222;");
        name.setWrapText(true);

        Label desc = new Label(item.getDescription());
        desc.setStyle("-fx-text-fill:#777;-fx-font-size:11px;");
        desc.setWrapText(true);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox bottom = new HBox(6);
        bottom.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label("Rs. " + (int) item.getPrice());
        price.getStyleClass().add("menu-item-price");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label badge = new Label();
        badge.setStyle("-fx-background-color:#1F4E79;-fx-text-fill:white;-fx-font-size:10px;" +
                "-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:2 7 2 7;");
        badge.setAlignment(Pos.CENTER);
        badgeMap.put(item.getId(), badge);

        int existingQty = CartManager.getItems().stream()
                .filter(oi -> oi.getName().equals(item.getName()))
                .mapToInt(oi -> oi.getQuantity()).sum();
        if (existingQty > 0) {
            badge.setText("+" + existingQty);
            badge.setVisible(true);
            badge.setManaged(true);
        } else {
            badge.setVisible(false);
            badge.setManaged(false);
        }

        Button addBtn = new Button("+ Add");
        addBtn.setStyle("-fx-background-color:#1F4E79;-fx-text-fill:white;-fx-font-size:11px;" +
                "-fx-background-radius:4;-fx-padding:4 10 4 10;-fx-cursor:hand;");

        addBtn.setOnAction(e -> {
            CartManager.addItem(item);
            int qty = CartManager.getItems().stream()
                    .filter(oi -> oi.getName().equals(item.getName()))
                    .mapToInt(oi -> oi.getQuantity()).sum();
            badge.setText("+" + qty);
            badge.setVisible(true);
            badge.setManaged(true);
            addBtn.setStyle("-fx-background-color:#0A5233;-fx-text-fill:white;-fx-font-size:11px;" +
                    "-fx-background-radius:4;-fx-padding:4 10 4 10;-fx-cursor:hand;");
            addBtn.setText("✓ Added");
            new Timeline(new KeyFrame(Duration.millis(900), ev -> {
                addBtn.setStyle("-fx-background-color:#1F4E79;-fx-text-fill:white;-fx-font-size:11px;" +
                        "-fx-background-radius:4;-fx-padding:4 10 4 10;-fx-cursor:hand;");
                addBtn.setText("+ Add");
            })).play();
        });

        bottom.getChildren().addAll(price, sp, badge, addBtn);
        card.getChildren().addAll(cat, name, desc, spacer, bottom);
        return card;
    }

    private void setTab(Button tab) {
        if (activeTab != null) activeTab.getStyleClass().setAll("tab-btn");
        tab.getStyleClass().setAll("tab-btn-active");
        activeTab = tab;
        searchField.clear();
    }
}
