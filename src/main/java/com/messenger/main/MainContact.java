package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.main.smallWindows.NewContactWindow;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainContact {
    @FXML
    private Pane mainContactPane;
    @FXML
    private Label mainContactAvatarLabel;
    @FXML
    private Label mainContactNameLabel;
    @FXML
    private Label mainContactMessageLabel;
    @FXML
    private Label mainContactTimeLabel;

    private AnchorPane mainAnchorPane;
    private int mainUserId;

    public void setMainUserId(int id) {
        this.mainUserId = id;
    }
    public void setName(String name) {
        mainContactNameLabel.setText(name);
    }
    public void setAvatar(int contactId) throws SQLException {
        if (UsersDataBase.getAvatarWithId(contactId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(contactId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(38);
            imageView.setFitWidth(38);
            imageView.setSmooth(true);
            mainContactAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(19);
            clip.setLayoutY(19);
            clip.setRadius(19);
            mainContactAvatarLabel.setClip(clip);
        }
    }
    public void setMessage(String message) {
        mainContactMessageLabel.setText(message);
    }
    public void setTime(String time) {
        String pattern = "\\d\\d:\\d\\d$";
        Pattern compliedPattern = Pattern.compile(pattern);
        Matcher matcher = compliedPattern.matcher(time);
        if (matcher.find()) {
            mainContactTimeLabel.setText(matcher.group());
        } else {
            mainContactTimeLabel.setText("");
        }
    }
    public void setPaneId(String name) {
        mainContactPane.setId("mainContactPane"+name);
    }
    public void setMainAnchorPane(AnchorPane anchorPane) {
        this.mainAnchorPane = anchorPane;
    }
    @FXML
    public void showChat() throws IOException, SQLException {
        int currentUserId = Integer.parseInt(mainContactPane.getId().split("mainContactPane")[1]);

        setPanesNormalStyle();
        setCurrentPaneFocusedStyle();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainChat.fxml"));
        Parent chatRoot = fxmlLoader.load();

        MainChatController mainChatController = fxmlLoader.getController();
        mainChatController.setMainAnchorPane(mainAnchorPane);
        mainChatController.setMainUserId(mainUserId);
        mainChatController.setContactId(currentUserId);
        mainChatController.initializeWithValue();

        mainAnchorPane.getChildren().removeIf(child -> Objects.equals(child.getId(), "chatAnchorPane"));
        mainAnchorPane.getChildren().add(0,chatRoot);
    }


    private void setPanesNormalStyle() {
        VBox mainVBox = (VBox) mainAnchorPane.lookup("#mainContactsVBox");

        mainVBox.getChildren().stream()
                .filter(node -> node instanceof AnchorPane)
                .map(node -> (AnchorPane) node)
                .flatMap(anchorPane -> anchorPane.getChildren().stream())
                .filter(child -> child instanceof Pane)
                .map(child -> (Pane) child)
                .forEach(pane -> {
                    pane.getStyleClass().setAll("contact-background-pane");
                });
    }
    private void setCurrentPaneFocusedStyle() {
        mainContactPane.getStyleClass().clear();
        mainContactPane.getStyleClass().add("contact-background-pane-focused");
    }






}
