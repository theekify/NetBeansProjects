package com.hotelapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton responsible for providing a single shared JDBC Connection
 * to the whole application.
 *
 * Why Singleton here (viva point):
 * - We only ever need ONE connection object managed centrally.
 * - Prevents scattering connection-string/credentials across every DAO.
 * - Makes it trivial to swap DB config in one place (e.g. for deployment).
 */
public class DBConnectionManager {

    private static DBConnectionManager instance;
    private Connection connection;

    // ⚠️ Update these to match your local MySQL Workbench setup
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "orypubit";

    private DBConnectionManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize database connection: " + e.getMessage(), e);
        }
    }

    public static synchronized DBConnectionManager getInstance() {
        if (instance == null) {
            instance = new DBConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Reconnect automatically if the connection dropped (e.g. MySQL idle timeout)
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reconnect to database: " + e.getMessage(), e);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
