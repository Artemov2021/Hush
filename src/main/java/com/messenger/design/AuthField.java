package com.messenger.design;

import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.util.Duration;

// Input Label Animation

public class AuthField {
    private double MOVE_X;
    private double MOVE_Y;
    private double WIDTH_CHANGE;
    private double HEIGHT_CHANGE;

    private final TextInputControl Field;
    private final Label Label;

    public AuthField(TextInputControl field, Label label) {
        Field = field;
        Label = label;
    }

    public void setLabelChanges(double WIDTH_CHANGE, double HEIGHT_CHANGE) {
        this.WIDTH_CHANGE = WIDTH_CHANGE;
        this.HEIGHT_CHANGE = HEIGHT_CHANGE;
    }

    public void setLabelMovePath(double MOVE_X, double MOVE_Y) {
        this.MOVE_X = MOVE_X;
        this.MOVE_Y = MOVE_Y;
    }

    public void setStyle() {
        Field.focusedProperty().addListener((observable, oldValue, newValue) -> { handleFocusProperty(newValue); });
        Field.textProperty().addListener((observable, oldValue, newValue) -> { handleTextProperty(newValue); });
    }

    private void handleFocusProperty(boolean focus) {
        String text = Field.getText();
        if (focus && text.isEmpty()) {
            move("up",Label);
        } else if (!focus && text.isEmpty()) {
            move("down",Label);
        }

        // text will be trimmed only if it is email field
        if (Field instanceof TextField && !(Field instanceof PasswordField)) {
            Field.setText(text.trim());
        }
    }
    private void handleTextProperty(String text) {
        // Blocks spaces at the beginning of the text
        if (text.trim().isEmpty()) {
            Field.setText("");
        }
    }

    private void move(String direction,Label label) {
        if (direction.equals("up")) {
            label.toFront();
        } else {
            label.toBack();
        }

        String newLabelStyle = direction.equals("up") ? "input-field-label-small" : "input-field-label";
        double signedWidthChange = direction.equals("up") ? WIDTH_CHANGE : -WIDTH_CHANGE;
        double signedHeightChange = direction.equals("up") ? HEIGHT_CHANGE : -HEIGHT_CHANGE;
        double signedMoveX = direction.equals("up") ? MOVE_X : -MOVE_X;
        double signedMoveY = direction.equals("up") ? MOVE_Y : -MOVE_Y;

        label.getStyleClass().clear();
        label.getStyleClass().add(newLabelStyle);
        label.setPrefWidth(label.getPrefWidth() + signedWidthChange);
        label.setPrefHeight(label.getPrefHeight() + signedHeightChange);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), label);
        translateTransition.setByX(signedMoveX);
        translateTransition.setByY(signedMoveY);
        translateTransition.play();
    }

    public static void setErrorStyle(TextInputControl textField,Label errorLabel,String message) {
        textField.getStyleClass().clear();
        textField.getStyleClass().add("input-field-error");

        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    public static void deleteErrorStyle(TextInputControl textField,Label errorLabel) {
        textField.getStyleClass().clear();
        textField.getStyleClass().add("input-field");

        errorLabel.setVisible(false);
    }

}
