package com.bryan.programming2coursework.dao;

import com.bryan.programming2coursework.model.MenuItem;
import com.bryan.programming2coursework.model.MenuItem.MenuCategory;
import com.bryan.programming2coursework.util.DatabaseManager;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data Access Object for MenuItem persistence using SQLite
 */
public class MenuItemDAO {
    private static MenuItemDAO instance;

    private MenuItemDAO() {
        // initialized in DatabaseManager
    }

    public static synchronized MenuItemDAO getInstance() {
        if (instance == null) {
            instance = new MenuItemDAO();
        }
        return instance;
    }

    public enum SortType {
        PRICE("price"),
        STOCK("stock"),
        CALORIES("calories");

        private final String column;

        SortType(String column) {
            this.column = column;
        }

    }

    /**
     *
     * @return a map of <menu_item_id, total sold>
     */
    public Map<Integer, Integer> getSalesStats() {
        Map<Integer, Integer> stats = new HashMap<>();
        String sql = """
                    SELECT menu_item_id, SUM(quantity) as total_sold
                    FROM order_items
                    GROUP BY menu_item_id
                """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getInt("menu_item_id"), rs.getInt("total_sold"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Unified search method handling filtering, category, and sorting.
     */
    public List<MenuItem> getItems(MenuCategory category, String searchQuery, SortType sortType, boolean ascending) {
        List<MenuItem> items = new ArrayList<>();

        // allows us to append filters in any order without the need for complicated string logic
        StringBuilder sql = new StringBuilder("SELECT * FROM menu_items WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // category filter is only used when it's not ALL_CATEGORIES
        if (category != null && category != MenuCategory.ALL_CATEGORIES) {
            sql.append(" AND category = ?");
            params.add(category.name());
        }

        // search filter only used if query is present
        // edit: probably not needed now since I've decided to do search filtering in Java but we will keep it
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ?)");
            String likePattern = "%" + searchQuery.trim() + "%";
            params.add(likePattern);
            params.add(likePattern);
        }

        // sorting
        if (sortType != null) {
            // hardcoded to prevent sql injection
            sql.append(" ORDER BY ").append(sortType.column).append(" ").append(ascending ? "ASC" : "DESC");
        } else {
            sql.append(" ORDER BY id ASC"); // default
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // inject
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToMenuItem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Query Error: " + e.getMessage());
        }
        return items;
    }

    /**
     * Map database result to MenuItem object
     */
    private MenuItem mapResultSetToMenuItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        int stock = rs.getInt("stock");
        String description = rs.getString("description");
        String imagePath = rs.getString("image_path");
        int calories = rs.getInt("calories");
        // double protein = rs.getDouble("protein"); // removed

        MenuItem.MenuCategory category;
        String categoryStr = rs.getString("category");
        try {
            category = MenuItem.MenuCategory.valueOf(categoryStr);
        } catch (IllegalArgumentException | NullPointerException e) { // fallback
            category = MenuItem.MenuCategory.ALL_CATEGORIES;
        }

        return new MenuItem(
                calories,
                category,
                description,
                id,
                imagePath,
                name,
                price,
                stock
        );
    }


    /**
     * Create a new menu item
     */
    public void create(MenuItem item) {
        String sql = "INSERT INTO menu_items(name, category, price, stock, description, image_path, calories) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, item.getName());
            statement.setString(2, item.getCategory().name()); // Store MENU_CATEGORY as String
            statement.setDouble(3, item.getPrice());
            statement.setInt(4, item.getStockQuantity());
            statement.setString(5, item.getDescription());
            statement.setString(6, item.getImageUrl() != null ? item.getImageUrl() : null);
            statement.setInt(7, item.getCalories());

            statement.executeUpdate();

            // get auto-generated ids and set the java object to it
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                item.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Error creating menu item: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void update(MenuItem originalItem, MenuItem updatedItem) {
        String sql = "UPDATE menu_items SET name = ?, category = ?, price = ?, stock = ?, description = ?, image_path = ?, calories = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, updatedItem.getName());
            statement.setString(2, updatedItem.getCategory().name()); // store as string
            statement.setDouble(3, updatedItem.getPrice());
            statement.setInt(4, updatedItem.getStockQuantity());
            statement.setString(5, updatedItem.getDescription());
            statement.setString(6, updatedItem.getImageUrl() != null ? updatedItem.getImageUrl() : null);
            statement.setInt(7, updatedItem.getCalories());
            statement.setInt(8, originalItem.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating menu item failed, no rows affected.");
            }

            // update original id in java to match
            updatedItem.setId(originalItem.getId());
        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(MenuItem item) {
        update(item, item);
    }

    public void delete(MenuItem menuItem) {
        String sql = "DELETE FROM menu_items WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, menuItem.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all categories (excluding ALL_CATEGORIES)
     */
    public List<MenuCategory> getAllCategories() {
        return Arrays.stream(MenuCategory.values())
                .filter(cat -> cat != MenuCategory.ALL_CATEGORIES)
                .collect(Collectors.toList());
    }


}