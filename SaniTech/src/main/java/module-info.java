module com.example.sanitech {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.desktop;
    requires fontawesomefx;
    requires java.sql;

    opens com.example.sanitech to javafx.fxml;
    exports com.example.sanitech;
}