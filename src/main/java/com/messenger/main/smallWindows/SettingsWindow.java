package com.messenger.main.smallWindows;

import com.messenger.Log;
import com.messenger.database.UsersDataBase;
import com.messenger.exceptions.*;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsWindow {
    private String name;
    private String email;
    private String avatar;
    private AnchorPane anchorPane;

    private boolean avatarIsChanged = false;
    String pathToNewAvatar = "";

    public SettingsWindow(String name,AnchorPane anchorPane) throws SQLException {
        this.name = name;
        this.anchorPane = anchorPane;
        this.email = UsersDataBase.getEmailWithName(name);
        this.avatar = UsersDataBase.getAvatar(name);
    }

    public void openWindow() throws SQLException, IOException {
        Pane overlay = new Pane();
        overlay.setPrefWidth(anchorPane.getPrefWidth());
        overlay.setPrefHeight(anchorPane.getPrefHeight());
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        Pane settingsPane = new Pane();
        settingsPane.setPrefWidth(354);
        settingsPane.setPrefHeight(391);
        settingsPane.getStyleClass().add("settings-window");
        settingsPane.setLayoutX(475);
        settingsPane.setLayoutY(120);

        FadeTransition FadeIn = new FadeTransition(Duration.millis(180),overlay);
        FadeIn.setFromValue(0);
        FadeIn.setToValue(1);
        FadeIn.play();

        TranslateTransition translateIn = new TranslateTransition(Duration.millis(180), settingsPane);
        translateIn.setFromX(0);
        translateIn.setToX(-35);
        translateIn.play();

        setContactWindowComponents(overlay, settingsPane);
        anchorPane.getChildren().addAll(overlay, settingsPane);

        overlay.setOnMouseClicked(event -> {
            hideWindow(overlay, settingsPane);
        });
    }

    private void hideWindow(Pane overlay, Pane settingsPane) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), settingsPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            anchorPane.getChildren().removeAll(overlay,settingsPane);
        });
        fadeOut.play();
    }

    private void setContactWindowComponents(Pane overlay, Pane settingsPane) throws SQLException, IOException {
        Button exitButton = new Button();
        exitButton.setPrefWidth(30);
        exitButton.getStyleClass().add("exit-button");
        exitButton.setLayoutX(314);
        exitButton.setLayoutY(13);

        Label title = new Label("Settings");
        title.getStyleClass().add("settings-title");
        title.setLayoutX(28);
        title.setLayoutY(20);

        Label avatarPicture = new Label();
        if (UsersDataBase.getAvatar(name) == null) {
            avatarPicture.getStyleClass().add("settings-avatar-default");
        } else {
            URL imageURL = new File("src/main/resources/avatars" + File.separator + UsersDataBase.getAvatar(name)).toURL();
            setAvatarPicture(imageURL,avatarPicture,90);
        }
        avatarPicture.setLayoutX(145);
        avatarPicture.setLayoutY(65);
        avatarPicture.setPrefWidth(91);
        avatarPicture.setPrefHeight(89);

        Label nameLabel = new Label("Name");
        nameLabel.getStyleClass().add("settings-identifier-label");
        nameLabel.setLayoutX(48);
        nameLabel.setLayoutY(170);

        TextField nameField = new TextField();
        nameField.getStyleClass().add("settings-identifier-field");
        nameField.setLayoutX(43);
        nameField.setLayoutY(193);
        nameField.setPrefWidth(273);
        nameField.setPrefHeight(39);
        nameField.setText(name);

        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add("settings-identifier-label");
        emailLabel.setLayoutX(48);
        emailLabel.setLayoutY(251);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.getStyleClass().add("settings-identifier-field");
        emailField.setLayoutX(43);
        emailField.setLayoutY(272);
        emailField.setPrefWidth(273);
        emailField.setPrefHeight(39);
        emailField.setText(email);

        Button saveButton = new Button();
        saveButton.getStyleClass().add("settings-save-button");
        saveButton.setLayoutX(255);
        saveButton.setLayoutY(335);
        saveButton.setPrefWidth(77);
        saveButton.setPrefHeight(31);

        Button cancelButton = new Button();
        cancelButton.setPrefWidth(78);
        cancelButton.setPrefHeight(29);
        cancelButton.getStyleClass().add("settings-cancel-button");
        cancelButton.setLayoutX(167);
        cancelButton.setLayoutY(336);

        Label emailErrorLabel = new Label("Error label exceptions");
        emailErrorLabel.getStyleClass().add("settings-error-label");
        emailErrorLabel.setPrefHeight(8);
        emailErrorLabel.setPrefWidth(150);
        emailErrorLabel.setLayoutX(48);
        emailErrorLabel.setLayoutY(312);
        emailErrorLabel.setVisible(false);

        Label nameErrorLabel = new Label("Error label exceptions");
        nameErrorLabel.getStyleClass().add("settings-error-label");
        nameErrorLabel.setPrefHeight(8);
        nameErrorLabel.setPrefWidth(150);
        nameErrorLabel.setLayoutX(48);
        nameErrorLabel.setLayoutY(233);
        nameErrorLabel.setVisible(false);



        // Adds all components to the contact pane
        settingsPane.getChildren().addAll(
                exitButton,
                title,
                avatarPicture,
                nameLabel,
                nameField,
                emailLabel,
                emailField,
                saveButton,
                cancelButton,
                emailErrorLabel,
                nameErrorLabel
        );

        exitButton.setOnAction(actionEvent -> {
            hideWindow(overlay, settingsPane);
        });
        cancelButton.setOnAction(actionEvent -> {
            hideWindow(overlay, settingsPane);
        });

        avatarPicture.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                Point2D paneCoordinates = avatarPicture.localToParent(mouseEvent.getX(), mouseEvent.getY());
                double x = paneCoordinates.getX();
                double y = paneCoordinates.getY();
                showAvatarButtons(settingsPane,x,y,avatarPicture);
            }
        });


        saveButton.setOnAction(ActionEvent -> {
            try {
                emailErrorLabel.setVisible(false);
                nameErrorLabel.setVisible(false);

                String newEmail = emailField.getText() != null ? emailField.getText().trim() : "";
                String newName = nameField.getText() != null ? nameField.getText().trim() : "";

                if (nameIsValid(newName) && (Objects.equals(name,newName) || !UsersDataBase.checkUserPresence(newName)) &&
                        emailIsValid(newEmail) && (Objects.equals(email,newEmail) || !UsersDataBase.checkUserPresence(newEmail))) {
                    changeDBInfo(this.name,newName,newEmail);
                    changeProfilInfo(newName,newEmail);
                    if (avatarIsChanged && !pathToNewAvatar.isEmpty()) {
                        changeAvatar(new File(pathToNewAvatar));
                    } else if (avatarIsChanged && pathToNewAvatar.isEmpty()) {
                        avatarPicture.getStyleClass().clear();
                        avatarPicture.getStyleClass().add("settings-avatar-default");
                    }
                    hideWindow(overlay,settingsPane);
                } else {
                    System.out.println("Checking");
                    checkFields(newName,newEmail);
                }

            } catch (InvalidName | NameAlreadyInDataBase nameException) {

                nameErrorLabel.setText(nameException.getMessage());
                nameErrorLabel.setVisible(true);
                nameField.getStyleClass().clear();
                nameField.getStyleClass().add("settings-identifier-field-error");

            } catch (InvalidEmail | EmailAlreadyInDataBase emailException ) {

                emailErrorLabel.setText(emailException.getMessage());
                emailErrorLabel.setVisible(true);
                emailField.getStyleClass().clear();
                emailField.getStyleClass().add("settings-identifier-field-error");

            } catch (Exception e) {

                emailErrorLabel.setText(e.getMessage());
                emailErrorLabel.setVisible(true);
                try {
                    Log.writeNewExceptionLog(e);
                } catch (IOException ioException) {
                    emailErrorLabel.setText(ioException.getMessage());
                }
            }

        });

    }

    private void setAvatarPicture(URL imageURL, Label avatarLabel, int size) throws SQLException {
        ImageView imageView = new ImageView(new Image(imageURL.toString()));
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        imageView.setSmooth(true);
        avatarLabel.setGraphic(imageView);
        Circle clip = new Circle();
        clip.setLayoutX(size / 2);
        clip.setLayoutY(size / 2);
        clip.setRadius(size / 2);
        avatarLabel.setClip(clip);
    }

    private void checkFields(String newName,String newEmail) throws InvalidName, SQLException, IOException, NameAlreadyInDataBase, EmailAlreadyInDataBase, InvalidEmail {
        if (!nameIsValid(newName)) {
            throw new InvalidName("Invalid name");
        }
        if (!newName.equals(name) && UsersDataBase.checkUserPresence(newName)) {
            throw new NameAlreadyInDataBase("Name is already taken");
        }

        if (!emailIsValid(newEmail)) {
            throw new InvalidEmail("Invalid email");
        }
        if (!Objects.equals(email,newEmail) && UsersDataBase.checkUserPresence(newEmail)) {
            throw new EmailAlreadyInDataBase("Email is already taken");
        }
    }

    private void changeDBInfo(String oldName,String newName,String newEmail) throws SQLException, IOException {
        UsersDataBase.changeName(oldName,newName);
        UsersDataBase.changeEmail(newName,newEmail);
        File oldFile = new File("details/"+oldName+".db");
        File newFile = new File("details/"+newName+".db");
        if (!oldFile.renameTo(newFile)) {
            Log.writeNewExceptionLog(new IOException("File was not created"));
            throw new IOException("File was not created");
        }
    }

    private void changeProfilInfo(String newName,String newEmail) throws SQLException, MalformedURLException {
        Label nameLabel = (Label) anchorPane.getScene().lookup("#nameLabel");
        Label emailLabel = (Label) anchorPane.getScene().lookup("#emailLabel");
        Label avatar = (Label) anchorPane.getScene().lookup("#avatarLabel");
        nameLabel.setText(newName);
        if (newEmail.isEmpty()) {
            emailLabel.setText("");
            emailLabel.setVisible(false);
            nameLabel.setLayoutY(30);
        } else {
            emailLabel.setText(newEmail);
            emailLabel.setVisible(true);
            nameLabel.setLayoutY(22);
        }
        if (avatarIsChanged) {
            if (pathToNewAvatar.isEmpty()) {
                avatar.getStyleClass().clear();
                avatar.getStyleClass().add("avatar-button-default");
                return;
            }
            URL newAvatarURL = new File(pathToNewAvatar).toURL();
            setAvatarPicture(newAvatarURL,avatar,34);
        }
    }

    private boolean emailIsValid(String email) {
        // can be empty
        // can not beginn with a number
        // must include one "@"
        // last domen must be at least 2 letter long ( e.g. ".uk")
        // can not be longer than 25

        String emailPattern = "^[a-z][a-zA-Z0-9\\.\\_\\-]*@[a-z0-9\\.\\-]+\\.[a-z]{2,}$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        if (email.isEmpty()) {
            return true;
        }
        return matcher.find() && email.length() <= 25;
    }

    private boolean nameIsValid(String name) {
        // can not be empty
        // can not beginn with a number
        // can include only letters,numbers and whitespaces
        // can not be longer than 25;

        String namePattern = "^[a-zA-Z][a-zA-Z0-9 ]*$";
        Pattern pattern = Pattern.compile(namePattern);
        Matcher matcher = pattern.matcher(name);
        return matcher.find() && name.length() <= 25;
    }



    private void imageResizer(File image) throws IOException {
        BufferedImage originalImage = ImageIO.read(image);

        /* ----------------- Saving Original Avatar Picture ------------------*/

        String avatarsFolderPath = "src/main/resources/avatars";
        File avatarsFolderFile = new File(avatarsFolderPath);
        File outPutFile = new File(avatarsFolderFile,name + "Original.png");
        ImageIO.write(originalImage,"png",outPutFile);

        /* -------------------------------------------------------------------*/

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        int cropLeft = 0;
        int cropRight = 0;
        int cropUp = 0;
        int cropDown = 0;

        if (originalWidth > originalHeight) {
            cropLeft = (originalWidth - originalHeight) / 2;
            cropRight = (originalWidth - originalHeight) / 2;
        } else if (originalHeight > originalWidth) {
            cropUp = (originalHeight - originalWidth) / 2;
            cropDown = (originalHeight - originalWidth) / 2;
        }

        int newWidth = originalWidth - cropLeft - cropRight;
        int newHeight = originalHeight - cropDown - cropUp;

        BufferedImage croppedImage = (originalWidth > originalHeight) ? originalImage.getSubimage(cropLeft, 0, newWidth, newHeight) : originalImage.getSubimage(0,cropUp,newWidth,newHeight);

        /* ----------------- Saving Cropped Avatar Picture ------------------*/

        File outputfile = new File(avatarsFolderFile,name + "Cropped.png");
        ImageIO.write(croppedImage, "png", outputfile);

        /* -------------------------------------------------------------------*/
    }

    private void showAvatarButtons(Pane settingsPane,double x,double y,Label avatarLabel) {
        Pane buttonsBackground = new Pane();
        buttonsBackground.getStyleClass().add("settings-avatar-buttons-background");
        buttonsBackground.setPrefWidth(100);
        buttonsBackground.setPrefHeight(60);
        buttonsBackground.setLayoutX(x);
        buttonsBackground.setLayoutY(y);

        Label changeButton = new Label();
        changeButton.setLayoutX(4);
        changeButton.setLayoutY(4);
        changeButton.setPrefWidth(92);
        changeButton.setPrefHeight(25);
        changeButton.getStyleClass().add("settings-avatar-button");

        Label changeSymbol = new Label();
        changeSymbol.setMouseTransparent(true);
        changeSymbol.getStyleClass().add("settings-avatar-button-change-symbol");
        changeSymbol.setPrefWidth(12);
        changeSymbol.setPrefHeight(7);
        changeSymbol.setLayoutX(10);
        changeSymbol.setLayoutY(9);

        Label changeText = new Label("Change picture");
        changeText.setMouseTransparent(true);
        changeText.getStyleClass().add("settings-avatar-button-change-text");
        changeText.setPrefWidth(70);
        changeText.setPrefHeight(7);
        changeText.setLayoutX(28);
        changeText.setLayoutY(9);

        Label deleteButton = new Label();
        deleteButton.setLayoutX(4);
        deleteButton.setLayoutY(30);
        deleteButton.setPrefWidth(92);
        deleteButton.setPrefHeight(25);
        deleteButton.getStyleClass().add("settings-avatar-button");

        Label deleteSymbol = new Label();
        deleteSymbol.setMouseTransparent(true);
        deleteSymbol.getStyleClass().add("settings-avatar-button-delete-symbol");
        deleteSymbol.setPrefWidth(18);
        deleteSymbol.setPrefHeight(7);
        deleteSymbol.setLayoutX(9);
        deleteSymbol.setLayoutY(35);

        Label deleteText = new Label("Delete picture");
        deleteText.setMouseTransparent(true);
        deleteText.getStyleClass().add("settings-avatar-button-change-text");
        deleteText.setPrefWidth(70);
        deleteText.setPrefHeight(7);
        deleteText.setLayoutX(28);
        deleteText.setLayoutY(35);

        buttonsBackground.getChildren().add(changeButton);
        buttonsBackground.getChildren().add(changeSymbol);
        buttonsBackground.getChildren().add(changeText);
        buttonsBackground.getChildren().add(deleteButton);
        buttonsBackground.getChildren().add(deleteSymbol);
        buttonsBackground.getChildren().add(deleteText);

        Pane backgroundPane = new Pane();
        backgroundPane.setLayoutX(0);
        backgroundPane.setLayoutY(0);
        backgroundPane.setPrefWidth(354);
        backgroundPane.setPrefHeight(391);

        settingsPane.getChildren().add(backgroundPane);
        settingsPane.getChildren().add(buttonsBackground);

        backgroundPane.setOnMouseClicked(ActionEvent -> {
            backgroundPane.setVisible(false);
            buttonsBackground.setVisible(false);
        });

        changeButton.setOnMouseClicked(ActionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select an image");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files","*.png","*.jpg"));
            File selectedFile = fileChooser.showOpenDialog(anchorPane.getScene().getWindow());
            if (selectedFile != null) {

                try {
                    setAvatarPicture(selectedFile.toURL(),avatarLabel,90);
                    avatarIsChanged = true;
                    pathToNewAvatar = selectedFile.getPath();
                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }

            }
            backgroundPane.setVisible(false);
            buttonsBackground.setVisible(false);
        });

        deleteButton.setOnMouseClicked(ActionEvent -> {
            System.out.println("Delete");
            backgroundPane.setVisible(false);
            buttonsBackground.setVisible(false);
        });
    }

    private void changeAvatar(File image) throws SQLException, IOException {
        imageResizer(image);
        String newAvatar = name + "Cropped.png";
        UsersDataBase.setAvatar(name,newAvatar);
    }
}
