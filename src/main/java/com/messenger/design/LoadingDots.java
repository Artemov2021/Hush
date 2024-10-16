package com.messenger.design;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoadingDots {
    private static Timeline timeline;
    private static int time = 0;
    private static Circle dot1;
    private static Circle dot2;
    private static Circle dot3;

    public static void startAnimation(AnchorPane authAnchorPane) {
        HBox dotsContainer = new HBox(3);
        dotsContainer.setId("dotsContainer");
        dotsContainer.setLayoutX(authAnchorPane.lookup("#singUpButton").getLayoutX()+40);
        dotsContainer.setLayoutY(authAnchorPane.lookup("#singUpButton").getLayoutY()+13);

        dot1 = createDot();
        dot2 = createDot();
        dot3 = createDot();

        dotsContainer.getChildren().addAll(dot1, dot2, dot3);
        authAnchorPane.getChildren().add(dotsContainer);

        startRepeatAnimation();
    }

    private static void startRepeatAnimation() {
        // Timeline to repeat the animation every second
        timeline = new Timeline(new KeyFrame(Duration.millis(330), event -> {
            startJumpAnimation(dot1, time);   // First dot
            time += 65;
            startJumpAnimation(dot2, time);   // Second dot with a delay
            time += 85;
            startJumpAnimation(dot3, time);   // Third dot with a delay
            time += 400; // Prepare time for the next round
        }));
        timeline.setCycleCount(Animation.INDEFINITE); // Repeat indefinitely
        timeline.play(); // Start the timeline
    }

    private static Circle createDot() {
        Circle dot = new Circle(3); // Radius of the dot
        dot.setFill(Color.WHITE); // Color of the dot
        return dot;
    }

    private static void startJumpAnimation(Circle dot, int delay) {
        TranslateTransition jumpUp = new TranslateTransition(Duration.millis(220), dot);
        jumpUp.setFromY(0);        // Starting position
        jumpUp.setToY(-6);        // Jump height
        jumpUp.setDelay(Duration.millis(delay)); // Delay for sequential animation

        // After jumping up, make it fall back to the original position
        TranslateTransition fallDown = new TranslateTransition(Duration.millis(220), dot);
        fallDown.setFromY(-6);    // Starting position for fall
        fallDown.setToY(0);         // Fall back to original position
        fallDown.setDelay(Duration.millis(0)); // Delay to start after jump

        // Sequentially play the jump and fall animations
        jumpUp.setOnFinished(event -> fallDown.play());
        jumpUp.play();
    }

}
