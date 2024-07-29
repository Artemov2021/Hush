package com.messenger.design;

import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;


public class AuthEmailField {
    private static final double moveX = -5;
    private static final double moveY = -25;
    private static final double widthChange = -25;
    private static final double heightChange = -2;

    public static void setStyle(TextField field, Label label) {
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && field.getText().isEmpty()) {
                moveUp(label);
            } else if (field.getText().trim().isEmpty() && !newValue) {
                moveDown(label);
            } else {
                field.setText(field.getText().trim());
            }
        });
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                field.setText("");
            }
        });
    }
    public static void moveUp(Label label) {
        label.toFront();
        label.getStyleClass().remove("input-field-label");
        label.getStyleClass().add("input-field-label-small");
        label.setPrefWidth(label.getPrefWidth() + widthChange);
        label.setPrefHeight(label.getPrefHeight() + heightChange);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), label);
        translateTransition.setByX(moveX);
        translateTransition.setByY(moveY);
        translateTransition.play();
    }
    public static void moveDown(Label label) {
        label.toBack();
        label.getStyleClass().remove("input-field-label-small");
        label.getStyleClass().add("input-field-label");
        label.setPrefWidth(label.getPrefWidth() - widthChange);
        label.setPrefHeight(label.getPrefHeight() - heightChange);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), label);
        translateTransition.setByX(-moveX);
        translateTransition.setByY(-moveY);
        translateTransition.play();
    }
}
