package com.messenger.main;

import com.messenger.Log;
import com.messenger.database.DetailedDataBase;
import com.messenger.database.UsersDataBase;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.SimpleTimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainContactList {
    public static void addContactToList(ScrollPane scrollPane, VBox box,String mainUser, String userName) throws SQLException, IOException {
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        box.setSpacing(5);

        Pane userPane = new Pane();
        userPane.getStyleClass().add("user-pane");
        userPane.setPrefWidth(box.getWidth());
        userPane.setMinHeight(55);

        //TODO
        Label avatar = new Label();
        avatar.setPrefWidth(38);
        avatar.setPrefHeight(38);
        String avatarUrl = "/avatars/" + UsersDataBase.getAvatar(userName);
        if (UsersDataBase.getAvatar(userName) != null) {
            URL url = MainContactList.class.getResource(avatarUrl);
            imageResizer(new URL(url.toString().replaceAll("target/classes","src/main/resources")));
            ImageView imageView = new ImageView(new Image(url.toString().replaceAll("target/classes","src/main/resources")));
            imageView.setFitHeight(38);
            imageView.setFitWidth(38);
            imageView.setSmooth(true);
            avatar.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(19);
            clip.setLayoutY(19);
            clip.setRadius(19);
            avatar.setClip(clip);
        } else {
            avatar.getStyleClass().add("user-pane-avatar-default");
        }
        avatar.setLayoutX(10);
        avatar.setLayoutY(10);

        Label name = new Label(userName);
        name.getStyleClass().add("user-pane-name");
        name.setLayoutX(58);
        name.setLayoutY(11);

        Label message = new Label(DetailedDataBase.getLastMessage(mainUser,userName));
        message.getStyleClass().add("user-pane-message");
        message.setLayoutX(58);
        message.setLayoutY(28);

        String timeText = message.getText().isEmpty() ? "" : DetailedDataBase.getMessageTime(mainUser,userName,DetailedDataBase.getLastMessageId(mainUser,userName));
        Label time = new Label(timeText);
        time.getStyleClass().add("user-pane-time");
        time.setLayoutX(240);
        time.setLayoutY(11);

        userPane.getChildren().addAll(
                avatar,
                name,
                message,
                time
        );
        box.getChildren().add(0,userPane);
    }

    public static void addContactsToList(ScrollPane scrollPane, VBox box,String mainUser) throws SQLException, IOException {
        ArrayList<String> contacts = DetailedDataBase.getContacts(mainUser);
        for (String contact: contacts) {
            addContactToList(scrollPane,box,mainUser,contact);
        }
    }

    private static void imageResizer(URL imageUrl) throws IOException {
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
        String newFileName = inputFile.getParent() + File.separator + inputFile.getName();
        File outputfile = new File(newFileName);

        // Save the cropped image
        ImageIO.write(croppedImage, "png", outputfile);

    }

}
