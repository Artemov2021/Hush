package com.messenger.main.smallWindows;

import com.messenger.database.UsersDataBase;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
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
    private Label changeText;
    @FXML
    private Label changeSymbol;
    @FXML
    private Label deleteButton;
    @FXML
    private Label deleteText;
    @FXML
    private Label deleteSymbol;

    private int mainUserId;
    private AnchorPane mainAnchorPane;

    private String newAvatarPath = "";


    public void initializeWithValue() throws SQLException {
        setDataBaseAvatarPicture();
        setNameInField();
        setEmailInField();
        hideErrorLabels();
        hideAvatarButtonsElements();

        showOpeningEffect();

        // Consume the event to prevent it from affecting backgroundPane
        settingsPane.setOnMouseClicked(Event::consume);

        // Set event handler for backgroundPane
        settingsBackgroundPane.setOnMouseClicked(event -> {
            hideWindow();
        });

        // Set event handler for avatar label
        settingsAvatarLabel.setOnMouseClicked(this::checkEvent);
    }

    public void setMainUserId(int id) throws SQLException {
        this.mainUserId = id;
    }
    public void setMainAnchorPane(AnchorPane mainAnchorPane) {
        this.mainAnchorPane = mainAnchorPane;
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
    private void setDataBaseAvatarPicture() throws SQLException {
        settingsAvatarLabel.getStyleClass().clear();
        if (UsersDataBase.getAvatarWithId(mainUserId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(mainUserId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(90);
            imageView.setFitWidth(90);
            imageView.setSmooth(true);
            settingsAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(45);
            clip.setLayoutY(45);
            clip.setRadius(45);
            settingsAvatarLabel.setClip(clip);
        } else {
            settingsAvatarLabel.getStyleClass().add("settings-avatar-default");
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
        // Setting change and delete button on the right position, creating background pane
        buttonsBackgroundOverlay.setVisible(true);
        buttonsBackgroundPane.setVisible(true);
        buttonsBackgroundOverlay.setLayoutX(x);
        buttonsBackgroundOverlay.setLayoutY(y);

        // Making text and symbol invisible for mouse
        changeText.setMouseTransparent(true);
        changeSymbol.setMouseTransparent(true);
        deleteText.setMouseTransparent(true);
        deleteSymbol.setMouseTransparent(true);

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
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setSmooth(true);
        settingsAvatarLabel.setGraphic(imageView);
        Circle clip = new Circle();
        clip.setLayoutX(50);
        clip.setLayoutY(50);
        clip.setRadius(50);
        settingsAvatarLabel.setClip(clip);
    }
    private void setDefaultAvatar() {
        settingsAvatarLabel.setGraphic(null);
        settingsAvatarLabel.getStyleClass().clear();
        settingsAvatarLabel.getStyleClass().add("settings-avatar-default");
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
    private void setErrorFieldStyle(TextField textField,Label messageLabel,String message) {
        textField.getStyleClass().clear();
        textField.getStyleClass().add("settings-identifier-field-error");
        messageLabel.setVisible(true);
        messageLabel.setText(message);
    }
    private void setDefaultFieldStyle(TextField textField,Label messageLabel) {
        textField.getStyleClass().clear();
        textField.getStyleClass().add("settings-identifier-field");
        messageLabel.setVisible(false);
    }
    private boolean nameIsValid(String name) throws SQLException {
        if (name.isEmpty() || !isNameFormatValid(name)) {
            setErrorFieldStyle(settingsNameField,settingsNameExceptionLabel,"Invalid information");
            return false;
        }

        if (name.length() > 25) {
            setErrorFieldStyle(settingsNameField,settingsNameExceptionLabel,"Name is too long! ( max. 25 character )");
            return false;
        }

        boolean userPresenceInDataBase = UsersDataBase.getUserPresence(name);
        String mainUserOldName = UsersDataBase.getNameWithId(mainUserId);
        if (userPresenceInDataBase && !name.equals(mainUserOldName)) {
            setErrorFieldStyle(settingsNameField,settingsNameExceptionLabel,"Name is already taken!");
            return false;
        }

        return true;
    }
    private boolean emailIsValid(String email) throws SQLException {
        if (!email.isEmpty() && !isEmailFormatValid(email)) {
            setErrorFieldStyle(settingsEmailField,settingsEmailExceptionLabel,"Invalid information");
            return false;
        }

        if (email.length() > 25) {
            setErrorFieldStyle(settingsNameField,settingsNameExceptionLabel,"Name is too long! ( max. 25 character )");
            return false;
        }

        boolean userPresenceInDataBase = UsersDataBase.getUserPresence(email);
        String mainUserOldEmail = UsersDataBase.getEmailWithId(mainUserId);
        if (userPresenceInDataBase && !email.equals(mainUserOldEmail)) {
            setErrorFieldStyle(settingsEmailField,settingsEmailExceptionLabel,"Email is already taken!");
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
            imageView.setFitHeight(34);
            imageView.setFitWidth(34);
            imageView.setSmooth(true);
            mainAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(17);
            clip.setLayoutY(17);
            clip.setRadius(17);
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
        mainNameLabel.setLayoutY(22);

        if (UsersDataBase.getEmailWithId(mainUserId) == null) {
            mainEmailLabel.setVisible(false);
            mainNameLabel.setLayoutY(30);
        }
    }

    @FXML
    public void saveInformation() throws SQLException, FileNotFoundException {
        String name = sanitize(settingsNameField.getText());
        String email = sanitize(settingsEmailField.getText());

        setDefaultFieldStyle(settingsNameField,settingsNameExceptionLabel);
        setDefaultFieldStyle(settingsEmailField,settingsEmailExceptionLabel);

        if (nameIsValid(name) && emailIsValid(email)) {
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

}

