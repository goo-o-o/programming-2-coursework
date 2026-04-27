package com.bryan.programming2coursework.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Order model representing a customer order
 */
public class Order {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int id;
    private int userId;
    private List<OrderItem> items;
    private LocalDateTime orderDate;
    private OrderStatus status;

    public enum OrderStatus {
        PENDING, // yet to cook
        PREPARING, // cooking
        READY, // ready to serve
        COMPLETED, // served
        CANCELLED // cancelled
    }

    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    public Order(int id, int userId) {
        this();
        this.id = id;
        this.userId = userId;
    }


    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addItem(OrderItem newItem) {
        for (OrderItem existingItem : items) {
            if (existingItem.getMenuItem().getId() == newItem.getMenuItem().getId()) {
                // exists already, update quantity
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        // add as new row if not
        items.add(newItem);
    }
    public void removeItem(OrderItem item) {
        items.remove(item);
    }

    public void clearItems() {
        items.clear();
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getFormattedOrderDate() {
        return orderDate.format(DATE_FORMATTER);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }


    @Override
    public String toString() {
        return String.format("Order #%d - %s - RM%.2f - %s",
                id, getFormattedOrderDate(), getTotalPrice(), status);
    }
}