package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
        String messageType = getTypeOfMessage(message);
        int senderId = (int) message.get(1);
        String messageTime = getMessageHours((String) message.get(6));

        HBox messageHBox = new HBox();
        messageHBox.setAlignment((senderId == mainUserId) ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);

        switch (messageType) {
            case "text":

                String messageText = (String) message.get(3);

                int normalMessagePaddingTop = 5;
                int normalMessagePaddingRight = 45;
                int normalMessagePaddingBottom = 8;
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

                Label normalMessagetimeLabel = new Label(messageTime);
                normalMessagetimeLabel.getStyleClass().add("chat-time-label");
                normalMessagetimeLabel.layoutXProperty().bind(normalMessagePane.widthProperty().subtract(normalMessagetimeLabel.widthProperty()).subtract(9)); // 10px padding from the right edge
                normalMessagetimeLabel.layoutYProperty().bind(normalMessagePane.heightProperty().subtract(normalMessagetimeLabel.heightProperty()).subtract(4)); // 10px padding from the bottom edge

                normalMessagePane.getChildren().addAll(normalMessageTextLabel, normalMessagetimeLabel);
                messageHBox.getChildren().add(normalMessagePane);

                if (avatarRequired) {
                    setAppropriateAvatar(messageHBox,normalMessagePane,senderId);
                } else {
                    // If no avatar, add appropriate margin
                    HBox.setMargin(normalMessagePane, (senderId == mainUserId) ? new Insets(0, 63, 0, 0) : new Insets(0, 0, 0, 63));
                }
                break;

            case "reply_with_text":

                String messageReplyText = (String) message.get(3);

                int replyWithTextPaddingTop = 33;
                int replyWithTextPaddingRight = 45;
                int replyWithTextPaddingBottom = 8;
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

                Pane replyWithTextReplyPane = new Pane();
                replyWithTextReplyPane.setPrefWidth(replyWithTextMessagePaneWidth - 14);
                replyWithTextReplyPane.setPrefHeight(21);
                replyWithTextReplyPane.getStyleClass().add("chat-message-reply-pane");
                replyWithTextReplyPane.setLayoutX(7); // margin
                replyWithTextReplyPane.setLayoutY(7);

                Label replyWithTextReplyName = new Label("Tymur");
                replyWithTextReplyName.getStyleClass().add("chat-message-reply-name");
                replyWithTextReplyName.setLayoutX(2);
                replyWithTextReplyName.setLayoutY(2);

                replyWithTextReplyPane.getChildren().add(replyWithTextReplyName);
                replyWithTextMessagePane.getChildren().addAll(replyWithTextReplyPane,replyWithTextMessageTextLabel, replyWithTextTimeLabel);
                messageHBox.getChildren().add(replyWithTextMessagePane);

                if (avatarRequired) {
                    setAppropriateAvatar(messageHBox,replyWithTextMessagePane,senderId);
                } else {
                    // If no avatar, add appropriate margin
                    HBox.setMargin(replyWithTextMessagePane, (senderId == mainUserId) ? new Insets(0, 63, 0, 0) : new Insets(0, 0, 0, 63));
                }
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
            chatScrollPane.setVvalue(1.0);  // This ensures the ScrollPane scrolls to the bottom
        });
    }
    private void setBottomButtonListener() {
        chatScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            Pane scrollDownButton = (Pane) chatBackgroundPane.lookup("#scrollDownButton");
            if (newValue.doubleValue() < 1.0) {
                if (scrollDownButton == null) showScrollToBottomButton();
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
        scrollDownBackground.setLayoutY(560);
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





}
