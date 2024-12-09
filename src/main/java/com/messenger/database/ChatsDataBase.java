package com.messenger.database;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import javax.print.attribute.standard.JobHoldUntil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatsDataBase {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/messengerdb";
    private static final String user = "root";
    private static final String password = "112233";

    public static String getLastMessage(int mainUserId,int contactId) throws SQLException {
        String statement = "SELECT message FROM chats WHERE sender_id IN (?,?) AND receiver_id IN (?,?) ORDER BY message_id DESC LIMIT 1";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,contactId);
            preparedStatement.setInt(3,mainUserId);
            preparedStatement.setInt(4,contactId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getString("message");
            }
        }
        return "";
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
    public static int addMessage(int senderId,int receiverId, String message,byte[] picture,int replyMessageId,String messageTime,boolean received) throws SQLException {
        String statement = "INSERT INTO chats (sender_id,receiver_id,message,picture,reply_message_id,message_time,received) VALUES (?,?,?,?,?,?,?)";
        InputStream inputStreamPicture = (picture == null) ? (null) : (new ByteArrayInputStream(picture));

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1,senderId);
            preparedStatement.setInt(2,receiverId);
            preparedStatement.setString(3,message);
            preparedStatement.setBlob(4,inputStreamPicture);
            preparedStatement.setObject(5,replyMessageId);
            preparedStatement.setString(6,messageTime);
            preparedStatement.setBoolean(7,received);

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
    public static ArrayList<List<Object>> getAllMessages(int mainUserId,int contactId) throws SQLException {
        ArrayList<List<Object>> messages = new ArrayList<>();
        String statement = "SELECT * FROM chats WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,contactId);
            preparedStatement.setInt(3,contactId);
            preparedStatement.setInt(4,mainUserId);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                List<Object> message = new ArrayList<>();
                message.add(result.getInt("message_id"));
                message.add(result.getInt("sender_id"));
                message.add(result.getInt("receiver_id"));
                message.add(result.getString("message"));
                message.add(result.getBytes("picture"));
                message.add(result.getInt("reply_message_id"));
                message.add(result.getString("message_time"));
                message.add(result.getString("received"));
                messages.add(message);
            }
        }
        return messages;
    }
    public static List<Object> getMessageWithId(int messageId) throws SQLException {
        List<Object> message = new ArrayList<>();
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
                message.add(result.getString("received"));
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




}
