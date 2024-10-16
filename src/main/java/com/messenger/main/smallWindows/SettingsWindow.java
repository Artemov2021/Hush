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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
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
    private Button settingsSaveButton;
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
    private String newName = "";
    private String newEmail = "";


    public void initializeWithValue() throws SQLException {
        showOpeningEffect();
        setDataBaseAvatarPicture();
        setNameInField();
        setEmailInField();

        hideExceptionLabels();
        hideAvatarButtonsElements();

        // Consume the event to prevent it from affecting backgroundPane
        settingsPane.setOnMouseClicked(Event::consume);

        // Set event handler for backgroundPane
        settingsBackgroundPane.setOnMouseClicked(event -> {
            hideWindow();
        });

        // Set event handler for avatar label
        settingsAvatarLabel.setOnMouseClicked(mouseEvent -> {
            checkEvent(mouseEvent);
        });
//
//        saveButton.setOnAction(event -> {
//            String newName = nameField.getText() != null ? nameField.getText().trim() : "";
//            String newEmail = emailField.getText() != null ? emailField.getText().trim() : "";
//
//            try {
//
//                //------------------ Checking Fields Information Validity -------------------
//
//                boolean nameEquality = Objects.equals(name, newName);   // if new name equals old name: name is valid
//                boolean nameValidity = isNameValid(newName) && newName.length() <= 25;  // if the name is valid and is not longer than 25 character: name is valid
//                boolean nameDataBasePresence = UsersDataBase.getUserPresence(newName);
//                boolean emailEquality = Objects.equals(email, newEmail);  // if new email equals old email: email is valid
//                boolean emailValidity = newEmail.isEmpty() || (isEmailValid(newEmail) && newEmail.length() <= 25);  // empty or valid and not longer than 25: email is valid
//                boolean emailDataBasePresence = UsersDataBase.getUserPresence(newEmail);
//
//
//                if ((nameEquality || (nameValidity && !nameDataBasePresence)) && (emailEquality || (emailValidity && !emailDataBasePresence))) {
//                    setDefaultFields();
//                    hideErrorLabels();
//
//                    changeDataBaseInfo(newName,newEmail);
//                    changeDetailedDataBase(newName);
//                    changeAvatarsInFolder(newName);
//                    changeAvatarInUsersDB(newName);
//                    changeMainLabels(newName,newEmail);
//                    changeMainAvatar(34);
//
//                    hideWindow();
//                } else {
//                    handleNameField(newName);
//                    handleEmailField(newEmail);
//                }
//
//            } catch (Exception e) {
//                emailErrorLabel.setText(e.getMessage());
//            }
//
//        });
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
            imageView.setFitHeight(100);
            imageView.setFitWidth(100);
            imageView.setSmooth(true);
            settingsAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(50);
            clip.setLayoutY(50);
            clip.setRadius(50);
            settingsAvatarLabel.setClip(clip);
        } else {
            settingsAvatarLabel.getStyleClass().add("avatar-button-default");
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
    private void hideExceptionLabels() {
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

        deleteButton.setOnMouseClicked(ActionEvent -> {
            setDefaultAvatar();
            newAvatarPath = "";
            // TODO

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













//    private void hideErrorLabels() {
//        nameErrorLabel.setVisible(false);
//        emailErrorLabel.setVisible(false);
//    }
//
//    private void setDefaultFields() {
//        nameField.getStyleClass().clear();
//        nameField.getStyleClass().add("settings-identifier-field");
//        emailField.getStyleClass().clear();
//        emailField.getStyleClass().add("settings-identifier-field");
//    }


    @FXML
    private void hideWindow() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), settingsPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            mainAnchorPane.getChildren().remove(settingsBackgroundPane);
        });
        fadeOut.play();
    }

//    private void setAvatarPicture(Label avatar, URL imageURL, int size) {
//        ImageView imageView = new ImageView(new Image(imageURL.toString()));
//        imageView.setFitHeight(size);
//        imageView.setFitWidth(size);
//        imageView.setSmooth(true);
//        avatar.setGraphic(imageView);
//        Circle clip = new Circle();
//        clip.setLayoutX((double) size / 2);
//        clip.setLayoutY((double) size / 2);
//        clip.setRadius((double) size / 2);
//        avatar.setClip(clip);
//    }
//
//    private void showAvatarButtons(double x, double y) {
//
//    }
//
//    private void changeDetailedDataBase(String newName) {
//        File oldDataBaseFile = new File("details/"+name+".db");
//        File newDataBaseFile = new File("details/"+newName+".db");
//        if (!oldDataBaseFile.renameTo(newDataBaseFile)) {
//            emailErrorLabel.setText("data base was not renamed");
//        }
//    }
//
//    private boolean isNameValid(String name) {
//        // can not be empty
//        // can not beginn with a number
//        // can include only letters,numbers and whitespaces
//
//        String namePattern = "^[a-zA-Z][a-zA-Z0-9 ]*$";
//        Pattern pattern = Pattern.compile(namePattern);
//        Matcher matcher = pattern.matcher(name);
//        return matcher.find();
//    }
//
//    private boolean isEmailValid(String email) {
//        // can be empty
//        // can not beginn with a number
//        // must include one "@"
//        // last domen must be at least 2 letter long ( e.g. ".uk")
//
//        String emailPattern = "^[a-z][a-zA-Z0-9\\.\\_\\-]*@[a-z0-9\\.\\-]+\\.[a-z]{2,}$";
//        Pattern pattern = Pattern.compile(emailPattern);
//        Matcher matcher = pattern.matcher(email);
//        if (email.isEmpty()) {
//            return true;
//        }
//        return matcher.find();
//    }
//
//    private void handleNameField(String newName) throws SQLException {
//        nameField.getStyleClass().clear();
//        nameField.getStyleClass().add("settings-identifier-field");
//        nameErrorLabel.setVisible(false);
//        boolean hasException = false;
//        String exceptionMessage = "";
//
//        if (!isNameValid(newName)) {
//            hasException = true;
//            exceptionMessage = "invalid name";
//        } else if (!Objects.equals(name, newName) && UsersDataBase.getUserPresence(newName)) {
//            hasException = true;
//            exceptionMessage = "Name is already taken";
//        } else if (newName.length() > 25) {
//            hasException = true;
//            exceptionMessage = "Name is too long";
//        }
//
//        if (hasException) {
//            nameErrorLabel.setVisible(true);
//            nameErrorLabel.setText(exceptionMessage);
//
//            nameField.getStyleClass().clear();
//            nameField.getStyleClass().add("settings-identifier-field-error");
//        }
//    }
//
//    private void handleEmailField(String newEmail) throws SQLException {
//        emailField.getStyleClass().clear();
//        emailField.getStyleClass().add("settings-identifier-field");
//        emailErrorLabel.setVisible(false);
//        boolean hasException = false;
//        String exceptionMessage = "";
//
//        if (!isEmailValid(newEmail)) {
//            hasException = true;
//            exceptionMessage = "invalid email";
//        } else if (!Objects.equals(email, newEmail) && UsersDataBase.getUserPresence(newEmail)) {
//            hasException = true;
//            exceptionMessage = "Email is already taken";
//        } else if (newEmail.length() > 25) {
//            hasException = true;
//            exceptionMessage = "Email is too long";
//        }
//
//        if (hasException) {
//            emailErrorLabel.setVisible(true);
//            emailErrorLabel.setText(exceptionMessage);
//
//            emailField.getStyleClass().clear();
//            emailField.getStyleClass().add("settings-identifier-field-error");
//        }
//    }
//
//    public void changeDataBaseInfo(String newName,String newEmail) throws SQLException, IOException {
//        UsersDataBase.changeName(name,newName);
//        UsersDataBase.changeEmail(newName,newEmail);
//    }
//
//    private void changeAvatarsInFolder(String newName) throws IOException {
//        File oldOriginalAvatar = new File("src/main/resources/avatars/"+name+"Original.png");
//        File oldCroppedAvatar = new File("src/main/resources/avatars/"+name+"Cropped.png");
//
//        // if user has already an avatar:
//        if (oldCroppedAvatar.exists() && oldCroppedAvatar.exists()) {
//            // if avatar was deleted:
//            if (avatarIsChanged && pathToNewAvatar == null) {
//                deleteAvatars();
//            // if avatar was changed:
//            } else if (avatarIsChanged && pathToNewAvatar != null) {
//                deleteAvatars();
//                imageResizer(new File(pathToNewAvatar),newName);
//            // if avatar was NOT changed:
//            } else {
//                File newOriginalAvatar = new File("src/main/resources/avatars/"+newName+"Original.png");
//                File newCroppedAvatar = new File("src/main/resources/avatars/"+newName+"Cropped.png");
//
//                if (!oldOriginalAvatar.renameTo(newOriginalAvatar)) {
//                    throw new IOException("File was not created");
//                }
//                if (!oldCroppedAvatar.renameTo(newCroppedAvatar)) {
//                    throw new IOException("File was not created");
//                }
//            }
//        // if user does not have an avatar:
//        } else if (!oldCroppedAvatar.exists() && !oldCroppedAvatar.exists()) {
//
//            if (avatarIsChanged && pathToNewAvatar != null) {
//                imageResizer(new File(pathToNewAvatar),newName);
//            }
//        } else {
//            emailErrorLabel.setText("Some of avatars misses");
//        }
//    }
//
//    public void changeMainLabels(String newName,String newEmail) {
//        Label nameLabel = (Label) mainAnchorPane.getScene().lookup("#mainNameLabel");
//        Label emailLabel = (Label) mainAnchorPane.getScene().lookup("#mainEmailLabel");
//        nameLabel.setText(newName);
//        if (newEmail.isEmpty()) {
//            emailLabel.setText("");
//            emailLabel.setVisible(false);
//            nameLabel.setLayoutY(30);
//        } else {
//            emailLabel.setText(newEmail);
//            emailLabel.setVisible(true);
//            nameLabel.setLayoutY(22);
//        }
//    }
//
//    private void changeAvatarInUsersDB(String newName) throws SQLException {
//        if (pathToNewAvatar == null) {
//            UsersDataBase.setAvatar(newName,null);
//        } else if (pathToNewAvatar.length() > 0){
//            UsersDataBase.setAvatar(newName,newName+"Cropped.png");
//        } else {
//            if (UsersDataBase.getAvatarWithId(mainUserId) != null) {
//                UsersDataBase.setAvatar(newName,newName+"Cropped.png");
//            }
//        }
//    }
//
//    private void changeMainAvatar(int size) throws MalformedURLException {
//        Label mainAvatarLabel = (Label) mainAnchorPane.getScene().lookup("#mainAvatarLabel");
//        if (pathToNewAvatar == null) {
//            mainAvatarLabel.setGraphic(null);
//            mainAvatarLabel.getStyleClass().clear();
//            mainAvatarLabel.getStyleClass().add("avatar-button-default");
//        } else if (pathToNewAvatar.length() > 0) {
//            setAvatarPicture(mainAvatarLabel,new File(pathToNewAvatar).toURL(),size);
//        }
//    }
//
//    private void deleteAvatars() {
//        String avatarsFolderPath = "src/main/resources/avatars";
//        File avatarOriginal = new File(avatarsFolderPath + File.separator + name + "Original.png");
//        File avatarCropped = new File(avatarsFolderPath + File.separator + name + "Cropped.png");
//        if (avatarOriginal.delete()) {
//            emailErrorLabel.setText("Avatars were not deleted");
//        }
//        if (avatarCropped.delete()) {
//            emailErrorLabel.setText("Avatars were not deleted");
//        }
//    }
//
//    private void imageResizer(File image,String newName) throws IOException {
//        BufferedImage originalImage = ImageIO.read(image);
//        /* ----------------- Deleting Old Avatars ------------------ */
//
//        // deleting old avatars, that has old name
//        deleteAvatars();
//
//        /* ----------------- Saving Original Avatar Picture ------------------ */
//
//        String avatarsFolderPath = "src/main/resources/avatars";
//        File avatarsFolderFile = new File(avatarsFolderPath);
//        File outPutFile = new File(avatarsFolderFile,newName + "Original.png");
//        ImageIO.write(originalImage,"png",outPutFile);
//
//        /* ------------------------------------------------------------------- */
//
//        int originalWidth = originalImage.getWidth();
//        int originalHeight = originalImage.getHeight();
//
//        int cropLeft = 0;
//        int cropRight = 0;
//        int cropUp = 0;
//        int cropDown = 0;
//
//        if (originalWidth > originalHeight) {
//            cropLeft = (originalWidth - originalHeight) / 2;
//            cropRight = (originalWidth - originalHeight) / 2;
//        } else if (originalHeight > originalWidth) {
//            cropUp = (originalHeight - originalWidth) / 2;
//            cropDown = (originalHeight - originalWidth) / 2;
//        }
//
//        int newWidth = originalWidth - cropLeft - cropRight;
//        int newHeight = originalHeight - cropDown - cropUp;
//
//        BufferedImage croppedImage = (originalWidth > originalHeight) ? originalImage.getSubimage(cropLeft, 0, newWidth, newHeight) : originalImage.getSubimage(0,cropUp,newWidth,newHeight);
//
//        /* ----------------- Saving Cropped Avatar Picture ------------------ */
//
//        File outputfile = new File(avatarsFolderFile,newName + "Cropped.png");
//        ImageIO.write(croppedImage, "png", outputfile);
//
//        /* ------------------------------------------------------------------- */
//    }

}

