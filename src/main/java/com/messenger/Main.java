package com.messenger;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.DataBaseConnectionPool;
import com.messenger.main.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

// The programm starts with the sing up window

public class Main extends Application {
    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");

        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        int savedUserId = prefs.getInt("user_id", -1); // default = -1 if not found

        FXMLLoader loader;
        if (savedUserId != -1) {
            loader = new FXMLLoader(getClass().getResource("/main/fxml/MainWindow.fxml"));
            Parent root = loader.load();

            MainWindowController controller = loader.getController();
            controller.setMainUserId(savedUserId);
            controller.initializeWithValue(); // now it's safe to load user dat

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setScene(scene);
            newStage.setTitle("Hush");
            if (ChatsDataBase.isThereUnreadMessages(savedUserId)) {
                newStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/iconNewMessages.png")));
            } else {
                newStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/icon.png")));
            }
            newStage.show();
        } else {
            loader = new FXMLLoader(getClass().getResource("/auth/AuthSingUp.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setResizable(false);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Sing Up");
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/icon.png")));
            primaryStage.show();
        }
    }

    @Override
    public void stop() {
        DataBaseConnectionPool.closePool(); // ✅ close the HikariCP pool here
    }
}
