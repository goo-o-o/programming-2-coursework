package com.bryan.programming2coursework.dao;

import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.Order.OrderStatus;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data Access Object for Order persistence
 */
public class OrderDAO {
    private static final String DATA_FILE = "data/orders.dat";
    private static OrderDAO instance;
    private List<Order> orders;
    private int nextId;

    private OrderDAO() {
        orders = new ArrayList<>();
        loadFromFile();

        if (orders.isEmpty()) {
            nextId = 1;
        }
    }

    public static synchronized OrderDAO getInstance() {
        if (instance == null) {
            instance = new OrderDAO();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(DATA_FILE);
        if (file.getParentFile().mkdirs()) {

            if (!file.exists()) {
                nextId = 1;
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                orders = (List<Order>) ois.readObject();
                nextId = ois.readInt();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading orders: " + e.getMessage());
                orders = new ArrayList<>();
                nextId = 1;
            }
        }
    }

    public void saveToFile() {
        File file = new File(DATA_FILE);
        if (file.getParentFile().mkdirs()) {

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(orders);
                oos.writeInt(nextId);
            } catch (IOException e) {
                System.err.println("Error saving orders: " + e.getMessage());
            }
        }
    }

    public Order create(Order order) {
        order.setId(nextId++);
        order.setOrderDate(LocalDateTime.now());
        order.calculateTotal();
        orders.add(order);
        saveToFile();
        return order;
    }

    public Optional<Order> findById(int id) {
        return orders.stream()
                .filter(order -> order.getId() == id)
                .findFirst();
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }

    public List<Order> findByUserId(int userId) {
        return orders.stream()
                .filter(order -> order.getUserId() == userId)
                .collect(Collectors.toList());
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orders.stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Order> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return orders.stream()
                .filter(order -> {
                    LocalDateTime orderDate = order.getOrderDate();
                    return !orderDate.isBefore(start) && !orderDate.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    public List<Order> findByDate(LocalDate date) {
        return orders.stream()
                .filter(order -> order.getOrderDate().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public void update(Order order) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId() == order.getId()) {
                order.calculateTotal();
                orders.set(i, order);
                saveToFile();
                return;
            }
        }
        throw new IllegalArgumentException("Order not found");
    }

    public void delete(int id) {
        orders.removeIf(order -> order.getId() == id);
        saveToFile();
    }

    public double getTotalSales() {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    public double getTotalSalesByDate(LocalDate date) {
        return orders.stream()
                .filter(o -> o.getOrderDate().toLocalDate().equals(date) &&
                        o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    public int getTotalOrderCount() {
        return orders.size();
    }
}