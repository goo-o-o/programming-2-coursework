package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.OrderDAO;
import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.Order.OrderStatus;
import com.bryan.programming2coursework.model.OrderItem;
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
 * Admin page for managing all orders
 */
public class AdminOrdersPage extends VBox {
    
    private OrderDAO orderDAO;
    private TableView<Order> ordersTable;
    private TextArea orderDetailsArea;
    private ComboBox<String> statusFilter;
    
    public AdminOrdersPage() {
        this.orderDAO = OrderDAO.getInstance();
        initializeUI();
        loadOrders();
    }
    
    private void initializeUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(20));
        
        HBox header = createHeader();
        HBox controls = createControls();
        
        HBox mainContent = new HBox(15);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        VBox ordersSection = createOrdersSection();
        HBox.setHgrow(ordersSection, Priority.ALWAYS);
        
        VBox detailsSection = createDetailsSection();
        detailsSection.setPrefWidth(350);
        
        mainContent.getChildren().addAll(ordersSection, detailsSection);
        
        this.getChildren().addAll(header, controls, mainContent);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> ViewSwitcher.switchTo(new AdminDashboardPage()));
        
        Label title = new Label("Order Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadOrders());
        
        header.getChildren().addAll(backBtn, title, spacer, refreshBtn);
        return header;
    }
    
    private HBox createControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));
        
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "PENDING", "PREPARING", 
                                      "READY", "COMPLETED", "CANCELLED");
        statusFilter.setValue("All Status");
        statusFilter.setOnAction(e -> filterOrders());
        
        controls.getChildren().addAll(new Label("Filter by Status:"), statusFilter);
        return controls;
    }
    
    private VBox createOrdersSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        ordersTable = new TableView<>();
        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerCol.setPrefWidth(120);
        
        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFormattedOrderDate()));
        dateCol.setPrefWidth(150);
        
        TableColumn<Order, Double> totalCol = new TableColumn<>("Total (RM)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setPrefWidth(100);
        
        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        
        TableColumn<Order, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<OrderStatus> statusCombo = new ComboBox<>();
            private final Button updateBtn = new Button("Update");
            private final HBox buttons = new HBox(5, statusCombo, updateBtn);
            
            {
                statusCombo.getItems().addAll(OrderStatus.values());
                statusCombo.setPrefWidth(120);
                
                updateBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    OrderStatus newStatus = statusCombo.getValue();
                    if (newStatus != null) {
                        updateOrderStatus(order, newStatus);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    statusCombo.setValue(order.getStatus());
                    setGraphic(buttons);
                }
            }
        });
        
        ordersTable.getColumns().addAll(idCol, customerCol, dateCol, 
                                        totalCol, statusCol, actionCol);
        
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                showOrderDetails(newVal);
            }
        });
        
        section.getChildren().add(ordersTable);
        return section;
    }
    
    private VBox createDetailsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label label = new Label("Order Details");
        label.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        orderDetailsArea = new TextArea();
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setWrapText(true);
        VBox.setVgrow(orderDetailsArea, Priority.ALWAYS);
        
        section.getChildren().addAll(label, orderDetailsArea);
        return section;
    }
    
    private void loadOrders() {
        List<Order> orders = orderDAO.findAll();
        orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        ordersTable.setItems(FXCollections.observableArrayList(orders));
        
        if (!orders.isEmpty()) {
            ordersTable.getSelectionModel().selectFirst();
        }
    }
    
    private void filterOrders() {
        String statusStr = statusFilter.getValue();
        List<Order> orders;
        
        if ("All Status".equals(statusStr)) {
            orders = orderDAO.findAll();
        } else {
            OrderStatus status = OrderStatus.valueOf(statusStr);
            orders = orderDAO.findByStatus(status);
        }
        
        orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        ordersTable.setItems(FXCollections.observableArrayList(orders));
    }
    
    private void showOrderDetails(Order order) {
        StringBuilder details = new StringBuilder();
        
        details.append("═══════════════════════════════\n");
        details.append("     Order Details (Admin)     \n");
        details.append("═══════════════════════════════\n\n");
        
        details.append("Order ID: #").append(order.getId()).append("\n");
        details.append("Customer: ").append(order.getCustomerName()).append("\n");
        details.append("Date: ").append(order.getFormattedOrderDate()).append("\n");
        details.append("Status: ").append(order.getStatus()).append("\n\n");
        
        details.append("Items:\n");
        details.append("───────────────────────────────\n");
        
        for (OrderItem item : order.getItems()) {
            details.append(String.format("%-20s x%-3d  RM%7.2f\n",
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getSubtotal()));
        }
        
        details.append("───────────────────────────────\n");
        details.append(String.format("Total Items: %d\n", order.getTotalItems()));
        details.append(String.format("TOTAL: RM %.2f\n", order.getTotalAmount()));
        details.append("═══════════════════════════════\n");
        
        orderDetailsArea.setText(details.toString());
    }
    
    private void updateOrderStatus(Order order, OrderStatus newStatus) {
        try {
            order.setStatus(newStatus);
            orderDAO.update(order);
            Utils.showInfo("Success", "Order status updated to " + newStatus);
            loadOrders();
        } catch (Exception e) {
            Utils.showError("Error", "Failed to update order: " + e.getMessage());
        }
    }
}