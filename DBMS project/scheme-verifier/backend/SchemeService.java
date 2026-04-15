// backend/SchemeService.java
package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SchemeService {

    /**
     * Helper mapping class for Schemes
     */
    public static class Scheme {
        public int id;
        public String name;
        public String description;
        public int minAge;
        public int maxAge;
        public double incomeLimit;
        public String genderReq;
        public String categoryReq;
        public String citizenshipReq;

        public Scheme(int id, String name, String description, int minAge, int maxAge, double incomeLimit,
                String genderReq, String categoryReq, String citizenshipReq) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.incomeLimit = incomeLimit;
            this.genderReq = genderReq;
            this.categoryReq = categoryReq;
            this.citizenshipReq = citizenshipReq;
        }

        // Returns a json representation
        public String toJson() {
            return String.format(
                    "{\"id\":%d, \"name\":\"%s\", \"description\":\"%s\", \"minAge\":%d, \"maxAge\":%d, \"incomeLimit\":%.2f, \"genderReq\":\"%s\", \"categoryReq\":\"%s\", \"citizenshipReq\":\"%s\"}",
                    id, name, description, minAge, maxAge, incomeLimit, genderReq, categoryReq, citizenshipReq);
        }
    }

    /**
     * Retrieves all schemes from the database.
     */
    public List<Scheme> getAllSchemes() {
        List<Scheme> schemes = new ArrayList<>();
        String query = "SELECT * FROM Scheme";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Scheme scheme = new Scheme(
                        rs.getInt("scheme_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("min_age"),
                        rs.getInt("max_age"),
                        rs.getDouble("income_limit"),
                        rs.getString("gender_req"),
                        rs.getString("category_req"),
                        rs.getString("citizenship_req"));
                schemes.add(scheme);
            }

        } catch (SQLException e) {
            System.err.println("Failed to fetch schemes: " + e.getMessage());
        }
        return schemes;
    }

    /**
     * Retrieves details for a specific scheme by ID.
     */
    public Scheme getSchemeById(int schemeId) {
        String query = "SELECT * FROM Scheme WHERE scheme_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, schemeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Scheme(
                        rs.getInt("scheme_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("min_age"),
                        rs.getInt("max_age"),
                        rs.getDouble("income_limit"),
                        rs.getString("gender_req"),
                        rs.getString("category_req"),
                        rs.getString("citizenship_req"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch scheme ID " + schemeId + ": " + e.getMessage());
        }
        return null;
    }
}
