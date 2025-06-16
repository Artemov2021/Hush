package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainContactController extends MainWindowController {
    @FXML private AnchorPane mainContactAnchorPane;
    @FXML public Pane mainContactPane;
    @FXML private Label mainContactAvatarLabel;
    @FXML private Label mainContactNameLabel;
    @FXML protected Label mainContactMessageLabel;
    @FXML protected Label mainContactTimeLabel;
    @FXML protected Label mainContactMessageCounterLabel;
    private MainWindowController mainWindowController;

    protected int contactId;

    public void injectUIElements(MainWindowController source) {
        this.mainWindowController = source;
        this.mainAnchorPane = source.mainAnchorPane;
        this.mainContactsVBox = source.mainContactsVBox;
    }
    public final void initializeContactPane() throws SQLException {
        setContactName(UsersDataBase.getNameWithId(contactId));
        setContactAvatar(UsersDataBase.getAvatarWithId(contactId));
        setContactLastMessage();
        setContactLastMessageTime();
        setNewMessagesCounter();
        mainContactPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    showChat();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (clickEvent.getButton() == MouseButton.SECONDARY) {
                int x = (int) convertToTopLevelAnchorPaneCoordinates(mainContactPane,clickEvent.getX(),clickEvent.getY()).getX();
                int y = (int) convertToTopLevelAnchorPaneCoordinates(mainContactPane,clickEvent.getX(),clickEvent.getY()).getY();
                if (!mainContactPane.getStyleClass().get(0).equals("contact-background-pane-focused")) {
                    mainContactPane.getStyleClass().clear();
                    setPaneHoveredStyle();
                }
                showDeleteContactButton(x,y);
            }
        });
    }
    public final void setContactId(int id) {
        this.contactId = id;
        setContactPaneId(id);
    }
    public final void setContactPaneId(int id) {
        mainContactAnchorPane.setId("mainContactAnchorPane"+id);
    }
    public final void setContactName(String name) {
        mainContactNameLabel.setText(name);
    }
    public final void setContactAvatar(byte[] avatarPicture) throws SQLException {
        if (UsersDataBase.getAvatarWithId(contactId) != null) {
            assert avatarPicture != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(avatarPicture);
            ImageView imageView = new ImageView(new Image(byteStream));
            imageView.setFitHeight(45);
            imageView.setFitWidth(45);
            imageView.setSmooth(true);
            mainContactAvatarLabel.setGraphic(imageView);
            Circle clip = new Circle();
            clip.setLayoutX(22.5);
            clip.setLayoutY(22.5);
            clip.setRadius(22.5);
            mainContactAvatarLabel.setClip(clip);
        }
    }
    public final void setContactLastMessage() throws SQLException {
        int lastMessageId = ChatsDataBase.getLastMessageId(mainUserId,contactId);
        String lastMessage = ChatsDataBase.getLastMessage(mainUserId,contactId);

        mainContactMessageLabel.getStyleClass().clear();

        // if there is no last message and its mainUserId ( chat is empty )
        if (lastMessage == null && lastMessageId == -1) {
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText("");

        // if there is a message id, but no message ( means it is a picture )
        } else if (lastMessageId != -1 && lastMessage == null) {
            mainContactMessageLabel.setStyle("-fx-text-fill: white;");
            mainContactMessageLabel.setText("Picture");

        // otherwise ( if there is last message )
        } else {
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText(lastMessage);
        }

    }
    public final void setContactLastMessageTime() throws SQLException {
        String lastMessageFullDate = ChatsDataBase.getLastMessageTime(mainUserId,contactId);
        String pattern = "(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}:\\d{2})"; // Extracts YYYY, MM, DD, HH:mm
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(lastMessageFullDate);

        if (matcher.find()) {
            String year = matcher.group(1);
            String month = matcher.group(2);
            String day = matcher.group(3);
            String time = matcher.group(4);

            LocalDate messageDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1); // Calculate yesterday's date

            if (messageDate.isEqual(today)) {
                mainContactTimeLabel.setText(time); // Show only HH:mm if it's today
            } else if (messageDate.isEqual(yesterday)) {
                mainContactTimeLabel.setText("yesterday"); // Show "yesterday" if the date is yesterday
            } else {
                mainContactTimeLabel.setText(day + "." + month + "." + year); // Show full date if not today
            }
        } else {
            mainContactTimeLabel.setText(""); // Default to empty if no match
        }
    }
    public final void setNewMessagesCounter() throws SQLException {
        long newMessagesAmount = ChatsDataBase.getUnreadMessagesAmount(mainUserId,contactId);
        if (newMessagesAmount > 0) {
            mainContactMessageCounterLabel.setVisible(true);
            if (newMessagesAmount > 9) {
                mainContactMessageCounterLabel.getStyleClass().clear();
                mainContactMessageCounterLabel.getStyleClass().add("contact-new-message-counter-overflow-label");
                mainContactMessageCounterLabel.setPadding(new Insets(5,2,5,2));
                mainContactMessageCounterLabel.setText("9+");
            } else {
                mainContactMessageCounterLabel.setText(String.valueOf(newMessagesAmount));
            }
        }
    }

    private void showChat() throws Exception {
        setPanesNormalStyle();
        setCurrentPaneFocusedStyle();

        // shut down the previous chat background threads
        for (Node child : mainAnchorPane.getChildren()) {
            if ("chatAnchorPane".equals(child.getId())) {
                Object data = child.getUserData();
                if (data instanceof MainChatController controller) {
                    controller.shutdown(); // Call shutdown to stop threads/listeners
                }
            }
        }

        mainAnchorPane.getChildren().removeIf(child ->
                child.getId() != null && child.getId().contains("chatAnchorPane")
        );

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainChat.fxml"));
        Parent chatRoot = fxmlLoader.load();

        MainChatController mainChatController = fxmlLoader.getController();
        mainChatController.setChatContactId(contactId);
        mainChatController.injectMainUIElements(mainWindowController);
        mainChatController.injectContactUIElements(this);
        mainChatController.initializeChat();

        chatRoot.setId("chatAnchorPane"+contactId);
        chatRoot.setUserData(mainChatController);

        mainAnchorPane.getChildren().add(0, chatRoot);
    }

    private void setPanesNormalStyle() {
        VBox mainVBox = (VBox) mainAnchorPane.lookup("#mainContactsVBox");

        mainVBox.getChildren().stream()
                .filter(node -> node instanceof AnchorPane)
                .map(node -> (AnchorPane) node)
                .flatMap(anchorPane -> anchorPane.getChildren().stream())
                .filter(child -> child instanceof Pane)
                .map(child -> (Pane) child)
                .forEach(pane -> {
                    pane.getStyleClass().setAll("contact-background-pane");
                });
    }
    private void setCurrentPaneFocusedStyle() {
        mainContactPane.getStyleClass().clear();
        mainContactPane.getStyleClass().add("contact-background-pane-focused");
    }
    private void showDeleteContactButton(int clickPlaceX,int clickPlaceY) {
        Pane messageButtonsOverlay = new Pane();
        messageButtonsOverlay.setPrefWidth(mainAnchorPane.getPrefWidth());
        messageButtonsOverlay.setPrefHeight(mainAnchorPane.getPrefHeight());
        messageButtonsOverlay.setLayoutX(0);
        messageButtonsOverlay.setLayoutY(0);
        messageButtonsOverlay.setStyle("-fx-background-color: transparent");
        mainAnchorPane.getChildren().add(messageButtonsOverlay);
        messageButtonsOverlay.setOnMouseClicked(clickEvent -> {
            mainAnchorPane.getChildren().remove(messageButtonsOverlay);
            if (!mainContactPane.getStyleClass().get(0).equals("contact-background-pane-focused")) {
                mainContactPane.getStyleClass().clear();
                mainContactPane.getStyleClass().add("contact-background-pane");
            }
        });
        Platform.runLater(() -> {
            messageButtonsOverlay.getScene().getStylesheets().add(getClass().getResource("/main/css/MainContact.css").toExternalForm());
        });

        Pane messageButtonsBackground = new Pane();
        messageButtonsBackground.setCursor(Cursor.HAND);
        messageButtonsBackground.setPrefWidth(153);
        messageButtonsBackground.setPrefHeight(44);
        messageButtonsBackground.setLayoutX(clickPlaceX);
        messageButtonsBackground.setLayoutY((clickPlaceY >= 900) ? (clickPlaceY - 42) : clickPlaceY);
        messageButtonsBackground.getStyleClass().add("contact-delete-button-background");
        messageButtonsOverlay.getChildren().add(messageButtonsBackground);

        Pane deletePane = new Pane();
        deletePane.setPrefWidth(144);
        deletePane.setPrefHeight(34);
        deletePane.setLayoutX(5);
        deletePane.setLayoutY(5);
        deletePane.getStyleClass().add("contact-delete-button-small-pane");
        deletePane.setOnMouseClicked(clickEvent -> {
            showDeleteContactConfirmation();
        });
        messageButtonsBackground.getChildren().add(deletePane);

        Label deleteSymbol = new Label();
        deleteSymbol.setPrefWidth(18);
        deleteSymbol.setPrefHeight(18);
        deleteSymbol.setLayoutX(7);
        deleteSymbol.setLayoutY(8);
        deleteSymbol.getStyleClass().add("contact-delete-button-symbol");
        deletePane.getChildren().add(deleteSymbol);

        Label deleteText = new Label("Delete contact");
        deleteText.setLayoutX(34);
        deleteText.setLayoutY(8);
        deleteText.getStyleClass().add("contact-delete-button-text");
        deletePane.getChildren().add(deleteText);
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
    private void setPaneHoveredStyle() {
        mainContactPane.getStyleClass().add("contact-background-pane-hovered");
    }
    private void showDeleteContactConfirmation() {
        Pane confirmationOverlay = new Pane();
        confirmationOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.68)");
        confirmationOverlay.setLayoutX(0);
        confirmationOverlay.setLayoutY(0);
        confirmationOverlay.setPrefWidth(mainAnchorPane.getWidth());
        confirmationOverlay.setPrefHeight(mainAnchorPane.getHeight());
        confirmationOverlay.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                mainAnchorPane.getChildren().remove(confirmationOverlay);
            }
        });
        mainAnchorPane.getChildren().add(confirmationOverlay);

        Pane confirmationBackground = new Pane();
        confirmationBackground.getStyleClass().add("contact-delete-confirmation-window-background");
        confirmationBackground.setLayoutX(778);
        confirmationBackground.setLayoutY(401);
        confirmationBackground.setPrefWidth(409);
        confirmationBackground.setPrefHeight(149);
        confirmationBackground.setOnMouseClicked(Event::consume);
        confirmationOverlay.getChildren().add(confirmationBackground);

        Label confirmationText = new Label("You want to delete that contact? ");
        confirmationText.getStyleClass().add("contact-delete-confirmation-window-text");
        confirmationText.setLayoutX(30);
        confirmationText.setLayoutY(20);
        confirmationBackground.getChildren().add(confirmationText);

        Label confirmationDeleteButton = new Label();
        confirmationDeleteButton.setCursor(Cursor.HAND);
        confirmationDeleteButton.getStyleClass().add("contact-delete-confirmation-window-delete-button");
        confirmationDeleteButton.setLayoutX(290);
        confirmationDeleteButton.setLayoutY(94);
        confirmationDeleteButton.setPrefWidth(98);
        confirmationDeleteButton.setPrefHeight(40);
        confirmationBackground.getChildren().add(confirmationDeleteButton);
        confirmationDeleteButton.setOnMouseClicked(clickEvent -> {
            ContactsDataBase.deleteContact(mainUserId,contactId);
            mainContactsVBox.getChildren().remove(mainContactAnchorPane);
            mainAnchorPane.getChildren().remove(confirmationOverlay);
        });

        Label confirmationCancelButton = new Label();
        confirmationCancelButton.setCursor(Cursor.HAND);
        confirmationCancelButton.getStyleClass().add("contact-delete-confirmation-window-cancel-button");
        confirmationCancelButton.setLayoutX(175);
        confirmationCancelButton.setLayoutY(94);
        confirmationCancelButton.setPrefWidth(104);
        confirmationCancelButton.setPrefHeight(40);
        confirmationBackground.getChildren().add(confirmationCancelButton);
        confirmationCancelButton.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                mainAnchorPane.getChildren().remove(confirmationOverlay);
            }
        });

        // Appearing time
        FadeTransition FadeIn = new FadeTransition(Duration.millis(180),confirmationOverlay);
        FadeIn.setFromValue(0);
        FadeIn.setToValue(1);
        FadeIn.play();

        // Appearing move to left
        TranslateTransition translateIn = new TranslateTransition(Duration.millis(180), confirmationBackground);
        translateIn.setFromX(0);
        translateIn.setToX(-35);
        translateIn.play();
    }

}
