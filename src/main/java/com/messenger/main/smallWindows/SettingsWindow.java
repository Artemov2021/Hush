package com.messenger.main.smallWindows;

import com.messenger.database.UsersDataBase;
import com.messenger.main.MainContactList;
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
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class SettingsWindow {
    private String name;
    private String email;
    private String avatar;
    private AnchorPane anchorPane;

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

    private void hideWindow(Pane overlay, Pane contactPane) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), contactPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            anchorPane.getChildren().removeAll(overlay,contactPane);
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
            String avatarUrl = "/avatars/" + UsersDataBase.getAvatar(name);
            URL url = MainContactList.class.getResource(avatarUrl);
            imageResizer(new URL(url.toString().replaceAll("target/classes","src/main/resources")));
            ImageView imageView = new ImageView(new Image(url.toString().replaceAll("target/classes","src/main/resources")));
            imageView.setFitHeight(90);
            imageView.setFitWidth(90);
            imageView.setSmooth(true);
            avatarPicture.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(45);
            clip.setLayoutY(45);
            clip.setRadius(45);
            avatarPicture.setClip(clip);
        }
        avatarPicture.setLayoutX(145);
        avatarPicture.setLayoutY(65);
        avatarPicture.setPrefWidth(91);
        avatarPicture.setPrefHeight(89);

        Label nameLabel = new Label("Name");
        nameLabel.getStyleClass().add("settings-identifier-label");
        nameLabel.setLayoutX(50);
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
        emailLabel.setLayoutX(50);
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
                cancelButton
        );

        exitButton.setOnAction(actionEvent -> {
            hideWindow(overlay, settingsPane);
        });
        avatarPicture.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                Point2D paneCoordinates = avatarPicture.localToParent(mouseEvent.getX(), mouseEvent.getY());
                double x = paneCoordinates.getX();
                double y = paneCoordinates.getY();
                showAvatarButtons(settingsPane,x,y);
            }
        });

    }

    private void imageResizer(URL imageUrl) throws IOException {
        BufferedImage originalImage = ImageIO.read(imageUrl);

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

        // Crop the image
        BufferedImage croppedImage = (originalWidth > originalHeight) ? originalImage.getSubimage(cropLeft, 0, newWidth, newHeight) : originalImage.getSubimage(0,cropUp,newWidth,newHeight);

        // Convert URL to a File and extract the path
        File inputFile = new File(imageUrl.getPath());

        // Create a new file name for the cropped image
        String newFileName = inputFile.getParent() + File.separator + inputFile.getName().replaceAll("\\.png","Cropped\\.png");
        File outputfile = new File(newFileName);

        // Save the cropped image
        ImageIO.write(croppedImage, "png", outputfile);
    }

    private void showAvatarButtons(Pane settingsPane,double x,double y) {
        Pane buttonsBackground = new Pane();
        buttonsBackground.getStyleClass().add("settings-avatar-buttons-background");
        buttonsBackground.setPrefWidth(70);
        buttonsBackground.setPrefHeight(35);
        buttonsBackground.setLayoutX(x);
        buttonsBackground.setLayoutY(y);

        Pane backgroundPane = new Pane();
        backgroundPane.setLayoutX(0);
        backgroundPane.setLayoutY(0);
        backgroundPane.setPrefWidth(354);
        backgroundPane.setPrefHeight(391);
        settingsPane.getChildren().add(buttonsBackground);
    }
}
