package Economy.Product;

import Database.DatabaseManager;
import Main.Main;
import java.sql.*;
import java.util.Scanner;

public class ProductManager {
    private DatabaseManager dbManager;
    private Scanner scanner;

    public ProductManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.scanner = Main.getScanner();
    }

    public void createPhysicalProduct() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("        CREATE PHYSICAL PRODUCT");
        System.out.println("=".repeat(50));

        try {
            System.out.print("Product name: ");
            String name = scanner.nextLine();

            System.out.print("Description: ");
            String description = scanner.nextLine();

            System.out.print("Price: ");
            double price = Double.parseDouble(scanner.nextLine());

            System.out.print("Tax (% of price): ");
            double taxRate = Double.parseDouble(scanner.nextLine());

            System.out.print("Stock quantity: ");
            int stockQuantity = Integer.parseInt(scanner.nextLine());

            System.out.print("Weight (kg): ");
            double weight = Double.parseDouble(scanner.nextLine());

            // Show available categories
            showCategories();
            int categoryId;
            while (true) {
                System.out.print("Category ID (or 0 to create new): ");
                categoryId = Integer.parseInt(scanner.nextLine());

                if (categoryId == 0) {
                    categoryId = createCategory();
                    break;
                } else if (categoryExists(categoryId)) {
                    break;
                } else {
                    System.out.println("❌ Invalid category ID. Please try again.");
                }
            }

            // Insert into DB
            String sql = """
                INSERT INTO products (name, description, price, tax_rate, category_id, 
                                    product_type, stock_quantity, weight) 
                VALUES (?, ?, ?, ?, ?, 'PHYSICAL', ?, ?)
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setDouble(4, taxRate);
            pstmt.setInt(5, categoryId);
            pstmt.setInt(6, stockQuantity);
            pstmt.setDouble(7, weight);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int productId = rs.getInt(1);
                    System.out.println("\n✅ Physical product created!");
                    System.out.println("   ID: " + productId);
                    System.out.println("   Name: " + name);
                    System.out.println("   Price: " + price + " ₸");
                    System.out.println("   Tax: " + taxRate + "%");
                    System.out.println("   Price with tax: " + (price + price * taxRate / 100) + " ₸");
                    System.out.println("   Weight: " + weight + " kg");
                    System.out.println("   In stock: " + stockQuantity + " pcs.");
                }
            }

            pstmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error creating product: " + e.getMessage());
        }
    }

    public void createDigitalProduct() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("         CREATE DIGITAL PRODUCT");
        System.out.println("=".repeat(50));

        try {
            System.out.print("Product name: ");
            String name = scanner.nextLine();

            System.out.print("Description: ");
            String description = scanner.nextLine();

            System.out.print("Price: ");
            double price = Double.parseDouble(scanner.nextLine());

            System.out.print("Tax (% of price): ");
            double taxRate = Double.parseDouble(scanner.nextLine());

            System.out.print("File size (MB): ");
            double fileSize = Double.parseDouble(scanner.nextLine());

            System.out.print("Download URL: ");
            String downloadUrl = scanner.nextLine();

            // Show available categories
            showCategories();
            int categoryId;
            while (true) {
                System.out.print("Category ID (or 0 to create new): ");
                categoryId = Integer.parseInt(scanner.nextLine());

                if (categoryId == 0) {
                    categoryId = createCategory();
                    break;
                } else if (categoryExists(categoryId)) {
                    break;
                } else {
                    System.out.println("❌ Invalid category ID. Please try again.");
                }
            }

            // Insert into DB
            String sql = """
                INSERT INTO products (name, description, price, tax_rate, category_id, 
                                    product_type, file_size, download_url, stock_quantity) 
                VALUES (?, ?, ?, ?, ?, 'DIGITAL', ?, ?, 999999)
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setDouble(4, taxRate);
            pstmt.setInt(5, categoryId);
            pstmt.setDouble(6, fileSize);
            pstmt.setString(7, downloadUrl);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int productId = rs.getInt(1);
                    System.out.println("\n✅ Digital product created!");
                    System.out.println("   ID: " + productId);
                    System.out.println("   Name: " + name);
                    System.out.println("   Price: " + price + " ₸");
                    System.out.println("   Tax: " + taxRate + "%");
                    System.out.println("   Price with tax: " + (price + price * taxRate / 100) + " ₸");
                    System.out.println("   Size: " + fileSize + " MB");
                    System.out.println("   URL: " + downloadUrl);
                }
            }

            pstmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error creating product: " + e.getMessage());
        }
    }

    public void viewAllProducts() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("                                    ALL PRODUCTS");
        System.out.println("=".repeat(100));

        try {
            String sql = """
                SELECT p.id, p.name, p.price, p.tax_rate, p.product_type, 
                       p.stock_quantity, c.name as category_name, p.is_active
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                ORDER BY p.id
            """;

            Statement stmt = dbManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.printf("%-5s | %-30s | %-10s | %-8s | %-10s | %-8s | %-20s | %-8s%n",
                    "ID", "Name", "Price", "Tax %", "Type", "Stock", "Category", "Active");
            System.out.println("-".repeat(100));

            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                double taxRate = rs.getDouble("tax_rate");
                String type = rs.getString("product_type");
                int stock = rs.getInt("stock_quantity");
                String category = rs.getString("category_name");
                boolean isActive = rs.getBoolean("is_active");

                System.out.printf("%-5d | %-30s | %-10.2f | %-8.2f | %-10s | %-8d | %-20s | %-8s%n",
                        id, truncate(name, 30), price, taxRate, type, stock,
                        category != null ? truncate(category, 20) : "N/A",
                        isActive ? "Yes" : "No");
            }

            System.out.println("=".repeat(100));
            System.out.println("Total products: " + count);

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void findProductById() {
        System.out.print("\nEnter product ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());

            String sql = """
                SELECT p.*, c.name as category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.id = ?
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("           PRODUCT INFORMATION");
                System.out.println("=".repeat(50));
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Description: " + rs.getString("description"));
                System.out.println("Price: " + rs.getDouble("price") + " ₸");
                System.out.println("Tax: " + rs.getDouble("tax_rate") + "%");

                double price = rs.getDouble("price");
                double tax = rs.getDouble("tax_rate");
                double priceWithTax = price + (price * tax / 100);
                System.out.println("Price with tax: " + priceWithTax + " ₸");

                System.out.println("Type: " + rs.getString("product_type"));
                System.out.println("Category: " + rs.getString("category_name"));
                System.out.println("In stock: " + rs.getInt("stock_quantity"));

                if ("PHYSICAL".equals(rs.getString("product_type"))) {
                    System.out.println("Weight: " + rs.getDouble("weight") + " kg");
                } else {
                    System.out.println("File size: " + rs.getDouble("file_size") + " MB");
                    System.out.println("URL: " + rs.getString("download_url"));
                }

                System.out.println("Active: " + (rs.getBoolean("is_active") ? "Yes" : "No"));
                System.out.println("Created: " + rs.getTimestamp("created_at"));
                System.out.println("=".repeat(50));
            } else {
                System.out.println("❌ Product with ID " + id + " not found!");
            }

            rs.close();
            pstmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void updateProduct() {
        System.out.print("\nEnter product ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());

            // Check if product exists
            String checkSql = "SELECT * FROM products WHERE id = ?";
            PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Product with ID " + id + " not found!");
                return;
            }

            System.out.println("\nWhat to update?");
            System.out.println("1 - Name");
            System.out.println("2 - Price");
            System.out.println("3 - Tax");
            System.out.println("4 - Stock quantity");
            System.out.println("5 - Active status");
            System.out.print("Choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            String updateSql = "";
            PreparedStatement updateStmt = null;

            switch (choice) {
                case 1 -> {
                    System.out.print("New name: ");
                    String newName = scanner.nextLine();
                    updateSql = "UPDATE products SET name = ? WHERE id = ?";
                    updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                    updateStmt.setString(1, newName);
                    updateStmt.setInt(2, id);
                }
                case 2 -> {
                    System.out.print("New price: ");
                    double newPrice = Double.parseDouble(scanner.nextLine());
                    updateSql = "UPDATE products SET price = ? WHERE id = ?";
                    updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                    updateStmt.setDouble(1, newPrice);
                    updateStmt.setInt(2, id);
                }
                case 3 -> {
                    System.out.print("New tax (%): ");
                    double newTax = Double.parseDouble(scanner.nextLine());
                    updateSql = "UPDATE products SET tax_rate = ? WHERE id = ?";
                    updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                    updateStmt.setDouble(1, newTax);
                    updateStmt.setInt(2, id);
                }
                case 4 -> {
                    System.out.print("New quantity: ");
                    int newStock = Integer.parseInt(scanner.nextLine());
                    updateSql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
                    updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                    updateStmt.setInt(1, newStock);
                    updateStmt.setInt(2, id);
                }
                case 5 -> {
                    System.out.print("Active? (true/false): ");
                    boolean isActive = Boolean.parseBoolean(scanner.nextLine());
                    updateSql = "UPDATE products SET is_active = ? WHERE id = ?";
                    updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                    updateStmt.setBoolean(1, isActive);
                    updateStmt.setInt(2, id);
                }
                default -> {
                    System.out.println("❌ Invalid choice!");
                    return;
                }
            }

            int affected = updateStmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ Product updated!");
            }

            updateStmt.close();
            rs.close();
            checkStmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void deleteProduct() {
        System.out.print("\nEnter product ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("⚠️ Are you sure? (yes/no): ");
            String confirm = scanner.nextLine();

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("❌ Operation cancelled.");
                return;
            }

            String sql = "DELETE FROM products WHERE id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ Product deleted!");
            } else {
                System.out.println("❌ Product not found!");
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void viewProductsByCategory() {
        showCategories();
        System.out.print("\nEnter category ID: ");
        try {
            int categoryId = Integer.parseInt(scanner.nextLine());

            String sql = """
                SELECT * FROM products 
                WHERE category_id = ?
                ORDER BY name
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n" + "=".repeat(80));
            System.out.printf("%-5s | %-30s | %-10s | %-10s | %-10s%n",
                    "ID", "Name", "Price", "Tax %", "Stock");
            System.out.println("-".repeat(80));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("%-5d | %-30s | %-10.2f | %-10.2f | %-10d%n",
                        rs.getInt("id"),
                        truncate(rs.getString("name"), 30),
                        rs.getDouble("price"),
                        rs.getDouble("tax_rate"),
                        rs.getInt("stock_quantity"));
            }

            System.out.println("=".repeat(80));
            System.out.println("Products found: " + count);

            rs.close();
            pstmt.close();

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

    private int createCategory() {
        try {
            System.out.print("New category name: ");
            String name = scanner.nextLine();

            System.out.print("Description: ");
            String description = scanner.nextLine();

            String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, description);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                int id = rs.getInt(1);
                System.out.println("✅ Category created with ID: " + id);
                rs.close();
                pstmt.close();
                return id;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error creating category: " + e.getMessage());
        }
        return 0;
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }

    private boolean categoryExists(int categoryId) {
        try {
            String sql = "SELECT id FROM categories WHERE id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            pstmt.close();
            return exists;
        } catch (SQLException e) {
            System.err.println("❌ Error checking category: " + e.getMessage());
            return false;
        }
    }
}