package com.bryan.programming2coursework.util;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

/**
 * Utility class containing helper methods and constants
 */
public class Utils {
    public static class Colors {
        public static Color MCD_YELLOW = Color.web("#FFC300");
    }

    public static String LOGO = "m195.8 17.933c23.3 0 42.2 98.3 42.2 219.7h34c0-130.7-34.3-236.5-76.3-236.5-24 0-45.2 31.7-59.2 81.5-14-49.8-35.2-81.5-59-81.5-42 0-76.2 105.7-76.2 236.4h34c0-121.4 18.7-219.6 42-219.6s42.2 90.8 42.2 202.8h33.8c0-112 19-202.8 42.3-202.8";
    public static String MINIMIZE = "M0,5 L10,5";
    public static String MAXIMIZE = "M2,2 H8 V8 H2 Z";
    public static String UNMAXIMIZE = "M3,4 H7 V8 H3 Z M5,2 H9 V6 H7";
    public static String CLOSE = "M2,2 L8,8 M8,2 L2,8";

    /**
     * Create an SVG path from string data, also automatically appends a global style class
     */
    public static SVGPath getSVG(String data) {
        SVGPath path = new SVGPath();
        path.setContent(data);
        path.getStyleClass().add("svg-icon");

        return path;
    }
    
    /**
     * Create a rounded rectangle clip for the window
     */
    public static Rectangle getClip(Pane root) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        return clip;
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // starts with 01, 10-11 digits
        String phoneRegex = "^01[0-9]{8,9}$";
        return phone.replaceAll("[\\s-]", "").matches(phoneRegex);
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Format currency in Malaysian Ringgit
     */
    public static String formatCurrency(double amount) {
        return String.format("RM %.2f", amount);
    }

    public static boolean isPositiveNumber(String text) {
        try {
            double value = Double.parseDouble(text);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveInteger(String text) {
        try {
            int value = Integer.parseInt(text);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Show alert dialog helper
     */
    public static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show error alert
     */
    public static void showError(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }
    
    /**
     * Show info alert
     */
    public static void showInfo(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }
    
    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}