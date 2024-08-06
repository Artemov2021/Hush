package com.messenger.auth;

import com.messenger.design.AuthErrorsDesign;
import com.messenger.design.AuthField;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogInController {
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

    public void initialize () {
        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);

        // group of all password field elements ( will be moved down, if email is invalid )
        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);

        // makes label animation and solves unnecessary spaces
        var emailFieldStyled = new AuthField(emailField,upperLabel);
        emailFieldStyled.setLabelChanges(-26,-2);
        emailFieldStyled.setLabelMovePath(-5,-24);
        emailFieldStyled.setStyle();

        var passwordFieldStyled = new AuthField(passwordField,lowerLabel);
        passwordFieldStyled.setLabelChanges(-12,-4);
        passwordFieldStyled.setLabelMovePath(-5,-24);
        passwordFieldStyled.setStyle();
    }

    public void logIn (ActionEvent e) throws InterruptedException {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        double emailLength = email.length();
        double emailLengthLimit = 25;

        // If both fields have valid information
        if (!email.isEmpty() && (emailLength <= emailLengthLimit) && !password.isEmpty()) {
            checkUser(email,password);
        }

        // If one of both fields has invalid information
        if (email.isEmpty()) {
            AuthErrorsDesign.setErrorStyle(emailErrorLabel,"incorrect information",emailField);
            passwordGroup.setLayoutY(16); // moves password field 16px down, to show email error message
        } else if (email.length()>25 ) {
            AuthErrorsDesign.setErrorStyle(emailErrorLabel,"email or name is too long",emailField);
            passwordGroup.setLayoutY(16);
        } else {
            AuthErrorsDesign.deleteErrorStyle(emailErrorLabel,emailField);
            passwordGroup.setLayoutY(0);
        }

        if (password.isEmpty()) {
            AuthErrorsDesign.setErrorStyle( passwordErrorLabel,"incorrect information",passwordField);
        } else {
            AuthErrorsDesign.deleteErrorStyle(passwordErrorLabel,passwordField);
        }
    }

    public void newAccount (ActionEvent e) {
        setProgressBar(() -> {
            ((Stage) (anchorPane.getScene().getWindow())).close();
            try {
                openSingUpWindow();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void openSingUpWindow() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/auth/SingUp.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Log In");
        stage.show();
    }

    private void setProgressBar (Runnable function) {
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

    private void handleDBInputIssue(String authBlock) {
        Platform.runLater(() -> {
            switch (authBlock) {
                case "email":
                    AuthErrorsDesign.setErrorStyle(emailErrorLabel, "Incorrect email", emailField);
                    passwordGroup.setLayoutY(16);
                    break;
                case "name":
                    AuthErrorsDesign.setErrorStyle(emailErrorLabel, "Incorrect name", emailField);
                    passwordGroup.setLayoutY(16);
                    break;
                case "password":
                    AuthErrorsDesign.setErrorStyle(passwordErrorLabel, "Incorrect password", passwordField);
                    passwordGroup.setLayoutY(0);
                    break;
                case "valid":
                    AuthErrorsDesign.deleteErrorStyle(emailErrorLabel, emailField);
                    AuthErrorsDesign.deleteErrorStyle(passwordErrorLabel, passwordField);
                    passwordGroup.setLayoutY(0);
                    break;
            }
        });
    }

    private void checkUser(String email, String password) {
        String authType = email.contains("@gmail.com") ? "email" : "name";
        String statement = "SELECT " + authType + ", password FROM users WHERE " + authType + " IS NOT NULL";
        String sql = "jdbc:sqlite:auth.db";

        try (var conn = DriverManager.getConnection(sql);
             var stmt = conn.createStatement();
             ResultSet result = stmt.executeQuery(statement)) {

            boolean userFound = false;

            while (result.next()) {
                if (result.getString(authType).equals(email)) {
                    userFound = true;
                    if (result.getString("password").equals(password)) {
                        handleDBInputIssue("valid");
                        setProgressBar(() -> {
                            openMainWindow(email);
                        });
                        return;
                    } else {
                        handleDBInputIssue("password");
                        return;
                    }
                }
            }

            if (!userFound) {
                handleDBInputIssue(authType);
            }

        } catch (SQLException e) {
            extraLabel.setText(e.getMessage());
        }
    }

    private void openMainWindow (String email) {
        ((Stage) (anchorPane.getScene().getWindow())).close();
        AuthMainWindow.openMainWindow(email);
    }
}
