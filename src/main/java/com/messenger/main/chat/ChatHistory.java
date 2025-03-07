package com.messenger.main.chat;

import com.messenger.database.ChatsDataBase;
import com.messenger.main.MainChatController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.print.attribute.standard.JobKOctets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatHistory {

    private int mainUserId;
    private int contactId;
    private Pane backgroundPane;
    private VBox chatVBox;


    public ChatHistory(int mainUserId, int contactId,Pane backgroundPane,VBox chatVBox) {
        this.mainUserId = mainUserId;
        this.contactId = contactId;
        this.backgroundPane = backgroundPane;
        this.chatVBox = chatVBox;
    }


    public void load() throws SQLException {
        boolean chatIsEmpty = ChatsDataBase.getAllMessages(mainUserId,contactId).isEmpty();

        if (chatIsEmpty) {
            setCurrentDateLabel();
        } else {
            loadChatHistory();
        }
    }


    public void loadChatHistory() throws SQLException {
        List<ArrayList<Object>> allMessages = ChatsDataBase.getAllMessages(mainUserId,contactId);
        HashMap<String,List<ArrayList<Object>>> splitIntoDaysMessages = getSplitIntoDaysMessages(allMessages);
        splitIntoDaysMessages.values().forEach(this::loadMessagesWithDateLabel);
    }

    public HashMap<String,List<ArrayList<Object>>> getSplitIntoDaysMessages(List<ArrayList<Object>> allMessages) {
        HashMap<String,List<ArrayList<Object>>> splitIntoDaysMessages = new HashMap<>();

        for (ArrayList<Object> message: allMessages) {
            if (!splitIntoDaysMessages.containsKey(getShortDateFromFullDate((String) message.get(6)))) {
                splitIntoDaysMessages.put(getShortDateFromFullDate((String) message.get(6)), new ArrayList<>());
            }
            splitIntoDaysMessages.get(getShortDateFromFullDate((String) message.get(6))).add(message);

        }
        return splitIntoDaysMessages;
    }
    public void loadMessagesWithDateLabel(List<ArrayList<Object>> messagesOnSameDay) {
        String labelDate = getDateForDateLabel((String) messagesOnSameDay.get(0).get(6));     // 2. March
        setChatDateLabel(labelDate);

        for (ArrayList<Object> message: messagesOnSameDay) {
//            String messageType = getMessageType(message);
//            switch (messageType) {
//                case "text":
//                    loadTextMessage(message);
//                    break;
//                case "reply_with_text":
//                    loadReplyWithTextMessage(message);
//                    break;
//            }
            System.out.println(message);
        }
    }


    // Message Loading
    public void loadTextMessage(ArrayList<Object> message) {

    }



    // Date Label
    private void setCurrentDateLabel() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.ENGLISH);
        String formattedDate = today.format(formatter);
        setChatDateLabel(formattedDate);
    }
    private void setChatDateLabel(String date) {
        Label chatDateLabel = new Label(date);
        chatDateLabel.getStyleClass().add("chat-date-label");
        VBox.setMargin(chatDateLabel,new Insets(8,0,8,0));
        chatVBox.getChildren().add(chatDateLabel);
    }


    // Date Operations
    private String getShortDateFromFullDate(String fullDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(fullDate, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return dateTime.format(outputFormatter);
    }
    private String getDateForDateLabel(String fullDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
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
}
