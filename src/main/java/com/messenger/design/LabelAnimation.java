package com.messenger.design;

import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;


public class LabelAnimation {
    public static void moveUp(Label label, double prefWidthChange, double prefHeightChange, double x, double y) {
        label.toFront();
        label.getStyleClass().remove("input-field-label");
        label.getStyleClass().add("input-field-label-small");
        label.setPrefWidth(label.getPrefWidth() + prefWidthChange);
        label.setPrefHeight(label.getPrefHeight() + prefHeightChange);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), label);
        translateTransition.setByX(x);
        translateTransition.setByY(y);
        translateTransition.play();
    }
    public static void moveDown(Label label, double prefWidthChange,double prefHeightChange,double x, double y) {
        label.toBack();
        label.getStyleClass().remove("input-field-label-small");
        label.getStyleClass().add("input-field-label");
        label.setPrefWidth(label.getPrefWidth() + prefWidthChange);
        label.setPrefHeight(label.getPrefHeight() + prefHeightChange);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), label);
        translateTransition.setByX(x);
        translateTransition.setByY(y);
        translateTransition.play();
    }
}
