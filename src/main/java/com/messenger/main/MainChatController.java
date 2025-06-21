package com.messenger.main;

import com.messenger.database.*;
import com.messenger.design.ScrollPaneEffect;
import javafx.animation.*;
import javafx.application.Platform;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    List<ChatMessage> allMessages;

    boolean isChatInitialized = false;

    private int lastContactActionId;

    private ScheduledExecutorService messageListenerExecutor;
    private int firstMessageId;

    protected byte[] mainUserDataBaseAvatar;
    protected byte[] contactDataBaseAvatar;
    protected ImageView mainUserMessageAvatar;
    protected ImageView contactMessageAvatar;

    private boolean isMessageTooLongVisible = false;
    private boolean suppressLazyLoading = false;

    private boolean isMessageSearchOverlayVisible = false;
    private ArrayList<Integer> foundMessageIds;
    private int currentHighlightMessageId = -1;

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
        this.mainContactMessageCounterLabel = mainContactController.mainContactMessageCounterLabel;
    }
    public final void initializeChat() throws Exception {
        initializeChatInterface();
        setAvatarsCache();
        loadChat();
        scrollDown();
        setChatLazyLoadingListener();
        deleteMessageSearchOverlay();
        setMessageSearchLupeOnMouseAction();
        setMessageListener();
        setAllMessagesRead();
        deleteNewMessagesCounter();
        resetPotentialNewMessagesIcon();
        setLastContactsAction();
        isChatInitialized = true;
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
    }
    private void removeTitle() {
        Set<String> titlesToRemove = new HashSet<>(Arrays.asList("mainTitle", "mainSmallTitle", "logInTitle"));

        List<Label> titles = mainAnchorPane.getChildren().stream()
                .filter(node -> node instanceof Label && titlesToRemove.contains(node.getId())) // Check type and ID
                .map(node -> (Label) node)
                .toList(); // Collect into List<Label>

        for (Label title: titles) {
            title.setVisible(false);
        }

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
    public void smoothScrollToTheBottomOrLoad() throws Exception {
        int lastMessageId = ChatsDataBase.getLastMessageId(mainUserId,contactId);
        int lastMessageInChatId = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .reduce((first, second) -> second) // gets the last HBox
                .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                .orElse(-1);
        boolean isLastMessageLoaded = (lastMessageInChatId == lastMessageId);

        if (isLastMessageLoaded) {
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
        } else {
            List<ChatMessage> allMessages = new ArrayList<>(ChatsDataBase.getAllMessages(mainUserId,contactId));
            List<Node> firstNodesToLoad = getFirstChatNodesToLoad(allMessages);

            chatVBox.getChildren().clear();
            chatVBox.getChildren().addAll(firstNodesToLoad);

            PauseTransition delay = new PauseTransition(Duration.millis(50));
            delay.setOnFinished(event -> {
                Node lastNode = chatVBox.getChildren().getLast();
                lastNode.requestFocus(); // Optionally bring focus
                chatScrollPane.setVvalue(chatScrollPane.getVmax());
            });
            delay.play();
        }
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
    private void deleteMessageSearchOverlay() {
        Node messageSearchOverlay = mainAnchorPane.lookup("#messageSearchOverlay");
        mainAnchorPane.getChildren().remove(messageSearchOverlay);
    }
    private void setMessageSearchLupeOnMouseAction() {
        messageSearchingLupe.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                Node messageSearchOverlay = mainAnchorPane.lookup("#messageSearchOverlay");
                if (messageSearchOverlay != null) {
                    mainAnchorPane.getChildren().remove(messageSearchOverlay);
                } else {
                    try {
                        showMessageSearchingField();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
    private void setAllMessagesRead() throws SQLException {
        ChatsDataBase.setAllMessagesRead(mainUserId,contactId);
    }
    private void deleteNewMessagesCounter() {
        mainContactMessageCounterLabel.setText("0");
        mainContactMessageCounterLabel.setVisible(false);
    }
    private void resetPotentialNewMessagesIcon() throws SQLException {
        if (!ChatsDataBase.isThereUnreadMessages(mainUserId)) {
            Stage currentStage = (Stage) mainAnchorPane.getScene().getWindow();
            currentStage.getIcons().clear();
            currentStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/icon.png")));
        }
    }
    private void setLastContactsAction() throws SQLException {
        lastContactActionId = LogsDataBase.getLastContactActionId(mainUserId,contactId);
    }

    // Shut Down Background Thread
    public void shutdown() {
        if (messageListenerExecutor != null && !messageListenerExecutor.isShutdown()) {
            messageListenerExecutor.shutdown(); // Stop accepting new tasks
            try {
                if (!messageListenerExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    messageListenerExecutor.shutdownNow(); // Force shutdown if not finished
                }
            } catch (InterruptedException e) {
                messageListenerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }


    // Message Listener
    private void setMessageListener() {
        messageListenerExecutor = Executors.newSingleThreadScheduledExecutor();

        messageListenerExecutor.scheduleAtFixedRate(() -> {
            try {
                if (isChatInitialized) {
                    checkForNewAction();
                }
            } catch (Exception e) {
                e.printStackTrace(); // Or use logging
            }
        }, 0, 2, TimeUnit.SECONDS); // Initial delay 0, repeat every 2 seconds
    }

    private void checkForNewAction() throws SQLException {
        int updatedLastContactActionId = LogsDataBase.getLastContactActionId(mainUserId,contactId);
        if (lastContactActionId != updatedLastContactActionId) {
            ArrayList<Integer> newActionIds = LogsDataBase.getNewContactActionIds(mainUserId,contactId,lastContactActionId);
            for (int actionId: newActionIds) {
                Platform.runLater(() -> {
                    try {
                        displayAction(actionId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            lastContactActionId = updatedLastContactActionId;
        }
    }
    private void displayAction(int actionId) throws Exception {
        Action action = new Action(actionId);
        if (action.receiver_id == mainUserId) {
            switch (action.change_type) {
                case ActionType.NEW -> displayNewMessage(action);
                case ActionType.EDITED -> displayEditedMessage(action);
                case ActionType.DELETED -> displayDeletedMessage(action);
                default -> throw new RuntimeException();
            }
        }
    }
    private void displayNewMessage(Action action) {
        Platform.runLater(() -> {
            try {
                ChatMessage newMessage = ChatsDataBase.getMessage(mainUserId,contactId,action.message_id);
                loadNewMessage(newMessage);
                makeMessageRead(newMessage);
                allMessages.add(newMessage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void displayEditedMessage(Action action) throws Exception {
        ChatMessage message = ChatsDataBase.getMessage(mainUserId,contactId,action.message_id);
        boolean isMessageLoaded = chatVBox.lookup("#messageHBox"+message.id) != null;
        if (isMessageLoaded) {
            message.reload(this);
        }

        for (int i = 0;i < allMessages.size();i++) {
            if (allMessages.get(i).id == message.id) {
                allMessages.remove(i);
                allMessages.add(i,message);
                break;
            }
        }
    }
    private void displayDeletedMessage(Action action) throws SQLException {
        moveMessageAvatarBack(action.message_id,action.sender_id);
        deleteDateLabel(action.message_id,action.message_time);
        removeMessageHBox(action.message_id);

        for (int i = 0;i < allMessages.size();i++) {
            if (allMessages.get(i).id == action.message_id) {
                allMessages.remove(i);
                break;
            }
        }
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
        allMessages = new ArrayList<>(ChatsDataBase.getAllMessages(mainUserId,contactId));
        List<Node> firstNodesToLoad = getFirstChatNodesToLoad(allMessages);

        chatVBox.getChildren().addAll(firstNodesToLoad);
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
    private void loadNewMessage(ChatMessage newMessage) throws Exception {
        if (newMessage.sender_id == mainUserId) {
            return;
        }

        if (isDateLabelRequired(allMessages,newMessage)) {
            String messageFullDate = newMessage.time;
            String labelDate = getDateForDateLabel(messageFullDate);
            chatVBox.getChildren().add(getChatDateLabel(labelDate,messageFullDate));
        }

        try {
            HBox loadedMessage = newMessage.render(this);
            chatVBox.getChildren().add(loadedMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private void makeMessageRead(ChatMessage message) throws SQLException {
        ChatsDataBase.setMessageRead(message.id);
    }
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
    private void setChatLazyLoadingListener() throws SQLException {
        updateFirstMessageIdEverySecond();
        chatScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressLazyLoading) return;
            try {
                int firstChatMessageId = chatVBox.getChildren().stream()
                        .filter(node -> node instanceof HBox)
                        .findFirst()
                        .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                        .orElse(-1);


                if (newVal.doubleValue() == 0.0 && !chatVBox.getChildren().isEmpty()) {
                    boolean hasMorePreviousMessages = hasMorePreviousMessages();
                    if (hasMorePreviousMessages) {
                        loadMoreMessagesUp();
                    }
                } else if (newVal.doubleValue() == 1.0 && !chatVBox.getChildren().isEmpty()) {
                    boolean hasMoreNextMessages = hasMoreNextMessages();
                    if (hasMoreNextMessages) {
                        loadMoreMessagesDown();
                    }
                }
                if (isMessageSearchOverlayVisible && newVal.doubleValue() == 0.0 && firstChatMessageId == firstMessageId) {
                    moveFirstMessageDown();
                } else if (isMessageSearchOverlayVisible && firstChatMessageId == firstMessageId) {
                    moveFirstMessageUp();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }
    private void updateFirstMessageIdEverySecond() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            try {
                firstMessageId = ChatsDataBase.getFirstMessageId(mainUserId, contactId);
            } catch (Exception e) {
                e.printStackTrace(); // Or use a logger
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE); // Runs forever
        timeline.play();
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
    private void updateLastMessage(String message) {
        mainContactMessageLabel.setStyle("");
        mainContactMessageLabel.getStyleClass().clear();
        mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
        mainContactMessageLabel.setText(message);
    }
    private void scrollDown() {
        chatScrollPane.setVvalue(1.0);
    }
    private void processMessageEdit() throws Exception {
        editChosenMessage();
        updatePotentialLastEditedMessage(chatTextField.getText().trim());
    }
    private void processMessageSend() throws Exception {
        ensureUserInContacts(contactId);
        insertAndDisplayMessage();
        updateInteractionTime();
        updateLastMessage(chatTextField.getText().trim());
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
            updateChangesLog(currentMessageId, ActionType.NEW);
            displayCurrentMessage(currentMessageId);
        }
    }
    private void updateChangesLog(int addedMessageId, ActionType changeType) throws SQLException {
        ChatMessage addedMessage = ChatsDataBase.getMessage(mainUserId,contactId,addedMessageId);
        LogsDataBase.addAction(changeType,addedMessageId,addedMessage.sender_id,addedMessage.receiver_id,addedMessage.message_text,addedMessage.picture,
                addedMessage.reply_message_id,addedMessage.time,addedMessage.type);
    }
    private boolean getCurrentMessageValidity() {
        String currentMessage = chatTextField.getText().trim();
        return !currentMessage.isEmpty();
    }
    private void editChosenMessage() throws Exception {
       editChosenMessageInDB();
       updateChangesLog(getEditWrapperId(), ActionType.EDITED);
       editChosenMessageInChat();
    }
    private void editChosenMessageInDB() throws SQLException {
        String currentMessage = chatTextField.getText().trim();
        int chosenMessageId = getEditWrapperId();

        String oldMessageType = ChatsDataBase.getMessage(mainUserId,contactId,chosenMessageId).type;
        String newMessageType = switch (oldMessageType) {
            case "text" -> "text";
            case "picture" -> "text";
            case "picture_with_text" -> "picture_with_text";
            case "reply_with_text" -> "reply_with_text";
            case "reply_with_picture" -> "reply_with_text";
            case "reply_with_picture_and_text" -> "reply_with_picture_and_text";
            default -> null;
        };

        boolean isMessageTooLong = (currentMessage.length() >= 1000);
        if (isMessageTooLong) {
            throw new IllegalArgumentException();
        } else {
            ChatsDataBase.editMessage(chosenMessageId, currentMessage, null, newMessageType);
        }
    }
    private void editChosenMessageInChat() throws Exception {
        String currentMessage = chatTextField.getText().trim();
        int chosenMessageId = getEditWrapperId();

        HBox chosenHBox = (HBox) chatVBox.lookup("#messageHBox"+chosenMessageId);
        StackPane chosenStackPane = (StackPane) chosenHBox.lookup("#messageStackPane"+chosenMessageId);
        VBox chosenMessageVBox = (VBox) chosenHBox.lookup("#messageVBox"+chosenMessageId);

        boolean isPicture = chosenMessageVBox != null;
        boolean hasText = isPicture && (chosenMessageVBox.lookup("#messageTextFlow"+chosenMessageId) != null);

        if (isPicture && hasText) {
            TextFlow chosenMessageTextFlow = (TextFlow) chosenMessageVBox.lookup("#messageTextFlow"+chosenMessageId);
            Text chosenMessageText = (Text) chosenMessageTextFlow.lookup("#messageText"+chosenMessageId);
            chosenMessageText.setText(currentMessage);
        } else if (isPicture) {
            chosenHBox.getChildren().clear();
            ChatMessage chosenMessage = ChatsDataBase.getMessage(mainUserId,contactId,chosenMessageId);
            chosenMessage.reload(this);
        } else {
            TextFlow chosenMessageTextFlow = (TextFlow) chosenStackPane.lookup("#messageTextFlow"+chosenMessageId);
            Text chosenMessageText = (Text) chosenMessageTextFlow.lookup("#messageText"+chosenMessageId);
            chosenMessageText.setText(currentMessage);
        }

    }
    private void updatePotentialLastEditedMessage(String message) throws SQLException {
        int chosenMessageId = getEditWrapperId();
        boolean isLastMessage = chosenMessageId == ChatsDataBase.getLastMessageId(mainUserId,contactId);

        if (isLastMessage) {
            updateLastMessage(message);
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
    private boolean hasMorePreviousMessages() throws SQLException {
        int firstMessageId = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .findFirst()
                .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                .get();
        boolean hasMorePreviousMessages = ChatsDataBase.hasMorePreviousMessages(mainUserId,contactId,firstMessageId);
        return hasMorePreviousMessages;
    }
    private boolean hasMoreNextMessages() throws SQLException {
        int lastMessageId = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .reduce((first, second) -> second) // Gets the last HBox
                .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                .get();
        boolean hasMoreNextMessages = ChatsDataBase.hasMoreNextMessages(mainUserId,contactId,lastMessageId);
        return hasMoreNextMessages;
    }
    private int getMessageHeight(HBox message) {
        VBox dummyVBox = new VBox();

        dummyVBox.getChildren().add(message);
        mainAnchorPane.getChildren().add(dummyVBox); // must be in scene graph

        // Force layout
        mainAnchorPane.applyCss();
        mainAnchorPane.layout();

        int height = (int) message.getBoundsInParent().getHeight();

        return height;
    }
    private void adjustScrollPosition(double oldChatVBoxHeight) {
        Platform.runLater(() -> {
            double newHeight = chatVBox.getHeight(); // Height after adding
            double delta = newHeight - oldChatVBoxHeight;

            // Scroll down by the exact pixel height of inserted content
            chatScrollPane.setVvalue(
                    chatScrollPane.getVvalue() + delta / (newHeight - chatScrollPane.getViewportBounds().getHeight())
            );
            suppressLazyLoading = false;
        });
    }
    private void loadMoreMessagesUp() throws Exception {
        int maxHeight = 8000; // max additional height of new loaded messages
        int totalHeight = 0;
        suppressLazyLoading = true;

        int firstMessageId = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .findFirst()
                .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                .get();
        List<ChatMessage> allMessages = new ArrayList<>(ChatsDataBase.getAllMessages(mainUserId,contactId));
        List<ChatMessage> allLeftMessages = new ArrayList<>(ChatsDataBase.getAllLeftMessages(mainUserId,contactId,firstMessageId));
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
    private void loadMoreMessagesDown() throws Exception {
        int maxHeight = 8000;
        int totalHeight = 0;
        suppressLazyLoading = true;

        int lastMessageId = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .reduce((first, second) -> second) // Gets the last HBox
                .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                .get();
        List<ChatMessage> allMessages = new ArrayList<>(ChatsDataBase.getAllMessages(mainUserId,contactId));
        List<ChatMessage> nextMessages = ChatsDataBase.getNextMessages(mainUserId,contactId,lastMessageId);
        List<Node> nodesToLoad = new ArrayList<>();

        for (int i = 0;i <= nextMessages.size()-1;i++) {
            ChatMessage message = nextMessages.get(i);
            if (totalHeight < maxHeight) {
                if (isDateLabelRequired(allMessages,message)) {
                    String messageFullDate = message.time;
                    String labelDate = getDateForDateLabel(messageFullDate);
                    nodesToLoad.add(getChatDateLabel(labelDate,messageFullDate));
                    short dateLabelHeight = 27;
                    totalHeight += dateLabelHeight;
                }

                HBox loadedMessage = message.load(this,allMessages);
                int nodeHeight = getMessageHeight(loadedMessage);

                nodesToLoad.add(loadedMessage);
                totalHeight += nodeHeight;
            } else {
                break;
            }
        }

        chatVBox.getChildren().addAll(nodesToLoad);
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
    private void showMessageSearchingField() throws SQLException {
        isMessageSearchOverlayVisible = true;

        Pane overlay = new Pane();
        overlay.setId("messageSearchOverlay");
        overlay.setPrefWidth(457);
        overlay.setPrefHeight(70);
        overlay.setLayoutX(1463);
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
                String searchQuery = messageSearchTextField.getText().trim().toLowerCase();

                if (searchQuery.isEmpty()) {
                    changeCounter(0, 0);
                    clearCurrentHighlightMessageIfNeeded();
                    foundMessageIds = new ArrayList<>();
                    currentHighlightMessageId = -1;
                    return;
                }

                foundMessageIds = ChatsDataBase.getFoundMessageIds(mainUserId, contactId, searchQuery);

                if (!foundMessageIds.isEmpty()) {
                    clearCurrentHighlightMessageIfNeeded();
                    loadChatWithFoundMessage(foundMessageIds.getFirst(), searchQuery);
                    changeCounter(1, foundMessageIds.size());
                } else {
                    changeCounter(0, 0);
                    clearCurrentHighlightMessageIfNeeded();
                    foundMessageIds = new ArrayList<>();
                    currentHighlightMessageId = -1;
                }
            } catch (Exception e) {
                // Consider using a logger instead of printing or throwing
                throw new RuntimeException("Failed to search messages", e);
            }
        });
        Platform.runLater(messageSearchTextField::requestFocus);
        messageSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isNewValueDifferent = !oldValue.trim().equals(newValue.trim());
            boolean isNewValueEmpty = newValue.trim().isEmpty();
            if (isNewValueDifferent && !isNewValueEmpty) {
                pause.playFromStart();
            } else if (isNewValueEmpty) {
                try {
                    changeCounter(0,0);
                    clearCurrentHighlightMessage();
                    foundMessageIds = new ArrayList<>();
                    currentHighlightMessageId = -1;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
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
        upButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                if (currentHighlightMessageId != -1 && currentHighlightMessageId != foundMessageIds.getFirst()) {
                    try {
                        int previousMessageId = foundMessageIds.get(foundMessageIds.indexOf(currentHighlightMessageId) - 1);
                        String word = messageSearchTextField.getText().trim().toLowerCase();
                        loadChatWithFoundMessage(previousMessageId,word);

                        changeCounter(foundMessageIds.indexOf(previousMessageId) + 1,foundMessageIds.size());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        overlay.getChildren().add(upButton);

        Label downButton = new Label();
        downButton.setPrefWidth(42);
        downButton.setPrefHeight(42);
        downButton.setLayoutX(330);
        downButton.setLayoutY(16);
        downButton.getStyleClass().add("chat-message-search-down-button");
        downButton.setCursor(Cursor.HAND);
        downButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                if (currentHighlightMessageId != -1 && currentHighlightMessageId != foundMessageIds.getLast()) {
                    try {
                        int nextMessageId = foundMessageIds.get(foundMessageIds.indexOf(currentHighlightMessageId) + 1);
                        String word = messageSearchTextField.getText().trim().toLowerCase();
                        loadChatWithFoundMessage(nextMessageId,word);

                        changeCounter(foundMessageIds.indexOf(nextMessageId) + 1,foundMessageIds.size());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
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
                try {
                    mainAnchorPane.getChildren().remove(overlay);
                    moveFirstMessageUp();

                    clearCurrentHighlightMessage();
                    foundMessageIds = new ArrayList<>();
                    currentHighlightMessageId = -1;
                    isMessageSearchOverlayVisible = false;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    private void clearCurrentHighlightMessageIfNeeded() throws SQLException {
        if (currentHighlightMessageId != -1) {
            clearCurrentHighlightMessage();
        }
    }
    private void clearCurrentHighlightMessage() throws SQLException {
        String message = ChatsDataBase.getMessage(mainUserId,contactId,currentHighlightMessageId).message_text;
        HBox messageHBox = (HBox) chatVBox.lookup("#messageHBox"+currentHighlightMessageId);
        StackPane messageStackPane = (StackPane) messageHBox.lookup("#messageStackPane"+currentHighlightMessageId);
        TextFlow messageTextFlow = (TextFlow) messageStackPane.lookup("#messageTextFlow"+currentHighlightMessageId);

        messageTextFlow.getChildren().clear();
        Text normalMessage = new Text(message);
        normalMessage.getStyleClass().add("chat-message-textflow-text");

        messageTextFlow.getChildren().add(normalMessage);
    }
    private void moveFirstMessageDown() throws SQLException {
        int firstMessageId = ChatsDataBase.getFirstMessageId(mainUserId,contactId);
        int firstChatMessageId = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .findFirst()
                .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                .orElse(-1);

        if (firstChatMessageId != -1 && firstChatMessageId == firstMessageId) {
            HBox message = (HBox) chatVBox.lookup("#messageHBox"+firstMessageId);
            VBox.setMargin(message,new Insets(32,0,0,0));
        }
    }
    private void moveFirstMessageUp() throws SQLException {
        int firstMessageId = ChatsDataBase.getFirstMessageId(mainUserId,contactId);
        int firstChatMessageId = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .findFirst()
                .map(node -> Integer.parseInt(node.getId().replaceAll("\\D+", "")))
                .orElse(-1);

        if (firstChatMessageId != -1 && firstChatMessageId == firstMessageId) {
            HBox message = (HBox) chatVBox.lookup("#messageHBox"+firstMessageId);
            VBox.setMargin(message,new Insets(5,0,0,0));
        }
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
        suppressLazyLoading = true;
        ArrayList<Node> newChatNode = new ArrayList<>();
        ArrayList<ChatMessage> allMessages = ChatsDataBase.getAllMessages(mainUserId,contactId);

        int maxPreviousHeight = 1000;
        int previousTotalHeight = 0;
        List<ChatMessage> previousMessages = ChatsDataBase.getAllLeftMessages(mainUserId,contactId,foundMessageId);
        for (int i = previousMessages.size() - 1;i >= 0;i--) {
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
            chatScrollPane.setVvalue(newChatPosition);
            suppressLazyLoading = false;
            highlightWord(foundMessageId,targetMessage);
            try {
                moveFirstMessageDown();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void highlightWord(int messageId,String word) {
        HBox messageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
        StackPane messageStackPane = (StackPane) messageHBox.lookup("#messageStackPane"+messageId);
        TextFlow messageTextFlow = (TextFlow) messageStackPane.lookup("#messageTextFlow"+messageId);

        addHighlightedText(messageTextFlow,word);
    }
    private void addHighlightedText(TextFlow textFlow,String word) {
        int messageId = textFlow.getChildren().stream()
                .map(Node::getId)
                .filter(id -> id != null && id.startsWith("messageText"))
                .map(id -> id.replaceAll("\\D+", ""))
                .filter(num -> !num.isEmpty())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(-1);
        currentHighlightMessageId = messageId;

        Text messageText = (Text) textFlow.lookup("#messageText"+messageId);
        String originalText = messageText.getText().trim();
        textFlow.getChildren().clear();

        Pattern pattern = Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(originalText);

        int lastIndex = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Add text before match
            if (lastIndex < start) {
                Text before = new Text(originalText.substring(lastIndex, start));
                before.getStyleClass().add("chat-message-textflow-text");
                textFlow.getChildren().add(before);
            }

            // Add highlighted match with background
            Text highlightText = new Text(originalText.substring(start, end));
            highlightText.getStyleClass().add("chat-message-textflow-text");

            StackPane highlightWrapper = new StackPane(highlightText);
            highlightWrapper.setStyle("-fx-background-color: #0D5D7B;");
            textFlow.getChildren().add(highlightWrapper);

            lastIndex = end;
        }

        // Add remaining text after last match
        if (lastIndex < originalText.length()) {
            Text remaining = new Text(originalText.substring(lastIndex));
            remaining.getStyleClass().add("chat-message-textflow-text");
            textFlow.getChildren().add(remaining);
        }
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
    private void moveMessageAvatarBack(int messageId,int senderId) throws SQLException {
        HBox targetMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
        HBox previousMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId));
        int previousMessageId = ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId);

        boolean hasAvatarLabel = targetMessageHBox.lookup("#messageAvatarLabel"+messageId) != null;
        boolean isSameSender = (previousMessageHBox != null) && senderId == ChatsDataBase.getMessage(mainUserId,contactId,previousMessageId).sender_id;
        boolean previousMessageNoAvatarLabel = (previousMessageHBox != null) && previousMessageHBox.lookup("#messageAvatarLabel"+ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId)) == null;

        if (hasAvatarLabel && isSameSender && previousMessageNoAvatarLabel) {
            addNewAvatarLabel(previousMessageHBox,ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId),senderId);
        }
    }
    private void addNewAvatarLabel(HBox messageHBox,int messageId,int senderId) throws SQLException {
        Label newAvatarLabel = new Label();
        newAvatarLabel.setId("messageAvatarLabel"+messageId);
        setMessageAvatar(newAvatarLabel,senderId);
        messageHBox.getChildren().addFirst(newAvatarLabel);

        StackPane messageStackPane = (StackPane) messageHBox.lookup("#messageStackPane"+messageId);
        HBox.setMargin(newAvatarLabel, (senderId == mainUserId) ? new Insets(0, 115, 0, 0) : new Insets(0, 0, 0, 105));
        HBox.setMargin(messageStackPane, (senderId == mainUserId) ? new Insets(0, 13, 0, 0) : new Insets(0, 0, 0, 13));
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
    private void deleteDateLabel(int messageId,String messageTime) throws SQLException {
        Label dateLabel = (Label) chatVBox.lookup("#dateLabel"+getDateLabelDate(messageTime));

        boolean isFirstMessage = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> node.getId())
                .noneMatch(id -> id != null && id.startsWith("#messageHBox"));
        boolean isThereMessageOnSameDate = ChatsDataBase.isThereMessagesOnSameDay(mainUserId,contactId,messageId,messageTime);

        if (!isFirstMessage && !isThereMessageOnSameDate) {
            chatVBox.getChildren().remove(dateLabel);
        }
    }
    private String getDateLabelDate(String fullTime) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(fullTime, inputFormatter);

        return dateTime.toLocalDate().toString(); // Outputs in yyyy-MM-dd format
    }
    private void removeMessageHBox(int messageId) {
        HBox targetMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+messageId);
        chatVBox.getChildren().remove(targetMessageHBox);
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
