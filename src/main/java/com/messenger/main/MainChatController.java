package com.messenger.main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.*;
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

    public void initializeWithValue() {
        removeTitle();

        chatBackgroundPane.setLayoutX(310);
        chatBackgroundPane.setLayoutY(0);

    }

    public void setMainAnchorPane(AnchorPane anchorPane) {
        this.mainAnchorPane = anchorPane;
    }
    public void setName(String name) {
        chatMainNameLabel.setText(name);
    }

    private void removeTitle() {
        Set<String> titlesToRemove = new HashSet<>(Arrays.asList("mainTitle", "mainSmallTitle", "mainLoginTitle"));

        List<Label> titles = mainAnchorPane.getChildren().stream()
                .filter(node -> node instanceof Label && titlesToRemove.contains(node.getId())) // Check type and ID
                .map(node -> (Label) node) // Cast to Label
                .collect(Collectors.toList()); // Collect into List<Label>
        mainAnchorPane.getChildren().removeAll(titles);
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
