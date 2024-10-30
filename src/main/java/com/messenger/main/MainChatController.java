package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.time.LocalDateTime;
import java.time.LocalTime;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


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
    private Label chatDateLabel;
    @FXML
    private TextField chatTextField;

    private AnchorPane mainAnchorPane;
    private int contactId;
    private int mainUserId;


    public void initializeWithValue() throws SQLException {
        initializeChatInterface();
        loadChatHistory();



        // Get current date and time
        LocalDateTime now = LocalDateTime.now();

        // Define the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        // Format the current date and time
        String formattedDateTime = now.format(formatter);

        // Print the result
        System.out.println(formattedDateTime);




    }

    private void initializeChatInterface() throws SQLException {
        setChatPosition();
        removeTitle();
        setProfilePicture();
        setName();
        setDateLabelSpacing();
        setMessageSpacing(3);
        ScrollPaneEffect.addScrollBarEffect(chatScrollPane);
        setTextFieldFocus();
    }
    private void loadChatHistory() throws SQLException {
        boolean chatIsEmpty = ChatsDataBase.getLastMessage(mainUserId,contactId).isEmpty();
        if (chatIsEmpty) {
            setChatDateLabel();
        } else {
            ArrayList<List<Object>> messages = ChatsDataBase.getAllMessages(mainUserId,contactId);
            System.out.println(messages);
        }
    }



    public void setMainAnchorPane(AnchorPane anchorPane) {
        this.mainAnchorPane = anchorPane;
    }
    public void setMainUserId(int id) {
        this.mainUserId = id;
    }
    public void setContactId(int id) {
        this.contactId = id;
    }


    private void setChatPosition() {
        chatBackgroundPane.setLayoutX(310);
    }
    private void removeTitle() {
        Set<String> titlesToRemove = new HashSet<>(Arrays.asList("mainTitle", "mainSmallTitle", "mainLoginTitle"));

        List<Label> titles = mainAnchorPane.getChildren().stream()
                .filter(node -> node instanceof Label && titlesToRemove.contains(node.getId())) // Check type and ID
                .map(node -> (Label) node) // Cast to Label
                .collect(Collectors.toList()); // Collect into List<Label>
        mainAnchorPane.getChildren().removeAll(titles);
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
    private void setDateLabelSpacing() {
        VBox.setMargin(chatDateLabel,new Insets(10,0,15,0));
    }
    private void setMessageSpacing(double space) {
        chatVBox.setSpacing(space);
    }
    private void setTextFieldFocus() {
        Platform.runLater(() -> {
            if (chatTextField.isVisible() && !chatTextField.isDisabled()) {
                chatTextField.requestFocus();
            }
        });
    }
    private void setChatDateLabel() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
        String formattedDate = today.format(formatter);
        chatDateLabel.setText(formattedDate);
    }
    private String getCurrentTime() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return currentTime.format(formatter);
    }
    private void sendAvatar(Label avatar) throws SQLException {
        byte[] blobBytes = UsersDataBase.getAvatarWithId(mainUserId);
        assert blobBytes != null;
        ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
        ImageView imageView = new ImageView(new Image(byteStream));
        imageView.setFitHeight(34);
        imageView.setFitWidth(34);
        imageView.setSmooth(true);
        avatar.setGraphic(imageView);
        Circle clip = new Circle();
        clip.setLayoutX(17);
        clip.setLayoutY(17);
        clip.setRadius(17);
        avatar.setClip(clip);
    }
    private boolean requiresAvatarDisplay() throws SQLException {
        return ChatsDataBase.getLastMessage(mainUserId,contactId).isEmpty();
        // TODO
    }
    private void showScrollDownButton() {
        Label scrollDownBackground = new Label();
        scrollDownBackground.setPrefWidth(34);
        scrollDownBackground.setPrefHeight(36);
        scrollDownBackground.getStyleClass().add("chat-scroll-down-background");
        scrollDownBackground.setLayoutX(800);
        scrollDownBackground.setLayoutY(500);
        chatBackgroundPane.getChildren().add(scrollDownBackground);
    }
    private int addMessageToDB(String message,byte[] picture,int replyMessageId,String time) throws SQLException {
        return ChatsDataBase.addMessage(mainUserId,contactId,message,picture,replyMessageId,time);
    }

    @FXML
    public void sendMessage() throws SQLException {
        String messageText = chatTextField.getText().trim();
        String currentTime = getCurrentTime();

        if (messageText.isEmpty()) {
            return;
        }

        chatTextField.setText("");

        HBox messageHBox = new HBox();
        messageHBox.setAlignment(Pos.BOTTOM_RIGHT);

        Pane messagePane = new Pane();
        messagePane.getStyleClass().add("chat-message-pane");

        Label messageLabel = new Label(messageText);
        messageLabel.setWrapText(true);
        messageLabel.setFocusTraversable(false);
        messageLabel.getStyleClass().add("chat-message-label");
        messageLabel.setMaxWidth(292);

        Label timeLabel = new Label(currentTime);
        timeLabel.getStyleClass().add("chat-time-label");
        timeLabel.layoutXProperty().bind(messagePane.widthProperty().subtract(timeLabel.widthProperty()).subtract(9)); // 10px padding from the right edge
        timeLabel.layoutYProperty().bind(messagePane.heightProperty().subtract(timeLabel.heightProperty()).subtract(4)); // 10px padding from the bottom edge

        if (requiresAvatarDisplay()) {
            Label avatarLabel = new Label();
            sendAvatar(avatarLabel);
            messageHBox.getChildren().add(avatarLabel);
            HBox.setMargin(avatarLabel, new Insets(0, 25, 0, 0));
            HBox.setMargin(messagePane, new Insets(0, 8, 0, 0));
        } else {
            HBox.setMargin(messagePane, new Insets(0, 68, 0, 0));
        }

        messagePane.getChildren().addAll(messageLabel,timeLabel);
        messageHBox.getChildren().add(0,messagePane);
        System.out.println(messageHBox.getChildren());
        chatVBox.getChildren().add(messageHBox);
        chatScrollPane.setVvalue(1.0); // scroll down after adding a message


        // set minimum size for Hbox and Message Label ( prevents shrinking )
        messageHBox.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            double hboxHeight = messageHBox.getHeight();
            messageHBox.setMinHeight(hboxHeight);
        });

        messageLabel.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            double labelHeight = messageLabel.getHeight();
            messageLabel.setMinHeight(labelHeight);
        });

        chatScrollPane.vvalueProperty().addListener((obs, oldValue, newValue) -> {
            showScrollDownButton();
        });

        System.out.println(ChatsDataBase.getLastMessageTime(mainUserId,contactId)+" new: "+currentTime);


//        String time1 = ChatsDataBase.getLastMessageTime(mainUserId,contactId); // First time
//        String time2 = "23:53"; // Second time
//
//        if (isDifferenceGreaterThanOneHour(time1, time2)) {
//            System.out.println("The difference is greater than one hour.");
//        } else {
//            System.out.println("The difference is NOT greater than one hour.");
//        }



        // saving message to the database
        int messageId = addMessageToDB(messageText,null,-1,currentTime);
        messagePane.setId("messagePane"+messageId);

        messagePane.setOnMouseClicked(mouseEvent -> {
            System.out.println(messagePane.getId());
        });



    }


    public static boolean isDifferenceGreaterThanOneHour(String time1, String time2) {
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

        return difference > 60; // Check if the difference is greater than 60 minutes
    }




//    private void loadMessageHistory() throws SQLException {
//        ArrayList<ArrayList<String>> messages = DetailedDataBase.getMessages(mainUserId,contactId);
//        setDateHistoryLabel(getHours(messages.get(0).get(2)));
//        for (ArrayList<String> message : messages) {
//            //setDateHistoryLabel(getHours(message.get(2)));
//        }
//    }
//
//

//    private void setDateHistoryLabel(String date) {
//        LocalDate today = LocalDate.parse(date);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
//        String formattedDate = today.format(formatter);
//        timeDialogBorderLabel.setText(formattedDate);
//    }
//    private String getHours(String fulldate) {
//        String datePattern = "^(\\d+)-(\\d+)-(\\d+)";
//        Pattern datePatternCompiled = Pattern.compile(datePattern);
//        Matcher matcher = datePatternCompiled.matcher(fulldate);
//        if (matcher.find()) {
//            return matcher.group();
//        }
//        return null;
//    }
}
