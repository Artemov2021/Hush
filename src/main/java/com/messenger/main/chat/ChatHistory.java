package com.messenger.main.chat;

import com.messenger.main.MainChatController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ChatHistory extends MainChatController {
    private List<List<Object>> allMessages;

    public ChatHistory(List<List<Object>> givenMessages) {
        this.allMessages = givenMessages;
    }
    public void load() {
        boolean chatIsEmpty = true; //allMessages.isEmpty();

        if (chatIsEmpty) {
            setCurrentDateLabel();
        }
    }

    private void setCurrentDateLabel() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
        String formattedDate = today.format(formatter);
        setChatDateLabel(formattedDate);
    }
    private void setChatDateLabel(String date) {
        System.out.println(date);
        Label chatDateLabel = new Label(date);
        chatDateLabel.getStyleClass().add("chat-date-label");
        VBox.setMargin(chatDateLabel,new Insets(15,0,25,0));
        chatVBox.getChildren().add(chatDateLabel);
    }
}
