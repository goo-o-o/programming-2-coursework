package com.bryan.programming2coursework.page;

import com.bryan.programming2coursework.dao.MenuItemDAO;
import com.bryan.programming2coursework.dao.OrderDAO;
import com.bryan.programming2coursework.model.Order;
import com.bryan.programming2coursework.model.Order.OrderStatus;
import com.bryan.programming2coursework.model.OrderItem;
import com.bryan.programming2coursework.util.ViewSwitcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin reports page with sales analytics and filtering
 */
public class AdminReportsPage extends VBox {
    
    private OrderDAO orderDAO;
    private MenuItemDAO menuItemDAO;
    
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> reportTypeCombo;
    
    private VBox reportsContainer;
    
    public AdminReportsPage() {
        this.orderDAO = OrderDAO.getInstance();
        this.menuItemDAO = MenuItemDAO.getInstance();
        initializeUI();
        generateReport();
    }
    
    private void initializeUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(20));
        
        HBox header = createHeader();
        HBox controls = createControls();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        reportsContainer = new VBox(20);
        reportsContainer.setPadding(new Insets(10));
        scrollPane.setContent(reportsContainer);
        
        this.getChildren().addAll(header, controls, scrollPane);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> ViewSwitcher.switchTo(new AdminDashboardPage()));
        
        Label title = new Label("Sales Reports & Analytics");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        header.getChildren().addAll(backBtn, title);
        return header;
    }
    
    private HBox createControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));
        controls.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll(
            "Summary Report",
            "Sales by Category",
            "Sales by Date",
            "Top Selling Items",
            "All Reports"
        );
        reportTypeCombo.setValue("All Reports");
        
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        endDatePicker = new DatePicker(LocalDate.now());
        
        Button generateBtn = new Button("Generate Report");
        generateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        generateBtn.setOnAction(e -> generateReport());
        
        Button exportBtn = new Button("Export to Text");
        exportBtn.setOnAction(e -> exportReport());
        
        controls.getChildren().addAll(
            new Label("Report Type:"), reportTypeCombo,
            new Label("From:"), startDatePicker,
            new Label("To:"), endDatePicker,
            generateBtn, exportBtn
        );
        
        return controls;
    }
    
    private void generateReport() {
        reportsContainer.getChildren().clear();
        
        String reportType = reportTypeCombo.getValue();
        
        if ("All Reports".equals(reportType) || "Summary Report".equals(reportType)) {
            reportsContainer.getChildren().add(createSummaryReport());
        }
        
        if ("All Reports".equals(reportType) || "Sales by Category".equals(reportType)) {
            reportsContainer.getChildren().add(createSalesByCategoryReport());
        }
        
        if ("All Reports".equals(reportType) || "Sales by Date".equals(reportType)) {
            reportsContainer.getChildren().add(createSalesByDateReport());
        }
        
        if ("All Reports".equals(reportType) || "Top Selling Items".equals(reportType)) {
            reportsContainer.getChildren().add(createTopSellingItemsReport());
        }
    }
    
    private VBox createSummaryReport() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label title = new Label("📊 Summary Report");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        List<Order> orders = getFilteredOrders();
        List<Order> completedOrders = orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
            .collect(Collectors.toList());
        
        double totalSales = completedOrders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        
        double avgOrderValue = completedOrders.isEmpty() ? 0 : 
            totalSales / completedOrders.size();
        
        int totalOrders = orders.size();
        int completedCount = completedOrders.size();
        int pendingCount = (int) orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING)
            .count();
        int cancelledCount = (int) orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
            .count();
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        
        int row = 0;
        addMetric(grid, row++, "Total Sales (Completed):", String.format("RM %.2f", totalSales));
        addMetric(grid, row++, "Total Orders:", String.valueOf(totalOrders));
        addMetric(grid, row++, "Completed Orders:", String.valueOf(completedCount));
        addMetric(grid, row++, "Pending Orders:", String.valueOf(pendingCount));
        addMetric(grid, row++, "Cancelled Orders:", String.valueOf(cancelledCount));
        addMetric(grid, row++, "Average Order Value:", String.format("RM %.2f", avgOrderValue));
        
        section.getChildren().addAll(title, new Separator(), grid);
        return section;
    }
    
    private void addMetric(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label valueNode = new Label(value);
        valueNode.setFont(Font.font("System", 14));
        valueNode.setStyle("-fx-text-fill: #2196F3;");
        
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
    
    private VBox createSalesByCategoryReport() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label title = new Label("📈 Sales by Category");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        List<Order> orders = getFilteredOrders().stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
            .collect(Collectors.toList());
        
        Map<String, Double> categoryTotals = new HashMap<>();
        Map<String, Integer> categoryQuantities = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String category = item.getMenuItem().getCategory();
                categoryTotals.merge(category, item.getSubtotal(), Double::sum);
                categoryQuantities.merge(category, item.getQuantity(), Integer::sum);
            }
        }
        
        TableView<CategorySales> table = new TableView<>();
        
        TableColumn<CategorySales, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);
        
        TableColumn<CategorySales, Integer> quantityCol = new TableColumn<>("Items Sold");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(100);
        
        TableColumn<CategorySales, Double> salesCol = new TableColumn<>("Total Sales (RM)");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("sales"));
        salesCol.setPrefWidth(150);
        
        table.getColumns().addAll(categoryCol, quantityCol, salesCol);
        
        ObservableList<CategorySales> data = FXCollections.observableArrayList();
        categoryTotals.forEach((cat, sales) -> {
            data.add(new CategorySales(cat, categoryQuantities.get(cat), sales));
        });
        
        data.sort((a, b) -> Double.compare(b.getSales(), a.getSales()));
        table.setItems(data);
        
        // Bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Sales by Category");
        chart.setPrefHeight(300);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        
        data.forEach(cs -> series.getData().add(new XYChart.Data<>(cs.getCategory(), cs.getSales())));
        chart.getData().add(series);
        
        section.getChildren().addAll(title, new Separator(), chart, table);
        return section;
    }
    
    private VBox createSalesByDateReport() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label title = new Label("📅 Sales by Date");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        List<Order> orders = getFilteredOrders().stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
            .collect(Collectors.toList());
        
        Map<LocalDate, Double> dateSales = new TreeMap<>();
        Map<LocalDate, Integer> dateOrders = new TreeMap<>();
        
        for (Order order : orders) {
            LocalDate date = order.getOrderDate().toLocalDate();
            dateSales.merge(date, order.getTotalAmount(), Double::sum);
            dateOrders.merge(date, 1, Integer::sum);
        }
        
        TableView<DateSales> table = new TableView<>();
        
        TableColumn<DateSales, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(150);
        
        TableColumn<DateSales, Integer> ordersCol = new TableColumn<>("Orders");
        ordersCol.setCellValueFactory(new PropertyValueFactory<>("orderCount"));
        ordersCol.setPrefWidth(100);
        
        TableColumn<DateSales, Double> salesCol = new TableColumn<>("Total Sales (RM)");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("sales"));
        salesCol.setPrefWidth(150);
        
        table.getColumns().addAll(dateCol, ordersCol, salesCol);
        
        ObservableList<DateSales> data = FXCollections.observableArrayList();
        dateSales.forEach((date, sales) -> {
            data.add(new DateSales(date.toString(), dateOrders.get(date), sales));
        });
        
        table.setItems(data);
        
        section.getChildren().addAll(title, new Separator(), table);
        return section;
    }
    
    private VBox createTopSellingItemsReport() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label title = new Label("🏆 Top Selling Items");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        List<Order> orders = getFilteredOrders().stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
            .collect(Collectors.toList());
        
        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, Double> itemSales = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String itemName = item.getMenuItem().getName();
                itemQuantities.merge(itemName, item.getQuantity(), Integer::sum);
                itemSales.merge(itemName, item.getSubtotal(), Double::sum);
            }
        }
        
        TableView<ItemSales> table = new TableView<>();
        
        TableColumn<ItemSales, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemCol.setPrefWidth(200);
        
        TableColumn<ItemSales, Integer> quantityCol = new TableColumn<>("Quantity Sold");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        quantityCol.setPrefWidth(120);
        
        TableColumn<ItemSales, Double> salesCol = new TableColumn<>("Total Sales (RM)");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        salesCol.setPrefWidth(150);
        
        table.getColumns().addAll(itemCol, quantityCol, salesCol);
        
        ObservableList<ItemSales> data = FXCollections.observableArrayList();
        itemQuantities.forEach((item, qty) -> {
            data.add(new ItemSales(item, qty, itemSales.get(item)));
        });
        
        data.sort((a, b) -> Integer.compare(b.getQuantitySold(), a.getQuantitySold()));
        table.setItems(data);
        
        section.getChildren().addAll(title, new Separator(), table);
        return section;
    }
    
    private List<Order> getFilteredOrders() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        return orderDAO.findByDateRange(start, end);
    }
    
    private void exportReport() {
        StringBuilder export = new StringBuilder();
        
        export.append("═══════════════════════════════════════\n");
        export.append("       McRonald's Sales Report\n");
        export.append("═══════════════════════════════════════\n");
        export.append("Generated: ").append(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        export.append("Period: ").append(startDatePicker.getValue())
              .append(" to ").append(endDatePicker.getValue()).append("\n");
        export.append("═══════════════════════════════════════\n\n");
        
        export.append(generateTextReport());
        
        TextArea textArea = new TextArea(export.toString());
        textArea.setEditable(false);
        textArea.setPrefSize(600, 400);
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Export Report");
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }
    
    private String generateTextReport() {
        StringBuilder report = new StringBuilder();
        
        List<Order> orders = getFilteredOrders();
        List<Order> completed = orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
            .collect(Collectors.toList());
        
        double total = completed.stream().mapToDouble(Order::getTotalAmount).sum();
        
        report.append("SUMMARY:\n");
        report.append(String.format("Total Orders: %d\n", orders.size()));
        report.append(String.format("Completed Orders: %d\n", completed.size()));
        report.append(String.format("Total Sales: RM %.2f\n", total));
        report.append(String.format("Average Order: RM %.2f\n\n", 
            completed.isEmpty() ? 0 : total / completed.size()));
        
        return report.toString();
    }
    
    // Helper classes for TableView
    public static class CategorySales {
        private String category;
        private Integer quantity;
        private Double sales;
        
        public CategorySales(String category, Integer quantity, Double sales) {
            this.category = category;
            this.quantity = quantity;
            this.sales = sales;
        }
        
        public String getCategory() { return category; }
        public Integer getQuantity() { return quantity; }
        public Double getSales() { return sales; }
    }
    
    public static class DateSales {
        private String date;
        private Integer orderCount;
        private Double sales;
        
        public DateSales(String date, Integer orderCount, Double sales) {
            this.date = date;
            this.orderCount = orderCount;
            this.sales = sales;
        }
        
        public String getDate() { return date; }
        public Integer getOrderCount() { return orderCount; }
        public Double getSales() { return sales; }
    }
    
    public static class ItemSales {
        private String itemName;
        private Integer quantitySold;
        private Double totalSales;
        
        public ItemSales(String itemName, Integer quantitySold, Double totalSales) {
            this.itemName = itemName;
            this.quantitySold = quantitySold;
            this.totalSales = totalSales;
        }
        
        public String getItemName() { return itemName; }
        public Integer getQuantitySold() { return quantitySold; }
        public Double getTotalSales() { return totalSales; }
    }
}