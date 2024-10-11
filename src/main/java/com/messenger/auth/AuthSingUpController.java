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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AuthSingUpController {
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
    private Label passwordLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Button accountButton;
    @FXML
    private Label extraLabel;
    @FXML
    private ProgressBar progressBar;

    private Group passwordGroup;

    public void initialize() {
        // Setting email and password field's unfocused, setting account button underline style
        identifierField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);

        // Setting all error labels to default, invisible state
        identifierErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);

        // group of all password field elements ( will be moved down, if email is invalid )
        passwordGroup = new Group(passwordLabel, passwordField, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);

        // makes label animation and solves unnecessary spaces
        fieldsApplyStyle();
    }
    public void checkInformation() {
        progressBar.setProgress(0.2);
        try {
            String identifier = identifierField.getText().trim();
            String password = passwordField.getText().trim();
            byte occuredExceptions = 0;

            AuthField.deleteErrorStyle(identifierField,identifierErrorLabel);
            AuthField.deleteErrorStyle(passwordField, passwordErrorLabel);
            passwordGroup.setLayoutY(0);

            String identifierType = getIdentifierType(identifier);

            if (identifierType.equals("-") || UsersDataBase.getUserPresence(identifier)) {  // if identifier is invalid
                String exceptionsReason = identifierType.equals("-") ? "Invalid information" :
                        (identifierType.substring(0,1).toUpperCase() + identifierType.substring(1)) + " is already taken";
                AuthField.setErrorStyle(identifierField,identifierErrorLabel,exceptionsReason);
                passwordGroup.setLayoutY(16);
                occuredExceptions++;
            }

            if (password.isEmpty() || password.length() > 25) {
                String exceptionsReason = password.isEmpty() ? "Invalid password" : "Password is too long";
                AuthField.setErrorStyle(passwordField,passwordErrorLabel,exceptionsReason);
                occuredExceptions++;
            }

            if (occuredExceptions == 0) {
                UsersDataBase.addUser(identifier,password);
                progressBar.setProgress(0.7);
                openManinWindow(identifier,identifierType);
            }
        } catch (Exception e) {
            extraLabel.setText(e.getMessage());
        }

    }
    public void openLogInWindow() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/AuthLogIn.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setResizable(false);
            stage.setScene(scene);
            stage.setTitle("Log In");
            stage.show();
            ((Stage) (anchorPane.getScene().getWindow())).close();  // close current window
        } catch (Exception e) {
            extraLabel.setText(e.getMessage());
        }
    }



    private void openManinWindow(String identifier,String identifierType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/MainWindow.fxml"));
            Parent root = loader.load();

            MainWindowController mainWindowController = loader.getController();
            mainWindowController.setId(identifierType.equals("email") ? UsersDataBase.getIdWithEmail(identifier) : UsersDataBase.getIdWithName(identifier));

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setScene(scene);
            newStage.setTitle("Main");
            progressBar.setProgress(1);
            closeSingUpWindow();  // close current window
            newStage.show();
        } catch (Exception e) {
            //extraLabel.setText(e.getMessage());
            e.printStackTrace();
        }
    }
    private void closeSingUpWindow() {
        ((Stage) (anchorPane.getScene().getWindow())).close();
    }
    private void fieldsApplyStyle() {
        var emailFieldStyled = new AuthField(identifierField,identifierLabel);
        emailFieldStyled.setLabelChanges(-26, -2);
        emailFieldStyled.setLabelMovePath(-5, -24);
        emailFieldStyled.setStyle();

        var passwordFieldStyled = new AuthField(passwordField, passwordLabel);
        passwordFieldStyled.setLabelChanges(-11, -2);
        passwordFieldStyled.setLabelMovePath(-5, -24);
        passwordFieldStyled.setStyle();
    }
    private String getIdentifierType(String identifier) {
        String emailPattern = "^[a-zA-Z].+@\\S*\\.[a-z]{2,}$";
        Pattern emailPatternCompile = Pattern.compile(emailPattern);
        Matcher emailMatcher = emailPatternCompile.matcher(identifier);

        String namePattern = "^[a-zA-Z][a-zA-Z0-9_ ]+$";
        Pattern namePatternCompile = Pattern.compile(namePattern);
        Matcher nameMatcher = namePatternCompile.matcher(identifier);

        if (emailMatcher.find()) {
            return "email";
        } else if (nameMatcher.find()) {
            return "name";
        } else {
            return "-";
        }
    }
}
