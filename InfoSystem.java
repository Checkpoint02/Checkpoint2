
//import hashmap just to temporarily store date before we can create the databases
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class InfoSystem {
    private static Scanner sc = new Scanner(System.in);

    // just to make sure our program runs, and edit this later
    private static Map<String, Map<String, String>> warehouses = new HashMap<>();
    private static Map<String, Map<String, String>> drones = new HashMap<>();
    private static Map<String, Map<String, String>> equipment = new HashMap<>();
    private static Map<String, Map<String, String>> customers = new HashMap<>();
    private static Map<String, Map<String, String>> purchaseOrders = new HashMap<>();
    private static Map<String, Map<String, String>> ratings = new HashMap<>();

    private static Map<String, Set<String>> entityRequirements = new HashMap<>();

    private static String[] WAREHOUSE_FIELDS = {};
    private static String[] DRONE_FIELDS = {};
    private static String[] EQUIPMENT_FIELDS = {};
    private static String[] CUSTOMER_FIELDS = {};
    private static String[] PURCHASE_ORDER_FIELDS = {};
    private static String[] RATING_FIELDS = {};

    public static void main(String[] args) {
        // trying to make an automatic database connection
        String defaultDbUrl = "jdbc:sqlite:checkpoint_four.db";
        String autoUrl = (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty()) ? args[0]
                : defaultDbUrl;
        System.out.println("Attempting automatic DB connect to: " + autoUrl);
        if (DatabaseControl.connect(autoUrl, null, null)) {
            System.out.println("Auto-connected to database.");
            /*
             * warehouses.clear();
             * warehouses.putAll(DatabaseControl.loadAllRecords("Warehouse"));
             * drones.clear(); drones.putAll(DatabaseControl.loadAllRecords("Drone"));
             * equipment.clear();
             * equipment.putAll(DatabaseControl.loadAllRecords("Equipment"));
             * customers.clear();
             * customers.putAll(DatabaseControl.loadAllRecords("Customer"));
             * purchaseOrders.clear();
             * purchaseOrders.putAll(DatabaseControl.loadAllRecords("Purchase Order"));
             * ratings.clear(); ratings.putAll(DatabaseControl.loadAllRecords("Rating"));
             */

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            WAREHOUSE_FIELDS = DatabaseControl.getColumnNames("Warehouse");
            DRONE_FIELDS = DatabaseControl.getColumnNames("Drone");
            EQUIPMENT_FIELDS = DatabaseControl.getColumnNames("Equipment");
            CUSTOMER_FIELDS = DatabaseControl.getColumnNames("Customer");
            PURCHASE_ORDER_FIELDS = DatabaseControl.getColumnNames("PurchaseOrders");
            RATING_FIELDS = DatabaseControl.getColumnNames("Rating");

            System.out.println("Loading column names and constraints...");

            // 1. Load Names (You already have this)
            // ... other fields ...

            // 2. Load Requirements (ADD THIS)
            entityRequirements.put("Warehouse", DatabaseControl.getRequiredColumns("Warehouse"));
            entityRequirements.put("Drone", DatabaseControl.getRequiredColumns("Drone"));
            entityRequirements.put("Equipment", DatabaseControl.getRequiredColumns("Equipment"));
            entityRequirements.put("Customer", DatabaseControl.getRequiredColumns("Customer"));
            entityRequirements.put("PurchaseOrders", DatabaseControl.getRequiredColumns("PurchaseOrders"));
            entityRequirements.put("Rating", DatabaseControl.getRequiredColumns("Rating"));
        } else {
            System.out.println("Auto-connect failed (continuing without DB). Use menu option 12 to connect manually.");
        }

        while (true) {
            System.out.println("\nHello, what would you like to look at today?");
            System.out.println("1. Warehouse Information");
            System.out.println("2. Drone Information");
            System.out.println("3. Equipment Information");
            System.out.println("4. Customer/Community Information");
            System.out.println("5. Purchase Order Information");
            System.out.println("6. Rating and Review Information");
            System.out.println("7. Rent Equipment");
            System.out.println("8. Return Equipment");
            System.out.println("9. Schedule Equipment Delivery");
            System.out.println("10. Schedule Equipment Pickup");
            System.out.println("11. Useful Reports");
            System.out.println("12. Connect to Database");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            int choice = getInt();

            switch (choice) {
                case 1 -> warehouseMenu();
                case 2 -> droneMenu();
                case 3 -> equipmentMenu();
                case 4 -> customerMenu();
                case 5 -> purchaseOrderMenu();
                case 6 -> ratingMenu();
                case 7 -> rentEquipment();
                case 8 -> returnEquipment();
                case 9 -> scheduleDelivery();
                case 10 -> schedulePickup();
                case 11 -> usefulReportsMenu();
                case 12 -> dbConnectMenu();
                case 0 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static int getInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private static String getNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private static void entityMenu(String entityName,
            String idLabel, Map<String, Map<String, String>> records, String[] fields) {
        while (true) {
            System.out.println("\n" + entityName + " Menu:");
            System.out.println("1. Add new record");
            System.out.println("2. Edit existing record");
            System.out.println("3. Delete record");
            System.out.println("4. Search records");
            System.out.println("5. List all records");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choice: ");
            int choice = getInt();
            switch (choice) {
                case 1 -> addRecord(entityName, idLabel, records, fields, entityName);
                case 2 -> editRecord(entityName, idLabel, records, fields, entityName);
                case 3 -> deleteRecord(entityName, idLabel, records, entityName);
                case 4 -> searchRecords(idLabel, entityName, fields);
                case 5 -> listRecords(entityName, records, fields);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // get the primary key column name for a table in the database
    private static String getPkColumnName(String entityName) {
        return switch (entityName) {
            case "Warehouse" -> "WarehouseID";
            case "Drone" -> "DroneSerialNumber";
            case "Equipment" -> "EquipmentSerialNumber";
            case "Customer" -> "UserID";
            case "PurchaseOrders" -> "OrderNumber";
            case "Rating" -> "RatingID";
            default -> "ID";
        };
    }

    // adds a record to the database
    private static void addRecord(String entityName,
            String idLabel,
            Map<String, Map<String, String>> records,
            String[] fields, String tableName) {
        String id = getNonEmptyLine("Enter " + idLabel + ": ");

        /*
         * Check whether the ID already exists. Prefer the in-memory map first, then
         * fall back to the DB.
         */
        if (records.containsKey(id) || DatabaseControl.primaryKeyValueExists(tableName, idLabel, id)) {
            System.out.println("A record with that ID already exists.");
            return;
        }
        Map<String, String> record = new HashMap<>();

        Set<String> requiredFields = entityRequirements.getOrDefault(entityName, new HashSet<>());

        for (String field : fields) {
            // checks if this field is the ID and skips it
            if (field.equals(idLabel)) {
                record.put(field, id);
                continue;
            }
            boolean isRequired = requiredFields.contains(field);
            if (isRequired) {
                // --- STRICT INPUT LOOP (Cannot be empty) ---
                while (true) {
                    System.out.print("Enter " + field + " (REQUIRED): ");
                    String value = sc.nextLine().trim();
                    if (!value.isEmpty()) {
                        record.put(field, value);
                        break; // Valid input, move to next field
                    }
                    System.out.println("Error: " + field + " cannot be empty.");
                }
            } else {
                // --- OPTIONAL INPUT (Can be skipped) ---
                System.out.print("Enter " + field + " (leave blank to skip): ");
                String value = sc.nextLine().trim();
                if (!value.isEmpty()) {
                    record.put(field, value);
                }
            }
        }

        records.put(id, record);
        // runs the insert function from DatabaseControl
        if (DatabaseControl.insertStuff(fields, tableName, record)) {
            System.out.println(entityName + " record created and saved for " + id + ".");

        } else {
            System.out.println(entityName + " record created in MEMORY ONLY for " + id + " (DB save failed).");
        }
    }

    /* TODO new implementation */
    private static void editRecord(String entityName,
            String idLabel,
            Map<String, Map<String, String>> records,
            String[] fields, String tableName) {
        String id = getNonEmptyLine("Enter " + idLabel + " to edit: ");
        Map<String, String> record = records.getOrDefault(id, new HashMap<>());
        if (id == null || !DatabaseControl.primaryKeyValueExists(tableName, idLabel, id)) {
            System.out.println("No record found with that ID: " + id);
            return;
        }
        while (true) {
            System.out.println("\nCurrent details for " + id + ":");

            // above works so far
            record = DatabaseControl.printRecord(tableName, id, record, fields);
            System.out.println("Select a field to update (0 to stop):");
            for (int i = 0; i < fields.length; i++) {
                System.out.println((i + 1) + ". " + fields[i]);
            }
            System.out.print("Choice: ");
            int choice = getInt();
            if (choice == 0) {
                return;
            }
            if (choice < 1 || choice > fields.length) {
                System.out.println("Invalid choice.");
                continue;
            }
            String field = fields[choice - 1];
            System.out.print("Enter new value for " + field + " (leave blank to clear): ");
            String value = sc.nextLine().trim();

            DatabaseControl.updateField(tableName, idLabel, id, field, value);

            if (value.isEmpty()) {
                record.remove(field);
                System.out.println(field + " cleared.");
            } else {
                record.put(field, value);
                System.out.println(field + " updated.");
            }
            // do not persist edits — only additions are stored in DB per requirement
        }
    }

    private static void deleteRecord(String entityName,
            String idLabel,
            Map<String, Map<String, String>> records,
            String tableName) {
        String id = getNonEmptyLine("Enter " + idLabel + " to delete: ");

        // 1. Try to delete from the Database first
        boolean dbSuccess = DatabaseControl.deletestuff(tableName, idLabel, id);

        // 2. If successful (or if you want to force sync), remove from Local Map
        if (dbSuccess) {
            records.remove(id);
            System.out.println(entityName + " record deleted successfully.");
        } else {
            // Optional: Check if it existed in the map anyway (phantom data)
            if (records.containsKey(id)) {
                records.remove(id);
                System.out.println("Record removed from memory (was not found in DB).");
            } else {
                System.out.println("No record found with that ID.");
            }
        }
    }

    private static void searchRecords(String idLabel, String entityName, String[] fields) {
        // 1. Ask for the ID (Just like deleteRecord)
        String id = getNonEmptyLine("Enter " + idLabel + " to find: ");

        // 2. Call the database helper
        boolean found = DatabaseControl.searchstuff(entityName, idLabel, id, fields);

        // 3. Handle the result
        if (!found) {
            System.out.println("No record found with that ID: " + id);
        }
    }
    /* TODO new implementation */
    // private static void searchRecords(String entityName,
    // String idLabel,
    // Map<String, Map<String, String>> records,
    // String tableName) {
    // String id = getNonEmptyLine("Enter " + idLabel + " to delete: ");
    // boolean dbsearch = DatabaseControl.searchstuff(tableName, idLabel, id);

    // if (!dbsearch) {
    // System.out.println("No " + entityName.toLowerCase() + " records.");
    // return;
    // }
    // for (Map.Entry<String, Map<String, String>> entry : records.entrySet()) {
    // DatabaseControl.printRecord(entityName, entry.getKey(), entry.getValue(),
    // fields);
    // }
    // }

    // if (records.isEmpty()) {
    // System.out.println("No " + entityName.toLowerCase() + " records available.");
    // return;
    // }
    // System.out.print("Enter search term (matches ID or field values): ");
    // String term = sc.nextLine().trim();
    // if (term.isEmpty()) {
    // System.out.println("Search term cannot be empty.");
    // return;
    // }
    // boolean found = false;
    // for (Map.Entry<String, Map<String, String>> entry : records.entrySet()) {
    // if (recordMatches(entry.getKey(), entry.getValue(), fields, term)) {
    // DatabaseControl.printRecord(entityName, entry.getKey(), entry.getValue(),
    // fields);
    // found = true;
    // }
    // }
    // if (!found) {
    // System.out.println("No matching " + entityName.toLowerCase() + " records
    // found.");
    // }
    // }

    /* TODO new implementation */
    private static void listRecords(String entityName,
            Map<String, Map<String, String>> records,
            String[] fields) {
        if (records.isEmpty()) {
            System.out.println("No " + entityName.toLowerCase() + " records available.");
            return;
        }
        int numAtt = DatabaseControl.numberAttributes(entityName);

        //this doesn't work? only iterates over values inserted via java program in that instance
        for (Map.Entry<String, Map<String, String>> entry : records.entrySet()) {
            DatabaseControl.printRecord(entityName, entry.getKey(), entry.getValue(), fields);
    } 
    }

    /* TODO new implementation */
    // private static boolean recordMatches(String id,
    // Map<String, String> record,
    // String[] fields,
    // String term) {
    // String lowerTerm = term.toLowerCase();
    // if (id.toLowerCase().contains(lowerTerm)) {
    // return true;
    // }
    // for (String field : fields) {
    // String value = record.get(field);
    // if (value != null && value.toLowerCase().contains(lowerTerm)) {
    // return true;
    // }
    // }
    // return false;
    // }

    // Warehouse Menu
    private static void warehouseMenu() {
        entityMenu("Warehouse", getPkColumnName("Warehouse"), warehouses, WAREHOUSE_FIELDS);
    }

    // Drone Menu
    private static void droneMenu() {
        entityMenu("Drone", getPkColumnName("Drone"), drones, DRONE_FIELDS);
    }

    // Equipment Menu
    private static void equipmentMenu() {
        entityMenu("Equipment", getPkColumnName("Equipment"), equipment, EQUIPMENT_FIELDS);
    }

    // Customer Menu
    private static void customerMenu() {
        entityMenu("Customer", getPkColumnName("Customer"), customers, CUSTOMER_FIELDS);
    }

    // Purchase Order Menu
    private static void purchaseOrderMenu() {
        entityMenu("Purchase Order", getPkColumnName("PurchaseOrders"), purchaseOrders, PURCHASE_ORDER_FIELDS);
    }

    // Rating & Review Menu
    private static void ratingMenu() {
        entityMenu("Rating", getPkColumnName("Rating"), ratings, RATING_FIELDS);
    }

    private static void rentEquipment() {
        System.out.println("\n--- Rent Equipment ---");
        String userId = getNonEmptyLine("Enter User ID (Required): ");
        String equipSerial = getNonEmptyLine("Enter Equipment Serial Number (Required): ");
        String droneSerial = getNonEmptyLine("Enter Drone Serial Number (Required): ");
        String due = getNonEmptyLine("Enter Due Date (YYYY-MM-DD) (Required): ");
        System.out.print("Enter Checkout Date (YYYY-MM-DD) (Press Enter to skip): ");
        String checkout = sc.nextLine().trim();
        System.out.print("Enter Daily Cost (e.g., 15.50) (Press Enter to skip): ");
        String dailyCost = sc.nextLine().trim();
        System.out.print("Enter Fees (e.g., 5.00) (Press Enter to skip): ");
        String fees = sc.nextLine().trim();
        boolean success = DatabaseControl.insertRental(userId, due, equipSerial, droneSerial, checkout, dailyCost,
                fees);

        if (success) {
            System.out.println("\n*** Rental Created Successfully ***");
            System.out.println("Customer ID: " + userId);
            System.out.println("Equipment:   " + equipSerial);
            System.out.println("Drone Deliv: " + droneSerial);
            System.out.println("Due Date:    " + due);

            // Handle optional fields for display
            String displayCheckout = checkout.isEmpty() ? "[Not set]" : checkout;
            String displayCost = dailyCost.isEmpty() ? "[Not set]" : "$" + dailyCost;

            System.out.println("Checkout:    " + displayCheckout);
            System.out.println("Daily Cost:  " + displayCost);
            System.out.println("-----------------------------------");
        } else {
            System.out.println("Rental failed. Please check the inputs and try again.");
        }
    }

    // Under developed, just interface -------------
    private static void returnEquipment() {
        System.out.println("\n(Still under development...)");
        System.out.println("\n--- Return Equipment ---");
        String userId = getNonEmptyLine("Enter User ID (Required): ");
        String equipSerial = getNonEmptyLine("Enter Equipment Serial Number (Required): ");
        String date_return = getNonEmptyLine("Enter Return Date (YYYY-MM-DD) (Required): ");

        System.out.println("\nReturn recorded for customer " + userId
                + " and equipment " + equipSerial + ".");
        System.out.println("Return date: " + date_return);
        System.out.println("Equipment returned.");
    }

    // Under developed, just interface
    private static void scheduleDelivery() {
        System.out.println("\n(Still under development...)");
        System.out.println("\nSchedule Equipment Delivery");
        String customerId = getNonEmptyLine("Enter customer ID: ");
        String equipmentId = getNonEmptyLine("Enter equipment ID: ");
        String deliveryDate = getNonEmptyLine("Enter delivery date (YYYY-MM-DD): ");
        String deliveryWindow = getNonEmptyLine("Enter delivery time window: ");
        String droneId = getNonEmptyLine("Enter assigned drone ID: ");

        System.out.println("\nDelivery scheduled and saved for customer " + customerId + " and equipment "
                + equipmentId + ".");
        System.out.println("Delivery date: " + deliveryDate);
        System.out.println("Delivery window: " + deliveryWindow);
        System.out.println("Assigned drone: " + droneId);

        // boolean ok = DatabaseControl.insertDeliveryTransactional(customerId,
        // equipmentId, deliveryDate, deliveryWindow,
        // droneId, "Scheduled");
        // if (ok) {
        // // update equipment status in memory
        // Map<String, String> eq = equipment.computeIfAbsent(equipmentId, k -> new
        // HashMap<>());
        // eq.put("Status", "Out for delivery");
        // System.out.println("\nDelivery scheduled and saved for customer " +
        // customerId + " and equipment "
        // + equipmentId + ".");
        // System.out.println("Delivery date: " + deliveryDate);
        // System.out.println("Delivery window: " + deliveryWindow);
        // System.out.println("Assigned drone: " + droneId);
        // } else {
        // System.out.println("Failed to schedule delivery (DB not connected or error).
        // Delivery not saved.");
        // }
    }

    // Under developed, just interface
    private static void schedulePickup() {
        System.out.println("\n(Still under development...)");
        System.out.println("\nSchedule Equipment Pickup");
        String customerId = getNonEmptyLine("Enter customer ID: ");
        String equipmentId = getNonEmptyLine("Enter equipment ID: ");
        String pickupDate = getNonEmptyLine("Enter pickup date (YYYY-MM-DD): ");
        String pickupWindow = getNonEmptyLine("Enter pickup time window: ");
        String droneId = getNonEmptyLine("Enter assigned drone ID: ");

        System.out.println("\nPickup scheduled for customer " + customerId
                + " and equipment " + equipmentId + ".");
        System.out.println("Pickup date: " + pickupDate);
        System.out.println("Pickup window: " + pickupWindow);
        System.out.println("Assigned drone: " + droneId);
        System.out.println("Equipment pickup scheduled.");
    }

    // ------------------
    private static void usefulReportsMenu() {
        String dburl = "jdbc:sqlite:checkpoint_four.db";

        System.out.println("\nUseful Reports");
        System.out.println("1. Renting Checkouts Report");
        System.out.println("2. Popular Item Report");
        System.out.println("3. Popular Manufacturer Report");
        System.out.println("4. Popular Drone Report");
        System.out.println("5. Member with Most Rentals Report");
        System.out.println("6. Equipment Year By Type Report");
        System.out.println("0. Back to Main Menu");
        System.out.print("Choice: ");

        int choice = getInt();
        try {
            switch (choice) {
                case 1 -> {
                    // Placeholder for future report
                    System.out.println("Insert name of customer for renting checkout report:");
                    String customerName = sc.nextLine().trim();
                    ReportGenerator.rentingCheckouts(dburl, customerName);
                }
                case 2 -> {
                    ReportGenerator.popularItem(dburl);
                }
                case 3 -> {
                    ReportGenerator.popularManufacturer(dburl);
                }
                case 4 -> {
                    ReportGenerator.showPopularDroneReport(dburl);
                }
                case 5 -> {
                    ReportGenerator.showFrequentRenterReport(dburl);
                }
                case 6 -> {
                    // Choose the Type from the set
                    System.out.println("\nSelect Equipment Type:");
                    System.out.println("1. Mechanical");
                    System.out.println("2. Electrical");
                    System.out.println("3. Hydraulic");
                    System.out.println("4. Pneumatic");
                    System.out.print("Enter choice: ");
                    int typeChoice = getInt();

                    String selectedType = switch (typeChoice) {
                        case 1 -> "Mechanical";
                        case 2 -> "Electrical";
                        case 3 -> "Hydraulic";
                        case 4 -> "Pneumatic";
                        default -> null;
                    };

                    if (selectedType == null) {
                        System.out.println("Invalid selection of type.");
                        break;
                    }
                    System.out.print("Enter maximum year (e.g. 2020): ");
                    int year = getInt();

                    // Step C: Run Report
                    ReportGenerator.showEquipmentByTypeAndMaxYear(dburl, selectedType, year);
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please select a number from the menu.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Minimal DB connect menu
    private static void dbConnectMenu() {
        System.out.println("\nDatabase Connection");
        String url = getNonEmptyLine("Enter JDBC URL (e.g. jdbc:sqlite:checkpoint_four.db): ");
        System.out.print("Enter DB username (leave blank if not required): ");
        String user = sc.nextLine().trim();
        System.out.print("Enter DB password (leave blank if not required): ");
        String pass = sc.nextLine().trim();
        if (DatabaseControl.connect(url, user.isEmpty() ? null : user, pass.isEmpty() ? null : pass)) {
            System.out.println("Connected to database.");
            // load existing records into memory
            warehouses.clear();
            warehouses.putAll(DatabaseControl.loadAllRecords("Warehouse"));
            drones.clear();
            drones.putAll(DatabaseControl.loadAllRecords("Drone"));
            equipment.clear();
            equipment.putAll(DatabaseControl.loadAllRecords("Equipment"));
            customers.clear();
            customers.putAll(DatabaseControl.loadAllRecords("Customer"));
            purchaseOrders.clear();
            purchaseOrders.putAll(DatabaseControl.loadAllRecords("PurchaseOrders"));
            ratings.clear();
            ratings.putAll(DatabaseControl.loadAllRecords("Rating"));
        } else {
            System.out.println(
                    "Failed to connect to database. Make sure JDBC driver is on classpath and URL is correct.");
        }
    }
}
