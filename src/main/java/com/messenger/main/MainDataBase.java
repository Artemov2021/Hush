package com.messenger.main;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainDataBase {
    private String sql;

    public MainDataBase (String sql) {
        this.sql = sql;
    }


}
