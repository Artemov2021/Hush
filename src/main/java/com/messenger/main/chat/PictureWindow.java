package com.messenger.main.chat;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import com.messenger.main.MainChatController;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PictureWindow extends MainChatController {
    private static Pane pictureSendingWindowBackground;
    private static Pane pictureSendingWindowOverlay;
    private static Label pictureSendingWindowPicture;
    private static TextField pictureSendingWindowTextField;
    private static byte[] picture;
    public static void showWindow(String givenPicturePath) throws IOException {
        convertIntoPicture(givenPicturePath);
        pictureSendingWindowBackground = new Pane();
        pictureSendingWindowBackground.setStyle("-fx-background-color: rgba(0, 0, 0, 0.68)");
        pictureSendingWindowBackground.setLayoutX(0);
        pictureSendingWindowBackground.setLayoutY(0);
        pictureSendingWindowBackground.setPrefWidth(mainAnchorPane.getWidth());
        pictureSendingWindowBackground.setPrefHeight(mainAnchorPane.getHeight());
        pictureSendingWindowBackground.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                hideWindowSmoothly();
            }
        });
        Platform.runLater(() -> {
            pictureSendingWindowBackground.getScene().getStylesheets().add(PictureWindow.class.getResource("/main/css/MainWindow.css").toExternalForm());
        });
        mainAnchorPane.getChildren().add(pictureSendingWindowBackground);

        pictureSendingWindowOverlay = new Pane();
        pictureSendingWindowOverlay.getStyleClass().add("picture-sending-window-background");
        pictureSendingWindowOverlay.setLayoutX(755);
        pictureSendingWindowOverlay.setLayoutY(235);
        pictureSendingWindowOverlay.setPrefWidth(436);
        pictureSendingWindowOverlay.setPrefHeight(481);
        pictureSendingWindowOverlay.setOnMouseClicked(Event::consume);
        pictureSendingWindowBackground.getChildren().add(pictureSendingWindowOverlay);
        showWindowSmoothly();

        Label pictureSendingWindowTitle = new Label("Sending Picture");
        pictureSendingWindowTitle.getStyleClass().add("picture-sending-window-title");
        pictureSendingWindowTitle.setPrefHeight(23);
        pictureSendingWindowTitle.setPrefWidth(180);
        pictureSendingWindowTitle.setLayoutX(24);
        pictureSendingWindowTitle.setLayoutY(17);
        pictureSendingWindowOverlay.getChildren().add(pictureSendingWindowTitle);

        pictureSendingWindowPicture = new Label();
        pictureSendingWindowPicture.getStyleClass().add("picture-sending-window-picture");
        pictureSendingWindowPicture.setCursor(Cursor.HAND);
        pictureSendingWindowPicture.setLayoutX(27);
        pictureSendingWindowPicture.setLayoutY(61);
        pictureSendingWindowPicture.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    showFullyPicture();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        pictureSendingWindowOverlay.getChildren().add(pictureSendingWindowPicture);
        setPictureToLabel();

        Label pictureSendingWindowChangePicture = new Label();
        pictureSendingWindowChangePicture.getStyleClass().add("picture-sending-window-picture-change-button");
        pictureSendingWindowChangePicture.setCursor(Cursor.HAND);
        pictureSendingWindowChangePicture.setPrefHeight(30);
        pictureSendingWindowChangePicture.setPrefWidth(30);
        pictureSendingWindowChangePicture.setLayoutX(376);
        pictureSendingWindowChangePicture.setLayoutY(70);
        pictureSendingWindowChangePicture.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    String newPicturePath = openFileChooserAndGetPath();
                    if (newPicturePath != null) {
                        convertIntoPicture(newPicturePath);
                        setPictureToLabel();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        pictureSendingWindowOverlay.getChildren().add(pictureSendingWindowChangePicture);

        pictureSendingWindowTextField = new TextField();
        pictureSendingWindowTextField.getStyleClass().add("picture-sending-window-textfield");
        pictureSendingWindowTextField.setPromptText("Add a comment...");
        pictureSendingWindowTextField.setPrefHeight(44);
        pictureSendingWindowTextField.setPrefWidth(295);
        pictureSendingWindowTextField.setLayoutX(26);
        pictureSendingWindowTextField.setLayoutY(412);
        pictureSendingWindowOverlay.getChildren().add(pictureSendingWindowTextField);

        Label pictureSendingWindowSendButton = new Label();
        pictureSendingWindowSendButton.getStyleClass().add("picture-sending-window-send-button");
        pictureSendingWindowSendButton.setCursor(Cursor.HAND);
        pictureSendingWindowSendButton.setPrefHeight(44);
        pictureSendingWindowSendButton.setPrefWidth(85);
        pictureSendingWindowSendButton.setLayoutX(328);
        pictureSendingWindowSendButton.setLayoutY(412);
        pictureSendingWindowSendButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    int addedMessageId = addPictureToTheDBAndGetID();
                    sendPictureMessage(addedMessageId);
                    hideWindowSmoothly();
                } catch (IOException | SQLException | ParseException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        pictureSendingWindowOverlay.getChildren().add(pictureSendingWindowSendButton);

        Label pictureSendingWindowExitButton = new Label();
        pictureSendingWindowExitButton.getStyleClass().add("picture-sending-window-exit-button");
        pictureSendingWindowExitButton.setCursor(Cursor.HAND);
        pictureSendingWindowExitButton.setPrefHeight(38);
        pictureSendingWindowExitButton.setPrefWidth(39);
        pictureSendingWindowExitButton.setLayoutX(386);
        pictureSendingWindowExitButton.setLayoutY(10);
        pictureSendingWindowExitButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                hideWindowSmoothly();
            }
        });
        pictureSendingWindowOverlay.getChildren().add(pictureSendingWindowExitButton);

    }

    private static void hideWindowSmoothly() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180),pictureSendingWindowOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            mainAnchorPane.getChildren().remove(pictureSendingWindowBackground);
        });
        fadeOut.play();
    }
    private static void showWindowSmoothly() {
        pictureSendingWindowOverlay.setOpacity(0); // start invisible
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), pictureSendingWindowOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    private static void setPictureToLabel() throws IOException {
        double pictureLabelWidth = 386;
        double pictureLabelHeight = 339;

        // Write byte[] to a temporary file
        File tempFile = File.createTempFile("tempImage", ".png");
        tempFile.deleteOnExit();  // Optionally delete when the program exits

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(picture);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Now load the image with background loading
        Image image = new Image(tempFile.toURI().toString(), true);

        // ImageView
        ImageView imageView = new ImageView(image);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.setPreserveRatio(true); // Keep aspect ratio

        // Wrap in StackPane to center it
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setPrefSize(pictureLabelWidth, pictureLabelHeight);
        imageContainer.setMaxSize(pictureLabelWidth, pictureLabelHeight);
        imageContainer.setMinSize(pictureLabelWidth, pictureLabelHeight);

        // Clip with rounded corners
        Rectangle clip = new Rectangle(pictureLabelWidth, pictureLabelHeight);
        clip.setArcWidth(20); // 10px radius = 20 arc
        clip.setArcHeight(20);
        imageContainer.setClip(clip);

        // When image is loaded, adjust size and cropping
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0) {
                double imageWidth = image.getWidth();
                double imageHeight = image.getHeight();

                // Scale factor to ensure image fills the label (fill the label, regardless of the image size)
                double scaleX = pictureLabelWidth / imageWidth; // scale based on width
                double scaleY = pictureLabelHeight / imageHeight; // scale based on height

                // If image is smaller than the label, scale it up. If image is larger, scale it down
                double scale = Math.max(scaleX, scaleY); // We ensure that the image will fill the label by using Math.max

                // Apply scaling to the image
                double scaledWidth = imageWidth * scale;
                double scaledHeight = imageHeight * scale;

                imageView.setFitWidth(scaledWidth);
                imageView.setFitHeight(scaledHeight);

                // Center the image if it's smaller than the label
                if (scaledWidth < pictureLabelWidth || scaledHeight < pictureLabelHeight) {
                    imageView.setX((pictureLabelWidth - scaledWidth) / 2); // Center horizontally
                    imageView.setY((pictureLabelHeight - scaledHeight) / 2); // Center vertically
                }
            }
        });


        // Add to label
        pictureSendingWindowPicture.setGraphic(imageContainer);
    }
    private static void showFullyPicture() throws IOException {
        Pane backgroundPane = new Pane();
        backgroundPane.setPrefWidth(1920);
        backgroundPane.setPrefHeight(1009);
        backgroundPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.78)");

        // Apply a fade-in transition to make the background appear smoothly
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), backgroundPane);
        fadeTransition.setFromValue(0.0); // Start from fully transparent
        fadeTransition.setToValue(1.0); // Fade to fully opaque
        fadeTransition.play();

        mainAnchorPane.getChildren().add(backgroundPane);

        backgroundPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                mainAnchorPane.getChildren().remove(backgroundPane);
            }
        });

        Label fullyPicturePreview = new Label();
        fullyPicturePreview.setOnMouseClicked(Event::consume);
        assert picture != null;

        ByteArrayInputStream byteStream = new ByteArrayInputStream(picture);
        Image image = new Image(byteStream);
        ImageView imageView = new ImageView(image);

        // Set max size while preserving aspect ratio
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(Math.min(image.getWidth(), 1520));
        imageView.setFitHeight(Math.min(image.getHeight(), 609));

        imageView.setSmooth(true);
        fullyPicturePreview.setGraphic(imageView);

        // Ensure the layout is updated before centering
        Platform.runLater(() -> {
            // Recalculate layout position after image rendering
            double initialWidth = imageView.getLayoutBounds().getWidth();
            double initialHeight = imageView.getLayoutBounds().getHeight();

            // Correct centering calculations
            fullyPicturePreview.setLayoutX((1920 - initialWidth) / 2.0);
            fullyPicturePreview.setLayoutY((1009 - initialHeight) / 2.0);

            backgroundPane.getChildren().add(fullyPicturePreview);

            // Call the zoom listener and pass the initial values for the image view
            setZoomListener(backgroundPane, imageView, fullyPicturePreview);
        });
    }
    private static void setZoomListener(Pane backgroundPane, ImageView fullyPicturePreview, Label fullyPicturePreviewLabel) {
        backgroundPane.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY();  // Get the direction of the scroll

            // Define zoom factors for scroll up and scroll down
            double zoomFactor = (deltaY > 0) ? 1.1 : 0.9;  // Zoom in if scrolling up, zoom out if scrolling down

            // Get the current width and height of the image
            double currentWidth = fullyPicturePreview.getFitWidth();
            double currentHeight = fullyPicturePreview.getFitHeight();

            // Apply the zoom factor to the current size
            double newWidth = currentWidth * zoomFactor;
            double newHeight = currentHeight * zoomFactor;

            // Update the image size
            fullyPicturePreview.setFitWidth(newWidth);
            fullyPicturePreview.setFitHeight(newHeight);

            // Get the layout bounds of the image after zooming
            double imageWidth = fullyPicturePreview.getLayoutBounds().getWidth();
            double imageHeight = fullyPicturePreview.getLayoutBounds().getHeight();

            // Recalculate the new layout position to keep the image centered
            double newLayoutX = (1920 - imageWidth) / 2.0;
            double newLayoutY = (1009 - imageHeight) / 2.0;

            // Update layout to keep it centered
            fullyPicturePreviewLabel.setLayoutX(newLayoutX);
            fullyPicturePreviewLabel.setLayoutY(newLayoutY);
        });
    }
    private static void convertIntoPicture(String picturePath) throws IOException {
        Path path = new File(picturePath).toPath();
        picture = Files.readAllBytes(path);
    }
    private static ChatHistory getChat() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PictureWindow.class.getResource("/main/fxml/MainChat.fxml"));
        Parent root = fxmlLoader.load();
        MainChatController controller = fxmlLoader.getController();
        return new ChatHistory(mainAnchorPane);
    }
    private static boolean thereIsReplyPane() {
        // Get all children of mainAnchorPane
        List<Node> children = new ArrayList<>(mainAnchorPane.getChildren());

        // Iterate through the children and remove nodes with IDs starting with "replyWrapper"
        for (Node node : children) {
            if (node.getId() != null && (node.getId().startsWith("replyWrapper"))) {
                return true;
            }
        }

        return false;
    }
    private static String getReplyWrapperId() {
        // Get all children of mainAnchorPane
        List<Node> children = new ArrayList<>(mainAnchorPane.getChildren());

        // Iterate through the children and remove nodes with IDs starting with "replyWrapper"
        for (Node node : children) {
            if (node.getId() != null && (node.getId().startsWith("replyWrapper"))) {
                return node.getId();
            }
        }

        return null;
    }
    private static String getCurrentFullTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.now().format(formatter);
    }
    private static boolean thereIsEditPane() {
        // Get all children of mainAnchorPane
        List<Node> children = new ArrayList<>(mainAnchorPane.getChildren());

        // Iterate through the children and remove nodes with IDs starting with "replyWrapper"
        for (Node node : children) {
            if (node.getId() != null && (node.getId().startsWith("editWrapper"))) {
                return true;
            }
        }

        return false;
    }
    private static String getCurrentPictureMessageType() {
        boolean thereIsPictureText = !pictureSendingWindowTextField.getText().trim().isEmpty();

        if (thereIsReplyPane() && thereIsPictureText) {
            return "reply_with_picture_and_text";
        }
        if (thereIsReplyPane() && !thereIsPictureText) {
            return "reply_with_picture";
        }
        if (thereIsEditPane() && thereIsPictureText) {
            return "edit_with_picture_and_text";
        }
        if (thereIsEditPane() && !thereIsPictureText) {
            return "edit_with_picture";
        }
        if (!thereIsReplyPane() && !thereIsEditPane() && thereIsPictureText) {
            return "picture_with_text";
        }
        if (!thereIsReplyPane() && !thereIsEditPane() && !thereIsPictureText) {
            return "picture";
        }

        return null;
    }
    private static void sendPictureMessage(int messageId) throws IOException, SQLException, ParseException {
        ChatHistory chatHistory = getChat();

        switch (Objects.requireNonNull(getCurrentPictureMessageType())) {
            case "reply_with_picture_and_text":
                //chatHistory.loadReplyWithPictureAndTextMessage(messageId);
        }
    }
    private static int addPictureToTheDBAndGetID() throws SQLException {
        int senderId = mainUserId;
        int receiverId = contactId;
        String text = pictureSendingWindowTextField.getText().trim();
        String message = text.isEmpty() ? null : text;
        int replyMessageId = thereIsReplyPane() ? Integer.parseInt(Objects.requireNonNull(getReplyWrapperId()).replaceAll("\\D+", "")) : -1;
        String messageTime = getCurrentFullTime();
        String messageType = getCurrentPictureMessageType();
        boolean received = false;

        return ChatsDataBase.addMessage(senderId,receiverId,message,picture,replyMessageId,messageTime,messageType,received);
    }




}
