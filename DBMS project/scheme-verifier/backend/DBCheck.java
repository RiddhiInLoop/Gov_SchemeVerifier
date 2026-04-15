package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBCheck {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connection successful.");
            String query = "SELECT * FROM Admin";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.println("Admin found: " + rs.getString("username"));
                    count++;
                }
                System.out.println("Total admins: " + count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
