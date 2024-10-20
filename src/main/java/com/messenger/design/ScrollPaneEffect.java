package com.messenger.design;

import javafx.animation.FadeTransition;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

public class ScrollPaneEffect {
    public static void addScrollBarEffect(ScrollPane scrollPane) {
        scrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            ScrollBar vBar = (ScrollBar) scrollPane.lookup(".scroll-bar:vertical");

            // Initially set vertical scrollbar policy to NEVER (hidden)
            scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);


            // Create a FadeTransition for smooth disappearing
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.6),vBar);
            fadeOut.setFromValue(1.0);  // Fully visible
            fadeOut.setToValue(0.0);    // Fully transparent

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.2),vBar);
            fadeIn.setFromValue(0.0);   // Start from transparent
            fadeIn.setToValue(1.0);     // Fully visible

            // Create a PauseTransition for the 5-second delay before hiding the scrollbar
//            PauseTransition hideScrollBar = new PauseTransition(Duration.seconds(5));

            // Set the action to hide the scrollbar after 5 seconds using fade-out
//            hideScrollBar.setOnFinished(event -> {
//                fadeOut.play();  // Start the fade-out animation
//            });

            // Show the scrollbar when the mouse enters the ScrollPane
            scrollPane.setOnMouseEntered(event -> {
                scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);  // Show scrollbar
                fadeIn.play();  // Smooth fade-in when mouse enters// Stop the hide timer if it's running
            });

            // Start the hide timer and smooth fade-out when mouse exits the ScrollPane
            scrollPane.setOnMouseExited(event -> {
                fadeOut.play();  // Start the 5-second timer to hide the scrollbar
            });

        });
    }
}
