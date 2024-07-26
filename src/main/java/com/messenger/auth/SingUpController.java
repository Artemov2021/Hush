package com.messenger.auth;

import com.messenger.design.LabelAnimation;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import javafx.event.ActionEvent;


public class SingUpController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button bottomButton;
    @FXML
    private Label upperLabel;
    @FXML
    private Label lowerLabel;

    private String email;
    private String password;

    public void initialize() {
        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        bottomButton.setUnderline(true);

        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && emailField.getText().isEmpty()) {
                LabelAnimation.moveUp(upperLabel,-25,-2,-5,-25);
            } else if (emailField.getText().trim().isEmpty() && !newValue) {
                emailField.setText("");
                LabelAnimation.moveDown(upperLabel,25,2,5,25);
            }
        });

        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && passwordField.getText().isEmpty()) {
                LabelAnimation.moveUp(lowerLabel,-14,-4,-5,-22);
            } else if (passwordField.getText().trim().isEmpty() && !newValue) {
                passwordField.setText("");
                LabelAnimation.moveDown(lowerLabel,14,4,5,22);
            }
        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                passwordField.setText("");
            }
        });
    }

    public void singUp(ActionEvent e) {
        email = emailField.getText().trim();
        password = passwordField.getText().trim();

        System.out.println("Singed up! \nEmail: " + email + "\nPassword: " + password );
    }
}
