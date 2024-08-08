package com.messenger.auth;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthSingUpWindow {
    public static void openSingUpWindow(Stage oldWindow) throws IOException {
        oldWindow.close();

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(AuthLogInWindow.class.getResource("/auth/SingUp.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Sing Up");
        stage.show();
    }
}
