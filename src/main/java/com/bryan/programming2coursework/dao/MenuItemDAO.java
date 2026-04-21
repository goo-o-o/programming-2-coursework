package com.bryan.programming2coursework.dao;

import com.bryan.programming2coursework.model.MenuItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data Access Object for MenuItem persistence
 */
public class MenuItemDAO {
    private static final String DATA_FILE = "data/menu_items.dat";
    private static MenuItemDAO instance;
    private List<MenuItem> menuItems;
    private int nextId;
    
    private MenuItemDAO() {
        menuItems = new ArrayList<>();
        loadFromFile();
        
        // Create default menu items if none exist
        if (menuItems.isEmpty()) {
            createDefaultMenuItems();
        }
    }
    
    public static synchronized MenuItemDAO getInstance() {
        if (instance == null) {
            instance = new MenuItemDAO();
        }
        return instance;
    }
    
    private void createDefaultMenuItems() {
        // Burgers
        menuItems.add(new MenuItem(nextId++, "Big Mac", "Burgers", 12.50, 50, 
            "Two all-beef patties, special sauce, lettuce, cheese", null));
        menuItems.add(new MenuItem(nextId++, "Cheeseburger", "Burgers", 8.00, 60,
            "Classic cheeseburger with pickles and onions", null));
        menuItems.add(new MenuItem(nextId++, "Quarter Pounder", "Burgers", 14.00, 40,
            "Quarter pound of beef with cheese", null));
        menuItems.add(new MenuItem(nextId++, "McChicken", "Burgers", 9.50, 55,
            "Crispy chicken fillet with mayo", null));
        
        // Sides
        menuItems.add(new MenuItem(nextId++, "French Fries (M)", "Sides", 5.50, 100,
            "Golden french fries", null));
        menuItems.add(new MenuItem(nextId++, "Chicken McNuggets (6pc)", "Sides", 10.00, 80,
            "Six piece chicken nuggets", null));
        menuItems.add(new MenuItem(nextId++, "Apple Pie", "Sides", 4.50, 70,
            "Hot apple pie", null));
        
        // Drinks
        menuItems.add(new MenuItem(nextId++, "Coca-Cola (M)", "Drinks", 4.50, 120,
            "Medium Coca-Cola", null));
        menuItems.add(new MenuItem(nextId++, "Sprite (M)", "Drinks", 4.50, 120,
            "Medium Sprite", null));
        menuItems.add(new MenuItem(nextId++, "Orange Juice", "Drinks", 6.00, 90,
            "Fresh orange juice", null));
        menuItems.add(new MenuItem(nextId++, "Coffee", "Drinks", 5.50, 100,
            "Hot coffee", null));
        
        // Breakfast
        menuItems.add(new MenuItem(nextId++, "Egg McMuffin", "Breakfast", 8.50, 45,
            "English muffin with egg and cheese", null));
        menuItems.add(new MenuItem(nextId++, "Pancakes", "Breakfast", 9.00, 50,
            "Stack of fluffy pancakes", null));
        
        saveToFile();
    }
    
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(DATA_FILE);
        file.getParentFile().mkdirs();
        
        if (!file.exists()) {
            nextId = 1;
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            menuItems = (List<MenuItem>) ois.readObject();
            nextId = ois.readInt();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading menu items: " + e.getMessage());
            menuItems = new ArrayList<>();
            nextId = 1;
        }
    }
    
    public void saveToFile() {
        File file = new File(DATA_FILE);
        file.getParentFile().mkdirs();
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(menuItems);
            oos.writeInt(nextId);
        } catch (IOException e) {
            System.err.println("Error saving menu items: " + e.getMessage());
        }
    }
    
    public MenuItem create(MenuItem item) {
        item.setId(nextId++);
        menuItems.add(item);
        saveToFile();
        return item;
    }
    
    public Optional<MenuItem> findById(int id) {
        return menuItems.stream()
                       .filter(item -> item.getId() == id)
                       .findFirst();
    }
    
    public List<MenuItem> findAll() {
        return new ArrayList<>(menuItems);
    }
    
    public List<MenuItem> findByCategory(String category) {
        return menuItems.stream()
                       .filter(item -> item.getCategory().equalsIgnoreCase(category))
                       .collect(Collectors.toList());
    }
    
    public List<String> getAllCategories() {
        return menuItems.stream()
                       .map(MenuItem::getCategory)
                       .distinct()
                       .sorted()
                       .collect(Collectors.toList());
    }
    
    public void update(MenuItem item) {
        for (int i = 0; i < menuItems.size(); i++) {
            if (menuItems.get(i).getId() == item.getId()) {
                menuItems.set(i, item);
                saveToFile();
                return;
            }
        }
        throw new IllegalArgumentException("Menu item not found");
    }
    
    public void delete(int id) {
        menuItems.removeIf(item -> item.getId() == id);
        saveToFile();
    }
    
    public List<MenuItem> searchByName(String query) {
        String lowerQuery = query.toLowerCase();
        return menuItems.stream()
                       .filter(item -> item.getName().toLowerCase().contains(lowerQuery))
                       .collect(Collectors.toList());
    }
}