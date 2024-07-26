module com.main.messenger {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.messenger.auth to javafx.fxml;
    exports com.messenger.auth;
}