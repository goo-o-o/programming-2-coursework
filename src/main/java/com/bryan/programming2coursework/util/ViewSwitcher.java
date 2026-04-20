package com.bryan.programming2coursework.util;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ViewSwitcher {
    public static VBox root;

    public static void setRoot(VBox root) {
        ViewSwitcher.root = root;
    }

    public static void switchTo(HBox newPage) {
        if (root == null) return;

        // since title bar would be at index 0, the page content is actually at index 1
        if (root.getChildren().size() > 1) {
            root.getChildren().remove(1);
        }

        // ensure grow
        VBox.setVgrow(newPage, Priority.ALWAYS);
        root.getChildren().add(newPage);
    }
}