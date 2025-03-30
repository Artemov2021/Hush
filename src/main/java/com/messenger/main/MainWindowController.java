package com.messenger.main;

import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import com.messenger.design.ToastMessage;
import com.messenger.main.smallWindows.NewContactWindow;
import com.messenger.main.smallWindows.SettingsWindow;
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
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class MainWindowController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label mainAvatarLabel;
    @FXML
    private Label mainNameLabel;
    @FXML
    private Label mainEmailLabel;
    @FXML
    private Label toastCopiedMessage;
    @FXML
    private Label settingsButton;
    @FXML
    private TextField mainSearchField;
    @FXML
    private Label addContactButton;
    @FXML
    private Label mainTitle;
    @FXML
    private Label logInTitle;
    @FXML
    private Label mainSmallTitle;
    @FXML
    public ScrollPane mainContactsScrollPane;
    @FXML
    public VBox mainContactsVBox;

    public int mainUserId;


    public void initializeWithValue () throws SQLException, IOException {
        setMainLogInTitle();
        setProfileInfo();
        setAppropriateAvatar();
        defocusSearchField();
        setScrollPaneEffect();
        loadContacts();
        setLazyLoading();
        addSearchFieldListener();
        setAddContactButtonListener();
        setSettingsButtonListener();
        setEmailLabelListener();
        removeTextFieldContextMenu();
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
            mainNameLabel.setLayoutY(23);
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
    private void defocusSearchField() {
        mainSearchField.setFocusTraversable(false);
    }
    private void setScrollPaneEffect() {
        mainContactsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        mainContactsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ScrollPaneEffect.addScrollBarEffect(mainContactsScrollPane);
    }
    private void loadContacts() throws SQLException, IOException {
        MainContactList.loadContacts(mainUserId,mainContactsVBox,anchorPane);
    }
    private void setLazyLoading() {
        mainContactsScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0) {
                int bottomContactId = getBottomContactId();
                try {
                    if (hasMoreContacts(mainUserId, bottomContactId)) {
                        MainContactList.loadMoreContacts(mainUserId,mainContactsVBox,anchorPane,bottomContactId);
                    }
                } catch (SQLException | RuntimeException | IOException e) {
                    throw new RuntimeException("Error fetching more contacts", e);
                }
            }
        });
    }
    private void addSearchFieldListener() {
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        pause.setOnFinished(event -> showFoundedContacts(mainSearchField.getText()));


        mainSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().length() == 0) {
                    mainContactsVBox.getChildren().clear();
                    MainContactList.loadContacts(mainUserId,mainContactsVBox,anchorPane);
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
            pause.playFromStart();
        });
    }
    private void setAddContactButtonListener(){
        addContactButton.setOnMouseClicked(clickEvent -> {
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
                    settingsWindow();
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
    private void removeTextFieldContextMenu() {
        mainSearchField.setContextMenu(new ContextMenu());
    }


    private void showFoundedContacts(String enteredName) {
        try {
            if (enteredName.trim().length() > 0) {
                mainContactsVBox.getChildren().clear();
                int[] foundedUsersId = ContactsDataBase.getMatchedUsersId(mainUserId,enteredName.trim());
                MainContactList.loadCustomContacts(mainUserId,foundedUsersId,mainContactsVBox,anchorPane);
            } else {
                mainContactsVBox.getChildren().clear();
                MainContactList.loadContacts(mainUserId,mainContactsVBox,anchorPane);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    private int getBottomContactId() {
        // Get the last child in the VBox and extract its ID
        AnchorPane lastContactPane = (AnchorPane) mainContactsVBox.getChildren().get(mainContactsVBox.getChildren().size() - 1);
        String contactIdString = lastContactPane.getChildren().get(0).getId().split("mainContactPane")[1];
        return Integer.parseInt(contactIdString);
    }
    public boolean hasMoreContacts(int mainUserId,int lastContactId) throws SQLException {
        int[] allContactsIds = ContactsDataBase.getContactsIdList(mainUserId);
        return allContactsIds[0] != lastContactId;    // 3,5,15   15 != 3
    }


    public void saveToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(mainEmailLabel.getText());
        clipboard.setContents(selection, null);
        toastCopiedMessage.setLayoutX(mainEmailLabel.getLayoutX() + mainEmailLabel.getWidth()/5);
        ToastMessage.applyFadeEffect(toastCopiedMessage);
    }
    public void addContactWindow () throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainNewContactWindow.fxml"));
        Parent newContactRoot = fxmlLoader.load();

        NewContactWindow newContactWindow = fxmlLoader.getController();
        newContactWindow.setMainUserId(mainUserId);
        newContactWindow.setMainAnchorPane(anchorPane);
        newContactWindow.setMainContactsVBox(mainContactsVBox);
        newContactWindow.initializeWithValue();

        anchorPane.getChildren().add(newContactRoot);
    }
    public void settingsWindow() throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/fxml/MainSettingsWindow.fxml"));
        Parent settingsWindowRoot = loader.load();

        SettingsWindow settingsWindow = loader.getController();
        settingsWindow.setMainUserId(mainUserId);
        settingsWindow.setMainAnchorPane(anchorPane);
        settingsWindow.initializeWithValue();
        // TODO

        anchorPane.getChildren().add(settingsWindowRoot);
    }


    public void setMainUserId(int mainUserId) throws SQLException, IOException {
        this.mainUserId = mainUserId;
        initializeWithValue();
    }
    public void initialize() throws SQLException, IOException {
        this.mainUserId = 1;
        initializeWithValue();
    }





}
