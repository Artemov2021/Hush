package com.messenger.auth;

import com.messenger.design.LabelAnimation;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.event.ActionEvent;
import javafx.stage.Stage;


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

    private Group passwordGroup;

    private String email;
    private String password;

    public void initialize() {
        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);

        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);

        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && emailField.getText().isEmpty()) {
                LabelAnimation.moveUp(upperLabel,-25,-2,-5,-25);
            } else if (emailField.getText().trim().isEmpty() && !newValue) {
                emailField.setText("");
                LabelAnimation.moveDown(upperLabel,25,2,5,25);
            } else {
                emailField.setText(emailField.getText().trim());
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

    public void singUp(ActionEvent e) {
        email = emailField.getText().trim();
        password = passwordField.getText().trim();

        int emailLength = email.length();

        if (!email.isEmpty() && emailLength <= 25 && !password.isEmpty()) {
            System.out.println("Singed Up!");
            ((Stage) anchorPane.getScene().getWindow()).close();
        }


        if (email.isEmpty()) {
            emailField.getStyleClass().clear();
            emailErrorLabel.getStyleClass().clear();
            emailField.getStyleClass().add("input-field-error");
            emailErrorLabel.getStyleClass().add("error-label-visible");
            passwordGroup.setLayoutY(16);
        } else if (email.length()>25 ) {
            System.out.println("length: " + email.length());
            emailField.getStyleClass().clear();
            emailErrorLabel.getStyleClass().clear();
            emailField.getStyleClass().add("input-field-error");
            emailErrorLabel.getStyleClass().add("error-label-visible");
            emailErrorLabel.setText("email or name is too long");
            passwordGroup.setLayoutY(16);
        } else {
            emailField.getStyleClass().clear();
            emailErrorLabel.getStyleClass().clear();
            emailField.getStyleClass().add("input-field");
            emailErrorLabel.getStyleClass().add("error-label-invisible");
            passwordGroup.setLayoutY(0);
        }

        if (password.isEmpty()) {
            passwordField.getStyleClass().clear();
            passwordErrorLabel.getStyleClass().clear();
            passwordField.getStyleClass().add("input-field-error");
            passwordErrorLabel.getStyleClass().add("error-label-visible");
        } else {
            passwordField.getStyleClass().clear();
            passwordErrorLabel.getStyleClass().clear();
            passwordField.getStyleClass().add("input-field");
            passwordErrorLabel.getStyleClass().add("error-label-invisible");
        }

    }
}
