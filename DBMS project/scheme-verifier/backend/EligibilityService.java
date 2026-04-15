// backend/EligibilityService.java
package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EligibilityService {

    /**
     * Checks if a citizen is eligible for a specific scheme based on age and income
     * limits.
     * Logic:
     * IF citizen.age >= scheme.min_age
     * AND citizen.age <= scheme.max_age
     * AND citizen.income <= scheme.income_limit
     * THEN return Eligible ELSE return Not Eligible
     */
    public String checkEligibility(int citizenId, int schemeId) {

        // Fetch citizen profile
        int citizenAge = 0;
        double citizenIncome = 0.0;
        String citizenGender = "";
        String citizenCategory = "";
        String citizenCitizenship = "";
        boolean citizenExists = false;

        String citizenQuery = "SELECT age, income, gender, category, citizenship FROM Citizen WHERE citizen_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(citizenQuery)) {
            stmt.setInt(1, citizenId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                citizenAge = rs.getInt("age");
                citizenIncome = rs.getDouble("income");
                citizenGender = rs.getString("gender");
                citizenCategory = rs.getString("category");
                citizenCitizenship = rs.getString("citizenship");
                citizenExists = true;
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching citizen: " + e.getMessage());
            return "Database Error";
        }

        if (!citizenExists)
            return "Citizen profile not found in database.";

        // Fetch scheme requirements
        int minAge = 0;
        int maxAge = 150; // default large upper bound
        double incomeLimit = Double.MAX_VALUE;
        String reqGender = "All";
        String reqCategory = "All";
        String reqCitizenship = "Indian";
        boolean schemeExists = false;

        String schemeQuery = "SELECT min_age, max_age, income_limit, gender_req, category_req, citizenship_req FROM Scheme WHERE scheme_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(schemeQuery)) {
            stmt.setInt(1, schemeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                minAge = rs.getInt("min_age");
                int dbMaxAge = rs.getInt("max_age");
                if (!rs.wasNull())
                    maxAge = dbMaxAge;

                double dbIncomeLimit = rs.getDouble("income_limit");
                if (!rs.wasNull())
                    incomeLimit = dbIncomeLimit;

                reqGender = rs.getString("gender_req");
                reqCategory = rs.getString("category_req");
                reqCitizenship = rs.getString("citizenship_req");

                schemeExists = true;
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching scheme: " + e.getMessage());
            return "Database Error";
        }

        if (!schemeExists)
            return "Scheme not found.";

        // Evaluate logic
        if (citizenAge < minAge || citizenAge > maxAge) {
            return "Age requirement not met. (Requires " + minAge + "-" + maxAge + " years)";
        }
        if (citizenIncome > incomeLimit) {
            return "Income exceeds the scheme limit of ₹" + incomeLimit;
        }
        if (!reqGender.equalsIgnoreCase("All") && !reqGender.equalsIgnoreCase(citizenGender)) {
            return "Scheme is restricted to " + reqGender + " gender only.";
        }
        if (!reqCategory.equalsIgnoreCase("All") && !reqCategory.equalsIgnoreCase(citizenCategory)) {
            return "Scheme is restricted to " + reqCategory + " category citizens.";
        }
        if (reqCitizenship != null && !reqCitizenship.equalsIgnoreCase("All")
                && !reqCitizenship.equalsIgnoreCase(citizenCitizenship)) {
            return "Citizenship requirement not met.";
        }

        return "Eligible";
    }
}
