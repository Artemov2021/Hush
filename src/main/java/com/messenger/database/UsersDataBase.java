package com.messenger.database;

import java.sql.*;

public class UsersDataBase {

    private static final String url = "jdbc:sqlite:auth.db";

    public static void addUser(String identifier,String password) throws SQLException {
        
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

    public static boolean checkPasswordValidity(String identifier) throws SQLException {
        // works only with a name or an email
        String identifierType = identifier.contains("@gmail.com") ? "email" : "name";
        String statement = "SELECT password FROM users WHERE " + identifierType + " = ?";

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

    public static int getContactsAmount(String name) throws SQLException {
        
        String statement = "SELECT contacts FROM users WHERE name = ?";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1, name);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt("contacts");
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
}
