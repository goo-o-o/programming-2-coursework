package com.bryan.programming2coursework.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * MenuItem model representing food/drink items in the menu
 */
public class MenuItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String name;
    private String category;
    private double price;
    private int stockQuantity;
    private String description;
    private String imagePath;
    
    public MenuItem() {
    }
    
    public MenuItem(int id, String name, String category, double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
    
    public MenuItem(int id, String name, String category, double price, int stockQuantity, 
                   String description, String imagePath) {
        this(id, name, category, price, stockQuantity);
        this.description = description;
        this.imagePath = imagePath;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
    
    @Override
    public String toString() {
        return "MenuItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stockQuantity +
                '}';
    }
}