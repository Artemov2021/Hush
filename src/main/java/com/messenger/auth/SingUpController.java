package com.messenger.auth;

import com.messenger.design.AuthEmailField;
import com.messenger.design.AuthErrorsDesign;
import com.messenger.design.AuthPasswordField;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


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
        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);

        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);   // group of all password field elements ( will be moved, if email is invalid )
        anchorPane.getChildren().add(passwordGroup);

        AuthEmailField.setStyle(emailField,upperLabel);            // makes label animation and solves unnecessary spaces
        AuthPasswordField passwordFieldLabelAnimation = new AuthPasswordField(-5,-22,-14,-4);
        passwordFieldLabelAnimation.setStyle(passwordField,lowerLabel);
    }

    public void singUp(ActionEvent e) throws InterruptedException {
        String email = emailField.getText();
        String password = passwordField.getText().trim();
        int emailLength = email.length();

        if (!email.isEmpty() && emailLength <= 25 && !password.isEmpty()) {     // if the info is valid
            setProgressBar(() -> {
                try {
                    if (email.contains("@gmail.com")) {
                        registerEmail(email, password);
                    } else {
                        registerName(email, password);
                    }
                    ((Stage) (anchorPane.getScene().getWindow())).close();
                } catch (SQLException dbIssue) {
                    extraLabel.setText(dbIssue.getMessage());
                }
            });
        }

        // if info is invalid:

        if (email.isEmpty()) {
            AuthErrorsDesign.setErrorStyle( emailErrorLabel,"incorrect information",emailField);
            passwordGroup.setLayoutY(16); // moves password field 16px down, to show email error message
        } else if (email.length()>25 ) {
            AuthErrorsDesign.setErrorStyle( emailErrorLabel,"email or name is too long",emailField);
            passwordGroup.setLayoutY(16);
        } else {
            AuthErrorsDesign.deleteErrorStyle(emailErrorLabel,emailField);
            passwordGroup.setLayoutY(0);
        }

        if (password.isEmpty()) {
            AuthErrorsDesign.setErrorStyle(passwordErrorLabel, "incorrect information",passwordField);
        } else {
            AuthErrorsDesign.deleteErrorStyle( passwordErrorLabel,passwordField);
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
        String statement = "CREATE TABLE IF NOT EXISTS users(id integer PRIMARY KEY, name text, email text, password text)";
        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            extraLabel.setText(e.getMessage());
        }
    }

    private void registerEmail(String email, String password) throws SQLException {
        String statement = "INSERT INTO users (email,password) VALUES (?,?)";
        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1,email);
            stmt.setString(2,password);
            stmt.executeUpdate();
        }
    }
    private void registerName(String name, String password) throws SQLException {
        String statement = "INSERT INTO users (name,password) VALUES (?,?)";
        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1,name);
            stmt.setString(2,password);
            stmt.executeUpdate();
        }
    }
}
