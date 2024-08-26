package com.messenger.auth;

import com.messenger.Log;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SingUp extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // the beginning line
        Log.writeNewActionLog(String.format("%0" + 65 + "d" + "\n",0).replace("0","-"));

        createDB();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/SingUp.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sing Up");
        primaryStage.show();

        Log.writeNewActionLog("Sing Up window: opened\n");

    }

    private void createDB() throws IOException {
        String sql = "jdbc:sqlite:auth.db";
        String statement = "CREATE TABLE IF NOT EXISTS users(id integer PRIMARY KEY, name text, email text, password text, phone_number text, contacts_amount integer)";

        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            Log.writeNewExceptionLog(e);
        }
        Log.writeNewActionLog("The table \"users\" inside \"auth.db\" was created ( if not existed )\n");
    }
}
