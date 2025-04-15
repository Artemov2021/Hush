package com.messenger.main;

import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import com.messenger.design.ToastMessage;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.IntStream;

public class MainWindowController {
    @FXML protected AnchorPane mainAnchorPane;
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


    public final void setMainUserId(int id) {
        mainUserId = id;
    }
    public final void initializeWithValue() throws SQLException, IOException {
        mainUserId = 1;
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
    }


    // Interface Initialization
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


    // Contacts
    private void loadAllContacts() throws SQLException, IOException {
        mainContactsVBox.getChildren().clear();
        mainContactsVBox.setSpacing(4.0);
        int[] allContactsId = ContactsDataBase.getContactsIdList(mainUserId);
        int[] lastContacts = Arrays.copyOfRange(allContactsId, allContactsId.length - Math.min(allContactsId.length, 20), allContactsId.length);
        for (int contactId: lastContacts) {
            addContactPane(contactId);
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
    protected void addContactPane(int contactId) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainContact.fxml"));
        Pane contactRoot = fxmlLoader.load();

        MainContactController contactPane = fxmlLoader.getController();
        contactPane.setContactId(contactId);
        contactPane.injectUIElements(this);
        contactPane.initializeContactPane();

        mainContactsVBox.getChildren().add(0,contactRoot);
    }
    private void loadCustomContacts(int[] contactsId) throws IOException, SQLException {
        for (int contactId: contactsId) {
            addContactPane(contactId);
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
        String contactIdString = lastContactPane.getChildren().get(0).getId().split("mainContactPane")[1];
        return Integer.parseInt(contactIdString);
    }
    private boolean hasMoreContacts() throws SQLException {
        int lastContactId = getVisibleBottomContactId();
        int[] allContactsIds = ContactsDataBase.getContactsIdList(mainUserId);
        return allContactsIds[0] != lastContactId;    // 3,5,15   15 != 3
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
