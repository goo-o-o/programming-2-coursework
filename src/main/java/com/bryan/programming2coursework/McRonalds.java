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
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class McRonalds extends Application {

    @Override
    public void start(Stage stage) {
        // remove the original windows titlebar because I think its ugly
        stage.initStyle(StageStyle.TRANSPARENT);

        // MAIN WINDOW
        VBox root = new VBox(0);
        root.getStyleClass().add("root-window");
        root.setPrefSize(800, 600);

        // add our own title bar
        CustomTitleBar titleBar = new CustomTitleBar(stage, "McRonald's");

        // initialize view switcher
        ViewSwitcher.setRoot(root);

        root.getChildren().add(titleBar);

        // load initial login page
        ViewSwitcher.switchTo(new LoginPage());

        // clip the entire window to a rectangle so that we can make it rounded
        root.setClip(Utils.getClip());

        Scene scene = new Scene(root, 800, 600);
        // so that we can see the actual rounding instead of having a background
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}