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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AuthSingUpController extends AuthWindow {
    @FXML private AnchorPane anchorPane;
    @FXML private Button singUpButton;

    @FXML
    public void initialize() {
        super.initialize();
        singUpButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                try {
                    handleSignupClick();
                } catch (SQLException e) {
                    extraLabel.setText(e.getMessage());
                }
            }
        });
        accountButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                openLogInWindow();
                closeSingUpWindow();
            }
        });
    }

    protected void handleSignupClick() throws SQLException {
        boolean isIdentifierValid = getIdentifierValidity().equals("valid");
        boolean isPasswordValid = getPasswordValidity().equals("valid");

        setIdentifierDefaultStyle();
        setPasswordDefaultStyle();

        if (!isIdentifierValid) {
            showIdentifierErrorMessage(getIdentifierValidity());
        }
        if (!isPasswordValid) {
            showPasswordErrorMessage(getPasswordValidity());
        }
        if (isIdentifierValid && isPasswordValid) {
            singUpAndOpenMainWindow();
        }

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
    private String getIdentifierValidity() throws SQLException {
        String identifier = identifierTextField.getText().trim();
        String identifierType = getIdentifierType(identifier);

        if (identifier.isEmpty())
            return "Email or name is empty";
        if (identifierType.equals("-"))
            return "Email or name is invalid";
        if ((identifierType.equals("email") && identifier.length() > 44) || (identifierType.equals("name") && identifier.length() > 30))
            return "Email or name is too long";
        if (UsersDataBase.getUserPresence(identifier))
            return "Email or name already exists";

        return "valid";
    }
    private String getPasswordValidity() {
        String password = passwordField.getText().trim();

        if (password.isEmpty())
            return "Password is empty";
        if (password.length() > 25)
            return "Password is too long";

        return "valid";
    }

    private void showIdentifierErrorMessage(String errorMessage) {
        identifierErrorLabel.setVisible(true);
        identifierErrorLabel.setText(errorMessage);
        identifierTextField.getStyleClass().clear();
        identifierTextField.getStyleClass().add("input-field-error");

        passwordGroup.setTranslateY(17);
    }
    private void showPasswordErrorMessage(String errorMessage) {
        passwordErrorLabel.setVisible(true);
        passwordErrorLabel.setText(errorMessage);
        passwordField.getStyleClass().clear();
        passwordField.getStyleClass().add("input-field-error");
    }

    private void singUpAndOpenMainWindow() throws SQLException {
        String identifier = identifierTextField.getText().trim();
        String password = passwordField.getText().trim();

        insertUserIntoDB(identifier,password);
        openMainWindow();
        closeSingUpWindow();
    }
    private void insertUserIntoDB(String identifier,String password) throws SQLException {
        setMainUserId(UsersDataBase.addUser(identifier,password));
    }
    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/fxml/MainWindow.fxml"));
            Parent root = loader.load();

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
    private void closeSingUpWindow() {
        ((Stage) (anchorPane.getScene().getWindow())).close();
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
        } catch (Exception e) {
            extraLabel.setText(e.getMessage());
        }
    }


}
