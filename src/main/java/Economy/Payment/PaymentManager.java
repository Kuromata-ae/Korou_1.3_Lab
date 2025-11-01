package Economy.Payment;

import Database.DatabaseManager;
import Main.Main;
import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class PaymentManager {
    private DatabaseManager dbManager;
    private Scanner scanner;

    public PaymentManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.scanner = Main.getScanner();
    }

    public void createPayment() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("            CREATE PAYMENT");
        System.out.println("=".repeat(50));

        try {
            System.out.print("User ID: ");
            int userId = Integer.parseInt(scanner.nextLine());

            // Check user and balance
            String userSql = "SELECT username, balance FROM users WHERE id = ?";
            PreparedStatement userStmt = dbManager.getConnection().prepareStatement(userSql);
            userStmt.setInt(1, userId);
            ResultSet userRs = userStmt.executeQuery();

            if (!userRs.next()) {
                System.out.println("❌ User not found!");
                return;
            }

            String username = userRs.getString("username");
            double userBalance = userRs.getDouble("balance");
            System.out.println("User: " + username);
            System.out.println("Balance: " + String.format("%,.2f ₸", userBalance));

            System.out.print("\nProduct ID: ");
            int productId = Integer.parseInt(scanner.nextLine());

            // Get product info
            String productSql = """
                SELECT name, price, tax_rate, stock_quantity, is_active 
                FROM products WHERE id = ?
            """;
            PreparedStatement productStmt = dbManager.getConnection().prepareStatement(productSql);
            productStmt.setInt(1, productId);
            ResultSet productRs = productStmt.executeQuery();

            if (!productRs.next()) {
                System.out.println("❌ Product not found!");
                return;
            }

            String productName = productRs.getString("name");
            double price = productRs.getDouble("price");
            double taxRate = productRs.getDouble("tax_rate");
            int stock = productRs.getInt("stock_quantity");
            boolean isActive = productRs.getBoolean("is_active");

            if (!isActive) {
                System.out.println("❌ Product is not active!");
                return;
            }

            if (stock <= 0) {
                System.out.println("❌ Product is out of stock!");
                return;
            }

            System.out.println("\nProduct: " + productName);
            System.out.println("Price: " + String.format("%,.2f ₸", price));
            System.out.println("Tax: " + taxRate + "%");

            System.out.print("Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            if (quantity > stock) {
                System.out.println("❌ Not enough stock! Available: " + stock);
                return;
            }

            // Calculate amount
            double amount = price * quantity;
            double taxAmount = amount * taxRate / 100;
            double totalAmount = amount + taxAmount;

            System.out.println("\n" + "-".repeat(50));
            System.out.println("Subtotal: " + String.format("%,.2f ₸", amount));
            System.out.println("Tax: " + String.format("%,.2f ₸", taxAmount));
            System.out.println("TOTAL: " + String.format("%,.2f ₸", totalAmount));
            System.out.println("-".repeat(50));

            if (totalAmount > userBalance) {
                System.out.println("❌ Insufficient funds!");
                System.out.println("Short by: " + String.format("%,.2f ₸", totalAmount - userBalance));
                return;
            }

            System.out.println("\nPayment method:");
            System.out.println("1 - Balance");
            System.out.println("2 - Credit Card");
            System.out.println("3 - Deposit");
            System.out.print("Choice: ");
            int paymentChoice = Integer.parseInt(scanner.nextLine());

            String paymentMethod = switch (paymentChoice) {
                case 1 -> "BALANCE";
                case 2 -> "CREDIT_CARD";
                case 3 -> "DEPOSIT";
                default -> "BALANCE";
            };

            System.out.print("\n✅ Confirm payment? (yes/no): ");
            String confirm = scanner.nextLine();

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("❌ Payment cancelled.");
                return;
            }

            // Create payment
            String transactionId = UUID.randomUUID().toString();
            String paymentSql = """
                INSERT INTO payments (user_id, product_id, amount, tax_amount, total_amount, 
                                    payment_method, status, transaction_id) 
                VALUES (?, ?, ?, ?, ?, ?, 'COMPLETED', ?)
            """;

            PreparedStatement paymentStmt = dbManager.getConnection().prepareStatement(paymentSql, Statement.RETURN_GENERATED_KEYS);
            paymentStmt.setInt(1, userId);
            paymentStmt.setInt(2, productId);
            paymentStmt.setDouble(3, amount);
            paymentStmt.setDouble(4, taxAmount);
            paymentStmt.setDouble(5, totalAmount);
            paymentStmt.setString(6, paymentMethod);
            paymentStmt.setString(7, transactionId);

            int affected = paymentStmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = paymentStmt.getGeneratedKeys();
                if (rs.next()) {
                    int paymentId = rs.getInt(1);

                    // Deduct from balance
                    String updateBalanceSql = "UPDATE users SET balance = balance - ? WHERE id = ?";
                    PreparedStatement balanceStmt = dbManager.getConnection().prepareStatement(updateBalanceSql);
                    balanceStmt.setDouble(1, totalAmount);
                    balanceStmt.setInt(2, userId);
                    balanceStmt.executeUpdate();
                    balanceStmt.close();

                    // Decrease stock
                    String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";
                    PreparedStatement stockStmt = dbManager.getConnection().prepareStatement(updateStockSql);
                    stockStmt.setInt(1, quantity);
                    stockStmt.setInt(2, productId);
                    stockStmt.executeUpdate();
                    stockStmt.close();

                    // Create transaction
                    String tranSql = """
                        INSERT INTO transactions (user_id, transaction_type, amount, description, reference_id, status) 
                        VALUES (?, 'PAYMENT', ?, ?, ?, 'COMPLETED')
                    """;
                    PreparedStatement tranStmt = dbManager.getConnection().prepareStatement(tranSql);
                    tranStmt.setInt(1, userId);
                    tranStmt.setDouble(2, totalAmount);
                    tranStmt.setString(3, "Payment for " + productName + " (x" + quantity + ")");
                    tranStmt.setInt(4, paymentId);
                    tranStmt.executeUpdate();
                    tranStmt.close();

                    System.out.println("\n✅ Payment completed successfully!");
                    System.out.println("Payment ID: " + paymentId);
                    System.out.println("Transaction: " + transactionId);
                    System.out.println("New balance: " + String.format("%,.2f ₸", userBalance - totalAmount));
                }
                rs.close();
            }

            paymentStmt.close();
            productRs.close();
            productStmt.close();
            userRs.close();
            userStmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void viewAllPayments() {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("                                           ALL PAYMENTS");
        System.out.println("=".repeat(120));

        try {
            String sql = """
                SELECT p.id, p.transaction_id, u.username, pr.name as product_name, 
                       p.amount, p.tax_amount, p.total_amount, p.payment_method, p.status, p.created_at
                FROM payments p
                JOIN users u ON p.user_id = u.id
                JOIN products pr ON p.product_id = pr.id
                ORDER BY p.created_at DESC
            """;

            Statement stmt = dbManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.printf("%-5s | %-15s | %-20s | %-15s | %-12s | %-12s | %-10s | %-20s%n",
                    "ID", "User", "Transaction", "Method", "Amount", "Total", "Status", "Date");
            System.out.println("-".repeat(120));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("%-5d | %-15s | %-20s | %-15s | %,10.2f ₸ | %,10.2f ₸ | %-10s | %s%n",
                        rs.getInt("id"),
                        truncate(rs.getString("username"), 15),
                        truncate(rs.getString("transaction_id"), 20),
                        rs.getString("payment_method"),
                        rs.getDouble("amount"),
                        rs.getDouble("total_amount"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at"));
            }

            System.out.println("=".repeat(120));
            System.out.println("Total payments: " + count);

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void viewUserPayments() {
        System.out.print("\nEnter user ID: ");
        try {
            int userId = Integer.parseInt(scanner.nextLine());

            String sql = """
                SELECT p.id, pr.name as product_name, p.amount, p.tax_amount, 
                       p.total_amount, p.payment_method, p.status, p.created_at
                FROM payments p
                JOIN products pr ON p.product_id = pr.id
                WHERE p.user_id = ?
                ORDER BY p.created_at DESC
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n" + "=".repeat(100));
            System.out.printf("%-5s | %-30s | %-12s | %-12s | %-15s | %-20s%n",
                    "ID", "Product", "Amount", "Total", "Method", "Date");
            System.out.println("-".repeat(100));

            int count = 0;
            double totalSpent = 0;

            while (rs.next()) {
                count++;
                double total = rs.getDouble("total_amount");
                totalSpent += total;

                System.out.printf("%-5d | %-30s | %,10.2f ₸ | %,10.2f ₸ | %-15s | %s%n",
                        rs.getInt("id"),
                        truncate(rs.getString("product_name"), 30),
                        rs.getDouble("amount"),
                        total,
                        rs.getString("payment_method"),
                        rs.getTimestamp("created_at"));
            }

            System.out.println("=".repeat(100));
            System.out.println("Payments: " + count);
            System.out.println("Total spent: " + String.format("%,.2f ₸", totalSpent));

            rs.close();
            pstmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }



    public void refundPayment() {
        System.out.print("\nEnter payment ID to refund: ");
        try {
            int paymentId = Integer.parseInt(scanner.nextLine());

            String sql = """
                SELECT p.*, pr.name as product_name, u.username
                FROM payments p
                JOIN products pr ON p.product_id = pr.id
                JOIN users u ON p.user_id = u.id
                WHERE p.id = ?
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, paymentId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Payment not found!");
                return;
            }

            String status = rs.getString("status");
            if ("REFUNDED".equals(status)) {
                System.out.println("❌ Payment already refunded!");
                return;
            }

            int userId = rs.getInt("user_id");
            int productId = rs.getInt("product_id");
            double totalAmount = rs.getDouble("total_amount");
            String productName = rs.getString("product_name");
            String username = rs.getString("username");

            System.out.println("\nPayment Information:");
            System.out.println("User: " + username);
            System.out.println("Product: " + productName);
            System.out.println("Refund amount: " + String.format("%,.2f ₸", totalAmount));

            System.out.print("\n⚠️ Confirm refund? (yes/no): ");
            String confirm = scanner.nextLine();

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("❌ Operation cancelled.");
                return;
            }

            // Update payment status
            String updatePaymentSql = "UPDATE payments SET status = 'REFUNDED' WHERE id = ?";
            PreparedStatement updateStmt = dbManager.getConnection().prepareStatement(updatePaymentSql);
            updateStmt.setInt(1, paymentId);
            updateStmt.executeUpdate();
            updateStmt.close();

            // Return money
            String updateBalanceSql = "UPDATE users SET balance = balance + ? WHERE id = ?";
            PreparedStatement balanceStmt = dbManager.getConnection().prepareStatement(updateBalanceSql);
            balanceStmt.setDouble(1, totalAmount);
            balanceStmt.setInt(2, userId);
            balanceStmt.executeUpdate();
            balanceStmt.close();

            // Return item to stock
            String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity + 1 WHERE id = ?";
            PreparedStatement stockStmt = dbManager.getConnection().prepareStatement(updateStockSql);
            stockStmt.setInt(1, productId);
            stockStmt.executeUpdate();
            stockStmt.close();

            // Create refund transaction
            String tranSql = """
                INSERT INTO transactions (user_id, transaction_type, amount, description, reference_id, status) 
                VALUES (?, 'REFUND', ?, ?, ?, 'COMPLETED')
            """;
            PreparedStatement tranStmt = dbManager.getConnection().prepareStatement(tranSql);
            tranStmt.setInt(1, userId);
            tranStmt.setDouble(2, totalAmount);
            tranStmt.setString(3, "Refund for " + productName);
            tranStmt.setInt(4, paymentId);
            tranStmt.executeUpdate();
            tranStmt.close();

            System.out.println("\n✅ Refund processed successfully!");
            System.out.println("Funds have been returned to the user's balance");

            rs.close();
            pstmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}