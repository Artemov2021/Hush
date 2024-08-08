package com.messenger.auth;

import com.messenger.design.AuthField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

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

    public void logIn (ActionEvent e) {
        // TODO
    }

}
