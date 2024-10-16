package com.messenger.auth;

import com.messenger.database.UsersDataBase;
import com.messenger.design.AuthField;
import com.messenger.design.LoadingDots;
import com.messenger.main.MainWindowController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Button singUpButton;
    @FXML
    private Button accountButton;
    @FXML
    private Label extraLabel;
    @FXML
    private ProgressBar progressBar;

    private Group passwordGroup;


    public void initialize () {
        identifierField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        accountButton.setUnderline(true);

        // setting all error labels to default, invisible state
        identifierErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);

        // group of all password field elements ( will be moved down, if email is invalid )
        passwordGroup = new Group(lowerLabel, passwordField, passwordErrorLabel);
        anchorPane.getChildren().add(passwordGroup);

        // makes label animation and solves unnecessary spaces
        fieldsApplyStyle();
    }
    public void checkInformation() {
        if (anchorPane.lookup("#dotsContainer") == null) {
            setDefaultFieldsStyle();
            setLoadingButton();
            setLoadingProgress(0.2);
            passwordGroup.setLayoutY(0);
        }
        try {
            String identifier = identifierField.getText().trim();
            String password = passwordField.getText().trim();
            byte occuredExceptions = 0;

            AuthField.deleteErrorStyle(identifierField,identifierErrorLabel);
            AuthField.deleteErrorStyle(passwordField, passwordErrorLabel);
            passwordGroup.setLayoutY(0);

            String identifierType = getIdentifierType(identifier);

            if (identifierType.equals("-") || !UsersDataBase.getUserPresence(identifier)) {  // if identifier is invalid
                String exceptionsReason = identifierType.equals("-") ? "Invalid information" : "Incorrect information";
                AuthField.setErrorStyle(identifierField,identifierErrorLabel,exceptionsReason);
                passwordGroup.setLayoutY(16);
                occuredExceptions++;
            }

            if (password.isEmpty() || !UsersDataBase.getPasswordValidity(identifier,password)) {
                String exceptionsReason = password.isEmpty() ? "Invalid password" : "Incorrect password";
                AuthField.setErrorStyle(passwordField,passwordErrorLabel,exceptionsReason);
                occuredExceptions++;
            }

            if (occuredExceptions == 0) {
                UsersDataBase.addUser(identifier,password);
                setLoadingProgress(0.7);
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                    openMainWindow(identifier,identifierType);
                    ((Stage) (anchorPane.getScene().getWindow())).close();  // close current window
                }));
                timeline.setCycleCount(1);
                timeline.play();
            }
            if (occuredExceptions > 0) {
                resetProgress();
                setNormalButton();
            }
        } catch (Exception e) {
            extraLabel.setText(e.getMessage());
            e.printStackTrace();
        }
    }
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



    private void openMainWindow(String identifier,String identifierType){
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
            newStage.show();
        } catch (Exception e) {
            extraLabel.setText(e.getMessage());
        }
    }
    private void fieldsApplyStyle() {
        var emailFieldStyled = new AuthField(identifierField,identifierLabel);
        emailFieldStyled.setLabelChanges(-26, -2);
        emailFieldStyled.setLabelMovePath(-5, -24);
        emailFieldStyled.setStyle();

        var passwordFieldStyled = new AuthField(passwordField, lowerLabel);
        passwordFieldStyled.setLabelChanges(-11, -2);
        passwordFieldStyled.setLabelMovePath(-5, -24);
        passwordFieldStyled.setStyle();
    }
    private String getIdentifierType(String identifier) {
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
