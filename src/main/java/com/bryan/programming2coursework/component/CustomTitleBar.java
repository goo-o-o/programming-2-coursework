package com.bryan.programming2coursework.component;

import com.bryan.programming2coursework.util.Utils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import static com.bryan.programming2coursework.util.ViewSwitcher.root;

public class CustomTitleBar extends HBox {
    private double xOffset = 0;
    private double yOffset = 0;

    private void toggleMaximized(Stage stage, Button maxBtn) {
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            maxBtn.setGraphic(Utils.getSVG(Utils.MAXIMIZE));
            // restore rounded corners
            root.setClip(Utils.getClip(root));
        } else {
            stage.setMaximized(true);
            maxBtn.setGraphic(Utils.getSVG(Utils.UNMAXIMIZE));
            root.setClip(null);
        }
    }

    public CustomTitleBar(Stage stage, String title) {
        this.getStyleClass().add("title-bar");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(0, 10, 0, 15));
        this.setPrefHeight(40);

        // reimplement dragging logic
        this.setOnMousePressed(event -> {
            if (!stage.isMaximized()) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        this.setOnMouseDragged(event -> {
            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        this.setOnMouseReleased(event -> {
            // check if mouse on top of screen
            if (event.getScreenY() < 5) {
                stage.setMaximized(true);

                // remove rounding border
                if (stage.getScene().getRoot() instanceof VBox root) {
                    root.setClip(null);
                }

            }
        });

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-label");

        Region spacer = new Region();
        // push buttons to right
        HBox.setHgrow(spacer, Priority.ALWAYS);


        Button minBtn = new Button();
        SVGPath minIcon = Utils.getSVG(Utils.MINIMIZE);
        minBtn.setGraphic(minIcon);
        minBtn.setOnAction(e -> stage.setIconified(true));
        minBtn.getStyleClass().add("window-btn");

        Button maxBtn = new Button();
        SVGPath maxIcon = Utils.getSVG(stage.isMaximized() ? Utils.UNMAXIMIZE : Utils.MAXIMIZE);
        maxBtn.setGraphic(maxIcon);
        maxBtn.setOnAction(e -> toggleMaximized(stage, maxBtn));
        maxBtn.getStyleClass().add("window-btn");


        Button closeBtn = new Button();
        SVGPath closeIcon = Utils.getSVG(Utils.CLOSE);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setOnAction(e -> System.exit(0));
        closeBtn.getStyleClass().add("window-btn");

        HBox buttonBox = new HBox(5, minBtn, maxBtn, closeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        this.getChildren().addAll(titleLabel, spacer, buttonBox);

    }
}