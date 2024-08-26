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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
    private ProgressBar progressBar;
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

        Log.writeNewActionLog("The stage was initialized!\n");
    }

    public void singUp() throws IOException {
        identifier = emailField.getText().trim();
        password = passwordField.getText().trim();
        Log.writeNewActionLog(String.format("New entered information: identifier - \"%s\"; password - \"%s\" \n",identifier,password));

        try {

            // settings all text fields to a normal state
            AuthField.deleteErrorStyle(emailField, emailErrorLabel);
            AuthField.deleteErrorStyle(passwordField, passwordErrorLabel);
            passwordGroup.setLayoutY(0);

            if (checkInformationValidity(identifier, password)) {
                Log.writeNewActionLog("The information is correct!\n");
                UsersDataBase.addUser(identifier, password);
                Log.writeNewActionLog("User was added to the \"auth.db\"\n");
                closeSingUpWindow();
                Log.writeNewActionLog("Sing Up window was closed\n");
                openManinWindow();
                Log.writeNewActionLog("Main window was opened\n");
            }

        } catch (SQLException | IOException extraException) {

            // if there is issues with database, they will be displayed on extra label
            extraLabel.setText(extraException.getMessage());

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

        }


    }

    public void openLogIn() throws IOException {
        AuthLogInWindow.openLogInWindow((Stage) anchorPane.getScene().getWindow());
        Log.writeNewActionLog(String.format("%0" + 65 + "d" + "\n",0).replace("0","-"));
        Log.writeNewActionLog("Log In window was: opened\n");
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
        Log.writeNewActionLog("Checking information validity...\n");
        if (identifier.isEmpty() && password.isEmpty()) {
            Log.writeNewActionLog("Identifier and password are empty!\n");
            throw new IncorrectWholeInformation("Incorrect information");
        }

        if (identifier.isEmpty()) {
            Log.writeNewActionLog("Identifier is empty!\n");
            throw new IncorrectIdentifierInformation("Incorrect information");
        }
        if (identifier.length() > 25) {
            Log.writeNewActionLog(String.format("Identifier length is more than 25! (%d)\n",identifier.length()));
            throw new LengthException("Email or name is too long");
        }
        if (hasNoLetters(identifier)) {
            Log.writeNewActionLog("Identifier has no letters (invalid information)!\n");
            throw new IncorrectIdentifierInformation("Incorrect information");
        }
        if (UsersDataBase.checkUserPresence(identifier)) {
            Log.writeNewActionLog("Identifier is already in database!\n");
            throw new TakenException("Email or name is already taken");
        }


        if (password.isEmpty()) {
            Log.writeNewActionLog("Password is empty!\n");
            throw new IncorrectPasswordInformation("Incorrect information");
        }

        return true;
    }

    private static boolean hasNoLetters(String identifier) {
        String numbersPattern = "^[^a-z]+$";
        Pattern pattern = Pattern.compile(numbersPattern);
        Matcher matcher = pattern.matcher(identifier);
        if (matcher.find()) {
            return true;
        }
        return false;
    }
}
