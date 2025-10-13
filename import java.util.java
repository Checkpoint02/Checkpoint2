//import hashmap just to temporarily store date before we can create the databases
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class InfoSystem {
    private static Scanner sc = new Scanner(System.in);

    // just to make sure our program runs, delete and edit this later
    private static Map<String, Map<String, String>> warehouses = new HashMap<>();
    private static Map<String, Map<String, String>> drones = new HashMap<>();
    private static Map<String, Map<String, String>> equipment = new HashMap<>();
    private static Map<String, Map<String, String>> customers = new HashMap<>();
    private static Map<String, Map<String, String>> purchaseOrders = new HashMap<>();
    private static Map<String, Map<String, String>> ratings = new HashMap<>();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nHello, what would you like to look at today?");
            System.out.println("1. Warehouse Information");
            System.out.println("2. Drone Information");
            System.out.println("3. Equipment Information");
            System.out.println("4. Customer/Community Information");
            System.out.println("5. Purchase Order Information");
            System.out.println("6. Rating and Review Information");
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

    private static void viewModifyMenu(Map<String, String> record, String key) {
        String value = record.getOrDefault(key, null);
        if (value == null) {
            System.out.println("Here is the information: (no information found)");
            System.out.println("1. Add new information");
            System.out.println("3. Exit to main menu");
        } else {
            System.out.println("Here is the information: " + value);
            System.out.println("1. Add new information");
            System.out.println("2. Modify existing information");
            System.out.println("3. Exit to main menu");
        }
        int opt = getInt();
        switch (opt) {
            case 1 -> {
                System.out.print("Enter new information: ");
                record.put(key, sc.nextLine());
                System.out.println("Information added.");
            }
            case 2 -> {
                if (value != null) {
                    System.out.print("Enter updated information: ");
                    record.put(key, sc.nextLine());
                    System.out.println("Information modified.");
                } else {
                    System.out.println("Nothing to modify.");
                }
            }
            case 3 -> {}
            default -> System.out.println("Invalid option.");
        }
    }



    // Warehouse Menu
    private static void warehouseMenu() {
        System.out.print("Input Warehouse ID: ");
        String id = sc.nextLine();
        warehouses.putIfAbsent(id, new HashMap<>());
        Map<String, String> w = warehouses.get(id);

        while (true) {
            System.out.println("""
                    \nWarehouse Menu:
                    1. Storage
                    2. Phone number
                    3. Drone capacity
                    4. Manager name
                    5. Address
                    6. City
                    0. Exit to Main Menu""");
            System.out.print("Choice: ");
            int c = getInt();
            switch (c) {
                case 1 -> viewModifyMenu(w, "Storage");
                case 2 -> viewModifyMenu(w, "Phone number");
                case 3 -> viewModifyMenu(w, "Drone capacity");
                case 4 -> viewModifyMenu(w, "Manager name");
                case 5 -> viewModifyMenu(w, "Address");
                case 6 -> viewModifyMenu(w, "City");
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // Drone Menu
    private static void droneMenu() {
        System.out.print("Input Drone serial number: ");
        String id = sc.nextLine();
        drones.putIfAbsent(id, new HashMap<>());
        Map<String, String> d = drones.get(id);

        String[] keys = {
                "Max speed", "Manufacturer", "Location", "Active status", "Distance autonomy",
                "Name", "Warranty expiration", "Model", "Year", "Status", "Employee responsible"
        };
        while (true) {
            System.out.println("\nDrone Menu:");
            for (int i = 0; i < keys.length; i++)
                System.out.println((i + 1) + ". " + keys[i]);
            System.out.println("0. Exit to Main Menu");
            System.out.print("Choice: ");
            int c = getInt();
            if (c == 0) return;
            if (c >= 1 && c <= keys.length) viewModifyMenu(d, keys[c - 1]);
            else System.out.println("Invalid choice.");
        }
    }

    // Equipment Menu
    private static void equipmentMenu() {
        System.out.println("View all available equipment or view specific equipment?");
        System.out.println("1. View all available equipment");
        System.out.println("2. View specific equipment");
        int c = getInt();
        if (c == 1) {
            System.out.print("Type the model number: ");
            String model = sc.nextLine();
            System.out.println("Here is the serial number of all available equipment for model " + model + ":");
            equipment.entrySet().stream()
                    .filter(e -> model.equals(e.getValue().get("Model")))
                    .forEach(e -> System.out.println("Serial: " + e.getKey()));
        } else if (c == 2) {
            System.out.print("Input equipment serial number: ");
            String id = sc.nextLine();
            equipment.putIfAbsent(id, new HashMap<>());
            Map<String, String> e = equipment.get(id);
            String[] keys = {"Description", "Model", "Year", "Status", "Location"};
            while (true) {
                System.out.println("\nEquipment Menu:");
                for (int i = 0; i < keys.length; i++)
                    System.out.println((i + 1) + ". " + keys[i]);
                System.out.println("0. Exit to Main Menu");
                int opt = getInt();
                if (opt == 0) return;
                if (opt >= 1 && opt <= keys.length)
                    viewModifyMenu(e, keys[opt - 1]);
                else
                    System.out.println("Invalid choice.");
            }
        }
    }

    // Customer Menu
    private static void customerMenu() {
        System.out.println("Add new customer or view existing customer?");
        System.out.println("1. Add new customer");
        System.out.println("2. View existing customer");
        int c = getInt();
        if (c == 1) {
            System.out.print("First and last name: ");
            String name = sc.nextLine();
            System.out.print("Address: ");
            String address = sc.nextLine();
            System.out.print("Email: ");
            String email = sc.nextLine();
            System.out.print("Start date: ");
            String start = sc.nextLine();
            System.out.print("Active moving status: ");
            String active = sc.nextLine();
            System.out.print("Phone number (used as ID): ");
            String phone = sc.nextLine();
            Map<String, String> info = new HashMap<>();
            info.put("Name", name);
            info.put("Address", address);
            info.put("Email", email);
            info.put("Start date", start);
            info.put("Active moving status", active);
            customers.put(phone, info);
            System.out.println("Customer added.");
        } else if (c == 2) {
            System.out.print("Input the customer phone number: ");
            String phone = sc.nextLine();
            customers.putIfAbsent(phone, new HashMap<>());
            Map<String, String> cust = customers.get(phone);
            String[] keys = {"Name", "Address", "Warehouse distance", "Email", "Start date", "Active moving status"};
            while (true) {
                System.out.println("\nCustomer Menu:");
                for (int i = 0; i < keys.length; i++)
                    System.out.println((i + 1) + ". " + keys[i]);
                System.out.println("0. Exit to Main Menu");
                int opt = getInt();
                if (opt == 0) return;
                if (opt >= 1 && opt <= keys.length)
                    viewModifyMenu(cust, keys[opt - 1]);
                else
                    System.out.println("Invalid choice.");
            }
        }
    }

    // Purchase Order Menu
    private static void purchaseOrderMenu() {
        System.out.print("Input the purchase order number: ");
        String id = sc.nextLine();
        purchaseOrders.putIfAbsent(id, new HashMap<>());
        Map<String, String> p = purchaseOrders.get(id);
        String[] keys = {"Element type", "Actual arrival date", "Quantity", "Estimated arrival date", "Value"};
        while (true) {
            System.out.println("\nPurchase Order Menu:");
            for (int i = 0; i < keys.length; i++)
                System.out.println((i + 1) + ". " + keys[i]);
            System.out.println("0. Exit to Main Menu");
            int opt = getInt();
            if (opt == 0) return;
            if (opt >= 1 && opt <= keys.length)
                viewModifyMenu(p, keys[opt - 1]);
            else
                System.out.println("Invalid choice.");
        }
    }

    // Rating & Review Menu
    private static void ratingMenu() {
        System.out.print("Input the rating ID: ");
        String id = sc.nextLine();
        ratings.putIfAbsent(id, new HashMap<>());
        Map<String, String> r = ratings.get(id);
        String[] keys = {"Review", "Rating"};
        while (true) {
            System.out.println("\nRating and Review Menu:");
            for (int i = 0; i < keys.length; i++)
                System.out.println((i + 1) + ". " + keys[i]);
            System.out.println("0. Exit to Main Menu");
            int opt = getInt();
            if (opt == 0) return;
            if (opt >= 1 && opt <= keys.length)
                viewModifyMenu(r, keys[opt - 1]);
            else
                System.out.println("Invalid choice.");
        }
    }
}
