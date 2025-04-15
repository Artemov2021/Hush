package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import com.messenger.main.chat.MessageButtons;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.Cursor;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainChatController extends MainContactController {
    @FXML private Pane chatBackground;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Label chatMainAvatar;
    @FXML private Label chatMainName;
    @FXML private VBox chatVBox;
    @FXML private TextField chatTextField;
    @FXML private Label chatAddPictureButton;
    @FXML private Label scrollDownButton;


    // Chat Interface Initialization, Chat Loading
    public void injectMainUIElements(MainWindowController source) {
        this.mainAnchorPane = source.mainAnchorPane;
    }
    public void injectContactUIElements(MainContactController mainContactController) {
        this.mainContactMessageLabel = mainContactController.mainContactMessageLabel;
        this.mainContactTimeLabel = mainContactController.mainContactTimeLabel;
    }
    public final void initializeChat() throws SQLException, IOException, ExecutionException, InterruptedException {
        initializeChatInterface();
        loadChat();
    }


    // Chat Interface Initialization
    private void initializeChatInterface() throws SQLException {
        removeTitle();
        setChatPosition();
        checkForWrappers();
        setProfilePicture();
        setName();
        applyScrollBarEffect(chatScrollPane);
        setMessageSpacing(5);
        setChatTextFieldFocus();
        scrollToTheBottom();
        setScrollDownButtonListener();
        removeTextFieldContextMenu();
        setAddPictureOnMouseAction();
    }
    private void removeTitle() {
        Set<String> titlesToRemove = new HashSet<>(Arrays.asList("mainTitle", "mainSmallTitle", "logInTitle"));

        List<Label> titles = mainAnchorPane.getChildren().stream()
                .filter(node -> node instanceof Label && titlesToRemove.contains(node.getId())) // Check type and ID
                .map(node -> (Label) node)
                .toList(); // Collect into List<Label>
        mainAnchorPane.getChildren().removeAll(titles);
    }
    private void setChatPosition() {
        chatBackground.setLayoutX(461);
    }
    private void checkForWrappers() {
        // Get all children of mainAnchorPane
        List<Node> children = new ArrayList<>(mainAnchorPane.getChildren());

        // Iterate through the children and remove nodes with IDs starting with "replyWrapper" or "editWrapper"
        for (Node node : children) {
            if (node.getId() != null && (node.getId().startsWith("replyWrapper") || node.getId().startsWith("editWrapper"))) {
                mainAnchorPane.getChildren().remove(node);
            }
        }
    }
    private void setProfilePicture() throws SQLException {
        if (UsersDataBase.getAvatarWithId(contactId) != null) {
            chatMainAvatar.setCursor(Cursor.HAND);
            byte[] blobBytes = UsersDataBase.getAvatarWithId(contactId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(42);
            imageView.setFitWidth(42);
            imageView.setSmooth(true);
            chatMainAvatar.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(21);
            clip.setLayoutY(21);
            clip.setRadius(21);
            chatMainAvatar.setClip(clip);
        }
    }
    private void setName() throws SQLException {
        chatMainName.setText(UsersDataBase.getNameWithId(contactId));
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
    public void scrollToTheBottom() {
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1);
        });
    }
    public void smoothScrollToTheBottom() {
        double durationInSeconds = 0.2;
        double startValue = chatScrollPane.getVvalue(); // Current scroll position
        double distance = 1 - startValue; // How much to scroll

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
    private void setScrollDownButtonListener() {
        // Fade-in Transition (0.2 seconds)
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.2), scrollDownButton);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setCycleCount(1);

        // Fade-out Transition (0.2 seconds)
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.2), scrollDownButton);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);
        fadeOut.setOnFinished(event -> scrollDownButton.setVisible(false)); // Hide after fading out

        chatScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < 1.0 && chatVBox.getHeight() >= 862) { // Scrolled up → Show button
                if (scrollDownButton.getOpacity() == 0) {
                    scrollDownButton.setVisible(true);
                    fadeOut.stop(); // Stop fade-out if it's playing
                    fadeIn.playFromStart();
                }
            } else { // Scrolled down → Hide button
                fadeIn.stop(); // Stop fade-in immediately if it's playing
                if (scrollDownButton.getOpacity() > 0) {
                    fadeOut.playFromStart();
                }
            }
        });
    }
    @FXML
    public void showContactFullAvatar() throws SQLException {
        if ((UsersDataBase.getAvatarWithId(contactId)) != null) {
            Pane backgroundPane = new Pane();
            backgroundPane.setPrefWidth(1920);
            backgroundPane.setPrefHeight(1009);
            backgroundPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.68)");
            mainAnchorPane.getChildren().add(backgroundPane);
            backgroundPane.setOnMouseClicked(clickEvent -> {
                if (clickEvent.getButton() == MouseButton.PRIMARY) {
                    mainAnchorPane.getChildren().remove(backgroundPane);
                }
            });

            Label pictureLabel = new Label();
            pictureLabel.setOnMouseClicked(Event::consume);
            byte[] blobBytes = UsersDataBase.getAvatarWithId(contactId);
            assert blobBytes != null;

            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            Image image = new Image(byteStream);
            ImageView imageView = new ImageView(image);

            // Set max size while preserving aspect ratio
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(Math.min(image.getWidth(), 1520));
            imageView.setFitHeight(Math.min(image.getHeight(), 609));

            imageView.setSmooth(true);
            pictureLabel.setGraphic(imageView);

            // Ensure the layout is updated before centering
            Platform.runLater(() -> {
                double finalWidth = imageView.getBoundsInLocal().getWidth();
                double finalHeight = imageView.getBoundsInLocal().getHeight();

                // Correct centering calculations
                pictureLabel.setLayoutX((1920 - finalWidth) / 2.0);
                pictureLabel.setLayoutY((1009 - finalHeight) / 2.0);
                backgroundPane.getChildren().add(pictureLabel);
            });
        }
    }
    private void removeTextFieldContextMenu() {
        chatTextField.setContextMenu(new ContextMenu());
    }
    private void setAddPictureOnMouseAction() {
        chatAddPictureButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    loadPicture();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }






    // Chat Loading
    private void loadChat() throws SQLException {
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
                case "text" -> loadTextMessage((int)message.get(0));
                case "reply_with_text" -> loadReplyWithTextMessage((int)message.get(0));
            }
        }
    }


    // Message Loading
    public void loadTextMessage(int messageId) throws SQLException, ParseException {
        ArrayList<Object> message = ChatsDataBase.getMessage(messageId);
        int senderId = (int) message.get(1);
        int receiverId = (int) message.get(2);
        String messageText = (String) message.get(3);
        String messageTime = getMessageTime((String) message.get(6));
        String messageFullDate = (String) message.get(6);
        int previousMessageId = ChatsDataBase.getPreviousMessageId(messageId,mainUserId,contactId);
        String previousMessageFullDate = (previousMessageId != -1) ? (String) ChatsDataBase.getMessage(previousMessageId).get(6) : null;

        // Date Label ( if necessary )
        addPotentialDateLabel(messageId);

        HBox messageHBox = new HBox();
        messageHBox.setMinHeight(40);
        messageHBox.setAlignment(senderId == mainUserId ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);
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
                MessageButtons messageButtons = new MessageButtons(mainAnchorPane,chatVBox,chatScrollPane,mainUserId,mainContactMessageLabel,mainContactTimeLabel);
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

        if (avatarIsRequired(messageId,senderId,receiverId)) {
            HBox.setMargin(messageStackPane,(senderId == mainUserId) ? new Insets(0,13, 0,0) : new Insets(0,0,0,13));
            Label avatarLabel = new Label();
            avatarLabel.setId("messageAvatarLabel" + messageId);
            setMessageAvatar(avatarLabel, senderId);
            int index = (senderId == mainUserId) ? messageHBox.getChildren().size() : 0;
            messageHBox.getChildren().add(index, avatarLabel);
            HBox.setMargin(avatarLabel,(senderId == mainUserId) ? new Insets(0,110, 0, 0) : new Insets(0,0,0,110));
        } else {
            if ((int) ChatsDataBase.getMessage(previousMessageId).get(1) == senderId && !messagesHaveOneDayDifference(previousMessageFullDate,messageFullDate)) {
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
    public void loadReplyWithTextMessage(int messageId) throws SQLException, ParseException {
        ArrayList<Object> message = ChatsDataBase.getMessage(messageId);
        int senderId = (int) message.get(1);
        int receiverId = (int) message.get(2);
        String messageText = (String) message.get(3);
        int repliedMessageId = (int) message.get(5);
        String messageTime = getMessageTime((String) message.get(6));
        String messageFullDate = (String) message.get(6);
        int previousMessageId = ChatsDataBase.getPreviousMessageId(messageId,mainUserId,contactId);
        String previousMessageFullDate = (String) ChatsDataBase.getMessage(previousMessageId).get(6);

        // Date Label ( if necessary )
        addPotentialDateLabel(messageId);

        HBox messageHBox = new HBox();
        messageHBox.setAlignment(senderId == mainUserId ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);
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
                MessageButtons messageButtons = new MessageButtons(mainAnchorPane,chatVBox,chatScrollPane,mainUserId,mainContactMessageLabel,mainContactTimeLabel);
                if (senderId == mainUserId) {
                    messageButtons.showMessageButtons(x, y, messageId);
                } else {
                    messageButtons.showMessageReplyButton(x, y, messageId);
                }
            }
        });
        messageHBox.getChildren().add(messageStackPane);

        boolean repliedMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,repliedMessageId);
        StackPane messageReplyPane = new StackPane();
        messageReplyPane.setId("messageReplyStackPane"+repliedMessageId);
        messageReplyPane.setCursor(repliedMessageExists ? Cursor.HAND : Cursor.DEFAULT);
        messageReplyPane.setMinWidth(80);
        messageReplyPane.setPrefHeight(37);
        messageReplyPane.setMaxHeight(37);
        messageReplyPane.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-reply-pane" : "chat-message-contact-reply-pane");
        StackPane.setAlignment(messageReplyPane,Pos.TOP_LEFT);
        StackPane.setMargin(messageReplyPane,new Insets(7,7,0,7));
        messageStackPane.getChildren().add(messageReplyPane);
        if (repliedMessageExists) messageReplyPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                HBox repliedmessageHBox = (HBox) chatVBox.lookup("#messageHBox"+repliedMessageId);
                double hboxPosition = getCenteredScrollPosition(repliedmessageHBox);
                smoothScrollTo(hboxPosition,0.4);
                fadeOutBackgroundColor(repliedmessageHBox);
            }
        });

        // TODO ( reply on picture.. )
        if (repliedMessageExists && (ChatsDataBase.getMessage(repliedMessageId).get(7).equals("text") || ChatsDataBase.getMessage(repliedMessageId).get(7).equals("reply_with_text"))) {
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
        } else {
            Label repliedMessageDeletedMessage = new Label("(deleted message)");
            repliedMessageDeletedMessage.getStyleClass().add((senderId == mainUserId) ? "chat-message-user-deleted-message" : "chat-message-contact-deleted-message");
            StackPane.setAlignment(repliedMessageDeletedMessage,Pos.TOP_LEFT);
            StackPane.setMargin(repliedMessageDeletedMessage,new Insets(10,8,5,8));
            messageReplyPane.getChildren().add(repliedMessageDeletedMessage);
        }


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

        if (avatarIsRequired(messageId,senderId,receiverId)) {
            HBox.setMargin(messageStackPane,(senderId == mainUserId) ? new Insets(0,13, 0,0) : new Insets(0,0,0,13));
            Label avatarLabel = new Label();
            avatarLabel.setId("messageAvatarLabel" + messageId);
            setMessageAvatar(avatarLabel, senderId);
            int index = (senderId == mainUserId) ? messageHBox.getChildren().size() : 0;
            messageHBox.getChildren().add(index, avatarLabel);
            HBox.setMargin(avatarLabel,(senderId == mainUserId) ? new Insets(0,110, 0, 0) : new Insets(0,0,0,110));
        } else {
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
    public void loadReplyWithPictureAndTextMessage(int messageId) throws SQLException, ParseException {
        // Date Label ( if necessary )
        addPotentialDateLabel(messageId);






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
        chatDateLabel.setId("dateLabel");
        chatDateLabel.getStyleClass().add("chat-date-label");
        VBox.setMargin(chatDateLabel,new Insets(8,0,8,0));
        chatVBox.getChildren().add(chatDateLabel);
    }
    private void addPotentialDateLabel(int messageId) throws ParseException, SQLException {
        int previousMessageId = ChatsDataBase.getPreviousMessageId(messageId,mainUserId,contactId);

        String currentMessageFullDate = (String) ChatsDataBase.getMessage(messageId).get(6);
        String previousMessageFullDate = (previousMessageId != -1) ? (String) ChatsDataBase.getMessage(previousMessageId).get(6) : null;

        boolean isDateLabelAlreadyAdded = chatVBox.getChildren().get(chatVBox.getChildren().size()-1).getId().equals("dateLabel");
        if (previousMessageId != -1 && messagesHaveOneDayDifference(previousMessageFullDate,currentMessageFullDate) && !isDateLabelAlreadyAdded) {
            String labelDate = getDateForDateLabel((String) ChatsDataBase.getMessage(messageId).get(6));     // 2. March
            setChatDateLabel(labelDate);
        }
    }


    // Date Operations
    private static String getMessageTime(String fullDate) {
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

        return firstMessageInChat || previousMessageIsFromDifferentSender || previousMessageIsAfterDay;
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







    // Message Sending
    @FXML
    private void saveAndDisplayCurrentTextMessage() throws SQLException, ParseException, IOException {
        String message = chatTextField.getText().trim();
        if (message.isEmpty()) return; // Avoid saving empty messages

        int replyId = getReplyId();
        int editedMessageId = getEditedMessageId();

        if (editedMessageId != -1) {
            updateExistingMessage(editedMessageId, message);
            removeCurrentWrapper();
            changeEditedLastMessage(editedMessageId,message);
            moveBackScrollDownButton();
        } else {
            sendNewMessage(message, replyId);
            moveBackScrollDownButton();
        }

        clearChatInput();
        chatVBox.setPadding(new Insets(0, 0, 20, 0));
    }
    private int getReplyId() {
        return mainAnchorPane.getChildren().stream()
                .map(Node::getId)
                .filter(id -> id != null && id.startsWith("replyWrapper"))
                .map(id -> id.replaceAll("\\D+", ""))
                .filter(num -> !num.isEmpty())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(-1);
    }
    private int getEditedMessageId() {
        return mainAnchorPane.getChildren().stream()
                .map(Node::getId)
                .filter(id -> id != null && id.startsWith("editWrapper"))
                .map(id -> id.substring("editWrapper".length()))
                .map(num -> num.replaceAll("\\D+", ""))
                .filter(num -> !num.isEmpty())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(-1);
    }
    private void updateExistingMessage(int editedMessageId, String message) throws SQLException {
        ChatsDataBase.editMessage(editedMessageId,message,null);

        HBox editedMessageHBox = (HBox) chatVBox.lookup("#messageHBox" + editedMessageId);
        StackPane editedMessageStackPane = (StackPane) editedMessageHBox.lookup("#messageStackPane" + editedMessageId);
        Label messageTextLabel = (Label) editedMessageStackPane.lookup("#messageTextLabel" + editedMessageId);
        messageTextLabel.setText(message);
    }
    private void sendNewMessage(String message, int replyId) throws SQLException, ParseException, IOException {
        int senderId = mainUserId;
        int receiverId = contactId;
        byte[] picture = null;
        String messageTime = getCurrentFullTime();
        String messageType = getCurrentMessageType();
        boolean received = false;

        ensureUserInContacts(receiverId);
        int currentMessageId = ChatsDataBase.addMessage(senderId, receiverId, message, picture, replyId, messageTime, messageType, received);

        displayCurrentTextMessage(currentMessageId);
        updateInteractionTime();
        updateLastMessage(message);
        updateLastMessageTime(getMessageHours(messageTime));

        removeCurrentWrapper();
        scrollToTheBottom();
    }
    private void ensureUserInContacts(int receiverId) throws SQLException {
        int[] contactListOfContact = ContactsDataBase.getContactsIdList(receiverId);
        boolean existsInContactList = Arrays.stream(contactListOfContact).anyMatch(id -> id == mainUserId);
        if (!existsInContactList) {
            ContactsDataBase.addContact(receiverId);
        }
    }
    private void updateInteractionTime() throws SQLException {
        ContactsDataBase.updateInteractionTime(mainUserId,contactId,getCurrentFullTime());
    }
    private void updateLastMessage(String newLastMessage) {
        mainContactMessageLabel.setText(newLastMessage);
    }
    private void updateLastMessageTime(String messageHours) {
        mainContactTimeLabel.setText(messageHours);
    }
    private void clearChatInput() {
        chatTextField.setText("");
    }
    private void displayCurrentTextMessage(int messageId) throws SQLException, ParseException, IOException {
//        String currentTextMessageType = getCurrentMessageType();
//        ChatHistory currentChat = new ChatHistory(mainAnchorPane);

//        switch (currentTextMessageType) {
//            case "text" -> currentChat.loadTextMessage(messageId);
//            case "reply_with_text" -> currentChat.loadReplyWithTextMessage(messageId);
//        }
    }
    private void removeCurrentWrapper() {
        Node wrapper = mainAnchorPane.lookupAll("*").stream()
                .filter(node -> node instanceof Pane && node.getId() != null &&
                        (node.getId().startsWith("replyWrapper") || node.getId().startsWith("editWrapper")))
                .findFirst()
                .orElse(null);

        // Remove the found wrapper if it exists
        if (wrapper != null) {
            mainAnchorPane.getChildren().remove(wrapper);
        }
    }
    private void moveBackScrollDownButton() {
        Label scrollDownButton = (Label) mainAnchorPane.lookup("#scrollDownButton");
        scrollDownButton.setLayoutY(871);
    }
    private void changeEditedLastMessage(int messageId,String newMessage) throws SQLException {
        boolean isLastMessage = (int) ChatsDataBase.getLastMessageWithId(mainUserId,contactId).get(1) == messageId;

        if (isLastMessage) {
            mainContactMessageLabel.setText(newMessage);
        }
    }


    // Picture Sending
    public void loadPicture() throws IOException {
         String chosenPicturePath = openFileChooserAndGetPath();
         //PictureWindow.showWindow(chosenPicturePath);
    }
    protected final String openFileChooserAndGetPath() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(mainAnchorPane.getScene().getWindow());

        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        } else {
            return null;
        }
    }


    // Small Functions
    private String getCurrentFullTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.now().format(formatter);
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
    private String getCurrentMessageType() {
        if (mainAnchorPane.getChildren().stream().anyMatch(node -> node.getId() != null && node.getId().startsWith("replyWrapper"))) {
            return "reply_with_text";
        } else if (mainAnchorPane.getChildren().stream().anyMatch(node -> node.getId() != null && node.getId().startsWith("changeWrapper"))) {
            return "change_with_text";
        } else {
            return "text";
        }
    }



}
