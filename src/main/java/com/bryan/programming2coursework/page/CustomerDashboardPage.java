package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.component.MenuItemCard;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Customer dashboard for placing and managing orders
 */
public class CustomerDashboardPage extends VBox {

    private MenuItemDAO menuItemDAO;
    private OrderDAO orderDAO;

    private Label totalLabel;

    private MenuItemDAO.SortType activeSortType;
    private boolean isAscending = true;

    public CustomerDashboardPage() {
        this.menuItemDAO = MenuItemDAO.getInstance();
        this.orderDAO = OrderDAO.getInstance();
        initializeUI();
        updateCart();
        refreshMenu();
    }

    private ScrollPane menuScrollPane;
    private FlowPane menuGrid;
    private String currentUsername = SessionManager.getInstance().getCurrentUser().getUsername();
    private Label subHeader;
    private MenuItem.MenuCategory currentCategory = MenuItem.MenuCategory.ALL_CATEGORIES;
    private String currentSearchQuery = "";

    private void initializeUI() {
        this.setStyle("-fx-background-color: " + Constants.CREAM);
        this.setPadding(new Insets(30));

        HBox mainLayout = new HBox(40);

        // sidebar is categories
        VBox sidebar = createSidebar();

        VBox mainContent = new VBox(15);
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
        clearSort.getStyleClass().add("hover-btn");
        clearSort.setPrefSize(20, 20);
        clearSort.setOnAction(e -> {
            // small performance tweak
            if (activeSortType != null) {
                priceSort.setActive(false);
                stockSort.setActive(false);
                calSort.setActive(false);
                this.activeSortType = null;
                refreshMenu();
            }
        });

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.currentSearchQuery = newValue.toLowerCase();
            applyFilters();
        });

        sortBox.getChildren().addAll(sortLabel, priceSort, stockSort, calSort, clearSort, Utils.getHorizontalSpacer(), searchField);

        // actual menu grid
        menuGrid = new FlowPane(20, 20);
        menuGrid.setPadding(new Insets(5));
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

    private void applyFilters() {
        for (Node node : menuGrid.getChildren()) {
            if (node instanceof VBox card) {
                MenuItem item = (MenuItem) card.getUserData();

                boolean matchesCategory = (currentCategory == MenuItem.MenuCategory.ALL_CATEGORIES)
                        || item.getCategory() == currentCategory;

                boolean matchesSearch = currentSearchQuery.isEmpty() || currentSearchQuery.isBlank() || item.getName().toLowerCase().contains(currentSearchQuery)
                        || item.getDescription().toLowerCase().contains(currentSearchQuery);

                boolean isVisible = matchesCategory && matchesSearch;

                // toggle visibility and DOM update
                card.setVisible(isVisible);
                card.setManaged(isVisible);
            }
        }
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
            menuGrid.getChildren().add(new MenuItemCard(item, this::addToCart)); // method reference
        }
    }

    private void handleSort(SortButton clickedButton, MenuItemDAO.SortType type) {
        clickedButton.toggle();
        this.activeSortType = type;
        this.isAscending = clickedButton.getIsAscending();
        refreshMenu();
        applyFilters();
    }

    private VBox cartItemContainer;

    private void updateCart() {
        cartItemContainer.getChildren().clear();
        for (OrderItem item : SessionManager.getInstance().getCurrentOrder().getItems()) {
            cartItemContainer.getChildren().add(createCartItemRow(item));
        }
        totalLabel.setText(String.format("RM %.2f", SessionManager.getInstance().getCurrentOrder().getTotalPrice()));
    }

    private HBox createCartItemRow(OrderItem orderItem) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 15;");

        VBox details = new VBox(2);
        Label name = new Label(orderItem.getMenuItem().getName());
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #290d0b; -fx-font-size: 13px;");

        Label qtyAndPrice = new Label(orderItem.getQuantity() + "x  RM " + String.format("%.2f", orderItem.getMenuItem().getPrice()));
        qtyAndPrice.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        Label subtotal = new Label(String.format("RM %.2f", orderItem.getSubtotal()));
        subtotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #290d0b;");

        details.getChildren().addAll(name, qtyAndPrice, subtotal);


        HBox.setHgrow(details, Priority.ALWAYS);


        // remove button
        Button removeBtn = new Button();
        removeBtn.setGraphic(Utils.getSVG(Constants.CLOSE)); // Using your suggested SVG
        removeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5");
        removeBtn.getStyleClass().add("hover-btn");
        removeBtn.setScaleX(2);
        removeBtn.setScaleY(2);
        removeBtn.setOnAction(e -> removeFromCart(orderItem));

        row.getChildren().addAll(details, removeBtn);
        return row;
    }

    private VBox createCartSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setPrefWidth(350);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 25;");
        section.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.08)));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label cartLabel = new Label("My Cart");
        cartLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #290d0b;");
        header.getChildren().addAll(cartLabel, Utils.getHorizontalSpacer(), Utils.getSVG(Constants.CART));

        // main container
        VBox itemContainer = new VBox(15);
        itemContainer.setStyle("-fx-background-color: transparent;");

        // for now wrap it in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(itemContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // footer
        VBox footer = new VBox(15);
        footer.setPadding(new Insets(10, 0, 0, 0));

        Separator sep = new Separator();
        sep.setOpacity(0.5);

        HBox totalBox = new HBox();
        Label totalTitle = new Label("Total Amount");
        totalTitle.setStyle("-fx-text-fill: #888; -fx-font-weight: bold;");

        totalLabel = new Label("RM 0.00");
        totalLabel.setStyle("-fx-text-fill: #290d0b; -fx-font-size: 20px; -fx-font-weight: 900;");

        totalBox.getChildren().addAll(totalTitle, Utils.getHorizontalSpacer(), totalLabel);
        totalBox.setAlignment(Pos.CENTER_LEFT);

        Button checkoutBtn = new Button("Confirm Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setCursor(javafx.scene.Cursor.HAND);
        checkoutBtn.setStyle("-fx-background-color: #ffbc0d; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 16px; -fx-padding: 12; -fx-background-radius: 15;");

        // simple checkout hover anim
        checkoutBtn.setOnMouseEntered(e -> checkoutBtn.setStyle(checkoutBtn.getStyle() + "-fx-scale-x: 1.04; -fx-scale-y: 1.04;"));
        checkoutBtn.setOnMouseExited(e -> checkoutBtn.setStyle(checkoutBtn.getStyle().replace("-fx-scale-x: 1.04; -fx-scale-y: 1.04;", "")));

        checkoutBtn.setOnAction(e -> checkout());

        footer.getChildren().addAll(sep, totalBox, checkoutBtn);

        section.getChildren().addAll(header, scrollPane, footer);

        // store in class field so that updateCart() can access
        this.cartItemContainer = itemContainer;

        return section;
    }

    private void addToCart(MenuItem item) {
        if (!item.isInStock()) {
            Utils.showError("Out of Stock", item.getName() + " is out of stock");
            return;
        }

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Add " + item.getName() + " to Cart");

        // add button
        ButtonType addButtonType = new ButtonType("Add to Cart", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().setMaxWidth(Double.MAX_VALUE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType);

        int amtInCart = SessionManager.getInstance().getCurrentOrder().getItems()
                .stream().filter(orderItem -> orderItem.getMenuItem().getId() == item.getId()).mapToInt(OrderItem::getQuantity).sum();
        int stockLeft = item.getStockQuantity() - amtInCart;

        if (stockLeft <= 0) {
            Utils.showError("Out of Stock", "You have already added all\navailable stock to your cart!");
            return;
        }

        // spinner with max range of the available stock
        Spinner<Integer> qtySpinner = new Spinner<>(1, stockLeft, 1);
        qtySpinner.setEditable(true);
        qtySpinner.setMaxWidth(Double.MAX_VALUE);
        qtySpinner.setStyle("-fx-background-color: white; -fx-border-color: #3b82f6; -fx-border-radius: 5;");


        VBox content = new VBox(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.getChildren().addAll(new Label("Select Quantity (" + stockLeft + " left):"), qtySpinner);
        content.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(content);

        // convert to integer
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return qtySpinner.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(qty -> {
            try {
                SessionManager.getInstance().getCurrentOrder().addItem(new OrderItem(item, qty));
                updateCart();
                Utils.showInfo("Added to Cart", qty + "x " + item.getName() + " added successfully.");
            } catch (NumberFormatException ignored) {
                // to suppress error message
            }
        });
    }

    private void removeFromCart(OrderItem item) {
        SessionManager.getInstance().getCurrentOrder().removeItem(item);
        updateCart();
    }


    private void checkout() {
        if (SessionManager.getInstance().getCurrentOrder().getItems().isEmpty()) {
            Utils.showError("Empty Cart", "Please add items to cart before checkout");
            return;
        }

        if (Utils.showConfirmation("Checkout", String.format("Proceed with order?\nTotal: RM %.2f", SessionManager.getInstance().getCurrentOrder().getTotalPrice()))) {
            try {
                // decrease stock for all items
                for (OrderItem item : SessionManager.getInstance().getCurrentOrder().getItems()) {
                    MenuItem menuItem = item.getMenuItem();
                    menuItem.decreaseStock(item.getQuantity());
                    menuItemDAO.update(menuItem);
                }

                // save order
                orderDAO.create(SessionManager.getInstance().getCurrentOrder());
                Utils.showInfo("Order Placed", "Your order has been placed successfully!\nOrder ID: " + SessionManager.getInstance().getCurrentOrder().getId());

                // reset cart
                SessionManager.getInstance().setCurrentOrder(new Order(0, SessionManager.getInstance().getCurrentUserId()));
                updateCart();
                refreshMenu(); // refresh stock display
            } catch (Exception e) {
                Utils.showError("Checkout Error", "Failed to place order: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}