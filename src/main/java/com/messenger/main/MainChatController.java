package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import com.messenger.main.chat.ChatHistory;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.scene.Cursor;

import java.text.ParseException;
import java.time.LocalDateTime;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainChatController {
    @FXML
    private Pane chatBackgroundPane;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    public Label chatMainAvatarLabel;
    @FXML
    public Label chatMainNameLabel;
    @FXML
    public VBox chatVBox;
    @FXML
    public TextField chatTextField;


    public AnchorPane mainAnchorPane;
    public int contactId;
    public int mainUserId;
    public Pane mainContactPane;
    public Label mainContactMessageLabel;
    public Label mainContactTimeLabel;
    public Label scrollDownButton;


    // Setting Main Values
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
        this.mainContactTimeLabel = (Label) mainContactPane.lookup("#mainContactTimeLabel");
    }


    // Chat Interface Initialization, Chat Loading
    public void initializeWithValue() throws SQLException, ExecutionException, InterruptedException {
        initializeChatInterface();
        loadChatHistory();
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
        chatBackgroundPane.setLayoutX(461);
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
            chatMainAvatarLabel.setCursor(Cursor.HAND);
            byte[] blobBytes = UsersDataBase.getAvatarWithId(contactId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(42);
            imageView.setFitWidth(42);
            imageView.setSmooth(true);
            chatMainAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(21);
            clip.setLayoutY(21);
            clip.setRadius(21);
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
            if (newVal.doubleValue() < 1.0) { // Scrolled up → Show button
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


    // Chat Loading
    private void loadChatHistory() throws SQLException, ExecutionException, InterruptedException {
        ChatHistory currentChatHistory = new ChatHistory(mainUserId,contactId,chatScrollPane,chatVBox,mainAnchorPane,mainContactMessageLabel,mainContactTimeLabel);
        currentChatHistory.load();
    }


    // Message Sending
    @FXML
    private void saveAndDisplayCurrentTextMessage() throws SQLException, ParseException {
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
    private void sendNewMessage(String message, int replyId) throws SQLException, ParseException {
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
            ContactsDataBase.addContact(receiverId, mainUserId);
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
    private void displayCurrentTextMessage(int messageId) throws SQLException, ParseException {
        String currentTextMessageType = getCurrentMessageType();
        ArrayList<Object> currentMessage = ChatsDataBase.getMessage(messageId);
        ChatHistory currentChat = new ChatHistory(mainUserId,contactId,chatScrollPane,chatVBox,mainAnchorPane,mainContactMessageLabel,mainContactTimeLabel);

        switch (currentTextMessageType) {
            case "text" -> currentChat.loadTextMessage(currentMessage);
            case "reply_with_text" -> currentChat.loadReplyWithTextMessage(currentMessage);
        }
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
