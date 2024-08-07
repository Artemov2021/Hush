package com.messenger.main.smallWindows;

import com.messenger.main.MainDataBase;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.sql.SQLException;

public class NewContactWindow {
    private AnchorPane anchorPane;

    public NewContactWindow (AnchorPane anchorPane) {
        this.anchorPane = anchorPane;
    }

    public void openWindow() {
        Pane overlay = new Pane();
        overlay.setPrefWidth(anchorPane.getPrefWidth());
        overlay.setPrefHeight(anchorPane.getPrefHeight());
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        Pane contactPane = new Pane();
        contactPane.setPrefWidth(337);
        contactPane.setPrefHeight(373);
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

        Label secondaryContactLabel = new Label("Fill the following fields to add a new contact");
        secondaryContactLabel.getStyleClass().add("add-contact-secondary-label");
        secondaryContactLabel.setLayoutX(25);
        secondaryContactLabel.setLayoutY(52);

        Button exitButton = new Button();
        exitButton.setPrefWidth(30);
        exitButton.getStyleClass().add("exit-button");
        exitButton.setLayoutX(295);
        exitButton.setLayoutY(15);

        Label contactInfo = new Label("Contact info");
        contactInfo.getStyleClass().add("add-contact-small-label");
        contactInfo.setLayoutX(25);
        contactInfo.setLayoutY(120);

        TextField contactInfoField = new TextField();
        contactInfoField.setPrefHeight(40);
        contactInfoField.setPrefWidth(280);
        contactInfoField.getStyleClass().add("add-contact-field");
        contactInfoField.setLayoutX(25);
        contactInfoField.setLayoutY(145);
        contactInfoField.setPromptText("Phone number or email");

        Label contactName = new Label("User name");
        contactName.getStyleClass().add("add-contact-small-label");
        contactName.setLayoutX(25);
        contactName.setLayoutY(205);

        TextField contactNameField = new TextField();
        contactNameField.setPrefHeight(40);
        contactNameField.setPrefWidth(280);
        contactNameField.getStyleClass().add("add-contact-field");
        contactNameField.setLayoutX(25);
        contactNameField.setLayoutY(230);
        contactNameField.setPromptText("Person's name");

        Button contactAddButton = new Button();
        contactAddButton.setPrefWidth(72);
        contactAddButton.setPrefHeight(30);
        contactAddButton.getStyleClass().add("add-contact-add-button");
        contactAddButton.setLayoutX(235);
        contactAddButton.setLayoutY(310);

        Button contactCancelButton = new Button();
        contactCancelButton.setPrefWidth(78);
        contactCancelButton.setPrefHeight(29);
        contactCancelButton.getStyleClass().add("add-contact-cancel-button");
        contactCancelButton.setLayoutX(148);
        contactCancelButton.setLayoutY(310);

        Label contactErrorLabel = new Label();
        contactErrorLabel.getStyleClass().add("add-contact-error-label");
        contactErrorLabel.setLayoutX(25);
        contactErrorLabel.setLayoutY(275);
        contactErrorLabel.setText("Person was not found");
        contactErrorLabel.setVisible(false);

        // Adds all components to the contact pane
        contactPane.getChildren().addAll(
                addContactLabel,
                secondaryContactLabel,
                exitButton,
                contactInfo,
                contactInfoField,
                contactName,
                contactNameField,
                contactAddButton,
                contactCancelButton,
                contactErrorLabel
        );

        exitButton.setOnAction(actionEvent -> { hideWindow(overlay, contactPane); });
        contactCancelButton.setOnAction(actionEvent -> { hideWindow(overlay, contactPane); });

        contactAddButton.setOnAction(actionEvent -> {
            MainDataBase usersDB = new MainDataBase("jdbc:sqlite:auth.db");
            try {
                if(usersDB.checkPerson(contactInfoField.getText())) {

                } else {
                    throw new SQLException();
                }
            } catch (SQLException e) {
                contactErrorLabel.setVisible(true);
            }
        });

    }
}
