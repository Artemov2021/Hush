package com.messenger.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataBaseConnectionPool {
    private static final String url = "jdbc:mysql://mysql-hush-timurt005-6121.g.aivencloud.com:28163/hush?useSSL=true&requireSSL=true&verifyServerCertificate=false";
    private static final String user = "avnadmin";
    private static final String password = "AVNS_vqwfSDAjXWc9ViFtnRN";

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);           // Number of concurrent DB connections
        config.setConnectionTimeout(5000);       // 5 seconds timeout to get a connection
        config.setIdleTimeout(6000000);           // 10 min idle timeout
        config.setMaxLifetime(1800000);          // 30 min max lifetime for a connection
        config.addDataSourceProperty("cachePrepStmts", "true");  // Optional MySQL optimization
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
