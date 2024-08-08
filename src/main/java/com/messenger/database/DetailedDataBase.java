package com.messenger.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DetailedDataBase {
    private final static String sqlPath = "jdbc:sqlite:details/";

    public static void createUserDataBase (String name) throws SQLException {
        String newDataBaseLink = sqlPath + name+ ".db";
        String statement = "CREATE TABLE IF NOT EXISTS contact_list (name text)";
        try (Connection connection = DriverManager.getConnection(newDataBaseLink)) {
            Statement stmt = connection.createStatement();
            stmt.execute(statement);
        }
    }
}
