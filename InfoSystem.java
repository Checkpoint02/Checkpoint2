
import java.util.*;

public class InfoSystem {
    private static final Scanner sc = new Scanner(System.in);

    private static final Map<String, Warehouse> warehouses = new HashMap<>();
    private static final Map<String, Drone> drones = new HashMap<>();
    private static final Map<String, Equipment> equipment = new HashMap<>();
    private static final Map<String, Customer> customers = new HashMap<>();
    private static final Map<String, PurchaseOrder> purchaseOrders = new HashMap<>();
    private static final Map<String, Rating> ratings = new HashMap<>();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nInfo System Menu:");
            System.out.println("1. Warehouse Information");
            System.out.println("2. Drone Information");
            System.out.println("3. Equipment Information");
            System.out.println("4. Customer Information");
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
                    System.out.println("Exiting");
                    sc.close();
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
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
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty.");
        }
    }


    private static void warehouseMenu() {
        String id = getNonEmptyLine("Enter Warehouse ID: ");
        warehouses.putIfAbsent(id, new Warehouse(id));
        Warehouse w = warehouses.get(id);
        w.displayMenu();
    }

    private static void droneMenu() {
        String id = getNonEmptyLine("Enter Drone Serial Number: ");
        drones.putIfAbsent(id, new Drone(id));
        drones.get(id).displayMenu();
    }

    private static void equipmentMenu() {
        System.out.println("1. View Equipment by Model");
        System.out.println("2. View/Edit Equipment by Serial Number");
        int choice = getInt();

        if (choice == 1) {
            String model = getNonEmptyLine("Enter Model: ");
            equipment.values().stream()
                    .filter(e -> model.equalsIgnoreCase(e.model))
                    .forEach(e -> System.out.println("Serial: " + e.serial));
        } else if (choice == 2) {
            String serial = getNonEmptyLine("Enter Equipment Serial Number: ");
            equipment.putIfAbsent(serial, new Equipment(serial));
            equipment.get(serial).displayMenu();
        }
    }

    private static void customerMenu() {
        System.out.println("1. Add New Customer");
        System.out.println("2. View/Edit Customer");
        int choice = getInt();

        if (choice == 1) {
            Customer c = Customer.createFromInput(sc);
            customers.put(c.phone, c);
            System.out.println("Customer added.");
        } else if (choice == 2) {
            String phone = getNonEmptyLine("Enter Customer Phone Number: ");
            customers.putIfAbsent(phone, new Customer(phone));
            customers.get(phone).displayMenu();
        }
    }

    private static void purchaseOrderMenu() {
        String id = getNonEmptyLine("Enter Purchase Order Number: ");
        purchaseOrders.putIfAbsent(id, new PurchaseOrder(id));
        purchaseOrders.get(id).displayMenu();
    }

    private static void ratingMenu() {
        String id = getNonEmptyLine("Enter Rating ID: ");
        ratings.putIfAbsent(id, new Rating(id));
        ratings.get(id).displayMenu();
    }


    static class Warehouse {
        String id, storage = "", phone = "", capacity = "", manager = "", address = "", city = "";

        Warehouse(String id) { this.id = id; }

        void displayMenu() {
            while (true) {
                System.out.println("\n--- Warehouse Menu ---");
                System.out.println("1. Storage\n2. Phone\n3. Drone Capacity\n4. Manager\n5. Address\n6. City\n0. Back");
                int c = getInt();
                switch (c) {
                    case 1 -> storage = getNonEmptyLine("Enter Storage: ");
                    case 2 -> phone = getNonEmptyLine("Enter Phone: ");
                    case 3 -> capacity = getNonEmptyLine("Enter Drone Capacity: ");
                    case 4 -> manager = getNonEmptyLine("Enter Manager Name: ");
                    case 5 -> address = getNonEmptyLine("Enter Address: ");
                    case 6 -> city = getNonEmptyLine("Enter City: ");
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    static class Drone {
        String id;
        Map<String, String> attributes = new LinkedHashMap<>();

        Drone(String id) {
            this.id = id;
            for (String key : List.of("Max speed", "Manufacturer", "Location", "Active status", "Distance autonomy",
                    "Name", "Warranty expiration", "Model", "Year", "Status", "Employee responsible")) {
                attributes.put(key, "");
            }
        }

        void displayMenu() {
            List<String> keys = new ArrayList<>(attributes.keySet());
            while (true) {
                System.out.println("\n--- Drone Menu ---");
                for (int i = 0; i < keys.size(); i++)
                    System.out.println((i + 1) + ". " + keys.get(i));
                System.out.println("0. Back");
                int c = getInt();
                if (c == 0) return;
                if (c > 0 && c <= keys.size())
                    attributes.put(keys.get(c - 1), getNonEmptyLine("Enter " + keys.get(c - 1) + ": "));
                else
                    System.out.println("Invalid choice.");
            }
        }
    }

    static class Equipment {
        String serial, description = "", model = "", year = "", status = "", location = "";

        Equipment(String serial) { this.serial = serial; }

        void displayMenu() {
            while (true) {
                System.out.println("\n--- Equipment Menu ---");
                System.out.println("1. Description\n2. Model\n3. Year\n4. Status\n5. Location\n0. Back");
                int opt = getInt();
                switch (opt) {
                    case 1 -> description = getNonEmptyLine("Enter Description: ");
                    case 2 -> model = getNonEmptyLine("Enter Model: ");
                    case 3 -> year = getNonEmptyLine("Enter Year: ");
                    case 4 -> status = getNonEmptyLine("Enter Status: ");
                    case 5 -> location = getNonEmptyLine("Enter Location: ");
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    static class Customer {
        String phone, name = "", address = "", email = "", startDate = "", activeStatus = "", warehouseDistance = "";

        Customer(String phone) { this.phone = phone; }

        static Customer createFromInput(Scanner sc) {
            Customer c = new Customer(getNonEmptyLine("Phone number: "));
            c.name = getNonEmptyLine("Name: ");
            c.address = getNonEmptyLine("Address: ");
            c.email = getNonEmptyLine("Email: ");
            c.startDate = getNonEmptyLine("Start date: ");
            c.activeStatus = getNonEmptyLine("Active status: ");
            return c;
        }

        void displayMenu() {
            while (true) {
                System.out.println("\nCustomer Menu:");
                System.out.println("1. Name\n2. Address\n3. Warehouse Distance\n4. Email\n5. Start Date\n6. Active Status\n0. Back");
                int opt = getInt();
                switch (opt) {
                    case 1 -> name = getNonEmptyLine("Enter Name: ");
                    case 2 -> address = getNonEmptyLine("Enter Address: ");
                    case 3 -> warehouseDistance = getNonEmptyLine("Enter Warehouse Distance: ");
                    case 4 -> email = getNonEmptyLine("Enter Email: ");
                    case 5 -> startDate = getNonEmptyLine("Enter Start Date: ");
                    case 6 -> activeStatus = getNonEmptyLine("Enter Active Status: ");
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    static class PurchaseOrder {
        String id;
        String type = "", actualDate = "", quantity = "", estimateDate = "", value = "";

        PurchaseOrder(String id) { this.id = id; }

        void displayMenu() {
            while (true) {
                System.out.println("\nPurchase Order Menu:");
                System.out.println("1. Element Type\n2. Actual Arrival Date\n3. Quantity\n4. Estimated Arrival Date\n5. Value\n0. Back");
                int opt = getInt();
                switch (opt) {
                    case 1 -> type = getNonEmptyLine("Enter Element Type: ");
                    case 2 -> actualDate = getNonEmptyLine("Enter Actual Arrival Date: ");
                    case 3 -> quantity = getNonEmptyLine("Enter Quantity: ");
                    case 4 -> estimateDate = getNonEmptyLine("Enter Estimated Arrival Date: ");
                    case 5 -> value = getNonEmptyLine("Enter Value: ");
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    static class Rating {
        String id, review = "", rating = "";

        Rating(String id) { this.id = id; }

        void displayMenu() {
            while (true) {
                System.out.println("\nRating & Review Menu:");
                System.out.println("1. Review\n2. Rating\n0. Back");
                int opt = getInt();
                switch (opt) {
                    case 1 -> review = getNonEmptyLine("Enter Review: ");
                    case 2 -> rating = getNonEmptyLine("Enter Rating (1-5): ");
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }
}
