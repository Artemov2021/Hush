package com.messenger.design;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

public class ShakeAnimation {
    public static void applyShakeAnimation(Node node) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(75), new KeyValue(node.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(225), new KeyValue(node.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(375), new KeyValue(node.translateXProperty(), -8)),
                new KeyFrame(Duration.millis(450), new KeyValue(node.translateXProperty(), 8)),
                new KeyFrame(Duration.millis(525), new KeyValue(node.translateXProperty(), -8)),
                new KeyFrame(Duration.millis(600), new KeyValue(node.translateXProperty(), 0))
        );

        timeline.setCycleCount(1); // Number of times to repeat the animation
        timeline.play();
    }
}