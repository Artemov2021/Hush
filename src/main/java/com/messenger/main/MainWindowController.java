package com.messenger.main;

import com.messenger.Log;
import com.messenger.database.DetailedDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.main.smallWindows.NewContactWindow;
import com.messenger.main.smallWindows.SettingsWindow;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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


    private int id;
    private String name;
    private String email;

    public void initializeWithValue () throws SQLException, IOException {
        settingsLabel.setMouseTransparent(true);
        addContactLabel.setMouseTransparent(true);

        nameLabel.setText(name);
        emailLabel.setText(email);

        // It creates a detailed database of user, if for some reason there is no
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
        this.id = UsersDataBase.getIdWithName(name);
        initializeWithValue();
    }

    public void setName (String name) throws SQLException, IOException {
        this.name = name;
        this.id = UsersDataBase.getIdWithName(name);
        initializeWithValue();
    }

    public void addContactWindow () {
        NewContactWindow newContactWindow = new NewContactWindow(anchorPane,nameLabel.getText());
        newContactWindow.openWindow();
    }

    @FXML
    private void openSettingsWindow() {
        try {
            // Load FXML settings window ( pane )
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/SettingsWindow.fxml"));
            Parent root = loader.load();

            // Pass the anchor pane of main window to settings controller file
            SettingsWindow settingsController = loader.getController();
            settingsController.setMainAnchorPane(anchorPane);
            settingsController.setName(UsersDataBase.getNameWithId(id));
            settingsController.initializeWithValue();

            anchorPane.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize() throws SQLException, IOException {
        name = UsersDataBase.getNameWithId(1);
        email = UsersDataBase.getEmailWithName(name);
        this.id = 1;
        initializeWithValue();
    }
}
