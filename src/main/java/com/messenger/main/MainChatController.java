package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import com.messenger.main.chat.ChatHistory;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.shape.Rectangle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.Cursor;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainChatController {
    @FXML
    private Pane chatBackgroundPane;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    public Label chatMainAvatarLabel;
    @FXML
    public Label chatMainNameLabel;
    @FXML
    public VBox chatVBox;
    @FXML
    public TextField chatTextField;


    public AnchorPane mainAnchorPane;
    public int contactId;
    public int mainUserId;
    public Pane mainContactPane;
    public Label mainContactMessageLabel;
    public Label mainContactTimeLabel;


    private String sendingMessageType = "text"; // the default type of the message is always text
    private int editedMessageId = -1;
    private int repliedMessageId = -1;


    private String pathToPicture = "";
    private String messageToThePicture = "";


    private boolean initializedWithValue = false;


    // set main value
    public void setMainAnchorPane(AnchorPane anchorPane) {
        this.mainAnchorPane = anchorPane;
    }
    public void setMainUserId(int id) {
        this.mainUserId = id;
    }
    public void setContactId(int id) {
        this.contactId = id;
    }
    public void setMainContactPane(Pane mainContactPane) {
        this.mainContactPane = mainContactPane;
        this.mainContactMessageLabel = (Label) mainContactPane.lookup("#mainContactMessageLabel");
        this.mainContactTimeLabel = (Label) mainContactPane.lookup("#mainContactTimeLabel");
    }


    // initialization of the chat window
    public void initializeWithValue() throws SQLException, ExecutionException, InterruptedException {
        initializeChatInterface();
        loadChatHistory();
    }


    // chat interface
    private void initializeChatInterface() throws SQLException {
        removeTitle();
        setChatPosition();
        checkForWrappers();
        setProfilePicture();
        setName();
        applyScrollBarEffect(chatScrollPane);
        setMessageSpacing(5);
        setChatTextFieldFocus();
    }
    private void removeTitle() {
        Set<String> titlesToRemove = new HashSet<>(Arrays.asList("mainTitle", "mainSmallTitle", "logInTitle"));

        List<Label> titles = mainAnchorPane.getChildren().stream()
                .filter(node -> node instanceof Label && titlesToRemove.contains(node.getId())) // Check type and ID
                .map(node -> (Label) node)
                .toList(); // Collect into List<Label>
        mainAnchorPane.getChildren().removeAll(titles);
    }
    private void setChatPosition() {
        chatBackgroundPane.setLayoutX(461);
    }
    private void checkForWrappers() {
        Pane replyPane = (Pane) mainAnchorPane.lookup("#reply-wrapper");
        Pane changePane = (Pane) mainAnchorPane.lookup("#reply-wrapper");
        if (replyPane != null) mainAnchorPane.getChildren().remove(replyPane);
        if (changePane != null) mainAnchorPane.getChildren().remove(changePane);
    }
    private void setProfilePicture() throws SQLException {
        if (UsersDataBase.getAvatarWithId(contactId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(contactId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(42);
            imageView.setFitWidth(42);
            imageView.setSmooth(true);
            chatMainAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(21);
            clip.setLayoutY(21);
            clip.setRadius(21);
            chatMainAvatarLabel.setClip(clip);
        }
    }
    private void setName() throws SQLException {
        chatMainNameLabel.setText(UsersDataBase.getNameWithId(contactId));
    }
    private void applyScrollBarEffect(ScrollPane scrollPane) {
        ScrollPaneEffect.addScrollBarEffect(scrollPane);
    }
    private void setMessageSpacing(double space) {
        chatVBox.setSpacing(space);
    }
    private void setChatTextFieldFocus() {
        Platform.runLater(() -> {
            if (chatTextField.isVisible() && !chatTextField.isDisabled()) {
                chatTextField.requestFocus();
            }
        });
    }


    // chat message history
    private void loadChatHistory() throws SQLException, ExecutionException, InterruptedException {
        ChatHistory currentChatHistory = new ChatHistory(mainUserId,contactId,chatScrollPane,chatVBox,mainAnchorPane);
        currentChatHistory.load();





    }











}
