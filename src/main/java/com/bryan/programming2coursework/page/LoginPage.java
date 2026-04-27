package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.UserDAO;
import com.bryan.programming2coursework.model.User;
import com.bryan.programming2coursework.util.Constants;
import com.bryan.programming2coursework.util.SessionManager;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.Optional;

/**
 * Login page for user authentication
 */
public class LoginPage extends HBox {

    public LoginPage() {
        this.setSpacing(0);
        this.setFillHeight(true);
        this.getChildren().addAll(getBanner(), getRightBox());
    }

    protected VBox getRightBox() {
        VBox rightBox = new VBox(20);
        rightBox.getStyleClass().add("right-pane");
        rightBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        rightBox.setMinWidth(0);
        rightBox.setPrefWidth(0);

        Label hello = new Label("Hello!");
        hello.setStyle("-fx-font-size: 32px; -fx-font-weight: 900");
        Label signIn = new Label("Sign in to your Account");
        signIn.setStyle("-fx-font-size: 18px");

        VBox loginFields = new VBox(10);
        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        TextField passwordText = new TextField();
        passwordText.setPromptText("Password");

        StackPane passwordRow = Utils.createPasswordFieldWithToggle(password, passwordText);

        loginFields.getStyleClass().add("login-fields");
        Button loginBtn = new Button("Login");

        // login button action
        loginBtn.setOnAction(e -> handleLogin(username.getText(), password.getText()));

        // let Enter key login, edit: now with both fields
        password.setOnAction(e -> handleLogin(username.getText(), password.getText()));
        passwordText.setOnAction(e -> handleLogin(username.getText(), password.getText()));
        username.setOnAction(e -> handleLogin(username.getText(), password.getText()));

        Text textPart = new Text("Don't have an account? ");
        textPart.setStyle("-fx-fill: gray");
        Hyperlink signUpLink = new Hyperlink("Sign Up");
        signUpLink.setOnAction(e -> ViewSwitcher.switchTo(new RegisterPage()));

        TextFlow signUpPrompt = new TextFlow(textPart, signUpLink);
        signUpPrompt.setTextAlignment(TextAlignment.CENTER);
        signUpPrompt.setPadding(new Insets(20, 0, 0, 0));

        loginBtn.prefWidthProperty().bind(loginFields.widthProperty().multiply(0.8F));
        loginFields.maxWidthProperty().bind(rightBox.widthProperty().divide(2));
        loginFields.setAlignment(Pos.CENTER);
        loginFields.getChildren().addAll(username, passwordRow, loginBtn, signUpPrompt);

        rightBox.getChildren().addAll(hello, signIn, loginFields);
        return rightBox;
    }

    /**
     * Handle login authentication
     */
    private void handleLogin(String username, String password) {
        // validate first
        if (username == null || username.trim().isEmpty()) {
            Utils.showError("Login Error", "Please enter username");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            Utils.showError("Login Error", "Please enter password");
            return;
        }

        try {
            // authenticate user
            UserDAO userDAO = UserDAO.getInstance();
            // it's probably cleaner to use a lambda here since we aren't returning anything, I'll do it in the next commit
            Optional<User> userOpt = userDAO.authenticate(username.trim(), password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                SessionManager.getInstance().login(user);

                // navigate appropriately
                if (user.isAdmin()) {
                    ViewSwitcher.switchTo(new AdminDashboardPage());
                } else {
                    ViewSwitcher.switchTo(new CustomerDashboardPage());
                }

            } else {
                Utils.showError("Login Failed", "Invalid username or password");
            }
        } catch (Exception ex) {
            Utils.showError("Login Error", "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    protected VBox getBanner() {
        SVGPath logo = Utils.getSVG(Constants.LOGO);
        logo.setFill(Constants.MCD_YELLOW);
        logo.setScaleX(0.4);
        logo.setScaleY(0.4);
        Group logoWrapper = new Group(logo);

        VBox leftBox = new VBox(10);
        leftBox.getStyleClass().add("left-pane");
        leftBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        leftBox.setMinWidth(0);
        leftBox.setPrefWidth(0);

        Label welcome = new Label("Welcome to");
        welcome.setStyle("-fx-font-size: 32px");
        Label brandName = new Label("McRonald's");
        brandName.setStyle("-fx-font-size: 32px; -fx-font-weight: 900");
        Label slogan = new Label("I'm Lovin' It");
        slogan.setStyle("-fx-font-weight: 300; -fx-font-size: 18px");

        leftBox.getChildren().addAll(welcome, logoWrapper, brandName, slogan);
        return leftBox;
    }
}