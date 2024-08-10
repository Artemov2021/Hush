package com.messenger.main;

import com.messenger.database.DetailedDataBase;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;

public class MainContactList {
    public static void addContactToList(ScrollPane scrollPane, VBox box,String mainUser, String userName) throws SQLException {
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        box.setSpacing(8);

        Pane userPane = new Pane();
        userPane.getStyleClass().add("user-pane");
        userPane.setPrefWidth(box.getWidth());
        userPane.setMinHeight(55);

        Label avatar = new Label();
        avatar.setPrefWidth(50);
        avatar.setPrefHeight(50);
        avatar.getStyleClass().add("user-pane-avatar");
        avatar.setLayoutX(10);
        avatar.setLayoutY(10);

        Label name = new Label(userName);
        name.getStyleClass().add("user-pane-name");
        name.setLayoutX(58);
        name.setLayoutY(13);

        Label message = new Label(DetailedDataBase.getLastMessage(mainUser,userName));
        message.getStyleClass().add("user-pane-message");
        message.setLayoutX(58);
        message.setLayoutY(30);

        userPane.getChildren().addAll(
                avatar,
                name,
                message
        );
        box.getChildren().addFirst(userPane);
    }

    public static void addContactsToList(ScrollPane scrollPane, VBox box,String mainUser) throws SQLException {
        ArrayList<String> contacts = DetailedDataBase.getContacts(mainUser);
        for (String contact: contacts) {
            addContactToList(scrollPane,box,mainUser,contact);
        }
    }
}
