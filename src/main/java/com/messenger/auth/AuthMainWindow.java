package com.messenger.auth;

import com.messenger.main.MainWindowController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuthMainWindow {
    public static void openMainWindow(String info) {
        try {
            // Load the FXML file and create the root
            FXMLLoader loader = new FXMLLoader(AuthMainWindow.class.getResource("/main/MainWindow.fxml"));
            Parent root = loader.load();  // Load the FXML first to get the root

            // Retrieve the controller and set the email
            MainWindowController mainWindowController = loader.getController();

            if (info.contains("@gmail.com")) {
                mainWindowController.setEmail(info);
                System.out.println("Setting email!");
            } else {
                mainWindowController.setName(info);
                System.out.println("Setting name!");
            }

            // Set up the new stage
            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setScene(scene);
            newStage.setTitle("Main");
            newStage.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
