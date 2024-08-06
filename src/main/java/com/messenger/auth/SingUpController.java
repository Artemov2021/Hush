package com.messenger.auth;

import com.messenger.design.AuthErrorsDesign;
import com.messenger.design.AuthField;
import com.messenger.main.MainWindowController;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.messenger.main.MainWindow;


public class SingUpController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField emailField;
    @FXML
    private Label upperLabel;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label lowerLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Button accountButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label extraLabel;

    private Group passwordGroup;

    private final String sql = "jdbc:sqlite:auth.db";

    public void initialize() {
        createDB();
        countUsers();
        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);
        //Platform.setImplicitExit(false);

        // group of all password field elements ( will be moved down, if email is invalid )
        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);

        // makes label animation and solves unnecessary spaces
        var emailFieldStyled = new AuthField(emailField, upperLabel);
        emailFieldStyled.setLabelChanges(-26, -2);
        emailFieldStyled.setLabelMovePath(-5, -24);
        emailFieldStyled.setStyle();

        var passwordFieldStyled = new AuthField(passwordField, lowerLabel);
        passwordFieldStyled.setLabelChanges(-11, -2);
        passwordFieldStyled.setLabelMovePath(-5, -24);
        passwordFieldStyled.setStyle();
    }

    public void singUp(ActionEvent e) throws InterruptedException {
        String email = emailField.getText();
        String password = passwordField.getText().trim();
        int emailLength = email.length();

        if (!email.isEmpty() && emailLength <= 25 && !password.isEmpty()) {     // if the info is valid
            setProgressBar(() -> {
                try {
                    if (email.contains("@gmail.com")) {
                        if (registerEmail(email, password)) {
                            AuthErrorsDesign.deleteErrorStyle(emailErrorLabel, emailField);
                            AuthErrorsDesign.deleteErrorStyle(passwordErrorLabel, passwordField);
                            passwordGroup.setLayoutY(0);
                            ((Stage) (anchorPane.getScene().getWindow())).close();
                            AuthMainWindow.openMainWindow(email);
                        }
                    } else {
                        if (registerName(email, password)) {
                            AuthErrorsDesign.deleteErrorStyle(emailErrorLabel, emailField);
                            AuthErrorsDesign.deleteErrorStyle(passwordErrorLabel, passwordField);
                            passwordGroup.setLayoutY(0);
                            ((Stage) (anchorPane.getScene().getWindow())).close();
                            AuthMainWindow.openMainWindow(email);
                        }
                    }
                } catch (SQLException dbIssue) {
                    extraLabel.setText(dbIssue.getMessage());
                }
            });
        }

        // if info is invalid:

        if (email.isEmpty()) {
            AuthErrorsDesign.setErrorStyle(emailErrorLabel, "incorrect information", emailField);
            passwordGroup.setLayoutY(16); // moves password field 16px down, to show email error message
        } else if (email.length() > 25) {
            AuthErrorsDesign.setErrorStyle(emailErrorLabel, "email or name is too long", emailField);
            passwordGroup.setLayoutY(16);
        } else {
            AuthErrorsDesign.deleteErrorStyle(emailErrorLabel, emailField);
            passwordGroup.setLayoutY(0);
        }

        if (password.isEmpty()) {
            AuthErrorsDesign.setErrorStyle(passwordErrorLabel, "incorrect information", passwordField);
        } else {
            AuthErrorsDesign.deleteErrorStyle(passwordErrorLabel, passwordField);
        }
    }

    public void oldAccount(ActionEvent e) {
        setProgressBar(() -> {
            ((Stage) (anchorPane.getScene().getWindow())).close();
            try {
                openLogInWindow();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

    }

    private void openLogInWindow() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/auth/LogIn.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Log In");
        stage.show();
    }

    private void setProgressBar(Runnable function) {
        progressBar.setProgress(0.15);
        PauseTransition pause1 = new PauseTransition(Duration.millis(500));
        pause1.setOnFinished(event1 -> {
            progressBar.setProgress(1);
            PauseTransition pause2 = new PauseTransition(Duration.millis(300));
            pause2.setOnFinished(event2 -> {
                function.run();
            });
            pause2.play();
        });
        pause1.play();
    }

    private void createDB() {
        String statement = "CREATE TABLE IF NOT EXISTS users(id integer PRIMARY KEY, name text, email text, password text, contacts integer)";
        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            extraLabel.setText(e.getMessage());
        }
    }

    private boolean registerEmail(String email, String password) throws SQLException {
        String statement = "INSERT INTO users (email,password,name,contacts) VALUES (?,?,?,?)";
        if (!(checkEmailPresence(email))) {
            try (var conn = DriverManager.getConnection(sql)) {
                var stmt = conn.prepareStatement(statement);
                stmt.setString(1, email);
                stmt.setString(2, password);
                stmt.setString(3, "User" + (countUsers() + 1));
                stmt.setInt(4, 0);
                stmt.executeUpdate();
                return true;
            }
        }
        AuthErrorsDesign.setErrorStyle(emailErrorLabel, "The email is aldready taken", emailField);
        passwordGroup.setLayoutY(16);
        return false;
    }

    private boolean registerName(String name, String password) throws SQLException {
        String statement = "INSERT INTO users (name,password,contacts) VALUES (?,?,?)";
        if (!(checkNamePresence(name))) {
            try (var conn = DriverManager.getConnection(sql)) {
                var stmt = conn.prepareStatement(statement);
                stmt.setString(1, name);
                stmt.setString(2, password);
                stmt.setInt(3, 0);
                stmt.executeUpdate();
                return true;
            }
        }
        AuthErrorsDesign.setErrorStyle(emailErrorLabel, "The name is aldready taken", emailField);
        passwordGroup.setLayoutY(16);
        return false;
    }

    private int countUsers() {
        String statement = "SELECT COUNT(*) FROM users";
        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(statement);
            return result.getInt(1);
        } catch (SQLException exception) {
            extraLabel.setText(exception.getMessage());
        }
        return -1;
    }

    private boolean checkNamePresence(String name) {
        String statement = "SELECT name FROM users WHERE name = ?";
        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException exception) {
            extraLabel.setText(exception.getMessage());
        }
        return false;
    }

    private boolean checkEmailPresence(String email) {
        String statement = "SELECT email FROM users WHERE email = ?";
        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException exception) {
            extraLabel.setText(exception.getMessage());
        }
        return false;
    }


}
