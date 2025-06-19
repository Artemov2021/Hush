package com.messenger.main;

import com.messenger.database.*;
import com.messenger.database.Action;
import com.messenger.design.ScrollPaneEffect;
import com.messenger.design.ToastMessage;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class MainWindowController {
    @FXML public AnchorPane mainAnchorPane;
    @FXML protected Label mainAvatarLabel;
    @FXML protected Label mainNameLabel;
    @FXML protected Label mainEmailLabel;
    @FXML private Label toastCopiedMessage;
    @FXML private Label settingsButton;
    @FXML private TextField mainSearchField;
    @FXML private Label openAddContactButton;
    @FXML private Label mainTitle;
    @FXML private Label logInTitle;
    @FXML private Label mainSmallTitle;
    @FXML public ScrollPane mainContactsScrollPane;
    @FXML public VBox mainContactsVBox;

    public static int mainUserId;
    private boolean isWindowInitialized;

    private static ScheduledExecutorService messageListenerExecutor;
    private int lastContactsActionId;

    public final void setMainUserId(int id) {
        mainUserId = id;
    }
    public final void initializeWithValue() throws SQLException, IOException {
        setMainLogInTitle();
        setProfileInfo();
        setAppropriateAvatar();
        setSearchFieldUnfocused();
        setScrollPaneEffect();
        loadAllContacts();
        setLazyLoading();
        setSearchFieldListener();
        setAddContactButtonListener();
        setSettingsButtonListener();
        setEmailLabelListener();
        removeTextFieldContextMenu();
        setNewMessageListener();
        setLastContactsAction();
        setAppropriateIcon();
        isWindowInitialized = true;
    }


    // Interface Initialization
    public static void shutDown() {
        if (messageListenerExecutor != null && !messageListenerExecutor.isShutdown()) {
            messageListenerExecutor.shutdownNow();
        }
    }
    private void setMainLogInTitle() throws SQLException {
        /* set main title on the right side. If the person has no contacts,
           there is going to be the default title ( pointing how to add a new contact ). If the person
           has already at least one contact, there is going to be "login-title"
        */
        if (UsersDataBase.getContactsAmount(mainUserId) > 0) {
            mainTitle.setVisible(false);
            mainSmallTitle.setVisible(false);
            logInTitle.setVisible(true);
        }
    }
    private void setProfileInfo() throws SQLException {
        // set name and/or email in the upper left corner
        mainNameLabel.setText(UsersDataBase.getNameWithId(mainUserId));
        mainEmailLabel.setText(UsersDataBase.getEmailWithId(mainUserId));
        if (UsersDataBase.getEmailWithId(mainUserId) == null) {
            mainEmailLabel.setVisible(false);
            mainNameLabel.setLayoutY(32);
        }
    }
    private void setAppropriateAvatar() throws SQLException {
        // set the profile avatar in the upper left corner
        if (UsersDataBase.getAvatarWithId(mainUserId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(mainUserId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setSmooth(true);
            mainAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(25);
            clip.setLayoutY(25);
            clip.setRadius(25);
            mainAvatarLabel.setClip(clip);
        } else {
            mainAvatarLabel.getStyleClass().clear();
            mainAvatarLabel.getStyleClass().add("avatar-button-default");
        }
    }
    private void setSearchFieldUnfocused() {
        mainSearchField.setFocusTraversable(false);
    }
    private void setScrollPaneEffect() {
        mainContactsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        mainContactsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ScrollPaneEffect.addScrollBarEffect(mainContactsScrollPane);
    }
    private void setSearchFieldListener() {
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        pause.setOnFinished(event -> showFoundedContacts(mainSearchField.getText()));

        mainSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().length() == 0) {
                    mainContactsVBox.getChildren().clear();
                    loadAllContacts();
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
            pause.playFromStart();
        });
    }
    private void setAddContactButtonListener(){
        openAddContactButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    addContactWindow();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    private void setSettingsButtonListener(){
        settingsButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    openSettingsWindow();
                } catch (IOException | SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    private void setEmailLabelListener() {
        mainEmailLabel.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                saveToClipboard();
            }
        });
    }
    private void setLazyLoading() {
        mainContactsScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0) {
                try {
                    if (hasMoreContacts()) {
                        loadMoreContacts();
                    }
                } catch (SQLException | RuntimeException | IOException e) {
                    throw new RuntimeException("Error fetching more contacts", e);
                }
            }
        });
    }
    private void removeTextFieldContextMenu() {
        mainSearchField.setContextMenu(new ContextMenu());
    }
    private void setNewMessageListener() {
        messageListenerExecutor = Executors.newSingleThreadScheduledExecutor();

        messageListenerExecutor.scheduleAtFixedRate(() -> {
            try {
                if (isWindowInitialized) {
                    checkContactChatsForChanges();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);

        Platform.runLater(() -> {
            Stage currentStage = (Stage) mainAnchorPane.getScene().getWindow();

            currentStage.setOnCloseRequest(event -> {
                // 1. Shut down the main window's executor
                if (messageListenerExecutor != null && !messageListenerExecutor.isShutdown()) {
                    messageListenerExecutor.shutdownNow();
                }

                // 2. Also shut down any open chat controller threads
                for (Node child : mainAnchorPane.getChildren()) {
                    if (child.getUserData() instanceof MainChatController controller) {
                        controller.shutdown();
                    }
                }

                // 3. Finally exit the app
                Platform.exit();
            });
        });
    }
    private void setLastContactsAction() throws SQLException {
        lastContactsActionId = LogsDataBase.getLastContactsActionId(mainUserId);
    }
    private void setAppropriateIcon() {
        Platform.runLater(() -> {
            try {
                if (ChatsDataBase.isThereUnreadMessages(mainUserId)) {
                    Stage currentStage = (Stage) mainAnchorPane.getScene().getWindow();
                    currentStage.getIcons().clear();
                    currentStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/iconNewMessages.png")));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void checkContactChatsForChanges() throws SQLException, IOException {
        int updatedLastContactsActionId = LogsDataBase.getLastContactsActionId(mainUserId);
        if (lastContactsActionId != updatedLastContactsActionId) {
            ArrayList<Integer> newActionIds = LogsDataBase.getNewActionIds(mainUserId,lastContactsActionId);
            for (int actionId: newActionIds) {
                Platform.runLater(() -> {
                    try {
                        displayAction(actionId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            lastContactsActionId = updatedLastContactsActionId;
        }
    }
    private void setNewMessagesIcon(Action action) {
        Platform.runLater(() -> {
            boolean isContactChatOpen = mainAnchorPane.getChildren().stream()
                    .map(Node::getId)
                    .filter(Objects::nonNull)
                    .filter(id -> id.equals("chatAnchorPane"+action.sender_id))
                    .map(id -> id.replaceAll("\\D+", ""))
                    .anyMatch(num -> !num.isEmpty());

            if (!isContactChatOpen) {
                Stage currentStage = (Stage) mainAnchorPane.getScene().getWindow();
                currentStage.getIcons().clear();
                currentStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/iconNewMessages.png")));
            }
        });
    }
    private void displayAction(int actionId) throws SQLException {
         Action action = new Action(actionId);

         switch (action.change_type) {
             case ActionType.NEW -> executeNewAction(action);
             case ActionType.EDITED -> executeEditAction(action);
             case ActionType.DELETED -> executeDeleteAction(action);
             default -> throw new RuntimeException();
         }
    }
    private void executeNewAction(Action action) throws SQLException {
        moveUpContact(action);
        changeLastMessage(action);
        changeLastMessageTime(action);
        changeNewMessageCounter(action);
        setNewMessagesIcon(action);
    }
    private void executeEditAction(Action action) throws SQLException {
        ChatMessage editedMessage = ChatsDataBase.getMessage(mainUserId,action.sender_id,action.message_id);
        boolean isLastMessage = ChatsDataBase.getLastMessageId(mainUserId,action.sender_id) == action.message_id;
        boolean isText = editedMessage.message_text != null && !editedMessage.message_text.trim().isEmpty();

        AnchorPane contactAnchorPane = (AnchorPane) mainContactsVBox.lookup("#mainContactAnchorPane"+action.sender_id);
        Pane contactPane = (Pane) contactAnchorPane.lookup("#mainContactPane");
        Label mainContactMessageLabel = (Label) contactPane.lookup("#mainContactMessageLabel");

        if (isLastMessage && !isText) {
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.setStyle("-fx-text-fill: white");
            mainContactMessageLabel.setText("Picture");
        } else if (isLastMessage) {
            String lastMessage = ChatsDataBase.getLastMessage(mainUserId,action.sender_id);
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText(lastMessage);
        }
    }
    private void executeDeleteAction(Action action) throws SQLException {
        resetLastMessage(action);
        resetLastMessageTime(action);
        changeNewMessageCounter(action);
        changeIcon();
    }
    private void resetLastMessage(Action action) throws SQLException {
        int messageId = action.message_id;
        int contactId = action.sender_id;
        int previousMessageId = ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId);
        int nextMessageId = ChatsDataBase.getNextMessageId(mainUserId,contactId,messageId);
        boolean previousMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,previousMessageId);
        boolean nextMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,nextMessageId);
        boolean previousMessageIsPicture = previousMessageExists && ChatsDataBase.getMessage(mainUserId,contactId,previousMessageId).picture != null;
        boolean previousMessageHasText = previousMessageExists && ChatsDataBase.getMessage(mainUserId,contactId,previousMessageId).message_text != null;
        boolean lastMessageIsPicture = ChatsDataBase.getMessage(mainUserId,contactId,ChatsDataBase.getLastMessageId(mainUserId,contactId)).picture != null;
        boolean lastMessageHasText = ChatsDataBase.getMessage(mainUserId,contactId,ChatsDataBase.getLastMessageId(mainUserId,contactId)).message_text != null;

        AnchorPane contactAnchorPane = (AnchorPane) mainContactsVBox.lookup("#mainContactAnchorPane"+action.sender_id);
        Pane contactPane = (Pane) contactAnchorPane.lookup("#mainContactPane");
        Label mainContactMessageLabel = (Label) contactPane.lookup("#mainContactMessageLabel");

        if (!previousMessageExists && !nextMessageExists) {
            mainContactMessageLabel.setText("");
        } else if (!nextMessageExists && previousMessageIsPicture && !previousMessageHasText) {
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.setStyle("-fx-text-fill: white");
            mainContactMessageLabel.setText("Picture");
        } else if (!nextMessageExists && (!previousMessageIsPicture || previousMessageIsPicture && previousMessageHasText)){
            String previousLastMessage = ChatsDataBase.getMessage(mainUserId,contactId,previousMessageId).message_text;
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText(previousLastMessage);
        } else if (nextMessageExists && lastMessageIsPicture && !lastMessageHasText) {
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.setStyle("-fx-text-fill: white");
            mainContactMessageLabel.setText("Picture");
        } else if (nextMessageExists && (!lastMessageIsPicture || lastMessageIsPicture && lastMessageHasText)) {
            String lastMessage = ChatsDataBase.getLastMessage(mainUserId, contactId);
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText(lastMessage);
        }
    }
    private void resetLastMessageTime(Action action) throws SQLException {
        int messageId = action.message_id;
        int contactId = action.sender_id;
        int previousMessageId = ChatsDataBase.getPreviousMessageId(mainUserId,contactId,messageId);
        int nextMessageId = ChatsDataBase.getNextMessageId(mainUserId,contactId,messageId);
        boolean previousMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,previousMessageId);
        boolean nextMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,nextMessageId);

        AnchorPane contactAnchorPane = (AnchorPane) mainContactsVBox.lookup("#mainContactAnchorPane"+action.sender_id);
        Pane contactPane = (Pane) contactAnchorPane.lookup("#mainContactPane");
        Label mainContactTimeLabel = (Label) contactPane.lookup("#mainContactTimeLabel");

        if (!previousMessageExists && !nextMessageExists) {
            mainContactTimeLabel.setText("");
        } else if (!nextMessageExists) {
            String previousMessageTime = getMessageTime(previousMessageId,contactId);
            mainContactTimeLabel.setText(previousMessageTime);
        } else if (!previousMessageExists || (previousMessageExists && nextMessageExists)){
            int lastMessageId = ChatsDataBase.getLastMessageId(mainUserId,contactId);
            mainContactTimeLabel.setText(getMessageTime(lastMessageId,contactId));
        }
    }
    private String getMessageTime(int messageId,int contactId) throws SQLException {
        String lastMessageFullDate = ChatsDataBase.getMessage(mainUserId,contactId,messageId).time;
        String pattern = "(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}:\\d{2})"; // Extracts YYYY, MM, DD, HH:mm
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(lastMessageFullDate);

        if (matcher.find()) {
            String year = matcher.group(1);
            String month = matcher.group(2);
            String day = matcher.group(3);
            String time = matcher.group(4);

            LocalDate messageDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1); // Calculate yesterday's date

            if (messageDate.isEqual(today)) {
                return time; // Show only HH:mm if it's today
            } else if (messageDate.isEqual(yesterday)) {
                return "yesterday"; // Show "yesterday" if the date is yesterday
            } else {
                return day + "." + month + "." + year; // Show full date if not today
            }
        } else {
            return ""; // Default to empty if no match
        }
    }
    private void changeIcon() throws SQLException {
        boolean isThereUnreadMessages = ChatsDataBase.isThereUnreadMessages(mainUserId);
        if (!isThereUnreadMessages) {
            Stage currentStage = (Stage) mainAnchorPane.getScene().getWindow();
            currentStage.getIcons().clear();
            currentStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/elements/icon.png")));
        }
    }


    // Contacts
    private void loadAllContacts() throws SQLException, IOException {
        mainContactsVBox.getChildren().clear();
        mainContactsVBox.setSpacing(4.0);
        int[] allContactsId = ContactsDataBase.getContactsIdList(mainUserId);
        int[] lastContacts = Arrays.copyOfRange(allContactsId, allContactsId.length - Math.min(allContactsId.length, 20), allContactsId.length);
        for (int contactId: lastContacts) {
            addContactPaneFirst(contactId);
        }
    }
    private void loadMoreContacts() throws SQLException, IOException {
        int lastContactId = getVisibleBottomContactId();
        int[] leftContacts = ContactsDataBase.getContactsIdListAfterContact(mainUserId,lastContactId);
        int[] lastContacts = Arrays.copyOfRange(leftContacts, leftContacts.length - Math.min(leftContacts.length,20), leftContacts.length);
        int[] reversedLastContacts = IntStream.range(0, lastContacts.length)
                .map(i -> lastContacts[lastContacts.length - 1 - i]) // Access elements in reverse order
                .toArray();

        for (int contactId: reversedLastContacts) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainContact.fxml"));
            Pane contactRoot = fxmlLoader.load();

            MainContactController contactPane = fxmlLoader.getController();
            contactPane.setContactId(contactId);
            contactPane.injectUIElements(this);
            contactPane.initializeContactPane();

            mainContactsVBox.getChildren().add(contactRoot);
        }
    }
    protected void addContactPaneFirst(int contactId) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainContact.fxml"));
        Pane contactRoot = fxmlLoader.load();

        MainContactController contactPane = fxmlLoader.getController();
        contactPane.setContactId(contactId);
        contactPane.injectUIElements(this);
        contactPane.initializeContactPane();

        mainContactsVBox.getChildren().addFirst(contactRoot);
    }
    private void loadCustomContacts(int[] contactsId) throws IOException, SQLException {
        for (int contactId: contactsId) {
            addContactPaneFirst(contactId);
        }
    }
    private void showFoundedContacts(String enteredName) {
        try {
            if (enteredName.trim().length() > 0) {
                mainContactsVBox.getChildren().clear();
                int[] foundedUsersId = ContactsDataBase.getMatchedUsersId(enteredName.trim());
                loadCustomContacts(foundedUsersId);
            } else {
                mainContactsVBox.getChildren().clear();
                loadAllContacts();
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    private int getVisibleBottomContactId() {
        // Get the last child in the VBox and extract its ID
        AnchorPane lastContactPane = (AnchorPane) mainContactsVBox.getChildren().get(mainContactsVBox.getChildren().size() - 1);
        String contactIdString = lastContactPane.getId().split("mainContactAnchorPane")[1];
        return Integer.parseInt(contactIdString);
    }
    private boolean hasMoreContacts() throws SQLException {
        int lastContactId = getVisibleBottomContactId();
        int[] allContactsIds = ContactsDataBase.getContactsIdList(mainUserId);
        return allContactsIds[0] != lastContactId;    // 3,5,15   15 != 3
    }
    private void moveUpContact(Action action) {
        int contactId = action.sender_id;
        AnchorPane contactAnchorPane = (AnchorPane) mainContactsVBox.lookup("#mainContactAnchorPane"+contactId);
        mainContactsVBox.getChildren().remove(contactAnchorPane);
        mainContactsVBox.getChildren().addFirst(contactAnchorPane);
    }
    private void changeLastMessage(Action action) throws SQLException {
        String message = action.message;

        AnchorPane contactAnchorPane = (AnchorPane) mainContactsVBox.lookup("#mainContactAnchorPane"+action.sender_id);
        Pane contactPane = (Pane) contactAnchorPane.lookup("#mainContactPane");
        Label mainContactMessageLabel = (Label) contactPane.lookup("#mainContactMessageLabel");

        if (message == null || message.trim().isEmpty()) {
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.setStyle("-fx-text-fill: white");
            mainContactMessageLabel.setText("Picture");
        } else {
            String lastMessage = ChatsDataBase.getLastMessage(mainUserId,action.sender_id);
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText(lastMessage);
        }
    }
    private void changeLastMessageTime(Action action) {
        AnchorPane contactAnchorPane = (AnchorPane) mainContactsVBox.lookup("#mainContactAnchorPane"+action.sender_id);
        Pane contactPane = (Pane) contactAnchorPane.lookup("#mainContactPane");
        Label mainContactTimeLabel = (Label) contactPane.lookup("#mainContactTimeLabel");

        mainContactTimeLabel.setText(getMessageHours(action.message_time));
    }
    private String getMessageHours(String messageFullTime) {
        // Define the input and output formats
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Parse the input string to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(messageFullTime, inputFormatter);

        // Format and return the output as a string
        return dateTime.format(outputFormatter);
    }
    private void changeNewMessageCounter(Action action) throws SQLException {
        AnchorPane contactAnchorPane = (AnchorPane) mainContactsVBox.lookup("#mainContactAnchorPane"+action.sender_id);
        Pane contactPane = (Pane) contactAnchorPane.lookup("#mainContactPane");
        Label mainContactMessageCounterLabel = (Label) contactPane.lookup("#mainContactMessageCounterLabel");

        boolean isContactChatOpen = mainAnchorPane.getChildren().stream()
                .map(Node::getId)
                .filter(Objects::nonNull)
                .filter(id -> id.equals("chatAnchorPane"+action.sender_id))
                .map(id -> id.replaceAll("\\D+", ""))
                .anyMatch(num -> !num.isEmpty());

        if (!isContactChatOpen) {
            long newMessagesAmount = ChatsDataBase.getUnreadMessagesAmount(mainUserId,action.sender_id);
            mainContactMessageCounterLabel.setVisible(true);
            if (newMessagesAmount > 9) {
                mainContactMessageCounterLabel.getStyleClass().clear();
                mainContactMessageCounterLabel.getStyleClass().add("contact-new-message-counter-overflow-label");
                mainContactMessageCounterLabel.setPadding(new Insets(5,2,5,2));
                mainContactMessageCounterLabel.setText("9+");
            } else if (newMessagesAmount == 0) {
                mainContactMessageCounterLabel.setText("0");
                mainContactMessageCounterLabel.setVisible(false);
            } else {
                mainContactMessageCounterLabel.setText(String.valueOf(newMessagesAmount));
            }
        }
    }


    // Email Clipboard
    private void saveToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(mainEmailLabel.getText());
        clipboard.setContents(selection, null);
        toastCopiedMessage.setLayoutX(mainEmailLabel.getLayoutX() + mainEmailLabel.getWidth()/5);
        ToastMessage.applyFadeEffect(toastCopiedMessage);
    }


    // Small Windows
    private void addContactWindow () throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainAddContactWindow.fxml"));
        Parent newContactRoot = fxmlLoader.load();

        AddContactWindowController addContactWindow = fxmlLoader.getController();
        addContactWindow.injectUIElements(this);
        addContactWindow.initializeAddContactInterface();

        mainAnchorPane.getChildren().add(newContactRoot);
    }
    private void openSettingsWindow() throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainSettingsWindow.fxml"));
        Parent settingsWindowRoot = fxmlLoader.load();

        SettingsWindowController settingsWindow = fxmlLoader.getController();
        settingsWindow.injectUIElements(this);
        settingsWindow.initializeSettingsInterface();

        mainAnchorPane.getChildren().add(settingsWindowRoot);
    }




}
