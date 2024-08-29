package com.messenger.auth;

import com.messenger.Log;
import com.messenger.database.DetailedDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.main.MainWindowController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthMainWindow {
    public static void openMainWindow(String info) throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(AuthMainWindow.class.getResource("/main/MainWindow.fxml"));
        Parent root = loader.load();

        // Retrieve the controller and set the email
        MainWindowController mainWindowController = loader.getController();
        if (isEmailOrName(info).equals("email")) {
            mainWindowController.setEmail(info);
            DetailedDataBase.createUserDataBase(UsersDataBase.getNameWithEmail(info));
        } else {
            mainWindowController.setName(info);
            DetailedDataBase.createUserDataBase(info);
        }

        // Set up the new stage
        Scene scene = new Scene(root);
        Stage newStage = new Stage();
        newStage.setResizable(false);
        newStage.setScene(scene);
        newStage.setTitle("Main");
        newStage.show();

    }

    private static String isEmailOrName(String identifier) {
        String emailPattern = "@\\S*\\.[a-z]{2,}$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher emailMatcher = pattern.matcher(identifier);
        if (emailMatcher.find()) {
            return "email";
        }
        return "name";
    }
}
