package com.bryan.programming2coursework;

import com.bryan.programming2coursework.component.CustomTitleBar;
import com.bryan.programming2coursework.page.LoginPage;
import com.bryan.programming2coursework.util.DatabaseManager;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

/**
 * Main application class for McRonald's Restaurant Management System
 */
public class McRonalds extends Application {

    @Override
    public void start(Stage stage) {
        // remove original windows bar because I think it is ugly
        stage.initStyle(StageStyle.TRANSPARENT);

        // MAIN WINDOW
        VBox root = new VBox(0);
        root.getStyleClass().add("root-window");
        root.setPrefSize(1000, 700);

        // custom bar
        CustomTitleBar titleBar = new CustomTitleBar(stage, "McRonald's");

        // set this as the root for the view switcher
        ViewSwitcher.setRoot(root);

        root.getChildren().add(titleBar);

        // switch to login
        ViewSwitcher.switchTo(new LoginPage());

        // for rounded corners
        root.setClip(Utils.getClip(root));

        Scene scene = new Scene(root, 1000, 700);
        // we need to make background transparent since scene has its own rectangular background
        scene.setFill(Color.TRANSPARENT);

        // load css
        try {
            String css = Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Could not load stylesheet: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.setTitle("McRonald's Restaurant Management System");
        stage.show();
    }

    public static void main(String[] args) {
        DatabaseManager.getInstance().initializeDatabase();
        launch(args);
    }
}