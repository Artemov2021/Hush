package com.messenger.database;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class DetailedDataBase {
    private final static String sqlPath = "jdbc:sqlite:details/";

    public static void createUserDataBase (int mainUserId) throws SQLException, IOException {
        String detailedDBLink = sqlPath + UsersDataBase.getNameWithId(mainUserId) + ".db";
        String statement = "CREATE TABLE IF NOT EXISTS contact_list (id integer)";
        try (Connection connection = DriverManager.getConnection(detailedDBLink)) {
            Statement stmt = connection.createStatement();
            stmt.execute(statement);
        } catch (Exception e) {
            throw e;
        }
    }

    public static ArrayList<ArrayList<String>> getMessages(int mainUserId,int contactId) throws SQLException {
        ArrayList<ArrayList<String>> messages = new ArrayList<>();
        String detailedDBLink = sqlPath + UsersDataBase.getNameWithId(mainUserId) + ".db";
        String statement = "SELECT id,message,message_time,photo FROM \""+contactId+"\"";
        try (Connection connection = DriverManager.getConnection(detailedDBLink)) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(statement);
            while (result.next()) {
                ArrayList<String> message = new ArrayList<>(Arrays.asList(result.getString("id"),result.getString("message"),
                        result.getString("message_time"),result.getString("photo")));
                messages.add(message);
            }
            return messages;
        }
    }
    public static void addContactToContactList(int mainUserId,int contactId) throws SQLException {
        // Adding a new contact to the contact list of the main user
        String detailedDBLink = sqlPath + UsersDataBase.getNameWithId(mainUserId) + ".db";
        String statement1 = "INSERT INTO contact_list (id) VALUES (?)";
        try (Connection connection1 = DriverManager.getConnection(detailedDBLink)) {
            PreparedStatement stmt1 = connection1.prepareStatement(statement1);
            stmt1.setInt(1,contactId);
            stmt1.executeUpdate();
        }
    }
    public static void createContactTable(int mainUserId,int contactId) throws SQLException {
        // Creating new contact table in user database
        String detailedDBLink = sqlPath + UsersDataBase.getNameWithId(mainUserId) + ".db";
        try (Connection connection = DriverManager.getConnection(detailedDBLink)) {
            String statement = String.format("CREATE TABLE IF NOT EXISTS %s (id integer PRIMARY KEY,user_id integer ,message text,message_time text,photo text)","\""+contactId+"\"");
            Statement stmt = connection.createStatement();
            stmt.execute(statement);
        }
    }

    public static boolean checkUserPresence(int mainUserId,String contactName) throws SQLException, IOException {
        String detailedDBLink = sqlPath + UsersDataBase.getNameWithId(mainUserId) + ".db";
        String statement = "SELECT id FROM contact_list WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(detailedDBLink)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setInt(1,UsersDataBase.getIdWithName(contactName));
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return true;
            }
            return false;
        } catch (Exception e) {

            throw e;
        }
    }

    public static String getLastMessage(int mainUserId,int contactId) throws SQLException {
        String mainUserName = UsersDataBase.getNameWithId(mainUserId);
        String lastMessage = null;
        String detailedDBLink = sqlPath + mainUserName + ".db";
        String statement = "SELECT message FROM \"" + contactId + "\" WHERE id = (SELECT MAX(id) FROM \"" + contactId + "\")";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(statement);
        if (result.next()) {
            lastMessage = result.getString(1);
            connection.close();
        }
        connection.close();
        System.out.println("last message: "+lastMessage);
        return lastMessage;
    }

    public static ArrayList<Integer> getContactsIds(int mainUserId) throws SQLException {
        ArrayList<Integer> contacts = new ArrayList<>();
        String detailedDBLink = sqlPath + UsersDataBase.getNameWithId(mainUserId) + ".db";
        String statement = "SELECT * FROM contact_list";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(statement);
        while (result.next()) {
            contacts.add(result.getInt("id"));
        }
        connection.close();
        return contacts;
    }

    public static ArrayList<String> getContactsNames(String mainUser) throws SQLException {
        ArrayList<String> contacts = new ArrayList<>();
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement = "SELECT * FROM contact_list";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(statement);
        while (result.next()) {
            contacts.add(UsersDataBase.getNameWithId(result.getInt("id")));
        }
        connection.close();
        return contacts;
    }

    public static int getLastMessageId(int mainUserId,int contactId) throws SQLException {
        String detailedDBLink = sqlPath + UsersDataBase.getNameWithId(mainUserId) + ".db";
        String statement = "SELECT id FROM \"" + contactId + "\" WHERE id = (SELECT MAX(id) FROM \"" + contactId + "\")";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(statement);
        if (result.next()) {
            int id = result.getInt(1);
            connection.close();
            return id;
        }
        connection.close();
        return -1;
    }

    public static String getMessageTime(int mainUserId,int contactId,int messageId) throws SQLException {
        String detailedDBLink = sqlPath + UsersDataBase.getNameWithId(mainUserId) + ".db";
        String statement = "SELECT message_time FROM \"" + contactId + "\" WHERE id = " + messageId;
        Connection connection = DriverManager.getConnection(detailedDBLink);
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(statement);
        if (result.next()) {
            String time = result.getString("message_time");
            connection.close();
            return time;
        }
        connection.close();
        return "";
    }
}
