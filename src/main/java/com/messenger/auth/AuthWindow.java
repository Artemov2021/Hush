package com.messenger.auth;

import com.messenger.design.AuthField;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public abstract class AuthWindow {
    @FXML protected TextField identifierTextField;
    @FXML protected Label identifierTextFieldLabel;
    @FXML protected PasswordField passwordField;
    @FXML protected Label passwordFieldLabel;
    @FXML protected Label identifierErrorLabel;
    @FXML protected Label passwordErrorLabel;
    @FXML protected Button accountButton;
    @FXML protected Label extraLabel;
    protected Group passwordGroup;
    private static int mainUserId;

    public void initialize() {
        setFieldsUnfocused();
        setErrorLabelsInvisible();
        setAccountButtonUnderlined();
        setPasswordGroup();
        setFieldsStyle();
    }


    private void setFieldsUnfocused() {
        identifierTextField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
    }
    private void setErrorLabelsInvisible() {
        identifierErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
    }
    private void setAccountButtonUnderlined() {
        accountButton.setUnderline(true);
    }
    private void setPasswordGroup() {
        passwordGroup = new Group(passwordField,passwordFieldLabel, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);
    }
    private void setFieldsStyle() {
        var emailFieldStyled = new AuthField(identifierTextField,identifierTextFieldLabel);
        emailFieldStyled.setLabelChanges(-26, -2);
        emailFieldStyled.setLabelMovePath(-5, -24);
        emailFieldStyled.setStyle();

        var passwordFieldStyled = new AuthField(passwordField,passwordFieldLabel);
        passwordFieldStyled.setLabelChanges(-11, -2);
        passwordFieldStyled.setLabelMovePath(-5, -24);
        passwordFieldStyled.setStyle();
    }


    public void setMainUserId(int newMainUserId) {
        mainUserId = newMainUserId;
    }
    public static int getMainUserId() {
        return mainUserId;
    }


    protected void setIdentifierDefaultStyle() {
        identifierErrorLabel.setVisible(false);
        identifierTextField.getStyleClass().clear();
        identifierTextField.getStyleClass().add("input-field");
    }
    protected void setPasswordDefaultStyle() {
        passwordErrorLabel.setVisible(false);
        passwordField.getStyleClass().clear();
        passwordField.getStyleClass().add("input-field");
        passwordGroup.setTranslateY(0);
    }




}
