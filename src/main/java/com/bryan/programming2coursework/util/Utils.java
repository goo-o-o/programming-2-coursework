package com.bryan.programming2coursework.util;

import com.bryan.programming2coursework.page.LoginPage;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 * Utility class containing helper methods and constants
 */
public class Utils {
    public static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;

        String[] words = str.split("\\s+"); // match one or more whitespace
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }


    /**
     * Unified the button theming code for reusability
     */
    public static void updateButtonStateDesign(Button button, boolean active) {
        if (active) {
            button.setStyle("-fx-background-color: " + Constants.MCD_RED_HEX + ";-fx-background-radius: 15; -fx-padding: 15; -fx-cursor: hand; -fx-font-weight: 800");
            button.setEffect(new DropShadow(5, Constants.MCD_RED));
            button.setTextFill(Color.WHITE);
        } else {
            button.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-cursor: hand;");
            button.setEffect(null);
            button.setTextFill(Color.BLACK);
        }
    }


    /**
     * Create an SVG path from string data, also automatically appends a global style class
     */
    public static SVGPath getSVG(String data) {
        SVGPath path = new SVGPath();
        path.setContent(data);
        path.setStrokeWidth(1);
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

    // moved these into helper methods, since they might be reused in future
    public static Region getHorizontalSpacer() {
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        return hSpacer;
    }

    public static Region getVerticalSpacer() {
        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);
        return vSpacer;
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

    public static boolean isValidUsername(String username) {
        // alphanumeric + symbols
        return username != null && username.length() > 3 && username.length() < 30 && username.matches("^[\\x21-\\x7E]+$");
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        // alphanumeric + symbols
        return password != null &&
                password.length() >= 3 &&
                password.matches("^[\\x21-\\x7E]+$");
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

    public static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        // default JavaFX alert is so ugly
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.setStyle(
                "-fx-background-color: white; -fx-padding: 10px; -fx-border-color: white; -fx-border-width: 1px; -fx-border-radius: 15px; -fx-background-radius: 15px;"
        );

        dialogPane.lookup(".content.label").setStyle(
                "-fx-font-size: 14px; -fx-text-fill: black; -fx-font-weight: normal;"
        );

        // buttons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle(
                    "-fx-background-color: #ffbc0d; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-cursor: hand; -fx-padding: 8px 20px;"
            );
        }

        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    public static void showInfo(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static StackPane createPasswordFieldWithToggle(PasswordField pf, TextField tf) {
        SVGPath eyeOpen = getSVG(Constants.EYE_OPEN);
        SVGPath eyeClosed = getSVG(Constants.EYE_CLOSED);
        eyeOpen.setStrokeWidth(0);
        eyeClosed.setStrokeWidth(0);
        eyeOpen.setFill(Paint.valueOf("gray"));
        eyeClosed.setFill(Paint.valueOf("gray"));

        Button toggleBtn = new Button();
        toggleBtn.setGraphic(eyeClosed);
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0 10 0 0;");
        toggleBtn.setMaxHeight(Double.MAX_VALUE); // for easier clicking


        // sync the tf and pf fields
        tf.textProperty().bindBidirectional(pf.textProperty());
        tf.setVisible(false);
        tf.setManaged(false);

        toggleBtn.setOnAction(e -> {
            boolean isCurrentlyShown = tf.isVisible();
            tf.setVisible(!isCurrentlyShown);
            tf.setManaged(!isCurrentlyShown);
            pf.setVisible(isCurrentlyShown);
            pf.setManaged(isCurrentlyShown);

            // update icon
            toggleBtn.setGraphic(!isCurrentlyShown ? eyeOpen : eyeClosed);
        });

        pf.setStyle("-fx-padding: 0 40 0 15;");
        tf.setStyle("-fx-padding: 0 40 0 15;");

        StackPane stack = new StackPane(pf, tf, toggleBtn);
        StackPane.setAlignment(toggleBtn, Pos.CENTER_RIGHT); // push to right

        return stack;
    }

    /**
     * It's possible to modify URL dimensions from a scene7 server. This is mainly used to hasten loading of images since I don't want to download the files locally, hopefully nothing goes wrong during presentation
     */
    // hopefully it works during the presentation
    public static String modifyUrlDimensions(String imageUrl, int width, int height) {
        if (imageUrl == null || imageUrl.isEmpty()) return null;

        if (imageUrl.contains("scene7.com")) {
            // only substring if the parameters are present
            String base = imageUrl.contains("?") ? imageUrl.substring(0, imageUrl.indexOf("?")) : imageUrl;
            return base + "?wid=" + width + "&hei=" + height + "&fmt=png-alpha&qlt=80";
        }
        return imageUrl;
    }

    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


}