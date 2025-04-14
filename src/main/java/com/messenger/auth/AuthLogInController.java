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

public class AuthLogInController extends AuthWindow {
    @FXML private Button logInButton;

    @FXML
    public void initialize() {
        super.initialize();
        logInButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                try {
                    handleLogInClick();
                } catch (SQLException e) {
                    extraLabel.setText(e.getMessage());
                }
            }
        });
        accountButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                closeLogInWindow();
                openSingUpWindow();
            }
        });
    }

    protected void handleLogInClick() throws SQLException {
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
            openMainWindow();
            closeLogInWindow();
        }

    }

    private String getIdentifierValidity() throws SQLException {
        String identifier = identifierTextField.getText().trim();

        if (identifier.isEmpty())
            return "Email or name is empty";
        if (!UsersDataBase.getUserPresence(identifier))
            return "Email or name was not found";

        return "valid";
    }
    private String getPasswordValidity() throws SQLException {
        String identifier = identifierTextField.getText().trim();
        String password = passwordField.getText().trim();

        if (password.isEmpty())
            return "Password is empty";
        if (!identifier.isEmpty() && UsersDataBase.getUserPresence(identifier) && !UsersDataBase.getPasswordValidity(identifier,password))
            return "Password is incorrect";

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

    public void openMainWindow(){
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
    private void closeLogInWindow() {
        ((Stage) (anchorPane.getScene().getWindow())).close();
    }

    private void openSingUpWindow() {
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


}
