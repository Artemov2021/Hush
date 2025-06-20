package com.messenger.database;

import com.messenger.main.ChatMessage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.scene.chart.PieChart;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatsDataBase {
    public static String getLastMessage(int senderId,int receiverId) throws SQLException {
        String statement = "SELECT message FROM chats WHERE sender_id IN (?,?) AND receiver_id IN (?,?) ORDER BY message_id DESC LIMIT 1";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,senderId);
            preparedStatement.setInt(2,receiverId);
            preparedStatement.setInt(3,senderId);
            preparedStatement.setInt(4,receiverId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getString("message");
            }
        }
        return "";
    }
    public static String getLastMessageTime(int mainUserId,int contactId) throws SQLException {
        String statement = "SELECT message_time FROM chats WHERE sender_id IN (?,?) AND receiver_id IN (?,?) ORDER BY message_id DESC LIMIT 1";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,contactId);
            preparedStatement.setInt(3,mainUserId);
            preparedStatement.setInt(4,contactId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getString("message_time");
            }
        }
        return "";
    }
    public static int getLastMessageId(int mainUserId,int contactId) throws SQLException {
        String statement = "SELECT message_id FROM chats WHERE sender_id IN (?,?) AND receiver_id IN (?,?) ORDER BY message_id DESC LIMIT 1";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,contactId);
            preparedStatement.setInt(3,mainUserId);
            preparedStatement.setInt(4,contactId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt("message_id");
            }
        }
        return -1;
    }
    public static int getFirstMessageId(int mainUserId, int contactId) throws SQLException {
        String statement = "SELECT message_id FROM chats WHERE sender_id IN (?,?) AND receiver_id IN (?,?) ORDER BY message_id ASC LIMIT 1";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, mainUserId);
            preparedStatement.setInt(4, contactId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt("message_id");
            }
        }
        return -1;
    }
    public static int addMessage(int senderId,int receiverId, String message,byte[] picture,int replyMessageId,String messageTime,String messageType,boolean received) throws SQLException {
        String statement = "INSERT INTO chats (sender_id,receiver_id,message,picture,reply_message_id,message_time,message_type,received) VALUES (?,?,?,?,?,?,?,?)";
        InputStream inputStreamPicture = (picture == null) ? (null) : (new ByteArrayInputStream(picture));

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1,senderId);
            preparedStatement.setInt(2,receiverId);
            preparedStatement.setString(3,message);
            preparedStatement.setBlob(4,inputStreamPicture);
            preparedStatement.setObject(5,replyMessageId);
            preparedStatement.setString(6,messageTime);
            preparedStatement.setString(7,messageType);
            preparedStatement.setBoolean(8,received);

            preparedStatement.executeUpdate();

            // Retrieve the generated keys
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1) == 0 ? -1 : generatedKeys.getInt(1); // Return the first generated key
                } else {
                    throw new SQLException("Failed to retrieve generated key, no key was returned.");
                }
            }
        }

    }
    public static ArrayList<ChatMessage> getAllMessages(int mainUserId, int contactId) throws SQLException {
        ArrayList<ChatMessage> messages = new ArrayList<>();
        String statement = "SELECT * FROM chats WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY message_time ASC;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, contactId);
            preparedStatement.setInt(4, mainUserId);
            ResultSet result = preparedStatement.executeQuery();

            ArrayList<ChatMessage> tempMessages = new ArrayList<>();

            // First load all messages and their IDs
            while (result.next()) {
                ChatMessage message = new ChatMessage(result); // Temp nextMessageId, will update later
                tempMessages.add(message);
            }

            // Now set the nextMessageId for each message
            for (int i = 0; i < tempMessages.size(); i++) {
                ChatMessage message = tempMessages.get(i);

                if (i > 0) {
                    ChatMessage previousMessage = tempMessages.get(i - 1);
                    message.setPreviousMessageData(previousMessage);
                } else {
                    message.setPreviousMessageData(null);
                }

                if (i < tempMessages.size() - 1) {
                    ChatMessage nextMessage = tempMessages.get(i + 1);
                    message.setNextMessageData(nextMessage);
                } else {
                    message.setNextMessageData(null);
                }

                messages.add(message);
            }
        }
        return messages;
    }
    public static ChatMessage getMessage(int mainUserId,int contactId,int messageId) throws SQLException {
        ChatMessage message = null;
        String statement = "SELECT * FROM chats WHERE message_id = ?";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                message = new ChatMessage(result);
                message.setPreviousMessageDataWithList(getPreviousMessage(mainUserId,contactId,messageId));
                message.setNextMessageDataWithList(getNextMessage(mainUserId,contactId,messageId));
            }
        }
        return message;
    }
    public static List<Object> getPreviousMessage(int mainUserId,int contactId,int messageId) throws SQLException {
        List<Object> previousMessage = new ArrayList<>();
        String getMessageIdStatement = "SELECT * \n" +
                "FROM chats \n" +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) \n" +
                "AND message_id < ?\n" +
                "ORDER BY message_id DESC;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            // Second Query: Get previous message_id
            PreparedStatement preparedStatement = connection.prepareStatement(getMessageIdStatement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, contactId);
            preparedStatement.setInt(4, mainUserId);
            preparedStatement.setInt(5, messageId); // Now correctly setting the last parameter

            ResultSet messageResult = preparedStatement.executeQuery();
            if (messageResult.next()) {
                previousMessage.add(messageResult.getInt("message_id"));
                previousMessage.add(messageResult.getInt("sender_id"));
                previousMessage.add(messageResult.getInt("receiver_id"));
                previousMessage.add(messageResult.getString("message"));
                previousMessage.add(messageResult.getBytes("picture"));
                previousMessage.add((messageResult.getInt("reply_message_id") == 0) ? (-1) : (messageResult.getInt("reply_message_id")));
                Timestamp ts = messageResult.getTimestamp("message_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String formattedTime = sdf.format(ts);
                previousMessage.add(formattedTime);
                previousMessage.add(messageResult.getString("message_type"));
                previousMessage.add(messageResult.getBoolean("received"));
                return previousMessage;
            }
        }
        return null;
    }
    public static List<Object> getNextMessage(int mainUserId,int contactId,int messageId) throws SQLException {
        List<Object> nextMessage = new ArrayList<>();
        String getMessageIdStatement = "SELECT * FROM chats WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) AND message_id > ?;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            // Second Query: Get previous message_id
            PreparedStatement preparedStatement = connection.prepareStatement(getMessageIdStatement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, contactId);
            preparedStatement.setInt(4, mainUserId);
            preparedStatement.setInt(5, messageId); // Now correctly setting the last parameter

            ResultSet messageResult = preparedStatement.executeQuery();
            if (messageResult.next()) {
                nextMessage.add(messageResult.getInt("message_id"));
                nextMessage.add(messageResult.getInt("sender_id"));
                nextMessage.add(messageResult.getInt("receiver_id"));
                nextMessage.add(messageResult.getString("message"));
                nextMessage.add(messageResult.getBytes("picture"));
                nextMessage.add((messageResult.getInt("reply_message_id") == 0) ? (-1) : (messageResult.getInt("reply_message_id")));
                Timestamp ts = messageResult.getTimestamp("message_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String formattedTime = sdf.format(ts);
                nextMessage.add(formattedTime);
                nextMessage.add(messageResult.getString("message_type"));
                nextMessage.add(messageResult.getBoolean("received"));
                return nextMessage;
            }
        }
        return nextMessage;
    }
    public static int getSenderIdWithMessageId(int messageId) throws SQLException {
        String statement = "SELECT sender_id FROM chats WHERE message_id = ?";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt("sender_id");
            }
        }
        return -1;
    }
    public static int getReceiverIdWithMessageId(int messageId) throws SQLException {
        String statement = "SELECT receiver_id FROM chats WHERE message_id = ?";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt("receiver_id");
            }
        }
        return -1;
    }
    public static void editMessage(int messageId,String newMessage,byte[] picture,String newMessageType) throws SQLException {
        String statement = "UPDATE chats SET message = ?,picture = ?,message_type = ? WHERE message_id = ?";
        InputStream inputStreamPicture = (picture == null) ? (null) : (new ByteArrayInputStream(picture));

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setString(1,newMessage);
            preparedStatement.setBlob(2,inputStreamPicture);
            preparedStatement.setString(3,newMessageType);
            preparedStatement.setInt(4,messageId);
            preparedStatement.executeUpdate();
        }
    }
    public static void deleteMessage(int messageId) throws SQLException {
        String statement = "DELETE FROM chats WHERE message_id = ?";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            preparedStatement.executeUpdate();
        }
    }
    public static boolean messageExists(int mainUserId,int contactId,int messageId) throws SQLException {
        String statement = "SELECT * FROM chats WHERE message_id = ? AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            preparedStatement.setInt(2,mainUserId);
            preparedStatement.setInt(3,contactId);
            preparedStatement.setInt(4,contactId);
            preparedStatement.setInt(5,mainUserId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return true;
            }
        }
        return false;
    }
    public static List<Integer> getRepliedMessageIds(int senderId,int receiverId,int messageId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String statement = "SELECT message_id FROM chats WHERE reply_message_id = ? AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            preparedStatement.setInt(2,senderId);
            preparedStatement.setInt(3,receiverId);
            preparedStatement.setInt(4,receiverId);
            preparedStatement.setInt(5,senderId);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                ids.add(result.getInt("message_id"));
            }
        }
        return ids;
    }
    public static int getPreviousMessageId(int mainUserId,int contactId,int messageId) throws SQLException {
        String getMessageIdStatement = "SELECT message_id \n" +
                "FROM chats \n" +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) \n" +
                "AND message_id < ?\n" +
                "ORDER BY message_id DESC;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
                // Second Query: Get previous message_id
                PreparedStatement preparedStatement = connection.prepareStatement(getMessageIdStatement);
                preparedStatement.setInt(1, mainUserId);
                preparedStatement.setInt(2, contactId);
                preparedStatement.setInt(3, contactId);
                preparedStatement.setInt(4, mainUserId);
                preparedStatement.setInt(5, messageId); // Now correctly setting the last parameter

                ResultSet messageIdResult = preparedStatement.executeQuery();
                if (messageIdResult.next()) {
                    int previousMessageId = messageIdResult.getInt(1);
                    if (!messageIdResult.wasNull()) {
                        return previousMessageId; // Return the previous message_id
                    }
                }
        }
        return -1; // Return -1 if no previous message is found
    }
    public static int getNextMessageId(int mainUserId,int contactId,int messageId) throws SQLException {
        String getMessageIdStatement = "SELECT message_id FROM chats WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) AND message_id > ?;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            // Second Query: Get previous message_id
            PreparedStatement preparedStatement = connection.prepareStatement(getMessageIdStatement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, contactId);
            preparedStatement.setInt(4, mainUserId);
            preparedStatement.setInt(5, messageId); // Now correctly setting the last parameter

            ResultSet messageIdResult = preparedStatement.executeQuery();
            if (messageIdResult.next()) {
                int nextMessageId = messageIdResult.getInt(1);
                if (!messageIdResult.wasNull()) {
                    return nextMessageId; // Return the previous message_id
                }
            }
        }
        return -1; // Return -1 if no previous message is found
    }
    public static ArrayList<ChatMessage> getNextMessages(int mainUserId,int contactId,int messageId) throws SQLException {
        ArrayList<ChatMessage> nextMessages = new ArrayList<>();
        String statement = "SELECT * \n" +
                "FROM chats \n" +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) \n" +
                "AND message_id > ?\n" +
                "ORDER BY message_id ASC;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, contactId);
            preparedStatement.setInt(4, mainUserId);
            preparedStatement.setInt(5,messageId);
            ResultSet result = preparedStatement.executeQuery();

            ArrayList<ChatMessage> tempMessages = new ArrayList<>();

            // First load all messages and their IDs
            while (result.next()) {
                ChatMessage message = new ChatMessage(result); // Temp nextMessageId, will update later
                tempMessages.add(message);
            }

            // Now set the nextMessageId for each message
            for (int i = 0; i < tempMessages.size(); i++) {
                ChatMessage message = tempMessages.get(i);

                if (i > 0) {
                    ChatMessage previousMessage = tempMessages.get(i - 1);
                    message.setPreviousMessageData(previousMessage);
                } else {
                    message.setPreviousMessageData(null);
                }

                if (i < tempMessages.size() - 1) {
                    ChatMessage nextMessage = tempMessages.get(i + 1);
                    message.setNextMessageData(nextMessage);
                } else {
                    message.setNextMessageData(null);
                }

                nextMessages.add(message);
            }
        }
        return nextMessages;
    }
    public static boolean isThereMessagesOnSameDay(int mainUserId, int contactId, int messageId, String fullTime) {
        String sql = """
        SELECT 1 FROM chats
        WHERE DATE(message_time) = DATE(?) 
          AND message_id != ?
          AND (
                (sender_id = ? AND receiver_id = ?)
                OR
                (sender_id = ? AND receiver_id = ?)
              )
        LIMIT 1
        """;

        try (Connection connection = DataBaseConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            // Set parameters
            preparedStatement.setString(1, fullTime);       // Full timestamp
            preparedStatement.setInt(2, messageId);         // Exclude the original
            preparedStatement.setInt(3, mainUserId);        // sender/receiver match
            preparedStatement.setInt(4, contactId);
            preparedStatement.setInt(5, contactId);
            preparedStatement.setInt(6, mainUserId);

            ResultSet result = preparedStatement.executeQuery();
            return result.next(); // returns true if at least 1 match found

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public static boolean hasMorePreviousMessages(int mainUserId, int contactId,int lastMessageId) throws SQLException {
        String getMessageIdStatement = "SELECT * \n" +
                "FROM chats \n" +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) \n" +
                "AND message_id < ?\n" +
                "ORDER BY message_id DESC;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            // Second Query: Get previous message_id
            PreparedStatement preparedStatement = connection.prepareStatement(getMessageIdStatement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, contactId);
            preparedStatement.setInt(4, mainUserId);
            preparedStatement.setInt(5, lastMessageId); // Now correctly setting the last parameter

            ResultSet messageResult = preparedStatement.executeQuery();
            if (messageResult.next()) {
                return true;
            }
        }
        return false;
    }
    public static boolean hasMoreNextMessages(int mainUserId, int contactId,int lastMessageId) throws SQLException {
        String getMessageIdStatement = "SELECT * \n" +
                "FROM chats \n" +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) \n" +
                "AND message_id > ?\n" +
                "ORDER BY message_id DESC;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            // Second Query: Get next message_id
            PreparedStatement preparedStatement = connection.prepareStatement(getMessageIdStatement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, contactId);
            preparedStatement.setInt(4, mainUserId);
            preparedStatement.setInt(5, lastMessageId); // Now correctly setting the last parameter

            ResultSet messageResult = preparedStatement.executeQuery();
            if (messageResult.next()) {
                return true;
            }
        }
        return false;
    }
    public static List<ChatMessage> getAllLeftMessages(int mainUserId,int contactId,int lastMessageId) throws SQLException {
        ArrayList<ChatMessage> previousMessages = new ArrayList<>();
        String statement = "SELECT * \n" +
                "FROM chats \n" +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) \n" +
                "AND message_id < ?\n" +
                "ORDER BY message_id ASC;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, contactId);
            preparedStatement.setInt(4, mainUserId);
            preparedStatement.setInt(5,lastMessageId);
            ResultSet result = preparedStatement.executeQuery();

            ArrayList<ChatMessage> tempMessages = new ArrayList<>();

            // First load all messages and their IDs
            while (result.next()) {
                ChatMessage message = new ChatMessage(result); // Temp nextMessageId, will update later
                tempMessages.add(message);
            }

            // Now set the nextMessageId for each message
            for (int i = 0; i < tempMessages.size(); i++) {
                ChatMessage message = tempMessages.get(i);
                List<Object> nextPotentialMessage = getNextMessage(mainUserId,contactId,tempMessages.get(i).id);

                if (i > 0) {
                    ChatMessage previousMessage = tempMessages.get(i - 1);
                    message.setPreviousMessageData(previousMessage);
                } else {
                    message.setPreviousMessageData(null);
                }

                if (i < tempMessages.size() - 1) {
                    ChatMessage nextMessage = tempMessages.get(i + 1);
                    message.setNextMessageData(nextMessage);
                } else if (i == tempMessages.size() - 1 && !nextPotentialMessage.isEmpty()) {
                    ChatMessage nextMessage = getMessage(mainUserId,contactId,(int) nextPotentialMessage.getFirst());
                    message.setNextMessageData(nextMessage);
                } else {
                    message.setNextMessageData(null);
                }

                previousMessages.add(message);
            }
        }
        return previousMessages;
    }
    public static ArrayList<Integer> getFoundMessageIds(int mainUserId,int contactId,String enteredTrimmedMessage) throws SQLException {
        ArrayList<ChatMessage> allMessages = getAllMessages(mainUserId,contactId);
        ArrayList<Integer> foundMessageIds = new ArrayList<>();
        for (ChatMessage message: allMessages) {
            if (message.message_text != null && message.message_text.toLowerCase().contains(enteredTrimmedMessage)) {
                foundMessageIds.add(message.id);
            }
        }
        return foundMessageIds;
    }
    public static long getUnreadMessagesAmount(int mainUserId,int contactId) throws SQLException {
        long amount = 0;
        String statement = "SELECT * FROM chats WHERE (receiver_id = ? AND sender_id = ?) AND received = 0";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,contactId);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                amount++;
            }
        }
        return amount;
    }
    public static void setAllMessagesRead(int mainUserId,int contactId) throws SQLException {
        String statement = "UPDATE chats SET received = 1 WHERE receiver_id = ? AND sender_id = ?";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,contactId);

            preparedStatement.executeUpdate();
        }
    }
    public static void setMessageRead(int messageId) throws SQLException {
        String statement = "UPDATE chats SET received = 1 WHERE message_id = ?";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1,messageId);

            preparedStatement.executeUpdate();
        }
    }
    public static boolean isThereUnreadMessages(int mainUserId) throws SQLException {
        String query = "SELECT EXISTS (SELECT 1 FROM chats WHERE receiver_id = ? AND received = 0) AS has_unread";

        try (Connection conn = DataBaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, mainUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("has_unread") == 1;
                }
            }
        }

        return false;
    }
}
