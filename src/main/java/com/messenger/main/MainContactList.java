package com.messenger.main;

import com.messenger.database.DetailedDataBase;
import com.messenger.database.UsersDataBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainContactList {
    private final AnchorPane mainAnchorPane;
    private final VBox contactsVBox;
    private final int mainUserId;

    public MainContactList(AnchorPane mainAnchorPane,ScrollPane contactsScrollPane,VBox contactsVBox,int mainUserId) {
        this.mainAnchorPane = mainAnchorPane;
        this.contactsVBox = contactsVBox;
        this.mainUserId = mainUserId;

        contactsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contactsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contactsVBox.setSpacing(5);
    }

    public void addContactToList(int contactId) throws SQLException {
        String userName = UsersDataBase.getNameWithId(contactId);

        Pane userPane = new Pane();
        userPane.getStyleClass().add("user-pane");
        userPane.setPrefWidth(contactsVBox.getWidth());
        userPane.setMinHeight(55);

        Label avatar = new Label();
        avatar.setPrefWidth(38);
        avatar.setPrefHeight(38);
        String avatarUrl = "/avatars/" + UsersDataBase.getAvatarWithId(contactId);
        if (UsersDataBase.getAvatarWithId(contactId) != null) {
            URL url = MainContactList.class.getResource(avatarUrl);
            assert url != null;
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

        System.out.println("before last message");
        Label message = new Label(DetailedDataBase.getLastMessage(mainUserId, contactId));
        message.setPrefWidth(180);
        message.getStyleClass().add("user-pane-message");
        message.setLayoutX(58);
        message.setLayoutY(31);

        System.out.println("before time");
        String messageTime = message.getText() == null ? "" : DetailedDataBase.getMessageTime(mainUserId,contactId,DetailedDataBase.getLastMessageId(mainUserId,contactId));
        String timeText = convertTimeToHours(messageTime);
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

        System.out.println("successfull");
        contactsVBox.getChildren().add(0,userPane);

        // opens dialog pane
        userPane.setOnMouseClicked(mouseEvent -> {
            try {

                // Load FXML new contact window ( pane )
                FXMLLoader loader = new FXMLLoader(MainContactList.class.getResource("/main/Dialog.fxml"));
                Parent dialogRoot = loader.load();

                // Pass the anchor pane of main window to settings controller file
                DialogController dialog = loader.getController();
                dialog.setContactId(contactId);
                dialog.setMainUserId(mainUserId);
                dialog.setMainAnchorPane(mainAnchorPane);
                dialog.initializeWithValue();

                mainAnchorPane.getChildren().removeIf(child -> Objects.equals(child.getId(), "dialogBackgroundPane"));
                mainAnchorPane.getChildren().add(0,dialogRoot);


            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String convertTimeToHours(String time) {
        String hoursPattern = "(\\d+):(\\d+)";
        Pattern compliedPattern = Pattern.compile(hoursPattern);
        Matcher matcher = compliedPattern.matcher(time);
        if (matcher.find()) {
            return String.format("%s:%s",matcher.group(1),matcher.group(2));
        }
        return null;
    }

    public void addUserContactsToList() throws SQLException {
        // List of users id's
        ArrayList<Integer> contacts = DetailedDataBase.getContactsIds(mainUserId);
        for (int contactId: contacts) {
            addContactToList(contactId);
        }
    }
    public void addCustomContactsToList(ArrayList<Integer> contactsIds) throws SQLException {
        for (int contactId: contactsIds) {
            addContactToList(contactId);
        }
    }


}
