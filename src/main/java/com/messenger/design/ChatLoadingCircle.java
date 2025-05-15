package com.messenger.design;

import com.messenger.main.MainChatController;
import com.messenger.main.MainWindowController;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collection;

public class ChatLoadingCircle {
    private VBox chatVBox;
    public ChatLoadingCircle(MainChatController mainChatController) {
        this.chatVBox = mainChatController.chatVBox;
    }

    public void addLoadingCircle() {
        double radius = 13;

        // Background circle centered at (0, 0)
        Circle backgroundCircle = new Circle(0, 0, radius);
        backgroundCircle.setStroke(Color.web("#2b2831"));
        backgroundCircle.setStrokeWidth(6);
        backgroundCircle.setFill(Color.TRANSPARENT);

        // Arc also centered at (0, 0)
        Arc arc = new Arc(0, 0, radius, radius, 0, 60);
        arc.setType(ArcType.OPEN);
        arc.setStroke(Color.WHITE);
        arc.setStrokeWidth(6);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);
        arc.setFill(Color.TRANSPARENT);

        // Group both and center the entire group at (radius, radius)
        Group arcGroup = new Group(arc);
        Group loadingCircle = new Group(backgroundCircle, arcGroup);
        loadingCircle.setId("chatLoadingCircle");
        loadingCircle.setTranslateX(0);
        loadingCircle.setTranslateY(0);

        // Rotate the entire group smoothly
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(0.7), loadingCircle);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rotateTransition.play();

        // Animate entry (slide down + fade in)
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(100), loadingCircle);
        slideIn.setFromY(-10); // 10px above
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), loadingCircle);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition appearTransition = new ParallelTransition(slideIn, fadeIn);

        loadingCircle.setOpacity(0); // Optional but ensures fade starts from invisible
        chatVBox.getChildren().addFirst(loadingCircle);
        VBox.setMargin(loadingCircle, new Insets(12, 0, 2, 0));

        // Animate in
        appearTransition.play();

    }
}
