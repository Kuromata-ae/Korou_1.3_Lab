package Database;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:h2:./marketplace_db";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "admin123";

    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ The database connection has been successfully established!");
        } catch (Exception e) {
            System.err.println("❌ Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void initDatabase() {
        try {
            createTables();
            System.out.println("✅ The database tables are initialized!");
        } catch (SQLException e) {
            System.err.println("❌ DATABASE initialization error: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Таблица категорий
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS categories (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL UNIQUE,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Таблица продуктов
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS products (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(200) NOT NULL,
                description TEXT,
                price DECIMAL(10,2) NOT NULL,
                tax_rate DECIMAL(5,2) DEFAULT 0.00,
                category_id INT,
                product_type VARCHAR(20) NOT NULL,
                stock_quantity INT DEFAULT 0,
                weight DECIMAL(10,2),
                file_size DECIMAL(10,2),
                download_url VARCHAR(500),
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
            )
        """);

        // Таблица пользователей
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                full_name VARCHAR(150),
                phone VARCHAR(20),
                balance DECIMAL(10,2) DEFAULT 0.00,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Таблица платежей
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS payments (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                product_id INT NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                tax_amount DECIMAL(10,2) NOT NULL,
                total_amount DECIMAL(10,2) NOT NULL,
                payment_method VARCHAR(50) NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                transaction_id VARCHAR(100) UNIQUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
            )
        """);

        // Таблица скидок на продукты
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS product_discounts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                product_id INT NOT NULL,
                discount_percent DECIMAL(5,2) NOT NULL,
                start_date TIMESTAMP NOT NULL,
                end_date TIMESTAMP NOT NULL,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
            )
        """);

        // Таблица скидок на категории
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS category_discounts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                category_id INT NOT NULL,
                discount_percent DECIMAL(5,2) NOT NULL,
                start_date TIMESTAMP NOT NULL,
                end_date TIMESTAMP NOT NULL,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
            )
        """);

        // Таблица стран
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS countries (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL UNIQUE,
                code VARCHAR(3) NOT NULL UNIQUE,
                shipping_cost DECIMAL(10,2) DEFAULT 0.00,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Таблица городов
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS cities (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                country_id INT NOT NULL,
                additional_cost DECIMAL(10,2) DEFAULT 0.00,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (country_id) REFERENCES countries(id) ON DELETE CASCADE
            )
        """);

        // Таблица доставок
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS deliveries (
                id INT AUTO_INCREMENT PRIMARY KEY,
                payment_id INT NOT NULL,
                country_id INT NOT NULL,
                city_id INT,
                address TEXT NOT NULL,
                delivery_cost DECIMAL(10,2) NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                tracking_number VARCHAR(100) UNIQUE,
                estimated_delivery DATE,
                delivered_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
                FOREIGN KEY (country_id) REFERENCES countries(id),
                FOREIGN KEY (city_id) REFERENCES cities(id)
            )
        """);

        // Таблица транзакций
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS transactions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                transaction_type VARCHAR(50) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                description TEXT,
                reference_id INT,
                status VARCHAR(20) DEFAULT 'COMPLETED',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """);

        stmt.close();
    }

    public void showAllTables() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            System.out.println("\n" + "=".repeat(50));
            System.out.println("           LIST OF TABLES IN THE DATABASE");
            System.out.println("=".repeat(50));

            int count = 0;
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (!tableName.startsWith("INFORMATION_SCHEMA")) {
                    count++;
                    System.out.printf("%d. %s%n", count, tableName);

                    // Показать количество записей
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                    if (rs.next()) {
                        System.out.printf("   └─ Records: %d%n", rs.getInt(1));
                    }
                    rs.close();
                    stmt.close();
                }
            }
            System.out.println("=".repeat(50));

            tables.close();
        } catch (SQLException e) {
            System.err.println("❌ Mistake: " + e.getMessage());
        }
    }

    public void clearAllTables() {
        try {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            System.out.print("\n⚠️ Are you sure? All data will be deleted! (yes/no): ");
            String confirm = scanner.nextLine();

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("❌ The operation was canceled.");
                return;
            }

            Statement stmt = connection.createStatement();

            // Отключаем проверку внешних ключей
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // Очищаем все таблицы
            String[] tables = {"transactions", "deliveries", "payments", "product_discounts",
                    "category_discounts", "products", "categories", "users",
                    "cities", "countries"};

            for (String table : tables) {
                stmt.execute("TRUNCATE TABLE " + table);
                System.out.println("✅ Table " + table + " cleared");
            }

            // Включаем обратно проверку внешних ключей
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");

            stmt.close();
            System.out.println("✅ All tables are cleared!");

        } catch (SQLException e) {
            System.err.println("❌Mistake: " + e.getMessage());
        }
    }

    public void resetDatabase() {
        try {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            System.out.print("\n⚠️⚠️⚠️ Attention! The database will be completely deleted! (yes/no): ");
            String confirm = scanner.nextLine();

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("❌ The operation was canceled.");
                return;
            }

            Statement stmt = connection.createStatement();
            stmt.execute("DROP ALL OBJECTS");
            stmt.close();

            System.out.println("✅ The database has been reset!");
            System.out.println("🔄 Reinitialization...");

            initDatabase();

        } catch (SQLException e) {
            System.err.println("❌ Mistake: " + e.getMessage());
        }
    }

    public void exportData() {
        System.out.println("\n📤 Data export is under development...");
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ The database connection is closed.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Connection closing error:" + e.getMessage());
        }
    }
}