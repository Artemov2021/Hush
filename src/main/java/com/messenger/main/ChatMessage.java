package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

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

    private HBox messageHBox;
    private StackPane messageStackPane;
    private Label messageTextLabel;
    private StackPane messageReplyStackPane;
    private Label messageAvatarLabel;

    private MainChatController mainChatController;

    public ChatMessage(int messageId) throws SQLException {
        ArrayList<Object> messageList = ChatsDataBase.getMessage(messageId);
        this.id = (int) messageList.get(0);
        this.sender_id = (int) messageList.get(1);
        this.receiver_id = (int) messageList.get(2);
        this.message_text = (String) messageList.get(3);
        this.picture = (byte[]) messageList.get(4);
        this.reply_message_id = (int) messageList.get(5);
        this.time = (String) messageList.get(6);
        this.type = (String) messageList.get(7);
        this.received = (boolean) messageList.get(8);
        this.previousMessageId = calculatePreviousMessageId();
    }
    private void injectChatElements(MainChatController mainChatController) {
        this.mainChatController = mainChatController;
        this.chatVBox = mainChatController.chatVBox;
        this.chatScrollPane = mainChatController.chatScrollPane;
    }
    public HBox render(MainChatController mainChatController) throws Exception {
        injectChatElements(mainChatController);
        setPotentialDateLabel();
        return switch (type) {
            case "text" -> buildTextMessage();
            case "reply_with_text" -> buildReplyWithTextMessage();
            default -> throw new Exception();
        };
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
    private HBox buildReplyWithTextMessage() throws SQLException, ParseException {
        setMessageHBox();
        setMessageStackPane();
        setReplyStackPane();
        setMessageTextLabel();
        setMessageTimeLabel();
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
    private void setMessageTextLabel() {
        Insets padding = getTextLabelPadding();
        messageTextLabel = new Label(message_text);
        StackPane.setAlignment(messageTextLabel,Pos.TOP_LEFT);
        messageTextLabel.setId("messageTextLabel"+id);
        messageTextLabel.setWrapText(true);
        messageTextLabel.getStyleClass().add("chat-message-text-label");
        StackPane.setMargin(messageTextLabel,padding);
        messageStackPane.getChildren().add(messageTextLabel);
    }
    private void setMessageTimeLabel() {
        String messageTime = getMessageTime(time);
        Label timeLabel = new Label(messageTime);
        timeLabel.getStyleClass().add("chat-time-label");
        StackPane.setAlignment(timeLabel,Pos.BOTTOM_RIGHT);
        StackPane.setMargin(timeLabel,new Insets(0,10,4,0));
        messageStackPane.getChildren().add(timeLabel);
    }
    private void setReplyStackPane() throws SQLException {
        short minWidth = 80;
        short prefHeight = 37;
        short maxHeight = 37;
        String messageReplyPaneStyle = (mainUserId == sender_id) ? "chat-message-user-reply-pane" : "chat-message-contact-reply-pane";
        boolean repliedMessageExists = ChatsDataBase.messageExists(sender_id,receiver_id,reply_message_id);
        boolean isRepliedMessagePicture = repliedMessageExists && ((String) ChatsDataBase.getMessage(reply_message_id).get(7)).contains("picture");

        messageReplyStackPane = new StackPane();
        messageReplyStackPane.setId("messageReplyStackPane"+id);
        messageReplyStackPane.setCursor(repliedMessageExists ? Cursor.HAND : Cursor.DEFAULT);
        messageReplyStackPane.setMinWidth(minWidth);
        messageReplyStackPane.setPrefHeight(prefHeight);
        messageReplyStackPane.setMaxHeight(maxHeight);
        messageReplyStackPane.getStyleClass().add(messageReplyPaneStyle);
        StackPane.setAlignment(messageReplyStackPane,Pos.TOP_LEFT);
        StackPane.setMargin(messageReplyStackPane,new Insets(7,7,0,7));
        messageStackPane.getChildren().add(messageReplyStackPane);


        if (!repliedMessageExists) {
            Label repliedMessageDeletedMessage = new Label("(deleted message)");
            repliedMessageDeletedMessage.getStyleClass().add((mainUserId == sender_id) ? "chat-message-user-deleted-message" : "chat-message-contact-deleted-message");
            StackPane.setAlignment(repliedMessageDeletedMessage,Pos.TOP_LEFT);
            StackPane.setMargin(repliedMessageDeletedMessage,new Insets(10,8,5,8));
            messageReplyStackPane.getChildren().add(repliedMessageDeletedMessage);
        } else if (isRepliedMessagePicture) {
            // TODO
        } else {
            String repliedMessageName = UsersDataBase.getNameWithId((int) ChatsDataBase.getMessage(reply_message_id).get(1));
            Label messageReplyNameLabel = new Label(repliedMessageName);
            messageReplyNameLabel.getStyleClass().add("chat-message-reply-name");
            messageReplyNameLabel.setMouseTransparent(true);
            StackPane.setAlignment(messageReplyNameLabel,Pos.TOP_LEFT);
            StackPane.setMargin(messageReplyNameLabel,new Insets(4,8,0,8));
            messageReplyStackPane.getChildren().add(messageReplyNameLabel);

            String repliedMessageText = (String) (ChatsDataBase.getMessage(reply_message_id)).get(3);
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
    private void setMessageAvatar() throws SQLException, ParseException {
        messageAvatarLabel = new Label();
        messageAvatarLabel.setId("messageAvatarLabel" + id);
        setAvatarLabel();
        int messageHBoxIndex = (mainUserId == sender_id) ? messageHBox.getChildren().size() : 0;
        messageHBox.getChildren().add(messageHBoxIndex, messageAvatarLabel);
        HBox.setMargin(messageAvatarLabel,(mainUserId == sender_id) ? new Insets(0,100, 0, 0) : new Insets(0,0,0,100));

        if (shouldRemovePreviousAvatar()) {
            removePreviousAvatar();
        }
    }

















    private Insets getTextLabelPadding() {
        return switch (type) {
            case "text" -> new Insets(9,50,7,13);
            case "reply_with_text" -> new Insets(48,50,7,12);
            default -> null;
        };
    }


    private int calculatePreviousMessageId() throws SQLException {
        return ChatsDataBase.getPreviousMessageId(id,sender_id,receiver_id);
    }
    private void setPotentialDateLabel() throws ParseException, SQLException {
        boolean isFirstMessage = chatVBox.getChildren().isEmpty();
        boolean isPreviousMessageOneDay = !isFirstMessage && messagesHaveOneDayDifference((String) ChatsDataBase.getMessage(previousMessageId).get(6),time);

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
    private String getLabelIdCurrentDate() {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(time, inputFormatter);

        return dateTime.toLocalDate().toString(); // Outputs in yyyy-MM-dd format
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
        byte[] blobBytes = UsersDataBase.getAvatarWithId(sender_id);
        if (blobBytes == null) {
            messageAvatarLabel.getStyleClass().clear();
            messageAvatarLabel.getStyleClass().add("chat-message-default-avatar");
            messageAvatarLabel.setPrefHeight(40);
            messageAvatarLabel.setPrefWidth(40);
            return;
        }
        ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
        ImageView imageView = new ImageView(new Image(byteStream));
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setSmooth(true);
        messageAvatarLabel.setGraphic(imageView);
        Circle clip = new Circle();
        clip.setLayoutX(20);
        clip.setLayoutY(20);
        clip.setRadius(20);
        messageAvatarLabel.setClip(clip);
    }
    private boolean shouldRemovePreviousAvatar() throws SQLException, ParseException {
        boolean isFirstMessage = !chatVBox.getChildren().stream()
                .anyMatch(node -> node.getId() != null && node.getId().startsWith("messageHBox"));
        boolean isPreviousMessageForeign = previousMessageId != -1 && (sender_id != (int) ChatsDataBase.getMessage(previousMessageId).get(1));
        boolean isPreviousMessageOneDay =  previousMessageId != -1 && messagesHaveOneDayDifference((String) ChatsDataBase.getMessage(previousMessageId).get(6),time);

        return !isFirstMessage && !isPreviousMessageForeign && !isPreviousMessageOneDay;
    }
    private void removePreviousAvatar() {
        HBox previousMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+previousMessageId);
        StackPane previousMessageStackPane = (StackPane) previousMessageHBox.lookup("#messageStackPane"+previousMessageId);
        Label previousMessageAvatar = (Label) previousMessageHBox.lookup("#messageAvatarLabel"+previousMessageId);

        previousMessageHBox.getChildren().remove(previousMessageAvatar);
        HBox.setMargin(previousMessageStackPane,(mainUserId == sender_id) ? new Insets(0, 153, 0, 0) : new Insets(0,0,0,153));
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
}
