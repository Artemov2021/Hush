package com.messenger.main;

import com.messenger.Main;
import com.messenger.database.UsersDataBase;
import com.messenger.main.smallWindows.NewContactWindow;
import com.messenger.main.smallWindows.SettingsWindow;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.messenger.design.MainStyling;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

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
    private Label mainSettingsLabel;
    @FXML
    private Button mainSettingsButton;
    @FXML
    private TextField mainSearchField;
    @FXML
    private Label searchLupeLabel;
    @FXML
    private Label mainAddContactLabel;
    @FXML
    private Label mainTitle;
    @FXML
    private Label mainSmallTitle;
    @FXML
    public ScrollPane mainContactsScrollPane;
    @FXML
    public VBox mainContactsVBox;

    public int id;


    public void initializeWithValue () throws SQLException, IOException {
        setMainTitle();
        setButtonsLabelTransparency();
        setProfileInfo();
        setAppropriateAvatar();
        applyHoverStyles();

        loadContacts();
        //addSearchFieldListener();




    }
    private void setMainTitle() throws SQLException {
        /* set main title on the right side. If the person has no contacts,
           there is going to be the default title ( pointing how to add a new contact ). If the person
           has already at least one contact, there is going to be "login-title"
        */
        if (UsersDataBase.getContactsAmount(id) > 0) {
            mainTitle.setVisible(false);
            mainSmallTitle.setVisible(false);
            Label title = new Label();
            title.setLayoutX(610);
            title.setLayoutY(300);
            title.getStyleClass().add("login-title");
            title.setId("loginTitle");
            title.setPrefWidth(300);
            title.setPrefHeight(51);
            anchorPane.getChildren().add(title);

            MainContactList mainContactList = new MainContactList(anchorPane,mainContactsScrollPane,mainContactsVBox,id);
            mainContactList.addUserContactsToList();
        }
    }
    private void setButtonsLabelTransparency() {
        /* make "add a contact button label" and "settings button label" transparent for a mouse, in order to
           be able to click on buttons that are behind them   */
        mainSettingsLabel.setMouseTransparent(true);
        mainAddContactLabel.setMouseTransparent(true);
    }
    private void setProfileInfo() throws SQLException {
        // set name and/or email in the upper left corner
        mainNameLabel.setText(UsersDataBase.getNameWithId(id));
        mainEmailLabel.setText(UsersDataBase.getEmailWithId(id));
        if (UsersDataBase.getEmailWithId(id) == null) {
            mainEmailLabel.setVisible(false);
            mainNameLabel.setLayoutY(30);
        }
    }
    private void setAppropriateAvatar() throws SQLException {
        // set the profile avatar in the upper left corner
        if (UsersDataBase.getAvatarWithId(id) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(id);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(34);
            imageView.setFitWidth(34);
            imageView.setSmooth(true);
            mainAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(17);
            clip.setLayoutY(17);
            clip.setRadius(17);
            mainAvatarLabel.setClip(clip);
        } else {
            mainAvatarLabel.getStyleClass().clear();
            mainAvatarLabel.getStyleClass().add("avatar-button-default");
        }
    }
    private void applyHoverStyles() {
        // If the settings button is hovered, settings label will have hover-style (change color )
        MainStyling.setHoverStyle(mainSettingsButton, mainSettingsLabel,"settings-label-hovered","settings-label-default");

        // If the search field is focused, the lupe will have focused-style ( change color )
        MainStyling.setFocusStyle(mainSearchField,searchLupeLabel,"search-field-lupe-focused","search-field-lupe-default");
    }


    private void loadContacts() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/MainContact.fxml"));
        Parent root = fxmlLoader.load();
        MainContact contact = fxmlLoader.getController();
        contact.setName("Ахмат");
        mainContactsVBox.getChildren().add(0,root);
        System.out.println(mainContactsVBox.getChildren());
    }
//    private void addSearchFieldListener() {
//        mainSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
//            try {
//                MainContactList mainContactList = new MainContactList(anchorPane,mainContactsScrollPane,mainContactsVBox,id);
//                if (newValue.length() > 0) {
//                    mainContactsVBox.getChildren().clear();
//                    ArrayList<Integer> foundedUsersIds = UsersDataBase.getMatchedUsersIds(UsersDataBase.getNameWithId(id),newValue);
//                    mainContactList.addCustomContactsToList(foundedUsersIds);
//                } else {
//                    mainContactsVBox.getChildren().clear();
//                    mainContactList.addUserContactsToList();
//                }
//            } catch (Exception e) {
//                throw new RuntimeException();
//            }
//        });
//    }


    @FXML
    public void addContactWindow () {
//        try {
//            // Load FXML new contact window ( pane )
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/NewContactWindow.fxml"));
//            Parent newContactRoot = loader.load();
//
//            // Pass the anchor pane of main window to settings controller file
////            NewContactWindow newContactWindow = loader.getController();
////            newContactWindow.setMainAnchorPane(anchorPane);
////            newContactWindow.setMainUserId(id);
////            newContactWindow.setMainAnchorPane(anchorPane);
////            newContactWindow.setContactsVBox(contactsVBox);
////            newContactWindow.setContactsScrollPane(contactsScrollPane);
////            newContactWindow.setMainScrollPane(contactsScrollPane);
////            newContactWindow.setMainContactVBox(contactsVBox);
////            newContactWindow.initializeWithValue();
////
////            anchorPane.getChildren().add(newContactRoot);
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        } catch (SQLException e) {
////            throw new RuntimeException(e);
////        }
    }

    @FXML
    private void openSettingsWindow() {
        try {
            // Load FXML settings window ( pane )
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/SettingsWindow.fxml"));
            Parent settingsRoot = loader.load();

            // Pass the anchor pane of main window to settings controller file
            SettingsWindow settingsController = loader.getController();
            settingsController.setId(id);
            settingsController.setMainAnchorPane(anchorPane);
            settingsController.initializeWithValue();

            anchorPane.getChildren().add(settingsRoot);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void setId(int id) throws SQLException, IOException {
        this.id = id;
        initializeWithValue();
    }

//    public void initialize() throws SQLException, IOException {
//        this.id = 1;
//        initializeWithValue();
//        elementsAreInitialized = true;
//    }
}
