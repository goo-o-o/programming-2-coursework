package com.bryan.programming2coursework.model;

/**
 * OrderItem represents a menu item with quantity in an order
 */
public class OrderItem {

    private MenuItem menuItem;
    private int quantity;

    public OrderItem() {
    }

    public OrderItem(MenuItem menuItem, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return menuItem.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return String.format("%s x%d - RM%.2f", menuItem.getName(), quantity, getSubtotal());
    }
}