// backend/ApplicationService.java
package backend;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ApplicationService {

    /**
     * Helper mapping class for Applications
     */
    public static class AppRecord {
        public int applicationId;
        public String status;
        public String appliedDate;
        public String rejectionReason;
        public int citizenId;
        public int schemeId;
        public String schemeName; // joined property for UI convenience

        public AppRecord(int applicationId, String status, String appliedDate, String rejectionReason, int citizenId,
                int schemeId, String schemeName) {
            this.applicationId = applicationId;
            this.status = status;
            this.appliedDate = appliedDate;
            this.rejectionReason = rejectionReason;
            this.citizenId = citizenId;
            this.schemeId = schemeId;
            this.schemeName = schemeName;
        }

        public String toJson() {
            return String.format(
                    "{\"applicationId\":%d, \"status\":\"%s\", \"appliedDate\":\"%s\", \"rejectionReason\":\"%s\", \"citizenId\":%d, \"schemeId\":%d, \"schemeName\":\"%s\"}",
                    applicationId, status, appliedDate,
                    rejectionReason == null ? "" : rejectionReason,
                    citizenId, schemeId, schemeName);
        }
    }

    /**
     * Submits a scheme application for a citizen and returns true if successful.
     */
    public boolean applyForScheme(int citizenId, int schemeId) {

        // Prevent duplicate applications for the same scheme
        String checkQuery = "SELECT COUNT(*) FROM Application WHERE citizen_id = ? AND scheme_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, citizenId);
            checkStmt.setInt(2, schemeId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Citizen already applied for this scheme.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("DB Error checking duplicates: " + e.getMessage());
            return false;
        }

        String insertQuery = "INSERT INTO Application (status, applied_date, citizen_id, scheme_id) VALUES ('Pending', ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now())); // Current date
            stmt.setInt(2, citizenId);
            stmt.setInt(3, schemeId);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("Failed to apply for scheme: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all applications submitted by a specific citizen.
     */
    public List<AppRecord> getCitizenApplications(int citizenId) {
        List<AppRecord> apps = new ArrayList<>();
        // Join with Scheme table to get the name for UI
        String query = "SELECT a.*, s.name as scheme_name FROM Application a " +
                "JOIN Scheme s ON a.scheme_id = s.scheme_id " +
                "WHERE a.citizen_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, citizenId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                apps.add(new AppRecord(
                        rs.getInt("application_id"),
                        rs.getString("status"),
                        rs.getDate("applied_date").toString(),
                        rs.getString("rejection_reason"),
                        rs.getInt("citizen_id"),
                        rs.getInt("scheme_id"),
                        rs.getString("scheme_name")));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch applications: " + e.getMessage());
        }
        return apps;
    }
}
