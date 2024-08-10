package com.messenger.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DetailedDataBase {
    private final static String sqlPath = "jdbc:sqlite:details/";

    public static void createUserDataBase (String name) throws SQLException {
        String detailedDBLink = sqlPath + name+ ".db";
        String statement = "CREATE TABLE IF NOT EXISTS contact_list (name text)";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        Statement stmt = connection.createStatement();
        stmt.execute(statement);
        connection.close();
    }

    public static void addContact(String mainUser,String info) throws SQLException {
        System.out.println(mainUser + " has new contact: " + info +  "(length "+info.length() + ")");

        String contactName = isPhoneNumber(info) ? UsersDataBase.getNameWithPhoneNumber(info) : info;
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement1 = "INSERT INTO contact_list (name) VALUES (?)";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        PreparedStatement stmt1 = connection.prepareStatement(statement1);
        stmt1.setString(1,contactName);
        stmt1.executeUpdate();

        String statement2 = "CREATE TABLE IF NOT EXISTS " + contactName + " (id integer PRIMARY KEY,message text,message_time text,photo text)";
        Statement stmt2 = connection.createStatement();
        stmt2.execute(statement2);
        connection.close();

        String UsersDBLink = "jdbc:sqlite:auth.db";
        String statement3 = "UPDATE users SET contacts_amount = " + ( UsersDataBase.getContactsAmount(mainUser) + 1 ) + " WHERE name = ?";
        Connection connection1 = DriverManager.getConnection(UsersDBLink);
        PreparedStatement stmt3 = connection1.prepareStatement(statement3);
        stmt3.setString(1,mainUser);
        stmt3.executeUpdate();
        System.out.println("Updated!");
    }

    public static boolean checkUserPresence(String mainUser,String contactName) throws SQLException {
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement = "SELECT name FROM contact_list WHERE name = ?";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        PreparedStatement stmt = connection.prepareStatement(statement);
        stmt.setString(1,contactName);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            connection.close();
            return true;
        }
        connection.close();
        return false;
    }

    private static boolean isPhoneNumber(String info) {
        String[] symbols = {"1","2","3","4","5","6","7","8","9","0","+","-"," "};
        Set<String> allowedSymbols = new HashSet<>(Arrays.asList(symbols));
        for (char s : info.toCharArray()) {
            if (!allowedSymbols.contains(String.valueOf(s))) {
                return false;
            }
        }
        return true;
    }

    public static String getLastMessage(String mainUser,String contact) throws SQLException {
        String detailedDBLink = sqlPath + mainUser + ".db";
        String statement = "SELECT message FROM " + contact + " WHERE id = (SELECT MAX(id) FROM " + contact + ")";
        Connection connection = DriverManager.getConnection(detailedDBLink);
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(statement);
        if (result.next()) {
            connection.close();
            return result.getString(1);
        }
        connection.close();
        return "";
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
}
