package Main;

import Database.DatabaseManager;
import Economy.Product.ProductManager;
import Economy.Payment.PaymentManager;
import Promo.DiscountManager;
import Delivery.DeliveryManager;
import java.util.Scanner;

public class Main {
    private static DatabaseManager dbManager;
    private static ProductManager productManager;
    private static PaymentManager paymentManager;
    private static AccountManager accountManager;
    private static DiscountManager discountManager;
    private static DeliveryManager deliveryManager;
    private static Scanner scanner;

    public static void main(String[] args) {
        init();
        showMainMenu();
    }

    private static void init() {
        scanner = new Scanner(System.in);
        dbManager = new DatabaseManager();
        dbManager.initDatabase();

        productManager = new ProductManager(dbManager);
        paymentManager = new PaymentManager(dbManager);
        accountManager = new AccountManager(dbManager);
        discountManager = new DiscountManager(dbManager);
        deliveryManager = new DeliveryManager(dbManager);

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   KURO LAB MARKETPLACE SYSTEM v1.0     ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("          MARKETPLACE MANAGEMENT CENTER");
            System.out.println("=".repeat(50));
            System.out.println("1  - Product Management");
            System.out.println("2  - User Management");
            System.out.println("3  - Payment management");
            System.out.println("4  - Discount management");
            System.out.println("5  - Delivery Management");
            System.out.println("6  - Transactions");
            System.out.println("7  - Statistics");
            System.out.println("8  - The database");
            System.out.println("0  - Exit");
            System.out.println("=".repeat(50));
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> productMenu();
                case 2 -> accountMenu();
                case 3 -> paymentMenu();
                case 4 -> discountMenu();
                case 5 -> deliveryMenu();
                case 6 -> transactionMenu();
                case 7 -> statisticsMenu();
                case 8 -> databaseMenu();
                case 0 -> {
                    System.out.println("\n👋 Log out of the system...");
                    dbManager.closeConnection();
                    System.exit(0);
                }
                default -> System.out.println("❌ Wrong choice!");
            }
        }
    }

    private static void productMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           PRODUCT MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1 - Create a physical product");
            System.out.println("2 - Create a digital product");
            System.out.println("3 - View all products");
            System.out.println("4 - Find a product by ID");
            System.out.println("5 - Update the product");
            System.out.println("6 - Delete a product");
            System.out.println("7 - Products by category");
            System.out.println("0 - Back");
            System.out.println("=".repeat(50));
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> productManager.createPhysicalProduct();
                case 2 -> productManager.createDigitalProduct();
                case 3 -> productManager.viewAllProducts();
                case 4 -> productManager.findProductById();
                case 5 -> productManager.updateProduct();
                case 6 -> productManager.deleteProduct();
                case 7 -> productManager.viewProductsByCategory();
                case 0 -> { return; }
                default -> System.out.println("❌ Select an option:");
            }
        }
    }

    private static void accountMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("         USER MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1 - Create a user");
            System.out.println("2 - View all users");
            System.out.println("3 - Find a user");
            System.out.println("4 - Update the user");
            System.out.println("5 - Delete a user");
            System.out.println("6 - Balance management");
            System.out.println("0 - Back");
            System.out.println("=".repeat(50));
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> accountManager.createUser();
                case 2 -> accountManager.viewAllUsers();
                case 3 -> accountManager.findUser();
                case 4 -> accountManager.updateUser();
                case 5 -> accountManager.deleteUser();
                case 6 -> accountManager.manageBalance();
                case 0 -> { return; }
                default -> System.out.println("❌ Select an option:");
            }
        }
    }

    private static void paymentMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("            PAYMENT MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1 - Create a payment");
            System.out.println("2 - View all payments");
            System.out.println("3 - User's payment history");
            System.out.println("4 - Refund of payment");
            System.out.println("0 - Back");
            System.out.println("=".repeat(50));
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> paymentManager.createPayment();
                case 2 -> paymentManager.viewAllPayments();
                case 3 -> paymentManager.viewUserPayments();
                case 4 -> paymentManager.refundPayment();
                case 0 -> { return; }
                default -> System.out.println("❌ Wrong choice!");
            }
        }
    }

    private static void discountMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("            DISCOUNT MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1 - Create a discount on a product");
            System.out.println("2 - Create a discount for a category");
            System.out.println("3 - View all discounts");
            System.out.println("4 - Delete a discount");
            System.out.println("0 - Back");
            System.out.println("=".repeat(50));
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> discountManager.createProductDiscount();
                case 2 -> discountManager.createCategoryDiscount();
                case 3 -> discountManager.viewAllDiscounts();
                case 4 -> discountManager.deleteDiscount();
                case 0 -> { return; }
                default -> System.out.println("❌ Wrong choice!");
            }
        }
    }

    private static void deliveryMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("            DELIVERY MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1 - Создать доставку");
            System.out.println("2 - View all deliveries");
            System.out.println("3 - Update the delivery status");
            System.out.println("4 - Country management");
            System.out.println("5 - City management");
            System.out.println("0 - Back");
            System.out.println("=".repeat(50));
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> deliveryManager.createDelivery();
                case 2 -> deliveryManager.viewAllDeliveries();
                case 3 -> deliveryManager.updateDeliveryStatus();
                case 4 -> deliveryManager.manageCountries();
                case 5 -> deliveryManager.manageCities();
                case 0 -> { return; }
                default -> System.out.println("❌Wrong choice!");
            }
        }
    }

    private static void transactionMenu() {
        System.out.println("\n🚧 The transaction menu is under development...");
    }

    private static void statisticsMenu() {
        System.out.println("\n📊 The statistics menu is under development...");
    }

    private static void databaseMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("          DATABASE MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1 - Show all tables");
            System.out.println("2 - Clear all tables");
            System.out.println("3 - Reset the database");
            System.out.println("4 - Exporting data");
            System.out.println("0 - Back");
            System.out.println("=".repeat(50));
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> dbManager.showAllTables();
                case 2 -> dbManager.clearAllTables();
                case 3 -> dbManager.resetDatabase();
                case 4 -> dbManager.exportData();
                case 0 -> { return; }
                default -> System.out.println("❌ Wrong choice!");
            }
        }
    }

    private static int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static Scanner getScanner() {
        return scanner;
    }
}