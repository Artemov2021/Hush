package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    private String sendingMessageType = "text"; // the default type of the message is always text


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
        boolean chatIsEmpty = ChatsDataBase.getLastMessage(mainUserId,contactId).isEmpty();
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
                normalMessagePane.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-pane" : "chat-message-contact-pane");
                normalMessagePane.setPrefHeight(normalMessagePaneHeight);
                normalMessagePane.setPrefWidth(normalMessagePaneWidth);

                Label normalMessageTextLabel = new Label(messageText);
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
                    setAppropriateAvatar(messageHBox,normalMessagePane,senderId);
                } else {
                    HBox.setMargin(normalMessagePane, (senderId == mainUserId) ? new Insets(0, 63, 0, 0) : new Insets(0, 0, 0, 63));
                }

                // right click on the message opens buttons (reply,edit,delete)
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
                replyWithTextMessagePane.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-pane" : "chat-message-contact-pane");
                replyWithTextMessagePane.setPrefHeight(replyWithTextMessagePaneHeight);
                replyWithTextMessagePane.setPrefWidth(replyWithTextMessagePaneWidth);

                Label replyWithTextMessageTextLabel = new Label(messageReplyText);
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
                replyWithTextReplyPane.setPrefWidth(replyWithTextReplyPaneWidth);
                replyWithTextReplyPane.setPrefHeight(replyWithTextReplyPaneHeight);
                replyWithTextReplyPane.getStyleClass().add((senderId == mainUserId) ? "chat-message-reply-user-pane" : "chat-message-reply-contact-pane");
                replyWithTextReplyPane.setLayoutX(replyWithTextPaneLeftMargin);
                replyWithTextReplyPane.setLayoutY(replyWithTextPaneTopMargin);

                int replyWithTextReplyNameLeftMargin = 5;
                int replyWithTextReplyNameTopMargin = 2;
                String repliedMessageUserName = UsersDataBase.getNameWithId((int) ChatsDataBase.getMessageWithId(replyMessageId).get(1));
                Label replyWithTextReplyName = new Label(repliedMessageUserName);
                replyWithTextReplyName.setMaxWidth(replyWithTextReplyPaneWidth - (replyWithTextReplyNameLeftMargin * 2));
                replyWithTextReplyName.getStyleClass().add("chat-message-reply-name");
                replyWithTextReplyName.setLayoutX(replyWithTextReplyNameLeftMargin);
                replyWithTextReplyName.setLayoutY(replyWithTextReplyNameTopMargin);

                int replyWithTextReplyMessageLeftMargin = 5;
                int replyWithTextReplyMessageTopMargin = 15;
                Label replyWithTextReplyMessage = new Label((String) ChatsDataBase.getMessageWithId(replyMessageId).get(3));
                replyWithTextReplyMessage.setMaxWidth(replyWithTextReplyPaneWidth - (replyWithTextReplyMessageLeftMargin * 2));
                replyWithTextReplyMessage.getStyleClass().add("chat-message-reply-message");
                replyWithTextReplyMessage.setLayoutX(replyWithTextReplyMessageLeftMargin);
                replyWithTextReplyMessage.setLayoutY(replyWithTextReplyMessageTopMargin);

                replyWithTextReplyPane.getChildren().addAll(replyWithTextReplyName,replyWithTextReplyMessage);
                replyWithTextMessagePane.getChildren().addAll(replyWithTextReplyPane,replyWithTextMessageTextLabel, replyWithTextTimeLabel);
                messageHBox.getChildren().add(replyWithTextMessagePane);

                if (avatarRequired) {
                    setAppropriateAvatar(messageHBox,replyWithTextMessagePane,senderId);
                } else {
                    HBox.setMargin(replyWithTextMessagePane, (senderId == mainUserId) ? new Insets(0, 63, 0, 0) : new Insets(0, 0, 0, 63));
                }

                replyWithTextReplyPane.setOnMouseClicked(mouseEvent -> {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                        HBox repliedMessageHBox = (HBox) mainAnchorPane.lookup("#messageHBox"+replyMessageId);
                        selectRepliedMessageHBox(repliedMessageHBox);
                        scrollSmooth(getHBoxVvalue(repliedMessageHBox));
                    }
                });

                // right click on the message opens buttons (reply,edit,delete)
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

                break;
        }


        chatVBox.getChildren().add(messageHBox);

    }



    // chat message sending
    @FXML
    public void sendMessage() throws SQLException, ExecutionException, InterruptedException {
        String currentMessageFullDate = getCurrentFullDate();
        switch (sendingMessageType) {
            case "text":
                String messageText = chatTextField.getText().trim();
                if (messageText.isEmpty()) return;

                int messageId = addMessageToDB(messageText,null,-1,currentMessageFullDate,false);
                List<List<Object>> allMessages = ChatsDataBase.getAllMessages(mainUserId,contactId);
                String previousMessageDate = getMessageDate((String) allMessages.get(getIndexWithMessageId(allMessages,messageId)-1).get(6));

                boolean isFirstMessage = isFirstMessage(allMessages,messageId);
                boolean isResponseMessage = isResponseMessage(allMessages,messageId);
                boolean isAfterOneHourMessage = isAfterOneHourMessage(allMessages,messageId);
                boolean isNewDayMessage = isNewDayMessage(getCurrentDate(),previousMessageDate);
                boolean avatarIsRequired = isFirstMessage || isResponseMessage || isAfterOneHourMessage || isNewDayMessage;

                List<Object> message = ChatsDataBase.getMessageWithId(messageId);

                if (isNewDayMessage(getCurrentDate(),previousMessageDate)) {
                    setChatDateLabel(getCurrentLongDate());
                }
                loadMessage(message,avatarIsRequired);
                mainContactMessageLabel.setText(messageText);
                break;
        }


        scrollToBottom();
        chatTextField.setText("");
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
    private void scrollToBottom() {
        Platform.runLater(() -> {
            scrollSmooth(1.0);  // This ensures the ScrollPane scrolls to the bottom
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
            scrollToBottom();
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.2),scrollDownBackground);
            fadeOut.setFromValue(1.0); // Start fully visible
            fadeOut.setToValue(0.0);   // End fully transparent
            fadeOut.play();
            fadeOut.setOnFinished(event1 -> chatBackgroundPane.getChildren().remove(scrollDownBackground));
        });
    }
    private int getAppropriateBottomButtonPosition() {
        return switch (sendingMessageType) {
            case "text" -> 560;
            case "reply_with_text" -> 520;
            case "reply_with_picture" -> 520;
            case "reply_with_picture_and_text" -> 520;
            case "picture" -> 520;
            case "picture_with_text" -> 520;
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
    private void setAppropriateAvatar(HBox messageHBox,Pane messagePane, int senderId) throws SQLException {
        Label avatarLabel = new Label();
        sendAvatar(avatarLabel, senderId);
        if (senderId == mainUserId) {
            // For your messages, avatar on the right side
            messageHBox.getChildren().add(avatarLabel);
            HBox.setMargin(avatarLabel, new Insets(0, 25, 0, 0));
            HBox.setMargin(messagePane, new Insets(0, 8, 0, 0));
        } else {
            // For contact messages, avatar on the left side
            messageHBox.getChildren().add(0,avatarLabel);
            HBox.setMargin(avatarLabel, new Insets(0, 0, 0, 25));
            HBox.setMargin(messagePane, new Insets(0, 0, 0, 8));
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
            Label replyWrapperName = (Label) mainAnchorPane.lookup("#replyWrapperName");
            String name = UsersDataBase.getNameWithId((int)ChatsDataBase.getMessageWithId(messageId).get(1));
            Label replyWrapperMessage = (Label) mainAnchorPane.lookup("#replyWrapperMessage");
            String message = (String) ChatsDataBase.getMessageWithId(messageId).get(3);

            replyWrapperName.setText(name);
            replyWrapperMessage.setText(message);
        } else if (mainAnchorPane.lookup("#reply-wrapper") == null) {
            sendingMessageType = "reply_with_text";
            Pane scrollDownButton = (Pane) chatBackgroundPane.lookup("#scrollDownButton");
            if (scrollDownButton != null) scrollDownButton.setLayoutY(getAppropriateBottomButtonPosition());
            chatScrollPane.setPadding(new Insets(0,0,50,0));
            chatTextField.requestFocus();
            Pane replyWrapperPane = new Pane();
            replyWrapperPane.setId("replyWrapper");
            replyWrapperPane.getStyleClass().add("chat-reply-wrapper");
            replyWrapperPane.setPrefWidth(846);
            replyWrapperPane.setPrefHeight(45);
            replyWrapperPane.setLayoutX(convertToTopLevelAnchorPaneCoordinates(chatTextField,chatTextField.getLayoutX(),chatTextField.getLayoutY()).getX());
            replyWrapperPane.setLayoutY(chatTextField.getScene().getHeight() - 94);

            Label replyWrapperExitLabel = new Label();
            replyWrapperExitLabel.getStyleClass().add("chat-reply-wrapper-exit-label");
            replyWrapperExitLabel.setPrefWidth(18);
            replyWrapperExitLabel.setPrefHeight(18);
            replyWrapperExitLabel.setLayoutX(806);
            replyWrapperExitLabel.setLayoutY(14);

            Label replyWrapperRow = new Label();
            replyWrapperRow.getStyleClass().add("chat-reply-wrapper-row-label");
            replyWrapperRow.setPrefWidth(26);
            replyWrapperRow.setPrefHeight(29);
            replyWrapperRow.setLayoutX(21);
            replyWrapperRow.setLayoutY(9);

            String messageUserName = UsersDataBase.getNameWithId((int)ChatsDataBase.getMessageWithId(messageId).get(1));
            Label replyWrapperName = new Label(messageUserName);
            replyWrapperName.setId("replyWrapperName");
            replyWrapperName.getStyleClass().add("chat-reply-wrapper-name-label");
            replyWrapperName.setLayoutX(58);
            replyWrapperName.setLayoutY(7);

            String message = (String) ChatsDataBase.getMessageWithId(messageId).get(3);
            Label replyWrapperMessageText = new Label(message);
            replyWrapperMessageText.setId("replyWrapperMessage");
            replyWrapperMessageText.getStyleClass().add("chat-reply-wrapper-message-label");
            replyWrapperMessageText.setMaxWidth(720);
            replyWrapperMessageText.setLayoutX(58);
            replyWrapperMessageText.setLayoutY(24);

            replyWrapperPane.getChildren().addAll(replyWrapperExitLabel,replyWrapperRow,replyWrapperName,replyWrapperMessageText);
            mainAnchorPane.getChildren().add(replyWrapperPane);

            replyWrapperExitLabel.setOnMouseClicked(clickedEvent -> {
                mainAnchorPane.getChildren().remove(replyWrapperPane);
                chatScrollPane.setPadding(new Insets(0,0,0,0));
                sendingMessageType = "text";
                Pane newScrollDownButton = (Pane) chatBackgroundPane.lookup("#scrollDownButton");
                if (newScrollDownButton != null) {
                    newScrollDownButton.setLayoutY(getAppropriateBottomButtonPosition());
                }
            });
        }
    }
    private void showEditWrapper(int messageId) throws SQLException {
        if (mainAnchorPane.lookup("#editWrapper") != null) {
            Label editWrapperName = (Label) mainAnchorPane.lookup("#editWrapperName");
            String name = UsersDataBase.getNameWithId((int) ChatsDataBase.getMessageWithId(messageId).get(1));
            Label editWrapperMessage = (Label) mainAnchorPane.lookup("#editWrapperMessage");
            String message = (String) ChatsDataBase.getMessageWithId(messageId).get(3);

            editWrapperName.setText(name);
            editWrapperMessage.setText(message);
        } else if (mainAnchorPane.lookup("#editWrapper") == null) {

        }
    }


}
