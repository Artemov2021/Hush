package com.messenger.design;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.util.Duration;

public class AuthEmailField {
    private final double MOVE_X = -5;
    private final double MOVE_Y = -25;
    private double WIDTH_CHANGE = -25;
    private double HEIGHT_CHANGE = -2;

    private TextInputControl Field;
    private Label Label;

    public AuthEmailField(TextInputControl field, Label label) {
        Field = field;
        Label = label;
    }

    public AuthEmailField(TextInputControl field, Label label, double WIDTH_CHANGE, double HEIGHT_CHANGE) {
        Field = field;
        Label = label;
        this.WIDTH_CHANGE = WIDTH_CHANGE;
        this.HEIGHT_CHANGE = HEIGHT_CHANGE;
    }

    public void setStyle() {
        Field.focusedProperty().addListener((observable, oldValue, newValue) -> { handleFocusProperty(newValue); });
        Field.textProperty().addListener((observable, oldValue, newValue) -> { handleTextProperty(newValue); });
    }

    private void handleFocusProperty(boolean focus) {
        String text = emailField.getText();
        if (focus && text.isEmpty()) {
            moveUp(emailLabel);
        } else if (!focus && text.trim().isEmpty()) {
            moveDown(emailLabel);
        }

        // text will be trimmed only if it is email field
        if (emailField instanceof TextField) {
            emailField.setText(text.trim());
        }
    }
    private void handleTextProperty(String text) {
        // Blocks spaces at the beginning of the text
        if (text.trim().isEmpty()) {
            emailField.setText("");
        }
    }

    private void moveUp(Label label) {
        label.toFront();
        label.getStyleClass().remove("input-field-label");
        label.getStyleClass().add("input-field-label-small");
        label.setPrefWidth(label.getPrefWidth() + widthChange);
        label.setPrefHeight(label.getPrefHeight() + heightChange);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), label);
        translateTransition.setByX(moveX);
        translateTransition.setByY(MOVE_Y);
        translateTransition.play();
    }
    private void moveDown(Label label) {
        label.toBack();
        label.getStyleClass().remove("input-field-label-small");
        label.getStyleClass().add("input-field-label");
        label.setPrefWidth(label.getPrefWidth() - widthChange);
        label.setPrefHeight(label.getPrefHeight() - heightChange);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), label);
        translateTransition.setByX(-moveX);
        translateTransition.setByY(-MOVE_Y);
        translateTransition.play();
    }
}
