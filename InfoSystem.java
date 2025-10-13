
import java.util.*;

public class InfoSystem {
    private static final Scanner sc = new Scanner(System.in);

    //we are temporarily using hash maps to let our code run without implementing databases
    private static final Map<String, Warehouse> warehouses = new HashMap<>();
    private static final Map<String, Drone> drones = new HashMap<>();
    private static final Map<String, Equipment> equipment = new HashMap<>();
    private static final Map<String, Customer> customers = new HashMap<>();
    private static final Map<String, PurchaseOrder> purchaseOrders = new HashMap<>();
    private static final Map<String, Rating> ratings = new HashMap<>();

//our main menu
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
            System.out.print("Enter your choice (number 0 - 6): ");
            int choice = getInt();

            //we use switch to handle menu choices
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
                //incase the user inputs something invalid
                default -> System.out.println("Invalid choice. Please input a valid number 0 - 6.");
            }
        }
    }

// make sure again that the input is an integer
    private static int getInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    //get the input as long as the input is not empty
    private static String getNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty.");
        }
    }

//selects the warehouse information based off of the warehouse id if inputted
    private static void warehouseMenu() {
        String id = getNonEmptyLine("Enter Warehouse ID: ");
        warehouses.putIfAbsent(id, new Warehouse(id));
        Warehouse w = warehouses.get(id);
        w.displayMenu();
    }

    //drone menu that takes in the drone serial number
    private static void droneMenu() {
        String id = getNonEmptyLine("Enter the Drone's Serial Number: ");
        drones.putIfAbsent(id, new Drone(id));
        drones.get(id).displayMenu();
    }

    //our equipment menu that allows user to view equipment by model or serial number
    private static void equipmentMenu() {
        System.out.println("1. View the equipment by model");
        System.out.println("2. View or edit the equipment by serial number");
        int choice = getInt();

        if (choice == 1) {
            String model = getNonEmptyLine("Enter the model number: ");
            equipment.values().stream()
                    .filter(e -> model.equalsIgnoreCase(e.model))
                    .forEach(e -> System.out.println("Serial: " + e.serial));
        } else if (choice == 2) {
            String serial = getNonEmptyLine("Enter the equipment serial number: ");
            equipment.putIfAbsent(serial, new Equipment(serial));
            equipment.get(serial).displayMenu();
        }
    }

    //customer menu that allows user to add new customer or view/edit existing customer
    private static void customerMenu() {
        System.out.println("1. Add new customer");
        System.out.println("2. View/edit customer");
        int choice = getInt();

        if (choice == 1) {
            Customer c = Customer.createFromInput(sc);
            customers.put(c.phone, c);
            System.out.println("Customer has been added into the system!");
        } else if (choice == 2) {
            String phone = getNonEmptyLine("Enter the customer's phone number: ");
            customers.putIfAbsent(phone, new Customer(phone));
            customers.get(phone).displayMenu();
        }
    }

    
    private static void purchaseOrderMenu() {
        String id = getNonEmptyLine("Enter the purchase order number: ");
        purchaseOrders.putIfAbsent(id, new PurchaseOrder(id));
        purchaseOrders.get(id).displayMenu();
    }

    private static void ratingMenu() {
        String id = getNonEmptyLine("Enter the rating ID: ");
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
                    //someone check to see if the warehouse questions are correct because idk about the first question
                    case 1 -> storage = getNonEmptyLine("Enter warehouse the drone is being stored: ");
                    case 2 -> phone = getNonEmptyLine("Enter phone: ");
                    case 3 -> capacity = getNonEmptyLine("Enter drone capacity: ");
                    case 4 -> manager = getNonEmptyLine("Enter manager name: ");
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
                System.out.println("\nDrone Menu:");
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
                System.out.println("\nEquipment Menu:");
                System.out.println("1. Description\n2. Model\n3. Year\n4. Status\n5. Location\n0. Back");
                int opt = getInt();
                switch (opt) {
                    case 1 -> description = getNonEmptyLine("Enter description: ");
                    case 2 -> model = getNonEmptyLine("Enter model: ");
                    case 3 -> year = getNonEmptyLine("Enter year: ");
                    case 4 -> status = getNonEmptyLine("Enter status: ");
                    case 5 -> location = getNonEmptyLine("Enter location: ");
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice. Please input a valid number 0 - 5.");
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
                    case 1 -> name = getNonEmptyLine("Enter name: ");
                    case 2 -> address = getNonEmptyLine("Enter address: ");
                    case 3 -> warehouseDistance = getNonEmptyLine("Enter the warehouse distance: ");
                    case 4 -> email = getNonEmptyLine("Enter email: ");
                    case 5 -> startDate = getNonEmptyLine("Enter start date: ");
                    case 6 -> activeStatus = getNonEmptyLine("Enter active status: ");
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice. Please input a valid number 0 - 6.");
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
                    case 1 -> type = getNonEmptyLine("Enter the element type: ");
                    case 2 -> actualDate = getNonEmptyLine("Enter actual arrival date: ");
                    case 3 -> quantity = getNonEmptyLine("Enter quantity: ");
                    case 4 -> estimateDate = getNonEmptyLine("Enter estimated arrival date: ");
                    case 5 -> value = getNonEmptyLine("Enter value: ");
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
                    case 1 -> review = getNonEmptyLine("Enter your review: ");
                    case 2 -> rating = getNonEmptyLine("Enter your rating (0-5): ");
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice. Please input a valid rating.");
                }
            }
        }
    }
}
