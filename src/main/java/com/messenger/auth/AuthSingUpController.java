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

    private Group passwordGroup;

    public void initialize() {
        setSceneStyles();
        fieldsApplyStyle(); // makes label animation
    }


    public void setSceneStyles() {
        // Set email and password field's unfocused
        identifierField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);

        // Set all error labels to default, invisible state
        identifierErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);

        // Set "account" bottom button underlined
        accountButton.setUnderline(true);

        // Group password label, password field and password error label together ( will be moved down, if email is invalid )
        passwordGroup = new Group(passwordField,passwordLabel, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);
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


    @FXML
    public void singUp() throws SQLException {
        String identifier = identifierField.getText().trim();
        String password = passwordField.getText().trim();

        String identifierStatus = checkIdentifier(identifier);
        String passwordStatus = checkPassword(password);
        setIdentifierDefaultStyle();
        setPasswordDefaultStyle();

        if (identifierStatus.equals("valid") && passwordStatus.equals("valid")) {

            insertUserIntoDB(identifier,password);
            openMainWindow(identifier,password);
            closeSingUpWindow();

            return;
        }

        if (identifierStatus.equals("Email or name is empty") || identifierStatus.equals("Email or name is too long") || identifierStatus.equals("Email or name is invalid")
        || identifierStatus.equals("Email or name already exists")) {

            identifierErrorLabel.setVisible(true);
            identifierErrorLabel.setText(identifierStatus);
            identifierField.getStyleClass().clear();
            identifierField.getStyleClass().add("input-field-error");

            passwordGroup.setTranslateY(17);

        }

        if (passwordStatus.equals("Password is empty") || passwordStatus.equals("Password is too long")) {

            passwordErrorLabel.setVisible(true);
            passwordErrorLabel.setText(passwordStatus);
            passwordField.getStyleClass().clear();
            passwordField.getStyleClass().add("input-field-error");

        }


    }
    public String checkIdentifier(String identifier) throws SQLException {
        String identifierType = getIdentifierType(identifier);

        if (identifier.isEmpty())
            return "Email or name is empty";
        if (identifierType.equals("-"))
            return "Email or name is invalid";
        if ((identifierType.equals("email") && identifier.length() > 37) || (identifierType.equals("name") && identifier.length() > 23))
            return "Email or name is too long";
        if (UsersDataBase.getUserPresence(identifier))
            return "Email or name already exists";

        return "valid";
    }
    public String checkPassword(String password) {

        if (password.isEmpty())
           return "Password is empty";
        if (password.length() > 25)
            return "Password is too long";

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
    public void insertUserIntoDB(String identifier,String password) throws SQLException {
        UsersDataBase.addUser(identifier,password);
    }


    @FXML
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


    private void openMainWindow(String identifier,String identifierType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/fxml/MainWindow.fxml"));
            Parent root = loader.load();

            MainWindowController mainWindowController = loader.getController();
            mainWindowController.setId(identifierType.equals("email") ? UsersDataBase.getIdWithEmail(identifier) : UsersDataBase.getIdWithName(identifier));

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setScene(scene);
            newStage.setTitle("Main");
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


    private String getIdentifierType(String identifier) {
        String emailPattern = "^[a-zA-Z0-9._@\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
        Pattern emailPatternCompile = Pattern.compile(emailPattern);
        Matcher emailMatcher = emailPatternCompile.matcher(identifier);

        String namePattern = "^[a-zA-Z][a-zA-Z0-9 ]*$";
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
