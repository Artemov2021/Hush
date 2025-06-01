package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatMessage extends MainChatController {
    public int id;
    public int sender_id;
    public int receiver_id;
    public String message_text;
    public byte[] picture;
    public int reply_message_id;
    public String time;
    public String type;
    public boolean received;


    public int previousMessageId;
    public int previousMessageSenderId;
    public int previousMessageReceiverId;
    public String previousMessageMessageText;
    public byte[] previousMessagePicture;
    public int previousMessageReplyMessageId;
    public String previousMessageTime;
    public String previousMessageType;
    public boolean previousMessageReceived;


    public int nextMessageId;
    public int nextMessageSenderId;
    public int nextMessageReceiverId;
    public String nextMessageMessageText;
    public byte[] nextMessagePicture;
    public int nextMessageReplyMessageId;
    public String nextMessageTime;
    public String nextMessageType;
    public boolean nextMessageReceived;

    private HBox messageHBox;
    private StackPane messageStackPane;
    private VBox messageVBox;
    private Label messagePictureLabel;
    private Label messageTextLabel;
    private StackPane messageReplyStackPane;
    private HBox messagePictureHBox;
    private Label messagePictureTimeLabel;
    private Label messageAvatarLabel;
    private Label previewPictureMessage;

    private MainChatController mainChatController;
    private List<ChatMessage> allMessages;


    public ChatMessage(ResultSet result) throws SQLException {
        this.id = result.getInt("message_id");
        this.sender_id = result.getInt("sender_id");
        this.receiver_id = result.getInt("receiver_id");
        this.message_text = result.getString("message");
        this.picture = result.getBytes("picture");
        this.reply_message_id = result.getInt("reply_message_id");
        Timestamp timestamp = result.getTimestamp("message_time");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        this.time = formatter.format(timestamp);
        this.type = result.getString("message_type");
        this.received = result.getBoolean("received");
    }
    private void injectChatElements(MainChatController mainChatController) {
        this.mainChatController = mainChatController;
        this.chatVBox = mainChatController.chatVBox;
        this.chatScrollPane = mainChatController.chatScrollPane;
        this.mainAnchorPane = mainChatController.mainAnchorPane;
        this.contactId = mainChatController.contactId;
        this.mainUserDataBaseAvatar = mainChatController.mainUserDataBaseAvatar;
        this.mainUserMessageAvatar = mainChatController.mainUserMessageAvatar;
        this.contactDataBaseAvatar = mainChatController.contactDataBaseAvatar;
        this.contactMessageAvatar = mainChatController.contactMessageAvatar;
    }
    public HBox load(MainChatController mainChatController,List<ChatMessage> allMessages) throws Exception {
        // no date label
        this.allMessages = allMessages;
        injectChatElements(mainChatController);
        setMessageHBox();
        return switch (type) {
            case "text" -> buildTextMessage();
            case "reply_with_text" -> buildReplyWithTextMessage();
            case "picture" -> buildPictureMessage();
            case "picture_with_text" -> buildPictureWithTextMessage();
            case "reply_with_picture" -> buildReplyWithPictureMessage();
            case "reply_with_picture_and_text" -> buildReplyWithPictureAndTextMessage();
            default -> throw new Exception();
        };
    }
    public HBox render(MainChatController mainChatController) throws Exception {
        injectChatElements(mainChatController);
        setPotentialDateLabel();
        setMessageHBox();
        return switch (type) {
            case "text" -> buildTextMessage();
            case "reply_with_text" -> buildReplyWithTextMessage();
            case "picture" -> buildPictureMessage();
            case "picture_with_text" -> buildPictureWithTextMessage();
            case "reply_with_picture" -> buildReplyWithPictureMessage();
            case "reply_with_picture_and_text" -> buildReplyWithPictureAndTextMessage();
            default -> throw new Exception();
        };
    }
    public void reload(MainChatController mainChatController) throws Exception {
        injectChatElements(mainChatController);
        messageHBox = (HBox) chatVBox.lookup("#messageHBox"+id);
        messageHBox.getChildren().clear();
        switch (type) {
            case "picture" -> buildPictureMessage();
            case "picture_with_text" -> buildPictureWithTextMessage();
            case "reply_with_picture" -> buildReplyWithPictureMessage();
            case "reply_with_picture_and_text" -> buildReplyWithPictureAndTextMessage();
            default -> throw new Exception();
        }
    }


    public void setPreviousMessageData(ChatMessage previousMessage) {
        boolean previousMessageExists = (previousMessage != null);
        this.previousMessageId = previousMessageExists ? previousMessage.id : -1;
        this.previousMessageSenderId = previousMessageExists ? previousMessage.sender_id : -1;
        this.previousMessageReceiverId = previousMessageExists ? previousMessage.receiver_id : -1;
        this.previousMessageMessageText = previousMessageExists ? previousMessage.message_text : null;
        this.previousMessagePicture = previousMessageExists ? previousMessage.picture : null;
        this.previousMessageReplyMessageId = previousMessageExists ? previousMessage.reply_message_id : -1;
        this.previousMessageTime = previousMessageExists ? previousMessage.time : null;
        this.previousMessageType = previousMessageExists ? previousMessage.type : null;
        this.previousMessageReceived = previousMessageExists && previousMessage.received;
    }
    public void setNextMessageData(ChatMessage nextMessage) {
        boolean nextMessageExists = (nextMessage != null);
        this.nextMessageId = nextMessageExists ? nextMessage.id : -1;
        this.nextMessageSenderId = nextMessageExists ? nextMessage.sender_id : -1;
        this.nextMessageReceiverId = nextMessageExists ? nextMessage.receiver_id : -1;
        this.nextMessageMessageText = nextMessageExists ? nextMessage.message_text : null;
        this.nextMessagePicture = nextMessageExists ? nextMessage.picture : null;
        this.nextMessageReplyMessageId = nextMessageExists ? nextMessage.reply_message_id : -1;
        this.nextMessageTime = nextMessageExists ? nextMessage.time : null;
        this.nextMessageType = nextMessageExists ? nextMessage.type : null;
        this.nextMessageReceived = nextMessageExists && nextMessage.received;
    }
    public void setPreviousMessageDataWithList(List<Object> previousMessage) throws SQLException {
        boolean previousMessageExists = (previousMessage != null);
        this.previousMessageId = previousMessageExists ? (int) previousMessage.get(0) : -1;
        this.previousMessageSenderId = previousMessageExists ? (int) previousMessage.get(1) : -1;
        this.previousMessageReceiverId = previousMessageExists ? (int) previousMessage.get(2) : -1;
        this.previousMessageMessageText = previousMessageExists ? (String) previousMessage.get(3) : null;
        this.previousMessagePicture = previousMessageExists ? (byte[]) previousMessage.get(4) : null;
        this.previousMessageReplyMessageId = previousMessageExists ? (int) previousMessage.get(5) : -1;
        this.previousMessageTime = previousMessageExists ? (String) previousMessage.get(6) : null;
        this.previousMessageType = previousMessageExists ? (String) previousMessage.get(7) : null;
        this.previousMessageReceived = previousMessageExists && (boolean) previousMessage.get(8);
    }
    public void setNextMessageDataWithList(List<Object> nextMessage) {
        boolean nextMessageExists = (!nextMessage.isEmpty());
        this.nextMessageId = nextMessageExists ? (int) nextMessage.get(0) : -1;
        this.nextMessageSenderId = nextMessageExists ? (int) nextMessage.get(1) : -1;
        this.nextMessageReceiverId = nextMessageExists ? (int) nextMessage.get(2) : -1;
        this.nextMessageMessageText = nextMessageExists ? (String) nextMessage.get(3) : null;
        this.nextMessagePicture = nextMessageExists ? (byte[]) nextMessage.get(4) : null;
        this.nextMessageReplyMessageId = nextMessageExists ? (int) nextMessage.get(5) : -1;
        this.nextMessageTime = nextMessageExists ? (String) nextMessage.get(6) : null;
        this.nextMessageType = nextMessageExists ? (String) nextMessage.get(7) : null;
        this.nextMessageReceived = nextMessageExists && (boolean) nextMessage.get(8);
    }

    // Message Building
    private HBox buildTextMessage() throws SQLException, ParseException {
        setMessageHBox();
        setMessageStackPane();
        setMessageTextLabel();
        setMessageTimeLabel();
        setMessageAvatar();
        return messageHBox;
    }
    private HBox buildReplyWithTextMessage() throws SQLException, ParseException, IOException {
        setMessageStackPane();
        setReplyStackPane();
        setMessageTextLabel();
        setMessageTimeLabel();
        setMessageAvatar();
        return messageHBox;
    }
    private HBox buildPictureMessage() throws IOException, SQLException, ParseException {
        setMessageStackPane();
        setMessageVBox();
        setMessagePictureHBox();
        setMessagePictureLabel();
        setMessagePictureTimeLabel();
        setMessageAvatar();
        return messageHBox;
    }
    private HBox buildPictureWithTextMessage() throws IOException, SQLException, ParseException {
        setMessageStackPane();
        setMessageTimeLabel();
        setMessageVBox();
        setMessagePictureHBox();
        setMessagePictureLabel();
        setMessageTextLabel();
        setMessageAvatar();
        return messageHBox;
    }
    private HBox buildReplyWithPictureMessage() throws SQLException, IOException, ParseException {
        setMessageStackPane();
        setMessageVBox();
        setReplyStackPane();
        setMessagePictureHBox();
        setMessagePictureLabel();
        setMessagePictureTimeLabel();
        setMessageAvatar();
        return messageHBox;
    }
    private HBox buildReplyWithPictureAndTextMessage() throws SQLException, IOException, ParseException {
        setMessageStackPane();
        setMessageTimeLabel();
        setMessageVBox();
        setReplyStackPane();
        setMessagePictureHBox();
        setMessagePictureLabel();
        setMessageTextLabel();
        setMessageAvatar();
        return messageHBox;
    }


    // Message Building: Elements
    private void setMessageHBox() {
        byte minHeight = 40;
        Pos messagePosition = (mainUserId == sender_id) ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT;
        messageHBox = new HBox();
        messageHBox.setMinHeight(minHeight);
        messageHBox.setAlignment(messagePosition);
        messageHBox.setId("messageHBox"+id);
    }
    private void setMessageStackPane() {
        short maxWidth = 408;
        String style = (mainUserId == sender_id) ? "chat-message-user-stackpane" : "chat-message-contact-stackpane";
        messageStackPane = new StackPane();
        HBox.setMargin(messageStackPane,(mainUserId == sender_id) ? new Insets(0,13, 0,0) : new Insets(0,0,0,13));
        messageStackPane.setId("messageStackPane"+id);
        messageStackPane.setMaxWidth(maxWidth);
        messageStackPane.getStyleClass().add(style);
        messageStackPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.SECONDARY) {
                showAppropriateMessageButton(clickEvent);
            }
        });
        messageHBox.getChildren().add(messageStackPane);
    }
    private void setMessageVBox() {
        messageVBox = new VBox();
        messageVBox.setMouseTransparent(false);
        messageStackPane.getChildren().add(messageVBox);
    }
    private void setMessageTextLabel() {
        Insets padding = getTextLabelPadding();
        messageTextLabel = new Label(message_text);
        StackPane.setAlignment(messageTextLabel,Pos.BOTTOM_LEFT);
        messageTextLabel.setId("messageTextLabel"+id);
        messageTextLabel.setWrapText(true);
        messageTextLabel.getStyleClass().add("chat-message-text-label");
        if (type.equals("picture_with_text") || type.equals("reply_with_picture_and_text")) {
            VBox.setMargin(messageTextLabel,padding);
            messageVBox.getChildren().add(messageTextLabel);
        } else {
            StackPane.setMargin(messageTextLabel,padding);
            messageStackPane.getChildren().add(messageTextLabel);
        }
    }
    private void setMessageTimeLabel() {
        String messageTime = getMessageTime(time);
        Label timeLabel = new Label(messageTime);
        timeLabel.getStyleClass().add("chat-time-label");
        StackPane.setMargin(timeLabel,new Insets(0,10,4,0));
        StackPane.setAlignment(timeLabel,Pos.BOTTOM_RIGHT);
        messageStackPane.getChildren().add(timeLabel);
    }
    private void setMessagePictureHBox() {
        messagePictureHBox = new HBox();
        messagePictureHBox.setAlignment(Pos.TOP_CENTER);
        messageVBox.getChildren().add(messagePictureHBox);
        messagePictureHBox.setMouseTransparent(false);
    }
    private void setReplyStackPane() throws SQLException, IOException {
        short minWidth = 80;
        short prefHeight = 37;
        short maxHeight = 37;
        ChatMessage repliedMessage = getRepliedMessage();
        boolean repliedMessageExists = (repliedMessage != null);
        String messageReplyPaneStyle = (mainUserId == sender_id) ? "chat-message-user-reply-pane" : "chat-message-contact-reply-pane";
        boolean isRepliedMessagePicture = repliedMessageExists && (repliedMessage.picture != null);

        messageReplyStackPane = new StackPane();
        messageReplyStackPane.setId("messageReplyStackPane"+id);
        messageReplyStackPane.setCursor(repliedMessageExists ? Cursor.HAND : Cursor.DEFAULT);
        messageReplyStackPane.setMinWidth(minWidth);
        messageReplyStackPane.setPrefHeight(prefHeight);
        messageReplyStackPane.setMaxHeight(maxHeight);
        messageReplyStackPane.getStyleClass().add(messageReplyPaneStyle);
        StackPane.setAlignment(messageReplyStackPane,Pos.TOP_LEFT);

        if (type.equals("reply_with_picture") || type.equals("reply_with_picture_and_text")) {
            VBox.setMargin(messageReplyStackPane,new Insets(7,7,0,7));
            messageVBox.getChildren().add(messageReplyStackPane);
        } else {
            StackPane.setMargin(messageReplyStackPane,new Insets(7,7,0,7));
            messageStackPane.getChildren().add(messageReplyStackPane);
        }

        if (!repliedMessageExists) {
            Label repliedMessageDeletedMessage = new Label("(deleted message)");
            repliedMessageDeletedMessage.getStyleClass().add((mainUserId == sender_id) ? "chat-message-user-deleted-message" : "chat-message-contact-deleted-message");
            StackPane.setAlignment(repliedMessageDeletedMessage,Pos.TOP_LEFT);
            StackPane.setMargin(repliedMessageDeletedMessage,new Insets(10,8,5,8));
            messageReplyStackPane.getChildren().add(repliedMessageDeletedMessage);
        } else if (isRepliedMessagePicture) {
            // -------------------------------------------------------
            // replied message picture label
            short maxReplyPictureWidth = 50;
            short maxReplyPictureHeight = 24;
            short minHeight = 24;

            Label replyPictureLabel = new Label();
            StackPane.setAlignment(replyPictureLabel,Pos.TOP_LEFT);
            StackPane.setMargin(replyPictureLabel,new Insets(7,0,0,7));

            File tempFile = File.createTempFile("tempImage", ".png");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] repliedMessagePicture = repliedMessage.picture;
                fos.write(repliedMessagePicture);
            }

            Image image = new Image(tempFile.toURI().toString(), true);
            ImageView imageView = new ImageView(image);
            imageView.setSmooth(true);
            imageView.setCache(true);
            imageView.setPreserveRatio(true); // Keep aspect ratio

            StackPane imageContainer = new StackPane(imageView);
            replyPictureLabel.setGraphic(imageContainer);
            messageReplyStackPane.getChildren().add(replyPictureLabel);

            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0) {
                    try {
                        double originalWidth = image.getWidth();
                        double originalHeight = image.getHeight();

                        // Scaling to fit max width & max height
                        double scaleX = maxReplyPictureWidth / originalWidth;
                        double scaleY = maxReplyPictureHeight / originalHeight;
                        double scale = Math.min(1.0, Math.min(scaleX, scaleY)); // Don't scale up

                        double scaledWidth = originalWidth * scale;
                        double scaledHeight = originalHeight * scale;

                        // Enforce minimum height of 40px
                        if (scaledHeight < minHeight) {
                            double minHeightScale = minHeight / originalHeight;
                            scaledHeight = minHeight;
                            scaledWidth = originalWidth * minHeightScale;
                            // Optional: If that causes width > maxAllowedWidth, clamp it again
                            if (scaledWidth > maxReplyPictureWidth) {
                                scaledWidth = maxReplyPictureWidth;
                                scaledHeight = originalHeight * (maxReplyPictureWidth / originalWidth);
                            }
                        }

                        imageView.setFitWidth(scaledWidth); // Height handled via preserveRatio

                        imageContainer.setPrefWidth(scaledWidth);
                        imageContainer.setPrefHeight(scaledHeight);

                        Rectangle clip = new Rectangle(scaledWidth, scaledHeight);
                        clip.setArcWidth(8);
                        clip.setArcHeight(8);
                        imageContainer.setClip(clip);

                        String repliedMessageName = UsersDataBase.getNameWithId(repliedMessage.sender_id);
                        Label messageReplyNameLabel = new Label(repliedMessageName);
                        messageReplyNameLabel.getStyleClass().add("chat-message-reply-name");
                        messageReplyNameLabel.setMouseTransparent(true);
                        StackPane.setAlignment(messageReplyNameLabel,Pos.TOP_LEFT);
                        StackPane.setMargin(messageReplyNameLabel,new Insets(4,8,0,scaledWidth + 16));
                        messageReplyStackPane.getChildren().add(messageReplyNameLabel);

                        Label messageReplyMessagePhotoSymbol = new Label();
                        messageReplyMessagePhotoSymbol.setPrefWidth(11);
                        messageReplyMessagePhotoSymbol.setPrefHeight(11);
                        messageReplyMessagePhotoSymbol.getStyleClass().add((mainUserId == sender_id) ? "chat-message-user-reply-photo-symbol" : "chat-message-contact-reply-photo-symbol");
                        messageReplyMessagePhotoSymbol.setMouseTransparent(true);
                        StackPane.setAlignment(messageReplyMessagePhotoSymbol,Pos.TOP_LEFT);
                        StackPane.setMargin(messageReplyMessagePhotoSymbol,new Insets(20,8,0,scaledWidth + 16));
                        messageReplyStackPane.getChildren().add(messageReplyMessagePhotoSymbol);

                        Label messageReplyMessagePhotoTitle = new Label("Photo");
                        messageReplyMessagePhotoTitle.getStyleClass().add((mainUserId == sender_id) ? "chat-message-user-reply-message" : "chat-message-contact-reply-message");
                        messageReplyMessagePhotoTitle.setMouseTransparent(true);
                        StackPane.setAlignment(messageReplyMessagePhotoTitle,Pos.TOP_LEFT);
                        StackPane.setMargin(messageReplyMessagePhotoTitle,new Insets(18,8,0,scaledWidth + 30));
                        messageReplyStackPane.getChildren().add(messageReplyMessagePhotoTitle);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }


                }
            });
            // -------------------------------------------------------


        } else {
            String repliedMessageName = UsersDataBase.getNameWithId(repliedMessage.sender_id);
            Label messageReplyNameLabel = new Label(repliedMessageName);
            messageReplyNameLabel.getStyleClass().add("chat-message-reply-name");
            messageReplyNameLabel.setMouseTransparent(true);
            StackPane.setAlignment(messageReplyNameLabel,Pos.TOP_LEFT);
            StackPane.setMargin(messageReplyNameLabel,new Insets(4,8,0,8));
            messageReplyStackPane.getChildren().add(messageReplyNameLabel);

            String repliedMessageText = repliedMessage.message_text;
            Label messageReplyMessageLabel = new Label(repliedMessageText);
            messageReplyMessageLabel.getStyleClass().add((mainUserId == sender_id) ? "chat-message-user-reply-message" : "chat-message-contact-reply-message");
            messageReplyMessageLabel.setMouseTransparent(true);
            StackPane.setAlignment(messageReplyMessageLabel,Pos.TOP_LEFT);
            StackPane.setMargin(messageReplyMessageLabel,new Insets(18,8,0,8));
            messageReplyStackPane.getChildren().add(messageReplyMessageLabel);
        }

        if (repliedMessageExists) {
            messageReplyStackPane.setOnMouseClicked(repliedClickEvent -> {
                if (repliedClickEvent.getButton() == MouseButton.PRIMARY) {
                    HBox replyHBoxMessage = (HBox) chatVBox.lookup("#messageHBox"+reply_message_id);
                    double replyHBoxPosition = getCenteredScrollPosition(replyHBoxMessage);
                    smoothScrollTo(replyHBoxPosition,0.4);
                    fadeOutBackgroundColor(replyHBoxMessage);
                }
            });
        }
    }
    private void setMessagePictureLabel() throws IOException {
        short maxWidth = 408;
        short maxHeight = 440;
        byte minHeight = 40;

        messagePictureLabel = new Label();
        messagePictureLabel.setCursor(Cursor.HAND);
        messagePictureLabel.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                showFullyMessagePicture();
            }
        });
        File tempFile = File.createTempFile("tempImage", ".png");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(picture);
        }

        Image image = new Image(tempFile.toURI().toString(), false); // sync load
        ImageView imageView = new ImageView(image);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.setPreserveRatio(true);

        // 🔧 Scale immediately since image is loaded
        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();

        // Scaling to fit max dimensions
        double scaleX = maxWidth / originalWidth;
        double scaleY = maxHeight / originalHeight;
        double scale = Math.min(1.0, Math.min(scaleX, scaleY)); // Don't scale up

        double scaledWidth = originalWidth * scale;
        double scaledHeight = originalHeight * scale;

        // Enforce minimum height
        if (scaledHeight < minHeight) {
            double minHeightScale = minHeight / originalHeight;
            scaledHeight = minHeight;
            scaledWidth = originalWidth * minHeightScale;
            if (scaledWidth > maxWidth) {
                scaledWidth = maxWidth;
                scaledHeight = originalHeight * (maxWidth / originalWidth);
            }
        }

        imageView.setFitWidth(scaledWidth); // preserveRatio will set height
        imageView.setPreserveRatio(true);

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setPrefSize(scaledWidth, scaledHeight);

        messagePictureLabel.setGraphic(imageContainer);
        messagePictureHBox.getChildren().add(messagePictureLabel);

        int finalScaledWidth = (int) Math.round(scaledWidth);
        int finalScaledHeight = (int) Math.round(scaledHeight);
        messageStackPane.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (type.equals("picture_with_text") && finalScaledWidth >= messageStackPane.getWidth()) {
                SVGPath svgClip = new SVGPath();
                svgClip.setContent(
                        "M0,11 " +                              // Move down from top-left
                                "Q0,0 11,0 " +                          // Top-left corner curve
                                "H" + (finalScaledWidth - 11) + " " +        // Line to before top-right curve
                                "Q" + finalScaledWidth + ",0 " + finalScaledWidth + ",11 " + // Top-right corner curve
                                "V" + finalScaledHeight + " " +              // Line down right side
                                "H0 Z"                                  // Line to left and close path
                );
                imageContainer.setClip(svgClip);
            } else if (type.equals("picture_with_text") && finalScaledWidth < messageStackPane.getWidth()) {
                messagePictureHBox.setPadding(new Insets(10,0,0,0));
            } else if (type.equals("picture")) {
                Rectangle clip = new Rectangle(finalScaledWidth, finalScaledHeight);
                clip.setArcWidth(22);
                clip.setArcHeight(22);
                imageContainer.setClip(clip);
            } else if (type.equals("reply_with_picture") || type.equals("reply_with_picture_and_text")) {
                messagePictureHBox.setPadding(new Insets(9,9,9,9));
                Rectangle clip = new Rectangle(finalScaledWidth, finalScaledHeight);
                clip.setArcWidth(14);
                clip.setArcHeight(14);
                imageContainer.setClip(clip);
            }
        });
    }
    private void setMessagePictureTimeLabel() {
        messagePictureTimeLabel = new Label(getMessageTime(time));
        messagePictureTimeLabel.setVisible(false);
        messagePictureTimeLabel.getStyleClass().add("chat-picture-message-time-label");
        HBox.setMargin(messagePictureTimeLabel,(mainUserId == sender_id) ? new Insets(0,10,0,0) : new Insets(0,0,0,10));
        int messageHBoxIndex = (mainUserId == sender_id) ? 0 : messageHBox.getChildren().size();
        messageHBox.getChildren().add(messageHBoxIndex,messagePictureTimeLabel);

        byte moveDistance = 17;
        messagePictureTimeLabel.setOpacity(0);
        messagePictureTimeLabel.setTranslateX((mainUserId == sender_id) ? moveDistance : -moveDistance); // Start slightly off-screen (right side)

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), messagePictureTimeLabel);
        slideIn.setFromX((mainUserId == sender_id) ? moveDistance : -moveDistance);
        slideIn.setToX(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), messagePictureTimeLabel);
        slideOut.setFromX(0);
        slideOut.setToX((mainUserId == sender_id) ? moveDistance : -moveDistance);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), messagePictureTimeLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), messagePictureTimeLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        messageStackPane.setOnMouseEntered(e -> {
            messagePictureTimeLabel.setVisible(true);
            slideIn.playFromStart();
            fadeIn.playFromStart();
        });

        messageStackPane.setOnMouseExited(e -> {
            slideOut.playFromStart();
            fadeOut.playFromStart();
            fadeOut.setOnFinished(ev -> messagePictureTimeLabel.setVisible(false));
        });
    }
    private void setMessageAvatar() throws SQLException, ParseException {
        if (isAvatarRequired()) {
            messageAvatarLabel = new Label();
            messageAvatarLabel.setId("messageAvatarLabel" + id);
            setAvatarLabel();
            int messageHBoxIndex = (mainUserId == sender_id) ? messageHBox.getChildren().size() : 0;
            messageHBox.getChildren().add(messageHBoxIndex, messageAvatarLabel);

            HBox.setMargin(messageAvatarLabel,(mainUserId == sender_id) ? new Insets(0,115, 0, 0) : new Insets(0,0,0,105));
        } else {
            HBox.setMargin(messageStackPane,(mainUserId == sender_id) ? new Insets(0, 168, 0, 0) : new Insets(0,0,0,158));
        }

        if (shouldRemovePreviousAvatar()) {
            removePreviousAvatar();
        }
    }


    // Small Functions
    private void setPotentialDateLabel() throws SQLException, ParseException {
        boolean isFirstMessage = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> node.getId())
                .noneMatch(id -> id != null && id.startsWith("messageHBox"));
        boolean isPreviousMessageOneDay = !isFirstMessage && messagesHaveOneDayDifference(ChatsDataBase.getMessage(mainUserId,contactId,previousMessageId).time,time);

        if (isFirstMessage || isPreviousMessageOneDay) {

            String labelDate = getDateForDateLabel(time);
            setChatDateLabel(labelDate);

            }
    }
    private void setChatDateLabel(String date) {
        Label chatDateLabel = new Label(date);
        chatDateLabel.setId("dateLabel"+getLabelIdCurrentDate());
        chatDateLabel.getStyleClass().add("chat-date-label");
        VBox.setMargin(chatDateLabel,new Insets(8,0,8,0));
        chatVBox.getChildren().add(chatDateLabel);
    }
    private String getLabelIdCurrentDate() {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(time, inputFormatter);

        return dateTime.toLocalDate().toString(); // Outputs in yyyy-MM-dd format
    }
    private String getDateForDateLabel(String fullDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.GERMAN);

        // Parse input date
        LocalDateTime dateTime = LocalDateTime.parse(fullDate, inputFormatter);
        LocalDate date = dateTime.toLocalDate(); // Extract only date

        // Get current year
        int currentYear = LocalDate.now().getYear();

        // Format output
        String formattedDate = date.format(outputFormatter);
        if (date.getYear() != currentYear) {
            formattedDate += " " + date.getYear();
        }

        return formattedDate;
    }
    private ChatMessage getRepliedMessage() throws SQLException {
        if (allMessages == null) {
            return ChatsDataBase.getMessage(mainUserId,contactId,reply_message_id);
        } else {
            for (ChatMessage message: allMessages) {
                if (message.id == reply_message_id) {
                    return message;
                }
            }
        }
        return null;
    }
    private Insets getTextLabelPadding() {
        return switch (type) {
            case "text" -> new Insets(5,50,9,13);
            case "reply_with_text" -> new Insets(48,50,7,12);
            case "picture_with_text" -> new Insets (8,50,7,13);
            case "reply_with_picture_and_text" -> new Insets(0,50,7,13);
            default -> null;
        };
    }
    private boolean isAvatarRequired() throws SQLException, ParseException {
        int lastMessageId = (allMessages == null) ? ChatsDataBase.getLastMessageId(mainUserId,contactId) : allMessages.getLast().id;
        boolean nextMessageExists = nextMessageId != -1;
        int nextMessageSender = nextMessageExists ? nextMessageSenderId : -1;

        boolean isLastMessage = (lastMessageId == id);
        boolean nextMessageIsForeign = nextMessageExists && (nextMessageSender != sender_id);
        boolean nextMessageIsOneDay = nextMessageExists && messagesHaveOneDayDifference(time,nextMessageTime);
        return isLastMessage || nextMessageIsForeign || nextMessageIsOneDay;
    }
    private static String getMessageTime(String fullDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(fullDate, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(outputFormatter);
    }
    private boolean messagesHaveOneDayDifference(String previousMessageFullDate, String currentMessageFullDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd"); // Only extracts the date

        Date date1 = dateFormat.parse(previousMessageFullDate);
        Date date2 = dateFormat.parse(currentMessageFullDate);

        // Extract only the day part (YYYY-MM-DD)
        String day1 = dayFormat.format(date1);
        String day2 = dayFormat.format(date2);

        return !day1.equals(day2); // True if the dates are different
    }
    private void showAppropriateMessageButton(MouseEvent clickEvent) {
        int x = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getX();
        int y = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getY();

        MessageButtons messageButtons = new MessageButtons(mainChatController);
        if (mainUserId == sender_id) {
            messageButtons.showMessageButtons(x, y,id);
        } else {
            messageButtons.showMessageReplyButton(x, y,id);
        }
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
    private void setAvatarLabel() throws SQLException {
        boolean isAvatarOfUser = (sender_id == mainUserId);
        boolean hasUserAvatar = (mainUserDataBaseAvatar != null);
        boolean isUserAvatarAlreadyLoaded = (mainUserMessageAvatar != null);

        boolean isAvatarOfContact = (sender_id == contactId);
        boolean hasContactAvatar = (contactDataBaseAvatar != null);
        boolean isContactAvatarAlreadyLoaded = (contactMessageAvatar != null);

        if ((isAvatarOfUser && !hasUserAvatar) || (isAvatarOfContact && !hasContactAvatar)) {

            messageAvatarLabel.getStyleClass().clear();
            messageAvatarLabel.getStyleClass().add("chat-message-default-avatar");
            messageAvatarLabel.setPrefHeight(40);
            messageAvatarLabel.setPrefWidth(40);
        } else if ((isAvatarOfUser && hasUserAvatar && !isUserAvatarAlreadyLoaded) || (isAvatarOfContact && hasContactAvatar && !isContactAvatarAlreadyLoaded)) {
            byte[] avatar = UsersDataBase.getAvatarWithId(sender_id);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(avatar);
            mainUserMessageAvatar = new ImageView(new Image(byteStream));
            mainUserMessageAvatar.setFitHeight(40);
            mainUserMessageAvatar.setFitWidth(40);
            mainUserMessageAvatar.setSmooth(true);
            messageAvatarLabel.setGraphic(mainUserMessageAvatar);
            Circle clip = new Circle();
            clip.setLayoutX(20);
            clip.setLayoutY(20);
            clip.setRadius(20);
            messageAvatarLabel.setClip(clip);
        } else if ((isAvatarOfUser && hasUserAvatar && isUserAvatarAlreadyLoaded) || (isAvatarOfContact && hasContactAvatar && isContactAvatarAlreadyLoaded)) {
            messageAvatarLabel.setGraphic(mainUserMessageAvatar);
            Circle clip = new Circle();
            clip.setLayoutX(20);
            clip.setLayoutY(20);
            clip.setRadius(20);
            messageAvatarLabel.setClip(clip);
        }
    }
    private boolean shouldRemovePreviousAvatar() throws SQLException, ParseException {
        boolean previousMessageExists = previousMessageId != -1;
        boolean isChatEmpty = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .noneMatch(hbox -> {
                    String id = hbox.getId();
                    return id != null && id.startsWith("messageHBox");
                });
        boolean isChatLoading = allMessages != null;
        boolean isFirstMessage = (allMessages == null) ? (ChatsDataBase.getFirstMessageId(mainUserId,contactId) == id) : (allMessages.get(0).id == id);
        boolean isPreviousMessageForeign = previousMessageExists && (sender_id != previousMessageSenderId);
        boolean isPreviousMessageOneDay =  previousMessageExists && messagesHaveOneDayDifference(previousMessageTime,time);

        return !isChatEmpty && !isChatLoading && !isFirstMessage && !isPreviousMessageForeign && !isPreviousMessageOneDay;
    }
    private void removePreviousAvatar() {
        HBox previousMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+previousMessageId);
        StackPane previousMessageStackPane = (StackPane) previousMessageHBox.lookup("#messageStackPane"+previousMessageId);
        Label previousMessageAvatar = (Label) previousMessageHBox.lookup("#messageAvatarLabel"+previousMessageId);

        previousMessageHBox.getChildren().remove(previousMessageAvatar);
        HBox.setMargin(previousMessageStackPane,(mainUserId == sender_id) ? new Insets(0, 168, 0, 0) : new Insets(0,0,0,158));
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
    private void showFullyMessagePicture() {
        Pane backgroundPane = new Pane();
        backgroundPane.setPrefWidth(1920);
        backgroundPane.setPrefHeight(1009);
        backgroundPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.78)");

        // Apply a fade-in transition to make the background appear smoothly
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), backgroundPane);
        fadeTransition.setFromValue(0.0); // Start from fully transparent
        fadeTransition.setToValue(1.0); // Fade to fully opaque
        fadeTransition.play();

        mainAnchorPane.getChildren().add(backgroundPane);

        backgroundPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                mainAnchorPane.getChildren().remove(backgroundPane);
            }
        });

        Label fullyPicturePreview = new Label();
        fullyPicturePreview.setOnMouseClicked(Event::consume);
        assert picture != null;

        ByteArrayInputStream byteStream = new ByteArrayInputStream(picture);
        Image image = new Image(byteStream);
        ImageView imageView = new ImageView(image);

        // Set max size while preserving aspect ratio
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(Math.min(image.getWidth(), 1520));
        imageView.setFitHeight(Math.min(image.getHeight(), 609));

        imageView.setSmooth(true);
        fullyPicturePreview.setGraphic(imageView);

        StackPane messageFullPicturePane = new StackPane();

        // Ensure the layout is updated before centering
        Platform.runLater(() -> {
            // Recalculate layout position after image rendering
            double initialWidth = imageView.getLayoutBounds().getWidth();
            double initialHeight = imageView.getLayoutBounds().getHeight();

            // Correct centering calculations
            double pictureLayoutX = (1920 - initialWidth) / 2.0;
            double pictureLayoutY = (1009 - initialHeight) / 2.0;

            messageFullPicturePane.setLayoutX(pictureLayoutX);
            messageFullPicturePane.setLayoutY(pictureLayoutY);

            messageFullPicturePane.getChildren().add(fullyPicturePreview);
            backgroundPane.getChildren().add(messageFullPicturePane);
            messageFullPicturePane.getScene().getStylesheets().add(getClass().getResource("/main/css/MainChat.css").toExternalForm());

            // Call the zoom listener and pass the initial values for the image view
            setZoomListener(backgroundPane, imageView,messageFullPicturePane);

            if (message_text != null) {
                previewPictureMessage = new Label(message_text);
                previewPictureMessage.setOnMouseClicked(Event::consume);
                previewPictureMessage.getStyleClass().add("chat-picture-message-preview-message-text");
                previewPictureMessage.setPrefWidth(Region.USE_COMPUTED_SIZE);
                previewPictureMessage.setMaxWidth(Region.USE_PREF_SIZE);
                previewPictureMessage.setAlignment(Pos.CENTER);
                StackPane.setAlignment(previewPictureMessage,Pos.BOTTOM_CENTER);
                StackPane.setMargin(previewPictureMessage,new Insets(0,0,6,0));
                messageFullPicturePane.getChildren().add(previewPictureMessage);

                double messagePrefWidth = previewPictureMessage.prefWidth(-1);
                if (messagePrefWidth >= (initialWidth - 40)) {
                    previewPictureMessage.setVisible(false);
                }
            }

        });
    }
    private void setZoomListener(Pane backgroundPane, ImageView fullyPicturePreview,StackPane pictureMessagePane) {
        backgroundPane.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY();  // Get the direction of the scroll

            // Define zoom factors for scroll up and scroll down
            double zoomFactor = (deltaY > 0) ? 1.1 : 0.9;  // Zoom in if scrolling up, zoom out if scrolling down

            // Get the current width and height of the image
            double currentWidth = fullyPicturePreview.getFitWidth();
            double currentHeight = fullyPicturePreview.getFitHeight();

            // Apply the zoom factor to the current size
            double newWidth = currentWidth * zoomFactor;
            double newHeight = currentHeight * zoomFactor;

            // Update the image size
            fullyPicturePreview.setFitWidth(newWidth);
            fullyPicturePreview.setFitHeight(newHeight);

            // Get the layout bounds of the image after zooming
            double imageWidth = fullyPicturePreview.getLayoutBounds().getWidth();
            double imageHeight = fullyPicturePreview.getLayoutBounds().getHeight();

            // Recalculate the new layout position to keep the image centered
            double newLayoutX = (1920 - imageWidth) / 2.0;
            double newLayoutY = (1009 - imageHeight) / 2.0;

            // Update layout to keep it centered
            pictureMessagePane.setLayoutX(newLayoutX);
            pictureMessagePane.setLayoutY(newLayoutY);

            double messagePrefWidth = (previewPictureMessage != null) ? previewPictureMessage.prefWidth(-1) : 0;
            if (messagePrefWidth >= (newWidth - 40)) {
                previewPictureMessage.setVisible(false);
            } else if (previewPictureMessage != null) {
                previewPictureMessage.setVisible(true);
            }
        });
    }
}
