package com.bytemenu.util;

import com.bytemenu.model.MenuItem;
import com.bytemenu.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Singleton in-memory cart shared across views.
 */
public class CartManager {

    private static final ObservableList<OrderItem> items = FXCollections.observableArrayList();

    public static void addItem(MenuItem menuItem) {
        // If item already in cart, increment quantity
        for (OrderItem oi : items) {
            if (oi.getMenuItemId() == menuItem.getId()) {
                oi.setQuantity(oi.getQuantity() + 1);
                // Notify observers by triggering a list change
                int idx = items.indexOf(oi);
                items.set(idx, oi);
                return;
            }
        }
        items.add(new OrderItem(menuItem.getId(), menuItem.getName(), 1, "", menuItem.getPrice()));
    }

    public static ObservableList<OrderItem> getItems() { return items; }

    public static double getTotal() {
        return items.stream().mapToDouble(OrderItem::getSubtotal).sum();
    }

    public static void clear() { items.clear(); }

    public static int size() { return items.size(); }
}
