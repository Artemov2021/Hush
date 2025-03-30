package com.messenger.auth;

import com.messenger.database.UsersDataBase;
import com.messenger.design.AuthField;
import com.messenger.main.MainWindowController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AuthLogInController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField identifierField;
    @FXML
    private Label identifierLabel;
    @FXML
    private Label identifierErrorLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label lowerLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Button accountButton;
    @FXML
    private Label extraLabel;

    private Group passwordGroup;


    public void initialize () {
        setSceneStyles();
        fieldsApplyStyle(); // makes label animation
    }


    public void setSceneStyles() {
        identifierField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);

        // setting all error labels to default, invisible state
        identifierErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);

        // group of all password field elements ( will be moved down, if email is invalid )
        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);
    }
    public void fieldsApplyStyle() {
        var emailFieldStyled = new AuthField(identifierField,identifierLabel);
        emailFieldStyled.setLabelChanges(-26, -2);
        emailFieldStyled.setLabelMovePath(-5, -24);
        emailFieldStyled.setStyle();

        var passwordFieldStyled = new AuthField(passwordField, lowerLabel);
        passwordFieldStyled.setLabelChanges(-11, -2);
        passwordFieldStyled.setLabelMovePath(-5, -24);
        passwordFieldStyled.setStyle();
    }


    @FXML
    public void login() throws SQLException {
        String identifier = identifierField.getText().trim();
        String password = passwordField.getText().trim();

        String identifierStatus = checkIdentifier(identifier);
        String passwordStatus = checkPassword(identifier,password);
        setIdentifierDefaultStyle();
        setPasswordDefaultStyle();

        if (identifierStatus.equals("valid") && passwordStatus.equals("valid")) {

            openMainWindow(identifier,password);
            closeLogInWindow();

            return;
        }

        if (identifierStatus.equals("Email or name is empty") || identifierStatus.equals("Email or name was not found")) {

            identifierErrorLabel.setVisible(true);
            identifierErrorLabel.setText(identifierStatus);
            identifierField.getStyleClass().clear();
            identifierField.getStyleClass().add("input-field-error");

            passwordGroup.setTranslateY(17);

        }

        if (passwordStatus.equals("Password is empty") || passwordStatus.equals("Password is incorrect")) {

            passwordErrorLabel.setVisible(true);
            passwordErrorLabel.setText(passwordStatus);
            passwordField.getStyleClass().clear();
            passwordField.getStyleClass().add("input-field-error");

        }


    }
    public String checkIdentifier(String identifier) throws SQLException {

        if (identifier.isEmpty())
            return "Email or name is empty";
        if (!UsersDataBase.getUserPresence(identifier))
            return "Email or name was not found";

        return "valid";
    }
    public String checkPassword(String identifier,String password) throws SQLException {

        if (password.isEmpty())
            return "Password is empty";
        if (!identifier.isEmpty() && UsersDataBase.getUserPresence(identifier) && !UsersDataBase.getPasswordValidity(identifier,password))
            return "Password is incorrect";

        return "valid";
    }
    public void setIdentifierDefaultStyle() {
        identifierErrorLabel.setVisible(false);
        identifierField.getStyleClass().clear();
        identifierField.getStyleClass().add("input-field");
    }
    public void setPasswordDefaultStyle() {
        passwordErrorLabel.setVisible(false);
        passwordField.getStyleClass().clear();
        passwordField.getStyleClass().add("input-field");
        passwordGroup.setTranslateY(0);
    }


    @FXML
    public void openSingUp() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/AuthSingUp.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setResizable(false);
            stage.setScene(scene);
            stage.setTitle("Sing Up");
            stage.show();
            ((Stage) (anchorPane.getScene().getWindow())).close();  // close current window
        } catch (Exception e) {
            extraLabel.setText(e.getMessage());
        }
    }


    public void openMainWindow(String identifier,String identifierType){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/fxml/MainWindow.fxml"));
            Parent root = loader.load();

            MainWindowController mainWindowController = loader.getController();
            mainWindowController.setMainUserId(identifierType.equals("email") ? UsersDataBase.getIdWithEmail(identifier) : UsersDataBase.getIdWithName(identifier));

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setScene(scene);
            newStage.setTitle("Main");
            newStage.show();
        } catch (Exception e) {
            extraLabel.setText(e.getMessage());
        }
    }
    public void closeLogInWindow() {
        ((Stage) (anchorPane.getScene().getWindow())).close();
    }


}
