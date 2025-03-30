package com.messenger.database;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsersDataBase {

    private static final String url = "jdbc:mysql://127.0.0.1:3306/messengerdb";
    private static final String user = "root";
    private static final String password = "112233";

    public static String addUser(String identifier,String userPassword) throws SQLException {
        String statement = "INSERT INTO users (name,password,email,avatar_picture) VALUES (?,?,?,?)";

        String userName = isEmailOrName(identifier).equals("name") ? identifier : "User"+(getLength()+1);
        String userEmail = isEmailOrName(identifier).equals("email") ? identifier : null;

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement prepareStatement = connection.prepareStatement(statement);
            prepareStatement.setString(1,userName);
            prepareStatement.setString(2,userPassword);
            prepareStatement.setString(3,userEmail);
            prepareStatement.setString(4,null);
            prepareStatement.executeUpdate();
            return "successfully";
        }
    }
    public static boolean getUserPresence(String identifier) throws SQLException {
        String identifierType = isEmailOrName(identifier);
        String statement = "SELECT " + identifierType + " FROM users WHERE " + identifierType + " = ?";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1,identifier);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return true;
            }
        }
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

    public static boolean getPasswordValidity(String identifier, String givenPassword) throws SQLException {
        // works only with a name or an email
        String identifierType = isEmailOrName(identifier);
        String statement = "SELECT password FROM users WHERE " + identifierType + " = ?";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1, identifier);
            ResultSet result = stmt.executeQuery();
            if (result.next() && result.getString("password").equals(givenPassword)) {
                return true;
            }
        }
        return false;
    }

    public static String getNameWithId(int id) throws SQLException {
        String statement = "SELECT name FROM users WHERE id = ?";
        try (var conn = DriverManager.getConnection(url,user,password)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setInt(1,id);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("name");
            }
        }
        return "";
    }

    public static String getEmailWithId(int id) throws SQLException {
        String statement = "SELECT email FROM users WHERE id = ?";
        try (var conn = DriverManager.getConnection(url,user,password)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setInt(1,id);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("email");
            }
        }
        return "";
    }

    public static int getIdWithName(String name) throws SQLException {
        String statement = "SELECT mainUserId FROM users WHERE name = ?";
        try (var conn = DriverManager.getConnection(url,user,password)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1,name);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt("id");
            }
        }
        return -1;
    }

    public static int getIdWithEmail(String email) throws SQLException {
        String statement = "SELECT mainUserId FROM users WHERE email = ?";
        try (var conn = DriverManager.getConnection(url,user,password)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1,email);
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
        return null;
    }

    public static int getContactsAmount(int id) throws SQLException {
        String statement = "SELECT COUNT(*) contacts_amount FROM contacts WHERE user_id = ?";
        try (var conn = DriverManager.getConnection(url,user,password)) {
            PreparedStatement stmt = conn.prepareStatement(statement);
            stmt.setInt(1,id);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt("contacts_amount");
            }
        }
        return -1;
    }

    public static int getLength() throws SQLException {
        String statement = "SELECT MAX(mainUserId) AS last_id FROM users";
        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(statement);
            if (result.next()) {
                return result.getInt(1);
            }
        }
        return 0;
    }

    public static byte[] getAvatarWithId(int id) throws SQLException {
        String statement = "SELECT avatar_picture FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setInt(1,id);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getBytes("avatar_picture");
            }
        }
        return null;
    }

    public static void deleteAvatar(int id) throws SQLException {
        String statement = "UPDATE users SET avatar_picture = NULL WHERE mainUserId = ?";
        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setInt(1,id);
            stmt.executeUpdate();
        }
    }
    public static void setAvatar(int id,String path) throws SQLException, FileNotFoundException {
        String statement = "UPDATE users SET avatar_picture = ? WHERE mainUserId = ?";
        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement stmt = connection.prepareStatement(statement);
            InputStream inputStream = new FileInputStream(path);
            stmt.setBlob(1,inputStream);
            stmt.setInt(2,id);
            stmt.executeUpdate();
        }
    }
    public static void setName(int id,String name) throws SQLException {
        name = name.isEmpty() ? null : name;
        String statement = "UPDATE users SET name = ? WHERE mainUserId = ?";
        try (Connection connection3 = DriverManager.getConnection(url,user,password)) {
            PreparedStatement stmt3 = connection3.prepareStatement(statement);
            stmt3.setString(1,name);
            stmt3.setInt(2,id);
            stmt3.executeUpdate();
        }
    }
    public static void setEmail(int id,String email) throws SQLException {
        email = email.isEmpty() ? null : email;
        String statement = "UPDATE users SET email = ? WHERE mainUserId = ?";
        try (Connection connection3 = DriverManager.getConnection(url,user,password)) {
            PreparedStatement stmt3 = connection3.prepareStatement(statement);
            stmt3.setString(1,email);
            stmt3.setInt(2,id);
            stmt3.executeUpdate();
        }
    }

}
