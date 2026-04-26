package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.model.User;
import com.bryan.programming2coursework.util.Constants;
import com.bryan.programming2coursework.util.SessionManager;
import com.bryan.programming2coursework.util.Utils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class UserProfilePage extends VBox {
    private VBox contentArea;
    private User currentUser;
    /**
     * Creates a {@code VBox} layout with {@code spacing = 0} and alignment at {@code TOP_LEFT}.
     */
    public UserProfilePage() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        initializeUI();
    }
    private void initializeUI() {
        this.setSpacing(30);
        this.setPadding(new Insets(40));
        this.setStyle("-fx-background-color: " + Constants.MCD_RED);

        VBox sidebar = createSidebar();

        contentArea = new VBox();
        contentArea.setMinWidth(500);
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        this.getChildren().addAll(sidebar, contentArea);

        // default view
        showProfileSection();
    }
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(280);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        sidebar.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.2)));

        // User Header in Sidebar
        VBox userHeader = new VBox(5);
        userHeader.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(currentUser.getUsername());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label email = new Label(currentUser.getEmail());
        email.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        userHeader.getChildren().addAll(name, email);
        userHeader.setPadding(new Insets(0, 0, 20, 0));

        // Sidebar Buttons
        Button btnProfile = createSidebarButton("My Profile", "👤");
        Button btnSettings = createSidebarButton("Settings", "⚙");
        Button btnLogout = createSidebarButton("Log Out", "↪");

        btnProfile.setOnAction(e -> showProfileSection());
        btnSettings.setOnAction(e -> showSettingsSection());
        btnLogout.setOnAction(e -> Utils.handleLogout());

        sidebar.getChildren().addAll(userHeader, new Separator(), btnProfile, btnSettings, btnLogout);
        return sidebar;
    }

    private Button createSidebarButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 12; -fx-font-size: 14px; -fx-cursor: hand;");

        // hover effects
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 10; -fx-padding: 12;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-padding: 12;"));

        return btn;
    }

    private void showProfileSection() {
        contentArea.getChildren().clear();

        VBox profileCard = new VBox(20);
        profileCard.setPadding(new Insets(30));
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        profileCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));

        Label title = new Label("Edit Profile");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // input fields
        VBox fields = new VBox(15);
        fields.getChildren().addAll(
                createInputField("Name", currentUser.getUsername()),
                createInputField("Email account", currentUser.getEmail()),
                createInputField("Location", "Malaysia")
        );

        Button saveBtn = new Button("Save Change");
        saveBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 10;");

        profileCard.getChildren().addAll(title, fields, saveBtn);
        contentArea.getChildren().add(profileCard);
    }

    private void showSettingsSection() {
        contentArea.getChildren().clear();
        VBox settingsCard = new VBox(20);
        settingsCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 30;");
        settingsCard.getChildren().add(new Label("Settings Section coming soon..."));
        contentArea.getChildren().add(settingsCard);
    }

    private VBox createInputField(String labelText, String value) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #777; -fx-font-size: 12px;");
        TextField tf = new TextField(value);
        tf.setStyle("-fx-background-color: transparent; -fx-border-color: #eee; -fx-border-width: 0 0 1 0; -fx-padding: 5 0;");
        box.getChildren().addAll(lbl, tf);
        return box;
    }

}
