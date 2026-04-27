package com.bryan.programming2coursework.dao;

import com.bryan.programming2coursework.model.User;
import com.bryan.programming2coursework.util.DatabaseManager;

import java.sql.*;
import java.util.Optional;

/**
 * Data Access Object for User persistence using SQLite
 */
public class UserDAO {
    private static UserDAO instance;

    private UserDAO() {
        // Database initialized in DatabaseManager
    }

    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    /**
     * Authenticate user with username and password
     */
    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public String getNameById(int id) {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown User"; // default
    }

    /**
     * Create a new user
     */
    public void create(User user) {
        String sql = "INSERT INTO users(username, password, role, email, phone) VALUES(?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getRole().toString());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPhone());

            statement.executeUpdate();

            // get the auto-incremented ID
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            throw new IllegalArgumentException("Error creating user: " + e.getMessage());
        }
    }

    /**
     * Update general profile information for an existing user
     */
    public void updateProfile(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, phone = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setInt(4, user.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Updating user profile failed, no rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            throw new RuntimeException("Database error during profile update: " + e.getMessage());
        }
    }

    /**
     * Update only the user's password
     */
    public void updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, newPassword);
            statement.setInt(2, userId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Updating password failed, no rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            throw new RuntimeException("Database error during password update: " + e.getMessage());
        }
    }

    /**
     * Helper to convert database row to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        return user;
    }

    /**
     * Check if username already exists
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            System.err.println("Error checking username: " + exception.getMessage());
            exception.printStackTrace();
        }
        return false;
    }
}