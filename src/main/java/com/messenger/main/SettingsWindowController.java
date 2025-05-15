package com.messenger.main;

import com.messenger.database.UsersDataBase;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SettingsWindowController extends MainWindowController {
    @FXML private Pane settingsBackground;
    @FXML private Pane settingsOverlay;
    @FXML private Label settingsAvatar;
    @FXML private TextField settingsNameField;
    @FXML private TextField settingsEmailField;
    @FXML private Label settingsNameErrorMessage;
    @FXML private Label settingsEmailErrorMessage;
    @FXML private Label emailTitle;
    @FXML private Pane buttonsBackground;
    @FXML private Pane buttonsOverlay;
    @FXML private Label changeButton;
    @FXML private Label deleteButton;

    private Group emailGroup;
    private String newAvatarPath = "";

    public void injectUIElements(MainWindowController source) {
        this.mainAnchorPane = source.mainAnchorPane;
        this.mainAvatarLabel = source.mainAvatarLabel;
        this.mainNameLabel = source.mainNameLabel;
        this.mainEmailLabel = source.mainEmailLabel;
    }
    public void initializeSettingsInterface() throws SQLException {
        initializeInterface();
        setupClickHandlers();
        showOpeningEffect();
    }


    // Initialize Interface
    public void initializeInterface() throws SQLException {
        setEmailGroup();
        setTextFieldsUnfocused();
        setAvatarPicture();
        setNameInsideField();
        setEmailInsideField();
        hideErrorLabels();
        setAvatarButtonsToFront();
        hideAvatarButtonsElements();
    }
    public void setEmailGroup() {
        emailGroup = new Group(emailTitle,settingsEmailField,settingsEmailErrorMessage);
        settingsOverlay.getChildren().add(emailGroup);
        emailGroup.toBack();
    }
    public void setTextFieldsUnfocused() {
        settingsNameField.setFocusTraversable(false);
        settingsEmailField.setFocusTraversable(false);
    }
    private void setAvatarPicture() throws SQLException {
        settingsAvatar.getStyleClass().clear();
        if (UsersDataBase.getAvatarWithId(mainUserId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(mainUserId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(155);
            imageView.setFitWidth(155);
            imageView.setSmooth(true);
            settingsAvatar.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(77.5);
            clip.setLayoutY(77.5);
            clip.setRadius(77.5);
            settingsAvatar.setClip(clip);
        } else {
            settingsAvatar.getStyleClass().add("settings-avatar");
        }
    }
    private void setNameInsideField() throws SQLException {
        String mainUserName = UsersDataBase.getNameWithId(mainUserId);
        settingsNameField.setText(mainUserName);
    }
    private void setEmailInsideField() throws SQLException {
        String mainUserEmail = UsersDataBase.getEmailWithId(mainUserId);
        settingsEmailField.setText(mainUserEmail);
    }
    private void hideErrorLabels() {
        // Setting all error label to "invisible" mode
        settingsNameErrorMessage.setVisible(false);
        settingsEmailErrorMessage.setVisible(false);
    }
    private void setAvatarButtonsToFront() {
        buttonsOverlay.toFront();
    }
    private void hideAvatarButtonsElements() {
        // Hiding change and delete avatar buttons
        buttonsBackground.setVisible(false);
        buttonsOverlay.setVisible(false);
    }


    // Set Up Click Handlers
    public void setupClickHandlers() {
        // Consume the event to prevent it from affecting backgroundPane
        settingsOverlay.setOnMouseClicked(Event::consume);

        // Set event handler for backgroundPane
        settingsBackground.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                hideWindow();
            }
        });

        // Set event handler for avatar label
        settingsAvatar.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.SECONDARY) {
                Point2D paneCoordinates = settingsAvatar.localToParent(clickEvent.getX(), clickEvent.getY());
                double x = paneCoordinates.getX();
                double y = paneCoordinates.getY();
                showAvatarsButton(x,y);
            }
        });
    }


    // Show Opening Effect
    private void showOpeningEffect() {
        // Appearing time
        FadeTransition FadeIn = new FadeTransition(Duration.millis(180),settingsBackground);
        FadeIn.setFromValue(0);
        FadeIn.setToValue(1);
        FadeIn.play();

        // Appearing move to left
        TranslateTransition translateIn = new TranslateTransition(Duration.millis(180),settingsOverlay);
        translateIn.setFromX(0);
        translateIn.setToX(-35);
        translateIn.play();
    }


    private void showAvatarsButton(double x,double y) {
        // Setting buttons visibility, creating background pane
        buttonsOverlay.setVisible(true);
        buttonsBackground.setVisible(true);
        buttonsOverlay.setLayoutX(x);
        buttonsOverlay.setLayoutY(y);

        // When you click away, buttons disappear
        buttonsBackground.setOnMouseClicked(event -> {
            buttonsOverlay.setVisible(false);
            buttonsBackground.setVisible(false);
        });

        // When you click "change" button
        changeButton.setOnMouseClicked(ActionEvent -> {
            File selectedFile = getFileFromFileChooser();
            if (selectedFile != null) {
                newAvatarPath = selectedFile.getPath();
                setAvatarPicture(selectedFile.getPath());
            }
            buttonsOverlay.setVisible(false);
            buttonsBackground.setVisible(false);
        });

        // When you click "delete" button
        deleteButton.setOnMouseClicked(ActionEvent -> {
            newAvatarPath = "-";
            setDefaultAvatar();
            buttonsOverlay.setVisible(false);
            buttonsBackground.setVisible(false);
        });
    }
    private File getFileFromFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files","*.png","*.jpg"));
        return fileChooser.showOpenDialog(mainAnchorPane.getScene().getWindow());
    }
    private void setAvatarPicture(String path) {
        ImageView imageView = new ImageView(new Image(new File(path).toURI().toString()));
        imageView.setFitHeight(155);
        imageView.setFitWidth(155);
        imageView.setSmooth(true);
        settingsAvatar.setGraphic(imageView);
        Circle clip = new Circle();
        clip.setLayoutX(77.5);
        clip.setLayoutY(77.5);
        clip.setRadius(77.5);
        settingsAvatar.setClip(clip);
    }
    private void setDefaultAvatar() {
        settingsAvatar.setGraphic(null);
        settingsAvatar.getStyleClass().clear();
        settingsAvatar.getStyleClass().add("settings-avatar");
    }
    private String sanitize(String input) {
        return (input == null) ? "" : input.trim();
    }
    private boolean isNameFormatValid(String name) {
        // can not be empty
        // can not beginn with a number
        // can include only letters,numbers and whitespaces

        String namePattern = "^[a-zA-Z][a-zA-Z0-9 ]*$";
        Pattern pattern = Pattern.compile(namePattern);
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }
    private boolean isEmailFormatValid(String email) {
        // can be empty
        // can not beginn with a number
        // must include one "@"
        // last domen must be at least 2 letter long ( e.g. ".uk")

        String emailPattern = "^[a-z][a-zA-Z0-9\\.\\_\\-]*@[a-z0-9\\.\\-]+\\.[a-z]{2,}$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        if (email.isEmpty()) {
            return true;
        }
        return matcher.find();
    }
    private void setErrorNameFieldStyle(TextField textField,Label messageLabel,String message) {
        textField.getStyleClass().clear();
        textField.getStyleClass().add("settings-name-field-error");
        messageLabel.setVisible(true);
        messageLabel.setText(message);
    }
    private void setDefaultNameFieldStyle(TextField textField,Label messageLabel) {
        textField.getStyleClass().clear();
        textField.getStyleClass().add("settings-name-field");
        messageLabel.setVisible(false);
    }
    private void setErrorEmailFieldStyle(TextField textField,Label messageLabel,String message) {
        textField.getStyleClass().clear();
        textField.getStyleClass().add("settings-email-field-error");
        messageLabel.setVisible(true);
        messageLabel.setText(message);
    }
    private void setDefaultEmailFieldStyle(TextField textField,Label messageLabel) {
        textField.getStyleClass().clear();
        textField.getStyleClass().add("settings-email-field");
        messageLabel.setVisible(false);
    }
    private boolean nameIsValid(String name) throws SQLException {
        if (name.isEmpty() || !isNameFormatValid(name)) {
            setErrorNameFieldStyle(settingsNameField,settingsNameErrorMessage,"Invalid information");
            emailGroup.setTranslateY(20);
            return false;
        }

        if (name.length() > 24) {
            setErrorNameFieldStyle(settingsNameField,settingsNameErrorMessage,"Name is too long! ( max. 25 character )");
            return false;
        }

        boolean userPresenceInDataBase = UsersDataBase.getUserPresence(name);
        String mainUserOldName = UsersDataBase.getNameWithId(mainUserId);
        if (userPresenceInDataBase && !name.equals(mainUserOldName)) {
            setErrorNameFieldStyle(settingsNameField,settingsNameErrorMessage,"Name is already taken!");
            return false;
        }

        return true;
    }
    private boolean emailIsValid(String email) throws SQLException {
        if (!email.isEmpty() && !isEmailFormatValid(email)) {
            setErrorEmailFieldStyle(settingsEmailField,settingsEmailErrorMessage,"Invalid information");
            return false;
        }

        if (email.length() > 38) {
            setErrorEmailFieldStyle(settingsNameField,settingsEmailErrorMessage,"Name is too long! ( max. 25 character )");
            return false;
        }

        boolean userPresenceInDataBase = UsersDataBase.getUserPresence(email);
        String mainUserOldEmail = UsersDataBase.getEmailWithId(mainUserId);
        if (userPresenceInDataBase && !email.equals(mainUserOldEmail)) {
            setErrorEmailFieldStyle(settingsEmailField,settingsEmailErrorMessage,"Email is already taken!");
            return false;
        }

        return true;
    }


    private void updateDataBaseAvatar() throws SQLException, FileNotFoundException {
        if (newAvatarPath.isEmpty()) {
            return;
        }

        if (newAvatarPath.equals("-")) {
            UsersDataBase.deleteAvatar(mainUserId);
        } else {
            UsersDataBase.setAvatar(mainUserId,newAvatarPath);
        }

    }
    private void updateDataBaseName(String name) throws SQLException {
        UsersDataBase.setName(mainUserId,name);
    }
    private void updateDataBaseEmail(String email) throws SQLException {
        UsersDataBase.setEmail(mainUserId,email);
    }
    private void setMainAvatarPicture() throws SQLException {
        Label mainAvatarLabel = (Label) mainAnchorPane.lookup("#mainAvatarLabel");
        if (UsersDataBase.getAvatarWithId(mainUserId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(mainUserId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setSmooth(true);
            mainAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(25);
            clip.setLayoutY(25);
            clip.setRadius(25);
            mainAvatarLabel.setClip(clip);
        } else {
            mainAvatarLabel.setGraphic(null);
            mainAvatarLabel.getStyleClass().clear();
            mainAvatarLabel.getStyleClass().add("avatar-button-default");
        }
    }
    private void setMainProfileInfo() throws SQLException {
        Label mainNameLabel = (Label) mainAnchorPane.lookup("#mainNameLabel");
        Label mainEmailLabel = (Label) mainAnchorPane.lookup("#mainEmailLabel");

        mainNameLabel.setText(UsersDataBase.getNameWithId(mainUserId));
        mainEmailLabel.setText(UsersDataBase.getEmailWithId(mainUserId));
        mainEmailLabel.setVisible(true);
        mainNameLabel.setLayoutY(23);

        if (UsersDataBase.getEmailWithId(mainUserId) == null) {
            mainEmailLabel.setVisible(false);
            mainNameLabel.setLayoutY(32);
        }
    }


    @FXML
    public void saveInformation() throws SQLException, FileNotFoundException {
        String name = sanitize(settingsNameField.getText());
        String email = sanitize(settingsEmailField.getText());

        setDefaultNameFieldStyle(settingsNameField,settingsNameErrorMessage);
        setDefaultEmailFieldStyle(settingsEmailField,settingsEmailErrorMessage);
        emailGroup.setTranslateY(0);

        boolean nameIsValid = nameIsValid(name);
        boolean emailIsValid = emailIsValid(email);
        if (nameIsValid && emailIsValid) {
            updateDataBaseAvatar();
            updateDataBaseName(name);
            updateDataBaseEmail(email);

            setMainAvatarPicture();
            setMainProfileInfo();
            hideWindow();
        }
    }
    @FXML
    public void hideWindow() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), settingsOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            mainAnchorPane.getChildren().remove(settingsBackground);
        });
        fadeOut.play();
    }
    @FXML
    public void logout() {
        try {
            Stage mainStage = (Stage) mainAnchorPane.getScene().getWindow();
            mainStage.close();  // Close the main window
            // Now open the login window
            Stage newLoginStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/AuthLogIn.fxml"));
            Scene scene = new Scene(loader.load());
            newLoginStage.setResizable(false);
            newLoginStage.setScene(scene);
            newLoginStage.setTitle("Log In");
            newLoginStage.show();  // Show the login window
        } catch (Exception e) {
            // Handle any exceptions (like FXML loading issues)
            settingsEmailErrorMessage.setText(e.getMessage());
        }
    }

}

