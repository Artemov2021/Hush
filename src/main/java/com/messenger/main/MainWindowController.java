package com.messenger.main;

import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import com.messenger.design.ToastMessage;
import com.messenger.main.smallWindows.NewContactWindow;
import com.messenger.main.smallWindows.SettingsWindow;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
    private TextField mainSearchField;
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

    public int id;


    public void initializeWithValue () throws SQLException, IOException {
        setMainLogInTitle();
        setProfileInfo();
        setAppropriateAvatar();
        defocusSearchField();
        setScrollPaneEffect();
        loadContacts();
        setLazyLoading();
        addSearchFieldListener();


    }


    private void setMainLogInTitle() throws SQLException {
        /* set main title on the right side. If the person has no contacts,
           there is going to be the default title ( pointing how to add a new contact ). If the person
           has already at least one contact, there is going to be "login-title"
        */
        if (UsersDataBase.getContactsAmount(id) > 0) {
            mainTitle.setVisible(false);
            mainSmallTitle.setVisible(false);
            logInTitle.setVisible(true);
        }
    }
    private void setProfileInfo() throws SQLException {
        // set name and/or email in the upper left corner
        mainNameLabel.setText(UsersDataBase.getNameWithId(id));
        mainEmailLabel.setText(UsersDataBase.getEmailWithId(id));
        if (UsersDataBase.getEmailWithId(id) == null) {
            mainEmailLabel.setVisible(false);
            mainNameLabel.setLayoutY(23);
        }
    }
    private void setAppropriateAvatar() throws SQLException {
        // set the profile avatar in the upper left corner
        if (UsersDataBase.getAvatarWithId(id) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(id);
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
        MainContactList.loadContacts(id,mainContactsVBox,anchorPane);
    }
    private void setLazyLoading() {
        mainContactsScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0) {
                int bottomContactId = getBottomContactId();
                try {
                    if (hasMoreContacts(id, bottomContactId)) {
                        MainContactList.loadMoreContacts(id,mainContactsVBox,anchorPane,bottomContactId);
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
                    MainContactList.loadContacts(id,mainContactsVBox,anchorPane);
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
            pause.playFromStart();
        });
    }


    private void showFoundedContacts(String enteredName) {
        try {
            if (enteredName.trim().length() > 0) {
                mainContactsVBox.getChildren().clear();
                int[] foundedUsersId = ContactsDataBase.getMatchedUsersId(id,enteredName.trim());
                MainContactList.loadCustomContacts(id,foundedUsersId,mainContactsVBox,anchorPane);
            } else {
                mainContactsVBox.getChildren().clear();
                MainContactList.loadContacts(id,mainContactsVBox,anchorPane);
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


    @FXML
    public void saveToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(mainEmailLabel.getText());
        clipboard.setContents(selection, null);
        toastCopiedMessage.setLayoutX(mainEmailLabel.getLayoutX() + mainEmailLabel.getWidth()/5);
        ToastMessage.applyFadeEffect(toastCopiedMessage);
    }
    @FXML
    public void addContactWindow () throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainNewContactWindow.fxml"));
        Parent newContactRoot = fxmlLoader.load();

        NewContactWindow newContactWindow = fxmlLoader.getController();
        newContactWindow.setMainUserId(id);
        newContactWindow.setMainAnchorPane(anchorPane);
        newContactWindow.setMainContactsVBox(mainContactsVBox);
        newContactWindow.initializeWithValue();

        anchorPane.getChildren().add(newContactRoot);
    }
    @FXML
    public void settingsWindow() throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/fxml/MainSettingsWindow.fxml"));
        Parent settingsWindowRoot = loader.load();

        SettingsWindow settingsWindow = loader.getController();
        settingsWindow.setMainUserId(id);
        settingsWindow.setMainAnchorPane(anchorPane);
        settingsWindow.initializeWithValue();
        // TODO

        anchorPane.getChildren().add(settingsWindowRoot);
    }


    public void setId(int id) throws SQLException, IOException {
        this.id = id;
        initializeWithValue();
    }
    public void initialize() throws SQLException, IOException {
        this.id = 1;
        initializeWithValue();
    }





}
