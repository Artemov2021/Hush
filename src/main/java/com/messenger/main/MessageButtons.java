package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.main.MainChatController;
import com.messenger.main.MainContactController;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageButtons extends MainChatController {

    public MessageButtons(MainChatController mainChatController) {
       this.mainAnchorPane = mainChatController.mainAnchorPane;
       this.chatVBox = mainChatController.chatVBox;
       this.scrollDownButton = mainChatController.scrollDownButton;
       this.mainContactMessageLabel = mainChatController.mainContactMessageLabel;
       this.mainContactTimeLabel = mainChatController.mainContactTimeLabel;
       this.contactId = mainChatController.contactId;
       this.chatScrollPane = mainChatController.chatScrollPane;
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
        messageButtonsBackground.setPrefWidth(107);
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
            try {
                int messageSenderId = (int) ChatsDataBase.getMessage(mainUserId,contactId,messageId).sender_id;
                setReplyWrapper(messageSenderId,messageId);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        editPane.setOnMouseClicked(clickEvent -> {
            try {
                int messageSenderId = ChatsDataBase.getMessage(mainUserId,contactId,messageId).sender_id;
                setEditWrapper(messageSenderId,messageId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
        deletePane.setOnMouseClicked(mouseEvent -> {
            showDeleteMessageConfirmation(messageId);
        });
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
        deleteText.setLayoutY(7);
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
        messageButtonsBackground.setPrefWidth(104);
        messageButtonsBackground.setPrefHeight(41);
        messageButtonsBackground.setLayoutX(clickPlaceX);
        messageButtonsBackground.setLayoutY((clickPlaceY >= 900) ? (clickPlaceY - 42) : clickPlaceY);
        messageButtonsBackground.getStyleClass().add("chat-message-buttons-background");
        messageButtonsOverlay.getChildren().add(messageButtonsBackground);


        Pane replyPane = new Pane();
        replyPane.setPrefWidth(96);
        replyPane.setPrefHeight(33);
        replyPane.setLayoutX(4);
        replyPane.setLayoutY(4);
        replyPane.getStyleClass().add("chat-message-buttons-small-pane");
        messageButtonsBackground.getChildren().add(replyPane);
        replyPane.setOnMouseClicked(clickEvent -> {
            try {
                int messageSenderId = (int) ChatsDataBase.getMessage(mainUserId,contactId,messageId).sender_id;
                setReplyWrapper(messageSenderId,messageId);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    }
    private void setReplyWrapper(int senderId, int messageId) throws SQLException {
        deletePreviousReplyWrapper();
        deletePreviousEditWrapper();
        setTextFieldFocused();
        setVBoxBottomPadding(65);
        raiseScrollDownButton();

        Pane replyWrapperBackground = new Pane();
        replyWrapperBackground.setId("replyWrapper"+messageId);
        replyWrapperBackground.getStyleClass().add("chat-wrapper-background");
        replyWrapperBackground.setLayoutX(462);
        replyWrapperBackground.setLayoutY(891);
        replyWrapperBackground.setPrefWidth(1477);
        replyWrapperBackground.setPrefHeight(59);
        mainAnchorPane.getChildren().add(replyWrapperBackground);

        Label replyWrapperSymbol = new Label();
        replyWrapperSymbol.getStyleClass().add("chat-reply-wrapper-symbol");
        replyWrapperSymbol.setLayoutX(27);
        replyWrapperSymbol.setLayoutY(10);
        replyWrapperSymbol.setPrefWidth(36);
        replyWrapperSymbol.setPrefHeight(43);
        replyWrapperBackground.getChildren().add(replyWrapperSymbol);

        String repliedMessageSenderName = UsersDataBase.getNameWithId(senderId);
        Label replyWrapperName = new Label(repliedMessageSenderName);
        replyWrapperName.getStyleClass().add("chat-wrapper-name");
        replyWrapperName.setLayoutX(79);
        replyWrapperName.setLayoutY(6);
        replyWrapperBackground.getChildren().add(replyWrapperName);

        boolean messageHasPicture = ChatsDataBase.getMessage(mainUserId,contactId,messageId).picture != null;
        if (messageHasPicture) {
            Pane replyWrapperMessagePictureGroup = new Pane();
            replyWrapperMessagePictureGroup.setLayoutX(80);
            replyWrapperMessagePictureGroup.setLayoutY(32);
            replyWrapperMessagePictureGroup.setCursor(Cursor.HAND);

            Label replyWrapperMessagePhotoSymbol = new Label();
            replyWrapperMessagePhotoSymbol.getStyleClass().add("chat-wrapper-photo-symbol");
            replyWrapperMessagePhotoSymbol.setLayoutY(3);
            replyWrapperMessagePhotoSymbol.setPrefWidth(13);
            replyWrapperMessagePhotoSymbol.setPrefHeight(13);
            replyWrapperMessagePictureGroup.getChildren().add(replyWrapperMessagePhotoSymbol);

            Label replyWrapperMessagePhotoTitle = new Label("Photo");
            replyWrapperMessagePhotoTitle.getStyleClass().add("chat-wrapper-photo-title");
            replyWrapperMessagePhotoTitle.setLayoutX(17);
            replyWrapperMessagePhotoTitle.setMaxWidth(40);
            replyWrapperMessagePhotoTitle.setMaxHeight(17);
            replyWrapperMessagePictureGroup.getChildren().add(replyWrapperMessagePhotoTitle);

            replyWrapperBackground.getChildren().add(replyWrapperMessagePictureGroup);
            replyWrapperMessagePictureGroup.setOnMouseClicked(clickEvent -> {
                if (clickEvent.getButton() == MouseButton.PRIMARY) {
                    HBox repliedmessageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
                    double hboxPosition = getCenteredScrollPosition(repliedmessageHBox);
                    smoothScrollTo(hboxPosition,0.4);
                    fadeOutBackgroundColor(repliedmessageHBox);
                }
            });
            replyWrapperMessagePictureGroup.setOnMouseEntered(clickEvent -> {
                replyWrapperMessagePhotoSymbol.getStyleClass().clear();
                replyWrapperMessagePhotoSymbol.getStyleClass().add("chat-wrapper-photo-symbol-hovered");
                replyWrapperMessagePhotoTitle.getStyleClass().clear();
                replyWrapperMessagePhotoTitle.getStyleClass().add("chat-wrapper-photo-title-hovered");
            });
            replyWrapperMessagePictureGroup.setOnMouseExited(clickEvent -> {
                replyWrapperMessagePhotoSymbol.getStyleClass().clear();
                replyWrapperMessagePhotoSymbol.getStyleClass().add("chat-wrapper-photo-symbol");
                replyWrapperMessagePhotoTitle.getStyleClass().clear();
                replyWrapperMessagePhotoTitle.getStyleClass().add("chat-wrapper-photo-title");
            });
        } else {
            String message = ChatsDataBase.getMessage(mainUserId,contactId,messageId).message_text;
            Label replyWrapperMessage = new Label(message);
            replyWrapperMessage.setCursor(Cursor.HAND);
            replyWrapperMessage.getStyleClass().add("chat-wrapper-message");
            replyWrapperMessage.setLayoutX(80);
            replyWrapperMessage.setLayoutY(31);
            replyWrapperMessage.setMaxWidth(400);
            replyWrapperMessage.setMaxHeight(17);
            replyWrapperMessage.setOnMouseClicked(clickEvent -> {
                if (clickEvent.getButton() == MouseButton.PRIMARY) {
                    HBox repliedmessageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
                    double hboxPosition = getCenteredScrollPosition(repliedmessageHBox);
                    smoothScrollTo(hboxPosition,0.4);
                    fadeOutBackgroundColor(repliedmessageHBox);
                }
            });
            replyWrapperBackground.getChildren().add(replyWrapperMessage);
        }


        Label wrapperExit = new Label();
        wrapperExit.setCursor(Cursor.HAND);
        wrapperExit.getStyleClass().add("chat-wrapper-exit");
        wrapperExit.setLayoutX(1412);
        wrapperExit.setLayoutY(24);
        wrapperExit.setPrefWidth(22);
        wrapperExit.setPrefHeight(22);
        wrapperExit.setOnMouseClicked(clickEvent -> {
            mainAnchorPane.getChildren().remove(
                    mainAnchorPane.lookupAll("*").stream()
                            .filter(node -> node instanceof Pane && node.getId() != null && node.getId().startsWith("replyWrapper"))
                            .findFirst()
                            .orElse(null)
            );
            setVBoxBottomPadding(20);
            moveBackScrollDownButton();
            updateScrollDownButtonVisibility();
        });
        replyWrapperBackground.getChildren().add(wrapperExit);

    }
    private void setEditWrapper(int senderId,int messageId) throws SQLException {
        deletePreviousEditWrapper();
        deletePreviousReplyWrapper();
        setTextFieldFocused();
        setText(messageId);
        setVBoxBottomPadding(65);
        raiseScrollDownButton();

        Pane editWrapperBackground = new Pane();
        editWrapperBackground.setId("editWrapper"+messageId);
        editWrapperBackground.getStyleClass().add("chat-wrapper-background");
        editWrapperBackground.setLayoutX(462);
        editWrapperBackground.setLayoutY(891);
        editWrapperBackground.setPrefWidth(1477);
        editWrapperBackground.setPrefHeight(59);
        mainAnchorPane.getChildren().add(editWrapperBackground);

        Label editWrapperSymbol = new Label();
        editWrapperSymbol.getStyleClass().add("chat-edit-wrapper-symbol");
        editWrapperSymbol.setLayoutX(30);
        editWrapperSymbol.setLayoutY(15);
        editWrapperSymbol.setPrefWidth(30);
        editWrapperSymbol.setPrefHeight(30);
        editWrapperBackground.getChildren().add(editWrapperSymbol);

        String editeMessageSenderName = UsersDataBase.getNameWithId(senderId);
        Label editWrapperName = new Label(editeMessageSenderName);
        editWrapperName.getStyleClass().add("chat-wrapper-name");
        editWrapperName.setLayoutX(78);
        editWrapperName.setLayoutY(6);
        editWrapperBackground.getChildren().add(editWrapperName);

        boolean messageHasPicture = ChatsDataBase.getMessage(mainUserId,contactId,messageId).picture != null;
        if (messageHasPicture) {
            Pane editWrapperMessagePictureGroup = new Pane();
            editWrapperMessagePictureGroup.setLayoutX(80);
            editWrapperMessagePictureGroup.setLayoutY(32);
            editWrapperMessagePictureGroup.setCursor(Cursor.HAND);

            Label editWrapperMessagePhotoSymbol = new Label();
            editWrapperMessagePhotoSymbol.getStyleClass().add("chat-wrapper-photo-symbol");
            editWrapperMessagePhotoSymbol.setLayoutY(3);
            editWrapperMessagePhotoSymbol.setPrefWidth(13);
            editWrapperMessagePhotoSymbol.setPrefHeight(13);
            editWrapperMessagePictureGroup.getChildren().add(editWrapperMessagePhotoSymbol);

            Label editWrapperMessagePhotoTitle = new Label("Photo");
            editWrapperMessagePhotoTitle.getStyleClass().add("chat-wrapper-photo-title");
            editWrapperMessagePhotoTitle.setLayoutX(17);
            editWrapperMessagePhotoTitle.setMaxWidth(40);
            editWrapperMessagePhotoTitle.setMaxHeight(17);
            editWrapperMessagePictureGroup.getChildren().add(editWrapperMessagePhotoTitle);

            editWrapperBackground.getChildren().add(editWrapperMessagePictureGroup);
            editWrapperMessagePictureGroup.setOnMouseClicked(clickEvent -> {
                if (clickEvent.getButton() == MouseButton.PRIMARY) {
                    HBox repliedmessageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
                    double hboxPosition = getCenteredScrollPosition(repliedmessageHBox);
                    smoothScrollTo(hboxPosition,0.4);
                    fadeOutBackgroundColor(repliedmessageHBox);
                }
            });
            editWrapperMessagePictureGroup.setOnMouseEntered(clickEvent -> {
                editWrapperMessagePhotoSymbol.getStyleClass().clear();
                editWrapperMessagePhotoSymbol.getStyleClass().add("chat-wrapper-photo-symbol-hovered");
                editWrapperMessagePhotoTitle.getStyleClass().clear();
                editWrapperMessagePhotoTitle.getStyleClass().add("chat-wrapper-photo-title-hovered");
            });
            editWrapperMessagePictureGroup.setOnMouseExited(clickEvent -> {
                editWrapperMessagePhotoSymbol.getStyleClass().clear();
                editWrapperMessagePhotoSymbol.getStyleClass().add("chat-wrapper-photo-symbol");
                editWrapperMessagePhotoTitle.getStyleClass().clear();
                editWrapperMessagePhotoTitle.getStyleClass().add("chat-wrapper-photo-title");
            });
        } else {
            String message = ChatsDataBase.getMessage(mainUserId,contactId,messageId).message_text;
            Label editWrapperMessage = new Label(message);
            editWrapperMessage.setCursor(Cursor.HAND);
            editWrapperMessage.getStyleClass().add("chat-wrapper-message");
            editWrapperMessage.setLayoutX(79);
            editWrapperMessage.setLayoutY(31);
            editWrapperMessage.setMaxWidth(400);
            editWrapperMessage.setMaxHeight(17);
            editWrapperMessage.setOnMouseClicked(clickEvent -> {
                if (clickEvent.getButton() == MouseButton.PRIMARY) {
                    HBox repliedmessageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
                    double hboxPosition = getCenteredScrollPosition(repliedmessageHBox);
                    smoothScrollTo(hboxPosition,0.4);
                    fadeOutBackgroundColor(repliedmessageHBox);
                }
            });
            editWrapperBackground.getChildren().add(editWrapperMessage);
        }



        Label wrapperExit = new Label();
        wrapperExit.setCursor(Cursor.HAND);
        wrapperExit.getStyleClass().add("chat-wrapper-exit");
        wrapperExit.setLayoutX(1412);
        wrapperExit.setLayoutY(24);
        wrapperExit.setPrefWidth(22);
        wrapperExit.setPrefHeight(22);
        wrapperExit.setOnMouseClicked(clickEvent -> {
            mainAnchorPane.getChildren().remove(
                    mainAnchorPane.lookupAll("*").stream()
                            .filter(node -> node instanceof Pane && node.getId() != null && node.getId().startsWith("editWrapper"))
                            .findFirst()
                            .orElse(null)
            );
            clearText();
            setVBoxBottomPadding(20);
            moveBackScrollDownButton();
            updateScrollDownButtonVisibility();
        });
        editWrapperBackground.getChildren().add(wrapperExit);


    }

    private void deletePreviousReplyWrapper() {
        Pane replyWrapper = (Pane) mainAnchorPane.lookupAll("*").stream()
                .filter(node -> node instanceof Pane && node.getId() != null && node.getId().startsWith("replyWrapper"))
                .findFirst()
                .orElse(null);

        mainAnchorPane.getChildren().remove(replyWrapper);
    }
    private void deletePreviousEditWrapper() {
        Pane replyWrapper = (Pane) mainAnchorPane.lookupAll("*").stream()
                .filter(node -> node instanceof Pane && node.getId() != null && node.getId().startsWith("editWrapper"))
                .findFirst()
                .orElse(null);

        mainAnchorPane.getChildren().remove(replyWrapper);
    }
    private void setVBoxBottomPadding(int bottomPadding) {
        chatVBox.setPadding(new Insets(0, 0, bottomPadding, 0));
    }
    private void raiseScrollDownButton() {
        Label scrollDownButton = (Label) mainAnchorPane.lookup("#scrollDownButton");
        scrollDownButton.setLayoutY(815);
    }
    private void moveBackScrollDownButton() {
        Label scrollDownButton = (Label) mainAnchorPane.lookup("#scrollDownButton");
        scrollDownButton.setLayoutY(871);
    }
    private void updateScrollDownButtonVisibility() {
        // Fade-out Transition (0.2 seconds)
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.2), scrollDownButton);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);
        fadeOut.setOnFinished(event -> scrollDownButton.setVisible(false)); // Hide after fading out

        if (scrollDownButton.getOpacity() > 0) {
            fadeOut.playFromStart();
        }
    }
    private double getCenteredScrollPosition(HBox targetHBox) {
        double hboxY = targetHBox.localToScene(0, 0).getY(); // Y position of HBox in scene
        double vboxY = chatVBox.localToScene(0, 0).getY(); // Y position of VBox in scene
        double viewportHeight = chatScrollPane.getViewportBounds().getHeight(); // Viewport height
        double totalHeight = chatVBox.getBoundsInLocal().getHeight(); // Total VBox height

        // Compute scroll position to center the HBox
        double position = (hboxY - vboxY - (viewportHeight / 2) + (targetHBox.getBoundsInLocal().getHeight() / 2))
                / (totalHeight - viewportHeight);

        // Ensure the value is between 0 and 1
        return Math.max(0, Math.min(1, position));
    }
    private void smoothScrollTo(double targetValue, double durationInSeconds) {
        double startValue = chatScrollPane.getVvalue(); // Current scroll position
        double distance = targetValue - startValue; // How much to scroll

        Timeline timeline = new Timeline();
        int frames = (int) (durationInSeconds * 60); // 60 FPS
        for (int i = 0; i <= frames; i++) {
            double progress = (double) i / frames; // Progress from 0 to 1
            double interpolatedValue = startValue + distance * progress; // Linear interpolation

            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * (1000.0 / 60)),
                    event -> chatScrollPane.setVvalue(interpolatedValue)));
        }

        timeline.setCycleCount(1);
        timeline.play();
    }
    private void fadeOutBackgroundColor(HBox hbox) {
        // Stop any existing animation on this HBox
        if (hbox.getUserData() instanceof Timeline) {
            ((Timeline) hbox.getUserData()).stop();
        }

        // Create a new Timeline
        Timeline fadeTimeline = new Timeline();
        hbox.setUserData(fadeTimeline); // Store animation in the HBox itself

        // Base color #333138
        Color startColor = Color.web("#333138");

        // Opacity property to interpolate alpha
        ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(startColor);
        colorProperty.addListener((obs, oldColor, newColor) -> {
            hbox.setBackground(new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, Insets.EMPTY)));
        });

        // Animate the alpha from 1.0 (solid) to 0.0 (transparent)
        KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(2),
                new KeyValue(colorProperty, Color.web("#333138", 0)) // Transparent version of the color
        );

        fadeTimeline.getKeyFrames().add(keyFrame);
        fadeTimeline.setCycleCount(1);

        // Clear animation reference on completion
        fadeTimeline.setOnFinished(event -> hbox.setUserData(null));

        fadeTimeline.play();
    }
    private void setTextFieldFocused() {
        TextField chatTextField = (TextField) mainAnchorPane.lookup("#chatTextField");
        chatTextField.requestFocus();
    }
    private void setText(int messageId) throws SQLException {
        String messageText = ChatsDataBase.getMessage(mainUserId,contactId,messageId).message_text;

        if (messageText != null && !messageText.isEmpty()) {
            TextField chatTextField = (TextField) mainAnchorPane.lookup("#chatTextField");
            chatTextField.setText(messageText);
            chatTextField.positionCaret(chatTextField.getText().length());
        }
    }
    private void clearText() {
        TextField chatTextField = (TextField) mainAnchorPane.lookup("#chatTextField");
        chatTextField.setText("");
    }
    private void showDeleteMessageConfirmation(int messageId) {
        Pane confirmationOverlay = new Pane();
        confirmationOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.68)");
        confirmationOverlay.setLayoutX(0);
        confirmationOverlay.setLayoutY(0);
        confirmationOverlay.setPrefWidth(mainAnchorPane.getWidth());
        confirmationOverlay.setPrefHeight(mainAnchorPane.getHeight());
        confirmationOverlay.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                mainAnchorPane.getChildren().remove(confirmationOverlay);
            }
        });
        mainAnchorPane.getChildren().add(confirmationOverlay);

        Pane confirmationBackground = new Pane();
        confirmationBackground.getStyleClass().add("contact-delete-confirmation-window-background");
        confirmationBackground.setLayoutX(778);
        confirmationBackground.setLayoutY(401);
        confirmationBackground.setPrefWidth(409);
        confirmationBackground.setPrefHeight(149);
        confirmationBackground.setOnMouseClicked(Event::consume);
        confirmationOverlay.getChildren().add(confirmationBackground);

        Label confirmationText = new Label("You want to delete that message? ");
        confirmationText.getStyleClass().add("contact-delete-confirmation-window-text");
        confirmationText.setLayoutX(30);
        confirmationText.setLayoutY(20);
        confirmationBackground.getChildren().add(confirmationText);

        Label confirmationDeleteButton = new Label();
        confirmationDeleteButton.setCursor(Cursor.HAND);
        confirmationDeleteButton.getStyleClass().add("contact-delete-confirmation-window-delete-button");
        confirmationDeleteButton.setLayoutX(290);
        confirmationDeleteButton.setLayoutY(94);
        confirmationDeleteButton.setPrefWidth(98);
        confirmationDeleteButton.setPrefHeight(40);
        confirmationBackground.getChildren().add(confirmationDeleteButton);
        confirmationDeleteButton.setOnMouseClicked(clickEvent -> {
            try {
                deleteMessage(messageId);
                mainAnchorPane.getChildren().remove(confirmationOverlay);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        Label confirmationCancelButton = new Label();
        confirmationCancelButton.setCursor(Cursor.HAND);
        confirmationCancelButton.getStyleClass().add("contact-delete-confirmation-window-cancel-button");
        confirmationCancelButton.setLayoutX(175);
        confirmationCancelButton.setLayoutY(94);
        confirmationCancelButton.setPrefWidth(104);
        confirmationCancelButton.setPrefHeight(40);
        confirmationBackground.getChildren().add(confirmationCancelButton);
        confirmationCancelButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                mainAnchorPane.getChildren().remove(confirmationOverlay);
            }
        });

        // Appearing time
        FadeTransition FadeIn = new FadeTransition(Duration.millis(180),confirmationOverlay);
        FadeIn.setFromValue(0);
        FadeIn.setToValue(1);
        FadeIn.play();

        // Appearing move to left
        TranslateTransition translateIn = new TranslateTransition(Duration.millis(180), confirmationBackground);
        translateIn.setFromX(0);
        translateIn.setToX(-35);
        translateIn.play();
    }

    private void deleteMessage(int messageId) throws SQLException {
        int senderId = ChatsDataBase.getSenderIdWithMessageId(messageId);
        int receiverId = ChatsDataBase.getReceiverIdWithMessageId(messageId);
        deleteMessageInChat(messageId,senderId,receiverId);
        deleteMessageFromDB(messageId);
        changeReplyMessages(messageId,senderId,receiverId);
        deletePotentialWrapper(messageId);
    }
    private void deleteMessageFromDB(int messageId) throws SQLException {
        ChatsDataBase.deleteMessage(messageId);
    }
    private void deleteMessageInChat(int messageId,int senderId,int receiverId) throws SQLException {
        moveMessageAvatarBack(messageId,senderId,receiverId);
        changeLastMessage(messageId);
        changeLastMessageTime(messageId);
        deleteDateLabel(messageId);
        removeMessageHBox(messageId);
    }
    private void moveMessageAvatarBack(int messageId,int senderId,int receiverId) throws SQLException {
        HBox targetMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
        HBox previousMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId));
        int previousMessageId = ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId);

        boolean hasAvatarLabel = targetMessageHBox.lookup("#messageAvatarLabel"+messageId) != null;
        boolean isSameSender = (previousMessageHBox != null) && senderId == (int) ChatsDataBase.getMessage(mainUserId,contactId,previousMessageId).sender_id;
        boolean previousMessageNoAvatarLabel = (previousMessageHBox != null) && previousMessageHBox.lookup("#messageAvatarLabel"+ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId)) == null;

        if (hasAvatarLabel && isSameSender && previousMessageNoAvatarLabel) {
            addNewAvatarLabel(previousMessageHBox,ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId),senderId);
        }
    }
    private void changeLastMessage(int messageId) throws SQLException {
        int previousMessageId = ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId);
        int nextMessageId = ChatsDataBase.getNextMessageId(mainUserId,contactId,messageId);
        boolean previousMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,previousMessageId);
        boolean nextMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,nextMessageId);
        boolean previousMessageIsPicture = previousMessageExists && ChatsDataBase.getMessage(mainUserId,contactId,previousMessageId).picture != null;
        boolean previousMessageHasText = previousMessageExists && ChatsDataBase.getMessage(mainUserId,contactId,previousMessageId).message_text != null;
        boolean lastMessageIsPicture = ChatsDataBase.getMessage(mainUserId,contactId,ChatsDataBase.getLastMessageId(mainUserId,contactId)).picture != null;
        boolean lastMessageHasText = ChatsDataBase.getMessage(mainUserId,contactId,ChatsDataBase.getLastMessageId(mainUserId,contactId)).message_text != null;

        if (!previousMessageExists && !nextMessageExists) {
            mainContactMessageLabel.setText("");
        } else if (!nextMessageExists && previousMessageIsPicture && !previousMessageHasText) {
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.setStyle("-fx-text-fill: white");
            mainContactMessageLabel.setText("Picture");
        } else if (!nextMessageExists && (!previousMessageIsPicture || previousMessageIsPicture && previousMessageHasText)){
            String previousLastMessage = ChatsDataBase.getMessage(mainUserId,contactId,previousMessageId).message_text;
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText(previousLastMessage);
        } else if (nextMessageExists && lastMessageIsPicture && !lastMessageHasText) {
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.setStyle("-fx-text-fill: white");
            mainContactMessageLabel.setText("Picture");
        } else if (nextMessageExists && (!lastMessageIsPicture || lastMessageIsPicture && lastMessageHasText)) {
            String lastMessage = ChatsDataBase.getLastMessage(mainUserId, contactId);
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText(lastMessage);
        }
    }
    private void changeLastMessageTime(int messageId) throws SQLException {
        int previousMessageId = ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId);
        int nextMessageId = ChatsDataBase.getNextMessageId(mainUserId,contactId,messageId);
        boolean previousMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,previousMessageId);
        boolean nextMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,nextMessageId);

        if (!previousMessageExists && !nextMessageExists) {
            mainContactTimeLabel.setText("");
        } else if (!nextMessageExists) {
            String previousMessageTime = getMessageTime(previousMessageId);
            mainContactTimeLabel.setText(previousMessageTime);
        } else if (!previousMessageExists || (previousMessageExists && nextMessageExists)){
            int lastMessageId = ChatsDataBase.getLastMessageId(mainUserId,contactId);
            mainContactTimeLabel.setText(getMessageTime(lastMessageId));
        }
    }
    private String getMessageTime(int messageId) throws SQLException {
        String lastMessageFullDate = ChatsDataBase.getMessage(mainUserId,contactId,messageId).time;
        String pattern = "(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}:\\d{2})"; // Extracts YYYY, MM, DD, HH:mm
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(lastMessageFullDate);

        if (matcher.find()) {
            String year = matcher.group(1);
            String month = matcher.group(2);
            String day = matcher.group(3);
            String time = matcher.group(4);

            LocalDate messageDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1); // Calculate yesterday's date

            if (messageDate.isEqual(today)) {
                return time; // Show only HH:mm if it's today
            } else if (messageDate.isEqual(yesterday)) {
                return "yesterday"; // Show "yesterday" if the date is yesterday
            } else {
                return day + "." + month + "." + year; // Show full date if not today
            }
        } else {
            return ""; // Default to empty if no match
        }
    }
    private void deleteDateLabel(int messageId) throws SQLException {
        String messageTime = ChatsDataBase.getMessage(mainUserId,contactId,messageId).time;
        Label dateLabel = (Label) chatVBox.lookup("#dateLabel"+getDateLabelDate(messageTime));

        boolean isThereMessageOnSameDate = ChatsDataBase.isThereMessagesOnSameDay(mainUserId,contactId,messageId,messageTime);

        if (!isThereMessageOnSameDate) {
            chatVBox.getChildren().remove(dateLabel);
        }
    }
    private String getDateLabelDate(String fullTime) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(fullTime, inputFormatter);

        return dateTime.toLocalDate().toString(); // Outputs in yyyy-MM-dd format
    }
    private void addNewAvatarLabel(HBox messageHBox,int messageId,int senderId) throws SQLException {
        Label newAvatarLabel = new Label();
        newAvatarLabel.setId("messageAvatarLabel"+messageId);
        setMessageAvatar(newAvatarLabel,senderId);
        messageHBox.getChildren().add(newAvatarLabel);

        StackPane messageStackPane = (StackPane) messageHBox.lookup("#messageStackPane"+messageId);
        HBox.setMargin(newAvatarLabel, (senderId == mainUserId) ? new Insets(0, 115, 0, 0) : new Insets(0, 0, 0, 105));
        HBox.setMargin(messageStackPane, (senderId == mainUserId) ? new Insets(0, 13, 0, 0) : new Insets(0, 0, 0, 13));
    }
    private void removeMessageHBox(int messageId) {
        HBox targetMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
        chatVBox.getChildren().remove(targetMessageHBox);
    }
    private void setMessageAvatar(Label avatar,int senderId) throws SQLException {
        byte[] blobBytes = UsersDataBase.getAvatarWithId(senderId);
        if (blobBytes == null) {
            avatar.getStyleClass().clear();
            avatar.getStyleClass().add("chat-message-default-avatar");
            avatar.setPrefHeight(40);
            avatar.setPrefWidth(40);
            return;
        }
        ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
        ImageView imageView = new ImageView(new Image(byteStream));
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setSmooth(true);
        avatar.setGraphic(imageView);
        Circle clip = new Circle();
        clip.setLayoutX(20);
        clip.setLayoutY(20);
        clip.setRadius(20);
        avatar.setClip(clip);
    }
    public static String getMessageHours(String messageFullTime) {
        // Define the input and output formats
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Parse the input string to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(messageFullTime, inputFormatter);

        // Format and return the output as a string
        return dateTime.format(outputFormatter);
    }
    public void changeReplyMessages(int messageId, int senderId, int receiverId) throws SQLException {
        // Get list of replied message IDs from DB
        List<Integer> ids = ChatsDataBase.getRepliedMessageIds(senderId, receiverId, messageId);
        List<HBox> foundHBoxes = ids.stream()
                .map(id -> (HBox) chatVBox.lookup("#messageHBox" + id))
                .filter(Objects::nonNull).toList();

        List<StackPane> messageStackPane = foundHBoxes.stream()
                .flatMap(hbox -> hbox.getChildren().stream()) // Get all children of each HBox
                .filter(node -> node instanceof StackPane) // Keep only StackPane elements
                .filter(node -> node.getId() != null && node.getId().startsWith("messageStackPane")) // Filter by ID prefix
                .map(node -> (StackPane) node) // Cast to StackPane
                .toList(); // Collect as List

        List<StackPane> replyMessageStackPane = messageStackPane.stream()
                .flatMap(messageStackPane1 -> messageStackPane1.getChildren().stream()) // Get all children of each HBox
                .filter(node -> node instanceof StackPane) // Keep only StackPane elements
                .filter(node -> node.getId() != null && node.getId().startsWith("messageReplyStackPane")) // Filter by ID prefix
                .map(node -> (StackPane) node) // Cast to StackPane
                .toList(); // Collect as List

        for (StackPane replyStackPane: replyMessageStackPane) {
            replyStackPane.getChildren().clear();
            Label repliedMessageDeletedMessage = new Label("(deleted message)");
            repliedMessageDeletedMessage.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-deleted-message" : "chat-message-contact-deleted-message");
            StackPane.setAlignment(repliedMessageDeletedMessage, Pos.TOP_LEFT);
            StackPane.setMargin(repliedMessageDeletedMessage,new Insets(10,8,5,8));
            replyStackPane.getChildren().add(repliedMessageDeletedMessage);
            replyStackPane.setOnMouseClicked(null);
            replyStackPane.setCursor(Cursor.DEFAULT);
        }

        List<StackPane> replyPictureMessageStackPanes = messageStackPane.stream()
                .flatMap(replyPictureMessageStackPane -> replyPictureMessageStackPane.getChildren().stream()) // Get all children
                .filter(node -> node instanceof VBox)
                .flatMap(vbox -> ((VBox) vbox).getChildren().stream())
                .filter(node -> node instanceof StackPane) // Keep only StackPane elements
                .filter(node -> node.getId() != null && node.getId().startsWith("messageReplyStackPane")) // Filter by ID prefix
                .map(node -> (StackPane) node) // Cast to StackPane
                .toList(); // Collect as List

        for (StackPane replyPictureStackPane: replyPictureMessageStackPanes) {
            replyPictureStackPane.getChildren().clear();
            Label repliedMessageDeletedMessage = new Label("(deleted message)");
            repliedMessageDeletedMessage.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-deleted-message" : "chat-message-contact-deleted-message");
            StackPane.setAlignment(repliedMessageDeletedMessage, Pos.TOP_LEFT);
            StackPane.setMargin(repliedMessageDeletedMessage,new Insets(10,8,5,8));
            replyPictureStackPane.getChildren().add(repliedMessageDeletedMessage);
            replyPictureStackPane.setOnMouseClicked(null);
            replyPictureStackPane.setCursor(Cursor.DEFAULT);
        }
    }
    public void deletePotentialWrapper(int messageId) {
        mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#replyWrapper"+messageId));
        mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#editWrapper"+messageId));
    }
}
