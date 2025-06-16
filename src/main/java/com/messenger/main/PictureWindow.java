package com.messenger.main;

import com.messenger.database.ActionType;
import com.messenger.database.ChatsDataBase;
import com.messenger.database.ContactsDataBase;
import com.messenger.database.LogsDataBase;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PictureWindow extends MainChatController {
    private Pane pictureSendingWindowBackground;
    private Pane pictureSendingWindowOverlay;
    private Label pictureSendingWindowPicture;
    private TextField pictureSendingWindowTextField;
    private String picturePath;
    private byte[] picture;

    private MainChatController mainChatController;
    private boolean isMessageTooLongVisible;

    public PictureWindow(MainChatController mainChatController,String picturePath) {
        this.mainChatController = mainChatController;
        this.picturePath = picturePath;
        this.mainAnchorPane = mainChatController.mainAnchorPane;
        this.contactId = mainChatController.contactId;
        this.chatVBox = mainChatController.chatVBox;
        this.mainContactMessageLabel = mainChatController.mainContactMessageLabel;
        this.mainContactsVBox = mainChatController.mainContactsVBox;
        this.mainContactTimeLabel = mainChatController.mainContactTimeLabel;
        this.chatTextField = mainChatController.chatTextField;
    }
    public void showWindow() throws IOException {
        convertIntoPicture(picturePath);
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
            pictureSendingWindowBackground.getScene().getStylesheets().add(PictureWindow.class.getResource("/main/css/MainChat.css").toExternalForm());
        });
        mainAnchorPane.getChildren().add(pictureSendingWindowBackground);

        pictureSendingWindowOverlay = new Pane();
        pictureSendingWindowOverlay.getStyleClass().add("picture-sending-window-background");
        pictureSendingWindowOverlay.setLayoutX(755);
        pictureSendingWindowOverlay.setLayoutY(235);
        pictureSendingWindowOverlay.setPrefWidth(436);
        pictureSendingWindowOverlay.setPrefHeight(477);
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
        pictureSendingWindowTextField.setText(chatTextField.getText());
        chatTextField.setText("");
        pictureSendingWindowTextField.setContextMenu(new ContextMenu());
        pictureSendingWindowTextField.getStyleClass().add("picture-sending-window-textfield");
        pictureSendingWindowTextField.setPromptText("Add a comment...");
        pictureSendingWindowTextField.setPrefHeight(44);
        pictureSendingWindowTextField.setPrefWidth(295);
        pictureSendingWindowTextField.setLayoutX(26);
        pictureSendingWindowTextField.setLayoutY(412);
        pictureSendingWindowTextField.setOnAction(clickEvent -> {
            try {
                sendPicture();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Platform.runLater(() -> {
            pictureSendingWindowTextField.requestFocus();
            pictureSendingWindowTextField.positionCaret(pictureSendingWindowTextField.getText().length());
        });
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
                    sendPicture();
                } catch (Exception e) {
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


    private void sendPicture() throws Exception {
        try {
            pictureMessageType pictureMessageType = getPictureMessageType();
            int messageId;

            if (pictureMessageType == PictureWindow.pictureMessageType.EDIT_WITH_PICTURE || pictureMessageType == PictureWindow.pictureMessageType.EDIT_WITH_PICTURE_AND_TEXT) {
                messageId = handlePictureMessageEditing();
            } else {
                messageId = handlePictureMessageSending();
                updateLastInteraction();
                updateLastMessageTime();
                moveContactPaneUp();
            }

            updateLastMessage(messageId);
            hideWindowSmoothly();
        } catch (IllegalArgumentException e) {
            showMessageTooLongException();
        }
    }
    private int handlePictureMessageSending() throws Exception {
        int messageId = insertPictureMessageIntoDB();
        updateChangesLog(messageId, ActionType.NEW);
        displayPictureMessage(messageId);
        hideReplyWrapper();
        setNormalPadding();
        return messageId;
    }
    private int handlePictureMessageEditing() throws Exception {
        int editedMessageId = getEditWrapperId();
        updateChangesLog(editedMessageId, ActionType.EDITED);
        editPictureMessageInDB();
        editPictureMessageInChat();
        hideEditWrapper();
        setNormalPadding();
        return editedMessageId;
    }

    
    private void hideWindowSmoothly() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180),pictureSendingWindowOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            mainAnchorPane.getChildren().remove(pictureSendingWindowBackground);
        });
        fadeOut.play();
    }
    private void showWindowSmoothly() {
        pictureSendingWindowOverlay.setOpacity(0); // start invisible
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), pictureSendingWindowOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    private void setPictureToLabel() throws IOException {
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
    private void showFullyPicture() throws IOException {
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
    private void setZoomListener(Pane backgroundPane, ImageView fullyPicturePreview, Label fullyPicturePreviewLabel) {
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
    private void convertIntoPicture(String picturePath) throws IOException {
        Path path = new File(picturePath).toPath();
        picture = Files.readAllBytes(path);
    }
    private pictureMessageType getPictureMessageType() {
        String messageText = pictureSendingWindowTextField.getText().trim();

        if (isThereReplyWrapper() && !messageText.isEmpty()) {
            return pictureMessageType.REPLY_WITH_PICTURE_AND_TEXT;
        }
        if (isThereReplyWrapper()) {
            return pictureMessageType.REPLY_WITH_PICTURE;
        }
        if (isThereEditWrapper() && !messageText.isEmpty()) {
            return pictureMessageType.EDIT_WITH_PICTURE_AND_TEXT;
        }
        if (isThereEditWrapper()) {
            return pictureMessageType.EDIT_WITH_PICTURE;
        }
        if (!messageText.isEmpty()) {
            return pictureMessageType.PICTURE_WITH_TEXT;
        }
        return pictureMessageType.PICTURE;
    }
    private boolean isThereReplyWrapper() {
        return mainAnchorPane.getChildren().stream().anyMatch(node -> node.getId() != null && node.getId().startsWith("replyWrapper"));
    }
    private boolean isThereEditWrapper() {
        return mainAnchorPane.getChildren().stream().anyMatch(node -> node.getId() != null && node.getId().startsWith("editWrapper"));
    }
    private int getReplyWrapperId() {
        return mainAnchorPane.getChildren().stream()
                .map(Node::getId)
                .filter(id -> id != null && id.startsWith("replyWrapper"))
                .map(id -> id.replaceAll("\\D+", ""))
                .filter(num -> !num.isEmpty())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(-1);
    }
    private int getEditWrapperId() {
        return mainAnchorPane.getChildren().stream()
                .map(Node::getId)
                .filter(id -> id != null && id.startsWith("editWrapper"))
                .map(id -> id.replaceAll("\\D+", ""))
                .filter(num -> !num.isEmpty())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(-1);
    }
    private String getCurrentFullTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.now().format(formatter);
    }
    enum pictureMessageType {
        REPLY_WITH_PICTURE_AND_TEXT,
        REPLY_WITH_PICTURE,
        EDIT_WITH_PICTURE_AND_TEXT,
        EDIT_WITH_PICTURE,
        PICTURE_WITH_TEXT,
        PICTURE
    }
    private int insertPictureMessageIntoDB() throws SQLException {
        String messageText = pictureSendingWindowTextField.getText().trim();
        pictureMessageType pictureMessageType = getPictureMessageType();

        int senderId = mainUserId;
        int receiverId = contactId;
        String message = messageText.isEmpty() ? null : messageText;
        int replyMessageId = getReplyWrapperId();
        String messageTime = getCurrentFullTime();
        String messageType = pictureMessageType.toString().toLowerCase();
        boolean received = false;
        
        if (message != null && message.length() >= 1000) {
            throw new IllegalArgumentException();
        } else {
            return ChatsDataBase.addMessage(senderId,receiverId,message,picture,replyMessageId,messageTime,messageType,received);
        }
    }
    private void updateChangesLog(int addedMessageId, ActionType changeType) throws SQLException {
        ChatMessage addedMessage = ChatsDataBase.getMessage(mainUserId,contactId,addedMessageId);
        LogsDataBase.addAction(changeType,addedMessageId,addedMessage.sender_id,addedMessage.receiver_id,addedMessage.message_text,addedMessage.picture,
                addedMessage.reply_message_id,addedMessage.time,addedMessage.type);
    }
    private void editPictureMessageInDB() throws SQLException {
        int editedMessageId = getEditWrapperId();
        boolean editedMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,editedMessageId);
        String newMessage = pictureSendingWindowTextField.getText().trim().isEmpty() ? null :  pictureSendingWindowTextField.getText().trim();
        String newMessageType = getEditedMessageType();

        if (!editedMessageExists) {
            return;
        }

        if (newMessage != null && newMessage.length() >= 1000) {
            throw new IllegalArgumentException();
        } else {
            ChatsDataBase.editMessage(editedMessageId,newMessage,picture,newMessageType);
        }
    }
    private void editPictureMessageInChat() throws Exception {
        int editedMessageId = getEditWrapperId();
        boolean editedMessageExists = ChatsDataBase.messageExists(mainUserId,contactId,editedMessageId);

        if (!editedMessageExists) {
            return;
        }

        ChatMessage chatMessage = ChatsDataBase.getMessage(mainUserId,contactId,editedMessageId);
        chatMessage.reload(mainChatController);
    }
    private void hideEditWrapper() {
        mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#editWrapper"+getEditWrapperId()));
    }
    private void hideReplyWrapper() {
        mainAnchorPane.getChildren().remove(mainAnchorPane.lookup("#replyWrapper"+getReplyWrapperId()));
    }
    private void setNormalPadding() {
        byte defaultBottomPadding = 20;
        chatVBox.setPadding(new Insets(0, 0, defaultBottomPadding, 0));
    }
    private String getEditedMessageType() throws SQLException {
        int editedMessageId = getEditWrapperId();
        String originalMessageType = ChatsDataBase.getMessage(mainUserId,contactId,editedMessageId).type;
        pictureMessageType newPictureMessageType = getPictureMessageType();

        if (newPictureMessageType == pictureMessageType.EDIT_WITH_PICTURE && originalMessageType.contains("reply")) {
            return "reply_with_picture";
        } else if (newPictureMessageType == pictureMessageType.EDIT_WITH_PICTURE) {
            return "picture";
        } else if (newPictureMessageType == pictureMessageType.EDIT_WITH_PICTURE_AND_TEXT && originalMessageType.contains("reply")) {
            return "reply_with_picture_and_text";
        } else if (newPictureMessageType == pictureMessageType.EDIT_WITH_PICTURE_AND_TEXT) {
            return "picture_with_text";
        } else {
            return null;
        }

    }
    private void displayPictureMessage(int messageId) throws Exception {
        ChatMessage chatMessage = ChatsDataBase.getMessage(mainUserId,contactId,messageId);
        chatVBox.getChildren().add(chatMessage.render(mainChatController));
    }
    public static String getMessageHours(String messageFullTime) {
        // Define the input and output formats
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Parse the input string to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(messageFullTime, inputFormatter);

        // Format and return the output as a string
        return dateTime.format(outputFormatter);
    }
    private void updateLastInteraction() throws SQLException {
        ContactsDataBase.updateInteractionTime(mainUserId,contactId,getCurrentFullTime());
    }
    private void updateLastMessage(int messageId) throws SQLException {
        boolean isLastMessage = ChatsDataBase.getLastMessageId(mainUserId,contactId) == messageId;

        if (isLastMessage && pictureSendingWindowTextField.getText().trim().isEmpty()) {
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.setStyle("-fx-text-fill: white");
            mainContactMessageLabel.setText("Picture");
        } else if (isLastMessage) {
            String lastMessage = ChatsDataBase.getLastMessage(mainUserId, contactId);
            mainContactMessageLabel.setStyle("");
            mainContactMessageLabel.getStyleClass().clear();
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText(lastMessage);
        }

    }
    private void updateLastMessageTime() {
        mainContactTimeLabel.setText(getMessageHours(getCurrentFullTime()));
    }
    private void moveContactPaneUp() {
        AnchorPane contactPane = (AnchorPane) mainContactsVBox.lookup("#mainContactAnchorPane"+contactId);
        mainContactsVBox.getChildren().remove(contactPane);
        mainContactsVBox.getChildren().add(0,contactPane);
    }
    private void showMessageTooLongException() {
        if (isMessageTooLongVisible) {
            return; // Prevent multiple alerts
        }

        Label errorMessage = new Label("Text is too long!");
        errorMessage.getStyleClass().add("chat-message-too-long-exception-label");
        errorMessage.setLayoutX(875);
        errorMessage.setLayoutY(720);
        errorMessage.setTranslateY(30);
        errorMessage.getStylesheets().add(getClass().getResource("/main/css/MainChat.css").toExternalForm());
        mainAnchorPane.getChildren().add(errorMessage);

        isMessageTooLongVisible = true;

        byte moveDistance = -15;
        errorMessage.setOpacity(0);
        errorMessage.setTranslateY(moveDistance);

        // Slide & fade in
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), errorMessage);
        slideIn.setFromY(moveDistance);
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), errorMessage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Slide & fade out
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), errorMessage);
        slideOut.setFromY(0);
        slideOut.setToY(moveDistance);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), errorMessage);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Play fade/slide in
        errorMessage.setVisible(true);
        slideIn.play();
        fadeIn.play();

        // After 2 seconds, fade and slide out, then remove the label
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            slideOut.play();
            fadeOut.play();

            // Remove the node from the UI after the transition ends
            fadeOut.setOnFinished(event -> {
                mainAnchorPane.getChildren().remove(errorMessage);
                isMessageTooLongVisible = false; // Reset flag only when it disappears
            });
        });

        delay.play();
    }

}
