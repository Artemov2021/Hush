package com.messenger.auth;

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

    public void initialize() {
        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);

        // setting all error labels to default, invisible state
        emailErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);

        // group of all password field elements ( will be moved down, if email is invalid )
        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);

        // makes label animation and solves unnecessary spaces
        fieldsApplyStyle();
    }

    public void singUp() {
        identifier = emailField.getText().trim();
        password = passwordField.getText().trim();

        try {

            // settings all text fields to a normal state
            AuthField.deleteErrorStyle(emailField,emailErrorLabel);
            AuthField.deleteErrorStyle(passwordField,passwordErrorLabel);
            passwordGroup.setLayoutY(0);

            if (checkInformationValidity(identifier,password)) {
                UsersDataBase.addUser(identifier,password);
                closeSingUpWindow();
                openManinWindow();
            }

        } catch (SQLException | IOException extraException) {

            // if there is issues with database, they will be displayed on extra label
            extraLabel.setText(extraException.getMessage());

        } catch (IncorrectWholeInformation IncorrectWholeInformation) {

            AuthField.setErrorStyle(emailField,emailErrorLabel,IncorrectWholeInformation.getMessage());
            passwordGroup.setLayoutY(16);
            AuthField.setErrorStyle(passwordField,passwordErrorLabel,IncorrectWholeInformation.getMessage());

        } catch (IncorrectIdentifierInformation | LengthException | TakenException identifierException) {

            AuthField.setErrorStyle(emailField,emailErrorLabel,identifierException.getMessage());
            passwordGroup.setLayoutY(16);

        } catch (IncorrectPasswordInformation passwordException ) {

            AuthField.setErrorStyle(passwordField,passwordErrorLabel,passwordException.getMessage());
            passwordGroup.setLayoutY(0);

        }


    }

    public void openLogIn() throws IOException {
        AuthLogInWindow.openLogInWindow((Stage) anchorPane.getScene().getWindow());
    }

    private void openManinWindow() throws IOException,SQLException {
        AuthMainWindow.openMainWindow(identifier);
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

    private boolean checkInformationValidity(String identifier, String password) throws SQLException, IncorrectIdentifierInformation, LengthException, TakenException, IncorrectWholeInformation, IncorrectPasswordInformation {
        if (identifier.isEmpty() && password.isEmpty()) {
            throw new IncorrectWholeInformation("Incorrect information");
        }

        if (identifier.isEmpty()) {
            throw new IncorrectIdentifierInformation("Incorrect information");
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

    private void closeSingUpWindow() {
        ((Stage) (anchorPane.getScene().getWindow())).close();
    }


}
