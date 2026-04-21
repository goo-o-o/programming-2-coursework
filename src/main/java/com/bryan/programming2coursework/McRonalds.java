package com.bryan.programming2coursework;

import com.bryan.programming2coursework.component.CustomTitleBar;
import com.bryan.programming2coursework.page.LoginPage;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main application class for McRonald's Restaurant Management System
 */
public class McRonalds extends Application {

    @Override
    public void start(Stage stage) {
        // Remove the original windows titlebar
        stage.initStyle(StageStyle.TRANSPARENT);

        // MAIN WINDOW
        VBox root = new VBox(0);
        root.getStyleClass().add("root-window");
        root.setPrefSize(1000, 700);

        // Add custom title bar
        CustomTitleBar titleBar = new CustomTitleBar(stage, "McRonald's");

        // Initialize view switcher
        ViewSwitcher.setRoot(root);

        root.getChildren().add(titleBar);

        // Load initial login page
        ViewSwitcher.switchTo(new LoginPage());

        // Clip the entire window to a rectangle for rounded corners
        root.setClip(Utils.getClip(root));

        Scene scene = new Scene(root, 1000, 700);
        // Make background transparent for rounded corners
        scene.setFill(Color.TRANSPARENT);

        // Load CSS stylesheet
        try {
            String css = getClass().getResource("style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Could not load stylesheet: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.setTitle("McRonald's Restaurant Management System");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}