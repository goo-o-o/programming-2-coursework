package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.MenuItemDAO;
import com.bryan.programming2coursework.dao.OrderDAO;
import com.bryan.programming2coursework.model.MenuItem;
import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.Order.OrderStatus;
import com.bryan.programming2coursework.model.OrderItem;
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
 * Page for customers to view and manage their orders
 */
public class CustomerOrdersPage extends VBox {

    private OrderDAO orderDAO;
    private TableView<Order> ordersTable;
    private TextArea orderDetailsArea;

    public CustomerOrdersPage() {
        this.orderDAO = OrderDAO.getInstance();
        initializeUI();
        loadOrders();
    }

    private void initializeUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(20));

        // Header
        HBox header = createHeader();

        // Main content
        HBox mainContent = new HBox(15);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // Left - Orders table
        VBox ordersSection = createOrdersSection();
        HBox.setHgrow(ordersSection, Priority.ALWAYS);

        // Right - Order details
        VBox detailsSection = createDetailsSection();
        detailsSection.setPrefWidth(350);

        mainContent.getChildren().addAll(ordersSection, detailsSection);

        this.getChildren().addAll(header, mainContent);
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));

        Button backBtn = new Button("← Back to Menu");
        backBtn.setOnAction(e -> ViewSwitcher.switchTo(new CustomerDashboardPage()));

        Label title = new Label("My Orders");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadOrders());

        header.getChildren().addAll(backBtn, title, spacer, refreshBtn);
        return header;
    }

    private VBox createOrdersSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        Label label = new Label("Order History");
        label.setFont(Font.font("System", FontWeight.BOLD, 18));

        ordersTable = new TableView<>();
        VBox.setVgrow(ordersTable, Priority.ALWAYS);

        TableColumn<Order, Integer> idCol = new TableColumn<>("Order #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFormattedOrderDate()));
        dateCol.setPrefWidth(150);

        TableColumn<Order, Integer> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getTotalItems()));
        itemsCol.setPrefWidth(80);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total (RM)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setPrefWidth(100);

        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<Order, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox buttons = new HBox(5, viewBtn, cancelBtn);

            {
                viewBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDetails(order);
                });

                cancelBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    cancelOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    // Only allow cancel for pending orders
                    cancelBtn.setDisable(order.getStatus() != OrderStatus.PENDING);
                    setGraphic(buttons);
                }
            }
        });

        ordersTable.getColumns().addAll(idCol, dateCol, itemsCol, totalCol, statusCol, actionCol);

        // select the first item to show details
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                showOrderDetails(newVal);
            }
        });

        section.getChildren().addAll(label, ordersTable);
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
        orderDetailsArea.setPromptText("Select an order to view details");
        VBox.setVgrow(orderDetailsArea, Priority.ALWAYS);

        section.getChildren().addAll(label, orderDetailsArea);
        return section;
    }

    private void loadOrders() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        List<Order> orders = orderDAO.findByUserId(userId);

        // sort newest first
        orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));

        ordersTable.setItems(FXCollections.observableArrayList(orders));

        // auto-select first order
        if (!orders.isEmpty()) {
            ordersTable.getSelectionModel().selectFirst();
        }
    }

    // I'll probably implement a nicer looking receipt in the future
    private void showOrderDetails(Order order) {
        StringBuilder details = new StringBuilder();

        details.append("═══════════════════════════════\n");
        details.append("   McRonald's - Order Receipt   \n");
        details.append("═══════════════════════════════\n\n");

        details.append("Order ID: #").append(order.getId()).append("\n");
        details.append("Date: ").append(order.getFormattedOrderDate()).append("\n");
        details.append("Customer: ").append(order.getCustomerName()).append("\n");
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

    private void cancelOrder(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            Utils.showError("Cannot Cancel", "Only pending orders can be cancelled");
            return;
        }

        if (Utils.showConfirmation("Cancel Order",
                "Are you sure you want to cancel Order #" + order.getId() + "?")) {

            try {
                order.setStatus(OrderStatus.CANCELLED);
                orderDAO.update(order);

                Utils.showInfo("Order Cancelled", "Order #" + order.getId() + " has been cancelled");
                loadOrders();

                // almost forgot, we need to revert the stocks for each of the items
                for (OrderItem item : order.getItems()) {
                    MenuItem menuItem = item.getMenuItem();
                    menuItem.increaseStock(item.getQuantity());
                    MenuItemDAO.getInstance().update(menuItem, menuItem);
                }

            } catch (Exception e) {
                Utils.showError("Error", "Failed to cancel order: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}