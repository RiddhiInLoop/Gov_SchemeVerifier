// backend/AdminService.java
package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    /**
     * Admin login using the Admin table. Returns true if valid credentials.
     */
    public boolean adminLogin(String username, String password) {
        String query = "SELECT admin_id, password FROM Admin WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                try {
                    return PasswordUtils.verifyPassword(password, storedHash);
                } catch (Exception ex) {
                    System.err.println("Admin password verify failed: " + ex.getMessage());
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("Admin login failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Helper mapping class for Admin View Applications
     */
    public static class AdminAppView {
        public int applicationId;
        public String status;
        public String appliedDate;
        public String rejectionReason;
        public int citizenId;
        public String citizenName;
        public String schemeName;

        public AdminAppView(int applicationId, String status, String appliedDate, String rejectionReason, int citizenId,
                String citizenName, String schemeName) {
            this.applicationId = applicationId;
            this.status = status;
            this.appliedDate = appliedDate;
            this.rejectionReason = rejectionReason;
            this.citizenId = citizenId;
            this.citizenName = citizenName;
            this.schemeName = schemeName;
        }

        public String toJson() {
            return String.format(
                    "{\"applicationId\":%d, \"status\":\"%s\", \"appliedDate\":\"%s\", \"rejectionReason\":\"%s\", \"citizenId\":%d, \"citizenName\":\"%s\", \"schemeName\":\"%s\"}",
                    applicationId, status, appliedDate,
                    rejectionReason == null ? "" : rejectionReason,
                    citizenId, citizenName, schemeName);
        }
    }

    /**
     * Retrieves all applications in the system, joining Citizen and Scheme tables
     * for UI clarity.
     */
    public List<AdminAppView> viewAllApplications() {
        List<AdminAppView> list = new ArrayList<>();
        String query = "SELECT a.application_id, a.status, a.applied_date, a.rejection_reason, " +
                "c.citizen_id, CONCAT(c.first_name, ' ', c.last_name) AS citizen_name, s.name AS scheme_name " +
                "FROM Application a " +
                "JOIN Citizen c ON a.citizen_id = c.citizen_id " +
                "JOIN Scheme s ON a.scheme_id = s.scheme_id " +
                "ORDER BY a.applied_date DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new AdminAppView(
                        rs.getInt("application_id"),
                        rs.getString("status"),
                        rs.getDate("applied_date").toString(),
                        rs.getString("rejection_reason"),
                        rs.getInt("citizen_id"),
                        rs.getString("citizen_name"),
                        rs.getString("scheme_name")));
            }

        } catch (SQLException e) {
            System.err.println("Failed to fetch all applications: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates an application status and records an optional reason.
     */
    public boolean updateApplicationStatus(int applicationId, String status, String reason) {
        String query = "UPDATE Application SET status = ?, rejection_reason = ? WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setString(2, reason); // Can be null
            stmt.setInt(3, applicationId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Failed to update application status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Adds a new scheme to the database.
     */
    public boolean addScheme(String name, String description, int minAge, int maxAge, double incomeLimit,
            String genderReq, String categoryReq, String citizenshipReq) {
        String query = "INSERT INTO Scheme (name, description, min_age, max_age, income_limit, gender_req, category_req, citizenship_req) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, minAge);
            stmt.setInt(4, maxAge);
            stmt.setDouble(5, incomeLimit);
            stmt.setString(6, genderReq);
            stmt.setString(7, categoryReq);
            stmt.setString(8, citizenshipReq);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to add scheme: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing scheme in the database.
     */
    public boolean updateScheme(int schemeId, String name, String description, int minAge, int maxAge,
            double incomeLimit, String genderReq, String categoryReq, String citizenshipReq) {
        String query = "UPDATE Scheme SET name=?, description=?, min_age=?, max_age=?, income_limit=?, gender_req=?, category_req=?, citizenship_req=? WHERE scheme_id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, minAge);
            stmt.setInt(4, maxAge);
            stmt.setDouble(5, incomeLimit);
            stmt.setString(6, genderReq);
            stmt.setString(7, categoryReq);
            stmt.setString(8, citizenshipReq);
            stmt.setInt(9, schemeId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update scheme: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a scheme from the database.
     */
    public boolean deleteScheme(int schemeId) {
        String query = "DELETE FROM Scheme WHERE scheme_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, schemeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete scheme: " + e.getMessage());
            return false;
        }
    }
}
