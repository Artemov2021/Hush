package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ChatLoadingCircle;
import com.messenger.design.ScrollPaneEffect;
import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Bounds;
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
import java.util.stream.Collectors;


public class MainChatController extends MainContactController {
    @FXML private Pane chatBackground;
    @FXML protected ScrollPane chatScrollPane;
    @FXML private Label chatMainAvatar;
    @FXML private Label chatMainName;
    @FXML public VBox chatVBox;
    @FXML protected TextField chatTextField;
    @FXML private Label chatAddPictureButton;
    @FXML protected Label scrollDownButton;
    @FXML private Label sendMessageButton;
    @FXML private Label messageSearchingLupe;

    protected byte[] mainUserDataBaseAvatar;
    protected byte[] contactDataBaseAvatar;
    protected ImageView mainUserMessageAvatar;
    protected ImageView contactMessageAvatar;

    private boolean isMessageTooLongVisible = false;
    private ArrayList<Integer> foundMessageIds;

    // Chat Interface Initialization, Chat Loading
    public void setChatContactId(int contactId) {
        this.contactId = contactId;
    }
    public void injectMainUIElements(MainWindowController mainWindowController) {
        this.mainAnchorPane = mainWindowController.mainAnchorPane;
        this.mainContactsVBox = mainWindowController.mainContactsVBox;
    }
    public void injectContactUIElements(MainContactController mainContactController) {
        this.mainContactMessageLabel = mainContactController.mainContactMessageLabel;
        this.mainContactTimeLabel = mainContactController.mainContactTimeLabel;
    }
    public final void initializeChat() throws Exception {
        initializeChatInterface();
        setAvatarsCache();
        loadChat();
        scrollDown();
        setChatLazyLoadingListener();
        //scrollLoadedChatDown();
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
        setScrollDownButtonListener();
        setSendMessageListener();
        removeTextFieldContextMenu();
        setAddPictureOnMouseAction();
        setMessageSearchLupeOnMouseAction();
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
    private void setSendMessageListener() {
        sendMessageButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    validateAndSendMessage();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
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
    private void setMessageSearchLupeOnMouseAction() {
        messageSearchingLupe.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                Node messageSearchOverlay = mainAnchorPane.lookup("#messageSearchOverlay");
                if (messageSearchOverlay != null) {
                    mainAnchorPane.getChildren().remove(messageSearchOverlay);
                } else {
                    showMessageSearchingField();
                }
            }
        });
    }


    // Chat Interface Initialization - FXML functions
    @FXML
    public void showContactFullAvatar() throws SQLException {
        if ((UsersDataBase.getAvatarWithId(contactId)) != null) {
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

            ByteArrayInputStream byteStream = new ByteArrayInputStream(Objects.requireNonNull(UsersDataBase.getAvatarWithId(contactId)));
            Image image = new Image(byteStream);
            ImageView imageView = new ImageView(image);

            // Set max size while preserving aspect ratio
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(Math.min(image.getWidth(), 1520));
            imageView.setFitHeight(Math.min(image.getHeight(), 609));

            imageView.setSmooth(true);
            fullyPicturePreview.setGraphic(imageView);

            // Ensure the layout is updated before centering
            Platform.runLater(() -> {
                // Recalculate layout position after image rendering
                double initialWidth = imageView.getLayoutBounds().getWidth();
                double initialHeight = imageView.getLayoutBounds().getHeight();

                // Correct centering calculations
                fullyPicturePreview.setLayoutX((1920 - initialWidth) / 2.0);
                fullyPicturePreview.setLayoutY((1009 - initialHeight) / 2.0);

                backgroundPane.getChildren().add(fullyPicturePreview);
            });
        }
    }


    // Chat Loading
    private void loadChat() throws Exception {
        boolean chatIsEmpty = ChatsDataBase.getAllMessages(mainUserId, contactId).isEmpty();

        if (chatIsEmpty) {
            setCurrentDateLabel();
        } else {
            loadChatHistory();
        }
    }
    public void loadChatHistory() throws Exception {
        List<ChatMessage> allMessages = new ArrayList<>(ChatsDataBase.getAllMessages(mainUserId,contactId));
        List<Node> firstNodesToLoad = getFirstChatNodesToLoad(allMessages);

        chatVBox.getChildren().addAll(firstNodesToLoad);


//        chatVBox.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
//            System.out.println("Height of the VBox after layout: " + newBounds.getHeight());
//        });
    }
    private List<Node> getFirstChatNodesToLoad(List<ChatMessage> allMessages) throws Exception {
        int maxHeight = 1600;
        double totalHeight = 0;

        List<Node> firstNodes = new ArrayList<>();

        for (int i = allMessages.size()-1;i >= 0;i--) {
            ChatMessage message = allMessages.get(i);
            if (totalHeight < maxHeight) {
                HBox loadedMessage = message.load(this,allMessages);
                int nodeHeight = getMessageHeight(loadedMessage);

                firstNodes.addFirst(loadedMessage);
                totalHeight += nodeHeight;

                if (isDateLabelRequired(allMessages,message)) {
                    String messageFullDate = message.time;
                    String labelDate = getDateForDateLabel(messageFullDate);
                    firstNodes.addFirst(getChatDateLabel(labelDate,messageFullDate));
                    short dateLabelHeight = 27;
                    totalHeight += dateLabelHeight;
                }
            } else {
                break;
            }
        }

        return firstNodes;
    }


    // Chat Loading: Small Functions
    private void setCurrentDateLabel() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
        String formattedDate = today.format(formatter);
        setChatDateLabel(formattedDate);
    }
    private void setChatDateLabel(String date) {
        Label chatDateLabel = new Label(date);
        chatDateLabel.setId("dateLabel"+getLabelIdCurrentDate(getCurrentFullTime()));
        chatDateLabel.getStyleClass().add("chat-date-label");
        VBox.setMargin(chatDateLabel,new Insets(8,0,8,0));
        chatVBox.getChildren().add(chatDateLabel);
    }
    private void setAvatarsCache() throws SQLException {
        mainUserDataBaseAvatar = UsersDataBase.getAvatarWithId(mainUserId);
        contactDataBaseAvatar = UsersDataBase.getAvatarWithId(contactId);
    }
    private String getLabelIdCurrentDate(String messageTime) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(messageTime, inputFormatter);

        return dateTime.toLocalDate().toString(); // Outputs in yyyy-MM-dd format
    }
    private Label getChatDateLabel(String date,String messageFullDate) {
        Label chatDateLabel = new Label(date);
        chatDateLabel.setId("dateLabel"+getLabelIdCurrentDate(messageFullDate));
        chatDateLabel.getStyleClass().add("chat-date-label");
        VBox.setMargin(chatDateLabel,new Insets(8,0,8,0));
        return chatDateLabel;
    }
    private boolean isDateLabelRequired(List<ChatMessage> allMessages,ChatMessage message) throws SQLException, ParseException {
        ChatMessage previousMessage = getPreviousMessage(allMessages,message);
        boolean previousMessageExists = (previousMessage != null);
        String previousMessageTime = previousMessageExists ? previousMessage.time : null;

        boolean isFirstMessage = ChatsDataBase.getFirstMessageId(mainUserId,contactId) == message.id;
        boolean isPreviousMessageOneDay = previousMessageExists && messagesHaveOneDayDifference(previousMessageTime,message.time);
        //System.out.println("message id: "+message.id+", previous message one day: "+isPreviousMessageOneDay+", message time: "+message.time+", previous message time: "+previousMessageTime+"\n\n");
        return isFirstMessage || isPreviousMessageOneDay;
    }
    private ChatMessage getPreviousMessage(List<ChatMessage> allMessages,ChatMessage message) {
        int messageIndex = -1;
        for (int i = 0;i <= allMessages.size()-1;i++) {
            if (allMessages.get(i).id == message.id) {
                messageIndex = i;
                break;
            }
        }

        if (messageIndex > 0) {
            return allMessages.get(messageIndex - 1);
        } else {
            return null; // No next object exists (either not found or it's the first message)
        }
    }


    // Chat Lazy Loading
    private void setChatLazyLoadingListener() {
        chatScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 0.0 && !chatVBox.getChildren().isEmpty()) {
                try {
                    boolean hasMoreMessages = hasMoreMessages();
                    if (hasMoreMessages) {
                        loadMoreMessagesUp();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    // Message Sending
    @FXML
    private void validateAndSendMessage() throws Exception {
        boolean isMessageValid = getCurrentMessageValidity();

        if (isMessageValid) {
            sendCurrentMessage();
        }
    }
    private void sendCurrentMessage() throws Exception {
        TextMessageType currentMessageType = getCurrentMessageType();

        try {
            if (currentMessageType == TextMessageType.EDIT_WITH_TEXT) {
                processMessageEdit();
            } else {
                processMessageSend();
            }

            finalizeMessageFlow();
        } catch (IllegalArgumentException e) {
            showMessageTooLongException();
        }

    }


    // Small Functions
    private void scrollDown() {
        chatScrollPane.setVvalue(1.0);
    }
    private void processMessageEdit() throws SQLException {
        editChosenMessage();
        updatePotentialLastMessage();
    }
    private void processMessageSend() throws Exception {
        ensureUserInContacts(contactId);
        insertAndDisplayMessage();
        updateInteractionTime();
        updateLastMessage();
        updateLastMessageTime();
        moveContactPaneUp();
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
    private void finalizeMessageFlow() {
        removePotentialWrapper();
        moveBackScrollDownButton();
        updateScrollDownButtonVisibility();
        clearChatInput();
    }
    private void insertAndDisplayMessage() throws Exception {
        int currentMessageId = insertCurrentMessageIntoDB();
        if (currentMessageId != -1) {
            displayCurrentMessage(currentMessageId);
        }
    }
    private boolean getCurrentMessageValidity() {
        String currentMessage = chatTextField.getText().trim();
        return !currentMessage.isEmpty();
    }
    private void editChosenMessage() throws SQLException {
       editChosenMessageInDB();
       editChosenMessageInChat();
    }
    private void editChosenMessageInDB() throws SQLException {
        String currentMessage = chatTextField.getText().trim();
        int chosenMessageId = getEditWrapperId();
        String oldMessageType = ChatsDataBase.getMessage(mainUserId,contactId,chosenMessageId).type;

        boolean isMessageTooLong = (currentMessage.length() >= 1000);
        if (isMessageTooLong) {
            throw new IllegalArgumentException();
        } else {
            ChatsDataBase.editMessage(chosenMessageId, currentMessage, null, oldMessageType);
        }
    }
    private void editChosenMessageInChat() {
        String currentMessage = chatTextField.getText().trim();
        int chosenMessageId = getEditWrapperId();

        HBox chosenHBox = (HBox) chatVBox.lookup("#messageHBox"+chosenMessageId);
        StackPane chosenStackPane = (StackPane) chosenHBox.lookup("#messageStackPane"+chosenMessageId);
        Label chosenMessageTextLabel = (Label) chosenStackPane.lookup("#messageTextLabel"+chosenMessageId);
        chosenMessageTextLabel.setText(currentMessage);
    }
    private void updatePotentialLastMessage() throws SQLException {
        int chosenMessageId = getEditWrapperId();
        boolean isLastMessage = chosenMessageId == ChatsDataBase.getLastMessageId(mainUserId,contactId);

        if (isLastMessage) {
            updateLastMessage();
        }
    }
    private int insertCurrentMessageIntoDB() throws SQLException {
        String currentMessage = chatTextField.getText().trim();

        int senderId = mainUserId;
        int receiverId = contactId;
        String message = currentMessage;
        byte[] picture = null;
        int replyMessageId = getReplyWrapperId();
        String messageTime = getCurrentFullTime();
        String messageType = getConvertedCurrentDBMessageType();
        boolean received = false;

        boolean isMessageTooLong = (message.length() >= 1000);
        if (isMessageTooLong) {
            throw new IllegalArgumentException();
        } else {
            return ChatsDataBase.addMessage(senderId,receiverId,message,picture,replyMessageId,messageTime,messageType,received);
        }
    }
    private void ensureUserInContacts(int contactId) throws SQLException {
        int[] contactListOfContact = ContactsDataBase.getContactsIdList(contactId);
        boolean existsInContactList = Arrays.stream(contactListOfContact).anyMatch(id -> id == mainUserId);
        if (!existsInContactList) {
            ContactsDataBase.addContact(contactId,mainUserId);
        }
    }
    private void displayCurrentMessage(int messageId) throws Exception {
        ChatMessage currentMessage = ChatsDataBase.getMessage(mainUserId,contactId,messageId);
        chatVBox.getChildren().add(currentMessage.render(this));
    }
    private int getReplyWrapperId() {
        return mainAnchorPane.getChildren().stream()
                .map(Node::getId)
                .filter(id -> id != null && id.startsWith("replyWrapper"))
                .map(id -> id.replaceAll("\\D+", ""))
                .filter(num -> !num.isEmpty())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(-1);
    }
    private int getEditWrapperId() {
        return mainAnchorPane.getChildren().stream()
                .map(Node::getId)
                .filter(id -> id != null && id.startsWith("editWrapper"))
                .map(id -> id.replaceAll("\\D+", ""))
                .filter(num -> !num.isEmpty())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(-1);
    }
    enum TextMessageType {
        REPLY_WITH_TEXT,
        EDIT_WITH_TEXT,
        TEXT
    }
    private TextMessageType getCurrentMessageType() {
        if (mainAnchorPane.getChildren().stream().anyMatch(node -> node.getId() != null && node.getId().startsWith("replyWrapper"))) {
            return TextMessageType.REPLY_WITH_TEXT;
        } else if (mainAnchorPane.getChildren().stream().anyMatch(node -> node.getId() != null && node.getId().startsWith("editWrapper"))) {
            return TextMessageType.EDIT_WITH_TEXT;
        } else {
            return TextMessageType.TEXT;
        }
    }
    private String getConvertedCurrentDBMessageType() throws SQLException {
        TextMessageType currentMessageType = getCurrentMessageType();

        if (currentMessageType == TextMessageType.REPLY_WITH_TEXT) {
            return "reply_with_text";
        } else {
            return "text";
        }
    }
    private void removePotentialWrapper() {
        Node wrapper = mainAnchorPane.lookupAll("*").stream()
                .filter(node -> node instanceof Pane && node.getId() != null &&
                        (node.getId().startsWith("replyWrapper") || node.getId().startsWith("editWrapper")))
                .findFirst()
                .orElse(null);

        // Remove the found wrapper if it exists
        if (wrapper != null) {
            mainAnchorPane.getChildren().remove(wrapper);
        }
        chatVBox.setPadding(new Insets(0, 0, 20, 0));
    }
    private void updateInteractionTime() throws SQLException {
        ContactsDataBase.updateInteractionTime(mainUserId,contactId,getCurrentFullTime());
    }
    private void updateLastMessage() {
        String currentMessage = chatTextField.getText().trim();
        mainContactMessageLabel.setStyle("");
        mainContactMessageLabel.getStyleClass().clear();
        mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
        mainContactMessageLabel.setText(currentMessage);
    }
    private void updateLastMessageTime() {
        mainContactTimeLabel.setText(getMessageHours(getCurrentFullTime()));
    }
    private void clearChatInput() {
        chatTextField.clear();
    }
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
    private void moveBackScrollDownButton() {
        Label scrollDownButton = (Label) mainAnchorPane.lookup("#scrollDownButton");
        scrollDownButton.setLayoutY(871);
    }
    private void updateScrollDownButtonVisibility() {
        // Fade-out Transition (0.2 seconds)
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.2), scrollDownButton);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);
        fadeOut.setOnFinished(event -> scrollDownButton.setVisible(false)); // Hide after fading out

        if (scrollDownButton.getOpacity() > 0) {
            fadeOut.playFromStart();
        }
    }
    private void moveContactPaneUp() {
        AnchorPane contactAnchorPane = (AnchorPane) mainContactsVBox.lookup("#mainContactAnchorPane"+contactId);
        mainContactsVBox.getChildren().remove(contactAnchorPane);
        mainContactsVBox.getChildren().add(0,contactAnchorPane);
    }
    private boolean hasMoreMessages() throws SQLException {
        int lastMessageId = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .findFirst()
                .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                .get();
        boolean hasMoreMessages = ChatsDataBase.hasMoreMessages(mainUserId,contactId,lastMessageId);
        return hasMoreMessages;
    }
    private int getMessageHeight(HBox message) {
        VBox dummyVBox = new VBox();
        mainAnchorPane.getChildren().add(dummyVBox);
        dummyVBox.setVisible(false);

        dummyVBox.getChildren().add(message);
        message.applyCss();
        message.autosize();
        int nodeHeight = (int) message.getBoundsInParent().getHeight();
        mainAnchorPane.getChildren().remove(dummyVBox);

        return nodeHeight;
    }
    private void adjustScrollPosition(double oldChatVBoxHeight) {
        Platform.runLater(() -> {
            double newHeight = chatVBox.getHeight(); // Height after adding
            double delta = newHeight - oldChatVBoxHeight;

            // Scroll down by the exact pixel height of inserted content
            chatScrollPane.setVvalue(
                    chatScrollPane.getVvalue() + delta / (newHeight - chatScrollPane.getViewportBounds().getHeight())
            );
        });
    }
    private void loadMoreMessagesUp() throws Exception {
        int maxHeight = 8000; // max additional height of new loaded messages
        int totalHeight = 0;

        int lastMessageId = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .findFirst()
                .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                .get();
        List<ChatMessage> allMessages = new ArrayList<>(ChatsDataBase.getAllMessages(mainUserId,contactId));
        List<ChatMessage> allLeftMessages = new ArrayList<>(ChatsDataBase.getAllLeftMessages(mainUserId,contactId,lastMessageId));
        List<Node> nodesToLoad = new ArrayList<>();

        for (int i = allLeftMessages.size()-1;i >= 0;i--) {
            ChatMessage message = allLeftMessages.get(i);
            if (totalHeight < maxHeight) {
                HBox loadedMessage = message.load(this,allMessages);
                int nodeHeight = getMessageHeight(loadedMessage);

                nodesToLoad.addFirst(loadedMessage);
                totalHeight += nodeHeight;

                if (isDateLabelRequired(allMessages,message)) {
                    String messageFullDate = message.time;
                    String labelDate = getDateForDateLabel(messageFullDate);
                    nodesToLoad.addFirst(getChatDateLabel(labelDate,messageFullDate));
                    short dateLabelHeight = 27;
                    totalHeight += dateLabelHeight;
                }
            } else {
                break;
            }
        }

        double oldHeight = chatVBox.getHeight(); // Height before adding

        chatVBox.getChildren().addAll(0, nodesToLoad);

        adjustScrollPosition(oldHeight);
    }
    private void showMessageTooLongException() {
        if (isMessageTooLongVisible) {
            return; // Prevent multiple alerts
        }

        boolean hasWrapper = mainAnchorPane.getChildren().stream()
                .map(Node::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .stream()
                .anyMatch(id -> id.startsWith("#eplyWrapper")) ||
                mainAnchorPane.getChildren().stream()
                        .map(Node::getId)
                        .filter(Objects::nonNull)
                        .anyMatch(id -> id.startsWith("editWrapper"));

        Label errorMessage = new Label("Text is too long!");
        errorMessage.getStyleClass().add("chat-message-too-long-exception-label");
        errorMessage.setLayoutX(1137);
        errorMessage.setLayoutY(hasWrapper ? 850 : 905);
        errorMessage.setTranslateY(30);
        errorMessage.getStylesheets().add(getClass().getResource("/main/css/MainChat.css").toExternalForm());
        mainAnchorPane.getChildren().add(errorMessage);

        isMessageTooLongVisible = true;

        byte moveDistance = 15;
        errorMessage.setOpacity(0);
        errorMessage.setTranslateY(moveDistance);

        // Slide & fade in
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), errorMessage);
        slideIn.setFromY(moveDistance);
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), errorMessage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Slide & fade out
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), errorMessage);
        slideOut.setFromY(0);
        slideOut.setToY(moveDistance);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), errorMessage);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Play fade/slide in
        errorMessage.setVisible(true);
        slideIn.play();
        fadeIn.play();

        // After 2 seconds, fade and slide out, then remove the label
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            slideOut.play();
            fadeOut.play();

            // Remove the node from the UI after the transition ends
            fadeOut.setOnFinished(event -> {
                mainAnchorPane.getChildren().remove(errorMessage);
                isMessageTooLongVisible = false; // Reset flag only when it disappears
            });
        });

        delay.play();
    }
    private void showMessageSearchingField() {
        Pane overlay = new Pane();
        overlay.setId("messageSearchOverlay");
        overlay.setPrefWidth(457);
        overlay.setPrefHeight(70);
        overlay.setLayoutX(1461);
        overlay.setLayoutY(81);
        overlay.getStyleClass().add("chat-message-search-overlay");
        Platform.runLater(() -> {
            overlay.getScene().getStylesheets().add(
                    getClass().getResource("/main/css/MainChat.css").toExternalForm()
            );
        });
        mainAnchorPane.getChildren().add(overlay);

        TextField messageSearchTextField = new TextField();
        messageSearchTextField.setPromptText("Search for a word...");
        messageSearchTextField.setPrefWidth(270);
        messageSearchTextField.setPrefHeight(46);
        messageSearchTextField.setLayoutX(14);
        messageSearchTextField.setLayoutY(11);
        messageSearchTextField.getStyleClass().add("chat-message-search-field");
        overlay.getChildren().add(messageSearchTextField);
        PauseTransition pause = new PauseTransition(Duration.millis(400));
        pause.setOnFinished(event -> {
            try {
                String targetTrimmedMessageText = messageSearchTextField.getText().trim().toLowerCase();
                foundMessageIds = ChatsDataBase.getFoundMessageIds(mainUserId,contactId,targetTrimmedMessageText);
                boolean messagesExist = !foundMessageIds.isEmpty();
                if (messagesExist && !targetTrimmedMessageText.isEmpty()) {
                    loadChatWithFoundMessage(foundMessageIds.getFirst(),targetTrimmedMessageText);
                    changeCounter(1,foundMessageIds.size());
                } else {
                    changeCounter(0,0);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Platform.runLater(messageSearchTextField::requestFocus);
        messageSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isNewValueDifferent = !oldValue.trim().equals(newValue.trim());
            boolean isNewValueEmpty = newValue.trim().isEmpty();
            if (isNewValueDifferent && !isNewValueEmpty) {
                pause.playFromStart();
            }
        });

        Label counter = new Label("0/0");
        counter.setId("messageSearchCounter");
        counter.setAlignment(Pos.CENTER_RIGHT);
        counter.setPrefWidth(45);
        counter.setLayoutY(26);
        counter.setLayoutX(225);
        counter.getStyleClass().add("chat-message-search-counter");
        overlay.getChildren().add(counter);

        Label upButton = new Label();
        upButton.setPrefWidth(42);
        upButton.setPrefHeight(42);
        upButton.setLayoutX(291);
        upButton.setLayoutY(16);
        upButton.getStyleClass().add("chat-message-search-up-button");
        upButton.setCursor(Cursor.HAND);
        overlay.getChildren().add(upButton);

        Label downButton = new Label();
        downButton.setPrefWidth(42);
        downButton.setPrefHeight(42);
        downButton.setLayoutX(330);
        downButton.setLayoutY(16);
        downButton.getStyleClass().add("chat-message-search-down-button");
        downButton.setCursor(Cursor.HAND);
        overlay.getChildren().add(downButton);

        Label exitButton = new Label();
        exitButton.setPrefWidth(42);
        exitButton.setPrefHeight(42);
        exitButton.setLayoutX(408);
        exitButton.setLayoutY(16);
        exitButton.getStyleClass().add("chat-message-search-exit-button");
        exitButton.setCursor(Cursor.HAND);
        overlay.getChildren().add(exitButton);
        exitButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                mainAnchorPane.getChildren().remove(overlay);
            }
        });
    }
    private void clearChat() {
        chatVBox.getChildren().clear();
    }
    private void changeCounter(int currentMessage,int foundMessages) {
        Label counter = (Label) mainAnchorPane.lookup("#messageSearchCounter");
        counter.setText(String.format("%d/%d",currentMessage,foundMessages));
    }
    private void loadChatWithFoundMessage(int foundMessageId,String targetMessage) throws Exception {
        clearChat();
        ArrayList<Node> newChatNode = new ArrayList<>();
        ArrayList<ChatMessage> allMessages = ChatsDataBase.getAllMessages(mainUserId,contactId);

        int maxPreviousHeight = 1000;
        int previousTotalHeight = 0;
        List<ChatMessage> previousMessages = ChatsDataBase.getAllLeftMessages(mainUserId,contactId,foundMessageId);
        for (int i = previousMessages.size()-1;i >= 0;i--) {
            ChatMessage message = previousMessages.get(i);
            if (previousTotalHeight < maxPreviousHeight) {
                HBox loadedMessage = message.load(this,allMessages);
                int nodeHeight = getMessageHeight(loadedMessage);

                newChatNode.addFirst(loadedMessage);
                previousTotalHeight += nodeHeight;

                if (isDateLabelRequired(allMessages,message)) {
                    String messageFullDate = message.time;
                    String labelDate = getDateForDateLabel(messageFullDate);
                    newChatNode.addFirst(getChatDateLabel(labelDate,messageFullDate));
                    short dateLabelHeight = 27;
                    previousTotalHeight += dateLabelHeight;
                }
            } else {
                break;
            }
        }

        ChatMessage foundMessage = ChatsDataBase.getMessage(mainUserId,contactId,foundMessageId);
        HBox loadedFoundMessage = foundMessage.load(this,allMessages);
        if (isDateLabelRequired(allMessages,foundMessage)) {
            String messageFullDate = foundMessage.time;
            String labelDate = getDateForDateLabel(messageFullDate);
            newChatNode.add(getChatDateLabel(labelDate,messageFullDate));
        }
        newChatNode.add(loadedFoundMessage);

        int maxNextHeight = 1000;
        int nextTotalHeight = 0;
        List<ChatMessage> nextMessages = ChatsDataBase.getNextMessages(mainUserId,contactId,foundMessageId);
        for (int i = 0;i <= nextMessages.size()-1;i++) {
            ChatMessage message = nextMessages.get(i);
            if (nextTotalHeight < maxNextHeight) {
                if (isDateLabelRequired(allMessages,message)) {
                    String messageFullDate = message.time;
                    String labelDate = getDateForDateLabel(messageFullDate);
                    newChatNode.add(getChatDateLabel(labelDate,messageFullDate));
                    short dateLabelHeight = 27;
                    previousTotalHeight += dateLabelHeight;
                }

                HBox loadedMessage = message.load(this,allMessages);
                int nodeHeight = getMessageHeight(loadedMessage);

                newChatNode.add(loadedMessage);
                nextTotalHeight += nodeHeight;
            } else {
                break;
            }
        }

        chatVBox.getChildren().addAll(newChatNode);

        Platform.runLater(() -> {
            double newChatPosition = getCenteredScrollPosition(loadedFoundMessage);
            System.out.println(newChatPosition);
            chatScrollPane.setVvalue(newChatPosition);
        });
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


    // Picture Sending
    public void loadPicture() throws IOException {
         String chosenPicturePath = openFileChooserAndGetPath();
         PictureWindow pictureWindow = new PictureWindow(this,chosenPicturePath);
         if (chosenPicturePath != null) pictureWindow.showWindow();

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

}
