package com.messenger.database;

import com.messenger.main.ChatMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatsDataBase {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/messengerdb";
    private static final String user = "root";
    private static final String password = "112233";

    public static String getLastMessage(int senderId,int receiverId) throws SQLException {
        String statement = "SELECT message FROM chats WHERE sender_id IN (?,?) AND receiver_id IN (?,?) ORDER BY message_id DESC LIMIT 1";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
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
    public static List<Object> getLastMessageWithId(int mainUserId,int contactId) throws SQLException {
        List<Object> lastMessageWithId = new ArrayList<>();
        String statement = "SELECT message,message_id FROM chats WHERE sender_id IN (?,?) AND receiver_id IN (?,?) ORDER BY message_id DESC LIMIT 1";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,contactId);
            preparedStatement.setInt(3,mainUserId);
            preparedStatement.setInt(4,contactId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                lastMessageWithId.add(result.getString("message"));
                lastMessageWithId.add(result.getInt("message_id"));
                return lastMessageWithId;
            }
        }
        lastMessageWithId.add(null);
        lastMessageWithId.add(-1);
        return lastMessageWithId;
    }
    public static String getLastMessageTime(int mainUserId,int contactId) throws SQLException {
        String statement = "SELECT message_time FROM chats WHERE sender_id IN (?,?) AND receiver_id IN (?,?) ORDER BY message_id DESC LIMIT 1";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
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

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
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
    public static int addMessage(int senderId,int receiverId, String message,byte[] picture,int replyMessageId,String messageTime,String messageType,boolean received) throws SQLException {
        String statement = "INSERT INTO chats (sender_id,receiver_id,message,picture,reply_message_id,message_time,message_type,received) VALUES (?,?,?,?,?,?,?,?)";
        InputStream inputStreamPicture = (picture == null) ? (null) : (new ByteArrayInputStream(picture));

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
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
                    return generatedKeys.getInt(1); // Return the first generated key
                } else {
                    throw new SQLException("Failed to retrieve generated key, no key was returned.");
                }
            }
        }

    }
    public static ArrayList<ChatMessage> getAllMessages(int mainUserId, int contactId) throws SQLException {
        ArrayList<ChatMessage> messages = new ArrayList<>();
        String statement = "SELECT * FROM chats WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY message_time ASC;";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,contactId);
            preparedStatement.setInt(3,contactId);
            preparedStatement.setInt(4,mainUserId);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                int messageId = result.getInt("message_id");
                ChatMessage message = new ChatMessage(messageId);
                messages.add(message);
            }
        }
        return messages;
    }
    public static ArrayList<Object> getMessage(int messageId) throws SQLException {
        ArrayList<Object> message = new ArrayList<>();
        String statement = "SELECT * FROM chats WHERE message_id = ?";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                message.add(result.getInt("message_id"));
                message.add(result.getInt("sender_id"));
                message.add(result.getInt("receiver_id"));
                message.add(result.getString("message"));
                message.add(result.getBytes("picture"));
                message.add((result.getInt("reply_message_id") == 0) ? (-1) : (result.getInt("reply_message_id")));
                message.add(result.getString("message_time"));
                message.add(result.getString("message_type"));
                message.add(result.getBoolean("received"));
            }
        }
        return message;
    }
    public static int getSenderIdWithMessageId(int messageId) throws SQLException {
        String statement = "SELECT sender_id FROM chats WHERE message_id = ?";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
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

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt("receiver_id");
            }
        }
        return -1;
    }
    public static void editMessage(int messageId,String newMessage,byte[] picture) throws SQLException {
        String statement = "UPDATE chats SET message = ?,picture = ? WHERE message_id = ?";
        InputStream inputStreamPicture = (picture == null) ? (null) : (new ByteArrayInputStream(picture));

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setString(1,newMessage);
            preparedStatement.setBlob(2,inputStreamPicture);
            preparedStatement.setInt(3,messageId);
            preparedStatement.executeUpdate();
        }
    }
    public static void deleteMessage(int messageId) throws SQLException {
        String statement = "DELETE FROM chats WHERE message_id = ?";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            preparedStatement.executeUpdate();
        }
    }
    public static boolean messageExists(int senderId,int receiverId,int messageId) throws SQLException {
        String statement = "SELECT * FROM chats WHERE message_id = ? AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,messageId);
            preparedStatement.setInt(2,senderId);
            preparedStatement.setInt(3,receiverId);
            preparedStatement.setInt(4,receiverId);
            preparedStatement.setInt(5,senderId);
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

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
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
    public static int getPreviousMessageId(int messageId,int mainUserId,int contactId) throws SQLException {
        String getMessageIdStatement = "SELECT message_id \n" +
                "FROM chats \n" +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) \n" +
                "AND message_id < ?\n" +
                "ORDER BY message_id DESC;";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
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
}
