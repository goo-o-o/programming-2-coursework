package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.MenuItemDAO;
import com.bryan.programming2coursework.model.MenuItem;
import com.bryan.programming2coursework.util.SessionManager;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Admin dashboard for managing menu items (CRUD operations)
 */
public class AdminDashboardPage extends VBox {
    
    private MenuItemDAO menuItemDAO;
    private TableView<MenuItem> menuTable;
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    
    public AdminDashboardPage() {
        this.menuItemDAO = MenuItemDAO.getInstance();
        initializeUI();
        loadMenuItems();
    }
    
    private void initializeUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(20));
        
        // Header
        HBox header = createHeader();
        
        // Controls
        HBox controls = createControls();
        
        // Table
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        
        this.getChildren().addAll(header, controls, tableSection);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        
        Label title = new Label("Admin Dashboard - Menu Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button ordersBtn = new Button("View Orders");
        ordersBtn.setOnAction(e -> ViewSwitcher.switchTo(new AdminOrdersPage()));
        
        Button reportsBtn = new Button("Reports");
        reportsBtn.setOnAction(e -> ViewSwitcher.switchTo(new AdminReportsPage()));
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> handleLogout());
        
        header.getChildren().addAll(title, spacer, ordersBtn, reportsBtn, logoutBtn);
        return header;
    }
    
    private HBox createControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));
        
        // Search
        searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, old, newVal) -> filterMenu());
        
        // Category filter
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.getItems().addAll(menuItemDAO.getAllCategories());
        categoryFilter.setValue("All Categories");
        categoryFilter.setOnAction(e -> filterMenu());
        
        Button addBtn = new Button("+ Add New Item");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> showAddDialog());
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadMenuItems());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        controls.getChildren().addAll(
            new Label("Search:"), searchField,
            new Label("Category:"), categoryFilter,
            spacer, addBtn, refreshBtn
        );
        
        return controls;
    }
    
    private VBox createTableSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        menuTable = new TableView<>();
        VBox.setVgrow(menuTable, Priority.ALWAYS);
        
        TableColumn<MenuItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
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
        
        TableColumn<MenuItem, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);
        
        TableColumn<MenuItem, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    showEditDialog(item);
                });
                
                deleteBtn.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    deleteItem(item);
                });
                
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        menuTable.getColumns().addAll(idCol, nameCol, categoryCol, priceCol, 
                                      stockCol, descCol, actionCol);
        
        section.getChildren().add(menuTable);
        return section;
    }
    
    private void loadMenuItems() {
        List<MenuItem> items = menuItemDAO.findAll();
        menuTable.setItems(FXCollections.observableArrayList(items));
        
        // Update category filter
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.getItems().addAll(menuItemDAO.getAllCategories());
        categoryFilter.setValue("All Categories");
    }
    
    private void filterMenu() {
        String search = searchField.getText().toLowerCase();
        String category = categoryFilter.getValue();
        
        List<MenuItem> allItems = menuItemDAO.findAll();
        
        menuTable.setItems(FXCollections.observableArrayList(
            allItems.stream()
                .filter(item -> {
                    boolean matchesSearch = search.isEmpty() || 
                                          item.getName().toLowerCase().contains(search);
                    boolean matchesCategory = "All Categories".equals(category) || 
                                             item.getCategory().equals(category);
                    return matchesSearch && matchesCategory;
                })
                .toList()
        ));
    }
    
    private void showAddDialog() {
        Dialog<MenuItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Menu Item");
        dialog.setHeaderText("Enter item details");
        
        GridPane grid = createItemForm(null);
        dialog.getDialogPane().setContent(grid);
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return extractItemFromForm(grid, null);
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(item -> {
            if (item != null) {
                menuItemDAO.create(item);
                Utils.showInfo("Success", "Menu item added successfully");
                loadMenuItems();
            }
        });
    }
    
    private void showEditDialog(MenuItem item) {
        Dialog<MenuItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Menu Item");
        dialog.setHeaderText("Edit item details");
        
        GridPane grid = createItemForm(item);
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return extractItemFromForm(grid, item);
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(updatedItem -> {
            if (updatedItem != null) {
                menuItemDAO.update(updatedItem);
                Utils.showInfo("Success", "Menu item updated successfully");
                loadMenuItems();
            }
        });
    }
    
    private GridPane createItemForm(MenuItem item) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField(item != null ? item.getName() : "");
        TextField categoryField = new TextField(item != null ? item.getCategory() : "");
        TextField priceField = new TextField(item != null ? String.valueOf(item.getPrice()) : "");
        TextField stockField = new TextField(item != null ? String.valueOf(item.getStockQuantity()) : "");
        TextArea descField = new TextArea(item != null ? item.getDescription() : "");
        descField.setPrefRowCount(3);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Price (RM):"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descField, 1, 4);
        
        return grid;
    }
    
    private MenuItem extractItemFromForm(GridPane grid, MenuItem existingItem) {
        try {
            TextField nameField = (TextField) grid.getChildren().get(1);
            TextField categoryField = (TextField) grid.getChildren().get(3);
            TextField priceField = (TextField) grid.getChildren().get(5);
            TextField stockField = (TextField) grid.getChildren().get(7);
            TextArea descField = (TextArea) grid.getChildren().get(9);
            
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();
            String desc = descField.getText().trim();
            
            // Validation
            if (name.isEmpty() || category.isEmpty()) {
                Utils.showError("Validation Error", "Name and category are required");
                return null;
            }
            
            if (!Utils.isPositiveNumber(priceStr)) {
                Utils.showError("Validation Error", "Invalid price");
                return null;
            }
            
            if (!Utils.isPositiveInteger(stockStr) && !stockStr.equals("0")) {
                Utils.showError("Validation Error", "Invalid stock quantity");
                return null;
            }
            
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);
            
            MenuItem item = existingItem != null ? existingItem : new MenuItem();
            item.setName(name);
            item.setCategory(category);
            item.setPrice(price);
            item.setStockQuantity(stock);
            item.setDescription(desc);
            
            return item;
            
        } catch (Exception e) {
            Utils.showError("Error", "Invalid input: " + e.getMessage());
            return null;
        }
    }
    
    private void deleteItem(MenuItem item) {
        if (Utils.showConfirmation("Delete Item", 
            "Are you sure you want to delete " + item.getName() + "?")) {
            
            try {
                menuItemDAO.delete(item.getId());
                Utils.showInfo("Success", "Menu item deleted successfully");
                loadMenuItems();
            } catch (Exception e) {
                Utils.showError("Error", "Failed to delete item: " + e.getMessage());
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