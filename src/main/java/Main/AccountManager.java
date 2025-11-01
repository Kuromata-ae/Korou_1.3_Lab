package Main;

import Database.DatabaseManager;
import java.sql.*;
import java.security.MessageDigest;
import java.util.Scanner;

public class AccountManager {
    private DatabaseManager dbManager;
    private Scanner scanner;

    public AccountManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.scanner = Main.getScanner();
    }

    public void createUser() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("          CREATE USER");
        System.out.println("=".repeat(50));

        try {
            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            System.out.print("Full name: ");
            String fullName = scanner.nextLine();

            System.out.print("Phone: ");
            String phone = scanner.nextLine();

            System.out.print("Initial balance: ");
            double balance = Double.parseDouble(scanner.nextLine());

            // Hash password
            String passwordHash = hashPassword(password);

            String sql = """
                INSERT INTO users (username, email, password_hash, full_name, phone, balance) 
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, fullName);
            pstmt.setString(5, phone);
            pstmt.setDouble(6, balance);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    System.out.println("\n✅ User created!");
                    System.out.println("   ID: " + userId);
                    System.out.println("   Username: " + username);
                    System.out.println("   Email: " + email);
                    System.out.println("   Balance: " + balance + " ₸");
                }
                rs.close();
            }

            pstmt.close();

        } catch (SQLException e) {
            if (e.getMessage().contains("Unique")) {
                System.err.println("❌ User with this username or email already exists!");
            } else {
                System.err.println("❌ Error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void viewAllUsers() {
        System.out.println("\n" + "=".repeat(110));
        System.out.println("                                        ALL USERS");
        System.out.println("=".repeat(110));

        try {
            String sql = "SELECT * FROM users ORDER BY id";
            Statement stmt = dbManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.printf("%-5s | %-15s | %-25s | %-20s | %-15s | %-12s | %-8s%n",
                    "ID", "Username", "Email", "Full Name", "Phone", "Balance", "Active");
            System.out.println("-".repeat(110));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("%-5d | %-15s | %-25s | %-20s | %-15s | %,10.2f ₸ | %-8s%n",
                        rs.getInt("id"),
                        truncate(rs.getString("username"), 15),
                        truncate(rs.getString("email"), 25),
                        truncate(rs.getString("full_name"), 20),
                        truncate(rs.getString("phone"), 15),
                        rs.getDouble("balance"),
                        rs.getBoolean("is_active") ? "Yes" : "No");
            }

            System.out.println("=".repeat(110));
            System.out.println("Total users: " + count);

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void findUser() {
        System.out.println("\n1 - Find by ID");
        System.out.println("2 - Find by username");
        System.out.println("3 - Find by email");
        System.out.print("Choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            String sql = "";
            PreparedStatement pstmt;

            switch (choice) {
                case 1 -> {
                    System.out.print("ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    sql = "SELECT * FROM users WHERE id = ?";
                    pstmt = dbManager.getConnection().prepareStatement(sql);
                    pstmt.setInt(1, id);
                }
                case 2 -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine();
                    sql = "SELECT * FROM users WHERE username = ?";
                    pstmt = dbManager.getConnection().prepareStatement(sql);
                    pstmt.setString(1, username);
                }
                case 3 -> {
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    sql = "SELECT * FROM users WHERE email = ?";
                    pstmt = dbManager.getConnection().prepareStatement(sql);
                    pstmt.setString(1, email);
                }
                default -> {
                    System.out.println("❌ Invalid choice!");
                    return;
                }
            }

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("        USER INFORMATION");
                System.out.println("=".repeat(50));
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Full Name: " + rs.getString("full_name"));
                System.out.println("Phone: " + rs.getString("phone"));
                System.out.println("Balance: " + String.format("%,.2f ₸", rs.getDouble("balance")));
                System.out.println("Active: " + (rs.getBoolean("is_active") ? "Yes" : "No"));
                System.out.println("Registered: " + rs.getTimestamp("created_at"));
                System.out.println("=".repeat(50));
            } else {
                System.out.println("❌ User not found!");
            }

            rs.close();
            pstmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void updateUser() {
        System.out.print("\nEnter user ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());

            // Check existence
            String checkSql = "SELECT * FROM users WHERE id = ?";
            PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ User not found!");
                return;
            }

            System.out.println("\nCurrent data:");
            System.out.println("Username: " + rs.getString("username"));
            System.out.println("Email: " + rs.getString("email"));
            System.out.println("Full Name: " + rs.getString("full_name"));

            System.out.println("\nWhat to update?");
            System.out.println("1 - Email");
            System.out.println("2 - Full Name");
            System.out.println("3 - Phone");
            System.out.println("4 - Active status");
            System.out.print("Choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            String updateSql = "";
            PreparedStatement updateStmt;

            switch (choice) {
                case 1 -> {
                    System.out.print("New email: ");
                    String newEmail = scanner.nextLine();
                    updateSql = "UPDATE users SET email = ? WHERE id = ?";
                    updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                    updateStmt.setString(1, newEmail);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                }
                case 2 -> {
                    System.out.print("New full name: ");
                    String newName = scanner.nextLine();
                    updateSql = "UPDATE users SET full_name = ? WHERE id = ?";
                    updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                    updateStmt.setString(1, newName);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                }
                case 3 -> {
                    System.out.print("New phone: ");
                    String newPhone = scanner.nextLine();
                    updateSql = "UPDATE users SET phone = ? WHERE id = ?";
                    updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                    updateStmt.setString(1, newPhone);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                }
                case 4 -> {
                    System.out.print("Active? (true/false): ");
                    boolean isActive = Boolean.parseBoolean(scanner.nextLine());
                    updateSql = "UPDATE users SET is_active = ? WHERE id = ?";
                    updateStmt = dbManager.getConnection().prepareStatement(updateSql);
                    updateStmt.setBoolean(1, isActive);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                }
                default -> {
                    System.out.println("❌ Invalid choice!");
                    return;
                }
            }

            System.out.println("✅ User updated!");

            rs.close();
            checkStmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void deleteUser() {
        System.out.print("\nEnter user ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("⚠️ All user data will be deleted! Continue? (yes/no): ");
            String confirm = scanner.nextLine();

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("❌ Operation cancelled.");
                return;
            }

            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ User deleted!");
            } else {
                System.out.println("❌ User not found!");
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public void manageBalance() {
        System.out.print("\nEnter user ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());

            // Show current balance
            String checkSql = "SELECT username, balance FROM users WHERE id = ?";
            PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ User not found!");
                return;
            }

            String username = rs.getString("username");
            double currentBalance = rs.getDouble("balance");

            System.out.println("\nUser: " + username);
            System.out.println("Current balance: " + String.format("%,.2f ₸", currentBalance));

            System.out.println("\n1 - Add to balance (Deposit)");
            System.out.println("2 - Subtract from balance (Withdraw)");
            System.out.println("3 - Set balance");
            System.out.print("Choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine());

            String updateSql = "";
            double newBalance = currentBalance;

            switch (choice) {
                case 1 -> {
                    newBalance = currentBalance + amount;
                    updateSql = "UPDATE users SET balance = ? WHERE id = ?";
                }
                case 2 -> {
                    if (amount > currentBalance) {
                        System.out.println("❌ Insufficient funds!");
                        return;
                    }
                    newBalance = currentBalance - amount;
                    updateSql = "UPDATE users SET balance = ? WHERE id = ?";
                }
                case 3 -> {
                    newBalance = amount;
                    updateSql = "UPDATE users SET balance = ? WHERE id = ?";
                }
                default -> {
                    System.out.println("❌ Invalid choice!");
                    return;
                }
            }

            PreparedStatement updateStmt = dbManager.getConnection().prepareStatement(updateSql);
            updateStmt.setDouble(1, newBalance);
            updateStmt.setInt(2, id);
            updateStmt.executeUpdate();

            System.out.println("✅ Balance updated!");
            System.out.println("Old balance: " + String.format("%,.2f ₸", currentBalance));
            System.out.println("New balance: " + String.format("%,.2f ₸", newBalance));

            // Log transaction
            String transactionSql = """
                INSERT INTO transactions (user_id, transaction_type, amount, description, status) 
                VALUES (?, ?, ?, ?, 'COMPLETED')
            """;
            PreparedStatement tranStmt = dbManager.getConnection().prepareStatement(transactionSql);
            tranStmt.setInt(1, id);
            tranStmt.setString(2, choice == 1 ? "DEPOSIT" : choice == 2 ? "WITHDRAW" : "BALANCE_SET");
            tranStmt.setDouble(3, amount);
            tranStmt.setString(4, "Manual balance adjustment");
            tranStmt.executeUpdate();
            tranStmt.close();

            updateStmt.close();
            rs.close();
            checkStmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password; // This should not be done in a real application!
        }
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}