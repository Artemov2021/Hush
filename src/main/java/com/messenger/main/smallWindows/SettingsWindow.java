package com.messenger.main.smallWindows;

import com.messenger.database.UsersDataBase;
import com.messenger.main.MainContactList;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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


public class SettingsWindow {
    @FXML
    private Pane settingsBackgroundPane;
    @FXML
    private Pane settingsPane;
    @FXML
    private Label settingsAvatarLabel;
    @FXML
    private TextField settingsNameField;
    @FXML
    private Label settingsNameExceptionLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private TextField settingsEmailField;
    @FXML
    private Label settingsEmailExceptionLabel;
    @FXML
    private Pane buttonsBackgroundPane;
    @FXML
    private Pane buttonsBackgroundOverlay;
    @FXML
    private Label changeButton;
    @FXML
    private Label deleteButton;


    private int mainUserId;
    private AnchorPane mainAnchorPane;
    private Group emailGroup;

    private String newAvatarPath = "";


    public void setMainUserId(int id) throws SQLException {
        this.mainUserId = id;
    }
    public void setMainAnchorPane(AnchorPane mainAnchorPane) {
        this.mainAnchorPane = mainAnchorPane;
    }


    public void initializeWithValue() throws SQLException {
        initializeInterface();
        showOpeningEffect();
        setupClickHandlers();
    }


    public void initializeInterface() throws SQLException {
        createEmailGroup();
        defocusTextFields();
        setDataBaseAvatarPicture();
        setNameInField();
        setEmailInField();
        hideErrorLabels();
        putAvatarButtonsToFront();
        hideAvatarButtonsElements();
    }
    public void createEmailGroup() {
        emailGroup = new Group(emailLabel,settingsEmailField,settingsEmailExceptionLabel);
        settingsPane.getChildren().add(emailGroup);
        emailGroup.toBack();
    }
    private void showOpeningEffect() {
        // Appearing time
        FadeTransition FadeIn = new FadeTransition(Duration.millis(180),settingsBackgroundPane);
        FadeIn.setFromValue(0);
        FadeIn.setToValue(1);
        FadeIn.play();

        // Appearing move to left
        TranslateTransition translateIn = new TranslateTransition(Duration.millis(180), settingsPane);
        translateIn.setFromX(0);
        translateIn.setToX(-35);
        translateIn.play();
    }
    public void setupClickHandlers() {
        // Consume the event to prevent it from affecting backgroundPane
        settingsPane.setOnMouseClicked(Event::consume);

        // Set event handler for backgroundPane
        settingsBackgroundPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                hideWindow();
            }
        });

        // Set event handler for avatar label
        settingsAvatarLabel.setOnMouseClicked(this::checkEvent);
    }


    public void defocusTextFields() {
        settingsNameField.setFocusTraversable(false);
        settingsEmailField.setFocusTraversable(false);
    }
    private void setDataBaseAvatarPicture() throws SQLException {
        settingsAvatarLabel.getStyleClass().clear();
        if (UsersDataBase.getAvatarWithId(mainUserId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(mainUserId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(155);
            imageView.setFitWidth(155);
            imageView.setSmooth(true);
            settingsAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(77.5);
            clip.setLayoutY(77.5);
            clip.setRadius(77.5);
            settingsAvatarLabel.setClip(clip);
        } else {
            settingsAvatarLabel.getStyleClass().add("settings-avatar");
        }
    }
    private void setNameInField() throws SQLException {
        String mainUserName = UsersDataBase.getNameWithId(mainUserId);
        settingsNameField.setText(mainUserName);
    }
    private void setEmailInField() throws SQLException {
        String mainUserEmail = UsersDataBase.getEmailWithId(mainUserId);
        settingsEmailField.setText(mainUserEmail);
    }
    private void hideErrorLabels() {
        // Setting all error label to "invisible" mode
        settingsNameExceptionLabel.setVisible(false);
        settingsEmailExceptionLabel.setVisible(false);
    }
    private void putAvatarButtonsToFront() {
        buttonsBackgroundOverlay.toFront();
    }
    private void hideAvatarButtonsElements() {
        // Hiding change and delete avatar buttons
        buttonsBackgroundPane.setVisible(false);
        buttonsBackgroundOverlay.setVisible(false);
    }


    private void checkEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            Point2D paneCoordinates = settingsAvatarLabel.localToParent(mouseEvent.getX(), mouseEvent.getY());
            double x = paneCoordinates.getX();
            double y = paneCoordinates.getY();
            showAvatarsButton(x,y);
        }
    }
    private void showAvatarsButton(double x,double y) {
        // Setting buttons visibility, creating background pane
        buttonsBackgroundOverlay.setVisible(true);
        buttonsBackgroundPane.setVisible(true);
        buttonsBackgroundOverlay.setLayoutX(x);
        buttonsBackgroundOverlay.setLayoutY(y);

        // When you click away, buttons disappear
        buttonsBackgroundPane.setOnMouseClicked(event -> {
            buttonsBackgroundOverlay.setVisible(false);
            buttonsBackgroundPane.setVisible(false);
        });

        // When you click "change" button
        changeButton.setOnMouseClicked(ActionEvent -> {
            File selectedFile = getFileFromFileChooser();
            if (selectedFile != null) {
                newAvatarPath = selectedFile.getPath();
                setAvatarPicture(selectedFile.getPath());
            }
            buttonsBackgroundOverlay.setVisible(false);
            buttonsBackgroundPane.setVisible(false);
        });

        // When you click "delete" button
        deleteButton.setOnMouseClicked(ActionEvent -> {
            newAvatarPath = "-";
            setDefaultAvatar();
            buttonsBackgroundOverlay.setVisible(false);
            buttonsBackgroundPane.setVisible(false);
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
        settingsAvatarLabel.setGraphic(imageView);
        Circle clip = new Circle();
        clip.setLayoutX(77.5);
        clip.setLayoutY(77.5);
        clip.setRadius(77.5);
        settingsAvatarLabel.setClip(clip);
    }
    private void setDefaultAvatar() {
        settingsAvatarLabel.setGraphic(null);
        settingsAvatarLabel.getStyleClass().clear();
        settingsAvatarLabel.getStyleClass().add("settings-avatar");
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
            setErrorNameFieldStyle(settingsNameField,settingsNameExceptionLabel,"Invalid information");
            emailGroup.setTranslateY(20);
            return false;
        }

        if (name.length() > 24) {
            setErrorNameFieldStyle(settingsNameField,settingsNameExceptionLabel,"Name is too long! ( max. 25 character )");
            return false;
        }

        boolean userPresenceInDataBase = UsersDataBase.getUserPresence(name);
        String mainUserOldName = UsersDataBase.getNameWithId(mainUserId);
        if (userPresenceInDataBase && !name.equals(mainUserOldName)) {
            setErrorNameFieldStyle(settingsNameField,settingsNameExceptionLabel,"Name is already taken!");
            return false;
        }

        return true;
    }
    private boolean emailIsValid(String email) throws SQLException {
        if (!email.isEmpty() && !isEmailFormatValid(email)) {
            setErrorEmailFieldStyle(settingsEmailField,settingsEmailExceptionLabel,"Invalid information");
            return false;
        }

        if (email.length() > 38) {
            setErrorEmailFieldStyle(settingsNameField,settingsNameExceptionLabel,"Name is too long! ( max. 25 character )");
            return false;
        }

        boolean userPresenceInDataBase = UsersDataBase.getUserPresence(email);
        String mainUserOldEmail = UsersDataBase.getEmailWithId(mainUserId);
        if (userPresenceInDataBase && !email.equals(mainUserOldEmail)) {
            setErrorEmailFieldStyle(settingsEmailField,settingsEmailExceptionLabel,"Email is already taken!");
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
            mainNameLabel.setLayoutY(30);
        }
    }

    @FXML
    public void saveInformation() throws SQLException, FileNotFoundException {
        String name = sanitize(settingsNameField.getText());
        String email = sanitize(settingsEmailField.getText());

        setDefaultNameFieldStyle(settingsNameField,settingsNameExceptionLabel);
        setDefaultEmailFieldStyle(settingsEmailField,settingsEmailExceptionLabel);
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
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), settingsPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            mainAnchorPane.getChildren().remove(settingsBackgroundPane);
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
            settingsEmailExceptionLabel.setText(e.getMessage());
        }
    }



}

