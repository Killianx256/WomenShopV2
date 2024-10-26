module com.example.womenshop.womenshopv2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;
    requires jdk.jfr;

    opens com.example.womenshop.womenshopv2 to javafx.fxml;
    exports com.example.womenshop.womenshopv2;
}