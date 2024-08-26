package com.messenger.main;

import com.messenger.database.DetailedDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.main.smallWindows.NewContactWindow;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.messenger.design.MainStyling;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainWindowController {
    @FXML
    private AnchorPane anchorPane;
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
    private Button addContactButton;
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
        settingsLabel.setMouseTransparent(true);
        addContactLabel.setMouseTransparent(true);

        nameLabel.setText(name);
        emailLabel.setText(email);

        // If there is no email, email label will become invisible and name label will be moved down
        if (email == null) {
            emailLabel.getStyleClass().clear();
            emailLabel.getStyleClass().add("email-label-invisible");
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
        NewContactWindow newContactWindow = new NewContactWindow(anchorPane,name);
        newContactWindow.openWindow();
    }
//    public void initialize() throws SQLException, IOException {
//        name = "Andrew Tate";
//
//        initializeWithValue();
//    }
}
