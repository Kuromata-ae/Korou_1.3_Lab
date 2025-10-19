package product;

import java.util.List;
import product.pricing.PricePolicy;
import product.tax.TaxPolicy;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private Category category;

    private static int SEQ = 0;
    private static int createdCount = 0;

    public Product() {
        this("AUTO-" + ++SEQ, "Unnamed", 0.0);
    }

    public Product(String id, String name, double price) {
        this(id, name, null, price, 0, null);
    }

    public Product(String id, String name, String description, double price, int quantity, Category category) {
        this.id = id;
        if (!trySetName(name)) {
            this.name = "Unnamed";
        }
        if (!trySetDescription(description)) {
            this.description = null;
        }
        if (!trySetPrice(price)) {
            this.price = 0.0;
        }
        if (quantity >= 0) {
            this.quantity = quantity;
        } else {
            this.quantity = 0;
        }
        this.category = category;
        createdCount++;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public Category getCategory() {
        return category;
    }

    public static int getCreatedCount() {
        return createdCount;
    }

    public boolean trySetName(String name) {
        if (name != null && name.trim().length() >= 2) {
            this.name = name;
            return true;
        }
        return false;
    }

    public boolean trySetDescription(String description) {
        if (description == null || description.trim().length() <= 200) {
            this.description = description;
            return true;
        }
        return false;
    }

    public boolean trySetPrice(double price) {
        if (price >= 0.0 && price <= 1000000.0) {
            this.price = price;
            return true;
        }
        return false;
    }

    public boolean addStock(int amount) {
        if (amount > 0) {
            this.quantity += amount;
            return true;
        }
        return false;
    }

    public boolean sellProduct(int amount) {
        if (amount > 0 && amount <= this.quantity) {
            this.quantity -= amount;
            return true;
        }
        return false;
    }

    public static Product of(String id, String name, double price) {
        return new Product(id, name, price);
    }

    public static Product freeSample(String name) {
        Product product = new Product();
        product.trySetName(name);
        product.trySetPrice(0.0);
        product.addStock(1);
        return product;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", category=" + (category != null ? category.getName() : "null") +
                '}';
    }

    public double finalPrice(int quantity) {
        if (quantity <= 0) {
            return 0.0;
        }
        return this.price * quantity;
    }

    public double finalPrice(int quantity, PricePolicy promotion, TaxPolicy tax) {
        if (quantity <= 0) {
            return 0.0;
        }
        double discountedPrice = promotion.calculatePrice(this, quantity);
        return tax.applyTax(discountedPrice, this);
    }

    public double finalPrice(int quantity, List<PricePolicy> promotions, TaxPolicy tax) {
        if (quantity <= 0) {
            return 0.0;
        }
        double bestPrice = this.price * quantity;
        for (PricePolicy promotion : promotions) {
            double currentPrice = promotion.calculatePrice(this, quantity);
            if (currentPrice < bestPrice) {
                bestPrice = currentPrice;
            }
        }
        return tax.applyTax(bestPrice, this);
    }
}
