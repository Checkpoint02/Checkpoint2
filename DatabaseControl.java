//import the libraries that we need to run sql

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseControl {
    private static Connection conn = null;

    // attempt to connect to the database. true if connedted, false if not and print
    // error message
    public static boolean connect(String url, String user, String pass) {
        try {
            if (user == null || user.isEmpty())
                conn = DriverManager.getConnection(url);
            else
                conn = DriverManager.getConnection(url, user, pass);
            return true;
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
            conn = null;
            return false;
        }
    }

    // get the column names for a table in the database
    public static String[] getColumnNames(String tableName) {
        if (conn == null)
            return new String[0];

        List<String> columns = new ArrayList<>();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            // The arguments are: Catalog, SchemaPattern, TableNamePattern,
            // ColumnNamePattern
            // We pass the tableName. Note: SQLite sometimes ignores case, but exact match
            // is safer.
            ResultSet rs = meta.getColumns(null, null, tableName, null);

            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME");
                // Optional: Skip "ID" columns if you want to auto-generate them,
                // but for now let's keep everything.
                columns.add(colName);
            }
        } catch (SQLException e) {
            System.out.println("Error getting columns: " + e.getMessage());
        }
        return columns.toArray(new String[0]);
    }

    // get the required columns for a table in the database
    public static Set<String> getRequiredColumns(String tableName) {
        Set<String> required = new HashSet<>();
        if (conn == null)
            return required;

        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, tableName, null);

            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME");
                int nullable = rs.getInt("NULLABLE");

                // If the database says NO NULLS allowed, add to our list
                if (nullable == DatabaseMetaData.columnNoNulls) {
                    required.add(colName);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting constraints: " + e.getMessage());
        }
        return required;
    }

    // Check whether a given value already exists in the primary key column of a
    // table
    public static boolean primaryKeyValueExists(String tableName, String pkColumn, String value) {
        if (conn == null) {
            System.out.println("Database not connected.");
            return false;
        }
        // Use a parameterized query to avoid SQL injection
        String sql = "SELECT " + pkColumn + " FROM " + tableName + " WHERE " + pkColumn + " = ? LIMIT 1";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, value);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking primary key existence: " + e.getMessage());
            return false;
        }
    }

    public static Map<String, String> printRecord(String tableName,
            String id,
            Map<String, String> record,
            String[] fields) {
        if (conn == null)
            return record;

        System.out.println("\nID: " + id);

        // First, try to load the record from database if it's empty
        // if (record.isEmpty()) {
        String pkColumnName = getPrimaryKeyColumn(tableName);
        String sql = "SELECT * FROM " + tableName + " WHERE " + pkColumnName + " = ? LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Populate the record map from the result set
                    for (String field : fields) {
                        String value = rs.getString(field);
                        if (value != null) {
                            record.put(field, value);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading record from database: " + e.getMessage());
        }
        // }

        // Print the record
        for (String field : fields) {
            String value = record.get(field);
            System.out.println("  " + field + ": " + (value == null || value.isEmpty() ? "(not set)" : value));
        }

        return record;
    }

    // Helper to get the primary key column name for a given table
    private static String getPrimaryKeyColumn(String tableName) {
        return switch (tableName) {
            case "Warehouse" -> "WarehouseID";
            case "Drone" -> "DroneSerialNumber";
            case "Equipment" -> "EquipmentSerialNumber";
            case "Customer" -> "UserID";
            case "PurchaseOrders" -> "OrderNumber";
            case "Rating" -> "RatingID";
            default -> "ID";
        };
    }

    // insert into a database. Retruns true if data was inserted into the database,
    // returns false if nothing was inserted, or the database is not connected
    public static boolean insertStuff(String[] fields, String tableName, Map<String, String> record) {
        // makes sure db is connected
        if (conn == null) {
            System.out.println("Database not connected.");
            return false;
        }

        // Build the SQL insert statement so its long enough for all tables
        String values = "(?";
        for (int i = 1; i < fields.length; i++) {
            values = values + ", ?";
        }
        values = values + ")";

        // create the insert statement
        String ins = "INSERT INTO " + tableName + " VALUES" + values;
        boolean anyInserted = false;

        // runs the prepared statement
        try (PreparedStatement pstmt = conn.prepareStatement(ins)) {

            // sets the values for the prepared statement
            for (int i = 0; i < fields.length; i++) {
                pstmt.setString(i + 1, record.get(fields[i]));
            }

            // execute the insert
            int rowsInserted = pstmt.executeUpdate();

            // check if any rows were inserted so user can be notified that it was added to
            // database
            if (rowsInserted > 0) {
                anyInserted = true;
            }

        } catch (SQLException ex) {
            System.out.println("Error with inserting record: " + ex.getMessage());
        }

        return anyInserted;
    }

    // deleting the record from the database
    // uses prepared statements to prevent sql injection (make sure to write this
    // for the analysis)

    public static boolean deletestuff(String tableName, String pkColumn, String id) {
        if (conn == null)
            return false;
        String sql = "DELETE FROM " + tableName + " WHERE " + pkColumn + " = ?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, id);
            int rowsAffected = p.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting record: " + e.getMessage());
            return false;
        }
    }

    public static boolean searchstuff(String tableName, String pkColumn, String id, String[] fields) {
        if (conn == null) {
            System.out.println("Database not connected.");
            return false;
        }

        String sql = "SELECT * FROM " + tableName + " WHERE " + pkColumn + " = ?";

        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, id);

            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    for (String field : fields) {
                        String value = rs.getString(field);
                        // Handle null values gracefully
                        if (value == null)
                            value = "[NULL]";
                        System.out.println(field + ": " + value);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching for record: " + e.getMessage());
            return false;
        }
    }

    public static boolean insertRental(String userId, String dueDate, String equipSerial,
            String droneSerial, String checkoutDate,
            String dailyCost, String fees, String returnDate, String deliveryDate, String pickupDate) {
        if (conn == null) {
            System.out.println("Database not connected.");
            return false;
        }

        String sql = "INSERT INTO Rentals  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";

        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(2, userId);
            //Need to get the next rental number. Also in our data the attributes are accidentally part of the database as values
            //and every time I try to remove it sqlstudio crashes, so I have to get the top 2 and use the second one
            String getRentalNumber = "SELECT RentalNumber FROM Rentals ORDER BY RentalNumber DESC LIMIT 2";
            ResultSet rs = conn.prepareStatement(getRentalNumber).executeQuery();
            rs.next();
            rs.next();
            String rentalNumber = rs.getString(1);
            System.out.println("rental number is: " + rentalNumber);
            int rentalNum = Integer.parseInt(rentalNumber.substring(1)) + 1;
            rentalNumber = "R0" + rentalNum;
            System.out.println("new rental number is: " + rentalNumber);
            p.setString(1, rentalNumber);

            try {
               // p.setDate(2, java.sql.Date.valueOf(dueDate));
                p.setString(3, dueDate);
                p.setString(5, checkoutDate);
                p.setString(9, returnDate);
                p.setString(10, deliveryDate);
                p.setString(11, pickupDate);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid Due Date format. Operation cancelled.");
                return false;
            }

            p.setString(7, equipSerial);
            p.setString(8, droneSerial);
            p.setString(4, dailyCost);
            p.setString(6, fees);

            // Execute and return result
            int rows = p.executeUpdate();
            if (rows > 0) {
                System.out.println("Rental created successfully!");
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error creating rental: " + e.getMessage());
            return false;
        }
    }

    // -------//
    // insert for the delivery things, can someone check if delivery date should be
    // an int instead
    public static boolean insertDelivery(String customerId, String equipmentId, String deliveryDate,
            String deliveryWindow, String droneId) {
        if (conn == null)
            return false;
        String ins = "INSERT INTO deliveries(customer_id,equipment_id,delivery_date,delivery_window,drone_id,status) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement p = conn.prepareStatement(ins)) {
            p.setString(1, customerId);
            p.setString(2, equipmentId);
            p.setString(3, deliveryDate);
            p.setString(4, deliveryWindow);
            p.setString(5, droneId);
            p.setString(6, "Scheduled");
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;

        }
    }

    // inserts for transactional delivery stuff
    public static boolean insertDeliveryTransactional(String customerId, String equipmentId, String deliveryDate,
            String deliveryWindow, String droneId, String status) {
        if (conn == null)
            return false;
        String ins = "INSERT INTO deliveries(customer_id,equipment_id,delivery_date,delivery_window,drone_id,status) VALUES(?,?,?,?,?,?)";
        String delField = "DELETE FROM records WHERE entity = ? AND id = ? AND field = ?";
        String insField = "INSERT INTO records(entity,id,field,value) VALUES(?,?,?,?)";
        boolean oldAuto = true;

        try {
            oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement p = conn.prepareStatement(ins)) {
                p.setString(1, customerId);
                p.setString(2, equipmentId);
                p.setString(3, deliveryDate);
                p.setString(4, deliveryWindow);
                p.setString(5, droneId);
                p.setString(6, status);
                p.executeUpdate();
            }
            try (PreparedStatement d = conn.prepareStatement(delField)) {
                d.setString(1, "Equipment");
                d.setString(2, equipmentId);
                d.setString(3, "Status");
                d.executeUpdate();
            }
            try (PreparedStatement i = conn.prepareStatement(insField)) {
                i.setString(1, "Equipment");
                i.setString(2, equipmentId);
                i.setString(3, "Status");
                i.setString(4, status);
                i.executeUpdate();
            }
            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            return false;

        } finally {
            try {
                conn.setAutoCommit(oldAuto);
            } catch (SQLException ignored) {
            }
        }
    }

    // map id, checking
    public static Map<String, Map<String, String>> loadAllRecords(String entity) {
        Map<String, Map<String, String>> result = new java.util.HashMap<>();

        if (conn == null)
            return result;
        String sel = "SELECT * FROM " + entity;
        try (PreparedStatement s = conn.prepareStatement(sel)) {
            System.out.println("test");
                try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) {

                    String id = rs.getString(1);
                    String field = rs.getString(2);
                    String value = rs.getString(3);
                    result.computeIfAbsent(id, k -> new java.util.HashMap<>()).put(field, value);
                }
            }
        } catch (SQLException ignored) {
        }
        return result;
    }

    public static void updateField(String tableName, String pkLabel, String id, String fieldName, String newValue) {
        if (conn == null)
            return;

        // Construct SQL: UPDATE tableName SET columnName = ? WHERE idColumn = ?
        String sql = "UPDATE " + tableName + " SET " + fieldName + " = ? WHERE " + pkLabel + " = ?";

        try (PreparedStatement p = conn.prepareStatement(sql)) {
            if (newValue == null || newValue.isEmpty()) {
                p.setNull(1, java.sql.Types.VARCHAR); // Sets the database field to NULL
            } else {
                p.setString(1, newValue);
            }
            p.setString(2, id); // The ID to identify which row to update

            int rowsAffected = p.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Database updated successfully.");
            } else {
                System.out.println("Update failed: ID not found in database.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
        }
    }

    public static void printAddress(String RentalNumber, boolean pickup){
        if (conn == null)
            return;
        String sql = "SELECT UserID FROM Rentals WHERE RentalNumber = ?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, RentalNumber);
            try (ResultSet rs = p.executeQuery()) {
                String userID = rs.getString("UserID");
                if(pickup){
                    String sqlAddr = "SELECT Address FROM Warehouse WHERE WarehouseID = (SELECT WarehouseID FROM Customer WHERE UserID = ?)";
                    try(PreparedStatement p2 = conn.prepareStatement(sqlAddr)){
                        p2.setString(1, userID);
                        try(ResultSet rs2 = p2.executeQuery()){
                            String address = rs2.getString("Address");
                            System.out.println("Pickup Address: " + address);
                        }
                    } 
                }
                else{
                    String sqlAddr = "SELECT Address FROM Customer WHERE UserID = ?";
                    try(PreparedStatement p2 = conn.prepareStatement(sqlAddr)){
                        p2.setString(1, userID);
                        try(ResultSet rs2 = p2.executeQuery()){
                            String address = rs2.getString("Address");
                            System.out.println("Delivery Address: " + address);
                        }
                    } 
            }
        } 
        }catch (SQLException e) {
            System.out.println("Error retrieving address: " + e.getMessage());
        }
    }
    

    // public static boolean updateRecordField(String entity, String id, String
    // field, String value) {
    // if (conn == null)
    // return false;

    // String del = "DELETE FROM records WHERE entity = ? AND id = ? AND field = ?";
    // String ins = "INSERT INTO records(entity,id,field,value) VALUES(?,?,?,?)";
    // try (PreparedStatement d = conn.prepareStatement(del)) {
    // d.setString(1, entity);
    // d.setString(2, id);
    // d.setString(3, field);
    // d.executeUpdate();
    // } catch (SQLException ignored) {
    // }
    // try (PreparedStatement i = conn.prepareStatement(ins)) {
    // i.setString(1, entity);
    // i.setString(2, id);
    // i.setString(3, field);
    // i.setString(4, value);
    // i.executeUpdate();
    // return true;

    // } catch (SQLException e) {
    // return false;
    // }
    // }
}
