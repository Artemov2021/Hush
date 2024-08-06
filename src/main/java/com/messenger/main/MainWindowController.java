package com.messenger.main;

import com.messenger.Main;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.messenger.design.MainStyling;
import javafx.scene.control.TextField;

public class MainWindowController {
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


    private String name;
    private String email;

    public void initializeWithValue () {

        settingsLabel.setMouseTransparent(true);
        addContactLabel.setMouseTransparent(true);

        nameLabel.setText(name);
        emailLabel.setText(email);

        // If there is no email, name label will be moved down
        if (email.isEmpty()) {
            emailLabel.getStyleClass().clear();
            emailLabel.getStyleClass().add("email-label-invisible");
            nameLabel.setLayoutY(30);
        }

        // If the settings button is hovered, settings label will change
        MainStyling.setHoverStyle(settingsButton,settingsLabel,"settings-label-hovered","settings-label-default");

        // If the search field is focused, the lupe will change the color
        MainStyling.setFocusStyle(searchField,searchLupeLabel,"search-field-lupe-focused","search-field-lupe-default");

        }

        public void setEmail (String email) {
            this.email = email;
            MainDataBase usersDB = new MainDataBase("jdbc:sqlite:auth.db");
            name = usersDB.getNameWithEmail(email);
            initializeWithValue();
        }

        public void setName (String name) {
            this.name = name;
            MainDataBase usersDB = new MainDataBase("jdbc:sqlite:auth.db");
            email = usersDB.getEmailWithName(name);
            initializeWithValue();
        }

        public void addContact() {
            System.out.println("added!");
        }

    public void initialize() {
        email = "timur005@gmail.com";
        name = "ahmed";
        initializeWithValue();
    }
}
