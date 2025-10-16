//import hashmap just to temporarily store date before we can create the databases
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class InfoSystem {
    private static Scanner sc = new Scanner(System.in);

    // just to make sure our program runs,  and edit this later
    private static Map<String, Map<String, String>> warehouses = new HashMap<>();
    private static Map<String, Map<String, String>> drones = new HashMap<>();
    private static Map<String, Map<String, String>> equipment = new HashMap<>();
    private static Map<String, Map<String, String>> customers = new HashMap<>();
    private static Map<String, Map<String, String>> purchaseOrders = new HashMap<>();
    private static Map<String, Map<String, String>> ratings = new HashMap<>();

    private static final String[] WAREHOUSE_FIELDS = {
            "Storage", "Phone number", "Drone capacity", "Manager name", "Address", "City"
    };
    private static final String[] DRONE_FIELDS = {
            "Max speed", "Manufacturer", "Location", "Active status", "Distance autonomy",
            "Name", "Warranty expiration", "Model", "Year", "Status", "Employee responsible"
    };
    private static final String[] EQUIPMENT_FIELDS = {
            "Description", "Model", "Year", "Status", "Location"
    };
    private static final String[] CUSTOMER_FIELDS = {
            "Name", "Address", "Warehouse distance", "Email", "Start date", "Active moving status"
    };
    private static final String[] PURCHASE_ORDER_FIELDS = {
            "Element type", "Actual arrival date", "Quantity", "Estimated arrival date", "Value"
    };
    private static final String[] RATING_FIELDS = {
            "Review", "Rating"
    };

    public static void main(String[] args) {
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
                                   String idLabel,
                                   Map<String, Map<String, String>> records,
                                   String[] fields) {
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
                case 1 -> addRecord(entityName, idLabel, records, fields);
                case 2 -> editRecord(entityName, idLabel, records, fields);
                case 3 -> deleteRecord(entityName, idLabel, records);
                case 4 -> searchRecords(entityName, records, fields);
                case 5 -> listRecords(entityName, records, fields);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addRecord(String entityName,
                                  String idLabel,
                                  Map<String, Map<String, String>> records,
                                  String[] fields) {
        String id = getNonEmptyLine("Enter " + idLabel + ": ");
        if (records.containsKey(id)) {
            System.out.println("A record with that ID already exists.");
            return;
        }
        Map<String, String> record = new HashMap<>();
        for (String field : fields) {
            System.out.print("Enter " + field + " (leave blank to skip): ");
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) {
                record.put(field, value);
            }
        }
        records.put(id, record);
        System.out.println(entityName + " record created for " + id + ".");
    }

    private static void editRecord(String entityName,
                                   String idLabel,
                                   Map<String, Map<String, String>> records,
                                   String[] fields) {
        String id = getNonEmptyLine("Enter " + idLabel + " to edit: ");
        Map<String, String> record = records.get(id);
        if (record == null) {
            System.out.println("No record found with that ID.");
            return;
        }
        while (true) {
            System.out.println("\nCurrent details for " + id + ":");
            printRecord(id, record, fields);
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
            if (value.isEmpty()) {
                record.remove(field);
                System.out.println(field + " cleared.");
            } else {
                record.put(field, value);
                System.out.println(field + " updated.");
            }
        }
    }

    private static void deleteRecord(String entityName,
                                     String idLabel,
                                     Map<String, Map<String, String>> records) {
        String id = getNonEmptyLine("Enter " + idLabel + " to delete: ");
        if (records.remove(id) != null) {
            System.out.println(entityName + " record deleted for " + id + ".");
        } else {
            System.out.println("No record found with that ID.");
        }
    }

    private static void searchRecords(String entityName,
                                      Map<String, Map<String, String>> records,
                                      String[] fields) {
        if (records.isEmpty()) {
            System.out.println("No " + entityName.toLowerCase() + " records available.");
            return;
        }
        System.out.print("Enter search term (matches ID or field values): ");
        String term = sc.nextLine().trim();
        if (term.isEmpty()) {
            System.out.println("Search term cannot be empty.");
            return;
        }
        boolean found = false;
        for (Map.Entry<String, Map<String, String>> entry : records.entrySet()) {
            if (recordMatches(entry.getKey(), entry.getValue(), fields, term)) {
                printRecord(entry.getKey(), entry.getValue(), fields);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No matching " + entityName.toLowerCase() + " records found.");
        }
    }

    private static void listRecords(String entityName,
                                    Map<String, Map<String, String>> records,
                                    String[] fields) {
        if (records.isEmpty()) {
            System.out.println("No " + entityName.toLowerCase() + " records available.");
            return;
        }
        for (Map.Entry<String, Map<String, String>> entry : records.entrySet()) {
            printRecord(entry.getKey(), entry.getValue(), fields);
        }
    }

    private static void printRecord(String id,
                                    Map<String, String> record,
                                    String[] fields) {
        System.out.println("\nID: " + id);
        for (String field : fields) {
            String value = record.get(field);
            System.out.println("  " + field + ": " + (value == null || value.isEmpty() ? "(not set)" : value));
        }
    }

    private static boolean recordMatches(String id,
                                         Map<String, String> record,
                                         String[] fields,
                                         String term) {
        String lowerTerm = term.toLowerCase();
        if (id.toLowerCase().contains(lowerTerm)) {
            return true;
        }
        for (String field : fields) {
            String value = record.get(field);
            if (value != null && value.toLowerCase().contains(lowerTerm)) {
                return true;
            }
        }
        return false;
    }



    // Warehouse Menu
    private static void warehouseMenu() {
        entityMenu("Warehouse", "Warehouse ID", warehouses, WAREHOUSE_FIELDS);
    }

    // Drone Menu
    private static void droneMenu() {
        entityMenu("Drone", "Drone serial number", drones, DRONE_FIELDS);
    }

    // Equipment Menu
    private static void equipmentMenu() {
        entityMenu("Equipment", "Equipment serial number", equipment, EQUIPMENT_FIELDS);
    }

    // Customer Menu
    private static void customerMenu() {
        entityMenu("Customer", "Customer phone number", customers, CUSTOMER_FIELDS);
    }

    // Purchase Order Menu
    private static void purchaseOrderMenu() {
        entityMenu("Purchase Order", "Purchase order number", purchaseOrders, PURCHASE_ORDER_FIELDS);
    }

    // Rating & Review Menu
    private static void ratingMenu() {
        entityMenu("Rating", "Rating ID", ratings, RATING_FIELDS);
    }

    private static void rentEquipment() {
        System.out.println("\nRent Equipment");
        String customerId = getNonEmptyLine("Enter customer ID: ");
        String equipmentId = getNonEmptyLine("Enter equipment ID: ");
        String rentalStart = getNonEmptyLine("Enter rental start date (YYYY-MM-DD): ");
        String expectedReturn = getNonEmptyLine("Enter expected return date (YYYY-MM-DD): ");
        String pickupMethod = getNonEmptyLine("Enter pickup method (in-person/drone delivery): ");

        System.out.println("\nRental created for customer " + customerId
                + " and equipment " + equipmentId + ".");
        System.out.println("Start date: " + rentalStart);
        System.out.println("Expected return: " + expectedReturn);
        System.out.println("Pickup method: " + pickupMethod);
        System.out.println("Equipment rented.");
    }

    private static void returnEquipment() {
        System.out.println("\nReturn Equipment");
        String customerId = getNonEmptyLine("Enter customer ID: ");
        String equipmentId = getNonEmptyLine("Enter equipment ID: ");
        String returnDate = getNonEmptyLine("Enter return date (YYYY-MM-DD): ");
        String condition = getNonEmptyLine("Enter equipment condition on return: ");

        System.out.println("\nReturn recorded for customer " + customerId
                + " and equipment " + equipmentId + ".");
        System.out.println("Return date: " + returnDate);
        System.out.println("Condition: " + condition);
        System.out.println("Equipment returned.");
    }

    private static void scheduleDelivery() {
        System.out.println("\nSchedule Equipment Delivery");
        String customerId = getNonEmptyLine("Enter customer ID: ");
        String equipmentId = getNonEmptyLine("Enter equipment ID: ");
        String deliveryDate = getNonEmptyLine("Enter delivery date (YYYY-MM-DD): ");
        String deliveryWindow = getNonEmptyLine("Enter delivery time window: ");
        String droneId = getNonEmptyLine("Enter assigned drone ID: ");

        System.out.println("\nDelivery scheduled for customer " + customerId
                + " and equipment " + equipmentId + ".");
        System.out.println("Delivery date: " + deliveryDate);
        System.out.println("Delivery window: " + deliveryWindow);
        System.out.println("Assigned drone: " + droneId);
        System.out.println("Equipment delivery scheduled.");
    }

    private static void schedulePickup() {
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
}
