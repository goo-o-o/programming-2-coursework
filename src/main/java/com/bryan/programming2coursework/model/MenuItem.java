package com.bryan.programming2coursework.model;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;

/**
 * MenuItem model representing food/drink items in the menu
 */
public class MenuItem implements Serializable {

    private int id;
    private String name;
    private MenuCategory category;
    private double price;
    private int stockQuantity;
    private int calories;
    private String description;
    private String imageUrl;


    /**
     * Menu categories enum with display names
     */
    public enum MenuCategory {
        // we'll probably need to attach a String representation because of some symbols
        // ok here we go, time to add almost every single McDonald's item from their website
        ALL_CATEGORIES("All Categories", null),
        BREAKFAST("Breakfast", ""),
        BURGERS("Burgers", "https://img.icons8.com/color/96/hamburger.png"),
        CHICKEN_AND_FISH_SANDWICHES("Chicken & Fish Sandwiches", "https://img.icons8.com/emoji/96/sandwich-emoji.png"),
        MC_NUGGETS_AND_MC_CRISPY_STRIPS("McNuggets® & McCrispy® Strips", "https://cdn-icons-png.freepik.com/512/1365/1365553.png"),
        SNACK_WRAP("Snack Wrap®", "https://img.icons8.com/color/96/wrap.png"),
        FRIES_AND_SIDES("Fries & Sides", "https://img.icons8.com/fluency/96/mcdonalds-french-fries.png"),
        HAPPY_MEAL("Happy Meal®", "https://cdn-icons-png.flaticon.com/512/13796/13796480.png"),
        SWEET_AND_TREATS("Sweets & Treats", "https://img.icons8.com/fluency/96/ice-cream-cone.png"),
        MC_CAFE_COFFEES("McCafé® Coffees", "https://www.riyadh.mcdelivery.com.sa/m/sa/riy/static/1773249251953/assets/9661/categories/alt/cat13_1_ar.jpg"),
        BEVERAGES("Beverages", "https://cdn-icons-png.flaticon.com/512/2719/2719313.png"),
        SAUCES_AND_CONDIMENTS("Sauces & Condiments", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQsv2s3WGWhoEbDgu2E2dxr1phpfjs5-vNRUw&s");

        // encapsulation just makes things harder in this case
        public final String display, iconURL;

        MenuCategory(String display, String iconURL) {
            this.display = display;
            this.iconURL = iconURL;
        }

//        @Override
//        public String toString() {
//            return display;
//        }
    }

    public MenuItem(int calories, MenuCategory category, String description, int id, String imageUrl, String name, double price, int stockQuantity) {
        this.calories = calories;
        this.category = category;
        this.description = description;
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public MenuCategory getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(MenuCategory category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String path) {
        if (path != null && !path.isEmpty()) {
            this.imageUrl = path;
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCalories() {
        return calories;
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= stockQuantity) {
            this.stockQuantity -= quantity;
        } else {
            throw new IllegalArgumentException("Not enough stock available");
        }
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return id == menuItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}