package com.messenger.design;

import javafx.scene.Node;
import javafx.scene.control.Label;

public class AuthErrorsDesign {
    public static void setErrorStyle(String message,Node field, Label errorLabel) {
        field.getStyleClass().clear();
        errorLabel.getStyleClass().clear();
        field.getStyleClass().add("input-field-error");
        errorLabel.getStyleClass().add("error-label-visible");
        errorLabel.setText(message);
    }
    public static void deleteErrorStyle(Node field,Label errorLabel) {
        field.getStyleClass().clear();
        errorLabel.getStyleClass().clear();
        field.getStyleClass().add("input-field");
        errorLabel.getStyleClass().add("error-label-invisible");
    }
}
