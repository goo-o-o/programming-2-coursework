package com.bryan.programming2coursework.component;

import com.bryan.programming2coursework.model.MenuItem;
import com.bryan.programming2coursework.util.Constants;
import com.bryan.programming2coursework.util.Utils;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * A custom UI component representing a single menu item card.
 */
public class MenuItemCard extends VBox {

    private final MenuItem item;
    private final Label descLabel;

    public MenuItemCard(MenuItem item, Consumer<MenuItem> onAddAction) {
        this.item = item;
        this.setUserData(item);

        this.setSpacing(10);
        this.setPrefWidth(200);
        this.setMinWidth(200);
        this.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 15; -fx-alignment: top-center;");
        this.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));

        // stats =====================================
        Label calTag = new Label(item.getCalories() + " kcal");
        calTag.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 5 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-text-fill: #888; -fx-font-weight: bold;");

        Label stockTag = new Label("Qty: " + item.getStockQuantity());
        String stockColor = item.getStockQuantity() < 5 ? Constants.MCD_RED_HEX : "gray";
        stockTag.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 5 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-text-fill: " + stockColor + "; -fx-font-weight: bold;");

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        HBox statsBox = new HBox(calTag, hSpacer, stockTag);
        statsBox.setAlignment(Pos.CENTER);

        // image =====================================
        ImageView imageView = new ImageView(new Image(Utils.modifyUrlDimensions(item.getImageUrl(), 300, 300), 300, 300, true, true, true));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        // name =====================================
        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #290d0b;");

        // desc =====================================
        this.descLabel = new Label(item.getDescription());
        this.descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray; -fx-italic: true; -fx-text-alignment: center;");
        this.descLabel.setWrapText(true);
        this.descLabel.setOpacity(0);
        this.descLabel.setMaxHeight(0);
        this.descLabel.setManaged(false);

        Label priceLabel = new Label(String.format("RM %.2f", item.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #ffbc0d; -fx-font-weight: 900; -fx-font-size: 18px;");

        // price and add =====================================
        Button addBtn = new Button("Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setCursor(javafx.scene.Cursor.HAND);
        addBtn.setStyle("-fx-background-color: #ffbc0d; -fx-background-radius: 15; -fx-text-fill: white; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> onAddAction.accept(item));

        this.getChildren().addAll(statsBox, imageView, nameLabel, this.descLabel, Utils.getVerticalSpacer(), priceLabel, addBtn);

        setupAnimations();
    }

    private void setupAnimations() {
        this.setOnMouseEntered(e -> {
            this.setStyle("-fx-background-color: #fff9e6; -fx-background-radius: 20; -fx-padding: 15; -fx-alignment: top-center; -fx-border-color: #ffbc0d; -fx-border-radius: 20;");
            descLabel.setVisible(true);
            descLabel.setManaged(true);
            double targetHeight = descLabel.prefHeight(this.getWidth() - 30) + 10;

            Timeline grow = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(descLabel.maxHeightProperty(), targetHeight)));
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), descLabel);
            fadeIn.setToValue(1);
            new ParallelTransition(grow, fadeIn).play();
        });

        this.setOnMouseExited(e -> {
            this.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 15; -fx-alignment: top-center; -fx-border-color: transparent;");
            Timeline shrink = new Timeline(new KeyFrame(Duration.millis(200), new KeyValue(descLabel.maxHeightProperty(), 0)));
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), descLabel);
            fadeOut.setToValue(0);
            ParallelTransition hide = new ParallelTransition(shrink, fadeOut);
            hide.setOnFinished(evt -> {
                descLabel.setVisible(false);
                descLabel.setManaged(false);
            });
            hide.play();
        });
    }

}