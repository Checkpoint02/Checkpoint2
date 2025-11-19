//import the libraries that we need to run sql

import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;





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

    //insert into a database
    public static boolean insertStuff(String[] fields, String tableName) {
        System.out.println("it works 1");
        if (conn == null) {
            System.out.println("Nope");
            return false;
        }
        String fieldscon = "";
        String values = "";
        for (int i = 0; i < fields.length; i++) {
            fieldscon = fieldscon + "," + fields[i];
            values = values + ",?";
        }
        String ins = "INSERT INTO " + tableName + "(" + fieldscon + ") VALUES(" + values + ")";
        boolean anyInserted = false;
        String dburl = "jdbc:sqlite:checkpoint_four.db";
        try (/*PreparedStatement i = conn.prepareStatement(ins)*/Connection conn = DriverManager.getConnection(dburl);
             Statement stmt = conn.createStatement()){
                System.out.println("it works 2");
                try(ResultSet rs = stmt.executeQuery(ins)){
                    rs.getString(1);
                    rs.getString(2);
                    rs.getString(3);
                    rs.getString(4);
                    anyInserted = true;
                    //catch exception, print out error message (untested so far, idk if it works)
                    System.out.println("it works 3");
                }catch (SQLException ex) {
                     System.out.println("Error with inserting record: " + ex.getMessage());
                }
                 /*ResultSet rs = stmt.executeQuery(ins) {
            
                for (Map.Entry<String, String> e : record.entrySet()) {
                try {
                    rs.getString(1);
                    rs.getString(2);
                    rs.getString(3);
                    rs.getString(4);
                    anyInserted = true;
                    //catch exception, print out error message (untested so far, idk if it works)
                    System.out.println("it works 3");
                } catch (SQLException ex) {
                     System.out.println("Error with inserting record: " + ex.getMessage());
                }
            }*/
               return anyInserted;
        } catch (SQLException e) {
            return false;
        }

             /*ResultSet rs = stmt.executeQuery(ins) {
            
            for (Map.Entry<String, String> e : record.entrySet()) {
                try {
                    rs.getString(1);
                    rs.getString(2);
                    rs.getString(3);
                    rs.getString(4);
                    anyInserted = true;
                    //catch exception, print out error message (untested so far, idk if it works)
                    System.out.println("it works 3");
                } catch (SQLException ex) {
                     System.out.println("Error with inserting record: " + ex.getMessage());
                }
            }*/
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
