import java.sql.*;

public class ReportGenerator {

    public static void showPopularDroneReport(String dbUrl) {
        // From Checkpoint 04 - 4 - m
        String sql = "SELECT Drone.DroneSerialNumber, sum(Customer.Distance) " + 
                     "FROM Customer, Drone, Rentals " +
                     "WHERE Customer.UserID = Rentals.UserID " +
                     "AND Rentals.DroneSerialNumber = Drone.DroneSerialNumber " +
                     "GROUP BY Drone.DroneSerialNumber ;";
                     //+
                    // "OR                int nullable = rs.getInt(\"NULLABLE\");\n" + //
                                               //   "R BY sum(Customer.Distance) DESC";//
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

     public static void rentingCheckouts(String dbURL, String name){
        String sql = "SELECT E.EquipmentSerialNumber, E.Model, R.RentalCheckOuts  " +
                     "FROM Equipment E Join Rentals R On E.EquipmentSerialNumber = R.EquipmentSerialNumber Join Customer C On R.UserID = C.UserID " +
                     "WHERE C.Name = ? ";

        System.out.println("\n--- Renting Checkout Report for " + name + " ---");
        System.out.printf("| %-30s | %-20s | %-20s |%n", "EquipmentSerialNumber", "Model", "RentalCheckOuts");
        System.out.println("|--------------------------------|----------------------|----------------------|");

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean equip = false;
                while (rs.next()) {
                    String EquipmentSerialNumber = rs.getString("EquipmentSerialNumber");
                    String Model = rs.getString("Model"); 
                    String RentalCheckOuts = rs.getString("RentalCheckOuts");
                    System.out.printf("| %-30s | %-20s | %-20s |%n", EquipmentSerialNumber, Model, RentalCheckOuts);
                    equip = true;
                }
                
                if (!equip) {
                    System.out.println("| No popular items found |");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error running report: " + e.getMessage());
        }
        System.out.println("|------------------------------------------------------------------------------|\n");
    }

    public static void popularItem(String dbURL){
        String sql = "SELECT e.equipmentSerialNumber, e.description, e.model, count(*) AS timesRented " +
                    "FROM equipment e JOIN rentals r ON e.equipmentSerialNumber = r.equipmentSerialNumber " +
                    "GROUP BY e.equipmentSerialNumber, e.description, e.model " +
                    "ORDER BY timesRented DESC ";
        System.out.println("\n--- Popular Item Report ---");
        System.out.printf("| %-20s | %-30s | %-20s | %-15s |%n", "Equipment Serial", "Description", "Model", "Times Rented");
        System.out.println("|----------------------|--------------------------------|----------------------|-----------------|");
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean equip = false;
                while (rs.next()) {
                    String serial = rs.getString("equipmentSerialNumber");
                    String description = rs.getString("description");
                    String model = rs.getString("model");
                    int timesRented = rs.getInt("timesRented");
                    System.out.printf("| %-20s | %-30s | %-20s | %-15d |%n", serial, description, model, timesRented);
                    equip = true;
                }
                
                if (!equip) {
                    System.out.println("| No popular items found |");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error running report: " + e.getMessage());
        }
        System.out.println("|------------------------------------------------------------------------------------------------|");
    }

    public static void popularManufacturer(String dbURL){
         String sql = "SELECT distinct m.name " +
                     "FROM manufacturer m join equipment e on m.manufacturerid = e.manufacturerid join rentals r on e.equipmentserialnumber = r.equipmentserialnumber " +
                     "WHERE r.userID IN ( " +
                     "    SELECT userID " +
                     "    FROM rentals " +
                     "    GROUP BY userID " +
                     "    HAVING COUNT(*) > " +
                     "(SELECT avg(total) " +
                     "FROM (SELECT count(*) as total FROM rentals GROUP BY userID) " +
                     "))";

        System.out.println("\n--- Popular Manufacturer Report ---");
        System.out.printf("| %-30s |%n", "Name");
        System.out.println("|--------------------------------|");

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean equip = false;
                while (rs.next()) {
                    String name = rs.getString("name");
                    System.out.printf("| %-30s |%n", name);
                    equip = true;
                }
                
                if (!equip) {
                    System.out.println("| No popular manufacturers found |");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error running report: " + e.getMessage());
        }
        System.out.println("|--------------------------------|\n");
    }
}