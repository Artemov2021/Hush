package com.messenger.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;


public class MainWindow extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/fxml/MainWindow.fxml"));
        Scene scene = new Scene(loader.load());
        MainWindowController mainWindowController = loader.getController();
        mainWindowController.initializeWithValue();
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.setTitle("Main");
        stage.show();
    }
}
