module com.bryan.programming2coursework {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.bryan.programming2coursework to javafx.fxml;
    exports com.bryan.programming2coursework;
    exports com.bryan.programming2coursework.component;
    opens com.bryan.programming2coursework.component to javafx.fxml;
}