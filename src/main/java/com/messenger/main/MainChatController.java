package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.shape.Rectangle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.Cursor;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainChatController {
    @FXML
    private Pane chatBackgroundPane;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private Label chatMainAvatarLabel;
    @FXML
    private Label chatMainNameLabel;
    @FXML
    private VBox chatVBox;
    @FXML
    private TextField chatTextField;

    private AnchorPane mainAnchorPane;
    private int contactId;
    private int mainUserId;
    private Pane mainContactPane;
    private Label mainContactMessageLabel;
    private Label mainContactTimeLabel;

    private String sendingMessageType = "text"; // the default type of the message is always text
    private int editedMessageId = -1;
    private int repliedMessageId = -1;

    private String pathToPicture = "";
    private String messageToThePicture = "";


    // set main value
    public void setMainAnchorPane(AnchorPane anchorPane) {
        this.mainAnchorPane = anchorPane;
    }
    public void setMainUserId(int id) {
        this.mainUserId = id;
    }
    public void setContactId(int id) {
        this.contactId = id;
    }
    public void setMainContactPane(Pane mainContactPane) {
        this.mainContactPane = mainContactPane;
        this.mainContactMessageLabel = (Label) mainContactPane.lookup("#mainContactMessageLabel");
    }
    public void setMainContactTimeLabel(Label timeLabel) {
        this.mainContactTimeLabel = timeLabel;
    }


    // initialization of the chat window
    public void initializeWithValue() throws SQLException, ExecutionException, InterruptedException {
        initializeChatInterface();
        loadChatHistory();
    }


    // chat interface
    private void initializeChatInterface() throws SQLException {
        removeTitle();
        setChatPosition();
        checkForWrappers();
        setProfilePicture();
        setName();
        applyScrollBarEffect(chatScrollPane);
        setMessageSpacing(3);
        setChatTextFieldFocus();
    }
    private void removeTitle() {
        Set<String> titlesToRemove = new HashSet<>(Arrays.asList("mainTitle", "mainSmallTitle", "mainLoginTitle"));

        List<Label> titles = mainAnchorPane.getChildren().stream()
                .filter(node -> node instanceof Label && titlesToRemove.contains(node.getId())) // Check type and ID
                .map(node -> (Label) node)
                .toList(); // Collect into List<Label>
        mainAnchorPane.getChildren().removeAll(titles);
    }
    private void setChatPosition() {
        chatBackgroundPane.setLayoutX(310);
    }
    private void checkForWrappers() {
        Pane replyPane = (Pane) mainAnchorPane.lookup("#reply-wrapper");
        Pane changePane = (Pane) mainAnchorPane.lookup("#reply-wrapper");
        if (replyPane != null) mainAnchorPane.getChildren().remove(replyPane);
        if (changePane != null) mainAnchorPane.getChildren().remove(changePane);
    }
    private void setProfilePicture() throws SQLException {
        if (UsersDataBase.getAvatarWithId(contactId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(contactId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(33);
            imageView.setFitWidth(33);
            imageView.setSmooth(true);
            chatMainAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(16.5F);
            clip.setLayoutY(16.5F);
            clip.setRadius(16.5F);
            chatMainAvatarLabel.setClip(clip);
        }
    }
    private void setName() throws SQLException {
        chatMainNameLabel.setText(UsersDataBase.getNameWithId(contactId));
    }
    private void applyScrollBarEffect(ScrollPane scrollPane) {
        ScrollPaneEffect.addScrollBarEffect(scrollPane);
    }
    private void setMessageSpacing(double space) {
        chatVBox.setSpacing(space);
    }
    private void setChatTextFieldFocus() {
        Platform.runLater(() -> {
            if (chatTextField.isVisible() && !chatTextField.isDisabled()) {
                chatTextField.requestFocus();
            }
        });
    }



    // chat message history
    private void loadChatHistory() throws SQLException, ExecutionException, InterruptedException {
        List<Object> lastMessage = ChatsDataBase.getLastMessageWithId(mainUserId,contactId);
        boolean chatIsEmpty = (lastMessage.get(0) == null) && ((int)lastMessage.get(1) == -1);
        if (chatIsEmpty) {
            setChatCurrentDateLabel();
        } else {
            List<List<Object>> allMessages = ChatsDataBase.getAllMessages(mainUserId,contactId);
            HashMap<String,List<List<Object>>> splitMessagesByDate = getSplitByDayMessages(allMessages);
            for (List<List<Object>> messagesBySameDay : splitMessagesByDate.values()) {
                String messagesLongDate = getMessageLongDate(messagesBySameDay.get(0).get(6).toString());
                setChatDateLabel(messagesLongDate);
                loadMessages(messagesBySameDay);
            }
            // Introduce a small delay before scrolling to the bottom
            PauseTransition pause = new PauseTransition(Duration.millis(150)); // Adjust the duration as necessary
            pause.setOnFinished(event -> chatScrollPane.setVvalue(1.0));
            pause.play();

            setBottomButtonListener();
        }
    }
    private void setChatCurrentDateLabel() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
        String formattedDate = today.format(formatter);
        setChatDateLabel(formattedDate);
    }
    private HashMap<String,List<List<Object>>> getSplitByDayMessages(List<List<Object>> allMessages) {
        // returns a hash map in format: "06.12.2024"=[[message],[message]], etc.
        HashMap<String,List<List<Object>>> splitMessagesByDate = new LinkedHashMap<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (List<Object> message : allMessages) {
            LocalDate messageOriginalDate = LocalDate.parse((String)message.get(6),dateTimeFormatter); //2024-12-06
            DateTimeFormatter desiredFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String formattedDate = messageOriginalDate.format(desiredFormat);
            splitMessagesByDate.computeIfAbsent(formattedDate,k -> new ArrayList<>()).add(message);
        }
        return splitMessagesByDate;
    }
    private void setChatDateLabel(String date) {
        Label chatDateLabel = new Label();
        chatDateLabel.getStyleClass().add("chat-date-label");
        chatDateLabel.setText(date);
        VBox.setMargin(chatDateLabel,new Insets(15,0,25,0));
        chatVBox.getChildren().add(chatDateLabel);
    }
    private boolean isFirstMessage(List<List<Object>> allMessages,int messageId) {
        return (int) allMessages.get(0).get(0) == messageId;
    }
    private boolean isResponseMessage(List<List<Object>> messages,int messageId) throws SQLException {
        if (messageId == (int) messages.get(0).get(0)) return false; // if the message is first
        int previousMessageSenderId = (int) messages.get(getIndexWithMessageId(messages,messageId)-1).get(1);
        int messageSenderId = ChatsDataBase.getSenderIdWithMessageId(messageId);
        return previousMessageSenderId != messageSenderId;
    }
    private boolean isAfterOneHourMessage(List<List<Object>> messages,int messageId) throws SQLException {
        if (messageId == (int) messages.get(0).get(0)) return false; // if the message is first
        String previousMessageTime = getMessageHours((String) messages.get(getIndexWithMessageId(messages,messageId)-1).get(6));
        String messageTime = getMessageHours((String) ChatsDataBase.getMessageWithId(messageId).get(6));
        return isDifferenceGreaterThanOneHour(previousMessageTime,messageTime);
    }
    private void loadMessages(List<List<Object>> messages) throws SQLException, ExecutionException, InterruptedException {
        for (List<Object> message : messages) {
            int messageId = (int) message.get(0);
            boolean isFirstMessage = isFirstMessage(messages,messageId);
            boolean isResponseMessage = isResponseMessage(messages,messageId);   // follows after a contacts message
            boolean isAfterOneHourMessage = isAfterOneHourMessage(messages,messageId);
            boolean avatarRequired =  isFirstMessage || isResponseMessage || isAfterOneHourMessage;  // avatar near the message is going to be sent withing those conditions
            loadMessage(message,avatarRequired);
        }
    }
    private void loadMessage(List<Object> message, boolean avatarRequired) throws SQLException, ExecutionException, InterruptedException {
        int messageId = (int) message.get(0);
        String messageType = getTypeOfMessage(message);
        int senderId = (int) message.get(1);
        String messageTime = getMessageHours((String) message.get(6));
        int replyMessageId = (int) message.get(5);
        byte[] picture = (byte[]) message.get(4);

        HBox messageHBox = new HBox();
        messageHBox.setId("messageHBox"+messageId);
        messageHBox.setAlignment((senderId == mainUserId) ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);

        switch (messageType) {
            case "text":

                String messageText = (String) message.get(3);

                int normalMessagePaddingTop = 7;
                int normalMessagePaddingRight = 45;
                int normalMessagePaddingBottom = 7;
                int normalMessagePaddingLeft = 12;

                int normalMessagePaneHeight = (normalMessagePaddingTop + normalMessagePaddingBottom) + calculateLabelHeight(messageText);
                int normalMessagePaneWidth = (normalMessagePaddingLeft + normalMessagePaddingRight) + calculateLabelWidth(messageText);

                Pane normalMessagePane = new Pane();
                normalMessagePane.setId("messagePane"+messageId);
                normalMessagePane.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-pane" : "chat-message-contact-pane");
                normalMessagePane.setPrefHeight(normalMessagePaneHeight);
                normalMessagePane.setPrefWidth(normalMessagePaneWidth);

                Label normalMessageTextLabel = new Label(messageText);
                normalMessageTextLabel.setId("messageLabel"+messageId);
                normalMessageTextLabel.getStyleClass().add("chat-message-label");
                normalMessageTextLabel.setWrapText(true);  // Text soll umgebrochen werden
                normalMessageTextLabel.setMinWidth(0);     // Minimale Breite 0
                normalMessageTextLabel.setMaxWidth(292);   // Maximale Breite 292
                normalMessageTextLabel.setLayoutX(normalMessagePaddingLeft);
                normalMessageTextLabel.setLayoutY(normalMessagePaddingTop);

                Label normalMessageTimeLabel = new Label(messageTime);
                normalMessageTimeLabel.getStyleClass().add("chat-time-label");
                normalMessageTimeLabel.layoutXProperty().bind(normalMessagePane.widthProperty().subtract(normalMessageTimeLabel.widthProperty()).subtract(9)); // 10px padding from the right edge
                normalMessageTimeLabel.layoutYProperty().bind(normalMessagePane.heightProperty().subtract(normalMessageTimeLabel.heightProperty()).subtract(4)); // 10px padding from the bottom edge

                normalMessagePane.getChildren().addAll(normalMessageTextLabel, normalMessageTimeLabel);
                messageHBox.getChildren().add(normalMessagePane);

                if (avatarRequired) {
                    setAppropriateAvatar(messageHBox,normalMessagePane,senderId,messageId);
                } else {
                    HBox.setMargin(normalMessagePane, (senderId == mainUserId) ? new Insets(0, 63, 0, 0) : new Insets(0, 0, 0, 63));
                }

                // right-click on the message opens buttons (reply,edit,delete)
                normalMessagePane.setOnMouseClicked(mouseEvent -> {
                    if ((senderId == mainUserId) && (mouseEvent.getButton() == MouseButton.SECONDARY)) {
                        Point2D anchorPaneCoordinates = convertToTopLevelAnchorPaneCoordinates(normalMessagePane,mouseEvent.getX(),mouseEvent.getY());
                        int anchorPaneScaleX = (int) anchorPaneCoordinates.getX();
                        int anchorPaneScaleY = (int) anchorPaneCoordinates.getY();
                        showMessageButtons(anchorPaneScaleX,anchorPaneScaleY,messageId);
                    } else if ((senderId != mainUserId) && (mouseEvent.getButton() == MouseButton.SECONDARY)) {
                        Point2D anchorPaneCoordinates = convertToTopLevelAnchorPaneCoordinates(normalMessagePane,mouseEvent.getX(),mouseEvent.getY());
                        int anchorPaneScaleX = (int) anchorPaneCoordinates.getX();
                        int anchorPaneScaleY = (int) anchorPaneCoordinates.getY();
                        showReplyMessageButton(anchorPaneScaleX,anchorPaneScaleY,messageId);
                    }
                });


                break;

            case "reply_with_text":

                String messageReplyText = (String) message.get(3);

                int replyWithTextPaddingTop = 45;
                int replyWithTextPaddingRight = 45;
                int replyWithTextPaddingBottom = 7;
                int replyWithTextPaddingLeft = 12;

                int replyWithTextMessagePaneHeight = (replyWithTextPaddingTop + replyWithTextPaddingBottom) + calculateLabelHeight(messageReplyText);
                int replyWithTextMessagePaneWidth = (replyWithTextPaddingLeft + replyWithTextPaddingRight) + calculateLabelWidth(messageReplyText);

                Pane replyWithTextMessagePane = new Pane();
                replyWithTextMessagePane.setId("messagePane"+messageId);
                replyWithTextMessagePane.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-pane" : "chat-message-contact-pane");
                replyWithTextMessagePane.setPrefHeight(replyWithTextMessagePaneHeight);
                replyWithTextMessagePane.setPrefWidth(replyWithTextMessagePaneWidth);

                Label replyWithTextMessageTextLabel = new Label(messageReplyText);
                replyWithTextMessageTextLabel.setId("messageLabel"+messageId);
                replyWithTextMessageTextLabel.getStyleClass().add("chat-message-label");
                replyWithTextMessageTextLabel.setWrapText(true);  // Text soll umgebrochen werden
                replyWithTextMessageTextLabel.setMinWidth(0);     // Minimale Breite 0
                replyWithTextMessageTextLabel.setMaxWidth(292);   // Maximale Breite 292
                replyWithTextMessageTextLabel.setLayoutX(replyWithTextPaddingLeft);
                replyWithTextMessageTextLabel.setLayoutY(replyWithTextPaddingTop);

                Label replyWithTextTimeLabel = new Label(messageTime);
                replyWithTextTimeLabel.getStyleClass().add("chat-time-label");
                replyWithTextTimeLabel.layoutXProperty().bind(replyWithTextMessagePane.widthProperty().subtract(replyWithTextTimeLabel.widthProperty()).subtract(9)); // 10px padding from the right edge
                replyWithTextTimeLabel.layoutYProperty().bind(replyWithTextMessagePane.heightProperty().subtract(replyWithTextTimeLabel.heightProperty()).subtract(4)); // 10px padding from the bottom edge

                int replyWithTextPaneTopMargin = 6;
                int replyWithTextPaneLeftMargin = 6;
                int replyWithTextReplyPaneWidth = replyWithTextMessagePaneWidth - (replyWithTextPaneLeftMargin * 2);
                int replyWithTextReplyPaneHeight = 33;
                Pane replyWithTextReplyPane = new Pane();
                replyWithTextReplyPane.setId("replyMessagePane"+messageId);
                replyWithTextReplyPane.setPrefWidth(replyWithTextReplyPaneWidth);
                replyWithTextReplyPane.setPrefHeight(replyWithTextReplyPaneHeight);
                replyWithTextReplyPane.getStyleClass().add((senderId == mainUserId) ? "chat-message-reply-user-pane" : "chat-message-reply-contact-pane");
                replyWithTextReplyPane.setLayoutX(replyWithTextPaneLeftMargin);
                replyWithTextReplyPane.setLayoutY(replyWithTextPaneTopMargin);

                boolean repliedMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,replyMessageId);

                if (repliedMessageExists) {
                    int replyWithTextReplyNameLeftMargin = 5;
                    int replyWithTextReplyNameTopMargin = 2;
                    String repliedMessageUserName = UsersDataBase.getNameWithId((int) ChatsDataBase.getMessageWithId(replyMessageId).get(1));
                    Label replyWithTextReplyName = new Label(repliedMessageUserName);
                    replyWithTextReplyName.setId("replyNameLabel"+messageId);
                    replyWithTextReplyName.setMaxWidth(replyWithTextReplyPaneWidth - (replyWithTextReplyNameLeftMargin * 2));
                    replyWithTextReplyName.getStyleClass().add("chat-message-reply-name");
                    replyWithTextReplyName.setLayoutX(replyWithTextReplyNameLeftMargin);
                    replyWithTextReplyName.setLayoutY(replyWithTextReplyNameTopMargin);


                    if (replyMessageId != -1 && ChatsDataBase.getMessageWithId(replyMessageId).get(4) != null) {
                        int replyWithTextReplyPhotoSymbolLeftMargin = 5;
                        int replyWithTextReplyPhotoSymbolTopMargin = 17;
                        Label replyWithTextReplyPhotoSymbolLabel = new Label();
                        replyWithTextReplyPhotoSymbolLabel.getStyleClass().add("chat-reply-photo-symbol");
                        replyWithTextReplyPhotoSymbolLabel.setPrefWidth(13);
                        replyWithTextReplyPhotoSymbolLabel.setPrefHeight(13);
                        replyWithTextReplyPhotoSymbolLabel.setLayoutX(replyWithTextReplyPhotoSymbolLeftMargin);
                        replyWithTextReplyPhotoSymbolLabel.setLayoutY(replyWithTextReplyPhotoSymbolTopMargin);

                        int replyWithTextReplyMessageLeftMargin = replyWithTextReplyPhotoSymbolLeftMargin + 15;
                        int replyWithTextReplyMessageTopMargin = replyWithTextReplyPhotoSymbolTopMargin - 1;
                        Label replyWithTextReplyMessage = new Label("Photo");
                        replyWithTextReplyMessage.setId("replyMessageLabel"+messageId);
                        replyWithTextReplyMessage.setMaxWidth(replyWithTextReplyPaneWidth - (replyWithTextReplyMessageLeftMargin * 2));
                        replyWithTextReplyMessage.getStyleClass().add("chat-message-reply-message");
                        replyWithTextReplyMessage.setLayoutX(replyWithTextReplyMessageLeftMargin);
                        replyWithTextReplyMessage.setLayoutY(replyWithTextReplyMessageTopMargin);

                        replyWithTextReplyPane.getChildren().addAll(replyWithTextReplyPhotoSymbolLabel,replyWithTextReplyMessage);
                    } else {
                        int replyWithTextReplyMessageLeftMargin = 5;
                        int replyWithTextReplyMessageTopMargin = 15;
                        Label replyWithTextReplyMessage = new Label((String) ChatsDataBase.getMessageWithId(replyMessageId).get(3));
                        replyWithTextReplyMessage.setId("replyMessageLabel"+messageId);
                        replyWithTextReplyMessage.setMaxWidth(replyWithTextReplyPaneWidth - (replyWithTextReplyMessageLeftMargin * 2));
                        replyWithTextReplyMessage.getStyleClass().add("chat-message-reply-message");
                        replyWithTextReplyMessage.setLayoutX(replyWithTextReplyMessageLeftMargin);
                        replyWithTextReplyMessage.setLayoutY(replyWithTextReplyMessageTopMargin);

                        replyWithTextReplyPane.getChildren().add(replyWithTextReplyMessage);
                    }


                    replyWithTextReplyPane.getChildren().addAll(replyWithTextReplyName);
                } else {
                    int replyWithTextDeletedMessageLeftMargin = 7;
                    int replyWithTextDeletedMessageTopMargin = 8;
                    Label deletedMessageLabel = new Label("(deleted)");
                    deletedMessageLabel.getStyleClass().add("chat-message-reply-deleted");
                    deletedMessageLabel.setMaxWidth(replyWithTextReplyPaneWidth - (replyWithTextDeletedMessageLeftMargin * 2));
                    deletedMessageLabel.setLayoutX(replyWithTextDeletedMessageLeftMargin);
                    deletedMessageLabel.setLayoutY(replyWithTextDeletedMessageTopMargin);

                    replyWithTextReplyPane.getChildren().add(deletedMessageLabel);
                }

                replyWithTextMessagePane.getChildren().addAll(replyWithTextReplyPane,replyWithTextMessageTextLabel, replyWithTextTimeLabel);
                messageHBox.getChildren().add(replyWithTextMessagePane);

                if (avatarRequired) {
                    setAppropriateAvatar(messageHBox,replyWithTextMessagePane,senderId,messageId);
                } else {
                    HBox.setMargin(replyWithTextMessagePane, (senderId == mainUserId) ? new Insets(0, 63, 0, 0) : new Insets(0, 0, 0, 63));
                }

                replyWithTextReplyPane.setOnMouseClicked(mouseEvent -> {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && (mainAnchorPane.lookup("#messageHBox"+replyMessageId) != null)) {
                        HBox repliedMessageHBox = (HBox) mainAnchorPane.lookup("#messageHBox"+replyMessageId);
                        selectRepliedMessageHBox(repliedMessageHBox);
                        scrollSmooth(getHBoxVvalue(repliedMessageHBox));
                    }
                });

                // right-click on the message opens buttons (reply,edit,delete)
                replyWithTextMessagePane.setOnMouseClicked(mouseEvent -> {
                    if ((senderId == mainUserId) && (mouseEvent.getButton() == MouseButton.SECONDARY)) {
                        Point2D anchorPaneCoordinates = convertToTopLevelAnchorPaneCoordinates(replyWithTextMessagePane,mouseEvent.getX(),mouseEvent.getY());
                        int anchorPaneScaleX = (int) anchorPaneCoordinates.getX();
                        int anchorPaneScaleY = (int) anchorPaneCoordinates.getY();
                        showMessageButtons(anchorPaneScaleX,anchorPaneScaleY,messageId);
                    } else if ((senderId != mainUserId) && (mouseEvent.getButton() == MouseButton.SECONDARY)) {
                        Point2D anchorPaneCoordinates = convertToTopLevelAnchorPaneCoordinates(replyWithTextMessagePane,mouseEvent.getX(),mouseEvent.getY());
                        int anchorPaneScaleX = (int) anchorPaneCoordinates.getX();
                        int anchorPaneScaleY = (int) anchorPaneCoordinates.getY();
                        showReplyMessageButton(anchorPaneScaleX,anchorPaneScaleY,messageId);
                    }
                });
                setHoverCursorToHand(replyWithTextReplyPane);
                break;

            case "reply_with_picture":

                int replyWithPicturePaddingTop = 45;
                int replyWithPicturePaddingRight = 6;
                int replyWithPicturePaddingBottom = 6;
                int replyWithPicturePaddingLeft = 6;
                int replyWithPictureMessagePaneHeight = (replyWithPicturePaddingTop + replyWithPicturePaddingBottom) + calculatePictureHeight(picture);
                int replyWithPictureMessagePaneWidth = 349;

                Pane replyWithPictureMessagePane = new Pane();
                replyWithPictureMessagePane.setId("messagePane"+messageId);
                replyWithPictureMessagePane.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-pane" : "chat-message-contact-pane");
                replyWithPictureMessagePane.setPrefHeight(replyWithPictureMessagePaneHeight);
                replyWithPictureMessagePane.setPrefWidth(replyWithPictureMessagePaneWidth);

                int replyWithPicturePaneTopMargin = 6;
                int replyWithPicturePaneLeftMargin = 6;
                int replyWithPictureReplyPaneWidth = replyWithPictureMessagePaneWidth - (replyWithPicturePaneLeftMargin * 2);
                int replyWithPictureReplyPaneHeight = 33;
                Pane replyWithPictureReplyPane = new Pane();
                replyWithPictureReplyPane.setId("replyMessagePane"+messageId);
                replyWithPictureReplyPane.setPrefWidth(replyWithPictureReplyPaneWidth);
                replyWithPictureReplyPane.setPrefHeight(replyWithPictureReplyPaneHeight);
                replyWithPictureReplyPane.getStyleClass().add((senderId == mainUserId) ? "chat-message-reply-user-pane" : "chat-message-reply-contact-pane");
                replyWithPictureReplyPane.setLayoutX(replyWithPicturePaneLeftMargin);
                replyWithPictureReplyPane.setLayoutY(replyWithPicturePaneTopMargin);

                Label replyWithPictureLabel = new Label();
                setPictureToLabel(picture,replyWithPictureLabel,337);
                replyWithPictureLabel.setLayoutX(replyWithPicturePaddingLeft);
                replyWithPictureLabel.setLayoutY(replyWithPicturePaddingTop);

                Label pictureTimeLabel = new Label(messageTime);
                pictureTimeLabel.setMouseTransparent(true);
                pictureTimeLabel.getStyleClass().add("chat-picture-time");
                pictureTimeLabel.layoutXProperty().bind(replyWithPictureMessagePane.widthProperty().subtract(pictureTimeLabel.widthProperty()).subtract(12)); // 10px padding from the right edge
                pictureTimeLabel.layoutYProperty().bind(replyWithPictureMessagePane.heightProperty().subtract(pictureTimeLabel.heightProperty()).subtract(12)); // 10px padding from the bottom edge

                replyWithPictureMessagePane.getChildren().addAll(replyWithPictureReplyPane,replyWithPictureLabel,pictureTimeLabel);
                messageHBox.getChildren().add(replyWithPictureMessagePane);

                boolean repliedMessageExists1 = ChatsDataBase.messageExists(mainUserId,contactId,replyMessageId);

                if (repliedMessageExists1) {
                    int replyWithTextReplyNameLeftMargin = 5;
                    int replyWithTextReplyNameTopMargin = 2;
                    String repliedMessageUserName = UsersDataBase.getNameWithId((int) ChatsDataBase.getMessageWithId(replyMessageId).get(1));
                    Label replyWithTextReplyName = new Label(repliedMessageUserName);
                    replyWithTextReplyName.setId("replyNameLabel"+messageId);
                    replyWithTextReplyName.setMaxWidth(replyWithPictureReplyPaneWidth - (replyWithTextReplyNameLeftMargin * 2));
                    replyWithTextReplyName.getStyleClass().add("chat-message-reply-name");
                    replyWithTextReplyName.setLayoutX(replyWithTextReplyNameLeftMargin);
                    replyWithTextReplyName.setLayoutY(replyWithTextReplyNameTopMargin);


                    if (replyMessageId != -1 && ChatsDataBase.getMessageWithId(replyMessageId).get(4) != null) {
                        int replyWithTextReplyPhotoSymbolLeftMargin = 5;
                        int replyWithTextReplyPhotoSymbolTopMargin = 17;
                        Label replyWithTextReplyPhotoSymbolLabel = new Label();
                        replyWithTextReplyPhotoSymbolLabel.getStyleClass().add("chat-reply-photo-symbol");
                        replyWithTextReplyPhotoSymbolLabel.setPrefWidth(13);
                        replyWithTextReplyPhotoSymbolLabel.setPrefHeight(13);
                        replyWithTextReplyPhotoSymbolLabel.setLayoutX(replyWithTextReplyPhotoSymbolLeftMargin);
                        replyWithTextReplyPhotoSymbolLabel.setLayoutY(replyWithTextReplyPhotoSymbolTopMargin);

                        int replyWithTextReplyMessageLeftMargin = replyWithTextReplyPhotoSymbolLeftMargin + 15;
                        int replyWithTextReplyMessageTopMargin = replyWithTextReplyPhotoSymbolTopMargin - 1;
                        Label replyWithTextReplyMessage = new Label("Photo");
                        replyWithTextReplyMessage.setId("replyMessageLabel"+messageId);
                        replyWithTextReplyMessage.setMaxWidth(replyWithPictureReplyPaneWidth - (replyWithTextReplyMessageLeftMargin * 2));
                        replyWithTextReplyMessage.getStyleClass().add("chat-message-reply-message");
                        replyWithTextReplyMessage.setLayoutX(replyWithTextReplyMessageLeftMargin);
                        replyWithTextReplyMessage.setLayoutY(replyWithTextReplyMessageTopMargin);

                        replyWithPictureReplyPane.getChildren().addAll(replyWithTextReplyPhotoSymbolLabel,replyWithTextReplyMessage);
                    } else {
                        int replyWithTextReplyMessageLeftMargin = 5;
                        int replyWithTextReplyMessageTopMargin = 15;
                        Label replyWithTextReplyMessage = new Label((String) ChatsDataBase.getMessageWithId(replyMessageId).get(3));
                        replyWithTextReplyMessage.setId("replyMessageLabel"+messageId);
                        replyWithTextReplyMessage.setMaxWidth(replyWithPictureReplyPaneWidth - (replyWithTextReplyMessageLeftMargin * 2));
                        replyWithTextReplyMessage.getStyleClass().add("chat-message-reply-message");
                        replyWithTextReplyMessage.setLayoutX(replyWithTextReplyMessageLeftMargin);
                        replyWithTextReplyMessage.setLayoutY(replyWithTextReplyMessageTopMargin);

                        replyWithPictureReplyPane.getChildren().add(replyWithTextReplyMessage);
                    }


                    replyWithPictureReplyPane.getChildren().addAll(replyWithTextReplyName);
                } else {
                    int replyWithTextDeletedMessageLeftMargin = 7;
                    int replyWithTextDeletedMessageTopMargin = 8;
                    Label deletedMessageLabel = new Label("(deleted)");
                    deletedMessageLabel.getStyleClass().add("chat-message-reply-deleted");
                    deletedMessageLabel.setMaxWidth(replyWithPictureReplyPaneWidth - (replyWithTextDeletedMessageLeftMargin * 2));
                    deletedMessageLabel.setLayoutX(replyWithTextDeletedMessageLeftMargin);
                    deletedMessageLabel.setLayoutY(replyWithTextDeletedMessageTopMargin);

                    replyWithPictureReplyPane.getChildren().add(deletedMessageLabel);
                }

                if (avatarRequired) {
                    setAppropriateAvatar(messageHBox,replyWithPictureMessagePane,senderId,messageId);
                } else {
                    HBox.setMargin(replyWithPictureMessagePane, (senderId == mainUserId) ? new Insets(0, 63, 0, 0) : new Insets(0, 0, 0, 63));
                }

                replyWithPictureReplyPane.setOnMouseClicked(mouseEvent -> {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && (mainAnchorPane.lookup("#messageHBox"+replyMessageId) != null)) {
                        HBox repliedMessageHBox = (HBox) mainAnchorPane.lookup("#messageHBox"+replyMessageId);
                        selectRepliedMessageHBox(repliedMessageHBox);
                        scrollSmooth(getHBoxVvalue(repliedMessageHBox));
                    }
                });

                // right-click on the message opens buttons (reply,edit,delete)
                replyWithPictureMessagePane.setOnMouseClicked(mouseEvent -> {
                    if ((senderId == mainUserId) && (mouseEvent.getButton() == MouseButton.SECONDARY)) {
                        Point2D anchorPaneCoordinates = convertToTopLevelAnchorPaneCoordinates(replyWithPictureMessagePane,mouseEvent.getX(),mouseEvent.getY());
                        int anchorPaneScaleX = (int) anchorPaneCoordinates.getX();
                        int anchorPaneScaleY = (int) anchorPaneCoordinates.getY();
                        showMessageButtons(anchorPaneScaleX,anchorPaneScaleY,messageId);
                    } else if ((senderId != mainUserId) && (mouseEvent.getButton() == MouseButton.SECONDARY)) {
                        Point2D anchorPaneCoordinates = convertToTopLevelAnchorPaneCoordinates(replyWithPictureMessagePane,mouseEvent.getX(),mouseEvent.getY());
                        int anchorPaneScaleX = (int) anchorPaneCoordinates.getX();
                        int anchorPaneScaleY = (int) anchorPaneCoordinates.getY();
                        showReplyMessageButton(anchorPaneScaleX,anchorPaneScaleY,messageId);
                    }
                });
                setHoverCursorToHand(replyWithPictureReplyPane);
                setHoverCursorToHand(replyWithPictureLabel);



                break;

            case "picture":

                Pane pictureMessagePane = new Pane();
                pictureMessagePane.setId("messagePane"+messageId);

                Label pictureLabel = new Label();
                setPictureToLabel(picture,pictureLabel,349);

                pictureMessagePane.getChildren().add(pictureLabel);
                messageHBox.getChildren().add(pictureMessagePane);
                setHoverCursorToHand(pictureLabel);

                Label pictureTimeLabel1 = new Label(messageTime);
                pictureTimeLabel1.setMouseTransparent(true);
                pictureTimeLabel1.getStyleClass().add("chat-picture-time");
                pictureTimeLabel1.layoutXProperty().bind(pictureMessagePane.widthProperty().subtract(pictureTimeLabel1.widthProperty()).subtract(7)); // 10px padding from the right edge
                pictureTimeLabel1.layoutYProperty().bind(pictureMessagePane.heightProperty().subtract(pictureTimeLabel1.heightProperty()).subtract(7)); // 10px padding from the bottom edge
                pictureMessagePane.getChildren().add(pictureTimeLabel1);

                if (avatarRequired) {
                    setAppropriateAvatar(messageHBox,pictureMessagePane,senderId,messageId);
                } else {
                    HBox.setMargin(pictureMessagePane,(senderId == mainUserId) ? new Insets(0, 63, 0, 0) : new Insets(0, 0, 0, 63));
                }

                pictureLabel.setOnMouseClicked(clickEvent -> {
                    if ((senderId == mainUserId) && (clickEvent.getButton() == MouseButton.SECONDARY)) {
                        Point2D anchorPaneCoordinates = convertToTopLevelAnchorPaneCoordinates(pictureLabel,clickEvent.getX(),clickEvent.getY());
                        int anchorPaneScaleX = (int) anchorPaneCoordinates.getX();
                        int anchorPaneScaleY = (int) anchorPaneCoordinates.getY();
                        showMessageButtons(anchorPaneScaleX,anchorPaneScaleY,messageId);
                    } else if ((senderId != mainUserId) && (clickEvent.getButton() == MouseButton.SECONDARY)) {
                        Point2D anchorPaneCoordinates = convertToTopLevelAnchorPaneCoordinates(pictureLabel,clickEvent.getX(),clickEvent.getY());
                        int anchorPaneScaleX = (int) anchorPaneCoordinates.getX();
                        int anchorPaneScaleY = (int) anchorPaneCoordinates.getY();
                        showReplyMessageButton(anchorPaneScaleX,anchorPaneScaleY,messageId);
                    }
                });

                setHoverCursorToHand(pictureLabel);


                break;
        }


        chatVBox.getChildren().add(messageHBox);

    }



    // chat message sending
    @FXML
    public void sendMessage() throws SQLException, ExecutionException, InterruptedException, IOException {
        String currentMessageFullDate = getCurrentFullDate();
        switch (sendingMessageType) {
            case "text":

                String messageText = chatTextField.getText().trim();
                if (messageText.isEmpty()) return;

                int messageId = addMessageToDB(messageText,null,-1,currentMessageFullDate,false);
                List<List<Object>> allMessages = ChatsDataBase.getAllMessages(mainUserId,contactId);
                String previousMessageDate = isFirstMessage(allMessages,messageId) ? "0" : getMessageDate((String) allMessages.get(getIndexWithMessageId(allMessages,messageId)-1).get(6));

                boolean isFirstMessage = isFirstMessage(allMessages,messageId);
                boolean isResponseMessage = isResponseMessage(allMessages,messageId);
                boolean isAfterOneHourMessage = isAfterOneHourMessage(allMessages,messageId);
                boolean isNewDayMessage = isNewDayMessage(getCurrentDate(),previousMessageDate);
                boolean avatarIsRequired = isFirstMessage || isResponseMessage || isAfterOneHourMessage || isNewDayMessage;

                List<Object> message = ChatsDataBase.getMessageWithId(messageId);

                if (isNewDayMessage(getCurrentDate(),previousMessageDate) && !isFirstMessage) {
                    setChatDateLabel(getCurrentLongDate());
                }

                loadMessage(message,avatarIsRequired);
                mainContactMessageLabel.getStyleClass().clear();
                mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
                mainContactMessageLabel.setText(messageText);
                mainContactTimeLabel.setText(getCurrentTime());
                scrollToBottom();

                break;

            case "edit_with_text":

                String editedMessageText = chatTextField.getText().trim();
                if (editedMessageText.isEmpty()) return;

                String messageType = getTypeOfMessage(ChatsDataBase.getMessageWithId(editedMessageId));

                int paddingTop = getPaddingTop(messageType);
                int paddingRight = 45;
                int paddingBottom = 7;
                int paddingLeft = 12;

                int messagePaneWidth = (paddingLeft + paddingRight) + calculateLabelWidth(editedMessageText);
                int messagePaneHeight = (paddingTop + paddingBottom) + calculateLabelHeight(editedMessageText);

                Pane messagePane = (( Pane ) mainAnchorPane.lookup("#messagePane"+editedMessageId));

                messagePane.setPrefWidth(messagePaneWidth);
                messagePane.setPrefHeight(messagePaneHeight);

                int replyPaneLeftPadding = 6;
                int nameMessageLeftPadding = 5;
                int replyMessagePaneWidth = messagePaneWidth - (replyPaneLeftPadding * 2);
                int replyMessageNameMessageWidth = messagePaneWidth - (nameMessageLeftPadding * 2) - (replyPaneLeftPadding * 2);
                if (messageType.contains("reply")) {
                    ((Pane) mainAnchorPane.lookup("#replyMessagePane"+editedMessageId)).setPrefWidth(replyMessagePaneWidth);
                    ((Label)(mainAnchorPane.lookup("#replyNameLabel"+editedMessageId))).setPrefWidth(replyMessageNameMessageWidth);
                    ((Label)(mainAnchorPane.lookup("#replyNameLabel"+editedMessageId))).setMinWidth(replyMessageNameMessageWidth);
                    ((Label)(mainAnchorPane.lookup("#replyMessageLabel"+editedMessageId))).setPrefWidth(replyMessageNameMessageWidth);
                    ((Label)(mainAnchorPane.lookup("#replyMessageLabel"+editedMessageId))).setMinWidth(replyMessageNameMessageWidth);
                }

                chatScrollPane.setPadding(new Insets(0,0,8,0));

                ChatsDataBase.editMessage(editedMessageId,editedMessageText);

                (( Label ) mainAnchorPane.lookup("#messageLabel"+editedMessageId)).setText(editedMessageText);

                if (mainAnchorPane.lookup("#editWrapper") != null) {
                    mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#editWrapper"));
                }

                if (editedMessageId == ChatsDataBase.getLastMessageId(mainUserId,contactId)) {
                    mainContactMessageLabel.getStyleClass().clear();
                    mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
                    mainContactMessageLabel.setText(editedMessageText);
                }

                List<Integer> linkedByReplyMessageIds = ChatsDataBase.getRepliedMessageIds(mainUserId,contactId,editedMessageId);
                if (!linkedByReplyMessageIds.isEmpty()) {
                    for (int id:linkedByReplyMessageIds) {
                        Pane replyPane = (Pane) chatVBox.lookup("#replyMessagePane"+id);
                        Label replyPaneMessageLabel = (Label) replyPane.lookup("#replyMessageLabel"+id);

                        replyPaneMessageLabel.setText(editedMessageText);
                    }
                }

                sendingMessageType = "text";
                editedMessageId = -1;

                break;

            case "edit_with_picture":

                mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#editWrapper"));
                chatScrollPane.setPadding(new Insets(0,0,8,0));

                HBox messageHBox = (HBox) chatVBox.lookup("#messageHBox"+editedMessageId);
                messageHBox.getChildren().remove(messageHBox.lookup("#messagePane"+editedMessageId));
                messageHBox.getChildren().remove(messageHBox.lookup("#pictureStackPane"+editedMessageId));

                Label pictureLabel = new Label();
                setPictureToLabel(convertPictureIntoByte(pathToPicture),pictureLabel,349);
                messageHBox.getChildren().add(0,pictureLabel);

                if (messageHBox.lookup("#avatarLabel"+editedMessageId) != null) {
                    HBox.setMargin(pictureLabel,new Insets(0, 8, 0, 0));
                } else {
                    HBox.setMargin(pictureLabel,new Insets(0, 63, 0, 0));
                }

                List<Integer> linkedByReplyMessageIds1 = ChatsDataBase.getRepliedMessageIds(mainUserId,contactId,editedMessageId);
                if (!linkedByReplyMessageIds1.isEmpty()) {
                    for (int id:linkedByReplyMessageIds1) {
                        Pane replyPane = (Pane) chatVBox.lookup("#replyMessagePane"+id);
                        Label replyPaneMessageLabel = (Label) replyPane.lookup("#replyMessageLabel"+id);
                        replyPane.getChildren().remove(replyPaneMessageLabel);

                        int replyWithTextReplyPhotoSymbolLeftMargin = 5;
                        int replyWithTextReplyPhotoSymbolTopMargin = 17;
                        Label replyWithTextReplyPhotoSymbolLabel = new Label();
                        replyWithTextReplyPhotoSymbolLabel.getStyleClass().add("chat-reply-photo-symbol");
                        replyWithTextReplyPhotoSymbolLabel.setPrefWidth(13);
                        replyWithTextReplyPhotoSymbolLabel.setPrefHeight(13);
                        replyWithTextReplyPhotoSymbolLabel.setLayoutX(replyWithTextReplyPhotoSymbolLeftMargin);
                        replyWithTextReplyPhotoSymbolLabel.setLayoutY(replyWithTextReplyPhotoSymbolTopMargin);

                        int replyWithTextReplyMessageLeftMargin = replyWithTextReplyPhotoSymbolLeftMargin + 15;
                        int replyWithTextReplyMessageTopMargin = replyWithTextReplyPhotoSymbolTopMargin - 1;
                        Label replyWithTextReplyMessage = new Label("Photo");
                        replyWithTextReplyMessage.setId("replyMessageLabel"+id);
                        replyWithTextReplyMessage.setMaxWidth(replyPane.getWidth() - (replyWithTextReplyMessageLeftMargin * 2));
                        replyWithTextReplyMessage.getStyleClass().add("chat-message-reply-message");
                        replyWithTextReplyMessage.setLayoutX(replyWithTextReplyMessageLeftMargin);
                        replyWithTextReplyMessage.setLayoutY(replyWithTextReplyMessageTopMargin);

                        replyPane.getChildren().addAll(replyWithTextReplyPhotoSymbolLabel,replyWithTextReplyMessage);
                    }
                }

                editedMessageId = -1;
                sendingMessageType = "text";

                break;

            case "reply_with_text":

                String replyMessageText = chatTextField.getText().trim();
                if (replyMessageText.isEmpty()) return;

                int replyMessageId = addMessageToDB(replyMessageText,null,repliedMessageId,currentMessageFullDate,false);
                List<List<Object>> allMessages2 = ChatsDataBase.getAllMessages(mainUserId,contactId);
                String previousMessageDate2 = getMessageDate((String) allMessages2.get(getIndexWithMessageId(allMessages2,replyMessageId)-1).get(6));

                boolean isFirstMessage2 = isFirstMessage(allMessages2,replyMessageId);
                boolean isResponseMessage2 = isResponseMessage(allMessages2,replyMessageId);
                boolean isAfterOneHourMessage2 = isAfterOneHourMessage(allMessages2,replyMessageId);
                boolean isNewDayMessage2 = isNewDayMessage(getCurrentDate(), previousMessageDate2);
                boolean avatarIsRequired2 = isFirstMessage2 || isResponseMessage2 || isAfterOneHourMessage2 || isNewDayMessage2;

                List<Object> replyMessage = ChatsDataBase.getMessageWithId(replyMessageId);

                if (isNewDayMessage(getCurrentDate(), previousMessageDate2)) {
                    setChatDateLabel(getCurrentLongDate());
                }
                loadMessage(replyMessage, avatarIsRequired2);

                mainContactMessageLabel.getStyleClass().clear();
                mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
                mainContactMessageLabel.setText(replyMessageText);
                mainContactTimeLabel.setText(getCurrentTime());

                if (mainAnchorPane.lookup("#replyWrapper") != null) {
                    mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#replyWrapper"));
                }

                chatScrollPane.setPadding(new Insets(0,0,8,0));

                sendingMessageType = "text";
                repliedMessageId = -1;

                scrollToBottom();

                break;

            case "reply_with_picture":

                mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#replyWrapper"));
                chatScrollPane.setPadding(new Insets(0,0,8,0));

                int messageId2 = addMessageToDB(null,convertPictureIntoByte(pathToPicture),repliedMessageId,currentMessageFullDate,false);
                List<Object> message2 = ChatsDataBase.getMessageWithId(messageId2);

                loadMessage(message2,false);

                scrollToBottom();

                break;
                
            case "picture":

                if (pathToPicture.isEmpty()) return;

                int messageId1 = addMessageToDB(null,convertPictureIntoByte(pathToPicture),-1,currentMessageFullDate,false);

                List<List<Object>> allMessages1 = ChatsDataBase.getAllMessages(mainUserId,contactId);
                String previousMessageDate1 = isFirstMessage(allMessages1,messageId1) ? "0" : getMessageDate((String) allMessages1.get(getIndexWithMessageId(allMessages1,messageId1)-1).get(6));

                boolean isFirstMessage1 = isFirstMessage(allMessages1,messageId1);
                boolean isResponseMessage1 = isResponseMessage(allMessages1,messageId1);
                boolean isAfterOneHourMessage1 = isAfterOneHourMessage(allMessages1,messageId1);
                boolean isNewDayMessage1 = isNewDayMessage(getCurrentDate(),previousMessageDate1);
                boolean avatarIsRequired1 = isFirstMessage1 || isResponseMessage1 || isAfterOneHourMessage1 || isNewDayMessage1;

                List<Object> message1 = ChatsDataBase.getMessageWithId(messageId1);

                if (isNewDayMessage(getCurrentDate(),previousMessageDate1) && !isFirstMessage1) {
                    setChatDateLabel(getCurrentLongDate());
                }

                loadMessage(message1,avatarIsRequired1);
                mainContactMessageLabel.setText("Picture");
                mainContactMessageLabel.getStyleClass().clear();
                mainContactMessageLabel.setStyle("-fx-text-fill: white;");

                mainContactTimeLabel.setText(getCurrentTime());
                scrollToBottom();

                pathToPicture = "";

                sendingMessageType = "text";

                break;





        }


        chatTextField.setText("");
    }
    @FXML
    public void sendPicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files","*.png","*.jpg"));

        File selectedFile = fileChooser.showOpenDialog(mainAnchorPane.getScene().getWindow());

        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());

            double width = image.getWidth();
            double height = image.getHeight();


            showSendPictureWindow(selectedFile.getPath(),(int) width,(int) height);
        }
    }


    // time operations
    private String getCurrentTime() {
        // for instance: "13:45"
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return currentTime.format(formatter);
    }
    private String getCurrentFullDate() {
        // for instance: "08.12.2024 16:55"
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return currentTime.format(formatter);
    }
    private String getCurrentLongDate() {
        // for instance: "09. December"
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter longDateFormatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
        return longDateFormatter.format(currentDate);
    }
    private String getCurrentDate() {
        // for instance: "09.12.2024"
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return dateFormatter.format(currentDate);
    }
    private String getMessageHours(String messageTime) {
        // for instance: "06.12.2024 12:24" -> "12:24"
        String datePattern = "(\\d+):(\\d+)$";
        Pattern datePatternCompiled = Pattern.compile(datePattern);
        Matcher matcher = datePatternCompiled.matcher(messageTime);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
    private String getMessageDate(String messageTime) {
        // for instance: "06.12.2024 12:24" -> "06.12.2024"
        String datePattern = "^(\\d+)\\.(\\d+)\\.(\\d+)";
        Pattern datePatternCompiled = Pattern.compile(datePattern);
        Matcher matcher = datePatternCompiled.matcher(messageTime);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
    private String getMessageLongDate(String messageTime) {
        // for instance: "06.12.2024" -> "6. December"
        String datePattern = "^(\\d+)\\.(\\d+)\\.(\\d+)";
        Pattern datePatternCompiled = Pattern.compile(datePattern);
        Matcher matcher = datePatternCompiled.matcher(messageTime);
        if (matcher.find()) {
            DateTimeFormatter originalDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate parsedMessageDate = LocalDate.parse(matcher.group(),originalDateFormatter);
            DateTimeFormatter longDateFormatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
            return parsedMessageDate.format(longDateFormatter);
        }
        return "";
    }
    private boolean isDifferenceGreaterThanOneHour(String time1, String time2) {
        LocalTime t1 = LocalTime.parse(time1);
        LocalTime t2 = LocalTime.parse(time2);

        // Convert both times to total minutes since midnight
        int totalMinutesT1 = t1.getHour() * 60 + t1.getMinute();
        int totalMinutesT2 = t2.getHour() * 60 + t2.getMinute();

        // Calculate the difference in minutes
        int difference = totalMinutesT2 - totalMinutesT1;

        // If difference is negative, adjust by adding 1440 minutes (24 hours)
        if (difference < 0) {
            difference += 24 * 60; // 24 hours in minutes
        }

        return difference >= 60; // Check if the difference is greater than 60 minutes
    }
    private boolean isNewDayMessage(String date1,String date2) {
        return !date1.equals(date2);
    }



    // Scroll to bottom button
    private void scrollSmoothToBottom() {
        Platform.runLater(() -> {
            scrollSmooth(1.0);  // This ensures the ScrollPane scrolls to the bottom
        });
    }
    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);  // This ensures the ScrollPane scrolls to the bottom
        });
    }
    private void setBottomButtonListener() {
        chatScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            Pane scrollDownButton = (Pane) chatBackgroundPane.lookup("#scrollDownButton");
            if (newValue.doubleValue() < 1.0) {
                if (scrollDownButton == null) showScrollToBottomButton();
                else {
                    scrollDownButton.setLayoutY(getAppropriateBottomButtonPosition());
                }
            } else {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.2),scrollDownButton);
                fadeOut.setFromValue(1.0); // Start fully visible
                fadeOut.setToValue(0.0);   // End fully transparent
                fadeOut.play();
                fadeOut.setOnFinished(event -> chatBackgroundPane.getChildren().remove(scrollDownButton));
            }
        });
    }
    private void showScrollToBottomButton() {
        Pane scrollDownBackground = new Pane();
        scrollDownBackground.setId("scrollDownButton");
        scrollDownBackground.setPrefWidth(34);
        scrollDownBackground.setPrefHeight(36);
        scrollDownBackground.getStyleClass().add("chat-scroll-down-background");
        scrollDownBackground.setLayoutX(797);
        scrollDownBackground.setLayoutY(getAppropriateBottomButtonPosition());
        Label arrow = new Label();
        arrow.setLayoutX(8);
        arrow.setLayoutY(8);
        arrow.setPrefWidth(19);
        arrow.setPrefHeight(19);
        arrow.getStyleClass().add("chat-scroll-down-arrow");
        scrollDownBackground.getChildren().add(arrow);
        chatBackgroundPane.getChildren().add(scrollDownBackground);
        scrollDownBackground.setOnMouseEntered(event -> {
            arrow.getStyleClass().clear();
            arrow.getStyleClass().add("chat-scroll-down-arrow-focused");
        });
        scrollDownBackground.setOnMouseExited(event -> {
            arrow.getStyleClass().clear();
            arrow.getStyleClass().add("chat-scroll-down-arrow");
        });
        scrollDownBackground.setOnMouseClicked(event -> {
            scrollSmoothToBottom();
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.2),scrollDownBackground);
            fadeOut.setFromValue(1.0); // Start fully visible
            fadeOut.setToValue(0.0);   // End fully transparent
            fadeOut.play();
            fadeOut.setOnFinished(event1 -> chatBackgroundPane.getChildren().remove(scrollDownBackground));
        });
        setHoverCursorToHand(scrollDownBackground);
    }
    private int getAppropriateBottomButtonPosition() {
        return switch (sendingMessageType) {
            case "text" -> 560;
            case "edit" -> 520;
            case "reply_with_text" -> 520;
            case "reply_with_picture" -> 520;
            case "reply_with_picture_and_text" -> 520;
            case "picture" -> 560;
            case "picture_with_text" -> 560;
            default -> 560;
        };
    }



    // Message loading
    private String getTypeOfMessage(List<Object> message) {
        String messageText = (String) message.get(3);
        byte[] messagePicture = (byte[]) message.get(4);
        int messageReplyMessageId = (int) message.get(5);

        // calculating type of message:
        boolean typeIsText = (messageText != null) && (messagePicture == null) && (messageReplyMessageId == -1);
        boolean typeIsReplyWithText = (messageText != null) && (messagePicture == null) && (messageReplyMessageId != -1);
        boolean typeIsReplyWIthPicture = (messageText == null) && (messagePicture != null) && (messageReplyMessageId != -1);
        boolean typeIsReplyWithPictureAndText = (messageText != null) && (messagePicture != null) && (messageReplyMessageId != -1);
        boolean typeIsPicture = (messageText == null) && (messagePicture != null) && (messageReplyMessageId == -1);
        boolean typeIsPictureWithText = (messageText != null) && (messagePicture != null) && (messageReplyMessageId == -1);

        if (typeIsText) return "text";
        if (typeIsReplyWithText) return "reply_with_text";
        if (typeIsReplyWIthPicture) return "reply_with_picture";
        if (typeIsReplyWithPictureAndText) return "reply_with_picture_and_text";
        if (typeIsPicture) return "picture";
        if (typeIsPictureWithText) return "picture_with_text";
        return "none";
    }
    public int calculateLabelHeight(String text) {
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 13; -fx-font-family: 'System';");
        textLabel.setVisible(false);
        textLabel.setMaxWidth(292);
        textLabel.setWrapText(true);

        mainAnchorPane.getChildren().add(textLabel);
        mainAnchorPane.applyCss();
        mainAnchorPane.layout();

        int labelHeight = (int) textLabel.getHeight();

        mainAnchorPane.getChildren().remove(textLabel);

        return labelHeight;
    }
    public int calculatePictureHeight(byte[] picture) {
        Label pictureLabel = new Label();
        setPictureToLabel(picture,pictureLabel,337);

        pictureLabel.setVisible(false);
        mainAnchorPane.getChildren().add(pictureLabel);
        mainAnchorPane.applyCss();
        mainAnchorPane.layout();

        int pictureHeight = (int) pictureLabel.getHeight();

        mainAnchorPane.getChildren().remove(pictureLabel);
        return pictureHeight;
    }
    public int calculateLabelWidth(String text) {
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 13; -fx-font-family: 'System';");
        textLabel.setVisible(false);
        textLabel.setMaxWidth(292);
        textLabel.setWrapText(true);

        mainAnchorPane.getChildren().add(textLabel);
        mainAnchorPane.applyCss();
        mainAnchorPane.layout();

        int labelWidth = (int) textLabel.getWidth();

        mainAnchorPane.getChildren().remove(textLabel);

        return labelWidth;
    }
    private void setAppropriateAvatar(HBox messageHBox,Node message, int senderId,int messageId) throws SQLException {
        Label avatarLabel = new Label();
        avatarLabel.setId("avatarLabel"+messageId);
        sendAvatar(avatarLabel, senderId);
        if (senderId == mainUserId) {
            // For your messages, avatar on the right side
            messageHBox.getChildren().add(avatarLabel);
            HBox.setMargin(avatarLabel, new Insets(0, 25, 0, 0));
            HBox.setMargin(message, new Insets(0, 8, 0, 0));
        } else {
            // For contact messages, avatar on the left side
            messageHBox.getChildren().add(0,avatarLabel);
            HBox.setMargin(avatarLabel, new Insets(0, 0, 0, 25));
            HBox.setMargin(message, new Insets(0, 0, 0, 8));
        }
    }


    // small functions
    private void sendAvatar(Label avatar,int senderId) throws SQLException {
        byte[] blobBytes = UsersDataBase.getAvatarWithId(senderId);
        assert blobBytes != null;
        ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
        ImageView imageView = new ImageView(new Image(byteStream));
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        imageView.setSmooth(true);
        avatar.setGraphic(imageView);
        Circle clip = new Circle();
        clip.setLayoutX(15);
        clip.setLayoutY(15);
        clip.setRadius(15);
        avatar.setClip(clip);
    }
    private int addMessageToDB(String message,byte[] picture,int replyMessageId,String time,boolean received) throws SQLException {
        return ChatsDataBase.addMessage(mainUserId,contactId,message,picture,replyMessageId,time,received);
    }
    private int getIndexWithMessageId(List<List<Object>> allMessages,int messageId) {
        for (int i = 0;i < allMessages.size();i++) {
            if ((int) allMessages.get(i).get(0) == messageId) {
                return i;
            }
        }
        return -1;
    }
    private void selectRepliedMessageHBox(HBox hbox) {
        // Initial and target colors
        Color initialColor = Color.web("#161518");
        Color targetColor = Color.web("#333138");

        // Object property to store color for animation
        ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(initialColor);

        // Listener to update the background as the color changes
        colorProperty.addListener((observable, oldColor, newColor) -> {
            hbox.setBackground(new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, null)));
        });

        // Animation to the target color
        Timeline toTargetColor = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(colorProperty, initialColor)),
                new KeyFrame(Duration.seconds(0.2),
                        new KeyValue(colorProperty, targetColor))
        );

        // Animation back to the initial color
        Timeline toInitialColor = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(colorProperty, targetColor)),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(colorProperty, initialColor))
        );

        // Pause between animations
        PauseTransition pause = new PauseTransition(Duration.seconds(0.4));

        // Play animations in sequence with the pause
        toTargetColor.setOnFinished(event -> pause.play());
        pause.setOnFinished(event -> toInitialColor.play());

        toTargetColor.play();
    }
    private double getHBoxVvalue(HBox hbox) {
            // Calculate the vertical position of the targetHBox relative to the VBox
            double hBoxY = hbox.getLayoutY();

            // Calculate the total height of the VBox
            double vboxHeight = chatVBox.getHeight();

            // Get the height of the viewport
            double viewportHeight = chatScrollPane.getViewportBounds().getHeight();

            // Adjust the vvalue so that the HBox is in the middle of the viewport
            double vvalue = (hBoxY - viewportHeight / 2 + hbox.getHeight() / 2) /
                    (vboxHeight - viewportHeight);

            // Clamp vvalue between 0 and 1 to avoid out-of-bound errors
            vvalue = Math.max(0, Math.min(vvalue, 1));

            // Scroll to the adjusted position
            return vvalue;
    }
    private void scrollSmooth(double vvalue) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), // Start immediately
                        new KeyValue(chatScrollPane.vvalueProperty(), chatScrollPane.getVvalue())),
                new KeyFrame(Duration.seconds(0.4), // Smooth scroll duration
                        new KeyValue(chatScrollPane.vvalueProperty(), vvalue))
        );

        timeline.play();
    }
    private Point2D convertToTopLevelAnchorPaneCoordinates(Node node, double x, double y) {
        if (node == null) {
            return new Point2D(x, y);  // If no parent, return the current coordinates.
        }

        // If this node is an AnchorPane, return the coordinates directly
        if (Objects.equals(node.getId(),"#anchorPane")) {
            return node.localToParent(x, y); // Convert the coordinates relative to the AnchorPane
        }

        // Otherwise, recursively move up the parent hierarchy
        Point2D pointInParent = node.localToParent(x, y);

        // Continue traversing up the parent hierarchy
        return convertToTopLevelAnchorPaneCoordinates(node.getParent(), pointInParent.getX(), pointInParent.getY());
    }
    private void showMessageButtons(int x,int y,int messageId) {
        Pane messageButtonsOverlayPane = new Pane();
        messageButtonsOverlayPane.setLayoutX(0);
        messageButtonsOverlayPane.setLayoutY(0);
        messageButtonsOverlayPane.setPrefWidth(mainAnchorPane.getPrefWidth());
        messageButtonsOverlayPane.setPrefHeight(mainAnchorPane.getPrefHeight());

        Pane messageButtonsBackgroundPane = new Pane();
        messageButtonsBackgroundPane.getStyleClass().add("chat-message-buttons-background-pane");
        messageButtonsBackgroundPane.setPrefWidth(83);
        messageButtonsBackgroundPane.setPrefHeight(99);
        messageButtonsBackgroundPane.setLayoutX(x);
        int appropriateY = y > 500 ? y - 99 : y;
        messageButtonsBackgroundPane.setLayoutY(appropriateY);

        Pane replyButtonPane = new Pane();
        replyButtonPane.getStyleClass().add("chat-message-buttons-background-button-pane");
        replyButtonPane.setPrefWidth(74);
        replyButtonPane.setPrefHeight(28);
        replyButtonPane.setLayoutX(4);
        replyButtonPane.setLayoutY(4);

        Label replySymbol = new Label();
        replySymbol.getStyleClass().add("chat-message-buttons-reply-symbol");
        replySymbol.setPrefWidth(13);
        replySymbol.setPrefHeight(13);
        replySymbol.setLayoutX(7);
        replySymbol.setLayoutY(6);

        Label replyText = new Label("Reply");
        replyText.getStyleClass().add("chat-message-buttons-text");
        replyText.setLayoutX(30);
        replyText.setLayoutY(5);

        Pane editButtonPane = new Pane();
        editButtonPane.getStyleClass().add("chat-message-buttons-background-button-pane");
        editButtonPane.setPrefWidth(74);
        editButtonPane.setPrefHeight(28);
        editButtonPane.setLayoutX(4);
        editButtonPane.setLayoutY(35);

        Label editSymbol = new Label();
        editSymbol.getStyleClass().add("chat-message-buttons-edit-symbol");
        editSymbol.setPrefWidth(13);
        editSymbol.setPrefHeight(13);
        editSymbol.setLayoutX(7);
        editSymbol.setLayoutY(6);

        Label editText = new Label("Edit");
        editText.getStyleClass().add("chat-message-buttons-text");
        editText.setLayoutX(30);
        editText.setLayoutY(5);

        Pane deleteButtonPane = new Pane();
        deleteButtonPane.getStyleClass().add("chat-message-buttons-background-button-pane");
        deleteButtonPane.setPrefWidth(74);
        deleteButtonPane.setPrefHeight(28);
        deleteButtonPane.setLayoutX(4);
        deleteButtonPane.setLayoutY(66);

        Label deleteSymbol = new Label();
        deleteSymbol.getStyleClass().add("chat-message-buttons-delete-symbol");
        deleteSymbol.setPrefWidth(13);
        deleteSymbol.setPrefHeight(13);
        deleteSymbol.setLayoutX(7);
        deleteSymbol.setLayoutY(6);

        Label deleteText = new Label("Delete");
        deleteText.getStyleClass().add("chat-message-buttons-text");
        deleteText.setLayoutX(30);
        deleteText.setLayoutY(5);

        replyButtonPane.getChildren().addAll(replySymbol, replyText);
        editButtonPane.getChildren().addAll(editSymbol,editText);
        deleteButtonPane.getChildren().addAll(deleteSymbol,deleteText);
        messageButtonsBackgroundPane.getChildren().addAll(replyButtonPane,editButtonPane,deleteButtonPane);
        messageButtonsOverlayPane.getChildren().add(messageButtonsBackgroundPane);
        mainAnchorPane.getChildren().add(messageButtonsOverlayPane);


        messageButtonsOverlayPane.setOnMouseClicked(clickEvent -> {
            mainAnchorPane.getChildren().remove(messageButtonsOverlayPane);
        });

        replyButtonPane.setOnMouseClicked(clickEvent -> {
            try {
                showReplyWrapper(messageId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        editButtonPane.setOnMouseClicked(clickEvent -> {
            try {
                showEditWrapper(messageId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        deleteButtonPane.setOnMouseClicked(clickEvent -> {
            try {
                showDeleteMessageCaution(messageId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        setHoverCursorToHand(replyButtonPane,editButtonPane,deleteButtonPane);

        Platform.runLater(() -> {
            messageButtonsOverlayPane.getScene().getStylesheets().add(getClass().getResource("/main/css/MainChat.css").toExternalForm());
        });

    }
    private void showReplyMessageButton(int x,int y,int messageId) {
        Pane messageButtonsOverlayPane = new Pane();
        messageButtonsOverlayPane.setLayoutX(0);
        messageButtonsOverlayPane.setLayoutY(0);
        messageButtonsOverlayPane.setPrefWidth(mainAnchorPane.getPrefWidth());
        messageButtonsOverlayPane.setPrefHeight(mainAnchorPane.getPrefHeight());

        Pane messageButtonsBackgroundPane = new Pane();
        messageButtonsBackgroundPane.getStyleClass().add("chat-message-buttons-background-pane");
        messageButtonsBackgroundPane.setPrefWidth(83);
        messageButtonsBackgroundPane.setPrefHeight(36);
        messageButtonsBackgroundPane.setLayoutX(x);
        messageButtonsBackgroundPane.setLayoutY(y);

        Pane replyButtonPane = new Pane();
        replyButtonPane.getStyleClass().add("chat-message-buttons-background-button-pane");
        replyButtonPane.setPrefWidth(74);
        replyButtonPane.setPrefHeight(28);
        replyButtonPane.setLayoutX(4);
        replyButtonPane.setLayoutY(4);

        Label replySymbol = new Label();
        replySymbol.getStyleClass().add("chat-message-buttons-reply-symbol");
        replySymbol.setPrefWidth(13);
        replySymbol.setPrefHeight(13);
        replySymbol.setLayoutX(7);
        replySymbol.setLayoutY(6);

        Label replyText = new Label("Reply");
        replyText.getStyleClass().add("chat-message-buttons-text");
        replyText.setLayoutX(30);
        replyText.setLayoutY(5);

        replyButtonPane.getChildren().addAll(replySymbol, replyText);
        messageButtonsBackgroundPane.getChildren().add(replyButtonPane);
        messageButtonsOverlayPane.getChildren().add(messageButtonsBackgroundPane);
        mainAnchorPane.getChildren().add(messageButtonsOverlayPane);

        messageButtonsOverlayPane.setOnMouseClicked(clickEvent -> {
            mainAnchorPane.getChildren().remove(messageButtonsOverlayPane);
        });

        Platform.runLater(() -> {
            messageButtonsOverlayPane.getScene().getStylesheets().add(getClass().getResource("/main/css/MainChat.css").toExternalForm());
        });

        replyButtonPane.setOnMouseClicked(clickEvent -> {
            try {
                showReplyWrapper(messageId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void showReplyWrapper(int messageId) throws SQLException {
        if (mainAnchorPane.lookup("#replyWrapper") != null) {
            mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#replyWrapper"));
        } else if (mainAnchorPane.lookup("#editWrapper") != null) {
            mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#editWrapper"));
        }
        if (mainAnchorPane.lookup("#reply-wrapper") == null) {
            sendingMessageType = "reply_with_text";
            repliedMessageId = messageId;
            Pane scrollDownButton = (Pane) chatBackgroundPane.lookup("#scrollDownButton");
            if (scrollDownButton != null) scrollDownButton.setLayoutY(getAppropriateBottomButtonPosition());
            chatScrollPane.setPadding(new Insets(0,0,50,0));
            chatTextField.requestFocus();
            Pane replyWrapperPane = new Pane();
            replyWrapperPane.setId("replyWrapper");
            replyWrapperPane.getStyleClass().add("chat-wrapper");
            replyWrapperPane.setPrefWidth(846);
            replyWrapperPane.setPrefHeight(45);
            replyWrapperPane.setLayoutX(convertToTopLevelAnchorPaneCoordinates(chatTextField,chatTextField.getLayoutX(),chatTextField.getLayoutY()).getX());
            replyWrapperPane.setLayoutY(chatTextField.getScene().getHeight() - 94);

            Label replyWrapperExitLabel = new Label();
            replyWrapperExitLabel.getStyleClass().add("chat-wrapper-exit-label");
            replyWrapperExitLabel.setPrefWidth(18);
            replyWrapperExitLabel.setPrefHeight(18);
            replyWrapperExitLabel.setLayoutX(806);
            replyWrapperExitLabel.setLayoutY(14);

            Label replyWrapperRow = new Label();
            replyWrapperRow.setId("replyWrapperRow");
            replyWrapperRow.getStyleClass().add("chat-reply-wrapper-row-label");
            replyWrapperRow.setPrefWidth(26);
            replyWrapperRow.setPrefHeight(29);
            replyWrapperRow.setLayoutX(21);
            replyWrapperRow.setLayoutY(9);

            String messageUserName = UsersDataBase.getNameWithId((int)ChatsDataBase.getMessageWithId(messageId).get(1));
            Label replyWrapperName = new Label(messageUserName);
            replyWrapperName.setId("replyWrapperName");
            replyWrapperName.getStyleClass().add("chat-wrapper-name-label");
            replyWrapperName.setLayoutX(58);
            replyWrapperName.setLayoutY(7);

            String messageType = getTypeOfMessage(ChatsDataBase.getMessageWithId(messageId));

            if (messageType.equals("text") || messageType.equals("reply_with_text") || messageType.equals("reply_with_picture_and_text") || messageType.equals("picture_and_text")) {
                String message = (String) ChatsDataBase.getMessageWithId(messageId).get(3);
                Label replyWrapperMessageText = new Label(message);
                replyWrapperMessageText.setId("replyWrapperMessage");
                replyWrapperMessageText.getStyleClass().add("chat-wrapper-message-label");
                replyWrapperMessageText.setMaxWidth(720);
                replyWrapperMessageText.setLayoutX(58);
                replyWrapperMessageText.setLayoutY(24);

                replyWrapperPane.getChildren().add(replyWrapperMessageText);
            } else {
                Label photoSymbol = new Label();
                photoSymbol.getStyleClass().add("chat-photo-symbol-wrapper");
                photoSymbol.setPrefWidth(13);
                photoSymbol.setPrefHeight(13);
                photoSymbol.setLayoutX(58);
                photoSymbol.setLayoutY(25);

                Label replyWrapperMessageText = new Label("Picture");
                replyWrapperMessageText.setId("replyWrapperMessage");
                replyWrapperMessageText.getStyleClass().add("chat-wrapper-message-label");
                replyWrapperMessageText.setMaxWidth(720);
                replyWrapperMessageText.setLayoutX(75);
                replyWrapperMessageText.setLayoutY(24);

                replyWrapperPane.getChildren().addAll(photoSymbol,replyWrapperMessageText);
            }



            replyWrapperPane.getChildren().addAll(replyWrapperExitLabel,replyWrapperRow,replyWrapperName);
            mainAnchorPane.getChildren().add(replyWrapperPane);

            replyWrapperExitLabel.setOnMouseClicked(clickedEvent -> {
                mainAnchorPane.getChildren().remove(replyWrapperPane);
                chatScrollPane.setPadding(new Insets(0,0,8,0));
                sendingMessageType = "text";
                repliedMessageId = -1;
                Pane newScrollDownButton = (Pane) chatBackgroundPane.lookup("#scrollDownButton");
                if (newScrollDownButton != null) {
                    newScrollDownButton.setLayoutY(getAppropriateBottomButtonPosition());
                }
            });
            setHoverCursorToHand(replyWrapperExitLabel);
        }
    }
    private void showEditWrapper(int messageId) throws SQLException {
        if (mainAnchorPane.lookup("#editWrapper") != null) {
            mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#editWrapper"));
        } else  if (mainAnchorPane.lookup("#replyWrapper") != null) {
            mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#replyWrapper"));
        }
        if (mainAnchorPane.lookup("#editWrapper") == null) {
            sendingMessageType = "edit_with_text";
            editedMessageId = messageId;
            chatTextField.setText((String)ChatsDataBase.getMessageWithId(messageId).get(3));
            Pane scrollDownButton = (Pane) chatBackgroundPane.lookup("#scrollDownButton");
            if (scrollDownButton != null) scrollDownButton.setLayoutY(getAppropriateBottomButtonPosition());
            chatScrollPane.setPadding(new Insets(0,0,50,0));
            chatTextField.requestFocus();
            chatTextField.deselect();
            chatTextField.positionCaret(chatTextField.getText().length());

            Pane editWrapperPane = new Pane();
            editWrapperPane.setId("editWrapper");
            editWrapperPane.getStyleClass().add("chat-wrapper");
            editWrapperPane.setPrefWidth(846);
            editWrapperPane.setPrefHeight(45);
            editWrapperPane.setLayoutX(convertToTopLevelAnchorPaneCoordinates(chatTextField,chatTextField.getLayoutX(),chatTextField.getLayoutY()).getX());
            editWrapperPane.setLayoutY(chatTextField.getScene().getHeight() - 94);

            Label editWrapperExitLabel = new Label();
            editWrapperExitLabel.getStyleClass().add("chat-wrapper-exit-label");
            editWrapperExitLabel.setPrefWidth(18);
            editWrapperExitLabel.setPrefHeight(18);
            editWrapperExitLabel.setLayoutX(806);
            editWrapperExitLabel.setLayoutY(14);

            Label editWrapperSign = new Label();
            editWrapperSign.setId("editWrapperSign");
            editWrapperSign.getStyleClass().add("chat-wrapper-edit-label");
            editWrapperSign.setPrefWidth(26);
            editWrapperSign.setPrefHeight(29);
            editWrapperSign.setLayoutX(21);
            editWrapperSign.setLayoutY(13);

            String messageUserName = UsersDataBase.getNameWithId((int)ChatsDataBase.getMessageWithId(messageId).get(1));
            Label replyWrapperName = new Label(messageUserName);
            replyWrapperName.setId("editWrapperName");
            replyWrapperName.getStyleClass().add("chat-wrapper-name-label");
            replyWrapperName.setLayoutX(58);
            replyWrapperName.setLayoutY(7);

            String message = (String) ChatsDataBase.getMessageWithId(messageId).get(3);
            Label editWrapperMessageText = new Label(message);
            editWrapperMessageText.setId("editWrapperMessage");
            editWrapperMessageText.getStyleClass().add("chat-wrapper-message-label");
            editWrapperMessageText.setMaxWidth(720);
            editWrapperMessageText.setLayoutX(58);
            editWrapperMessageText.setLayoutY(24);

            editWrapperPane.getChildren().addAll(editWrapperExitLabel, editWrapperSign,replyWrapperName, editWrapperMessageText);
            mainAnchorPane.getChildren().add(editWrapperPane);

            editWrapperExitLabel.setOnMouseClicked(clickedEvent -> {
                chatTextField.setText("");
                mainAnchorPane.getChildren().remove(editWrapperPane);
                chatScrollPane.setPadding(new Insets(0,0,8,0));
                sendingMessageType = "text";
                Pane newScrollDownButton = (Pane) chatBackgroundPane.lookup("#scrollDownButton");
                if (newScrollDownButton != null) {
                    newScrollDownButton.setLayoutY(getAppropriateBottomButtonPosition());
                }
            });
            setHoverCursorToHand(editWrapperExitLabel);
        }
    }
    private void showDeleteMessageCaution(int messageId) throws SQLException {
        Pane deleteMessageOverlayPane = new Pane();
        deleteMessageOverlayPane.setStyle("-fx-background-color: #0000007C;");
        deleteMessageOverlayPane.setLayoutX(0);
        deleteMessageOverlayPane.setLayoutY(0);
        deleteMessageOverlayPane.setPrefWidth(mainAnchorPane.getPrefWidth());
        deleteMessageOverlayPane.setPrefHeight(mainAnchorPane.getPrefHeight());

        Pane deleteMessageBackgroundPane = new Pane();
        deleteMessageBackgroundPane.getStyleClass().add("chat-delete-message-background");
        deleteMessageBackgroundPane.setLayoutX(543);
        deleteMessageBackgroundPane.setLayoutY(286);
        deleteMessageBackgroundPane.setPrefWidth(246);
        deleteMessageBackgroundPane.setPrefHeight(102);

        Label deleteMessageLabel = new Label("You want to delete the message?");
        deleteMessageLabel.getStyleClass().add("chat-delete-message-text");
        deleteMessageLabel.setLayoutX(15);
        deleteMessageLabel.setLayoutY(12);

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("chat-delete-button");
        deleteButton.setLayoutX(177);
        deleteButton.setLayoutY(68);
        deleteButton.setPrefWidth(60);
        deleteButton.setPrefHeight(15);

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("chat-cancel-button");
        cancelButton.setLayoutX(110);
        cancelButton.setLayoutY(68);
        cancelButton.setPrefWidth(60);
        cancelButton.setPrefHeight(15);

        deleteMessageBackgroundPane.getChildren().addAll(deleteMessageLabel,deleteButton,cancelButton);
        deleteMessageOverlayPane.getChildren().add(deleteMessageBackgroundPane);
        mainAnchorPane.getChildren().add(deleteMessageOverlayPane);

        showOpeningEffect(deleteMessageOverlayPane,deleteMessageBackgroundPane);

        deleteMessageBackgroundPane.setOnMouseClicked(Event::consume);

        deleteMessageOverlayPane.setOnMouseClicked(clickEvent -> {
            mainAnchorPane.getChildren().remove(deleteMessageOverlayPane);
        });

        cancelButton.setOnMouseClicked(clickEvent -> {
            mainAnchorPane.getChildren().remove(deleteMessageOverlayPane);
        });

        setHoverCursorToHand(deleteButton,cancelButton);

        Platform.runLater(() -> {
            deleteMessageOverlayPane.getScene().getStylesheets().add(getClass().getResource("/main/css/MainChat.css").toExternalForm());
        });

        deleteButton.setOnMouseClicked(clickEvent -> {
            try {
                int messageChatIndex = getMessageIndexWithId(messageId);
                List<List<Object>> allMessages = ChatsDataBase.getAllMessages(mainUserId,contactId);

                boolean isTheOnlyOneMessage = ChatsDataBase.getAllMessages(mainUserId,contactId).size() == 1;
                boolean isTheLastMessageWithDateLabel = (ChatsDataBase.getLastMessageId(mainUserId,contactId) == messageId) && (chatVBox.getChildren().get(messageChatIndex-1) instanceof Label);
                boolean isBetweenDateLabels = !isTheOnlyOneMessage && !isTheLastMessageWithDateLabel &&
                        (chatVBox.getChildren().get(messageChatIndex-1) instanceof Label) && (chatVBox.getChildren().get(messageChatIndex+1) instanceof Label);
                boolean dateLabelRemoveRequired = isTheOnlyOneMessage || isTheLastMessageWithDateLabel || isBetweenDateLabels;


                if (dateLabelRemoveRequired) {
                    chatVBox.getChildren().remove(messageChatIndex-1);
                }

                if (isTheOnlyOneMessage) {
                    setChatCurrentDateLabel();
                    mainContactMessageLabel.setText("");
                    mainContactTimeLabel.setText("");
                } else if (ChatsDataBase.getLastMessageId(mainUserId,contactId) != messageId){
                    int previousMessageId = (int) allMessages.get(getIndexWithMessageId(allMessages,messageId)+1).get(0);
                    allMessages.remove(getIndexWithMessageId(allMessages,messageId));
                    boolean isFirstMessage = isFirstMessage(allMessages,previousMessageId);
                    boolean isResponseMessage = isResponseMessage(allMessages,previousMessageId);
                    boolean isAfterOneHourMessage = isAfterOneHourMessage(allMessages,previousMessageId);
                    boolean avatarRequired =  isFirstMessage || isResponseMessage || isAfterOneHourMessage;
                    boolean hasAlreadyAvatar = chatVBox.lookup("#avatarLabel"+previousMessageId) != null;

                    if (avatarRequired && !hasAlreadyAvatar) {
                        Label avatarLabel = new Label();
                        sendAvatar(avatarLabel,mainUserId);
                        HBox messageHBox = (HBox) chatVBox.lookup("#messageHBox"+previousMessageId);
                        Pane messagePane = (Pane) chatVBox.lookup("#messagePane"+previousMessageId);
                        Label pictureLabel = (Label) chatVBox.lookup("#messageLabel"+previousMessageId);
                        messageHBox.getChildren().add(avatarLabel);
                        HBox.setMargin(avatarLabel, new Insets(0, 25, 0, 0));
                        HBox.setMargin((messagePane == null ? pictureLabel : messagePane), new Insets(0, 8, 0, 0));
                    }
                } else {
                    int followedMessageId = (int) allMessages.get(getIndexWithMessageId(allMessages,messageId) - 1).get(0);
                    changeLastContactMessageAndTime(followedMessageId);
                }

                List<Integer> linkedByReplyMessageIds = ChatsDataBase.getRepliedMessageIds(mainUserId,contactId,messageId);
                if (!linkedByReplyMessageIds.isEmpty()) {
                    for (int id:linkedByReplyMessageIds) {
                        Pane replyPane = (Pane) chatVBox.lookup("#replyMessagePane"+id);
                        replyPane.getChildren().clear();

                        int replyWithTextDeletedMessageLeftMargin = 7;
                        int replyWithTextDeletedMessageTopMargin = 8;
                        Label deletedMessageLabel = new Label("(deleted)");
                        deletedMessageLabel.setAlignment(Pos.TOP_CENTER);
                        deletedMessageLabel.getStyleClass().add("chat-message-reply-deleted");
                        deletedMessageLabel.setMaxWidth(replyPane.getWidth() - (replyWithTextDeletedMessageLeftMargin * 2));
                        deletedMessageLabel.setLayoutX(replyWithTextDeletedMessageLeftMargin);
                        deletedMessageLabel.setLayoutY(replyWithTextDeletedMessageTopMargin);

                        replyPane.getChildren().add(deletedMessageLabel);
                    }
                }

                ChatsDataBase.deleteMessage(messageId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            chatVBox.getChildren().remove(mainAnchorPane.lookup("#messageHBox"+messageId));
            mainAnchorPane.getChildren().remove(deleteMessageOverlayPane);
        });
    }
    private void showOpeningEffect(Pane overlay,Pane background) {
        // Appearing time
        FadeTransition FadeIn = new FadeTransition(Duration.millis(180),overlay);
        FadeIn.setFromValue(0);
        FadeIn.setToValue(1);
        FadeIn.play();

        // Appearing move to left
        TranslateTransition translateIn = new TranslateTransition(Duration.millis(180), background);
        translateIn.setFromX(0);
        translateIn.setToX(-35);
        translateIn.play();
    }
    private int getPaddingTop(String messageType) {
        return switch (messageType) {
            case "text" -> 7;
            case "reply_with_text" -> 45;
            case "picture_with_picture_and_text" -> 7;
            case "picture_with_text" -> 7;
            default -> -1;
        };
    }
    private int getMessageIndexWithId(int messageId) {
        for (int i = 0;i < chatVBox.getChildren().size(); i++) {
            if (chatVBox.getChildren().get(i).getId() != null && chatVBox.getChildren().get(i).getId().contains(String.valueOf(messageId))) return i;
        }
        return -1;
    }
    private void changeLastContactMessageAndTime(int messageId) throws SQLException {
        List<Object> message = ChatsDataBase.getMessageWithId(messageId);
        String messageType = getTypeOfMessage(message);
        if (messageType.equals("text") || messageType.equals("edit_with_text") || messageType.equals("reply_with_text")) {
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText((String) message.get(3));
            mainContactTimeLabel.setText(getMessageHours((String) message.get(6)));
        } else {
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.setStyle("-fx-text-fill: white;");
            mainContactTimeLabel.setText(getMessageHours((String) message.get(6)));
        }
        // TODO
    }
    private void setPreviewImageToLabel(String imagePath, Label label, int width, int height) {
        // Load the original image
        Image originalImage = new Image("file:" + imagePath);

        // Calculate the crop area
        double cropWidth = (width < height) ? width : height;
        double cropHeight = (width < height) ? (width / 1.15) : (height / 1.15);
        double offsetX = (originalImage.getWidth() - cropWidth) / 2; // Center horizontally
        double offsetY = (originalImage.getHeight() - cropHeight) / 2; // Center vertically

        // Ensure the crop area is within bounds
        cropWidth = Math.min(cropWidth, originalImage.getWidth());
        cropHeight = Math.min(cropHeight, originalImage.getHeight());

        // Crop the image
        WritableImage croppedImage = new WritableImage(
                originalImage.getPixelReader(),
                (int) offsetX, (int) offsetY, (int) cropWidth, (int) cropHeight
        );

        // Resize the image to fit the label size (276x240)
        ImageView imageView = new ImageView(croppedImage);
        imageView.setFitWidth(276);
        imageView.setFitHeight(240);
        imageView.setPreserveRatio(true); // Maintain aspect ratio while resizing

        // Set the image view to the label
        label.setGraphic(imageView);

        // Apply rounded corners to the label
        label.setStyle(
                "-fx-background-color: transparent;" + // Set background color for visibility
                        "-fx-background-radius: 12;" +  // Apply rounded corners
                        "-fx-border-radius: 12;" +      // Ensure border matches the background radius
                        "-fx-border-color: transparent;" // Optional: Border color can be set or made transparent
        );

        // Clip the label to maintain rounded corners effect
        Rectangle clip = new Rectangle(label.getPrefWidth(), label.getPrefHeight());
        clip.setArcWidth(20); // Double the radius for a smoother effect
        clip.setArcHeight(20);
        label.setClip(clip);
    }
    private void showSendPictureWindow(String path,int width,int height) {
        Pane sendingPictureOverlayPane = new Pane();
        sendingPictureOverlayPane.setStyle("-fx-background-color: #0000007C;");
        sendingPictureOverlayPane.setLayoutX(0);
        sendingPictureOverlayPane.setLayoutY(0);
        sendingPictureOverlayPane.setPrefWidth(mainAnchorPane.getPrefWidth());
        sendingPictureOverlayPane.setPrefHeight(mainAnchorPane.getPrefHeight());

        Pane sendingPictureBackgroundPane = new Pane();
        sendingPictureBackgroundPane.setStyle("-fx-background-color: #1D1C20; -fx-background-radius: 10");
        sendingPictureBackgroundPane.setLayoutX(505);
        sendingPictureBackgroundPane.setLayoutY(150);
        sendingPictureBackgroundPane.setPrefWidth(300);
        sendingPictureBackgroundPane.setPrefHeight(348);

        Label pictureLabel = new Label();
        pictureLabel.setLayoutX(12);
        pictureLabel.setLayoutY(12);
        pictureLabel.setPrefWidth(276);
        pictureLabel.setPrefHeight(240);
        setPreviewImageToLabel(path,pictureLabel,width,height);

        Label editeLabel = new Label();
        editeLabel.getStyleClass().add("chat-send-picture-edite");
        editeLabel.setLayoutX(258);
        editeLabel.setLayoutY(17);
        editeLabel.setPrefWidth(26);
        editeLabel.setPrefHeight(26);

        Label sendPictureButton = new Label();
        sendPictureButton.getStyleClass().add("chat-send-picture");
        sendPictureButton.setLayoutX(230);
        sendPictureButton.setLayoutY(310);
        sendPictureButton.setPrefWidth(58);
        sendPictureButton.setPrefHeight(29);

        Label sendPictureCancelButton = new Label();
        sendPictureCancelButton.getStyleClass().add("chat-send-picture-cancel");
        sendPictureCancelButton.setLayoutX(145);
        sendPictureCancelButton.setLayoutY(310);
        sendPictureCancelButton.setPrefWidth(77);
        sendPictureCancelButton.setPrefHeight(29);

        TextField sendPictureTextField = new TextField();
        sendPictureTextField.setPromptText("Add a comment...");
        sendPictureTextField.getStyleClass().add("chat-send-picture-field");
        sendPictureTextField.setLayoutX(12);
        sendPictureTextField.setLayoutY(265);
        sendPictureTextField.setPrefWidth(276);
        sendPictureTextField.setPrefHeight(33);
        Platform.runLater(() -> {
            sendPictureTextField.requestFocus();
            if (!chatTextField.getText().isEmpty()) {
                sendPictureTextField.setText(chatTextField.getText().trim());
                sendPictureTextField.deselect();
                sendPictureTextField.positionCaret(chatTextField.getText().length());
                chatTextField.setText("");
            }
        });

        sendingPictureBackgroundPane.getChildren().addAll(pictureLabel,sendPictureButton,sendPictureCancelButton,sendPictureTextField,editeLabel);
        sendingPictureOverlayPane.getChildren().add(sendingPictureBackgroundPane);
        mainAnchorPane.getChildren().add(sendingPictureOverlayPane);
        setHoverCursorToHand(sendPictureCancelButton,sendPictureButton,editeLabel);

        sendPictureCancelButton.setOnMouseClicked(clickEvent -> {
            showFastCloseEffect(sendingPictureOverlayPane,sendingPictureBackgroundPane);
        });

        editeLabel.setOnMouseClicked(clickEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select an image");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files","*.png","*.jpg"));

            File selectedFile = fileChooser.showOpenDialog(mainAnchorPane.getScene().getWindow());

            if (selectedFile != null) {
                Image image = new Image(selectedFile.toURI().toString());

                double newWidth = image.getWidth();
                double newHeight = image.getHeight();

                setPreviewImageToLabel(selectedFile.getPath(),pictureLabel,(int)newWidth,(int)newHeight);
            }
        });

        sendPictureButton.setOnMouseClicked(clickEvent -> {
            switch (sendingMessageType) {
                case "text":
                    if (!sendPictureTextField.getText().trim().isEmpty()) {
                        sendingMessageType = "picture_with_text";
                    } else {
                        sendingMessageType = "picture";
                    }
                    break;
                case "reply_with_text":
                    if (!sendPictureTextField.getText().trim().isEmpty()) {
                        sendingMessageType = "reply_with_picture_and_text";
                    } else {
                        sendingMessageType = "reply_with_picture";
                    }
                    break;
                case "edit_with_text":
                    if (!sendPictureTextField.getText().trim().isEmpty()) {
                        sendingMessageType = "edit_with_picture_and_text";
                    } else {
                        sendingMessageType = "edit_with_picture";
                    }
                    break;
            }
            pathToPicture = path;
            mainAnchorPane.getChildren().remove(sendingPictureOverlayPane);
            try {
                sendMessage();
            } catch (SQLException | ExecutionException | InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        Platform.runLater(() -> {
            sendingPictureOverlayPane.getScene().getStylesheets().add(getClass().getResource("/main/css/MainChat.css").toExternalForm());
        });


    }
    private void showFastCloseEffect(Pane overlay,Pane background) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), background);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            mainAnchorPane.getChildren().remove(overlay);
        });
        fadeOut.play();
    }
    private void setHoverCursorToHand(Node... elements) {
        for (Node element : elements) {
            element.setOnMouseEntered(event -> element.setCursor(Cursor.HAND));
            element.setOnMouseExited(event -> element.setCursor(Cursor.DEFAULT));
        }
    }
    private byte[] convertPictureIntoByte(String path) throws IOException {
        Path pathObject = Paths.get(path);
        return Files.readAllBytes(pathObject);
    }
    private void setPictureToLabel(byte[] picture,Label pictureLabel,int pictureWidth) {
        ByteArrayInputStream originalPictureInputStream = new ByteArrayInputStream(picture);
        Image originalImage = new Image(originalPictureInputStream);

        double originalPictureWidth = originalImage.getWidth();
        double originalPictureHeight = originalImage.getHeight();
        double originalPictureRatio = originalPictureWidth / originalPictureHeight;

        int pictureHeight = (int) (pictureWidth / originalPictureRatio);
        pictureLabel.setPrefWidth(pictureWidth);
        pictureLabel.setPrefHeight(pictureHeight);


        ImageView imageView = new ImageView(originalImage);
        imageView.setFitWidth(pictureWidth);
        imageView.setFitHeight(pictureHeight);
        imageView.setPreserveRatio(true); // Maintain aspect ratio while resizing

        // Set the image view to the label
        pictureLabel.setGraphic(imageView);

        // Apply rounded corners to the label
        pictureLabel.setStyle(
                "-fx-background-color: transparent;" + // Set background color for visibility
                        "-fx-background-radius: 12;" +  // Apply rounded corners
                        "-fx-border-radius: 12;" +      // Ensure border matches the background radius
                        "-fx-border-color: transparent;" // Optional: Border color can be set or made transparent
        );

        // Clip the label to maintain rounded corners effect
        Rectangle clip = new Rectangle(pictureLabel.getPrefWidth(), pictureLabel.getPrefHeight());
        clip.setArcWidth(24); // Double the radius for a smoother effect
        clip.setArcHeight(24);
        pictureLabel.setClip(clip);
    }
}
