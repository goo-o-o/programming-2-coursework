package com.bryan.programming2coursework.dao;

import com.bryan.programming2coursework.model.MenuItem;
import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.Order.OrderStatus;
import com.bryan.programming2coursework.model.OrderItem;
import com.bryan.programming2coursework.model.User;
import com.bryan.programming2coursework.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;

public class OrderDAO {
    private static OrderDAO instance;

    private OrderDAO() {
    }

    public static synchronized OrderDAO getInstance() {
        if (instance == null) {
            instance = new OrderDAO();
        }
        return instance;
    }

    public Order create(Order order) {
        String orderSql = "INSERT INTO orders (user_id, order_date, status) VALUES (?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // queue the statements

            // insert order
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, order.getUserId());
                pstmt.setString(2, LocalDateTime.now().toString());
                String status = (order.getStatus() != null) ? order.getStatus().toString() : "PENDING";
                pstmt.setString(3, status);

                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    order.setId(rs.getInt(1));
                }
            }

            // insert order items
            try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                for (OrderItem item : order.getItems()) {
                    pstmt.setInt(1, order.getId());
                    pstmt.setInt(2, item.getMenuItem().getId());
                    pstmt.setInt(3, item.getQuantity());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit(); // save all changes
            return order;
        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Error creating order: " + e.getMessage());
        }
    }

    public void updateStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // populate the order item list as well
                order.setItems(fetchOrderItems(order.getId()));
                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        LocalDateTime dateTime = rs.getObject("order_date", LocalDateTime.class);

        OrderStatus status;
        try {
            status = OrderStatus.valueOf(rs.getString("status"));
        } catch (IllegalArgumentException | NullPointerException e) {
            status = OrderStatus.PENDING;
        }

        Order order = new Order(id, userId);
        order.setOrderDate(dateTime);
        order.setStatus(status);

        return order;
    }

    private MenuItem mapResultSetToMenuItem(ResultSet rs) throws SQLException {
        return new MenuItem(
                rs.getInt("calories"),
                MenuItem.MenuCategory.valueOf(rs.getString("category")),
                rs.getString("description"),
                rs.getInt("id"),
                rs.getString("image_path"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getInt("stock")
        );
    }


    /**
     * @param userId the id of the user
     * @return a list of orders made by this user
     */
    public List<Order> findByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY id DESC"; //

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setOrderDate(LocalDateTime.parse(rs.getString("order_date")));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));

                // fetch items for this order
                order.setItems(fetchOrderItems(order.getId()));

                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private List<OrderItem> fetchOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        // get all the necessary columns for the 8 param constructor
        String sql = "SELECT oi.quantity, mi. * FROM order_items oi JOIN menu_items mi ON oi.menu_item_id = mi.id WHERE oi.order_id = ? ";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                items.add(new OrderItem(mapResultSetToMenuItem(rs), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public double getTotalSales() {
        // select all completed orders and get the sum of quantity * price
        String sql = """
                SELECT SUM(oi.quantity * mi.price)
                FROM order_items oi
                JOIN menu_items mi ON oi.menu_item_id = mi.id
                JOIN orders o ON oi.order_id = o.id
                WHERE o.status = 'COMPLETED'
                """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    public Map<String, Integer> getDailyOrderVolume() {
        Map<String, Integer> data = new LinkedHashMap<>();
        // gets order counts per day for the last week
        String sql = """
                    SELECT date(order_date) as day, COUNT(*) as count
                    FROM orders
                    WHERE status = 'COMPLETED'
                    AND order_date >= date('now', '-7 days')
                    GROUP BY day
                    ORDER BY day
                """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("day"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public int getTotalItemsSold() {
        String sql = "SELECT SUM(quantity) FROM order_items oi JOIN orders o ON oi.order_id = o.id WHERE o.status = 'COMPLETED'";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalOrdersCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public Map<String, Integer> getTopSellingItems(int limit) {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT mi.name, SUM(oi.quantity) as total FROM order_items oi " +
                "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE o.status = 'COMPLETED' GROUP BY mi.id ORDER BY total DESC LIMIT " + limit;
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) data.put(rs.getString("name"), rs.getInt("total"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}