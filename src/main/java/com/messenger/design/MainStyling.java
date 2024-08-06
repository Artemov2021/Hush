package com.messenger.design;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class MainStyling {
    public static void setHoverStyle(Node mainObject, Node changedObject, String newStyleClass, String oldStyleClass) {
        mainObject.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                changedObject.getStyleClass().clear();
                changedObject.getStyleClass().add(newStyleClass);
            }
        });
        mainObject.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                changedObject.getStyleClass().clear();
                changedObject.getStyleClass().add(oldStyleClass);
            }
        });
    }

    public static void setFocusStyle(Node mainObject, Node changedObject, String newStyleClass, String oldStyleClass) {
        mainObject.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                changedObject.getStyleClass().clear();
                changedObject.getStyleClass().add(newStyleClass);
            } else {
                changedObject.getStyleClass().clear();
                changedObject.getStyleClass().add(oldStyleClass);
            }
        });
    }
}
