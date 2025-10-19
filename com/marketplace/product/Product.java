package com.marketplace.product;

import com.marketplace.product.pricing.PricePolicy;
import com.marketplace.product.tax.TaxPolicy;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String category;

    private static int instanceCount = 0;

    public Product(String id, String name, String description, double price, int quantity, String category) {
        if (!trySetId(id)) {
            throw new IllegalArgumentException("Invalid id");
        }
        if (!trySetName(name)) {
            throw new IllegalArgumentException("Invalid name");
        }
        if (!trySetDescription(description)) {
            throw new IllegalArgumentException("Invalid description");
        }
        if (!trySetPrice(price)) {
            throw new IllegalArgumentException("Invalid price");
        }
        if (!trySetQuantity(quantity)) {
            throw new IllegalArgumentException("Invalid quantity");
        }
        if (!trySetCategory(category)) {
            throw new IllegalArgumentException("Invalid category");
        }
        instanceCount++;
    }

    public Product(String id, String name, double price) {
        this(id, name, "No description available.", price, 0, "Uncategorized");
    }

    public Product() {
        this("default-id", "Default Product", 0.0);
    }

    public static int getInstanceCount() {
        return instanceCount;
    }

    public static Product of(String id, String name, double price) {
        return new Product(id, name, price);
    }

    public static Product freeSample(String id, String name) {
        Product sample = new Product(id, name, 0.0);
        sample.trySetDescription("This is a free sample product.");
        return sample;
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

    public String getCategory() {
        return category;
    }

    public boolean trySetId(String id) {
        if (id != null && !id.trim().isEmpty()) {
            this.id = id;
            return true;
        }
        return false;
    }

    public boolean trySetName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
            return true;
        }
        return false;
    }

    public boolean trySetDescription(String description) {
        if (description != null) {
            this.description = description;
            return true;
        }
        return false;
    }

    public boolean trySetPrice(double price) {
        if (price >= 0) {
            this.price = price;
            return true;
        }
        return false;
    }

    public boolean trySetQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
            return true;
        }
        return false;
    }

    public boolean trySetCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            this.category = category;
            return true;
        }
        return false;
    }

    public double finalPrice(int quantity) {
        return getPrice() * quantity;
    }

    public double finalPrice(int quantity, PricePolicy policy) {
        return policy.calculatePrice(this, quantity);
    }

    public double finalPrice(int quantity, List<PricePolicy> policies) {
        double bestPrice = finalPrice(quantity);
        for (PricePolicy policy : policies) {
            bestPrice = Math.min(bestPrice, finalPrice(quantity, policy));
        }
        return bestPrice;
    }

    public double finalPrice(int quantity, List<PricePolicy> policies, TaxPolicy taxPolicy) {
        double price = finalPrice(quantity, policies);
        return taxPolicy.applyTax(this, quantity, price);
    }
}
