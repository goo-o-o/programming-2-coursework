package com.bryan.programming2coursework.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Order model representing a customer order
 */
public class Order implements Serializable {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int id;
    private int userId;
    private List<OrderItem> items;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private double totalAmount;
    private String customerName;

    public enum OrderStatus {
        PENDING, PREPARING, READY, COMPLETED, CANCELLED
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

    public Order(int id, int userId, String customerName) {
        this(id, userId);
        this.customerName = customerName;
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
        calculateTotal();
    }

    public void addItem(OrderItem item) {
        // Check if item already exists, if so, increase quantity
        for (OrderItem existingItem : items) {
            if (existingItem.getMenuItem().getId() == item.getMenuItem().getId()) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                calculateTotal();
                return;
            }
        }
        items.add(item);
        calculateTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        calculateTotal();
    }

    public void clearItems() {
        items.clear();
        calculateTotal();
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

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void calculateTotal() {
        totalAmount = items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    @Override
    public String toString() {
        return String.format("Order #%d - %s - RM%.2f - %s",
                id, getFormattedOrderDate(), totalAmount, status);
    }
}