package com.bryan.programming2coursework.util;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

import static com.bryan.programming2coursework.util.ViewSwitcher.root;

public class Utils {
    public static String LOGO = "m195.8 17.933c23.3 0 42.2 98.3 42.2 219.7h34c0-130.7-34.3-236.5-76.3-236.5-24 0-45.2 31.7-59.2 81.5-14-49.8-35.2-81.5-59-81.5-42 0-76.2 105.7-76.2 236.4h34c0-121.4 18.7-219.6 42-219.6s42.2 90.8 42.2 202.8h33.8c0-112 19-202.8 42.3-202.8";
    public static String MINIMIZE = "M0,5 L10,5";
    public static String MAXIMIZE = "M2,2 H8 V8 H2 Z";
    public static String UNMAXIMIZE = "M3,4 H7 V8 H3 Z M5,2 H9 V6 H7";
    public static String CLOSE = "M2,2 L8,8 M8,2 L2,8";

    public static SVGPath getSVG(String content) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.getStyleClass().add("svg-icon");
        return path;
    }

    private static final Rectangle clip = new Rectangle();

    public static Rectangle getClip() {
        if (root != null) {
            clip.widthProperty().bind(root.widthProperty());
            clip.heightProperty().bind(root.heightProperty());
        }
        return clip;
    }

    static {
        clip.setArcWidth(30);
        clip.setArcHeight(30);
    }

}
