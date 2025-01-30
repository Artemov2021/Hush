package com.messenger.auth;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.AuthField;
import com.messenger.design.LoadingDots;
import com.messenger.main.MainWindowController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.Timer;
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
    private Button singUpButton;
    @FXML
    private Button accountButton;
    @FXML
    private Label extraLabel;
    @FXML
    private ProgressBar progressBar;

    private Group passwordGroup;

    public void initialize() {
        setSceneStyles();

        // makes label animation
        fieldsApplyStyle();
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
        System.out.println(identifierStatus);
        System.out.println(passwordStatus);
        System.out.println("");

        if (identifierStatus.equals("valid") && passwordStatus.equals("valid")) {

            progressBar.setProgress(0.25);

            // 1. show loading
            // 2. insert new user into db ( upload loading )
            // 3. open main window ( upload loading )
            // 4. close sing up window


            return;
        }

        if (identifierStatus.equals("Email or name is empty")) {
            // 1. apply error style
            // 2. move password group
        }

        if (identifierStatus.equals("Email or name is too long")) {
            // 1. apply error style
            // 2. move password group
        }

        if (identifierStatus.equals("Email or name is invalid")) {

        }

        if (identifierStatus.equals("Email or name already exists")) {

        }

        if (passwordStatus.equals("Password is empty")) {

        }

        if (passwordStatus.equals("Password is too long")) {

        }
    }
    public String checkIdentifier(String identifier) throws SQLException {
        String identifierType = getIdentifierType(identifier);

        if (identifier.isEmpty())
            return "Email or name is empty";
        if (identifierType.equals("-"))
            return "Email or name is invalid";
        if ((identifierType.equals("email") && identifier.length() > 38) || (identifierType.equals("name") && identifier.length() > 24))
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













//        if (anchorPane.lookup("#dotsContainer") == null) {
//            setDefaultFieldsStyle();
//            setLoadingButton();
//            setLoadingProgress(0.2);
//            passwordGroup.setLayoutY(0);
//        }
//
//        try {
//
//            String identifier = identifierField.getText().trim();
//            String password = passwordField.getText().trim();
//            String identifierType = getIdentifierType(identifier);
//            byte occuredExceptions = 0;
//
//            if (identifierType.equals("-") || UsersDataBase.getUserPresence(identifier)) {  // if identifier is invalid
//                String exceptionsReason = identifierType.equals("-") ? "Invalid information" :
//                        (identifierType.substring(0,1).toUpperCase() + identifierType.substring(1)) + " is already taken";
//                AuthField.setErrorStyle(identifierField,identifierErrorLabel,exceptionsReason);
//                passwordGroup.setLayoutY(16);
//                occuredExceptions++;
//            }
//
//            if (password.isEmpty() || password.length() > 25) {
//                String exceptionsReason = password.isEmpty() ? "Invalid password" : "Password is too long";
//                AuthField.setErrorStyle(passwordField,passwordErrorLabel,exceptionsReason);
//                occuredExceptions++;
//            }
//
//            if (occuredExceptions == 0) {
//                UsersDataBase.addUser(identifier,password);
//                setLoadingProgress(0.7);
//                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
//                    openManinWindow(identifier,identifierType);
//                    setLoadingProgress(1);
//                }));
//                timeline.setCycleCount(1);
//                timeline.play();
//            }
//            if (occuredExceptions > 0) {
//                resetProgress();
//                setNormalButton();
//            }
//        } catch (Exception e) {
//            extraLabel.setText(e.getMessage());
//        }







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

    private void openManinWindow(String identifier,String identifierType) {
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




    private String getIdentifierType(String identifier) {
        String emailPattern = "^[a-zA-Z0-9._@\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
        Pattern emailPatternCompile = Pattern.compile(emailPattern);
        Matcher emailMatcher = emailPatternCompile.matcher(identifier);

        String namePattern = "^[a-zA-Z][a-zA-Z0-9]*$";
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
    private void setDefaultFieldsStyle() {
        AuthField.deleteErrorStyle(identifierField,identifierErrorLabel);
        AuthField.deleteErrorStyle(passwordField, passwordErrorLabel);
    }
    private void setLoadingProgress(double progress) {
        Platform.runLater(()->{
            progressBar.setProgress(progress);
        });
    }
    private void resetProgress() {
        Platform.runLater(()-> {
            progressBar.setProgress(0);
        });
    }
    private void setLoadingButton() {
        Platform.runLater(()-> {
            singUpButton.getStyleClass().clear();
            singUpButton.getStyleClass().add("main-button-loading");
            LoadingDots.startAnimation(anchorPane);
        });
    }
    private void setNormalButton() {
        Platform.runLater(()->{
            singUpButton.getStyleClass().clear();
            singUpButton.getStyleClass().add("main-button");
            anchorPane.getChildren().remove(anchorPane.lookup("#dotsContainer"));
        });
    }



}
