package com.messenger.main.smallWindows;

import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ShakeAnimation;
import com.messenger.exceptions.AlreadyInDataBase;
import com.messenger.exceptions.IncorrectIdentifierInformation;
import com.messenger.exceptions.NotInDataBase;
import com.messenger.main.MainContactList;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NewContactWindow {
    @FXML
    private Pane newContactBackgroundPane;
    @FXML
    private Pane newContactOverlayPane;
    @FXML
    private TextField newContactInfoField;
    @FXML
    private Button newContactAddButton;
    @FXML
    private Button newContactCancelButton;
    @FXML
    private Label newContactExceptionLabel;
    @FXML
    private Button newContactExitButton;

    private int mainUserId;
    private AnchorPane mainAnchorPane;
    private VBox mainContactsVBox;

    public void initializeWithValue() {
        // window opens with an effect
        showOpeningEffect();

        // hide all exception labels ( labels for error output )
        hideExceptionsLabel();

        // close window by clicking away ( on the transparent background pane )
        newContactBackgroundPane.setOnMouseClicked(event -> hideWindow());

        // debugging closing window by clicking on the new contact window pane
        newContactOverlayPane.setOnMouseClicked(Event::consume);

        // Checking, whether new contact is valid
        newContactAddButton.setOnAction(actionEvent -> {
            checkInformation();
        });

    }
    private void showOpeningEffect() {
        FadeTransition FadeIn = new FadeTransition(Duration.millis(180),newContactBackgroundPane);
        FadeIn.setFromValue(0);
        FadeIn.setToValue(1);
        FadeIn.play();

        TranslateTransition translateIn = new TranslateTransition(Duration.millis(180),newContactOverlayPane);
        translateIn.setFromX(0);
        translateIn.setToX(-35);
        translateIn.play();
    }
    private void hideExceptionsLabel() {
        newContactExceptionLabel.setVisible(false);
    }
    private void checkInformation() {
        try {
            String identifier = newContactInfoField.getText().trim();
            if (identifierIsValid(identifier)) {
                int contactId = getIdentifierType(identifier).equals("name") ? UsersDataBase.getIdWithName(identifier) : UsersDataBase.getIdWithEmail(identifier);
                ContactsDataBase.addContact(mainUserId,contactId);
                MainContactList.addContactToList(mainUserId,contactId,mainContactsVBox);
                hideWindow();
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }


    public void setMainUserId(int id) {
        this.mainUserId = id;
    }
    public void setMainAnchorPane(AnchorPane anchorPane) {
        this.mainAnchorPane = anchorPane;
    }
    public void setMainContactsVBox(VBox VBox) {
        this.mainContactsVBox = VBox;
    }



    private boolean identifierIsValid(String identifier) throws SQLException {
        cleanExceptionStyle();

        String identifierType = getIdentifierType(identifier);

        if (identifier.isEmpty()) {
            showExceptionStyle("Incorrect information");
            return false;
        }

        if (identifierType.equals("-") || !UsersDataBase.getUserPresence(identifier) || identifier.equals(UsersDataBase.getNameWithId(mainUserId))) {
            showExceptionStyle("The person was not found");
            return false;
        }

        if (contactIsAlreadyAdded(identifier)) {
            showExceptionStyle("The person is already added");
            return false;
        }

        return true;
    }
    @FXML
    private void hideWindow() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180),newContactOverlayPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> mainAnchorPane.getChildren().removeAll(newContactBackgroundPane,newContactOverlayPane));
        fadeOut.play();
    }
    private void cleanExceptionStyle() {
        hideExceptionsLabel();
        newContactInfoField.getStyleClass().clear();
        newContactInfoField.getStyleClass().add("add-contact-field");
    }
    private void showExceptionStyle(String reason) {
        newContactInfoField.getStyleClass().clear();
        newContactInfoField.getStyleClass().add("add-contact-field-error");
        newContactExceptionLabel.setVisible(true);
        newContactExceptionLabel.setText(reason);
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
    private boolean contactIsAlreadyAdded(String identifier) throws SQLException {
        String identifierType = getIdentifierType(identifier);
        assert !identifierType.equals("-");
        int identifierId = identifierType.equals("name") ? UsersDataBase.getIdWithName(identifier) : UsersDataBase.getIdWithEmail(identifier);
        return Arrays.stream(ContactsDataBase.getContactsIdList(mainUserId)).anyMatch(id -> id == identifierId);
    }


}
