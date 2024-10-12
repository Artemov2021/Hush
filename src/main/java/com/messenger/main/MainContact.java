package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;

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
            mainContactMessageLabel.setGraphic(imageView);
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
        mainContactTimeLabel.setText(time);
    }


    public void setPaneId(String name) {
        mainContactPane.setId("mainContactPane"+name);
    }


}
