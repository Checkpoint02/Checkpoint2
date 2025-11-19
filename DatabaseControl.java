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
    private static Connection conn =  null;
//attempt to connect to the database. true if connedted, false if not and print error message
    public static boolean connect(String url, String user, String pass) {
        try {
            if (user == null || user.isEmpty()) conn = DriverManager.getConnection(url);
            else conn = DriverManager.getConnection(url, user, pass);
            return true;
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: "  + e.getMessage());
            conn = null;
            return false;
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public static String[] getColumnNames(String tableName) {
        if (conn == null) return new String[0];
        
        List<String> columns = new ArrayList<>();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            // The arguments are: Catalog, SchemaPattern, TableNamePattern, ColumnNamePattern
            // We pass the tableName. Note: SQLite sometimes ignores case, but exact match is safer.
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
    public static Set<String> getRequiredColumns(String tableName) {
        Set<String> required = new HashSet<>();
        if (conn == null) return required;

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
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   
    //insert into a database
    public static boolean insertRecord(String tableName, String pkColumnName, String pkValue, Map<String, String> data) {
        if (conn == null) {
            System.out.println("Database not connected.");
            return false;
        }

        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");

        // Add the Primary Key (ID) column first
        columns.add(pkColumnName);
        placeholders.add("?");

        // Add the rest of the fields from the data map
        for (String key : data.keySet()) {
            columns.add(key);
            placeholders.add("?");
        }
        
        return anyInserted;

             
    }

    // deleting the record from the database
//uses prepared statements to prevent sql injection (make sure to write this for the analysis)
    public static boolean deleteRecord(String entity, String id) {
        if (conn == null) return false;
        String del = "DELETE FROM records WHERE entity = ? AND id = ?";
        try (PreparedStatement d = conn.prepareStatement(del)) {  d.setString(1, entity);
            d.setString(2, id);
            d.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    //insert for the delivery things, can someone check if delivery date should be an int instead
    public static boolean insertDelivery(String customerId, String equipmentId, String deliveryDate, String deliveryWindow, String droneId) {
        if (conn == null) return false;
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
    public static boolean insertDeliveryTransactional(String customerId, String equipmentId, String deliveryDate, String deliveryWindow, String droneId, String status) {
        if (conn == null) return false;
        String ins = "INSERT INTO deliveries(customer_id,equipment_id,delivery_date,delivery_window,drone_id,status) VALUES(?,?,?,?,?,?)";
        String delField = "DELETE FROM records WHERE entity = ? AND id = ? AND field = ?";
        String  insField = "INSERT INTO records(entity,id,field,value) VALUES(?,?,?,?)"; 
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
            try (PreparedStatement i = conn.prepareStatement(insField)) {  i.setString(1,  "Equipment");
                i.setString(2, equipmentId);
                i.setString(3, "Status");
                i.setString(4, status);
                i.executeUpdate();
            }
            conn.commit();
            return true;

 
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            return false;


        } finally {
            try { conn.setAutoCommit(oldAuto); } catch (SQLException ignored) {}
        }
    }

    // map id, checking 
    public static Map<String, Map<String, String>> loadAllRecords(String entity) {
        Map<String, Map<String, String>> result = new java.util.HashMap<>();
     
     
        if (conn == null) return result;
        String sel = "SELECT id, field, value FROM records WHERE entity = ? ORDER BY id";
        try (PreparedStatement s = conn.prepareStatement(sel)) {
            s.setString(1, entity);
            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                  
                    String id = rs.getString(1);
                    String field = rs.getString(2);
                    String value = rs.getString(3);
                    result.computeIfAbsent(id, k -> new java.util.HashMap<>()).put(field, value);
                }
            }
        } catch (SQLException ignored) {}
        return result;
    }

    public static boolean updateRecordField(String entity, String id, String field, String value) {
        if (conn == null) return false;

        String del = "DELETE FROM records WHERE entity = ? AND id = ? AND field = ?";
        String ins = "INSERT INTO records(entity,id,field,value) VALUES(?,?,?,?)";
        try (PreparedStatement d = conn.prepareStatement(del)) {
            d.setString(1, entity);
            d.setString(2,  id); 
            d.setString(3, field);
            d.executeUpdate(); 
        } catch (SQLException ignored) {}
        try (PreparedStatement i = conn.prepareStatement(ins)) {
            i.setString(1, entity);
            i.setString(2, id);
            i.setString(3, field);
            i.setString(4,  value);
            i.executeUpdate();
            return true;

        
        } catch (SQLException e) {
            return false;
        }
    }
}
