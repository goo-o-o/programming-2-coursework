package com.bryan.programming2coursework.component;

import com.bryan.programming2coursework.util.Constants;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class SortButton extends Button {
    private final BooleanProperty ascending = new SimpleBooleanProperty(false);
    private final BooleanProperty active = new SimpleBooleanProperty(false);
    private SortButton[] siblings;
    private final String baseText;

    public SortButton(String text) {
        super(text);
        this.baseText = text;

        // make text change based on properties
        this.textProperty().bind(Bindings.createStringBinding(() -> {
            if (active.get()) return baseText + (ascending.get() ? " ↑" : " ↓");
            return baseText;
        }, active, ascending));

        this.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 50; -fx-background-radius: 50; -fx-cursor: hand;");
        this.setEffect(null);
        this.setTextFill(Color.BLACK);
    }

    public void setSiblings(SortButton... siblings) {
        this.siblings = siblings;
    }

    // I want to change the color on whenever it is toggled, so this is where encapsulation is good
    public void setActive(boolean active) {
        this.active.set(active);
        if (active) {
            this.setStyle("-fx-background-color: " + Constants.MCD_RED_HEX + "; -fx-border-color: white; -fx-border-radius: 50; -fx-background-radius: 50; -fx-cursor: hand;");
            this.setEffect(new DropShadow(5, Constants.MCD_RED));
            this.setTextFill(Color.WHITE);
        } else {
            this.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 50; -fx-background-radius: 50; -fx-cursor: hand;");
            this.setEffect(null);
            this.setTextFill(Color.BLACK);
        }
    }

    public void toggle() {
        if (active.get()) {
            ascending.set(!ascending.get()); // if already active, toggle
        } else {
            setActive(true); // activate if not active
            ascending.set(true); // default ascending
        }

        if (siblings != null) {
            for (SortButton sibling : siblings) {
                sibling.setActive(false); // disable other siblings
            }
        }
    }

    public boolean getIsAscending() {
        return ascending.get();
    }
}