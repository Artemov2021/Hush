package com.messenger.main.chat;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class MessageButtons {
    private AnchorPane mainAnchorPane;

    public MessageButtons(AnchorPane mainAnchorPane) {
        this.mainAnchorPane = mainAnchorPane;
    }

    public void showMessageButtons(int clickPlaceX,int clickPlaceY,int messageId) {
        Pane messageButtonsOverlay = new Pane();
        messageButtonsOverlay.setPrefWidth(mainAnchorPane.getPrefWidth());
        messageButtonsOverlay.setPrefHeight(mainAnchorPane.getPrefHeight());
        messageButtonsOverlay.setLayoutX(0);
        messageButtonsOverlay.setLayoutY(0);
        messageButtonsOverlay.setStyle("-fx-background-color: transparent");
        mainAnchorPane.getChildren().add(messageButtonsOverlay);
        messageButtonsOverlay.setOnMouseClicked(clickEvent -> {
            mainAnchorPane.getChildren().remove(messageButtonsOverlay);
        });
        Platform.runLater(() -> {
            messageButtonsOverlay.getScene().getStylesheets().add(getClass().getResource("/main/css/MainChat.css").toExternalForm());
        });


        Pane messageButtonsBackground = new Pane();
        messageButtonsBackground.setPrefWidth(105);
        messageButtonsBackground.setPrefHeight(113);
        messageButtonsBackground.setLayoutX(clickPlaceX);
        messageButtonsBackground.setLayoutY(clickPlaceY >= 820 ? (clickPlaceY - 113) : (clickPlaceY));
        messageButtonsBackground.getStyleClass().add("chat-message-buttons-background");
        messageButtonsOverlay.getChildren().add(messageButtonsBackground);


        Pane replyPane = new Pane();
        replyPane.setCursor(Cursor.HAND);
        replyPane.setPrefWidth(96);
        replyPane.setPrefHeight(33);
        replyPane.setLayoutX(5);
        replyPane.setLayoutY(5);
        replyPane.getStyleClass().add("chat-message-buttons-small-pane");
        messageButtonsBackground.getChildren().add(replyPane);
        replyPane.setOnMouseClicked(clickEvent -> {
            setReplyWrapper();
        });


        Label replySymbol = new Label();
        replySymbol.setPrefWidth(18);
        replySymbol.setPrefHeight(18);
        replySymbol.setLayoutX(7);
        replySymbol.setLayoutY(7);
        replySymbol.getStyleClass().add("chat-message-buttons-reply-symbol");
        replyPane.getChildren().add(replySymbol);

        Label replyText = new Label("Reply");
        replyText.setLayoutX(34);
        replyText.setLayoutY(6);
        replyText.getStyleClass().add("chat-message-buttons-text");
        replyPane.getChildren().add(replyText);

        Pane editPane = new Pane();
        editPane.setCursor(Cursor.HAND);
        editPane.setPrefWidth(96);
        editPane.setPrefHeight(33);
        editPane.setLayoutX(5);
        editPane.setLayoutY(40);
        editPane.getStyleClass().add("chat-message-buttons-small-pane");
        messageButtonsBackground.getChildren().add(editPane);

        Label editSymbol = new Label();
        editSymbol.setPrefWidth(18);
        editSymbol.setPrefHeight(18);
        editSymbol.setLayoutX(7);
        editSymbol.setLayoutY(7);
        editSymbol.getStyleClass().add("chat-message-buttons-edit-symbol");
        editPane.getChildren().add(editSymbol);

        Label editText = new Label("Edit");
        editText.setLayoutX(34);
        editText.setLayoutY(6);
        editText.getStyleClass().add("chat-message-buttons-text");
        editPane.getChildren().add(editText);

        Pane deletePane = new Pane();
        deletePane.setCursor(Cursor.HAND);
        deletePane.setPrefWidth(96);
        deletePane.setPrefHeight(33);
        deletePane.setLayoutX(5);
        deletePane.setLayoutY(75);
        deletePane.getStyleClass().add("chat-message-buttons-small-pane");
        messageButtonsBackground.getChildren().add(deletePane);

        Label deleteSymbol = new Label();
        deleteSymbol.setPrefWidth(18);
        deleteSymbol.setPrefHeight(18);
        deleteSymbol.setLayoutX(7);
        deleteSymbol.setLayoutY(7);
        deleteSymbol.getStyleClass().add("chat-message-buttons-delete-symbol");
        deletePane.getChildren().add(deleteSymbol);

        Label deleteText = new Label("Delete");
        deleteText.setLayoutX(34);
        deleteText.setLayoutY(6);
        deleteText.getStyleClass().add("chat-message-buttons-text");
        deletePane.getChildren().add(deleteText);

    }
    public void showMessageReplyButton(int clickPlaceX,int clickPlaceY,int messageId) {
        Pane messageButtonsOverlay = new Pane();
        messageButtonsOverlay.setPrefWidth(mainAnchorPane.getPrefWidth());
        messageButtonsOverlay.setPrefHeight(mainAnchorPane.getPrefHeight());
        messageButtonsOverlay.setLayoutX(0);
        messageButtonsOverlay.setLayoutY(0);
        messageButtonsOverlay.setStyle("-fx-background-color: transparent");
        mainAnchorPane.getChildren().add(messageButtonsOverlay);
        messageButtonsOverlay.setOnMouseClicked(clickEvent -> {
            mainAnchorPane.getChildren().remove(messageButtonsOverlay);
        });
        Platform.runLater(() -> {
            messageButtonsOverlay.getScene().getStylesheets().add(getClass().getResource("/main/css/MainChat.css").toExternalForm());
        });


        Pane messageButtonsBackground = new Pane();
        messageButtonsBackground.setCursor(Cursor.HAND);
        messageButtonsBackground.setPrefWidth(105);
        messageButtonsBackground.setPrefHeight(42);
        messageButtonsBackground.setLayoutX(clickPlaceX);
        messageButtonsBackground.setLayoutY((clickPlaceY >= 900) ? (clickPlaceY - 42) : clickPlaceY);
        messageButtonsBackground.getStyleClass().add("chat-message-buttons-background");
        messageButtonsOverlay.getChildren().add(messageButtonsBackground);


        Pane replyPane = new Pane();
        replyPane.setPrefWidth(96);
        replyPane.setPrefHeight(33);
        replyPane.setLayoutX(5);
        replyPane.setLayoutY(5);
        replyPane.getStyleClass().add("chat-message-buttons-small-pane");
        messageButtonsBackground.getChildren().add(replyPane);

        Label replySymbol = new Label();
        replySymbol.setPrefWidth(18);
        replySymbol.setPrefHeight(18);
        replySymbol.setLayoutX(7);
        replySymbol.setLayoutY(7);
        replySymbol.getStyleClass().add("chat-message-buttons-reply-symbol");
        replyPane.getChildren().add(replySymbol);

        Label replyText = new Label("Reply");
        replyText.setLayoutX(34);
        replyText.setLayoutY(6);
        replyText.getStyleClass().add("chat-message-buttons-text");
        replyPane.getChildren().add(replyText);
    }

    private void setReplyWrapper() {
        Pane replyWrapperBackground = new Pane();
        replyWrapperBackground.getStyleClass().add("chat-reply-wrapper-background");
        replyWrapperBackground.setLayoutX(462);
        replyWrapperBackground.setLayoutY(888);
        replyWrapperBackground.setPrefWidth(1477);
        replyWrapperBackground.setPrefHeight(58);
        mainAnchorPane.getChildren().add(replyWrapperBackground);

        Label replyWrapperSymbol = new Label();
        replyWrapperSymbol.getStyleClass().add("chat-reply-wrapper-symbol");
        replyWrapperSymbol.setLayoutX(30);
        replyWrapperSymbol.setLayoutY(10);
        replyWrapperSymbol.setPrefWidth(36);
        replyWrapperSymbol.setPrefHeight(43);
        replyWrapperBackground.getChildren().add(replyWrapperSymbol);

        Label replyWrapperName = new Label("Tymur Artemov");
        replyWrapperName.getStyleClass().add("chat-reply-wrapper-name");
        replyWrapperName.setLayoutX(70);
        replyWrapperName.setLayoutY(6);
        replyWrapperBackground.getChildren().add(replyWrapperName);

        Label replyWrapperMessage = new Label("hello how are you");
        replyWrapperMessage.getStyleClass().add("chat-reply-wrapper-message");
        replyWrapperMessage.setLayoutX(75);
        replyWrapperMessage.setLayoutY(30);
        replyWrapperBackground.getChildren().add(replyWrapperMessage);


    }
}
