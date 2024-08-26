package com.messenger.database;

import com.messenger.Log;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailedDataBase {
    private final static String sqlPath = "jdbc:sqlite:details/";

    public static void createUserDataBase (String name) throws SQLException, IOException {
        String detailedDBLink = sqlPath + name+ ".db";
        String statement = "CREATE TABLE IF NOT EXISTS contact_list (name text)";
        try (Connection connection = DriverManager.getConnection(detailedDBLink)) {
            Statement stmt = connection.createStatement();
            stmt.execute(statement);
        } catch (Exception e) {
            Log.writeNewExceptionLog(e);
            throw e;
        }

    }

    public static void addContact(String mainUser,String info) throws SQLException, IOException, InterruptedException {
        System.out.println(mainUser + " has new contact: " + info +  "(length "+info.length() + ")");

        // Adding a new contact to the contact list of the user
        String contactName = isPhoneNumber(info) ? UsersDataBase.getNameWithPhoneNumber(info) : info;
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement1 = "INSERT INTO contact_list (name) VALUES (?)";
        try (Connection connection1 = DriverManager.getConnection(detailedDBLink)) {
            PreparedStatement stmt1 = connection1.prepareStatement(statement1);
            stmt1.setString(1,contactName);
            stmt1.executeUpdate();
        } catch (Exception e) {
            Log.writeNewExceptionLog(e);
            throw e;
        }
        System.out.println("Connection 1");

        // Creating new contact table in user database
        try (Connection connection2 = DriverManager.getConnection(detailedDBLink)) {
            String statement2 = String.format("CREATE TABLE IF NOT EXISTS %s (id integer PRIMARY KEY,message text,message_time text,photo text)","\""+contactName+"\"");
            Statement stmt2 = connection2.createStatement();
            System.out.println(info);
            stmt2.execute(statement2);
        } catch (Exception e) {
            Log.writeNewExceptionLog(e);
            throw e;
        }
        System.out.println("Connection 2");

        // Changing contacts amount of the user
        String UsersDBLink = "jdbc:sqlite:auth.db";
        int contactsAmount = UsersDataBase.getContactsAmount(mainUser);
        try (Connection connection3 = DriverManager.getConnection(UsersDBLink);
             PreparedStatement stmt3 = connection3.prepareStatement("UPDATE users SET contacts_amount = ? WHERE name = ?")) {
            stmt3.setInt(1, contactsAmount + 1);
            stmt3.setString(2, mainUser);
            Thread.sleep(1000);
            stmt3.executeUpdate();
        } catch (SQLException e) {
            Log.writeNewExceptionLog(e);
            throw e;
        }
    }

    public static boolean checkUserPresence(String mainUser,String contactName) throws SQLException, IOException {
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement = "SELECT name FROM contact_list WHERE name = ?";
        try (Connection connection = DriverManager.getConnection(detailedDBLink)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,contactName);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.writeNewExceptionLog(e);
            throw e;
        }
    }

    private static boolean isPhoneNumber(String info) {
        String phoneNumberPattern = "^[0-9-+ ]+$";
        Pattern pattern = Pattern.compile(phoneNumberPattern);
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public static String getLastMessage(String mainUser,String contact) throws SQLException {
        String lastMessage = "";
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement = "SELECT message FROM \"" + contact + "\" WHERE id = (SELECT MAX(id) FROM \"" + contact + "\")";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(statement);
        if (result.next()) {
            lastMessage = result.getString(1);
            connection.close();
        }
        connection.close();
        return lastMessage;
    }

    public static ArrayList<String> getContacts(String mainUser) throws SQLException {
        ArrayList<String> contacts = new ArrayList<>();
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement = "SELECT * FROM contact_list";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(statement);
        while (result.next()) {
            contacts.add(result.getString("name"));
        }
        connection.close();
        return contacts;
    }

    public static int getLastMessageId(String mainUser,String contact) throws SQLException {
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement = "SELECT id FROM " + contact + " WHERE id = (SELECT MAX(id) FROM " + contact + ")";
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

    public static String getMessageTime(String mainUser,String contact,int messageId) throws SQLException {
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement = "SELECT message_time FROM " + contact + " WHERE id = " + messageId;
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
