package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.MenuItemDAO;
import com.bryan.programming2coursework.dao.OrderDAO;
import com.bryan.programming2coursework.model.MenuItem;
import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.OrderItem;
import com.bryan.programming2coursework.util.SessionManager;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Customer dashboard for placing and managing orders
 */
public class CustomerDashboardPage extends VBox {
    
    private MenuItemDAO menuItemDAO;
    private OrderDAO orderDAO;
    private Order currentOrder;
    
    private TableView<MenuItem> menuTable;
    private TableView<OrderItem> cartTable;
    private Label totalLabel;
    private ComboBox<String> categoryFilter;
    private TextField searchField;
    
    public CustomerDashboardPage() {
        this.menuItemDAO = MenuItemDAO.getInstance();
        this.orderDAO = OrderDAO.getInstance();
        this.currentOrder = new Order(0, SessionManager.getInstance().getCurrentUserId(),
                                     SessionManager.getInstance().getCurrentUser().getUsername());
        
        initializeUI();
        loadMenuItems();
    }
    
    private void initializeUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(20));
        this.getStyleClass().add("customer-dashboard");
        
        // Header
        HBox header = createHeader();
        
        // Main content - Split between menu and cart
        HBox mainContent = new HBox(15);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Left side - Menu
        VBox menuSection = createMenuSection();
        HBox.setHgrow(menuSection, Priority.ALWAYS);
        
        // Right side - Cart
        VBox cartSection = createCartSection();
        cartSection.setPrefWidth(350);
        
        mainContent.getChildren().addAll(menuSection, cartSection);
        
        this.getChildren().addAll(header, mainContent);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        
        Label title = new Label("Welcome, " + SessionManager.getInstance().getCurrentUser().getUsername());
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button myOrdersBtn = new Button("My Orders");
        myOrdersBtn.setOnAction(e -> ViewSwitcher.switchTo(new CustomerOrdersPage()));
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> handleLogout());
        
        header.getChildren().addAll(title, spacer, myOrdersBtn, logoutBtn);
        return header;
    }
    
    private VBox createMenuSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label menuLabel = new Label("Menu");
        menuLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        // searc and filter
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, old, newVal) -> filterMenu());
        
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.getItems().addAll(menuItemDAO.getAllCategories());
        categoryFilter.setValue("All Categories");
        categoryFilter.setOnAction(e -> filterMenu());
        
        filterBox.getChildren().addAll(new Label("Search:"), searchField, 
                                       new Label("Category:"), categoryFilter);
        
        // menu table
        menuTable = new TableView<>();
        VBox.setVgrow(menuTable, Priority.ALWAYS);
        
        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<MenuItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);
        
        TableColumn<MenuItem, Double> priceCol = new TableColumn<>("Price (RM)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);
        
        TableColumn<MenuItem, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        stockCol.setPrefWidth(80);
        
        TableColumn<MenuItem, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("Add to Cart");
            
            {
                addBtn.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    addToCart(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    MenuItem menuItem = getTableView().getItems().get(getIndex());
                    addBtn.setDisable(!menuItem.isInStock());
                    setGraphic(addBtn);
                }
            }
        });
        
        menuTable.getColumns().addAll(nameCol, categoryCol, priceCol, stockCol, actionCol);
        
        section.getChildren().addAll(menuLabel, filterBox, menuTable);
        return section;
    }
    
    private VBox createCartSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label cartLabel = new Label("Shopping Cart");
        cartLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        // Cart table
        cartTable = new TableView<>();
        VBox.setVgrow(cartTable, Priority.ALWAYS);
        
        TableColumn<OrderItem, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getMenuItem().getName()));
        itemCol.setPrefWidth(130);
        
        TableColumn<OrderItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(50);
        
        TableColumn<OrderItem, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getSubtotal()));
        subtotalCol.setPrefWidth(80);
        
        TableColumn<OrderItem, Void> removeCol = new TableColumn<>("Action");
        removeCol.setPrefWidth(60);
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("X");
            
            {
                removeBtn.setOnAction(e -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    removeFromCart(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
        
        cartTable.getColumns().addAll(itemCol, qtyCol, subtotalCol, removeCol);
        
        // Total and buttons
        totalLabel = new Label("Total: RM 0.00");
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Button clearBtn = new Button("Clear Cart");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearCart());
        
        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        checkoutBtn.setOnAction(e -> checkout());
        
        section.getChildren().addAll(cartLabel, cartTable, totalLabel, clearBtn, checkoutBtn);
        return section;
    }
    
    private void loadMenuItems() {
        List<MenuItem> items = menuItemDAO.findAll();
        menuTable.setItems(FXCollections.observableArrayList(items));
    }
    
    private void filterMenu() {
        String search = searchField.getText().toLowerCase();
        String category = categoryFilter.getValue();
        
        List<MenuItem> allItems = menuItemDAO.findAll();
        
        ObservableList<MenuItem> filtered = FXCollections.observableArrayList(
            allItems.stream()
                .filter(item -> {
                    boolean matchesSearch = search.isEmpty() || 
                                          item.getName().toLowerCase().contains(search);
                    boolean matchesCategory = "All Categories".equals(category) || 
                                             item.getCategory().equals(category);
                    return matchesSearch && matchesCategory;
                })
                .toList()
        );
        
        menuTable.setItems(filtered);
    }
    
    private void addToCart(MenuItem item) {
        if (!item.isInStock()) {
            Utils.showError("Out of Stock", item.getName() + " is out of stock");
            return;
        }
        
        // ask for quantity
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
                    Utils.showError("Insufficient Stock", 
                        "Only " + item.getStockQuantity() + " available");
                    return;
                }
                
                OrderItem orderItem = new OrderItem(item, qty);
                currentOrder.addItem(orderItem);
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
    
    private void clearCart() {
        if (Utils.showConfirmation("Clear Cart", "Are you sure you want to clear the cart?")) {
            currentOrder.clearItems();
            updateCart();
        }
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
        
        if (Utils.showConfirmation("Checkout", 
            String.format("Proceed with order?\nTotal: RM %.2f", currentOrder.getTotalAmount()))) {
            
            try {
                // decrease stock for all items
                for (OrderItem item : currentOrder.getItems()) {
                    MenuItem menuItem = item.getMenuItem();
                    menuItem.decreaseStock(item.getQuantity());
                    menuItemDAO.update(menuItem);
                }
                
                // save order
                orderDAO.create(currentOrder);
                
                Utils.showInfo("Order Placed", 
                    "Your order has been placed successfully!\nOrder ID: " + currentOrder.getId());
                
                // reset cart
                currentOrder = new Order(0, SessionManager.getInstance().getCurrentUserId(),
                                       SessionManager.getInstance().getCurrentUser().getUsername());
                updateCart();
                loadMenuItems(); // refresh stock display
                
            } catch (Exception e) {
                Utils.showError("Checkout Error", "Failed to place order: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void handleLogout() {
        if (Utils.showConfirmation("Logout", "Are you sure you want to logout?")) {
            SessionManager.getInstance().logout();
            ViewSwitcher.switchTo(new LoginPage());
        }
    }
}