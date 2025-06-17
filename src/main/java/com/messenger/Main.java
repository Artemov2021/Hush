package com.messenger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

// The programm starts with the sing up window

public class Main extends Application {
    public static void main(String[] args) {
            launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/AuthSingUp.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sing Up");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/icon.png")));
        primaryStage.show();
    }

}
