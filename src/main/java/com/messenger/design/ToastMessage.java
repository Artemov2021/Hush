package com.messenger.design;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class ToastMessage {
    public static void applyFadeEffect(Label label) {
        label.setVisible(true);
        // Fade In (Ease In) - 300ms
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), label);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setCycleCount(1);

        // Delay before Fade Out - 1000ms
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), label);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);
        fadeOut.setDelay(Duration.millis(1000)); // 1000ms delay before fade out

        // Play transitions in sequence
        fadeIn.setOnFinished(e -> fadeOut.play());
        fadeIn.play();

    }
}
