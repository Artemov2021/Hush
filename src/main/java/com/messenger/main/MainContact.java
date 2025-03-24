package com.messenger.main;

import com.messenger.database.UsersDataBase;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainContact {
    @FXML
    private Pane mainContactPane;
    @FXML
    private Label mainContactAvatarLabel;
    @FXML
    private Label mainContactNameLabel;
    @FXML
    private Label mainContactMessageLabel;
    @FXML
    private Label mainContactTimeLabel;

    private AnchorPane mainAnchorPane;
    private int mainUserId;

    public void setMainUserId(int id) {
        this.mainUserId = id;
    }
    public void setName(String name) {
        mainContactNameLabel.setText(name);
    }
    public void setAvatar(int contactId) throws SQLException {
        if (UsersDataBase.getAvatarWithId(contactId) != null) {
            byte[] blobBytes = UsersDataBase.getAvatarWithId(contactId);
            assert blobBytes != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(blobBytes);
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
    public void setMessage(String message,int messageId) {
        mainContactMessageLabel.getStyleClass().clear();

        // if there is no last message and its id ( chat is empty )
        if (message == null && messageId == -1) {
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText("");

        // if there is no message, but there is message id ( means it is a picture )
        } else if (message == null && messageId != -1) {
            mainContactMessageLabel.setStyle("-fx-text-fill: white;");
            mainContactMessageLabel.setText("Picture");

        // otherwise ( if there is last message )
        } else {
            mainContactMessageLabel.getStyleClass().add("contact-last-message-label");
            mainContactMessageLabel.setText(message);
        }

    }
    public void setTime(String time) {
        String pattern = "\\d\\d:\\d\\d$";
        Pattern compliedPattern = Pattern.compile(pattern);
        Matcher matcher = compliedPattern.matcher(time);
        if (matcher.find()) {
            mainContactTimeLabel.setText(matcher.group());
        } else {
            mainContactTimeLabel.setText("");
        }
    }
    public void setPaneId(int contactId) {
        mainContactPane.setId("mainContactPane"+contactId);
    }
    public void setMainAnchorPane(AnchorPane anchorPane) {
        this.mainAnchorPane = anchorPane;
    }
    public void initialize() {
        mainContactPane.setOnMouseClicked(clickEvent -> {
            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    showChat();
                } catch (IOException | SQLException | ExecutionException | InterruptedException e) {
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

    public void showChat() throws IOException, SQLException, ExecutionException, InterruptedException {
        int currentUserId = Integer.parseInt(mainContactPane.getId().split("mainContactPane")[1]);

        setPanesNormalStyle();
        setCurrentPaneFocusedStyle();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/fxml/MainChat.fxml"));
        Parent chatRoot = fxmlLoader.load();

        MainChatController mainChatController = fxmlLoader.getController();
        mainChatController.setMainAnchorPane(mainAnchorPane);
        mainChatController.setMainUserId(mainUserId);
        mainChatController.setContactId(currentUserId);
        mainChatController.setMainContactPane(mainContactPane);
        mainChatController.initializeWithValue();

        mainAnchorPane.getChildren().removeIf(child -> Objects.equals(child.getId(), "chatAnchorPane"));
        mainAnchorPane.getChildren().add(0,chatRoot);
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
