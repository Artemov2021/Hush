package com.messenger.auth;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthLogInWindow {
    public static void openLogInWindow(Stage oldWindow) throws IOException {
        oldWindow.close();

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(AuthLogInWindow.class.getResource("/auth/LogIn.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Log In");
        stage.show();
    }
}
