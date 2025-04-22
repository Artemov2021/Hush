package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
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
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.Cursor;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class MainChatController extends MainContactController {
    @FXML private Pane chatBackground;
    @FXML protected ScrollPane chatScrollPane;
    @FXML private Label chatMainAvatar;
    @FXML private Label chatMainName;
    @FXML protected VBox chatVBox;
    @FXML private TextField chatTextField;
    @FXML private Label chatAddPictureButton;
    @FXML protected Label scrollDownButton;
    @FXML private Label sendMessageButton;


    // Chat Interface Initialization, Chat Loading
    public void setChatContactId(int contactId) {
        this.contactId = contactId;
    }
    public void injectMainUIElements(MainWindowController source) {
        this.mainAnchorPane = source.mainAnchorPane;
        this.mainContactsVBox = source.mainContactsVBox;
    }
    public void injectContactUIElements(MainContactController mainContactController) {
        this.mainContactMessageLabel = mainContactController.mainContactMessageLabel;
        this.mainContactTimeLabel = mainContactController.mainContactTimeLabel;
    }
    public final void initializeChat() throws Exception {
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
        boolean chatIsEmpty = ChatsDataBase.getAllMessages(mainUserId,contactId).isEmpty();

        if (chatIsEmpty) {
            setCurrentDateLabel();
        } else {
            loadChatHistory();
        }
    }
    public void loadChatHistory() throws Exception {
        List<ChatMessage> allMessages = ChatsDataBase.getAllMessages(mainUserId,contactId);
        for (ChatMessage message: allMessages) {
            chatVBox.getChildren().add(message.render(this));
        }
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
        chatDateLabel.setId("dateLabel");
        chatDateLabel.getStyleClass().add("chat-date-label");
        VBox.setMargin(chatDateLabel,new Insets(8,0,8,0));
        chatVBox.getChildren().add(chatDateLabel);
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

        if (currentMessageType == TextMessageType.EDIT_WITH_TEXT) {
            processMessageEdit();
        } else {
            processMessageSend();
        }

        finalizeMessageFlow();
    }


    // Message Sending: Small Functions
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
    private void finalizeMessageFlow() {
        removePotentialWrapper();
        moveBackScrollDownButton();
        updateScrollDownButtonVisibility();
        clearChatInput();
    }
    private void insertAndDisplayMessage() throws Exception {
        int currentMessageId = insertCurrentMessageIntoDB();
        displayCurrentMessage(currentMessageId);
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

        ChatsDataBase.editMessage(chosenMessageId,currentMessage,null);
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

        return ChatsDataBase.addMessage(senderId,receiverId,message,picture,replyMessageId,messageTime,messageType,received);
    }
    private void ensureUserInContacts(int contactId) throws SQLException {
        int[] contactListOfContact = ContactsDataBase.getContactsIdList(contactId);
        boolean existsInContactList = Arrays.stream(contactListOfContact).anyMatch(id -> id == mainUserId);
        if (!existsInContactList) {
            ContactsDataBase.addContact(contactId,mainUserId);
        }
    }
    private void displayCurrentMessage(int messageId) throws Exception {
        ChatMessage currentMessage = new ChatMessage(messageId);
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
