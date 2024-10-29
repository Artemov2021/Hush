package com.messenger.database;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    public static int addMessage(int senderId,int receiverId, String message,byte[] picture,int replyMessageId,String messageTime) throws SQLException {
        String statement = "INSERT INTO chats (sender_id,receiver_id,message,picture,reply_message_id,message_time) VALUES (?,?,?,?,?,?)";
        InputStream inputStreamPicture = (picture.length == 0) ? (null) : (new ByteArrayInputStream(picture));

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1,senderId);
            preparedStatement.setInt(2,receiverId);
            preparedStatement.setString(3,message);
            preparedStatement.setBlob(4,inputStreamPicture);
            preparedStatement.setObject(5, (replyMessageId == -1) ? null : replyMessageId);
            preparedStatement.setString(6,messageTime);

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






}
