package com.bryan.programming2coursework.util;

import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.User;
import com.bryan.programming2coursework.page.LoginPage;

/**
 * Manages the current logged-in user session
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Order currentOrder;

    private SessionManager() {
        this.currentOrder = new Order();
    }

    public Order getCurrentOrder() {
        if (currentOrder == null) {
            currentOrder = new Order(0, getCurrentUserId()); // dumb!!! forgot to create with the correct user id
        }
        return currentOrder;
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        // lazily instantiate
        return instance;
    }
    
    public void login(User user) {
        this.currentUser = user;
        if (this.currentOrder != null) {
            this.currentOrder.setUserId(user.getId());
        }
    }

    public void logout() {
        if (Utils.showConfirmation("Logout", "Are you sure you want to logout?")) {
            ViewSwitcher.switchTo(new LoginPage());
            this.currentUser = null;
            this.currentOrder = null;
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
    }
}