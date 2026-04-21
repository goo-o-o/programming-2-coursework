package com.bryan.programming2coursework.util;

import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Utility class for switching between different views/pages
 */
public class ViewSwitcher {
    public static VBox root;

    public static void setRoot(VBox rootNode) {
        root = rootNode;
    }

    public static void switchTo(Node page) {
        // remove all children except the title bar
        if (root.getChildren().size() > 1) {
            root.getChildren().remove(1, root.getChildren().size());
        }

        // add new page
        VBox.setVgrow(page, Priority.ALWAYS);
        root.getChildren().add(page);
    }
}