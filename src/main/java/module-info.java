module com.main.messenger {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;


    opens com.messenger.auth to javafx.fxml;
    exports com.messenger.auth;

    opens com.messenger.main to javafx.fxml;
    exports com.messenger.main;

    opens com.messenger to javafx.graphics;

}