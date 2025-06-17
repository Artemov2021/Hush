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
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthLogInController {
    @FXML private AnchorPane anchorPane;
    @FXML private TextField identifierTextField;
    @FXML private Label identifierTextFieldLabel;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordFieldLabel;
    @FXML private Label identifierErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Button logInButton;
    @FXML private Button accountButton;
    @FXML private Label extraLabel;
    protected Group passwordGroup;
    private int userId;


    @FXML
    public void initialize() {
        setFieldsUnfocused();
        setErrorLabelsInvisible();
        setAccountButtonUnderlined();
        setPasswordGroup();
        setFieldsStyle();
        setLogInButtonAction();
        setAccountButtonAction();
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
    private void setLogInButtonAction() {
        logInButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                try {
                    handleLogInClick();
                } catch (SQLException e) {
                    extraLabel.setText(e.getMessage());
                }
            }
        });
    }
    private void setAccountButtonAction() {
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

        if (identifier.isEmpty())
            return "Email or name is empty";
        if (!UsersDataBase.getUserPresence(identifier))
            return "Email or name was not found";

        userId = getIdentifierType(identifier).equals("name") ? UsersDataBase.getIdWithName(identifier) : UsersDataBase.getIdWithEmail(identifier);
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

            MainWindowController controller = loader.getController();
            controller.setMainUserId(userId);
            controller.initializeWithValue(); // now it's safe to load user dat

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setScene(scene);
            newStage.setTitle("Hush");
            newStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/icon.png")));
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
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/icon.png")));
            stage.show();
            ((Stage) (anchorPane.getScene().getWindow())).close();  // close current window
        } catch (Exception e) {
            extraLabel.setText(e.getMessage());
        }
    }


}
