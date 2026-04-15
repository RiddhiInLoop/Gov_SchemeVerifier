// backend/CitizenService.java
package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CitizenService {

    /**
     * Registers a new citizen into the database.
     */
    public boolean registerCitizen(String firstName, String middleName, String lastName, int age,
            String gender, String category, String citizenship, double income,
            String residenceType, String area, String landmark, String pinCode,
            String email, String password) {
        String query = "INSERT INTO Citizen (first_name, middle_name, last_name, age, gender, category, citizenship, income, residence_type, area, landmark, pin_code, email, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, firstName);
            stmt.setString(2, middleName);
            stmt.setString(3, lastName);
            stmt.setInt(4, age);
            stmt.setString(5, gender);
            stmt.setString(6, category);
            stmt.setString(7, citizenship);
            stmt.setDouble(8, income);
            stmt.setString(9, residenceType);
            stmt.setString(10, area);
            stmt.setString(11, landmark);
            stmt.setString(12, pinCode);
            stmt.setString(13, email);
            try {
                String hashed = PasswordUtils.hashPassword(password);
                stmt.setString(14, hashed);
            } catch (Exception e) {
                System.err.println("Password hashing failed: " + e.getMessage());
                return false;
            }

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticates a citizen and returns their ID. Returns -1 if failed.
     */
    public int loginCitizen(String email, String password) {
        String query = "SELECT citizen_id, password FROM Citizen WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                try {
                    if (PasswordUtils.verifyPassword(password, storedHash)) {
                        return rs.getInt("citizen_id");
                    }
                } catch (Exception ex) {
                    System.err.println("Password verify error: " + ex.getMessage());
                    return -1;
                }
            }

        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves citizen details based on their ID. Returns mock JSON String for
     * simplicity in this project.
     */
    public String getCitizenById(int citizenId) {
        String query = "SELECT * FROM Citizen WHERE citizen_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, citizenId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Formatting as JSON string for frontend consumption
                // Provide both snake_case DB names and camelCase/common keys so frontend can use `id` and `name`.
                int cid = rs.getInt("citizen_id");
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                String fullName = (first != null ? first : "") + (last != null && !last.isEmpty() ? " " + last : "");
                return String.format(
                    "{\"citizen_id\":%d, \"id\":%d, \"firstName\":\"%s\", \"lastName\":\"%s\", \"name\":\"%s\", \"age\":%d, \"gender\":\"%s\", \"category\":\"%s\", \"income\":%.2f, \"residenceType\":\"%s\", \"email\":\"%s\"}",
                    cid,
                    cid,
                    first,
                    last,
                    fullName.trim(),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("category"),
                    rs.getDouble("income"),
                    rs.getString("residence_type"),
                    rs.getString("email"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch citizen: " + e.getMessage());
        }
        return null;
    }
}
