package com.messenger.main;

import com.messenger.database.DetailedDataBase;
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

import java.io.IOException;
import java.net.URL;
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
    private Label settingsLabel;
    @FXML
    private Button settingsButton;
    @FXML
    private TextField searchField;
    @FXML
    private Label searchLupeLabel;
    @FXML
    private Label addContactLabel;
    @FXML
    private Label mainTitle;
    @FXML
    private Label smallTitle;
    @FXML
    public ScrollPane contactsScrollPane;
    @FXML
    public VBox contactsVBox;

    public boolean elementsAreInitialized = false;

    public int id;

    public void initializeWithValue () throws SQLException, IOException {
        settingsLabel.setMouseTransparent(true);
        addContactLabel.setMouseTransparent(true);

        mainNameLabel.setText(UsersDataBase.getNameWithId(id));
        mainEmailLabel.setText(UsersDataBase.getEmailWithId(id));

        // It creates a detailed database of user, if for some reason there is no
        DetailedDataBase.createUserDataBase(id);

        if (UsersDataBase.getAvatarWithId(id) != null) {
            String avatarUrl = "/avatars/" + UsersDataBase.getAvatarWithId(id);
            URL url = MainContactList.class.getResource(avatarUrl);
            assert url != null;
            ImageView imageView = new ImageView(new Image(url.toString().replaceAll("target/classes","src/main/resources")));
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

        // If there is no email, email label will become invisible and name label will be moved down
        if (UsersDataBase.getEmailWithId(id) == null) {
            mainEmailLabel.setVisible(false);
            mainNameLabel.setLayoutY(30);
        }

        // If the settings button is hovered, settings label will change
        MainStyling.setHoverStyle(settingsButton,settingsLabel,"settings-label-hovered","settings-label-default");

        // If the search field is focused, the lupe will change the color
        MainStyling.setFocusStyle(searchField,searchLupeLabel,"search-field-lupe-focused","search-field-lupe-default");

        if (UsersDataBase.getContactsAmount(id) > 0) {
            mainTitle.setVisible(false);
            smallTitle.setVisible(false);
            Label title = new Label();
            title.setLayoutX(610);
            title.setLayoutY(300);
            title.getStyleClass().add("login-title");
            title.setId("loginTitle");
            title.setPrefWidth(300);
            title.setPrefHeight(51);
            anchorPane.getChildren().add(title);

            MainContactList mainContactList = new MainContactList(anchorPane,contactsScrollPane,contactsVBox,id);
            mainContactList.addUserContactsToList();
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                MainContactList mainContactList = new MainContactList(anchorPane,contactsScrollPane,contactsVBox,id);
                if (newValue.length() > 0) {
                    contactsVBox.getChildren().clear();
                    ArrayList<Integer> foundedUsersIds = UsersDataBase.getMatchedUsersIds(UsersDataBase.getNameWithId(id),newValue);
                    mainContactList.addCustomContactsToList(foundedUsersIds);
                } else {
                    contactsVBox.getChildren().clear();
                    mainContactList.addUserContactsToList();
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });

    }
    @FXML
    public void addContactWindow () {
        try {
            // Load FXML new contact window ( pane )
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/NewContactWindow.fxml"));
            Parent newContactRoot = loader.load();

            // Pass the anchor pane of main window to settings controller file
            NewContactWindow newContactWindow = loader.getController();
            newContactWindow.setMainAnchorPane(anchorPane);
            newContactWindow.setMainUserId(id);
            newContactWindow.setMainAnchorPane(anchorPane);
            newContactWindow.setContactsVBox(contactsVBox);
            newContactWindow.setContactsScrollPane(contactsScrollPane);
            newContactWindow.initializeWithValue();

            anchorPane.getChildren().add(newContactRoot);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
    public void setId(int id) {
        this.id = id;
    }

    public void initialize() throws SQLException, IOException {
        this.id = 1;
        initializeWithValue();
        elementsAreInitialized = true;
    }
}
