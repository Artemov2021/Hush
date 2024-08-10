package com.messenger.main.smallWindows;

import com.messenger.database.DetailedDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.exceptions.AlreadyInDataBase;
import com.messenger.exceptions.IncorrectIdentifierInformation;
import com.messenger.exceptions.IncorrectWholeInformation;
import com.messenger.exceptions.NotInDataBase;
import com.messenger.main.MainContactList;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.NotActiveException;
import java.util.Arrays;
import java.util.HashSet;

import java.sql.SQLException;
import java.util.Set;


public class NewContactWindow {
    private String name;
    private AnchorPane anchorPane;

    public NewContactWindow (AnchorPane anchorPane, String name) {
        this.anchorPane = anchorPane;
        this.name = name;
    }

    public void openWindow() {
        Pane overlay = new Pane();
        overlay.setPrefWidth(anchorPane.getPrefWidth());
        overlay.setPrefHeight(anchorPane.getPrefHeight());
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        Pane contactPane = new Pane();
        contactPane.setPrefWidth(337);
        contactPane.setPrefHeight(283);
        contactPane.getStyleClass().add("add-contact-window");
        contactPane.setLayoutX(475);
        contactPane.setLayoutY(160);

        FadeTransition FadeIn = new FadeTransition(Duration.millis(180),overlay);
        FadeIn.setFromValue(0);
        FadeIn.setToValue(1);
        FadeIn.play();

        TranslateTransition translateIn = new TranslateTransition(Duration.millis(180),contactPane);
        translateIn.setFromX(0);
        translateIn.setToX(-35);
        translateIn.play();

        setContactWindowComponents(overlay,contactPane);
        anchorPane.getChildren().addAll(overlay,contactPane);

        overlay.setOnMouseClicked(event -> {
            hideWindow(overlay,contactPane);
        });
    }

    private void hideWindow(Pane overlay, Pane contactPane) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), contactPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            anchorPane.getChildren().removeAll(overlay,contactPane);
        });
        fadeOut.play();
    }

    private void setContactWindowComponents(Pane overlay, Pane contactPane) {
        Label addContactLabel = new Label("Add a contact");
        addContactLabel.getStyleClass().add("add-contact-main-label");
        addContactLabel.setLayoutX(25);
        addContactLabel.setLayoutY(25);

        Label secondaryContactLabel = new Label("Fill the following field to add a new contact");
        secondaryContactLabel.getStyleClass().add("add-contact-secondary-label");
        secondaryContactLabel.setLayoutX(25);
        secondaryContactLabel.setLayoutY(52);

        Button exitButton = new Button();
        exitButton.setPrefWidth(30);
        exitButton.getStyleClass().add("exit-button");
        exitButton.setLayoutX(295);
        exitButton.setLayoutY(15);

        Label contactInfoLabel = new Label("Contact info");
        contactInfoLabel.getStyleClass().add("add-contact-small-label");
        contactInfoLabel.setLayoutX(25);
        contactInfoLabel.setLayoutY(110);

        TextField contactInfoField = new TextField();
        contactInfoField.setPrefHeight(40);
        contactInfoField.setPrefWidth(280);
        contactInfoField.getStyleClass().add("add-contact-field");
        contactInfoField.setLayoutX(25);
        contactInfoField.setLayoutY(135);
        contactInfoField.setPromptText("Name or phone number");

        Button contactAddButton = new Button();
        contactAddButton.setPrefWidth(72);
        contactAddButton.setPrefHeight(30);
        contactAddButton.getStyleClass().add("add-contact-add-button");
        contactAddButton.setLayoutX(235);
        contactAddButton.setLayoutY(230);

        Button contactCancelButton = new Button();
        contactCancelButton.setPrefWidth(78);
        contactCancelButton.setPrefHeight(29);
        contactCancelButton.getStyleClass().add("add-contact-cancel-button");
        contactCancelButton.setLayoutX(148);
        contactCancelButton.setLayoutY(230);

        Label contactErrorLabel = new Label();
        contactErrorLabel.getStyleClass().add("add-contact-error-label");
        contactErrorLabel.setLayoutX(25);
        contactErrorLabel.setLayoutY(175);
        contactErrorLabel.setText("Person was not found");
        contactErrorLabel.setVisible(false);

        // Adds all components to the contact pane
        contactPane.getChildren().addAll(
                addContactLabel,
                secondaryContactLabel,
                exitButton,
                contactInfoLabel,
                contactInfoField,
                contactAddButton,
                contactCancelButton,
                contactErrorLabel
        );

        exitButton.setOnAction(actionEvent -> { hideWindow(overlay, contactPane); });
        contactCancelButton.setOnAction(actionEvent -> { hideWindow(overlay, contactPane); });

        // Checking, whether new contact exists / is in database
        contactAddButton.setOnAction(actionEvent -> {

            // checking, whether new contact user is in database, if true, addContact will be called automatically
            String info = contactInfoField.getText().trim();

            try {
                if (checkValidity(info, contactInfoField, contactErrorLabel, overlay, contactPane)) {
                    String infoType = isPhoneNumber(info) ? "phone number" : "name";
                    String contactName = infoType.contains("phone number") ? UsersDataBase.getNameWithPhoneNumber(convertToPhoneStyle(info)) : info;
                    MainContactList.addContactToList((ScrollPane) anchorPane.getScene().lookup("#contactsScrollPane"), (VBox) anchorPane.getScene().lookup("#contactsVBox"), name, contactName);
                }
            } catch (SQLException e) {
                contactErrorLabel.setText(e.getMessage());
            }

        });

    }



    private boolean checkValidity(String info,TextField field,Label errorLabel,Pane overlay,Pane contactPane) {
        try {
            errorLabel.setVisible(false);
            field.getStyleClass().clear();
            field.getStyleClass().add("add-contact-field");

            if (info.isEmpty()) {
                throw new IncorrectIdentifierInformation("Incorrect information");
            }

            String infoType = isPhoneNumber(convertToPhoneStyle(info)) ? "phone number" : "name";
            boolean presenceInDB = infoType.equals("phone number") ? UsersDataBase.checkPhonePresence(convertToPhoneStyle(info)) : UsersDataBase.checkNamePresence(info);

            if (!presenceInDB) {
                throw new NotInDataBase("The person was not found");
            }

            if (DetailedDataBase.checkUserPresence(name,isPhoneNumber(convertToPhoneStyle(info)) ? UsersDataBase.getNameWithPhoneNumber(convertToPhoneStyle(info)) : info)) {
                throw new AlreadyInDataBase("The contact is already added");
            }

            addContact(isPhoneNumber(info) ? convertToPhoneStyle(info) : info);
            hideWindow(overlay,contactPane);
            return true;
        } catch (SQLException e) {

            // applying error style on field and label
            field.getStyleClass().clear();
            field.getStyleClass().add("add-contact-field-error");
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            return false;

        } catch (IncorrectIdentifierInformation | AlreadyInDataBase | NotInDataBase e) {

            field.getStyleClass().clear();
            field.getStyleClass().add("add-contact-field-error");
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            return false;

        }
    }

    private boolean isPhoneNumber(String info) {
        String[] symbols = {"1","2","3","4","5","6","7","8","9","0","+","-"," "};
        Set<String> allowedSymbols = new HashSet<>(Arrays.asList(symbols));
        for (char s : info.toCharArray()) {
            if (!allowedSymbols.contains(String.valueOf(s))) {
                return false;
            }
        }
        return true;
    }


    // convert to a phone number style, that is in data base
    // Input: 45 283 1843645
    // Converted: +452831843645
    private String convertToPhoneStyle(String phoneNumber) {
        String convertedPhoneNumber = String.valueOf(phoneNumber.charAt(0)).equals("+") ? "" : "+";
        for (int i = 0;i < phoneNumber.length();i++) {
            if (!String.valueOf(phoneNumber.charAt(i)).equals(" ")) {
                convertedPhoneNumber += phoneNumber.charAt(i);
            }
        }
        return convertedPhoneNumber;
    }

    private void addContact(String info) throws SQLException{
        DetailedDataBase.addContact(name,info);
    }

}
