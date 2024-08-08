package com.messenger.database;

import java.sql.*;

public class UsersDataBase {

    private static final String url = "jdbc:sqlite:auth.db";

    public static void addUser(String identifier,String password) throws SQLException {
        String identifierType = identifier.contains("@gmail.com") ? "email" : "name";
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
        String identifierType = identifier.contains("@gmail.com") ? "email" : "name";
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

    public static boolean checkPasswordValidity(String identifier,String password) throws SQLException {
        // works only with a name or an email
        String identifierType = identifier.contains("@gmail.com") ? "email" : "name";
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
        String identifierType = identifier.contains("@gmail.com") ? "email" : "name";
        String statement = "SELECT contacts_amount FROM users WHERE " + identifierType + " = ?";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
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
}
