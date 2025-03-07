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
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        setContactsLazyLoading();
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
    private void setContactsLazyLoading() {
        // Once scroll pane was triggered:
        // 1) load messages on visible area ( loading beginns 12 elements from the uppermost element downward )
        // 2) making the invisible messages empty, as placeholders
        // 3) limiting overall messages amount into 20, then 20 more messages are going to be added dynamically

        mainContactsScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            loadVisibleMessages(getUppermostElementIndex(mainContactsVBox,mainContactsScrollPane),getBottommostElement(mainContactsVBox,mainContactsScrollPane));


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
        System.out.println(enteredName);
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
    private void loadVisibleMessages(int beginningIndex,int endingIndex) {
        for (int i = beginningIndex;i <= endingIndex;i++) {
            System.out.println(i);
        }
    }
    public int getUppermostElementIndex(VBox vbox, ScrollPane scrollPane) {
        int index = -1;
        // Get the height of the viewport
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollPosition = scrollPane.getVvalue() * (vbox.getHeight() - viewportHeight);

        // Iterate through VBox children to find the uppermost visible element
        for (int i = 0; i < vbox.getChildren().size(); i++) {
            // Get the current child of the VBox
            AnchorPane anchorPane = (AnchorPane) vbox.getChildren().get(i);
            double elementTop = anchorPane.getLayoutY();
            double elementBottom = elementTop + anchorPane.getHeight();
            index = i;

            // Check if the element is visible in the viewport
            // The element is considered visible if the top is above the viewport and the bottom is below the viewport
            if (elementTop < (scrollPosition + viewportHeight) && elementBottom > scrollPosition) {
                // If the element is visible, print its ID
                return index;

            }
        }
        return index;
    }
    public int getBottommostElement(VBox vbox, ScrollPane scrollPane) {
        int index = -1;
        // Get the height of the viewport
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollPosition = scrollPane.getVvalue() * (vbox.getHeight() - viewportHeight);

        // Iterate through VBox children to find the bottommost visible element
        AnchorPane bottommostElement = null; // Store the bottommost element
        for (int i = 0; i < vbox.getChildren().size(); i++) {
            // Get the current child of the VBox
            AnchorPane anchorPane = (AnchorPane) vbox.getChildren().get(i);
            double elementTop = anchorPane.getLayoutY();
            double elementBottom = elementTop + anchorPane.getHeight();

            // Check if the element is visible in the viewport
            // The element is considered visible if its top is above the viewport and its bottom is below the viewport
            if (elementTop < (scrollPosition + viewportHeight) && elementBottom > scrollPosition) {
                bottommostElement = anchorPane;  // Update the bottommost visible element
                index = i;
            }
        }

        return index;
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
