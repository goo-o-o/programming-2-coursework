package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.MenuItemDAO;
import com.bryan.programming2coursework.dao.OrderDAO;
import com.bryan.programming2coursework.dao.UserDAO;
import com.bryan.programming2coursework.model.MenuItem;
import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.OrderItem;
import com.bryan.programming2coursework.util.Constants;
import com.bryan.programming2coursework.util.Utils;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminDashboardPage extends VBox {
    private MenuItemDAO menuItemDAO = MenuItemDAO.getInstance();
    private OrderDAO orderDAO = OrderDAO.getInstance();
    private TableView<MenuItem> inventoryTable;
    TabPane tabPane;

    public AdminDashboardPage() {
        this.setPadding(new Insets(30));
        this.setSpacing(20);
        this.setStyle("-fx-background-color: " + Constants.CREAM);

        HBox headerBox = new HBox();
        Label header = new Label("Admin Management System");
        header.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: gray;");
        Button profileBtn = new Button();
        profileBtn.setGraphic(Utils.getSVG(Constants.USER));
        profileBtn.setOnMouseClicked(event -> ViewSwitcher.switchTo(new UserProfilePage()));
        profileBtn.setBackground(Background.EMPTY);
        profileBtn.setBorder(Border.EMPTY);
        profileBtn.setCursor(javafx.scene.Cursor.HAND);

        headerBox.getChildren().addAll(header, Utils.getHorizontalSpacer(), profileBtn);

        tabPane = new TabPane();
        // let it all grow
        this.setMaxHeight(Double.MAX_VALUE);
        this.setFillWidth(true);
        tabPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        tabPane.getTabs().addAll(createInventoryTab(), createOrderManagementTab(), createReportsTab());

        VBox.setVgrow(tabPane, Priority.ALWAYS);

        this.getChildren().addAll(headerBox, tabPane);
    }

    private Tab createOrderManagementTab() {
        Tab tab = new Tab("Order Management");
        tab.setClosable(false);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        TableView<Order> orderTable = new TableView<>();
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(orderTable, Priority.ALWAYS);

        TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Order, String> nameCol = new TableColumn<>("Customer");
        nameCol.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUserId();
            String customerInfo = userId + " | " + UserDAO.getInstance().getNameById(userId);
            return new SimpleStringProperty(customerInfo);
        });

        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getOrderDate();
            if (date == null) return new SimpleStringProperty("");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            return new SimpleStringProperty(date.format(formatter));
        });

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total (RM)");
        totalCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalPrice()));


        TableColumn<Order, Void> detailsCol = new TableColumn<>("Details");

        detailsCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View Items");

            {
                btn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDetailsDialog(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });


        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));


        // update status
        TableColumn<Order, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Update Status");

            {
                btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showUpdateStatusDialog(order, orderTable);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn); // set graphic if not empty
            }
        });


        orderTable.getColumns().addAll(idCol, nameCol, dateCol, totalCol, detailsCol, statusCol, actionCol);

        // initial load
        orderTable.setItems(FXCollections.observableArrayList(orderDAO.getAllOrders()));

        layout.getChildren().addAll(new Label("Customer Orders"), orderTable);
        tab.setContent(layout);
        return tab;
    }

    private void showOrderDetailsDialog(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Details - #" + order.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(300);

        Label header = new Label("Items for Order #" + order.getId());
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        content.getChildren().add(header);

        for (OrderItem item : order.getItems()) {
            HBox row = new HBox();
            row.setSpacing(10);

            Label nameQty = new Label(item.getMenuItem().getName() + " x" + item.getQuantity());
            Region spacer = Utils.getHorizontalSpacer();
            Label price = new Label(String.format("RM %.2f", item.getSubtotal()));

            row.getChildren().addAll(nameQty, spacer, price);
            content.getChildren().add(row);
        }

        content.getChildren().add(new Separator());

        Label totalLabel = new Label("Total: RM " + String.format("%.2f", order.getTotalPrice()));
        totalLabel.setStyle("-fx-font-weight: bold;");
        totalLabel.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().add(totalLabel);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showUpdateStatusDialog(Order order, TableView<Order> table) {
        // convert enum to string
        List<String> statusOptions = Arrays.stream(Order.OrderStatus.values())
                .map(Enum::name)
                .toList();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(order.getStatus().name(), statusOptions);
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Update status for Order #" + order.getId());
        dialog.setContentText("Choose status:");

        dialog.showAndWait().ifPresent(newStatusName -> {
            // back to enum
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(newStatusName);

            order.setStatus(newStatus);
            orderDAO.updateStatus(order.getId(), newStatus);
            table.refresh();
            refreshReports();
            // also should refresh the sales report
        });
    }

    private void refreshReports() {
        // easiest would just be to regenerate the entire tab
        Tab reportsTab = tabPane.getTabs().stream()
                .filter(t -> t.getText().equals("Sales Reports")) // get only the sales report
                .findFirst().orElse(null);

        if (reportsTab != null) {
            reportsTab.setContent(createReportsTab().getContent());
        }
    }

    //menu_item_id, total sold
    private Map<Integer, Integer> salesStats;

    private Tab createInventoryTab() {
        Tab tab = new Tab("Menu Management");
        tab.setClosable(false);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        // crud
        HBox actionBar = new HBox(10);
        Button addBtn = new Button("Add New Item");
        Button editBtn = new Button("Edit Selected");
        Button deleteBtn = new Button("Delete Selected");

        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

        actionBar.getChildren().addAll(addBtn, editBtn, deleteBtn);

        inventoryTable = new TableView<>();
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
        TableColumn<MenuItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<MenuItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<MenuItem, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        TableColumn<MenuItem, Integer> calCol = new TableColumn<>("Calories");
        calCol.setCellValueFactory(new PropertyValueFactory<>("calories"));

        TableColumn<MenuItem, Integer> soldCol = new TableColumn<>("Amount Sold");
        soldCol.setCellValueFactory(cellData -> {
            MenuItem item = cellData.getValue();
            int count = salesStats.getOrDefault(item.getId(), 0); // default to zero, because there will be nulls
            return new SimpleObjectProperty<>(count);
        });

        inventoryTable.getColumns().addAll(idCol, nameCol, priceCol, stockCol, calCol, soldCol);
        refreshInventoryTable();

        addBtn.setOnAction(e -> showItemForm(null));
        editBtn.setOnAction(e -> {
            MenuItem selected = inventoryTable.getSelectionModel().getSelectedItem();
            if (selected != null) showItemForm(selected);
        });
        deleteBtn.setOnAction(e -> handleDelete());

        layout.getChildren().addAll(actionBar, inventoryTable);
        tab.setContent(layout);
        return tab;
    }

    private Tab createReportsTab() {
        Tab tab = new Tab("Sales Reports");
        tab.setClosable(false);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true); // ensure stretch

        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #f4f4f4;");

        HBox metricsBox = new HBox(20);
        metricsBox.setAlignment(Pos.CENTER);
        metricsBox.getChildren().addAll(
                createMetricCard("Total Revenue", "RM " + String.format("%.2f", orderDAO.getTotalSales()), "#27ae60"),
                createMetricCard("Items Sold", String.valueOf(orderDAO.getTotalItemsSold()), "#2980b9"),
                createMetricCard("Orders Received", String.valueOf(orderDAO.getTotalOrdersCount()), "#8e44ad")
        );

        VBox chartsBox = new VBox(30);
        chartsBox.setAlignment(Pos.CENTER);

        // bar graph
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Top Selling Items");
        xAxis.setLabel("Item Name");
        yAxis.setLabel("Quantity Sold");

        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        orderDAO.getTopSellingItems(10).forEach((name, qty) -> {
            barSeries.getData().add(new XYChart.Data<>(name, qty));
        });
        barChart.getData().add(barSeries);
        barChart.setLegendVisible(false);
        barChart.setMinWidth(400);
        barChart.setPrefHeight(400);

        // line graph
        CategoryAxis lineXAxis = new CategoryAxis();
        NumberAxis lineYAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(lineXAxis, lineYAxis);
        lineChart.setTitle("Order Volume (Last 7 Days)");
        lineXAxis.setLabel("Date");
        lineYAxis.setLabel("Number of Orders");

        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.setName("Orders");
        orderDAO.getDailyOrderVolume().forEach((date, count) ->
                lineSeries.getData().add(new XYChart.Data<>(date, count))
        );
        lineChart.getData().add(lineSeries);
        lineChart.setMinWidth(800);
        lineChart.setPrefHeight(400);

        chartsBox.getChildren().addAll(barChart, lineChart);

        layout.getChildren().addAll(metricsBox, new Separator(), chartsBox);

        scrollPane.setContent(layout);
        tab.setContent(scrollPane);
        return tab;
    }

    private VBox createMetricCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 24px; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLbl, valueLbl);
        return card;
    }

    private void refreshInventoryTable() {
        inventoryTable.getItems().setAll(menuItemDAO.getItems(MenuItem.MenuCategory.ALL_CATEGORIES, "", null, true));
        salesStats = menuItemDAO.getSalesStats();
    }

    private void showItemForm(MenuItem item) {
        boolean isEdit = (item != null);
        Dialog<MenuItem> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Item" : "Add New Item");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(isEdit ? item.getName() : "");
        TextField priceField = new TextField(isEdit ? String.valueOf(item.getPrice()) : "");
        TextField stockField = new TextField(isEdit ? String.valueOf(item.getStockQuantity()) : "");
        TextField calField = new TextField(isEdit ? String.valueOf(item.getCalories()) : "");
        TextField descField = new TextField(isEdit ? item.getDescription() : "");
        TextField imgField = new TextField(isEdit ? item.getImageUrl() : "https://placehold.co/300/png");

        ComboBox<MenuItem.MenuCategory> catCombo = new ComboBox<>(FXCollections.observableArrayList(MenuItemDAO.getInstance().getAllCategories()));
        catCombo.setValue(isEdit ? item.getCategory() : MenuItem.MenuCategory.BURGERS);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catCombo, 1, 1);
        grid.add(new Label("Price (RM):"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Calories:"), 0, 4);
        grid.add(calField, 1, 4);
        grid.add(new Label("Description:"), 0, 5);
        grid.add(descField, 1, 5);
        grid.add(new Label("Image Path:"), 0, 6);
        grid.add(imgField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {

                if (nameField.getText().isEmpty() || nameField.getText().isBlank()) {
                    Utils.showError("Input Error", "Name cannot be empty");
                    return null;
                }

                try {
                    return new MenuItem(
                            Integer.parseInt(calField.getText()),
                            catCombo.getValue(),
                            descField.getText(),
                            isEdit ? item.getId() : 0, // id = 0 -> auto-increment by db
                            imgField.getText(),
                            nameField.getText(),
                            Double.parseDouble(priceField.getText()),
                            Integer.parseInt(stockField.getText())
                    );
                } catch (NumberFormatException e) {
                    Utils.showError("Input Error", "Invalid entries for Price, Stock, or Calories.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (isEdit) {
                menuItemDAO.update(result);
            } else {
                menuItemDAO.create(result);
            }
            refreshInventoryTable();
        });
    }

    private void handleDelete() {
        MenuItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null && Utils.showConfirmation("Delete", "Are you sure?")) {
            menuItemDAO.delete(selected);
            refreshInventoryTable();
        }
    }
}