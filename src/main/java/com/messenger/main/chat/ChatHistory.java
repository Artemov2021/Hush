package com.messenger.main.chat;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.main.MainChatController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.print.attribute.standard.JobKOctets;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatHistory {

    private int mainUserId;
    private int contactId;
    private Pane backgroundPane;
    private VBox chatVBox;
    private AnchorPane mainAnchorPane;

    public ChatHistory(int mainUserId, int contactId,Pane backgroundPane,VBox chatVBox,AnchorPane mainAnchorPane) {
        this.mainUserId = mainUserId;
        this.contactId = contactId;
        this.backgroundPane = backgroundPane;
        this.chatVBox = chatVBox;
        this.mainAnchorPane = mainAnchorPane;
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
        HashMap<String,List<ArrayList<Object>>> splitIntoDaysMessages = getSplitIntoDaysMessages(allMessages);
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
                case "text":
                    loadTextMessage(message);
                    break;
                case "reply_with_text":
                    //loadReplyWithTextMessage(message);
                    break;
            }
        }
    }


    // Small functions
    public HashMap<String,List<ArrayList<Object>>> getSplitIntoDaysMessages(List<ArrayList<Object>> allMessages) {
        HashMap<String,List<ArrayList<Object>>> splitIntoDaysMessages = new HashMap<>();

        for (ArrayList<Object> message: allMessages) {
            if (!splitIntoDaysMessages.containsKey(getShortDateFromFullDate((String) message.get(6)))) {
                splitIntoDaysMessages.put(getShortDateFromFullDate((String) message.get(6)), new ArrayList<>());
            }
            splitIntoDaysMessages.get(getShortDateFromFullDate((String) message.get(6))).add(message);

        }
        return splitIntoDaysMessages;
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
    private boolean avatarIsRequired(int messageId,int senderId,int receiverId) throws SQLException, ParseException {
        int previousMessageId = ChatsDataBase.getPreviousMessageId(messageId,senderId,receiverId);

        boolean firstMessageInChat = (previousMessageId == -1);
        boolean previousMessageIsFromContact = !firstMessageInChat && ((int) ChatsDataBase.getMessage(previousMessageId).get(1)) != mainUserId;
        boolean previousMessageIsAfterDay = !firstMessageInChat && messagesHaveOneDayDifference((String) ChatsDataBase.getMessage(previousMessageId).get(6),(String) ChatsDataBase.getMessage(messageId).get(6));
        boolean previousMessageIsAfterHour = !firstMessageInChat && messagesHaveOneHourDifference((String) ChatsDataBase.getMessage(previousMessageId).get(6),(String) ChatsDataBase.getMessage(messageId).get(6));

        return firstMessageInChat || previousMessageIsFromContact || previousMessageIsAfterDay || previousMessageIsAfterHour;
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
    private void showMessageButtons(int clickPlaceX,int clickPlaceY) {
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

        Pane messageButtonsBackground = new Pane();
        messageButtonsBackground.setPrefWidth(105);
        messageButtonsBackground.setPrefHeight(113);
        messageButtonsBackground.setLayoutX(clickPlaceX);
        messageButtonsBackground.setLayoutY(clickPlaceY);
        messageButtonsBackground.setStyle("-fx-background-color: green");
       // messageButtonsBackground.getStyleClass().add("chat-message-buttons-background");
        messageButtonsOverlay.getChildren().add(messageButtonsBackground);

    }


    // Message Loading
    public void loadTextMessage(ArrayList<Object> message) throws SQLException, ParseException {
        int messageId = (int) message.get(0);
        int senderId = (int) message.get(1);
        int receiverId = (int) message.get(2);
        String messageText = (String) message.get(3);
        String messageTime = getMessageTime((String) message.get(6));

        HBox messageHBox = new HBox();
        messageHBox.setAlignment(senderId == mainUserId ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        messageHBox.setId("messageHBox"+messageId);

        StackPane messageStackPane = new StackPane();
        messageStackPane.setMaxWidth(408);
        messageStackPane.getStyleClass().add("chat-message-user-stackpane");

        Label messageTextLabel = new Label(messageText);
        messageTextLabel.setId("messageTextLabel"+messageId);
        messageTextLabel.setWrapText(true);
        messageTextLabel.getStyleClass().add("chat-message-text-label");
        StackPane.setMargin(messageTextLabel,new Insets(7,50,7,12));

        Label messageTimeLabel = new Label(messageTime);
        messageTimeLabel.getStyleClass().add("chat-time-label");
        StackPane.setAlignment(messageTimeLabel,Pos.BOTTOM_RIGHT);
        StackPane.setMargin(messageTimeLabel,new Insets(0,10,4,0));

        messageHBox.getChildren().add(messageStackPane);
        if (avatarIsRequired(messageId, senderId, receiverId)) {
            double messageMargin = (senderId == mainUserId) ? 13 : 0;
            HBox.setMargin(messageStackPane, new Insets(0, messageMargin, 0, 13 - messageMargin));
            Label avatarLabel = new Label();
            avatarLabel.setId("avatarLabel" + messageId);
            setMessageAvatar(avatarLabel, senderId);
            int index = (senderId == mainUserId) ? messageHBox.getChildren().size() : 0;
            messageHBox.getChildren().add(index, avatarLabel);
            double avatarMargin = (senderId == mainUserId) ? 110 : 0;
            HBox.setMargin(avatarLabel, new Insets(0, avatarMargin, 0, 110 - avatarMargin));
        } else {
            double noAvatarMargin = (senderId == mainUserId) ? 163 : 0;
            HBox.setMargin(messageStackPane, new Insets(0, noAvatarMargin, 0, 163 - noAvatarMargin));
        }

        messageStackPane.getChildren().addAll(messageTextLabel,messageTimeLabel);
        chatVBox.getChildren().add(messageHBox);

        messageStackPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.SECONDARY) {
                int x = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getX();
                int y = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getY();
                showMessageButtons(x,y);
            }
        });
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
    private String getMessageTime(String fullDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(fullDate, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(outputFormatter);
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

}
