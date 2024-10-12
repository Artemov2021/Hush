//package com.messenger.main;
//
//import com.messenger.database.UsersDataBase;
//import javafx.fxml.FXML;
//import javafx.scene.control.Label;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.AnchorPane;
//import javafx.scene.layout.Pane;
//import javafx.scene.layout.VBox;
//import javafx.scene.shape.Circle;
//
//import java.io.File;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.sql.SQLException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Locale;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class DialogController {
//    private int contactId;
//    private int mainUserId;
//    private AnchorPane mainAnchorPane;
//    @FXML
//    private Pane dialogBackgroundPane;
//    @FXML
//    private Label contactAvatarLabel;
//    @FXML
//    private Label contactNameLabel;
//    @FXML
//    private VBox dialogVBox;
//
//    @FXML
//    private Label timeDialogBorderLabel;
//
//    public void initializeWithValue() throws MalformedURLException, SQLException {
//        // delete login title, if needs
//        Label loginTitle = (Label) mainAnchorPane.getScene().lookup("#loginTitle");
//        if (loginTitle != null)
//            loginTitle.setVisible(false);
//
//        // set correct dialog pane position
//        dialogBackgroundPane.setLayoutX(311);
//        dialogBackgroundPane.setLayoutY(1);
//
//        // set avatar on block above
//        URL avatarUrl = new File("src/main/resources/avatars" + File.separator + UsersDataBase.getAvatarWithId(contactId)).toURL();
//        if (UsersDataBase.getAvatarWithId(contactId)==null) {
//            contactAvatarLabel.setGraphic(null);
//            contactAvatarLabel.getStyleClass().clear();
//            contactAvatarLabel.getStyleClass().add("avatar-label");
//        } else {
//            contactAvatarLabel.getStyleClass().clear();
//            setAvatarPicture(contactAvatarLabel,avatarUrl,33);
//        }
//
//        // set name
//        contactNameLabel.setText(UsersDataBase.getNameWithId(contactId));
//
//        // set time label between message history
//        if (DetailedDataBase.getLastMessage(mainUserId,contactId) == null) {
//            setCurrentDateHistoryLabel(); // if the user has no messages in the dialog
//        } else {
//            loadMessageHistory(); // if the user HAS messages in the dialog
//        }
//
//
//    }
//
//
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
//}
