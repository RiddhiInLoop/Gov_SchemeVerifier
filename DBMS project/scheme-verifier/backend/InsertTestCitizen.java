package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class InsertTestCitizen {
    public static void main(String[] args) {
        String email = args.length > 0 ? args[0] : "test@local";
        String password = args.length > 1 ? args[1] : "test123";

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO Citizen (first_name, middle_name, last_name, age, gender, category, citizenship, income, residence_type, area, landmark, pin_code, email, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "Test");
                ps.setString(2, "");
                ps.setString(3, "User");
                ps.setInt(4, 30);
                ps.setString(5, "Male");
                ps.setString(6, "General");
                ps.setString(7, "Indian");
                ps.setDouble(8, 20000.0);
                ps.setString(9, "Owned");
                ps.setString(10, "Test Area");
                ps.setString(11, "Near Test");
                ps.setString(12, "000000");
                ps.setString(13, email);
                String hashed = PasswordUtils.hashPassword(password);
                ps.setString(14, hashed);
                int r = ps.executeUpdate();
                System.out.println("Inserted test citizen: " + email + " rows=" + r);
            }
        } catch (Exception e) {
            System.err.println("Failed to insert test citizen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
