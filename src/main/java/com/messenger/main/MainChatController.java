package com.messenger.main;

import com.messenger.database.UsersDataBase;
import com.messenger.design.ScrollPaneEffect;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.swing.text.Position;
import java.awt.*;
import java.awt.Button;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainChatController {
    @FXML
    private Pane chatBackgroundPane;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private Label chatMainAvatarLabel;
    @FXML
    private Label chatMainNameLabel;
    @FXML
    private VBox chatVBox;
    @FXML
    private Label chatDateLabel;
    @FXML
    private TextField chatTextField;

    private AnchorPane mainAnchorPane;
    private int contactId;
    private int mainUserId;

    private final int messageLabelWidthMax = 230;

    public void initializeWithValue() throws SQLException {
        setChatPosition();
        removeTitle();
        setProfilePicture();
        setName();
        setDateLabelSpacing();
        setMessageSpacing(2);
        removeHorizontalScrollBar();
        ScrollPaneEffect.addScrollBarEffect(chatScrollPane);
    }


    public void setMainAnchorPane(AnchorPane anchorPane) {
        this.mainAnchorPane = anchorPane;
    }
    public void setMainUserId(int id) {
        this.mainUserId = id;
    }
    public void setContactId(int id) {
        this.contactId = id;
    }


    private void setChatPosition() {
        chatBackgroundPane.setLayoutX(310);
    }
    private void removeTitle() {
        Set<String> titlesToRemove = new HashSet<>(Arrays.asList("mainTitle", "mainSmallTitle", "mainLoginTitle"));

        List<Label> titles = mainAnchorPane.getChildren().stream()
                .filter(node -> node instanceof Label && titlesToRemove.contains(node.getId())) // Check type and ID
                .map(node -> (Label) node) // Cast to Label
                .collect(Collectors.toList()); // Collect into List<Label>
        mainAnchorPane.getChildren().removeAll(titles);
    }
    private void setProfilePicture() throws SQLException {
        if (UsersDataBase.getAvatarWithId(contactId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(contactId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(33);
            imageView.setFitWidth(33);
            imageView.setSmooth(true);
            chatMainAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(16.5F);
            clip.setLayoutY(16.5F);
            clip.setRadius(16.5F);
            chatMainAvatarLabel.setClip(clip);
        }
    }
    private void setName() throws SQLException {
        chatMainNameLabel.setText(UsersDataBase.getNameWithId(contactId));
    }
    private void setDateLabelSpacing() {
        VBox.setMargin(chatDateLabel,new Insets(10,0,10,0));
    }
    private void setMessageSpacing(double space) {
        chatVBox.setSpacing(space);
    }
    private void removeHorizontalScrollBar() {
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    @FXML
    public void sendMessage() throws SQLException {
        String messageText = chatTextField.getText().trim();



        Label messageLabel = new Label(messageText);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("chat-message-label");
        messageLabel.setMaxWidth(272);


        HBox messageHBox = new HBox();
        messageHBox.setAlignment(Pos.TOP_RIGHT);
        messageHBox.setStyle("-fx-background-color: blue;");





        HBox.setMargin(messageLabel, new Insets(0, 40, 0, 0));
        messageHBox.getChildren().add(messageLabel);
        chatVBox.getChildren().add(messageHBox);


        messageLabel.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            double labelHeight = messageLabel.getHeight();
            messageLabel.setMinHeight(labelHeight);
            System.out.println("Label Height: "+labelHeight);
        });

        messageHBox.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            double hboxHeight = messageHBox.getHeight();
            messageHBox.setMinHeight(hboxHeight);
            System.out.println("HBox Height: "+hboxHeight);
        });



        chatVBox.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            chatScrollPane.setVvalue(1.0); // прокрутка в самый низ
        });

    }













//    public void setMainAnchorPane(AnchorPane anchorPane) {
//        mainAnchorPane = anchorPane;
//    }
//    public void setContactId(int id) {
//        this.contactId = id;
//    }
//    public void setMainUserId(int id) {
//        this.mainUserId = id;
//    }
//
//    private void setAvatarPicture(Label avatar, URL imageURL, int size) {
//        ImageView imageView = new ImageView(new Image(imageURL.toString()));
//        imageView.setFitHeight(size);
//        imageView.setFitWidth(size);
//        imageView.setSmooth(true);
//        avatar.setGraphic(imageView);
//        Circle clip = new Circle();
//        clip.setLayoutX((double) size / 2);
//        clip.setLayoutY((double) size / 2);
//        clip.setRadius((double) size / 2);
//        avatar.setClip(clip);
//    }
//    private void loadMessageHistory() throws SQLException {
//        ArrayList<ArrayList<String>> messages = DetailedDataBase.getMessages(mainUserId,contactId);
//        setDateHistoryLabel(getHours(messages.get(0).get(2)));
//        for (ArrayList<String> message : messages) {
//            //setDateHistoryLabel(getHours(message.get(2)));
//        }
//    }
//
//
//    private void setCurrentDateHistoryLabel() {
//        LocalDate today = LocalDate.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
//        String formattedDate = today.format(formatter);
//        timeDialogBorderLabel.setText(formattedDate);
//    }
//    private void setDateHistoryLabel(String date) {
//        LocalDate today = LocalDate.parse(date);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
//        String formattedDate = today.format(formatter);
//        timeDialogBorderLabel.setText(formattedDate);
//    }
//    private String getHours(String fulldate) {
//        String datePattern = "^(\\d+)-(\\d+)-(\\d+)";
//        Pattern datePatternCompiled = Pattern.compile(datePattern);
//        Matcher matcher = datePatternCompiled.matcher(fulldate);
//        if (matcher.find()) {
//            return matcher.group();
//        }
//        return null;
//    }
}
