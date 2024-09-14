package com.messenger.auth;

import com.messenger.Log;
import com.messenger.database.UsersDataBase;
import com.messenger.design.AuthField;
import com.messenger.exceptions.*;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    @FXML
    private Label extraLabel;

    private Group passwordGroup;

    private String identifier;
    private String password;


    public void initialize() throws IOException {
        // Setting email and password field's focus to false, setting account button underline style
        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);

        accountButton.setUnderline(true);

        // Setting all error labels to default, invisible state
        emailErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);

        // group of all password field elements ( will be moved down, if email is invalid )
        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);

        // makes label animation and solves unnecessary spaces
        fieldsApplyStyle();

    }

    public void singUp() throws IOException {
        identifier = emailField.getText().trim();
        password = passwordField.getText().trim();

        try {
            // settings all text fields to a normal state
            AuthField.deleteErrorStyle(emailField, emailErrorLabel);
            AuthField.deleteErrorStyle(passwordField, passwordErrorLabel);
            passwordGroup.setLayoutY(0);

            if (checkInformationValidity(identifier, password)) {
                UsersDataBase.addUser(identifier, password);
                closeSingUpWindow();
                openManinWindow();
            }

        } catch (IncorrectWholeInformation IncorrectWholeInformation) {

            AuthField.setErrorStyle(emailField, emailErrorLabel, IncorrectWholeInformation.getMessage());
            passwordGroup.setLayoutY(16);
            AuthField.setErrorStyle(passwordField, passwordErrorLabel, IncorrectWholeInformation.getMessage());

        } catch (IncorrectIdentifierInformation | LengthException | TakenException identifierException) {

            AuthField.setErrorStyle(emailField, emailErrorLabel, identifierException.getMessage());
            passwordGroup.setLayoutY(16);

        } catch (IncorrectPasswordInformation passwordException) {

            AuthField.setErrorStyle(passwordField, passwordErrorLabel, passwordException.getMessage());
            passwordGroup.setLayoutY(0);

        } catch (Exception extraException) {
            // if there is issues with database, they will be displayed on extra label
            extraLabel.setText(extraException.getMessage());
        }

    }

    public void openLogIn() throws IOException {
        AuthLogInWindow.openLogInWindow((Stage) anchorPane.getScene().getWindow());
    }

    private void openManinWindow() throws IOException, SQLException {
        AuthMainWindow.openMainWindow(identifier);
    }

    private void closeSingUpWindow() {
        ((Stage) (anchorPane.getScene().getWindow())).close();
    }

    private void fieldsApplyStyle() {
        var emailFieldStyled = new AuthField(emailField, upperLabel);
        emailFieldStyled.setLabelChanges(-26, -2);
        emailFieldStyled.setLabelMovePath(-5, -24);
        emailFieldStyled.setStyle();

        var passwordFieldStyled = new AuthField(passwordField, lowerLabel);
        passwordFieldStyled.setLabelChanges(-11, -2);
        passwordFieldStyled.setLabelMovePath(-5, -24);
        passwordFieldStyled.setStyle();
    }

    private boolean checkInformationValidity(String identifier, String password) throws SQLException, IncorrectIdentifierInformation, LengthException, TakenException, IncorrectWholeInformation, IncorrectPasswordInformation, IOException {
        if (identifier.isEmpty() && password.isEmpty()) {
            throw new IncorrectWholeInformation("Incorrect information");
        }

        if (identifier.isEmpty()) {
            throw new IncorrectIdentifierInformation("Invalid information");
        }
        if (getIdentifierType(identifier).equals("-")) {
            throw new IncorrectIdentifierInformation("Invalid information");
        }
        if (identifier.length() > 25) {
            throw new LengthException("Email or name is too long");
        }
        if (UsersDataBase.checkUserPresence(identifier)) {
            throw new TakenException("Email or name is already taken");
        }


        if (password.isEmpty()) {
            throw new IncorrectPasswordInformation("Incorrect information");
        }

        return true;
    }

    private static String getIdentifierType(String identifier) {
        String emailPattern = "^.+@\\S*\\.[a-z]{2,}$";
        Pattern emailPatternCompile = Pattern.compile(emailPattern);
        Matcher emailMatcher = emailPatternCompile.matcher(identifier);

        String namePattern = "^[a-zA-Z][a-zA-Z0-9 ]+$";
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
