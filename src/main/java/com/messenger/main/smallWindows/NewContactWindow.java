package com.messenger.main.smallWindows;

import com.messenger.database.DetailedDataBase;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NewContactWindow {
    private int mainUserId;
    private String mainUserName;

    private int contactId = -1;

    private AnchorPane mainAnchorPane;
    private ScrollPane mainScrollPane;
    private VBox mainContactVBox;
    private VBox contactsVBox;
    private ScrollPane contactsScrollPane;

    @FXML
    private Pane newContactBackgroundPane;
    @FXML
    private Pane newContactOverlayPane;
    @FXML
    private TextField contactInfoField;
    @FXML
    private Button contactAddButton;
    @FXML
    private Label contactErrorLabel;

    public void initializeWithValue() {
        showOpeningEffect();

        contactErrorLabel.setVisible(false);

        newContactBackgroundPane.setOnMouseClicked(event -> hideWindow());

        newContactOverlayPane.setOnMouseClicked(Event::consume);

        // Checking, whether new is valid and is in database
        contactAddButton.setOnAction(actionEvent -> {

            String identifier = contactInfoField.getText().trim();

            try {
                if (checkValidity(identifier)) {
                    contactId = getIdentifierType(identifier).equals("email") ? UsersDataBase.getIdWithEmail(identifier) : UsersDataBase.getIdWithName(identifier);
                    addContactToMainWindowList();
                    System.out.println("Added to main window list");
                    addContactToDB();
                    hideWindow();
                }
            } catch (SQLException | IOException |  InterruptedException e) {
                contactErrorLabel.setText(e.getMessage());
            }

        });

    }

    public void setMainUserId(int id) throws SQLException {
        this.mainUserId = id;
        setMainUserName(UsersDataBase.getNameWithId(id));
    }
    private void setMainUserName(String name) {
        this.mainUserName = name;
    }
    public void setMainAnchorPane(AnchorPane anchorPane) {
        this.mainAnchorPane = anchorPane;
    }
    public void setContactsVBox(VBox vBox) {
        this.contactsVBox = vBox;
    }
    public void setContactsScrollPane(ScrollPane scrollPane) {
        this.contactsScrollPane = scrollPane;
    }
    public void setMainScrollPane(ScrollPane scrollPane) {
        this.mainScrollPane = scrollPane;
    }
    public void setMainContactVBox(VBox VBox) {
        this.mainContactVBox = VBox;
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

    public void hideWindow() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180),newContactOverlayPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> mainAnchorPane.getChildren().removeAll(newContactBackgroundPane,newContactOverlayPane));
        fadeOut.play();
    }

    private boolean checkValidity(String identifier) {
        try {
            contactErrorLabel.setVisible(false);
            contactInfoField.getStyleClass().clear();
            contactInfoField.getStyleClass().add("add-contact-field");

            String identifierType = getIdentifierType(identifier);
            boolean presenceInDB = UsersDataBase.getUserPresence(identifier);

            if (identifier.isEmpty()) {
                throw new IncorrectIdentifierInformation("Incorrect information");
            }
            if (identifierType.equals("-")) {
                throw new IncorrectIdentifierInformation("Incorrect information");
            }

            if (!presenceInDB || Objects.equals(mainUserName,identifier)) {
                throw new NotInDataBase("The person was not found");
            }

            if (DetailedDataBase.checkUserPresence(mainUserId,identifierType.equals("email") ? UsersDataBase.getNameWithEmail(identifier) : identifier)) {
                throw new AlreadyInDataBase("The contact is already added");
            }

            return true;
        } catch (Exception e) {
            if (identifier.isEmpty()) {
                ShakeAnimation.applyShakeAnimation(contactInfoField);
            }
            contactInfoField.getStyleClass().clear();
            contactInfoField.getStyleClass().add("add-contact-field-error");
            contactErrorLabel.setText(e.getMessage());
            contactErrorLabel.setVisible(true);
            return false;
        }

    }

    private static String getIdentifierType(String identifier) {
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
    private void addContactToMainWindowList() throws SQLException, IOException {
        MainContactList mainContactList = new MainContactList(mainAnchorPane,mainScrollPane,mainContactVBox,mainUserId);
        mainContactList.addContactToList(contactId);
    }

    private void addContactToDB() throws SQLException, IOException, InterruptedException {
        if (contactId != -1) {
            DetailedDataBase.addContactToContactList(mainUserId,contactId);
            DetailedDataBase.createContactTable(mainUserId,contactId);
            UsersDataBase.addContactsAmount(mainUserId);
        }
    }

}
