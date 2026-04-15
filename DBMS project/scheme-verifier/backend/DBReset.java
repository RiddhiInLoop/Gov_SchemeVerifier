package backend;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class DBReset {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            System.out.println("Dropping old tables...");
            stmt.executeUpdate("DROP TABLE IF EXISTS Application");
            stmt.executeUpdate("DROP TABLE IF EXISTS Admin");
            stmt.executeUpdate("DROP TABLE IF EXISTS Scheme");
            stmt.executeUpdate("DROP TABLE IF EXISTS Citizen");

            System.out.println("Reading schema.sql...");
            String schemaSql = new String(Files.readAllBytes(Paths.get("database/schema.sql")));
            String[] queries = schemaSql.split(";");

            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    System.out.println("Executing: " + query.substring(0, Math.min(query.length(), 50)) + "...");
                    stmt.executeUpdate(query);
                }
            }

            // Ensure default admin password is stored hashed
            try {
                String hashedAdmin = PasswordUtils.hashPassword("admin123");
                try (java.sql.PreparedStatement ups = conn.prepareStatement("UPDATE Admin SET password = ? WHERE username = ?")) {
                    ups.setString(1, hashedAdmin);
                    ups.setString(2, "admin@gov.in");
                    int u = ups.executeUpdate();
                    System.out.println("Updated admin password rows: " + u);
                }
            } catch (Exception e) {
                System.err.println("Failed to hash/update admin password: " + e.getMessage());
            }

            System.out.println("Database reset successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
