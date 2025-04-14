package com.messenger.database;

import com.messenger.main.MainWindowController;

import javax.xml.transform.Result;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ContactsDataBase extends MainWindowController {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/messengerdb";
    private static final String user = "root";
    private static final String password = "112233";

    public static int[] getContactsIdList(int mainUserId) throws SQLException {
        List<Integer> contactsIdList = new ArrayList<>();
        String statement = "SELECT contact_id FROM contacts WHERE user_id = ? ORDER BY last_interaction ASC;";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                contactsIdList.add(result.getInt("contact_id"));
            }
        }
        return contactsIdList.stream().mapToInt(Integer::intValue).toArray();
    }
    public static int[] getMatchedUsersId(String userNamePiece) throws SQLException {
        /* For example: user enters "Ar" and that method gives all user's mainUserId, which name
           beginns with "Ar" ( e.g. Artur,Ariana )  */

        int[] contactsId = getContactsIdList(mainUserId);
        return Arrays.stream(contactsId)
                .filter(contactId -> {
                    try {
                        return checkUserMatching(userNamePiece,contactId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray();
    }
    public static void addContact(int contactId) throws SQLException {
        String statement = "INSERT INTO contacts (user_id,contact_id,last_interaction) VALUES (?,?,?)";

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDateTimeNow = now.format(formatter1);

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.setTimestamp(3, Timestamp.valueOf(formattedDateTimeNow)); // Insert the Timestamp
            preparedStatement.executeUpdate();
        }
    }
    public static int[] getContactsIdListAfterContact(int mainUserId,int lastContactId) throws SQLException {
        List<Integer> contactsIdList = new ArrayList<>();
        String statement = "SELECT * FROM contacts WHERE last_interaction < ? ORDER BY last_interaction ASC;";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setTimestamp(1,getContactLastInteractionTime(mainUserId,lastContactId));
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                contactsIdList.add(result.getInt("contact_id"));
            }
        }
        return contactsIdList.stream().mapToInt(Integer::intValue).toArray();
    }
    public static void updateInteractionTime(int mainUserId,int contactId,String newTime) throws SQLException {
        String statement = "UPDATE contacts SET last_interaction = ? " +
                "WHERE (user_id = ? AND contact_id = ?) " +
                "OR (user_id = ? AND contact_id = ?)";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setString(1,newTime);
            preparedStatement.setInt(2,mainUserId);
            preparedStatement.setInt(3,contactId);
            preparedStatement.setInt(4,contactId);
            preparedStatement.setInt(5,mainUserId);
            preparedStatement.executeUpdate();
        }
    }
    public static void deleteContact(int mainUserId, int contactId) {
        String statement = "DELETE FROM contacts WHERE user_id = ? AND contact_id = ?;";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.executeUpdate();  // Anzahl der gelöschten Zeilen
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static boolean checkUserMatching(String enteredName,int userId) throws SQLException {
        String userName = UsersDataBase.getNameWithId(userId);

        String pattern = String.format("^%s",enteredName);
        Pattern patternObject = Pattern.compile(pattern);
        Matcher matcher = patternObject.matcher(userName);
        return matcher.find();
    }
    private static Timestamp getContactLastInteractionTime(int mainUserId,int lastContactId) throws SQLException {
        String statement = "SELECT last_interaction FROM contacts WHERE user_id = ? AND contact_id = ?";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            preparedStatement.setInt(2,lastContactId);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                return result.getTimestamp("last_interaction");
            }
        }
        return null;

    }
}
