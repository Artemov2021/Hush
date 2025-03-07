package com.messenger.design;

import javafx.animation.FadeTransition;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

public class ScrollPaneEffect {
    public static void addScrollBarEffect(ScrollPane scrollPane) {
        scrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            ScrollBar vBar = (ScrollBar) scrollPane.lookup(".scroll-bar:vertical");

            if (vBar == null) return; // Prevent crashes if the scrollbar is not found

            // Make the scrollbar almost invisible but still interactive
            vBar.setOpacity(0.01);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Fade effects
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.6), vBar);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.01); // Keep it slightly visible so events still work

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.2), vBar);
            fadeIn.setFromValue(0.01);
            fadeIn.setToValue(1.0);

            // Show scrollbar when mouse enters the ScrollPane
            scrollPane.setOnMouseEntered(event -> {
                fadeOut.stop(); // Stop hiding animation if it's running
                fadeIn.play();  // Play fade-in animation
            });

            // Also trigger when hovering over the scrollbar itself
            vBar.setOnMouseEntered(event -> {
                fadeOut.stop();
                fadeIn.play();
            });

            // Hide scrollbar when mouse exits both the ScrollPane and the ScrollBar
            scrollPane.setOnMouseExited(event -> {
                if (!vBar.isHover()) { // Ensure we are not still hovering the scrollbar
                    fadeIn.stop();
                    fadeOut.play();
                }
            });

            vBar.setOnMouseExited(event -> {
                if (!scrollPane.isHover()) { // Ensure we are not still hovering the ScrollPane
                    fadeIn.stop();
                    fadeOut.play();
                }
            });
        });
    }
}
