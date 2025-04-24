package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.UsersDataBase;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ChatMessage extends MainChatController {
    public int id;
    public int sender_id;
    public int receiver_id;
    public String message_text;
    public byte[] picture;
    public int reply_message_id;
    public String time;
    public String type;
    public boolean received;
    public int previousMessageId;

    private HBox messageHBox;
    private StackPane messageStackPane;
    private VBox messageVBox;
    private Label messagePictureLabel;
    private Label messageTextLabel;
    private StackPane messageReplyStackPane;
    private HBox messagePictureHBox;
    private Label messagePictureTimeLabel;
    private Label messageAvatarLabel;

    private MainChatController mainChatController;

    public ChatMessage(int messageId) throws SQLException {
        ArrayList<Object> messageList = ChatsDataBase.getMessage(messageId);
        this.id = (int) messageList.get(0);
        this.sender_id = (int) messageList.get(1);
        this.receiver_id = (int) messageList.get(2);
        this.message_text = (String) messageList.get(3);
        this.picture = (byte[]) messageList.get(4);
        this.reply_message_id = (int) messageList.get(5);
        this.time = (String) messageList.get(6);
        this.type = (String) messageList.get(7);
        this.received = (boolean) messageList.get(8);
        this.previousMessageId = calculatePreviousMessageId();
    }
    private void injectChatElements(MainChatController mainChatController) {
        this.mainChatController = mainChatController;
        this.chatVBox = mainChatController.chatVBox;
        this.chatScrollPane = mainChatController.chatScrollPane;
        this.mainAnchorPane = mainChatController.mainAnchorPane;
    }
    public HBox render(MainChatController mainChatController) throws Exception {
        injectChatElements(mainChatController);
        setPotentialDateLabel();
        return switch (type) {
            case "text" -> buildTextMessage();
            case "reply_with_text" -> buildReplyWithTextMessage();
            case "picture" -> buildPictureMessage();
            case "picture_with_text" -> buildPictureWithTextMessage();
            default -> throw new Exception();
        };
    }


    // Message Building
    private HBox buildTextMessage() throws SQLException, ParseException {
        setMessageHBox();
        setMessageStackPane();
        setMessageTextLabel();
        setMessageTimeLabel();
        setMessageAvatar();
        return messageHBox;
    }
    private HBox buildReplyWithTextMessage() throws SQLException, ParseException {
        setMessageHBox();
        setMessageStackPane();
        setReplyStackPane();
        setMessageTextLabel();
        setMessageTimeLabel();
        setMessageAvatar();
        return messageHBox;
    }
    private HBox buildPictureMessage() throws IOException, SQLException, ParseException {
        setMessageHBox();
        setMessageStackPane();
        setMessageVBox();
        setMessagePictureHBox();
        setMessagePictureLabel();
        setMessagePictureTimeLabel();
        setMessageAvatar();
        return messageHBox;
    }
    private HBox buildPictureWithTextMessage() throws IOException, SQLException, ParseException {
        setMessageHBox();
        setMessageStackPane();
        setMessageTimeLabel();
        setMessageVBox();
        setMessagePictureHBox();
        setMessagePictureLabel();
        setMessageTextLabel();
        setMessageAvatar();
        return messageHBox;
    }


    // Message Building: Elements
    private void setMessageHBox() {
        byte minHeight = 40;
        Pos messagePosition = (mainUserId == sender_id) ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT;
        messageHBox = new HBox();
        messageHBox.setMinHeight(minHeight);
        messageHBox.setAlignment(messagePosition);
        messageHBox.setId("messageHBox"+id);
    }
    private void setMessageStackPane() {
        short maxWidth = 408;
        String style = (mainUserId == sender_id) ? "chat-message-user-stackpane" : "chat-message-contact-stackpane";
        messageStackPane = new StackPane();
        HBox.setMargin(messageStackPane,(mainUserId == sender_id) ? new Insets(0,13, 0,0) : new Insets(0,0,0,13));
        messageStackPane.setId("messageStackPane"+id);
        messageStackPane.setMaxWidth(maxWidth);
        messageStackPane.getStyleClass().add(style);
        messageStackPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.SECONDARY) {
                showAppropriateMessageButton(clickEvent);
            }
        });
        messageHBox.getChildren().add(messageStackPane);
    }
    private void setMessageVBox() {
        messageVBox = new VBox();
        messageStackPane.getChildren().add(messageVBox);
    }
    private void setMessageTextLabel() {
        Insets padding = getTextLabelPadding();
        messageTextLabel = new Label(message_text);
        StackPane.setAlignment(messageTextLabel,Pos.BOTTOM_LEFT);
        messageTextLabel.setId("messageTextLabel"+id);
        messageTextLabel.setWrapText(true);
        messageTextLabel.getStyleClass().add("chat-message-text-label");
        if (type.equals("picture_with_text")) {
            VBox.setMargin(messageTextLabel,padding);
            messageVBox.getChildren().add(messageTextLabel);
        } else {
            StackPane.setMargin(messageTextLabel,padding);
            messageStackPane.getChildren().add(messageTextLabel);
        }
    }
    private void setMessageTimeLabel() {
        String messageTime = getMessageTime(time);
        Label timeLabel = new Label(messageTime);
        timeLabel.getStyleClass().add("chat-time-label");
        StackPane.setMargin(timeLabel,new Insets(0,10,4,0));
        StackPane.setAlignment(timeLabel,Pos.BOTTOM_RIGHT);
        messageStackPane.getChildren().add(timeLabel);
    }
    private void setMessagePictureHBox() {
        messagePictureHBox = new HBox();
        messagePictureHBox.setAlignment(Pos.TOP_CENTER);
        messageVBox.getChildren().add(messagePictureHBox);
    }
    private void setReplyStackPane() throws SQLException {
        short minWidth = 80;
        short prefHeight = 37;
        short maxHeight = 37;
        String messageReplyPaneStyle = (mainUserId == sender_id) ? "chat-message-user-reply-pane" : "chat-message-contact-reply-pane";
        boolean repliedMessageExists = ChatsDataBase.messageExists(sender_id,receiver_id,reply_message_id);
        boolean isRepliedMessagePicture = repliedMessageExists && ((String) ChatsDataBase.getMessage(reply_message_id).get(7)).contains("picture");

        messageReplyStackPane = new StackPane();
        messageReplyStackPane.setId("messageReplyStackPane"+id);
        messageReplyStackPane.setCursor(repliedMessageExists ? Cursor.HAND : Cursor.DEFAULT);
        messageReplyStackPane.setMinWidth(minWidth);
        messageReplyStackPane.setPrefHeight(prefHeight);
        messageReplyStackPane.setMaxHeight(maxHeight);
        messageReplyStackPane.getStyleClass().add(messageReplyPaneStyle);
        StackPane.setAlignment(messageReplyStackPane,Pos.TOP_LEFT);
        StackPane.setMargin(messageReplyStackPane,new Insets(7,7,0,7));
        messageStackPane.getChildren().add(messageReplyStackPane);


        if (!repliedMessageExists) {
            Label repliedMessageDeletedMessage = new Label("(deleted message)");
            repliedMessageDeletedMessage.getStyleClass().add((mainUserId == sender_id) ? "chat-message-user-deleted-message" : "chat-message-contact-deleted-message");
            StackPane.setAlignment(repliedMessageDeletedMessage,Pos.TOP_LEFT);
            StackPane.setMargin(repliedMessageDeletedMessage,new Insets(10,8,5,8));
            messageReplyStackPane.getChildren().add(repliedMessageDeletedMessage);
        } else if (isRepliedMessagePicture) {
            // TODO
        } else {
            String repliedMessageName = UsersDataBase.getNameWithId((int) ChatsDataBase.getMessage(reply_message_id).get(1));
            Label messageReplyNameLabel = new Label(repliedMessageName);
            messageReplyNameLabel.getStyleClass().add("chat-message-reply-name");
            messageReplyNameLabel.setMouseTransparent(true);
            StackPane.setAlignment(messageReplyNameLabel,Pos.TOP_LEFT);
            StackPane.setMargin(messageReplyNameLabel,new Insets(4,8,0,8));
            messageReplyStackPane.getChildren().add(messageReplyNameLabel);

            String repliedMessageText = (String) (ChatsDataBase.getMessage(reply_message_id)).get(3);
            Label messageReplyMessageLabel = new Label(repliedMessageText);
            messageReplyMessageLabel.getStyleClass().add((mainUserId == sender_id) ? "chat-message-user-reply-message" : "chat-message-contact-reply-message");
            messageReplyMessageLabel.setMouseTransparent(true);
            StackPane.setAlignment(messageReplyMessageLabel,Pos.TOP_LEFT);
            StackPane.setMargin(messageReplyMessageLabel,new Insets(18,8,0,8));
            messageReplyStackPane.getChildren().add(messageReplyMessageLabel);
        }

        if (repliedMessageExists) {
            messageReplyStackPane.setOnMouseClicked(repliedClickEvent -> {
                if (repliedClickEvent.getButton() == MouseButton.PRIMARY) {
                    HBox replyHBoxMessage = (HBox) chatVBox.lookup("#messageHBox"+reply_message_id);
                    double replyHBoxPosition = getCenteredScrollPosition(replyHBoxMessage);
                    smoothScrollTo(replyHBoxPosition,0.4);
                    fadeOutBackgroundColor(replyHBoxMessage);
                }
            });
        }
    }
    private void setMessagePictureLabel() throws IOException {
        short maxWidth = 408;
        short maxHeight = 440;
        byte minHeight = 40;

        messagePictureLabel = new Label();
        messagePictureLabel.setCursor(Cursor.HAND);
        messagePictureLabel.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                showFullyMessagePicture();
            }
        });

        File tempFile = File.createTempFile("tempImage", ".png");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(picture);
        }

        Image image = new Image(tempFile.toURI().toString(), true);
        ImageView imageView = new ImageView(image);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.setPreserveRatio(true); // Keep aspect ratio

        StackPane imageContainer = new StackPane(imageView);
        messagePictureLabel.setGraphic(imageContainer);
        messagePictureHBox.getChildren().add(messagePictureLabel);

        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0) {
                double originalWidth = image.getWidth();
                double originalHeight = image.getHeight();

                // Scaling to fit max width & max height
                double scaleX = maxWidth / originalWidth;
                double scaleY = maxHeight / originalHeight;
                double scale = Math.min(1.0, Math.min(scaleX, scaleY)); // Don't scale up

                double scaledWidth = originalWidth * scale;
                double scaledHeight = originalHeight * scale;

                // Enforce minimum height of 40px
                if (scaledHeight < minHeight) {
                    double minHeightScale = minHeight / originalHeight;
                    scaledHeight = minHeight;
                    scaledWidth = originalWidth * minHeightScale;
                    // Optional: If that causes width > maxAllowedWidth, clamp it again
                    if (scaledWidth > maxWidth) {
                        scaledWidth = maxWidth;
                        scaledHeight = originalHeight * (maxWidth / originalWidth);
                    }
                }

                imageView.setFitWidth(scaledWidth); // Height handled via preserveRatio

                imageContainer.setPrefWidth(scaledWidth);
                imageContainer.setPrefHeight(scaledHeight);

                if (type.equals("picture_with_text") && messageStackPane.getWidth() <= scaledWidth) {
                    SVGPath svgClip = new SVGPath();
                    svgClip.setContent(
                            "M0,11 " +                              // Move down from top-left
                                    "Q0,0 11,0 " +                          // Top-left corner curve
                                    "H" + (scaledWidth - 11) + " " +        // Line to before top-right curve
                                    "Q" + scaledWidth + ",0 " + scaledWidth + ",11 " + // Top-right corner curve
                                    "V" + scaledHeight + " " +              // Line down right side
                                    "H0 Z"                                  // Line to left and close path
                    );
                    imageContainer.setClip(svgClip);
                } else if (type.equals("picture_with_text") && messageStackPane.getWidth() > scaledWidth) {
                    messagePictureHBox.setPadding(new Insets(10,0,0,0));
                } else if (type.equals("picture")) {
                    Rectangle clip = new Rectangle(scaledWidth, scaledHeight);
                    clip.setArcWidth(22);
                    clip.setArcHeight(22);
                    imageContainer.setClip(clip);
                }
            }
        });
    }
    private void setMessagePictureTimeLabel() {
        messagePictureTimeLabel = new Label(getMessageTime(time));
        messagePictureTimeLabel.setVisible(false);
        messagePictureTimeLabel.getStyleClass().add("chat-picture-message-time-label");
        HBox.setMargin(messagePictureTimeLabel,(mainUserId == sender_id) ? new Insets(0,10,0,0) : new Insets(0,0,0,10));
        int messageHBoxIndex = (mainUserId == sender_id) ? 0 : messageHBox.getChildren().size();
        messageHBox.getChildren().add(messageHBoxIndex,messagePictureTimeLabel);



        messagePictureTimeLabel.setOpacity(0);
        messagePictureTimeLabel.setTranslateX(18); // Start slightly off-screen (right side)

// Transitions
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), messagePictureTimeLabel);
        slideIn.setFromX(18);
        slideIn.setToX(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), messagePictureTimeLabel);
        slideOut.setFromX(0);
        slideOut.setToX(18);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), messagePictureTimeLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), messagePictureTimeLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Hover events
        messagePictureLabel.setOnMouseEntered(e -> {
            messagePictureTimeLabel.setVisible(true);
            slideIn.playFromStart();
            fadeIn.playFromStart();
        });

        messagePictureLabel.setOnMouseExited(e -> {
            slideOut.playFromStart();
            fadeOut.playFromStart();
            fadeOut.setOnFinished(ev -> messagePictureTimeLabel.setVisible(false));
        });
    }
    private void setMessageAvatar() throws SQLException, ParseException {
        messageAvatarLabel = new Label();
        messageAvatarLabel.setId("messageAvatarLabel" + id);
        setAvatarLabel();
        int messageHBoxIndex = (mainUserId == sender_id) ? messageHBox.getChildren().size() : 0;
        messageHBox.getChildren().add(messageHBoxIndex, messageAvatarLabel);

        HBox.setMargin(messageAvatarLabel,(mainUserId == sender_id) ? new Insets(0,115, 0, 0) : new Insets(0,0,0,105));

        if (shouldRemovePreviousAvatar()) {
            removePreviousAvatar();
        }
    }

















    private Insets getTextLabelPadding() {
        return switch (type) {
            case "text" -> new Insets(5,50,9,13);
            case "reply_with_text" -> new Insets(48,50,7,12);
            case "picture_with_text" -> new Insets (8,50,7,13);
            default -> null;
        };
    }


    private int calculatePreviousMessageId() throws SQLException {
        return ChatsDataBase.getPreviousMessageId(sender_id,receiver_id,id);
    }
    private void setPotentialDateLabel() throws ParseException, SQLException {
        boolean isFirstMessage = chatVBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> node.getId())
                .noneMatch(id -> id != null && id.startsWith("messageHBox"));
        boolean isPreviousMessageOneDay = !isFirstMessage && messagesHaveOneDayDifference((String) ChatsDataBase.getMessage(previousMessageId).get(6),time);

        if (isFirstMessage || isPreviousMessageOneDay) {

            String labelDate = getDateForDateLabel(time);
            setChatDateLabel(labelDate);
        }
    }
    private void setChatDateLabel(String date) {
        Label chatDateLabel = new Label(date);
        chatDateLabel.setId("dateLabel"+getLabelIdCurrentDate());
        chatDateLabel.getStyleClass().add("chat-date-label");
        VBox.setMargin(chatDateLabel,new Insets(8,0,8,0));
        chatVBox.getChildren().add(chatDateLabel);
    }
    private String getDateForDateLabel(String fullDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.GERMAN);

        // Parse input date
        LocalDateTime dateTime = LocalDateTime.parse(fullDate, inputFormatter);
        LocalDate date = dateTime.toLocalDate(); // Extract only date

        // Get current year
        int currentYear = LocalDate.now().getYear();

        // Format output
        String formattedDate = date.format(outputFormatter);
        if (date.getYear() != currentYear) {
            formattedDate += " " + date.getYear();
        }

        return formattedDate;
    }
    private String getLabelIdCurrentDate() {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(time, inputFormatter);

        return dateTime.toLocalDate().toString(); // Outputs in yyyy-MM-dd format
    }
    private static String getMessageTime(String fullDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(fullDate, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(outputFormatter);
    }
    private boolean messagesHaveOneDayDifference(String previousMessageFullDate, String currentMessageFullDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd"); // Only extracts the date

        Date date1 = dateFormat.parse(previousMessageFullDate);
        Date date2 = dateFormat.parse(currentMessageFullDate);

        // Extract only the day part (YYYY-MM-DD)
        String day1 = dayFormat.format(date1);
        String day2 = dayFormat.format(date2);

        return !day1.equals(day2); // True if the dates are different
    }
    private void showAppropriateMessageButton(MouseEvent clickEvent) {
        int x = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getX();
        int y = (int) convertToTopLevelAnchorPaneCoordinates(messageStackPane,clickEvent.getX(),clickEvent.getY()).getY();

        MessageButtons messageButtons = new MessageButtons(mainChatController);
        if (mainUserId == sender_id) {
            messageButtons.showMessageButtons(x, y,id);
        } else {
            messageButtons.showMessageReplyButton(x, y,id);
        }
    }
    private Point2D convertToTopLevelAnchorPaneCoordinates(Node node, double x, double y) {
        if (node == null) {
            return new Point2D(x, y);  // If no parent, return the current coordinates.
        }

        // If this node is an AnchorPane, return the coordinates directly
        if (Objects.equals(node.getId(),"#anchorPane")) {
            return node.localToParent(x, y); // Convert the coordinates relative to the AnchorPane
        }

        // Otherwise, recursively move up the parent hierarchy
        Point2D pointInParent = node.localToParent(x, y);

        // Continue traversing up the parent hierarchy
        return convertToTopLevelAnchorPaneCoordinates(node.getParent(), pointInParent.getX(), pointInParent.getY());
    }
    private void setAvatarLabel() throws SQLException {
        byte[] blobBytes = UsersDataBase.getAvatarWithId(sender_id);
        if (blobBytes == null) {
            messageAvatarLabel.getStyleClass().clear();
            messageAvatarLabel.getStyleClass().add("chat-message-default-avatar");
            messageAvatarLabel.setPrefHeight(40);
            messageAvatarLabel.setPrefWidth(40);
            return;
        }
        ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
        ImageView imageView = new ImageView(new Image(byteStream));
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setSmooth(true);
        messageAvatarLabel.setGraphic(imageView);
        Circle clip = new Circle();
        clip.setLayoutX(20);
        clip.setLayoutY(20);
        clip.setRadius(20);
        messageAvatarLabel.setClip(clip);
    }
    private boolean shouldRemovePreviousAvatar() throws SQLException, ParseException {
        boolean isFirstMessage = !chatVBox.getChildren().stream()
                .anyMatch(node -> node.getId() != null && node.getId().startsWith("messageHBox"));
        boolean isPreviousMessageForeign = previousMessageId != -1 && (sender_id != (int) ChatsDataBase.getMessage(previousMessageId).get(1));
        boolean isPreviousMessageOneDay =  previousMessageId != -1 && messagesHaveOneDayDifference((String) ChatsDataBase.getMessage(previousMessageId).get(6),time);

        return !isFirstMessage && !isPreviousMessageForeign && !isPreviousMessageOneDay;
    }
    private void removePreviousAvatar() {
        HBox previousMessageHBox = (HBox) chatVBox.lookup("#messageHBox"+previousMessageId);
        StackPane previousMessageStackPane = (StackPane) previousMessageHBox.lookup("#messageStackPane"+previousMessageId);
        Label previousMessageAvatar = (Label) previousMessageHBox.lookup("#messageAvatarLabel"+previousMessageId);

        previousMessageHBox.getChildren().remove(previousMessageAvatar);
        HBox.setMargin(previousMessageStackPane,(mainUserId == sender_id) ? new Insets(0, 168, 0, 0) : new Insets(0,0,0,158));
    }
    private void smoothScrollTo(double targetValue, double durationInSeconds) {
        double startValue = chatScrollPane.getVvalue(); // Current scroll position
        double distance = targetValue - startValue; // How much to scroll

        Timeline timeline = new Timeline();
        int frames = (int) (durationInSeconds * 60); // 60 FPS
        for (int i = 0; i <= frames; i++) {
            double progress = (double) i / frames; // Progress from 0 to 1
            double interpolatedValue = startValue + distance * progress; // Linear interpolation

            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * (1000.0 / 60)),
                    event -> chatScrollPane.setVvalue(interpolatedValue)));
        }

        timeline.setCycleCount(1);
        timeline.play();
    }
    private double getCenteredScrollPosition(HBox targetHBox) {
        double hboxY = targetHBox.localToScene(0, 0).getY(); // Y position of HBox in scene
        double vboxY = chatVBox.localToScene(0, 0).getY(); // Y position of VBox in scene
        double viewportHeight = chatScrollPane.getViewportBounds().getHeight(); // Viewport height
        double totalHeight = chatVBox.getBoundsInLocal().getHeight(); // Total VBox height

        // Compute scroll position to center the HBox
        double position = (hboxY - vboxY - (viewportHeight / 2) + (targetHBox.getBoundsInLocal().getHeight() / 2))
                / (totalHeight - viewportHeight);

        // Ensure the value is between 0 and 1
        return Math.max(0, Math.min(1, position));
    }
    private void fadeOutBackgroundColor(HBox hbox) {
        // Stop any existing animation on this HBox
        if (hbox.getUserData() instanceof Timeline) {
            ((Timeline) hbox.getUserData()).stop();
        }

        // Create a new Timeline
        Timeline fadeTimeline = new Timeline();
        hbox.setUserData(fadeTimeline); // Store animation in the HBox itself

        // Base color #333138
        Color startColor = Color.web("#333138");

        // Opacity property to interpolate alpha
        ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(startColor);
        colorProperty.addListener((obs, oldColor, newColor) -> {
            hbox.setBackground(new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, Insets.EMPTY)));
        });

        // Animate the alpha from 1.0 (solid) to 0.0 (transparent)
        KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(2),
                new KeyValue(colorProperty, Color.web("#333138", 0)) // Transparent version of the color
        );

        fadeTimeline.getKeyFrames().add(keyFrame);
        fadeTimeline.setCycleCount(1);

        // Clear animation reference on completion
        fadeTimeline.setOnFinished(event -> hbox.setUserData(null));

        fadeTimeline.play();
    }
    private void showFullyMessagePicture() {
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
}
