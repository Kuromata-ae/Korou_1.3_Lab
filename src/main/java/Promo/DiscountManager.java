package Promo;

import Database.DatabaseManager;
import Main.Main;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class DiscountManager {
    private DatabaseManager dbManager;
    private Scanner scanner;

    public DiscountManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.scanner = Main.getScanner();
    }

    public void createProductDiscount() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("        CREATE PRODUCT DISCOUNT");
        System.out.println("=".repeat(50));

        try {
            System.out.print("Product ID: ");
            int productId = Integer.parseInt(scanner.nextLine());

            // Check product
            String checkSql = "SELECT name, price FROM products WHERE id = ?";
            PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, productId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Product not found!");
                return;
            }

            String productName = rs.getString("name");
            double price = rs.getDouble("price");

            System.out.println("Product: " + productName);
            System.out.println("Current price: " + String.format("%,.2f ₸", price));

            System.out.print("\nDiscount percent (0-100): ");
            double discountPercent = Double.parseDouble(scanner.nextLine());

            if (discountPercent < 0 || discountPercent > 100) {
                System.out.println("❌ Percent must be between 0 and 100!");
                return;
            }

            double discountedPrice = price - (price * discountPercent / 100);
            System.out.println("Discounted price: " + String.format("%,.2f ₸", discountedPrice));

            System.out.print("\nStart date (YYYY-MM-DD HH:MM or enter for current): ");
            String startDateStr = scanner.nextLine();
            Timestamp startDate;

            if (startDateStr.isEmpty()) {
                startDate = Timestamp.valueOf(LocalDateTime.now());
            } else {
                startDate = Timestamp.valueOf(LocalDateTime.parse(startDateStr + ":00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            System.out.print("End date (YYYY-MM-DD HH:MM): ");
            String endDateStr = scanner.nextLine();
            Timestamp endDate = Timestamp.valueOf(LocalDateTime.parse(endDateStr + ":00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            String sql = """
                INSERT INTO product_discounts (product_id, discount_percent, start_date, end_date) 
                VALUES (?, ?, ?, ?)
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, productId);
            pstmt.setDouble(2, discountPercent);
            pstmt.setTimestamp(3, startDate);
            pstmt.setTimestamp(4, endDate);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int discountId = generatedKeys.getInt(1);
                    System.out.println("\n✅ Discount created!");
                    System.out.println("Discount ID: " + discountId);
                    System.out.println("Product: " + productName);
                    System.out.println("Discount: " + discountPercent + "%");
                    System.out.println("Period: " + startDate + " - " + endDate);
                }
                generatedKeys.close();
            }

            pstmt.close();
            rs.close();
            checkStmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void createCategoryDiscount() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("        CREATE CATEGORY DISCOUNT");
        System.out.println("=".repeat(50));

        try {
            // Show categories
            showCategories();

            System.out.print("\nCategory ID: ");
            int categoryId = Integer.parseInt(scanner.nextLine());

            // Check category
            String checkSql = "SELECT name FROM categories WHERE id = ?";
            PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, categoryId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Category not found!");
                return;
            }

            String categoryName = rs.getString("name");
            System.out.println("Category: " + categoryName);

            // Show products in category
            String productsSql = "SELECT COUNT(*) as count FROM products WHERE category_id = ?";
            PreparedStatement productsStmt = dbManager.getConnection().prepareStatement(productsSql);
            productsStmt.setInt(1, categoryId);
            ResultSet productsRs = productsStmt.executeQuery();

            if (productsRs.next()) {
                System.out.println("Products in category: " + productsRs.getInt("count"));
            }

            System.out.print("\nDiscount percent (0-100): ");
            double discountPercent = Double.parseDouble(scanner.nextLine());

            if (discountPercent < 0 || discountPercent > 100) {
                System.out.println("❌ Percent must be between 0 and 100!");
                return;
            }

            System.out.print("\nStart date (YYYY-MM-DD HH:MM or enter for current): ");
            String startDateStr = scanner.nextLine();
            Timestamp startDate;

            if (startDateStr.isEmpty()) {
                startDate = Timestamp.valueOf(LocalDateTime.now());
            } else {
                startDate = Timestamp.valueOf(LocalDateTime.parse(startDateStr + ":00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            System.out.print("End date (YYYY-MM-DD HH:MM): ");
            String endDateStr = scanner.nextLine();
            Timestamp endDate = Timestamp.valueOf(LocalDateTime.parse(endDateStr + ":00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            String sql = """
                INSERT INTO category_discounts (category_id, discount_percent, start_date, end_date) 
                VALUES (?, ?, ?, ?)
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, categoryId);
            pstmt.setDouble(2, discountPercent);
            pstmt.setTimestamp(3, startDate);
            pstmt.setTimestamp(4, endDate);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int discountId = generatedKeys.getInt(1);
                    System.out.println("\n✅ Discount created!");
                    System.out.println("Discount ID: " + discountId);
                    System.out.println("Category: " + categoryName);
                    System.out.println("Discount: " + discountPercent + "%");
                    System.out.println("Period: " + startDate + " - " + endDate);
                }
                generatedKeys.close();
            }

            pstmt.close();
            productsRs.close();
            productsStmt.close();
            rs.close();
            checkStmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void viewAllDiscounts() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("                                    ALL ACTIVE DISCOUNTS");
        System.out.println("=".repeat(100));

        try {
            // Product discounts
            System.out.println("\n📦 PRODUCT DISCOUNTS:");
            String productDiscountsSql = """
                SELECT pd.id, p.name as product_name, pd.discount_percent, 
                       pd.start_date, pd.end_date, pd.is_active
                FROM product_discounts pd
                JOIN products p ON pd.product_id = p.id
                ORDER BY pd.created_at DESC
            """;

            Statement stmt1 = dbManager.getConnection().createStatement();
            ResultSet rs1 = stmt1.executeQuery(productDiscountsSql);

            System.out.printf("%-5s | %-40s | %-10s | %-20s | %-20s | %-8s%n",
                    "ID", "Product", "Discount %", "Start", "End", "Active");
            System.out.println("-".repeat(100));

            int count1 = 0;
            while (rs1.next()) {
                count1++;
                System.out.printf("%-5d | %-40s | %-10.2f | %-20s | %-20s | %-8s%n",
                        rs1.getInt("id"),
                        truncate(rs1.getString("product_name"), 40),
                        rs1.getDouble("discount_percent"),
                        rs1.getTimestamp("start_date"),
                        rs1.getTimestamp("end_date"),
                        rs1.getBoolean("is_active") ? "Yes" : "No");
            }
            System.out.println("Total: " + count1);

            // Category discounts
            System.out.println("\n📁 CATEGORY DISCOUNTS:");
            String categoryDiscountsSql = """
                SELECT cd.id, c.name as category_name, cd.discount_percent, 
                       cd.start_date, cd.end_date, cd.is_active
                FROM category_discounts cd
                JOIN categories c ON cd.category_id = c.id
                ORDER BY cd.created_at DESC
            """;

            Statement stmt2 = dbManager.getConnection().createStatement();
            ResultSet rs2 = stmt2.executeQuery(categoryDiscountsSql);

            System.out.printf("%-5s | %-40s | %-10s | %-20s | %-20s | %-8s%n",
                    "ID", "Category", "Discount %", "Start", "End", "Active");
            System.out.println("-".repeat(100));

            int count2 = 0;
            while (rs2.next()) {
                count2++;
                System.out.printf("%-5d | %-40s | %-10.2f | %-20s | %-20s | %-8s%n",
                        rs2.getInt("id"),
                        truncate(rs2.getString("category_name"), 40),
                        rs2.getDouble("discount_percent"),
                        rs2.getTimestamp("start_date"),
                        rs2.getTimestamp("end_date"),
                        rs2.getBoolean("is_active") ? "Yes" : "No");
            }
            System.out.println("Total: " + count2);
            System.out.println("=".repeat(100));

            rs1.close();
            stmt1.close();
            rs2.close();
            stmt2.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void deleteDiscount() {
        System.out.println("\n1 - Delete product discount");
        System.out.println("2 - Delete category discount");
        System.out.print("Choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 1) {
                System.out.print("Product discount ID: ");
                int discountId = Integer.parseInt(scanner.nextLine());

                String sql = "DELETE FROM product_discounts WHERE id = ?";
                PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
                pstmt.setInt(1, discountId);

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    System.out.println("✅ Discount deleted!");
                } else {
                    System.out.println("❌ Discount not found!");
                }

                pstmt.close();

            } else if (choice == 2) {
                System.out.print("Category discount ID: ");
                int discountId = Integer.parseInt(scanner.nextLine());

                String sql = "DELETE FROM category_discounts WHERE id = ?";
                PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
                pstmt.setInt(1, discountId);

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    System.out.println("✅ Discount deleted!");
                } else {
                    System.out.println("❌ Discount not found!");
                }

                pstmt.close();

            } else {
                System.out.println("❌ Invalid choice!");
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private void showCategories() {
        try {
            String sql = "SELECT id, name FROM categories ORDER BY name";
            Statement stmt = dbManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\nAvailable categories:");
            System.out.println("-".repeat(40));
            while (rs.next()) {
                System.out.printf("%d - %s%n", rs.getInt("id"), rs.getString("name"));
            }
            System.out.println("-".repeat(40));

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Error loading categories: " + e.getMessage());
        }
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}