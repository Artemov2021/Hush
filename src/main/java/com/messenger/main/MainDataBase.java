package com.messenger.main;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.concurrent.ExecutionException;

public class MainDataBase {
    private String sql;

    public MainDataBase (String sql) {
        this.sql = sql;
    }

    public String getNameWithEmail( String email ) {
        String statement = "SELECT name FROM users WHERE email = ?";
        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1,email);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("name");
            }
        } catch (Exception e ) {
            System.err.println(e.getMessage());
        }
        return "";
    }

    public String getEmailWithName( String name ) {
        String statement = "SELECT name FROM users WHERE email = ?";
        try (var conn = DriverManager.getConnection(sql)) {
            var stmt = conn.prepareStatement(statement);
            stmt.setString(1,name);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("name");
            }
        } catch (Exception e ) {
            System.err.println(e.getMessage());
        }
        return "";
    }
}
