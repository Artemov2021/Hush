package com.messenger.auth;

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
        if (getIdentifierType(info).equals("email")) {
            mainWindowController.setId(UsersDataBase.getIdWithEmail(info));
            DetailedDataBase.createUserDataBase(UsersDataBase.getIdWithEmail(info));
        } else {
            mainWindowController.setId(UsersDataBase.getIdWithName(info));
            System.out.println(UsersDataBase.getIdWithName(info));
            DetailedDataBase.createUserDataBase(UsersDataBase.getIdWithName(info));
        }

        // Set up the new stage
        Scene scene = new Scene(root);
        Stage newStage = new Stage();
        newStage.setResizable(false);
        newStage.setScene(scene);
        newStage.setTitle("Main");
        newStage.show();

    }

    private static String getIdentifierType(String identifier) {
        String emailPattern = "^.+@\\S*\\.[a-z]{2,}$";
        Pattern emailPatternCompile = Pattern.compile(emailPattern);
        Matcher emailMatcher = emailPatternCompile.matcher(identifier);

        String namePattern = "^[a-zA-Z][a-zA-Z0-9 ]+$";
        Pattern namePatternCompile = Pattern.compile(namePattern);
        Matcher nameMatcher = namePatternCompile.matcher(identifier);

        if (emailMatcher.find()) {
            return "email";
        } else if (nameMatcher.find()) {
            return "name";
        } else {
            return "-";
        }
    }
}
