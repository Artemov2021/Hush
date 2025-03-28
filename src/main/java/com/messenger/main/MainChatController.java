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
        Pane replyPane = (Pane) mainAnchorPane.lookup("#reply-wrapper");
        Pane changePane = (Pane) mainAnchorPane.lookup("#reply-wrapper");
        if (replyPane != null) mainAnchorPane.getChildren().remove(replyPane);
        if (changePane != null) mainAnchorPane.getChildren().remove(changePane);
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
        ChatHistory currentChatHistory = new ChatHistory(mainUserId,contactId,chatScrollPane,chatVBox,mainAnchorPane);
        currentChatHistory.load();
    }


    // Message Sending
    @FXML
    public void sendCurrentTextMessage() throws SQLException {
        boolean isCurrentTextMessageEmpty = chatTextField.getText().trim().isEmpty();
        if (!isCurrentTextMessageEmpty) {
            saveAndDisplayCurrentTextMessage();
            clearChatInput();
        }
    }
    private void saveAndDisplayCurrentTextMessage() throws SQLException {
        // Message Information
        int senderId = mainUserId;
        int receiverId = contactId;
        String message = chatTextField.getText().trim();
        byte[] picture = null;
        int replyId = mainAnchorPane.getChildren().stream()
                .map(Node::getId)
                .filter(id -> id != null && id.startsWith("replyWrapper"))
                .map(id -> id.replaceAll("\\D+", "")) // Nur Zahlen extrahieren
                .map(num -> num.isEmpty() ? null : Integer.parseInt(num)) // Falls leer, setze null
                .findFirst()
                .orElse(-1); // Falls kein passendes Element existiert, wird null zurückgegeben
        String messageTime = getCurrentFullTime();
        String messageType = getCurrentMessageType();
        boolean received = false;

        // Check and Add ( if necessary ) User to Contact’s List of Contact
        int[] contactListOfContact = ContactsDataBase.getContactsIdList(contactId);
        boolean existsInContactList = Arrays.stream(contactListOfContact).anyMatch(id -> id == mainUserId);
        if (!existsInContactList) ContactsDataBase.addContact(contactId,mainUserId);

        // Adding Message to DB and Displaying it
        try {
            int currentMessageId = ChatsDataBase.addMessage(senderId,receiverId,message,picture,replyId,messageTime,messageType,received);
            int previousMessageId = ChatsDataBase.getPreviousMessageId(currentMessageId,mainUserId,contactId);
            updateInteractionTime();
            displayCurrentTextMessage(currentMessageId);
            if (previousMessageId != -1) removeLastMessagePadding( previousMessageId);
            scrollToTheBottom();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateInteractionTime() throws SQLException {
        ContactsDataBase.updateInteractionTime(mainUserId,contactId,getCurrentFullTime());
    }
    private void clearChatInput() {
        chatTextField.setText("");
    }
    private void displayCurrentTextMessage(int messageId) throws SQLException, ParseException {
        String currentTextMessageType = getCurrentMessageType();
        ArrayList<Object> currentMessage = ChatsDataBase.getMessage(messageId);
        ChatHistory currentChat = new ChatHistory(mainUserId,contactId,chatScrollPane,chatVBox,mainAnchorPane);

        switch (currentTextMessageType) {
            case "text" -> currentChat.loadTextMessage(currentMessage);
        }
    }
    private void removeLastMessagePadding(int messageId) {
        HBox messageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
        VBox.setMargin(messageHBox,new Insets(0,0,0,0));
    }


    // Small Functions
    private String getCurrentFullTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.now().format(formatter);
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
