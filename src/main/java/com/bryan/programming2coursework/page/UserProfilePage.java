package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.OrderDAO;
import com.bryan.programming2coursework.dao.UserDAO;
import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.User;
import com.bryan.programming2coursework.util.Constants;
import com.bryan.programming2coursework.util.SessionManager;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.List;

public class UserProfilePage extends HBox {

    private final VBox contentArea;
    private final User currentUser;

    public UserProfilePage() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        this.setPadding(new Insets(40));
        this.setSpacing(30);
        this.setStyle("-fx-background-color: " + Constants.MCD_RED_HEX);

        VBox sidebar = createSidebar();

        contentArea = new VBox();
        contentArea.setMinWidth(500);
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        this.getChildren().addAll(sidebar, contentArea);

        // default to profile view
        showProfileSection();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(280);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        sidebar.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.2)));

        Label name = new Label(currentUser.getUsername());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Button btnProfile = createSidebarButton("My Profile", Utils.getSVG(Constants.USER));
        Button btnSettings = createSidebarButton("Settings", Utils.getSVG(Constants.SETTINGS));
        Button btnLogout = createSidebarButton("Log Out", Utils.getSVG(Constants.LOGOUT));
        Button btnOrders = null;
        if (!currentUser.isAdmin()) {
            btnOrders = createSidebarButton("Orders", Utils.getSVG(Constants.CART));
            btnOrders.setOnAction(e -> showOrdersSection());
        }

        btnProfile.setOnAction(e -> showProfileSection());
        btnSettings.setOnAction(e -> showSettingsSection());
        btnLogout.setOnAction(e -> SessionManager.getInstance().logout());

        Button exitBtn = createSidebarButton("Go Back", Utils.getSVG(Constants.BACK));
        exitBtn.setOnAction(e -> ViewSwitcher.switchTo(currentUser.getRole() == User.UserRole.ADMIN ? new AdminDashboardPage() : new CustomerDashboardPage()));

        sidebar.getChildren().addAll(name, new Separator(), btnProfile);
        if (!currentUser.isAdmin())
            sidebar.getChildren().add(btnOrders);
        sidebar.getChildren().addAll(btnSettings, btnLogout, Utils.getVerticalSpacer(), exitBtn);
        return sidebar;
    }

    private Button createSidebarButton(String text, SVGPath icon) {
        Button btn = new Button(text, icon);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 12; -fx-font-size: 14px; -fx-cursor: hand;");
        return btn;
    }

    private Label usernameError, emailError, phoneError, passwordError;

    private void showProfileSection() {
        contentArea.getChildren().clear();

        usernameError = getErrorLabel();
        emailError = getErrorLabel();
        phoneError = getErrorLabel();
        passwordError = getErrorLabel();

        VBox profileCard = new VBox(25);
        profileCard.setPadding(new Insets(40));
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 25;");
        profileCard.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.15)));

        // user profile
        VBox fieldsContainer = new VBox(15);
        Label userProfile = new Label("User Profile");
        userProfile.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField nameField = new TextField(currentUser.getUsername());
        String FIELD_STYLE = "-fx-background-color: transparent; -fx-border-color: #eee; -fx-border-width: 0 0 1 0; -fx-padding: 5 0;";
        nameField.setStyle(FIELD_STYLE);
        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setStyle(FIELD_STYLE);
        TextField phoneField = new TextField(currentUser.getPhone());
        phoneField.setStyle(FIELD_STYLE);

        Button saveGeneralBtn = new Button("Save Changes");
        saveGeneralBtn.setMaxWidth(Double.MAX_VALUE);
        saveGeneralBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 10;");

        saveGeneralBtn.setOnAction(e -> handleUpdateProfile(nameField.getText(), emailField.getText(), phoneField.getText()));

        fieldsContainer.getChildren().addAll(
                userProfile,
                new Label("Username"), usernameError, nameField,
                new Label("Email"), emailError, emailField,
                new Label("Phone"), phoneError, phoneField,
                saveGeneralBtn,
                new Separator()
        );

        // change password
        VBox passwordContainer = new VBox(10);
        Label passTitle = new Label("Security");
        passTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        PasswordField curPf = new PasswordField();
        curPf.setPromptText("Original Password");
        PasswordField newPf = new PasswordField();
        newPf.setPromptText("New Password");
        PasswordField confPf = new PasswordField();
        confPf.setPromptText("Confirm New Password");
        curPf.setStyle(FIELD_STYLE);
        newPf.setStyle(FIELD_STYLE);
        confPf.setStyle(FIELD_STYLE);

        Button changePassBtn = new Button("Change Password");
        changePassBtn.setMaxWidth(Double.MAX_VALUE);
        changePassBtn.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10;");

        changePassBtn.setOnAction(e -> handlePasswordChange(curPf, newPf, confPf, changePassBtn));

        passwordContainer.getChildren().addAll(passTitle, passwordError, curPf, newPf, confPf, changePassBtn);

        profileCard.getChildren().addAll(fieldsContainer, passwordContainer);
        contentArea.getChildren().add(profileCard);
    }

    private void handleUpdateProfile(String username, String email, String phone) {
        clearErrors();
        if (username.isEmpty()) {
            showError(usernameError, "Username cannot be empty");
            return;
        }
        if (!Utils.isValidEmail(email)) {
            showError(emailError, "Invalid email format");
            return;
        }
        if (!Utils.isValidPhone(phone)) {
            showError(phoneError, "Invalid phone number");
            return;
        }

        // update user stuff
        currentUser.setUsername(username);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);
        UserDAO.getInstance().updateProfile(currentUser);
        Utils.showInfo("Success", "Profile updated successfully");
    }

    private void handlePasswordChange(PasswordField curPf, PasswordField newPf, PasswordField confPf, Button btn) {
        clearErrors();
        String currentInput = curPf.getText();
        String newPass = newPf.getText();
        String confPass = confPf.getText();

        if (!currentInput.equals(currentUser.getPassword())) {
            showError(passwordError, "Incorrect original password");
        } else if (!Utils.isValidPassword(newPass)) {
            showError(passwordError, "Invalid new password (min 6 chars, alphanumeric only)");
        } else if (!newPass.equals(confPass)) {
            showError(passwordError, "Passwords do not match");
        } else {
            // success
            currentUser.setPassword(newPass);
            UserDAO.getInstance().updatePassword(currentUser.getId(), newPass);
            Utils.showInfo("Success", "Password updated successfully");
            curPf.clear();
            newPf.clear();
            confPf.clear();
        }
    }

    private static Label getErrorLabel() {
        Label error = new Label();
        error.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        error.setManaged(false);
        error.setVisible(false);
        return error;
    }

    private void showError(Label errorLabel, String text) {
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
        errorLabel.setText(text);
    }

    private void clearErrors() {
        Label[] labels = {usernameError, emailError, phoneError, passwordError};
        for (Label l : labels) {
            if (l != null) {
                l.setVisible(false);
                l.setManaged(false);
            }
        }
    }

    private void showSettingsSection() {
        contentArea.getChildren().clear();
        VBox settingsCard = new VBox(20);
        settingsCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 30;");
        settingsCard.getChildren().add(new Label("Settings Section coming soon..."));
        contentArea.getChildren().add(settingsCard);
    }

    private void showOrdersSection() {
        contentArea.getChildren().clear();

        VBox ordersContainer = new VBox(20);
        ordersContainer.setPadding(new Insets(10));

        Label title = new Label("My Orders");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        contentArea.getChildren().add(title);

        // fetch from database through DAO
        List<Order> userOrders = OrderDAO.getInstance().findByUserId(currentUser.getId());

        if (userOrders.isEmpty()) {
            Label noOrders = new Label("You haven't placed any orders yet.");
            noOrders.setStyle("-fx-text-fill: #eee; -fx-font-style: italic;");
            ordersContainer.getChildren().add(noOrders);
        } else {
            for (Order order : userOrders) {
                ordersContainer.getChildren().add(createOrderCard(order));
            }
        }

        ScrollPane scrollPane = new ScrollPane(ordersContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        contentArea.getChildren().add(scrollPane);
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label orderId = new Label("Order #" + order.getId());
        orderId.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");

        // container for order items
        VBox itemsContainer = new VBox(5);
        if (order.getItems() == null || order.getItems().isEmpty()) {
            Label empty = new Label("No items found");
            empty.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            itemsContainer.getChildren().add(empty);
        } else {
            order.getItems().forEach(orderItem -> {
                Label label = new Label(String.format("• %s x %d | $%.2f",
                        orderItem.getMenuItem().getName(),
                        orderItem.getQuantity(),
                        orderItem.getSubtotal()));
                label.setStyle("-fx-text-fill: #555;");
                itemsContainer.getChildren().add(label);
            });
        }

        Label statusLabel = new Label(order.getStatus().toString());
        statusLabel.setStyle("-fx-padding: 5 10; -fx-background-radius: 10; -fx-font-weight: bold; " +
                getStatusColorStyle(order.getStatus()));

        header.getChildren().addAll(orderId, Utils.getHorizontalSpacer(), statusLabel);

        Label details = new Label(String.format("Date: %s\nTotal: $%.2f",
                order.getFormattedOrderDate(), order.getTotalPrice()));
        details.setStyle("-fx-text-fill: #666; -fx-padding: 5 0;");

        Button cancelBtn = new Button("Cancel Order");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            cancelBtn.setDisable(true);
            cancelBtn.setOpacity(0.5);
            cancelBtn.setText("Cannot Cancel");
            cancelBtn.setStyle("-fx-background-color: #9ca3af; -fx-text-fill: white; -fx-background-radius: 8;");
        }

        cancelBtn.setOnAction(e -> {
            boolean confirm = Utils.showConfirmation("Cancel Order", "Are you sure you want to cancel Order #" + order.getId() + "?");
            if (confirm) {
                OrderDAO.getInstance().updateStatus(order.getId(), Order.OrderStatus.CANCELLED);
                showOrdersSection();
            }
        });

        card.getChildren().addAll(header, itemsContainer, details, cancelBtn);
        return card;
    }

    private String getStatusColorStyle(Order.OrderStatus status) {
        return switch (status) {
            case PENDING -> "-fx-background-color: orange;";
            case PREPARING -> "-fx-background-color: blue;";
            case READY -> "-fx-background-color: purple;";
            case COMPLETED -> "-fx-background-color: green;";
            case CANCELLED -> "-fx-background-color: red;";
        };
    }
}