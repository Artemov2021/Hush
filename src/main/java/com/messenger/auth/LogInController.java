package com.messenger.auth;

import com.messenger.design.AuthEmailField;
import com.messenger.design.AuthErrorsDesign;
import com.messenger.design.AuthPasswordField;
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
    private final String sql = "jdbc:sqlite:auth.db";

    public void initialize() {
        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);

        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);   // group of all password field elements ( will be moved, if email is invalid )
        anchorPane.getChildren().add(passwordGroup);

        var emailFieldStyled = new AuthEmailField(passwordField,upperLabel);          // makes label animation and solves unnecessary spaces
        emailFieldStyled.setStyle();

        AuthPasswordField passwordFieldLabelAnimation = new AuthPasswordField(-5,-22,-5,-4);
        passwordFieldLabelAnimation.setStyle(passwordField,lowerLabel);

    }

    public void logIn(ActionEvent e) throws InterruptedException {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        int emailLength = email.length();

        if (!email.isEmpty() && emailLength <= 25 && !password.isEmpty()) { // if the info is valid
            passwordGroup.setLayoutY(16); // Initially set the layout
            try {
                if (email.contains("@gmail.com")) {
                    String emailCheckResult = checkEmail(email, password);
                    System.out.println(emailCheckResult);

                    if (emailCheckResult.equals("valid")) {
                        setProgressBar(() -> {
                            ((Stage) (anchorPane.getScene().getWindow())).close();
                        });
                    } else if (emailCheckResult.equals("email")) {
                        Platform.runLater(() -> {
                            AuthErrorsDesign.setErrorStyle(emailErrorLabel, "Incorrect email address", emailField);
                            passwordGroup.setLayoutY(16);
                        });
                        System.out.println("Email error!");
                    } else if (emailCheckResult.equals("password")) {
                        Platform.runLater(() -> {
                            AuthErrorsDesign.setErrorStyle(passwordErrorLabel, "Incorrect password", passwordField);
                            passwordGroup.setLayoutY(0);
                        });
                    }
                }
            } catch (SQLException dbIssues) {
                extraLabel.setText(dbIssues.getMessage());
            }
        }




        // if info is invalid:

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
    public void newAccount(ActionEvent e) {
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

    private String checkEmail(String email, String password) throws SQLException {
        String statement = "SELECT email,password FROM users WHERE email IS NOT NULL";
        var conn = DriverManager.getConnection(sql);
        var stmt = conn.createStatement();
        stmt.execute(statement);
        ResultSet result = stmt.getResultSet();
        while (result.next()) {
            if (result.getString("email").equals(email)) {
                if (result.getString("password").equals(password)) {
                    return "valid";
                }
                return "password";  // password is invalid
            }
        }
        conn.close();
        return "email"; // email is invalid
    }
}
