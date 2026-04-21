package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.UserDAO;
import com.bryan.programming2coursework.model.User;
import com.bryan.programming2coursework.model.User.UserRole;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 * Registration page for new users
 */
public class RegisterPage extends LoginPage {

    public RegisterPage() {
        super();
    }

    @Override
    protected VBox getRightBox() {
        VBox rightBox = new VBox(20);
        rightBox.getStyleClass().add("right-pane");
        rightBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        rightBox.setMinWidth(0);
        rightBox.setPrefWidth(0);

        Label hello = new Label("Create Account");
        hello.setStyle("-fx-font-size: 32px; -fx-font-weight: 900");
        Label signUp = new Label("Sign up for a new account");
        signUp.setStyle("-fx-font-size: 18px");

        VBox registerFields = new VBox(10);

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password (min 6 characters)");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");

        TextField email = new TextField();
        email.setPromptText("Email");

        TextField phone = new TextField();
        phone.setPromptText("Phone Number (e.g., 0123456789)");

        CheckBox toggleButton = new CheckBox("Admin Account");


        registerFields.getStyleClass().add("login-fields");
        Button registerBtn = new Button("Sign Up");

        registerBtn.setOnAction(e -> handleRegister(
                username.getText(),
                password.getText(),
                confirmPassword.getText(),
                email.getText(),
                phone.getText()
        ));

        Text textPart = new Text("Already have an account? ");
        textPart.setStyle("-fx-fill: gray");
        Hyperlink loginLink = new Hyperlink("Login");
        loginLink.setOnAction(e -> ViewSwitcher.switchTo(new LoginPage()));

        TextFlow loginPrompt = new TextFlow(textPart, loginLink);
        loginPrompt.setTextAlignment(TextAlignment.CENTER);
        loginPrompt.setPadding(new Insets(20, 0, 0, 0));

        registerBtn.prefWidthProperty().bind(registerFields.widthProperty().multiply(0.8F));
        registerFields.maxWidthProperty().bind(rightBox.widthProperty().divide(2));
        registerFields.setAlignment(Pos.CENTER);
        registerFields.getChildren().addAll(username, password, confirmPassword, email, phone, toggleButton, registerBtn, loginPrompt);

        rightBox.getChildren().addAll(hello, signUp, registerFields);
        return rightBox;
    }

    /**
     * Handle user registration
     */
    private void handleRegister(String username, String password, String confirmPassword,
                                String email, String phone) {
        try {
            if (username == null || username.trim().isEmpty()) {
                Utils.showError("Registration Error", "Please enter username");
                return;
            }

            if (username.trim().length() < 3) {
                Utils.showError("Registration Error", "Username must be at least 3 characters");
                return;
            }

            if (!Utils.isValidPassword(password)) {
                Utils.showError("Registration Error", "Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                Utils.showError("Registration Error", "Passwords do not match");
                return;
            }

            if (email != null && !email.trim().isEmpty() && !Utils.isValidEmail(email)) {
                Utils.showError("Registration Error", "Invalid email format");
                return;
            }

            if (phone != null && !phone.trim().isEmpty() && !Utils.isValidPhone(phone)) {
                Utils.showError("Registration Error", "Invalid phone number format (e.g., 0123456789)");
                return;
            }

            // check if username already exists
            UserDAO userDAO = UserDAO.getInstance();
            if (userDAO.usernameExists(username.trim())) {
                Utils.showError("Registration Error", "Username already exists");
                return;
            }

            // create new user
            User newUser = new User();
            newUser.setUsername(username.trim());
            newUser.setPassword(password);
            newUser.setEmail(email != null ? email.trim() : "");
            newUser.setPhone(phone != null ? phone.trim() : "");
            newUser.setRole(UserRole.CUSTOMER);

            userDAO.create(newUser);

            Utils.showInfo("Registration Successful",
                    "Account created successfully! You can now login.");

            ViewSwitcher.switchTo(new LoginPage());

        } catch (Exception ex) {
            Utils.showError("Registration Error", "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}