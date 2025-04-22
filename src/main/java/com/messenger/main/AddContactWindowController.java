package com.messenger.main;

import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddContactWindowController extends MainWindowController {
    @FXML private Pane addContactBackground;
    @FXML private Pane addContactOverlay;
    @FXML private TextField addContactIdentifierField;
    @FXML private Label addContactErrorMessage;
    @FXML private Button addContactButton;

    public void injectUIElements(MainWindowController source) {
        this.mainAnchorPane = source.mainAnchorPane;
        this.mainContactsVBox = source.mainContactsVBox;
    }
    public void initializeAddContactInterface() {
        hideExceptionsLabel();
        setCloseActionOnClickAway();
        consumeOverlayClickEvents();
        setAddContactButtonAction();
        showOpeningEffect();
    }


    // Interface Initialization
    private void hideExceptionsLabel() {
        addContactErrorMessage.setVisible(false);
    }
    private void setCloseActionOnClickAway() {
        addContactBackground.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                hideWindow();
            }
        });
    }
    private void consumeOverlayClickEvents() {
        addContactOverlay.setOnMouseClicked(Event::consume);
    }
    private void setAddContactButtonAction() {
        addContactButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    checkContact();
                } catch (SQLException | IOException e) {
                    addContactErrorMessage.setText(e.getMessage());
                }
            }
        });
    }
    private void showOpeningEffect() {
        FadeTransition FadeIn = new FadeTransition(Duration.millis(180),addContactBackground);
        FadeIn.setFromValue(0);
        FadeIn.setToValue(1);
        FadeIn.play();

        TranslateTransition translateIn = new TranslateTransition(Duration.millis(180),addContactOverlay);
        translateIn.setFromX(0);
        translateIn.setToX(-35);
        translateIn.play();
    }


    // Add Contact Button
    @FXML
    public void checkContact() throws SQLException, IOException {
        boolean isIdentifierValid = getIdentifierValidity().equals("valid");

        if (isIdentifierValid) {
            addContact();
            hideWindow();
        }

    }
    private void addContact() throws SQLException, IOException {
        String identifier = addContactIdentifierField.getText().trim();
        int contactId = getIdentifierType(identifier).equals("name") ? UsersDataBase.getIdWithName(identifier) : UsersDataBase.getIdWithEmail(identifier);
        ContactsDataBase.addContact(mainUserId,contactId);
        addContactPane(contactId);
    }


    // Checking Entered Contact
    private String getIdentifierValidity() throws SQLException {
        boolean identifierExists = UsersDataBase.getUserPresence(addContactIdentifierField.getText().trim());
        boolean isInContactList = identifierExists && contactIsAlreadyAdded(addContactIdentifierField.getText().trim());
        boolean isUserHimself = identifierExists && isUserHimself();

        showDefaultStyle();
        if (!identifierExists) {
            showExceptionStyle("User was not found");
            return "User was not found";
        }
        if (isInContactList) {
            showExceptionStyle("User was already added");
            return "User was already added";
        }
        if (isUserHimself) {
            showExceptionStyle("User can not be added");
            return "User can not be added";
        }

        return "valid";
    }
    private boolean contactIsAlreadyAdded(String identifier) throws SQLException {
        String identifierType = getIdentifierType(identifier);
        int identifierId = identifierType.equals("name") ? UsersDataBase.getIdWithName(identifier) : UsersDataBase.getIdWithEmail(identifier);
        return Arrays.stream(ContactsDataBase.getContactsIdList(mainUserId)).anyMatch(id -> id == identifierId);
    }
    private boolean isUserHimself() throws SQLException {
        String identifier = addContactIdentifierField.getText().trim();
        String identifierType = getIdentifierType(identifier);
        return identifierType.equals("name") ? (identifier.equals(UsersDataBase.getNameWithId(mainUserId))) : (identifier.equals(UsersDataBase.getEmailWithId(mainUserId)));
    }
    private String getIdentifierType(String identifier) {
        String emailPattern = "^.+@\\S*\\.[a-z]{2,}$";
        Pattern emailPatternCompile = Pattern.compile(emailPattern);
        Matcher emailMatcher = emailPatternCompile.matcher(identifier);

        String namePattern = "^[a-zA-Z][a-zA-Z0-9 ]+$";
        Pattern namePatternCompile = Pattern.compile(namePattern);
        Matcher nameMatcher = namePatternCompile.matcher(identifier);

        if (emailMatcher.find()) {
            return "email";
        } else if (nameMatcher.find()) {
            return "name";
        } else {
            return "-";
        }
    }


    // Text Field, Error Label styles
    private void showDefaultStyle() {
        hideExceptionsLabel();
        addContactIdentifierField.getStyleClass().clear();
        addContactIdentifierField.getStyleClass().add("add-contact-field");
    }
    private void showExceptionStyle(String message) {
        addContactIdentifierField.getStyleClass().clear();
        addContactIdentifierField.getStyleClass().add("add-contact-field-error");
        addContactErrorMessage.setVisible(true);
        addContactErrorMessage.setText(message);
    }


    @FXML
    private void hideWindow() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180),addContactOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> mainAnchorPane.getChildren().removeAll(addContactBackground,addContactOverlay));
        fadeOut.play();
    }


}
