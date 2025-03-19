package com.messenger.main.chat;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatHistory {

    private int mainUserId;
    private int contactId;
    private ScrollPane chatScrollPane;
    private VBox chatVBox;
    private AnchorPane mainAnchorPane;

    private Timeline currentFadeTimeline = null;




    public ChatHistory(int mainUserId, int contactId,ScrollPane chatScrollPane,VBox chatVBox,AnchorPane mainAnchorPane) {
        this.mainUserId = mainUserId;
        this.contactId = contactId;
        this.chatVBox = chatVBox;
        this.mainAnchorPane = mainAnchorPane;
        this.chatScrollPane = chatScrollPane;
    }


    public void load() throws SQLException {
        boolean chatIsEmpty = ChatsDataBase.getAllMessages(mainUserId,contactId).isEmpty();

        if (chatIsEmpty) {
            setCurrentDateLabel();
        } else {
            loadChatHistory();
        }
    }
    public void loadChatHistory() throws SQLException {
        List<ArrayList<Object>> allMessages = ChatsDataBase.getAllMessages(mainUserId,contactId);
        LinkedHashMap<String,List<ArrayList<Object>>> splitIntoDaysMessages = getSplitIntoDaysMessages(allMessages);
        splitIntoDaysMessages.values().forEach(message -> {
            try {
                loadMessagesWithDateLabel(message);
            } catch (SQLException | ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void loadMessagesWithDateLabel(List<ArrayList<Object>> messagesOnSameDay) throws SQLException, ParseException {
        String labelDate = getDateForDateLabel((String) messagesOnSameDay.get(0).get(6));     // 2. March
        setChatDateLabel(labelDate);

        for (ArrayList<Object> message: messagesOnSameDay) {
            String messageType = (String) message.get(7);
            switch (messageType) {
                case "text" -> loadTextMessage(message);
                case "reply_with_text" -> loadReplyWithTextMessage(message);
            }
        }
    }


    // Message Loading
    public void loadTextMessage(ArrayList<Object> message) throws SQLException, ParseException {
        int messageId = (int) message.get(0);
        int senderId = (int) message.get(1);
        int receiverId = (int) message.get(2);
        String messageText = (String) message.get(3);
        String messageTime = getMessageTime((String) message.get(6));

        HBox messageHBox = new HBox();
        messageHBox.setAlignment(senderId == mainUserId ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);
        if (ChatsDataBase.getLastMessageId(mainUserId,contactId) == messageId) VBox.setMargin(messageHBox,new Insets(0,0,20,0));
        messageHBox.setId("messageHBox"+messageId);
        chatVBox.getChildren().add(messageHBox);

        StackPane messageStackPane = new StackPane();
        messageStackPane.setId("messageStackPane"+messageId);
        messageStackPane.setMaxWidth(408);
        messageStackPane.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-stackpane" : "chat-message-contact-stackpane");
        messageStackPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.SECONDARY) {
                int x = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getX();
                int y = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getY();
                MessageButtons messageButtons = new MessageButtons(mainAnchorPane);
                if (senderId == mainUserId) {
                    messageButtons.showMessageButtons(x, y, messageId);
                } else {
                    messageButtons.showMessageReplyButton(x, y, messageId);
                }
            }
        });
        messageHBox.getChildren().add(messageStackPane);

        Label messageTextLabel = new Label(messageText);
        messageTextLabel.setId("messageTextLabel"+messageId);
        messageTextLabel.setWrapText(true);
        messageTextLabel.getStyleClass().add("chat-message-text-label");
        StackPane.setMargin(messageTextLabel,new Insets(7,50,7,12));
        messageStackPane.getChildren().add(messageTextLabel);

        Label messageTimeLabel = new Label(messageTime);
        messageTimeLabel.getStyleClass().add("chat-time-label");
        StackPane.setAlignment(messageTimeLabel,Pos.BOTTOM_RIGHT);
        StackPane.setMargin(messageTimeLabel,new Insets(0,10,4,0));
        messageStackPane.getChildren().add(messageTimeLabel);

        if (avatarIsRequired(messageId, senderId, receiverId)) {
            HBox.setMargin(messageStackPane,(senderId == mainUserId) ? new Insets(0,13, 0,0) : new Insets(0,0,0,13));
            Label avatarLabel = new Label();
            avatarLabel.setId("messageAvatarLabel" + messageId);
            setMessageAvatar(avatarLabel, senderId);
            int index = (senderId == mainUserId) ? messageHBox.getChildren().size() : 0;
            messageHBox.getChildren().add(index, avatarLabel);
            HBox.setMargin(avatarLabel,(senderId == mainUserId) ? new Insets(0,110, 0, 0) : new Insets(0,0,0,110));
        } else {
            int previousMessageId = ChatsDataBase.getPreviousMessageId(messageId,senderId,receiverId);
            if ((int) ChatsDataBase.getMessage(previousMessageId).get(1) == senderId) {
                HBox previousMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+previousMessageId);
                if (previousMessageHBox != null) {
                    Node previousAvatarLabel = previousMessageHBox.lookup("#messageAvatarLabel" + previousMessageId);
                    StackPane previousMessageStackPane = (StackPane) previousMessageHBox.lookup("#messageStackPane"+previousMessageId);
                    if (previousAvatarLabel instanceof Label) {
                        previousMessageHBox.getChildren().remove(previousAvatarLabel);
                    }
                    HBox.setMargin(previousMessageStackPane, (senderId == mainUserId) ? new Insets(0, 163, 0, 0) : new Insets(0,0,0,163));
                }

                Label newAvatarLabel = new Label();
                newAvatarLabel.setId("messageAvatarLabel" + messageId);
                setMessageAvatar(newAvatarLabel, senderId);
                int index = (senderId == mainUserId) ? messageHBox.getChildren().size() : 0;
                messageHBox.getChildren().add(index, newAvatarLabel);
                HBox.setMargin(newAvatarLabel,(senderId == mainUserId) ? new Insets(0,110, 0, 0) : new Insets(0,0,0,110));
                HBox.setMargin(messageStackPane,(senderId == mainUserId) ? new Insets(0,13, 0,0) : new Insets(0,0,0,13));
            }




        }

    }
    public void loadReplyWithTextMessage(ArrayList<Object> message) throws SQLException, ParseException {
        int messageId = (int) message.get(0);
        int senderId = (int) message.get(1);
        int receiverId = (int) message.get(2);
        String messageText = (String) message.get(3);
        int repliedMessageId = (int) message.get(5);
        String messageTime = getMessageTime((String) message.get(6));

        HBox messageHBox = new HBox();
        messageHBox.setAlignment(senderId == mainUserId ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);
        if (ChatsDataBase.getLastMessageId(mainUserId,contactId) == messageId) VBox.setMargin(messageHBox,new Insets(0,0,20,0));
        messageHBox.setId("messageHBox"+messageId);
        chatVBox.getChildren().add(messageHBox);

        StackPane messageStackPane = new StackPane();
        messageStackPane.setMaxWidth(408);
        messageStackPane.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-stackpane" : "chat-message-contact-stackpane");
        messageStackPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.SECONDARY) {
                int x = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getX();
                int y = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getY();
                MessageButtons messageButtons = new MessageButtons(mainAnchorPane);
                if (senderId == mainUserId) {
                    messageButtons.showMessageButtons(x, y, messageId);
                } else {
                    messageButtons.showMessageReplyButton(x, y, messageId);
                }
            }
        });
        messageHBox.getChildren().add(messageStackPane);

        StackPane messageReplyPane = new StackPane();
        messageReplyPane.setMinWidth(80);
        messageReplyPane.setPrefHeight(37);
        messageReplyPane.setMaxHeight(37);
        messageReplyPane.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-reply-pane" : "chat-message-contact-reply-pane");
        StackPane.setAlignment(messageReplyPane,Pos.TOP_LEFT);
        StackPane.setMargin(messageReplyPane,new Insets(7,7,0,7));
        messageStackPane.getChildren().add(messageReplyPane);
        messageReplyPane.setOnMouseClicked(clickEvent -> {
            HBox repliedmessageHBox = (HBox) chatVBox.lookup("#messageHBox"+repliedMessageId);
            double hboxPosition = getCenteredScrollPosition(repliedmessageHBox);
            smoothScrollTo(hboxPosition,0.4);
            fadeOutBackgroundColor(repliedmessageHBox);
        });

        String repliedMessageName = UsersDataBase.getNameWithId((int) ChatsDataBase.getMessage(repliedMessageId).get(1));
        Label messageReplyNameLabel = new Label(repliedMessageName);
        messageReplyNameLabel.getStyleClass().add("chat-message-reply-name");
        StackPane.setAlignment(messageReplyNameLabel,Pos.TOP_LEFT);
        StackPane.setMargin(messageReplyNameLabel,new Insets(4,8,0,8));
        messageReplyPane.getChildren().add(messageReplyNameLabel);

        String repliedMessageText = (String) (ChatsDataBase.getMessage(repliedMessageId)).get(3);
        Label messageReplyMessageLabel = new Label(repliedMessageText);
        messageReplyMessageLabel.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-reply-message" : "chat-message-contact-reply-message");
        StackPane.setAlignment(messageReplyMessageLabel,Pos.TOP_LEFT);
        StackPane.setMargin(messageReplyMessageLabel,new Insets(18,8,0,8));
        messageReplyPane.getChildren().add(messageReplyMessageLabel);

        Label messageTextLabel = new Label(messageText);
        messageTextLabel.setId("messageTextLabel"+messageId);
        messageTextLabel.setWrapText(true);
        messageTextLabel.getStyleClass().add("chat-message-text-label");
        StackPane.setMargin(messageTextLabel,new Insets(48,50,7,12));
        StackPane.setAlignment(messageTextLabel,Pos.TOP_LEFT);
        messageStackPane.getChildren().add(messageTextLabel);

        Label messageTimeLabel = new Label(messageTime);
        messageTimeLabel.getStyleClass().add("chat-time-label");
        StackPane.setAlignment(messageTimeLabel,Pos.BOTTOM_RIGHT);
        StackPane.setMargin(messageTimeLabel,new Insets(0,10,4,0));
        messageStackPane.getChildren().add(messageTimeLabel);

        if (avatarIsRequired(messageId, senderId, receiverId)) {
            HBox.setMargin(messageStackPane,(senderId == mainUserId) ? new Insets(0,13, 0,0) : new Insets(0,0,0,13));
            Label avatarLabel = new Label();
            avatarLabel.setId("messageAvatarLabel" + messageId);
            setMessageAvatar(avatarLabel, senderId);
            int index = (senderId == mainUserId) ? messageHBox.getChildren().size() : 0;
            messageHBox.getChildren().add(index, avatarLabel);
            HBox.setMargin(avatarLabel,(senderId == mainUserId) ? new Insets(0,110, 0, 0) : new Insets(0,0,0,110));
        } else {
            int previousMessageId = ChatsDataBase.getPreviousMessageId(messageId, senderId, receiverId);
            if ((int) ChatsDataBase.getMessage(previousMessageId).get(1) == senderId) {
                HBox previousMessageHBox = (HBox) chatVBox.lookup("#messageHBox" + previousMessageId);
                if (previousMessageHBox != null) {
                    Node previousAvatarLabel = previousMessageHBox.lookup("#messageAvatarLabel" + previousMessageId);
                    StackPane previousMessageStackPane = (StackPane) previousMessageHBox.lookup("#messageStackPane" + previousMessageId);
                    if (previousAvatarLabel instanceof Label) {
                        previousMessageHBox.getChildren().remove(previousAvatarLabel);
                    }
                    HBox.setMargin(previousMessageStackPane, (senderId == mainUserId) ? new Insets(0, 163, 0, 0) : new Insets(0, 0, 0, 163));
                }

                Label newAvatarLabel = new Label();
                newAvatarLabel.setId("messageAvatarLabel" + messageId);
                setMessageAvatar(newAvatarLabel, senderId);
                int index = (senderId == mainUserId) ? messageHBox.getChildren().size() : 0;
                messageHBox.getChildren().add(index, newAvatarLabel);
                HBox.setMargin(newAvatarLabel, (senderId == mainUserId) ? new Insets(0, 110, 0, 0) : new Insets(0, 0, 0, 110));
                HBox.setMargin(messageStackPane, (senderId == mainUserId) ? new Insets(0, 13, 0, 0) : new Insets(0, 0, 0, 13));
            }
        }

    }


    // Date Label
    private void setCurrentDateLabel() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
        String formattedDate = today.format(formatter);
        setChatDateLabel(formattedDate);
    }
    private void setChatDateLabel(String date) {
        Label chatDateLabel = new Label(date);
        chatDateLabel.getStyleClass().add("chat-date-label");
        VBox.setMargin(chatDateLabel,new Insets(8,0,8,0));
        chatVBox.getChildren().add(chatDateLabel);
    }


    // Date Operations
    private String getMessageTime(String fullDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(fullDate, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(outputFormatter);
    }
    private String getShortDateFromFullDate(String fullDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(fullDate, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return dateTime.format(outputFormatter);
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


    // Small functions
    public LinkedHashMap<String,List<ArrayList<Object>>> getSplitIntoDaysMessages(List<ArrayList<Object>> allMessages) {
        LinkedHashMap<String,List<ArrayList<Object>>> splitIntoDaysMessages = new LinkedHashMap<>();

        for (ArrayList<Object> message: allMessages) {
            if (!splitIntoDaysMessages.containsKey(getShortDateFromFullDate((String) message.get(6)))) {
                splitIntoDaysMessages.put(getShortDateFromFullDate((String) message.get(6)), new ArrayList<>());
            }
            splitIntoDaysMessages.get(getShortDateFromFullDate((String) message.get(6))).add(message);

        }
        return splitIntoDaysMessages;
    }
    private boolean avatarIsRequired(int messageId,int senderId,int receiverId) throws SQLException, ParseException {
        int previousMessageId = ChatsDataBase.getPreviousMessageId(messageId,senderId,receiverId);

        boolean firstMessageInChat = (previousMessageId == -1);
        boolean previousMessageIsFromDifferentSender = !firstMessageInChat && ((int) ChatsDataBase.getMessage(previousMessageId).get(1)) != ((int) ChatsDataBase.getMessage(messageId).get(1));
        boolean previousMessageIsAfterDay = !firstMessageInChat && messagesHaveOneDayDifference((String) ChatsDataBase.getMessage(previousMessageId).get(6),(String) ChatsDataBase.getMessage(messageId).get(6));
        boolean previousMessageIsAfterHour = !firstMessageInChat && messagesHaveOneHourDifference((String) ChatsDataBase.getMessage(previousMessageId).get(6),(String) ChatsDataBase.getMessage(messageId).get(6));

        return firstMessageInChat || previousMessageIsFromDifferentSender || previousMessageIsAfterDay || previousMessageIsAfterHour;
    }
    private boolean messagesHaveOneHourDifference(String previousMessageFullDate,String currentMessageFullDater) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date1 = dateFormat.parse(previousMessageFullDate);
        Date date2 = dateFormat.parse(currentMessageFullDater);
        long diffInMillis = Math.abs(date2.getTime() - date1.getTime());
        long diffInHours = diffInMillis / (60 * 60 * 1000);
        return diffInHours >= 1;
    }
    private boolean messagesHaveOneDayDifference(String previousMessageFullDate,String currentMessageFullDater) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date1 = dateFormat.parse(previousMessageFullDate);
        Date date2 = dateFormat.parse(currentMessageFullDater);
        long diffInMillis = Math.abs(date2.getTime() - date1.getTime());
        long diffInHours = diffInMillis / (24 * 60 * 60 * 1000);
        return diffInHours >= 1;
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
        // Create a new Timeline specific to this HBox
        Timeline fadeTimeline = new Timeline();

        // Set the initial background color to #333138
        BackgroundFill initialBackgroundFill = new BackgroundFill(Color.web("#333138"), CornerRadii.EMPTY, null);
        hbox.setBackground(new Background(initialBackgroundFill));

        // Interpolate the background color from #333138 to transparent
        for (int i = 0; i <= 100; i++) {
            final double progress = i / 100.0; // Progress between 0 and 1

            KeyFrame keyFrame = new KeyFrame(
                    Duration.seconds(3 * progress), // Time duration for each step
                    event -> {
                        // Interpolating the color from #333138 to transparent
                        Color interpolatedColor = Color.web("#333138").deriveColor(0, 1, 1, 1 - progress);
                        hbox.setBackground(new Background(
                                new BackgroundFill(interpolatedColor, CornerRadii.EMPTY, null)
                        ));
                    }
            );
            fadeTimeline.getKeyFrames().add(keyFrame);
        }

        // Start the timeline
        fadeTimeline.setCycleCount(1);
        fadeTimeline.play();
    }



}
