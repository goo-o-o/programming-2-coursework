package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.UserDAO;
import com.bryan.programming2coursework.model.User.UserRole;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 * Registration page for new users
 */
public class RegisterPage extends LoginPage {
    Label usernameError;
    Label passwordError;
    Label confirmPasswordError;
    Label emailError;
    Label phoneError;

    public RegisterPage() {
        super();
    }

    @Override
    protected VBox getRightBox() {
        // initialize label
        usernameError = getErrorLabel();
        passwordError = getErrorLabel();
        confirmPasswordError = getErrorLabel();
        emailError = getErrorLabel();
        phoneError = getErrorLabel();


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
        Utils.filterSpace(username);

        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password (min 6 characters)");
        Utils.filterSpace(password);

        TextField passwordText = new TextField();
        passwordText.setPromptText("Password (min 6 characters)");

        StackPane passwordRow = Utils.createPasswordFieldWithToggle(password, passwordText);

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");
        TextField confirmPasswordText = new TextField();
        confirmPasswordText.setPromptText("Confirm Password");

        StackPane confirmPasswordRow = Utils.createPasswordFieldWithToggle(confirmPassword, confirmPasswordText);


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
                phone.getText(),
                toggleButton.isSelected()
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
        registerFields.getChildren().addAll(usernameError, username, passwordError, passwordRow, confirmPasswordError, confirmPasswordRow, emailError, email, phoneError, phone, toggleButton, registerBtn, loginPrompt);

        rightBox.getChildren().addAll(hello, signUp, registerFields);
        return rightBox;
    }

    private static Label getErrorLabel() {
        Label error = new Label();
        error.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        error.setManaged(false);
        error.setVisible(false);
        return error;
    }

    private static void showError(Label errorLabel, String text) {
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
        errorLabel.setText(text);
    }

    private void clearErrors() {
        Label[] errorLabels = new Label[]{usernameError, passwordError, confirmPasswordError, emailError, phoneError};
        for (Label label : errorLabels) {
            label.setVisible(false);
            label.setManaged(false);
        }
    }

    /**
     * Handle user registration
     */
    private void handleRegister(String username, String password, String confirmPassword,
                                String email, String phone, boolean isAdmin) {
        try {
            clearErrors();
            if (username == null || username.trim().isEmpty()) {
                showError(usernameError, "Please enter username");
//                Utils.showError("Registration Error", "Please enter username");
                return;
            } else if (!Utils.isValidUsername(username)) {
                showError(usernameError, "Username must be 3-20 characters long and not contain any invalid characters");
                return;
            }

            if (!Utils.isValidPassword(password)) {
                showError(passwordError, "Password must be at least 6 characters and not contain any invalid characters");
//                Utils.showError("Registration Error", "Password must be at least 6 characters");
                return;
            } else if (!password.equals(confirmPassword)) {
                showError(confirmPasswordError, "Passwords do not match");
//                Utils.showError("Registration Error", "Passwords do not match");
                return;
            }
            if (email == null || email.trim().isEmpty()) {
                showError(emailError, "Please enter email");
                return;
            } else if (!Utils.isValidEmail(email)) {
                showError(emailError, "Invalid email format");
//                Utils.showError("Registration Error", "Invalid email format");
                return;
            }
            if (phone == null || phone.trim().isEmpty()) {
                showError(phoneError, "Please enter phone number");
                return;
            } else if (!Utils.isValidPhone(phone)) {
                showError(phoneError, "Invalid phone number format");
//                Utils.showError("Registration Error", "Invalid phone number format (e.g., 0123456789)");
                return;
            }

            // check if username already exists
            UserDAO userDAO = UserDAO.getInstance();
            if (userDAO.usernameExists(username.trim())) {
                Utils.showError("Registration Error", "User already exists");
                return;
            }

            // create new user
            com.bryan.programming2coursework.model.User newUser = new com.bryan.programming2coursework.model.User();
            newUser.setUsername(username.trim());
            newUser.setPassword(password);
            newUser.setEmail(email.trim()); // made not nullable
            newUser.setPhone(phone.trim()); // made not nullable
            newUser.setRole(isAdmin ? UserRole.ADMIN : UserRole.CUSTOMER);

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