package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.util.Utils;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class LoginPage extends HBox {

    public LoginPage() {
        this.setSpacing(0);
        this.setFillHeight(true);

        SVGPath logo = Utils.getSVG(Utils.LOGO);
        logo.setFill(Color.web("#FFC300")); // McD yellow
        logo.getStyleClass().add("mcd-logo");
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
        brandName.setStyle("-fx-font-size: 32px");
        Label slogan = new Label("I'm Lovin' It");
        slogan.setStyle("-fx-font-weight: 300");

        leftBox.getChildren().addAll(welcome, logoWrapper, brandName, slogan);

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
        TextField password = new TextField();
        password.setPromptText("Password");
        loginFields.getStyleClass().add("login-fields");
        loginFields.maxWidthProperty().bind(rightBox.widthProperty().divide(2));
        loginFields.getChildren().addAll(username, password);


        rightBox.getChildren().addAll(hello, signIn, loginFields);

        this.getChildren().addAll(leftBox, rightBox);
    }
}