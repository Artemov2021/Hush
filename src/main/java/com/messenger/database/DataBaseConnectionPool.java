package com.messenger.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataBaseConnectionPool {
    private static final String url = "jdbc:mysql://mysql-hush-timurt005-6121.g.aivencloud.com:28163/hush" +
            "?useSSL=true&requireSSL=true&verifyServerCertificate=false";
    private static final String user = "avnadmin";
    private static final String password = "AVNS_vqwfSDAjXWc9ViFtnRN";

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        // Connection pool settings
        config.setMaximumPoolSize(10);             // Max concurrent connections
        config.setConnectionTimeout(5000);         // 5 sec timeout for getting a connection
        config.setIdleTimeout(6000000);             // 100 min idle timeout (600000 ms)
        config.setMaxLifetime(18000000);            // 300 min max lifetime (1800000 ms)

        // Health check and reconnection support
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);         // 3 sec validation timeout
        config.setInitializationFailTimeout(-1);   // Don't fail on startup if DB is down

        // MySQL driver performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
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
