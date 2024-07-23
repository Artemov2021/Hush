module com.main.messenger {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.messenger.auth to javafx.fxml;
    exports com.messenger.auth;
}