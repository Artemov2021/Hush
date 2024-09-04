package com.messenger.database;

import com.messenger.Log;

import javax.swing.plaf.nimbus.State;
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


    public static boolean checkUserPresence(String identifier) throws SQLException {
        // works only with a name or an email
        String identifierType = isEmailOrName(identifier);
        String statement = "SELECT " + identifierType + " FROM users WHERE " + identifierType + " = ?";

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,identifier);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return true;
            }
        }
        return false;
    }

    public static void changeName(String oldName,String newName) throws IOException, SQLException {
        String UsersDBLink = "jdbc:sqlite:auth.db";
        try (Connection connection3 = DriverManager.getConnection(UsersDBLink);
             PreparedStatement stmt3 = connection3.prepareStatement("UPDATE users SET name = ? WHERE name = ?")) {
            stmt3.setString(1,newName);
            stmt3.setString(2,oldName);
            stmt3.executeUpdate();
        } catch (SQLException e) {
            Log.writeNewExceptionLog(e);
            throw e;
        }
    }

    public static void changeEmail(String name,String newEmail) throws IOException, SQLException {
        String UsersDBLink = "jdbc:sqlite:auth.db";
        newEmail = newEmail.isEmpty() ? null : newEmail;
        try (Connection connection3 = DriverManager.getConnection(UsersDBLink);
             PreparedStatement stmt3 = connection3.prepareStatement("UPDATE users SET email = ? WHERE name = ?")) {
            stmt3.setString(1,newEmail);
            stmt3.setString(2,name);
            stmt3.executeUpdate();
        } catch (SQLException e) {
            Log.writeNewExceptionLog(e);
            throw e;
        }
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
        return null;
    }

    public static String getNameWithId(int id) throws SQLException {
        String statement = "SELECT name FROM users WHERE id = ?";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setInt(1,id);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("name");
            }
        }
        return null;
    }

    public static int getIdWithName(String name) throws SQLException {
        String statement = "SELECT id FROM users WHERE name = ?";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1,name);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt("id");
            }
        }
        return -1;
    }

    public static String getEmailWithName( String name ) throws SQLException {
        String statement = "SELECT email FROM users WHERE name = ?";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1, name);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("email");
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
        String statement = "UPDATE users SET contacts_amount = ? WHERE name = ?";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setInt(1,contactAmount+1);
            stmt.setString(2,name);
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
        String identifier = getIdentifierType(contact).equals("email") ? UsersDataBase.getNameWithEmail(contact) : contact;
        String statement = "SELECT avatar FROM users WHERE name = ?";
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,identifier);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString(1);
            }
        }
        return null;
    }

    public static void setAvatar(String name,String avatar) throws SQLException {
        String statement = "UPDATE users SET avatar = ? WHERE name = ?";
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,avatar);
            stmt.setString(2,name);
            stmt.executeUpdate();
        }
    }

    private static String getIdentifierType(String identifier) {
        String emailPattern = "^.+@\\S*\\.[a-z]{2,}$";
        Pattern emailPatternCompile = Pattern.compile(emailPattern);
        Matcher emailMatcher = emailPatternCompile.matcher(identifier);

        String namePattern = "^[a-zA-Z][a-zA-Z0-9 ]+$";
        Pattern namePatternCompile = Pattern.compile(namePattern);
        Matcher nameMatcher = namePatternCompile.matcher(identifier);

        if (emailMatcher.find()) {
            return "email";
        } else if (nameMatcher.find()) {
            return "name";
        } else {
            return "-";
        }
    }
}
