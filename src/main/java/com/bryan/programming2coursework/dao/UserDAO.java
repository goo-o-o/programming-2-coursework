package com.bryan.programming2coursework.dao;

import com.bryan.programming2coursework.model.User;
import com.bryan.programming2coursework.model.User.UserRole;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User persistence using binary file storage
 */
public class UserDAO {
    private static final String DATA_FILE = "data/users.dat";
    private static UserDAO instance;
    private List<User> users;
    private int nextId;
    
    private UserDAO() {
        users = new ArrayList<>();
        loadFromFile();
        
        // Create default admin if no users exist
        if (users.isEmpty()) {
            createDefaultUsers();
        }
    }
    
    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }
    
    private void createDefaultUsers() {
        // Default admin account
        User admin = new User(nextId++, "admin", "admin123", UserRole.ADMIN, 
                            "admin@mcronalds.com", "0123456789");
        users.add(admin);
        
        // Default customer account for testing
        User customer = new User(nextId++, "customer", "customer123", UserRole.CUSTOMER,
                               "customer@example.com", "0987654321");
        users.add(customer);
        
        saveToFile();
    }
    
    /**
     * Load users from binary file
     */
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(DATA_FILE);
        
        // Create data directory if it doesn't exist
        file.getParentFile().mkdirs();
        
        if (!file.exists()) {
            nextId = 1;
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (List<User>) ois.readObject();
            nextId = ois.readInt();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage());
            users = new ArrayList<>();
            nextId = 1;
        }
    }
    
    /**
     * Save users to binary file
     */
    public void saveToFile() {
        File file = new File(DATA_FILE);
        file.getParentFile().mkdirs();
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(users);
            oos.writeInt(nextId);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user
     */
    public Optional<User> authenticate(String username, String password) {
        return users.stream()
                   .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                   .findFirst();
    }
    
    /**
     * Create new user
     */
    public User create(User user) {
        // Check if username already exists
        if (findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        user.setId(nextId++);
        users.add(user);
        saveToFile();
        return user;
    }
    
    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return users.stream()
                   .filter(u -> u.getUsername().equals(username))
                   .findFirst();
    }
    
    /**
     * Find user by ID
     */
    public Optional<User> findById(int id) {
        return users.stream()
                   .filter(u -> u.getId() == id)
                   .findFirst();
    }
    
    /**
     * Get all users
     */
    public List<User> findAll() {
        return new ArrayList<>(users);
    }
    
    /**
     * Update user
     */
    public void update(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                users.set(i, user);
                saveToFile();
                return;
            }
        }
        throw new IllegalArgumentException("User not found");
    }
    
    /**
     * Delete user
     */
    public void delete(int id) {
        users.removeIf(u -> u.getId() == id);
        saveToFile();
    }
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }
}