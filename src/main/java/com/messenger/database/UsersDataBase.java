package com.messenger.database;

import com.messenger.Log;

import java.io.IOException;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsersDataBase {

    private static final String url = "jdbc:sqlite:auth.db";

    public static void addUser(String identifier,String password) throws SQLException {
        String identifierType = isEmailOrName(identifier);
        String statement = "INSERT INTO users (name,email,password,contacts_amount) VALUES (?,?,?,0)";

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,identifierType.equals("email") ? "User"+(getLength()+1) : identifier);
            stmt.setString(2,identifierType.equals("email") ? identifier : null);
            stmt.setString(3,password);
            stmt.executeUpdate();
        }
    }


    public static boolean checkUserPresence(String identifier) throws SQLException, IOException {
        // works only with a name or an email
        Log.writeNewActionLog("Checking user presence in db....\n");
        String identifierType = isEmailOrName(identifier);
        Log.writeNewActionLog(String.format("Identifier (%s) was defined as a %s\n",identifier,identifierType));
        String statement = "SELECT " + identifierType + " FROM users WHERE " + identifierType + " = ?";

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,identifier);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                Log.writeNewActionLog("User is in db!\n");
                return true;
            }
        }
        Log.writeNewActionLog("User is not in db!\n");
        return false;
    }

    private static String isEmailOrName(String identifier) {
        String emailPattern = "@\\S*\\.[a-z]{2,}$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher emailMatcher = pattern.matcher(identifier);
        if (emailMatcher.find()) {
            return "email";
        }
        return "name";
    }

    public static boolean checkPhonePresence(String phone) throws SQLException {
        // works only with a name
        String statement = "SELECT phone_number FROM users WHERE phone_number = ?";

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,phone);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkPasswordValidity(String identifier,String password) throws SQLException {
        // works only with a name or an email
        String identifierType = isEmailOrName(identifier);
        String statement = "SELECT " + identifierType + " FROM users WHERE password = ?";

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,password);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return true;
            }
        }
        return false;
    }

    public static String getNameWithEmail( String email ) throws SQLException {
        String statement = "SELECT name FROM users WHERE email = ?";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1,email);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("name");
            }
        }
        return "";
    }

    public static String getNameWithPhoneNumber( String phoneNumber ) throws SQLException {
        String statement = "SELECT name FROM users WHERE  phone_number = ?";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1, phoneNumber);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("name");
            }
        }
        return "";
    }

    public static String getEmailWithName( String name ) throws SQLException {
        String statement = "SELECT name FROM users WHERE email = ?";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1, name);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("name");
            }
        }
        return "";
    }

    public static int getContactsAmount(String identifier) throws SQLException {
        String identifierType = isEmailOrName(identifier);
        String statement = "SELECT contacts_amount FROM users WHERE " + identifierType + " = ?";

        try (var conn = DriverManager.getConnection(url);
             var stmt = conn.prepareStatement(statement)) {

            stmt.setString(1, identifier);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt("contacts_amount");
            }
        }
        return 0;
    }

    public static void addContactsAmount(String name) throws SQLException {
        int contactAmount = getContactsAmount(name);
        String statement = "INSERT INTO users (contact) VALUES (?) WHERE name = ";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setInt(1,contactAmount+1);
            stmt.executeUpdate();
        }
    }

    public static int getLength() throws SQLException {
        String statement = "SELECT COUNT(*) FROM users";
        try (Connection connection = DriverManager.getConnection(url)) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(statement);
            if (result.next()) {
                return result.getInt(1);
            }
        }
        return 0;
    }

    public static String getAvatar(String contact) throws SQLException {
        String statement = "SELECT avatar FROM users WHERE name = ?";
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,contact);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString(1);
            }
        }
        return "";
    }
}
