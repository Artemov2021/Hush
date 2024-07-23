package com.messenger.auth;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class SingUpController {
    @FXML
    private Button bottomButton;

    @FXML
    private TextField emailField;

    @FXML
    private Label upperLabel;

    public void initialize() {
        emailField.setFocusTraversable(false);
        bottomButton.setUnderline(true);


        TranslateTransition translateDown = new TranslateTransition(Duration.millis(200), upperLabel);
        translateDown.setByY(23); // Move the label down by 30 units

        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                up(upperLabel);
            } else {
                translateDown.play();
            }
        });
    }
    public void up(Label label) {
        label.toFront();
        label.getStyleClass().add("email-label-small");
        System.out.println(label.getPrefWidth());
        TranslateTransition translateUp = new TranslateTransition(Duration.millis(200), label);
        translateUp.setByY(-23);
        translateUp.setByX(-15);// Move the label up by 30 units
        translateUp.play();
    }
}
