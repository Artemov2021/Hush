package com.messenger.main;

import com.messenger.database.UsersDataBase;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

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

    int mainUserId;


    public void setName(String name) {
        mainContactNameLabel.setText(name);
    }

}
