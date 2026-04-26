package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.component.SortButton;
import com.bryan.programming2coursework.dao.MenuItemDAO;
import com.bryan.programming2coursework.dao.OrderDAO;
import com.bryan.programming2coursework.model.MenuItem;
import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.OrderItem;
import com.bryan.programming2coursework.util.Constants;
import com.bryan.programming2coursework.util.SessionManager;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.List;

/**
 * Customer dashboard for placing and managing orders
 */
public class CustomerDashboardPage extends VBox {

    private MenuItemDAO menuItemDAO;
    private OrderDAO orderDAO;
    private Order currentOrder;

    private TableView<OrderItem> cartTable;
    private Label totalLabel;

    private MenuItemDAO.SortType activeSortType;
    private boolean isAscending = true;

    public CustomerDashboardPage() {
        this.menuItemDAO = MenuItemDAO.getInstance();
        this.orderDAO = OrderDAO.getInstance();
        this.currentOrder = new Order(0, SessionManager.getInstance().getCurrentUserId(),
                SessionManager.getInstance().getCurrentUser().getUsername());
        initializeUI();
        refreshMenu(); // Start with a fresh load of items
    }

    private ScrollPane menuScrollPane;
    private FlowPane menuGrid;
    private String currentUsername = SessionManager.getInstance().getCurrentUser().getUsername();
    private Label subHeader;
    private MenuItem.MenuCategory currentCategory = MenuItem.MenuCategory.ALL_CATEGORIES;
    private String currentSearchQuery = "";

    private void initializeUI() {
        this.setStyle("-fx-background-color: #fdfaf4;");
        this.setPadding(new Insets(30));

        HBox mainLayout = new HBox(40);

        // sidebar is categories
        VBox sidebar = createSidebar();

        VBox mainContent = new VBox(25);
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        // welcome
        HBox welcomeAndProfile = new HBox();
        welcomeAndProfile.setAlignment(Pos.CENTER);
        Label welcomeLabel = new Label("Welcome, " + currentUsername + "!");
        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #290d0b;");

        Button profileBtn = new Button();
        profileBtn.setGraphic(Utils.getSVG(Constants.USER));
        profileBtn.setOnMouseClicked(event -> ViewSwitcher.switchTo(new UserProfilePage()));
        profileBtn.setBackground(Background.EMPTY);
        profileBtn.setBorder(Border.EMPTY);
        profileBtn.setCursor(javafx.scene.Cursor.HAND);

        welcomeAndProfile.getChildren().addAll(welcomeLabel, Utils.getHorizontalSpacer(), profileBtn);

        subHeader = new Label(MenuItem.MenuCategory.ALL_CATEGORIES.display);
        subHeader.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #6e1a11;");

        // sorting
        HBox sortBox = new HBox(15);
        sortBox.setAlignment(Pos.CENTER_LEFT);

        Label sortLabel = new Label("Sort by:");
        sortLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        SortButton priceSort = new SortButton("Price");
        SortButton stockSort = new SortButton("Stock");
        SortButton calSort = new SortButton("Calories");

        // link
        priceSort.setSiblings(stockSort, calSort);
        stockSort.setSiblings(priceSort, calSort);
        calSort.setSiblings(priceSort, stockSort);

        priceSort.setOnAction(e -> handleSort(priceSort, MenuItemDAO.SortType.PRICE));
        stockSort.setOnAction(e -> handleSort(stockSort, MenuItemDAO.SortType.STOCK));
        calSort.setOnAction(e -> handleSort(calSort, MenuItemDAO.SortType.CALORIES));

        Button clearSort = new Button();
        clearSort.setGraphic(Utils.getSVG(Constants.CLOSE));
        clearSort.setBorder(Border.EMPTY);
        clearSort.setBackground(Background.EMPTY);
        clearSort.setCursor(javafx.scene.Cursor.HAND);
        clearSort.getStyleClass().add("clear-sort-btn");
        clearSort.setPrefSize(20, 20);
        clearSort.setOnAction(e -> {
            priceSort.setActive(false);
            stockSort.setActive(false);
            calSort.setActive(false);
            this.activeSortType = null;
            refreshMenu();
        });

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setOnKeyTyped(e -> {
            this.currentSearchQuery = searchField.getText();
            refreshMenu();
        });

        sortBox.getChildren().addAll(sortLabel, priceSort, stockSort, calSort, clearSort, Utils.getHorizontalSpacer(), searchField);

        // actual menu grid
        menuGrid = new FlowPane(20, 20);
        menuGrid.setPadding(new Insets(10));
        menuGrid.setAlignment(Pos.CENTER);
        menuScrollPane = new ScrollPane(menuGrid);
        menuScrollPane.setFitToWidth(true);
        menuScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(menuScrollPane, Priority.ALWAYS);

        mainContent.getChildren().addAll(welcomeAndProfile, subHeader, sortBox, menuScrollPane);

        // cart
        VBox cartArea = createCartSection();

        mainLayout.getChildren().addAll(sidebar, mainContent, cartArea);
        this.getChildren().add(mainLayout);
    }

    private VBox createItemCard(MenuItem item) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 15; -fx-alignment: top-center;");
        card.setPrefWidth(225);
        card.setMinWidth(225);
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));

        // stats =====================================
        Label calTag = new Label(item.getCalories() + " kcal");
        calTag.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 5 10; -fx-background-radius: 10; " +
                "-fx-font-size: 10px; -fx-text-fill: #888; -fx-font-weight: bold;");

        Label stockTag = new Label("Qty: " + item.getStockQuantity());
        String stockColor = item.getStockQuantity() < 5 ? Constants.MCD_RED_HEX : "gray";
        stockTag.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 5 10; -fx-background-radius: 10; " +
                "-fx-font-size: 10px; -fx-text-fill: " + stockColor + "; -fx-font-weight: bold;");

        Region hSpacer = Utils.getHorizontalSpacer();

        HBox statsBox = new HBox(calTag, hSpacer, stockTag);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setMaxWidth(Double.MAX_VALUE);

        // image =====================================
        ImageView imageView = new ImageView(new Image(Utils.modifyUrlDimensions(item.getImageUrl(), 300, 300), 300, 300, true, true, true));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        // name =====================================
        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #290d0b;");

        // desc =====================================
        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray; -fx-italic: true; -fx-text-alignment: center;");
        descLabel.setWrapText(true);
        descLabel.setOpacity(0);
        descLabel.setMaxHeight(0);
        descLabel.setManaged(false);

        // price and add =====================================
        Label priceLabel = new Label(String.format("RM %.2f", item.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #ffbc0d; -fx-font-weight: 900; -fx-font-size: 18px;");

        Button addBtn = new Button("Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setCursor(javafx.scene.Cursor.HAND);
        addBtn.setStyle("-fx-background-color: #ffbc0d; -fx-background-radius: 15; -fx-text-fill: white; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> addToCart(item));

        // Push button to the very bottom
        Region vSpacer = Utils.getVerticalSpacer();

        card.getChildren().addAll(statsBox, imageView, nameLabel, descLabel, vSpacer, priceLabel, addBtn);

        // Animations
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #fff9e6; -fx-background-radius: 20; -fx-padding: 15; -fx-alignment: top-center; -fx-border-color: #ffbc0d; -fx-border-radius: 20;");
            descLabel.setManaged(true);
            double targetHeight = descLabel.prefHeight(card.getWidth() - 30) + 10;
            Timeline grow = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(descLabel.maxHeightProperty(), targetHeight)));
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), descLabel);
            fadeIn.setToValue(1);
            new ParallelTransition(grow, fadeIn).play();
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 15; -fx-alignment: top-center; -fx-border-color: transparent;");
            Timeline shrink = new Timeline(new KeyFrame(Duration.millis(200), new KeyValue(descLabel.maxHeightProperty(), 0)));
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), descLabel);
            fadeOut.setToValue(0);
            ParallelTransition hide = new ParallelTransition(shrink, fadeOut);
            hide.setOnFinished(evt -> descLabel.setManaged(false));
            hide.play();
        });

        return card;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(220);

        for (MenuItem.MenuCategory cat : MenuItem.MenuCategory.values()) {
            Button catBtn = new Button(cat.display);
            catBtn.setMaxWidth(Double.MAX_VALUE);
            catBtn.setAlignment(Pos.CENTER_LEFT);
            catBtn.setCursor(javafx.scene.Cursor.HAND);

            Utils.updateButtonStateDesign(catBtn, cat == MenuItem.MenuCategory.ALL_CATEGORIES);

            catBtn.setOnAction(e -> {
                for (Node node : sidebar.getChildren()) {
                    if (node instanceof Button btn) Utils.updateButtonStateDesign(btn, false);
                }
                Utils.updateButtonStateDesign(catBtn, true);
                subHeader.setText(cat.display);
                currentCategory = cat;
                refreshMenu();
            });

            sidebar.getChildren().add(catBtn);
        }
        return sidebar;
    }

    private void refreshMenu() {
        menuGrid.getChildren().clear();
        List<MenuItem> items = menuItemDAO.getItems(
                currentCategory,
                currentSearchQuery,
                activeSortType,
                isAscending
        );
        for (MenuItem item : items) {
            menuGrid.getChildren().add(createItemCard(item));
        }
    }

    private void handleSort(SortButton clickedButton, MenuItemDAO.SortType type) {
        clickedButton.toggle();
        this.activeSortType = type;
        this.isAscending = clickedButton.getIsAscending();
        refreshMenu();
    }

    private VBox createCartSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setPrefWidth(320);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        section.setEffect(new DropShadow(5, Color.LIGHTGRAY));

        Label cartLabel = new Label("Shopping Cart");
        cartLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        cartTable = new TableView<>();
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        TableColumn<OrderItem, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMenuItem().getName()));
        itemCol.setPrefWidth(120);

        TableColumn<OrderItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(45);

        TableColumn<OrderItem, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getSubtotal()));
        subtotalCol.setPrefWidth(70);

        TableColumn<OrderItem, Void> removeCol = new TableColumn<>("");
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("X");
            {
                removeBtn.setStyle("-fx-text-fill: #ff4444; -fx-background-color: transparent; -fx-font-weight: bold;");
                removeBtn.setOnAction(e -> removeFromCart(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        cartTable.getColumns().addAll(itemCol, qtyCol, subtotalCol, removeCol);

        totalLabel = new Label("Total: RM 0.00");
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        checkoutBtn.setOnAction(e -> checkout());

        section.getChildren().addAll(cartLabel, cartTable, totalLabel, checkoutBtn);
        return section;
    }

    private void addToCart(MenuItem item) {
        if (!item.isInStock()) {
            Utils.showError("Out of Stock", item.getName() + " is out of stock");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Add " + item.getName() + " to cart");
        dialog.setContentText("Quantity:");

        dialog.showAndWait().ifPresent(qtyStr -> {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    Utils.showError("Invalid Quantity", "Quantity must be positive");
                    return;
                }
                if (qty > item.getStockQuantity()) {
                    Utils.showError("Insufficient Stock", "Only " + item.getStockQuantity() + " available");
                    return;
                }
                currentOrder.addItem(new OrderItem(item, qty));
                updateCart();
            } catch (NumberFormatException e) {
                Utils.showError("Invalid Input", "Please enter a valid number");
            }
        });
    }

    private void removeFromCart(OrderItem item) {
        currentOrder.removeItem(item);
        updateCart();
    }

    private void updateCart() {
        cartTable.setItems(FXCollections.observableArrayList(currentOrder.getItems()));
        totalLabel.setText(String.format("Total: RM %.2f", currentOrder.getTotalAmount()));
    }

    private void checkout() {
        if (currentOrder.getItems().isEmpty()) {
            Utils.showError("Empty Cart", "Please add items to cart before checkout");
            return;
        }

        if (Utils.showConfirmation("Checkout", String.format("Proceed with order?\nTotal: RM %.2f", currentOrder.getTotalAmount()))) {
            try {
                // decrease stock for all items
                for (OrderItem item : currentOrder.getItems()) {
                    MenuItem menuItem = item.getMenuItem();
                    menuItem.decreaseStock(item.getQuantity());
                    menuItemDAO.update(menuItem);
                }

                // save order
                orderDAO.create(currentOrder);
                Utils.showInfo("Order Placed", "Your order has been placed successfully!\nOrder ID: " + currentOrder.getId());

                // reset cart
                currentOrder = new Order(0, SessionManager.getInstance().getCurrentUserId(),
                        SessionManager.getInstance().getCurrentUser().getUsername());
                updateCart();
                refreshMenu(); // refresh stock display
            } catch (Exception e) {
                Utils.showError("Checkout Error", "Failed to place order: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}