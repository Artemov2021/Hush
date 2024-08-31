package com.messenger.main;

import com.messenger.Log;
import com.messenger.database.DetailedDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.main.smallWindows.NewContactWindow;
import com.messenger.main.smallWindows.SettingsWindow;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.messenger.design.MainStyling;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainWindowController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label avatarLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
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
    private ScrollPane contactsScrollPane;
    @FXML
    private VBox contactsVBox;


    private String name;
    private String email;

    public void initializeWithValue () throws SQLException, IOException {
        Log.writeNewActionLog("Status: singed up/logged in successfully!\n");
        Log.writeNewActionLog(String.format("\n%0" + 65 + "d" + "\n",0).replace("0","-"));
        Log.writeNewActionLog(String.format("%35s\n","Main Window"));
        Log.writeNewActionLog(String.format("User: %s\n",name));
        Log.writeNewActionLog(String.format("Email: %s\n",email==null?"-":email));

        settingsLabel.setMouseTransparent(true);
        addContactLabel.setMouseTransparent(true);

        nameLabel.setText(name);
        emailLabel.setText(email);

        DetailedDataBase.createUserDataBase(name);

        if (UsersDataBase.getAvatar(name) != null) {
            String avatarUrl = "/avatars/" + UsersDataBase.getAvatar(name);
            URL url = MainContactList.class.getResource(avatarUrl);
            ImageView imageView = new ImageView(new Image(url.toString().replaceAll("target/classes","src/main/resources")));
            imageView.setFitHeight(34);
            imageView.setFitWidth(34);
            imageView.setSmooth(true);
            avatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(17);
            clip.setLayoutY(17);
            clip.setRadius(17);
            avatarLabel.setClip(clip);
        } else {
            avatarLabel.getStyleClass().clear();
            avatarLabel.getStyleClass().add("avatar-button-default");
        }

        // If there is no email, email label will become invisible and name label will be moved down
        if (email == null) {
            emailLabel.setVisible(false);
            nameLabel.setLayoutY(30);
        }

        // If the settings button is hovered, settings label will change
        MainStyling.setHoverStyle(settingsButton,settingsLabel,"settings-label-hovered","settings-label-default");

        // If the search field is focused, the lupe will change the color
        MainStyling.setFocusStyle(searchField,searchLupeLabel,"search-field-lupe-focused","search-field-lupe-default");

        if (UsersDataBase.getContactsAmount(name) > 0) {
            mainTitle.setVisible(false);
            smallTitle.setVisible(false);
            Label title = new Label();
            title.setLayoutX(610);
            title.setLayoutY(300);
            title.getStyleClass().add("login-title");
            title.setPrefWidth(300);
            title.setPrefHeight(51);
            anchorPane.getChildren().add(title);

            MainContactList.addContactsToList(contactsScrollPane,contactsVBox,name);
            Log.writeNewActionLog(String.format("All contacts were displayed (%d)\n",UsersDataBase.getContactsAmount(name)));
        }
    }

    public void setEmail (String email) throws SQLException, IOException {
        this.email = email;
        name = UsersDataBase.getNameWithEmail(email);
        initializeWithValue();
    }

    public void setName (String name) throws SQLException, IOException {
        this.name = name;
        initializeWithValue();
    }

    public void addContactWindow () {
        NewContactWindow newContactWindow = new NewContactWindow(anchorPane,nameLabel.getText());
        newContactWindow.openWindow();
    }

    public void settings() throws SQLException, IOException {
        SettingsWindow settingsWindow = new SettingsWindow(nameLabel.getText(),anchorPane);
        settingsWindow.openWindow();
    }

    public void initialize() throws SQLException, IOException {
        name = UsersDataBase.getNameWithId(1);
        email = UsersDataBase.getEmailWithName(name);
        initializeWithValue();
    }
}
