package Delivery;

import Database.DatabaseManager;
import Main.Main;
import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class DeliveryManager {
    private DatabaseManager dbManager;
    private Scanner scanner;

    public DeliveryManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.scanner = Main.getScanner();
    }

    public void createDelivery() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("            CREATE DELIVERY");
        System.out.println("=".repeat(50));

        try {
            System.out.print("Payment ID: ");
            int paymentId = Integer.parseInt(scanner.nextLine());

            // Check payment
            String checkPaymentSql = """
                SELECT p.*, u.username, pr.name as product_name
                FROM payments p
                JOIN users u ON p.user_id = u.id
                JOIN products pr ON p.product_id = pr.id
                WHERE p.id = ?
            """;

            PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkPaymentSql);
            checkStmt.setInt(1, paymentId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Payment not found!");
                return;
            }

            String username = rs.getString("username");
            String productName = rs.getString("product_name");

            System.out.println("User: " + username);
            System.out.println("Product: " + productName);

            // Check if delivery already exists
            String checkDeliverySql = "SELECT id FROM deliveries WHERE payment_id = ?";
            PreparedStatement checkDelStmt = dbManager.getConnection().prepareStatement(checkDeliverySql);
            checkDelStmt.setInt(1, paymentId);
            ResultSet delRs = checkDelStmt.executeQuery();

            if (delRs.next()) {
                System.out.println("❌ Delivery for this payment already exists!");
                return;
            }

            // Show countries
            showCountries();

            System.out.print("\nCountry ID: ");
            int countryId = Integer.parseInt(scanner.nextLine());

            // Get shipping cost for the country
            String countrySql = "SELECT name, shipping_cost FROM countries WHERE id = ?";
            PreparedStatement countryStmt = dbManager.getConnection().prepareStatement(countrySql);
            countryStmt.setInt(1, countryId);
            ResultSet countryRs = countryStmt.executeQuery();

            if (!countryRs.next()) {
                System.out.println("❌ Country not found!");
                return;
            }

            String countryName = countryRs.getString("name");
            double shippingCost = countryRs.getDouble("shipping_cost");

            System.out.println("Country: " + countryName);
            System.out.println("Shipping cost: " + String.format("%,.2f ₸", shippingCost));

            // Show cities
            showCitiesByCountry(countryId);

            System.out.print("\nCity ID (or 0 if none): ");
            int cityId = Integer.parseInt(scanner.nextLine());

            double additionalCost = 0;
            if (cityId > 0) {
                String citySql = "SELECT name, additional_cost FROM cities WHERE id = ?";
                PreparedStatement cityStmt = dbManager.getConnection().prepareStatement(citySql);
                cityStmt.setInt(1, cityId);
                ResultSet cityRs = cityStmt.executeQuery();

                if (cityRs.next()) {
                    String cityName = cityRs.getString("name");
                    additionalCost = cityRs.getDouble("additional_cost");
                    System.out.println("City: " + cityName);
                    System.out.println("Additional cost: " + String.format("%,.2f ₸", additionalCost));
                }
                cityRs.close();
                cityStmt.close();
            }

            System.out.print("\nDelivery address: ");
            String address = scanner.nextLine();

            System.out.print("Estimated delivery date (YYYY-MM-DD): ");
            String estimatedDate = scanner.nextLine();

            double totalDeliveryCost = shippingCost + additionalCost;
            System.out.println("\n" + "-".repeat(50));
            System.out.println("TOTAL delivery cost: " + String.format("%,.2f ₸", totalDeliveryCost));
            System.out.println("-".repeat(50));

            System.out.print("\n✅ Create delivery? (yes/no): ");
            String confirm = scanner.nextLine();

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("❌ Operation cancelled.");
                return;
            }

            // Create delivery
            String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            String sql = """
                INSERT INTO deliveries (payment_id, country_id, city_id, address, 
                                      delivery_cost, tracking_number, estimated_delivery, status) 
                VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, paymentId);
            pstmt.setInt(2, countryId);

            if (cityId > 0) {
                pstmt.setInt(3, cityId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setString(4, address);
            pstmt.setDouble(5, totalDeliveryCost);
            pstmt.setString(6, trackingNumber);
            pstmt.setDate(7, Date.valueOf(estimatedDate));

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int deliveryId = generatedKeys.getInt(1);
                    System.out.println("\n✅ Delivery created!");
                    System.out.println("Delivery ID: " + deliveryId);
                    System.out.println("Tracking number: " + trackingNumber);
                    System.out.println("Address: " + address);
                    System.out.println("Cost: " + String.format("%,.2f ₸", totalDeliveryCost));
                    System.out.println("Estimated date: " + estimatedDate);
                }
                generatedKeys.close();
            }

            pstmt.close();
            countryRs.close();
            countryStmt.close();
            delRs.close();
            checkDelStmt.close();
            rs.close();
            checkStmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void viewAllDeliveries() {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("                                          ALL DELIVERIES");
        System.out.println("=".repeat(120));

        try {
            String sql = """
                SELECT d.id, d.tracking_number, u.username, c.name as country_name, 
                       ct.name as city_name, d.delivery_cost, d.status, d.estimated_delivery
                FROM deliveries d
                JOIN payments p ON d.payment_id = p.id
                JOIN users u ON p.user_id = u.id
                JOIN countries c ON d.country_id = c.id
                LEFT JOIN cities ct ON d.city_id = ct.id
                ORDER BY d.created_at DESC
            """;

            Statement stmt = dbManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.printf("%-5s | %-15s | %-15s | %-20s | %-20s | %-12s | %-12s | %-12s%n",
                    "ID", "Tracking number", "User", "Country", "City", "Cost", "Status", "Date");
            System.out.println("-".repeat(120));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("%-5d | %-15s | %-15s | %-20s | %-20s | %,10.2f ₸ | %-12s | %s%n",
                        rs.getInt("id"),
                        rs.getString("tracking_number"),
                        truncate(rs.getString("username"), 15),
                        truncate(rs.getString("country_name"), 20),
                        truncate(rs.getString("city_name"), 20),
                        rs.getDouble("delivery_cost"),
                        rs.getString("status"),
                        rs.getDate("estimated_delivery"));
            }

            System.out.println("=".repeat(120));
            System.out.println("Total deliveries: " + count);

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void updateDeliveryStatus() {
        System.out.print("\nEnter delivery ID: ");
        try {
            int deliveryId = Integer.parseInt(scanner.nextLine());

            // Check delivery
            String checkSql = """
                SELECT d.*, d.tracking_number, u.username
                FROM deliveries d
                JOIN payments p ON d.payment_id = p.id
                JOIN users u ON p.user_id = u.id
                WHERE d.id = ?
            """;

            PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, deliveryId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Delivery not found!");
                return;
            }

            String trackingNumber = rs.getString("tracking_number");
            String username = rs.getString("username");
            String currentStatus = rs.getString("status");

            System.out.println("\nTracking number: " + trackingNumber);
            System.out.println("User: " + username);
            System.out.println("Current status: " + currentStatus);

            System.out.println("\nNew status:");
            System.out.println("1 - PENDING");
            System.out.println("2 - PROCESSING");
            System.out.println("3 - SHIPPED");
            System.out.println("4 - IN_TRANSIT");
            System.out.println("5 - OUT_FOR_DELIVERY");
            System.out.println("6 - DELIVERED");
            System.out.println("7 - CANCELLED");
            System.out.print("Choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            String newStatus = switch (choice) {
                case 1 -> "PENDING";
                case 2 -> "PROCESSING";
                case 3 -> "SHIPPED";
                case 4 -> "IN_TRANSIT";
                case 5 -> "OUT_FOR_DELIVERY";
                case 6 -> "DELIVERED";
                case 7 -> "CANCELLED";
                default -> null;
            };

            if (newStatus == null) {
                System.out.println("❌ Invalid choice!");
                return;
            }

            String updateSql = "UPDATE deliveries SET status = ? WHERE id = ?";
            PreparedStatement updateStmt = dbManager.getConnection().prepareStatement(updateSql);
            updateStmt.setString(1, newStatus);
            updateStmt.setInt(2, deliveryId);

            // If delivered, set delivery date
            if ("DELIVERED".equals(newStatus)) {
                updateSql = "UPDATE deliveries SET status = ?, delivered_at = CURRENT_TIMESTAMP WHERE id = ?";
                updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                updateStmt.setString(1, newStatus);
                updateStmt.setInt(2, deliveryId);
            }

            int affected = updateStmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Delivery status updated!");
                System.out.println("Old status: " + currentStatus);
                System.out.println("New status: " + newStatus);
            }

            updateStmt.close();
            rs.close();
            checkStmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void manageCountries() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("          COUNTRY MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1 - Add country");
            System.out.println("2 - View all countries");
            System.out.println("3 - Update shipping cost");
            System.out.println("4 - Delete country");
            System.out.println("0 - Back");
            System.out.print("Choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> addCountry();
                case 2 -> showCountries();
                case 3 -> updateCountryShippingCost();
                case 4 -> deleteCountry();
                case 0 -> { return; }
                default -> System.out.println("❌ Invalid choice!");
            }
        }
    }

    public void manageCities() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("          CITY MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1 - Add city");
            System.out.println("2 - View all cities");
            System.out.println("3 - Delete city");
            System.out.println("0 - Back");
            System.out.print("Choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> addCity();
                case 2 -> showAllCities();
                case 3 -> deleteCity();
                case 0 -> { return; }
                default -> System.out.println("❌ Invalid choice!");
            }
        }
    }

    private void addCountry() {
        try {
            System.out.print("\nCountry name: ");
            String name = scanner.nextLine();

            System.out.print("Country code (3 letters): ");
            String code = scanner.nextLine().toUpperCase();

            System.out.print("Shipping cost: ");
            double shippingCost = Double.parseDouble(scanner.nextLine());

            String sql = "INSERT INTO countries (name, code, shipping_cost) VALUES (?, ?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, code);
            pstmt.setDouble(3, shippingCost);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("✅ Country added with ID: " + rs.getInt(1));
                }
                rs.close();
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private void showCountries() {
        try {
            String sql = "SELECT * FROM countries ORDER BY name";
            Statement stmt = dbManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\n" + "=".repeat(60));
            System.out.printf("%-5s | %-30s | %-5s | %-15s%n", "ID", "Name", "Code", "Cost");
            System.out.println("-".repeat(60));

            while (rs.next()) {
                System.out.printf("%-5d | %-30s | %-5s | %,13.2f ₸%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("code"),
                        rs.getDouble("shipping_cost"));
            }
            System.out.println("=".repeat(60));

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private void updateCountryShippingCost() {
        try {
            showCountries();
            System.out.print("\nCountry ID: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("New shipping cost: ");
            double cost = Double.parseDouble(scanner.nextLine());

            String sql = "UPDATE countries SET shipping_cost = ? WHERE id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setDouble(1, cost);
            pstmt.setInt(2, id);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Cost updated!");
            } else {
                System.out.println("❌ Country not found!");
            }

            pstmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteCountry() {
        try {
            showCountries();
            System.out.print("\nCountry ID to delete: ");
            int id = Integer.parseInt(scanner.nextLine());

            String sql = "DELETE FROM countries WHERE id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Country deleted!");
            } else {
                System.out.println("❌ Country not found!");
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private void addCity() {
        try {
            showCountries();
            System.out.print("\nCountry ID: ");
            int countryId = Integer.parseInt(scanner.nextLine());

            System.out.print("City name: ");
            String name = scanner.nextLine();

            System.out.print("Additional cost: ");
            double additionalCost = Double.parseDouble(scanner.nextLine());

            String sql = "INSERT INTO cities (name, country_id, additional_cost) VALUES (?, ?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setInt(2, countryId);
            pstmt.setDouble(3, additionalCost);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("✅ City added with ID: " + rs.getInt(1));
                }
                rs.close();
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private void showAllCities() {
        try {
            String sql = """
                SELECT c.id, c.name, co.name as country_name, c.additional_cost
                FROM cities c
                JOIN countries co ON c.country_id = co.id
                ORDER BY co.name, c.name
            """;

            Statement stmt = dbManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\n" + "=".repeat(80));
            System.out.printf("%-5s | %-30s | %-30s | %-10s%n", "ID", "City", "Country", "Add. cost");
            System.out.println("-".repeat(80));

            while (rs.next()) {
                System.out.printf("%-5d | %-30s | %-30s | %,8.2f ₸%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("country_name"),
                        rs.getDouble("additional_cost"));
            }
            System.out.println("=".repeat(80));

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private void showCitiesByCountry(int countryId) {
        try {
            String sql = "SELECT id, name, additional_cost FROM cities WHERE country_id = ? ORDER BY name";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, countryId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nAvailable cities:");
            System.out.println("-".repeat(60));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("%d - %s (add. cost: %,.2f ₸)%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("additional_cost"));
            }

            if (count == 0) {
                System.out.println("No available cities");
            }

            System.out.println("-".repeat(60));

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteCity() {
        try {
            showAllCities();
            System.out.print("\nCity ID to delete: ");
            int id = Integer.parseInt(scanner.nextLine());

            String sql = "DELETE FROM cities WHERE id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ City deleted!");
            } else {
                System.out.println("❌ City not found!");
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}