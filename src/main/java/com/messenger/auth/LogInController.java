package com.messenger.auth;

import com.messenger.design.AuthErrorsDesign;
import javafx.animation.PauseTransition;
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

    private Group passwordGroup;

    public void initialize() {
        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);

        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);



//        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue && emailField.getText().isEmpty()) {
//                TextFieldDesign.moveUp(upperLabel,-25,-2,-5,-25);
//            } else if (emailField.getText().trim().isEmpty() && !newValue) {
//                emailField.setText("");
//                TextFieldDesign.moveDown(upperLabel,25,2,5,25);
//            } else {
//                emailField.setText(emailField.getText().trim());
//            }
//        });
//
//        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue && passwordField.getText().isEmpty()) {
//                TextFieldDesign.moveUp(lowerLabel,-5,-4,-5,-22);
//            } else if (passwordField.getText().trim().isEmpty() && !newValue) {
//                passwordField.setText("");
//                TextFieldDesign.moveDown(lowerLabel,5,4,5,22);
//            }
//        });
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                emailField.setText("");
            }
        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                passwordField.setText("");
            }
        });
    }

    public void logIn(ActionEvent e) throws InterruptedException {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        int emailLength = email.length();

        if (!email.isEmpty() && emailLength <= 25 && !password.isEmpty()) {
            setProgressBar(() -> {
                ((Stage) (anchorPane.getScene().getWindow())).close();
            });
        }

        if (email.isEmpty()) {
            AuthErrorsDesign.setErrorStyle(emailField, emailErrorLabel);
            passwordGroup.setLayoutY(16);
        } else if (email.length()>25 ) {
            AuthErrorsDesign.setErrorStyle(emailField, emailErrorLabel);
            emailErrorLabel.setText("email or name is too long");
            passwordGroup.setLayoutY(16);
        } else {
            AuthErrorsDesign.deleteErrorStyle(emailField, emailErrorLabel);
            passwordGroup.setLayoutY(0);
        }

        if (password.isEmpty()) {
            AuthErrorsDesign.setErrorStyle(passwordField, passwordErrorLabel);
        } else {
            AuthErrorsDesign.deleteErrorStyle(passwordField, passwordErrorLabel);
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
        stage.setTitle("Sing Up");
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
}
