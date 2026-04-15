// backend/DBConnection.java
package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database credentials
    // Read connection info from environment variables, fall back to sensible defaults
    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/scheme_verifier_db");
    // Typical local defaults - override with DB_USER and DB_PASSWORD env vars
    private static final String USER = System.getenv().getOrDefault("DB_USER", "YOUR_DB_USERNAME");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "YOUR_DB_PASSWORD");

    /**
     * Establishes and returns a new database connection.
     */
    public static Connection getConnection() {
        try {
            // Ensure the JDBC driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("DB connected: " + (conn != null));
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found. " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database. " + e.getMessage());
        }
        return null;
    }
}
