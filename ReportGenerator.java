import java.sql.*;

public class ReportGenerator {

    public static void showPopularDroneReport(String dbUrl) {
        // From Checkpoint 04 - 4 - m
        String sql = "SELECT Drone.DroneSerialNumber, sum(Customer.Distance) " + 
                     "FROM Customer, Drone, Rentals " +
                     "WHERE Customer.UserID = Rentals.UserID " +
                     "AND Rentals.DroneSerialNumber = Drone.DroneSerialNumber " +
                     "GROUP BY Drone.DroneSerialNumber " +
                     "ORDER BY sum(Customer.Distance) DESC";
        // Set up
        System.out.println("\n--- Popular Drone Report (Total Distance) ---");
        System.out.printf("| %-20s | %-18s |%n", "Drone Serial", "Total Distance");
        System.out.println("|----------------------|--------------------|");
        // Printing
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String serial = rs.getString(1);
                double totalDist = rs.getDouble(2);
                System.out.printf("| %-20s | %-18.0f |%n", serial, totalDist);
            }

        } catch (SQLException e) {
            System.out.println("Error running report: " + e.getMessage());
        }
        System.out.println("|----------------------|--------------------|");
    }
    public static void showFrequentRenterReport(String dbUrl) {
        // From Checkpoint 03
        String sql = "SELECT UserID, Count(RentalNumber) " +
                     "FROM Rentals " +
                     "GROUP BY UserID " +
                     "HAVING Count(RentalNumber) = (" +
                     "    SELECT Max(count) FROM (" +
                     "        SELECT Count(RentalNumber) as count FROM Rentals GROUP BY UserID" +
                     "    ))";
        // Set up
        System.out.println("\n---- Member with Most Rentals ----");
        System.out.printf("| %-15s | %-15s |%n", "User ID", "Items Rented");
        System.out.println("|-----------------|-----------------|");
        // Print
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String userId = rs.getString(1);
                int count = rs.getInt(2);
                System.out.printf("| %-15s | %-15d |%n", userId, count);
            }

        } catch (SQLException e) {
            System.out.println("Error running report: " + e.getMessage());
        }
        System.out.println("|-----------------|-----------------|");
    }
    public static void showEquipmentByTypeAndMaxYear(String dbUrl, String type, int maxYear) {
        // Revised From Checkpoint 03
        String sql = "SELECT Type, Description, Year " +
                     "FROM Equipment " +
                     "WHERE Type = ? AND Year <= ?";

        System.out.println("\n--- Equipment Report (" + type + " after " + maxYear + ") ---");
        System.out.printf("| %-15s | %-30s | %-6s |%n", "Type", "Description", "Year");
        System.out.println("|-----------------|--------------------------------|--------|");

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type);
            pstmt.setInt(2, maxYear);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean equip = false;
                while (rs.next()) {
                    String resType = rs.getString("Type");
                    String desc = rs.getString("Description");
                    int year = rs.getInt("Year");
                    System.out.printf("| %-15s | %-30s | %-6d |%n", resType, desc, year);
                    equip = true;
                }
                
                if (!equip) {
                    System.out.printf("| No %-12s equipment found after %-4d |%n", type, maxYear);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error running report: " + e.getMessage());
        }
        System.out.println("|-----------------|--------------------------------|--------|\n");
    }
}