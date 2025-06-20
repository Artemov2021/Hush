package com.messenger.database;

import com.messenger.main.ChatMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LogsDataBase {
    public static void addAction(ActionType changeType, int messageId, int senderId, int receiverId, String message, byte[] picture, int replyMessageId, String messageTime, String messageType) throws SQLException {
        String statement = "INSERT INTO logs (change_type,message_id,sender_id,receiver_id,message,picture,reply_message_id,message_time,message_type) VALUES (?,?,?,?,?,?,?,?,?)";
        InputStream inputStreamPicture = (picture == null) ? (null) : (new ByteArrayInputStream(picture));

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1,changeType.name().toLowerCase());
            preparedStatement.setInt(2,messageId);
            preparedStatement.setInt(3,senderId);
            preparedStatement.setInt(4,receiverId);
            preparedStatement.setString(5,message);
            preparedStatement.setBlob(6,inputStreamPicture);
            preparedStatement.setObject(7,replyMessageId);
            preparedStatement.setString(8,messageTime);
            preparedStatement.setString(9,messageType);

            preparedStatement.executeUpdate();
        }

    }
    public static int getLastContactsActionId(int mainUserId) throws SQLException {
        int lastId = -1;  // default value if no result found
        String statement = "SELECT id FROM logs WHERE receiver_id = ? ORDER BY id DESC LIMIT 1";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, mainUserId);
            ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {
                lastId = result.getInt("id") == 0 ? -1 : result.getInt("id");
            }
        }

        return lastId;
    }
    public static int getLastContactActionId(int mainUserId,int contactId) throws SQLException {
        String statement = "SELECT id FROM logs WHERE sender_id IN (?,?) AND receiver_id IN (?,?) ORDER BY id DESC LIMIT 1";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,contactId);
            preparedStatement.setInt(3,contactId);
            preparedStatement.setInt(4,mainUserId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt("id");
            }
        }
        return -1;
    }
    public static ArrayList<Integer> getNewActionIds(int mainUserId,int lastActionId) throws SQLException {
        ArrayList<Integer> newActionIds = new ArrayList<>();
        String statement = "SELECT id FROM logs WHERE (receiver_id = ?) AND id > ? ORDER BY message_id ASC;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(3, lastActionId);
            ResultSet result = preparedStatement.executeQuery();

            // First load all messages and their IDs
            while (result.next()) {
                newActionIds.add(result.getInt("id"));
            }
        }
        return newActionIds;
    }
    public static ArrayList<Integer> getNewContactActionIds(int mainUserId,int contactId,int lastActionId) throws SQLException {
        ArrayList<Integer> newActionIds = new ArrayList<>();
        String statement = "SELECT id FROM logs WHERE (receiver_id = ? AND sender_id = ?) AND id > ? ORDER BY message_id ASC;";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setInt(3, lastActionId);
            ResultSet result = preparedStatement.executeQuery();

            // First load all messages and their IDs
            while (result.next()) {
                newActionIds.add(result.getInt("id"));
            }
        }
        return newActionIds;
    }
    public static ArrayList<Object> getAction(int actionId) throws SQLException {
        ArrayList<Object> action = new ArrayList<>();
        String statement = "SELECT * FROM logs WHERE id = ?";

        try (Connection connection = DataBaseConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,actionId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                action.add(result.getInt("id"));
                action.add(ActionType.fromString(result.getString("change_type")));
                action.add(result.getInt("message_id"));
                action.add(result.getInt("sender_id"));
                action.add(result.getInt("receiver_id"));
                action.add(result.getString("message"));
                action.add(result.getBytes("picture"));
                action.add(result.getInt("reply_message_id"));
                Timestamp ts = result.getTimestamp("message_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String formattedTime = ts != null ? sdf.format(ts) : null;
                action.add(formattedTime);
                action.add(result.getString("message_type"));
            }
        }
        return action;
    }
}
