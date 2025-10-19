package com.marketplace.product;

import com.marketplace.product.pricing.PricePolicy;
import com.marketplace.product.shipping.Shippable;
import java.util.List;

public class PhysicalProduct extends Product implements Shippable {
    private double weight;
    private double length;
    private double width;
    private double height;

    public PhysicalProduct(String id, String name, double price, double weight, double length, double width, double height) {
        super(id, name, price);
        if (!trySetWeight(weight) || !trySetDimensions(length, width, height)) {
            throw new IllegalArgumentException("Invalid physical attributes");
        }
    }

    public double getWeight() {
        return weight;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean trySetWeight(double weight) {
        if (weight > 0) {
            this.weight = weight;
            return true;
        }
        return false;
    }

    public boolean trySetDimensions(double length, double width, double height) {
        if (length > 0 && width > 0 && height > 0) {
            this.length = length;
            this.width = width;
            this.height = height;
            return true;
        }
        return false;
    }

    public double estimateShippingCost() {
        double volume = length * width * height;
        // Simple shipping cost calculation: base cost + weight-based cost + volume-based cost
        return 5.0 + (weight * 0.5) + (volume * 0.01);
    }

    @Override
    public double finalPrice(int quantity) {
        return super.finalPrice(quantity) + estimateShippingCost();
    }

    @Override
    public double finalPrice(int quantity, PricePolicy policy) {
        return super.finalPrice(quantity, policy) + estimateShippingCost();
    }

    @Override
    public double finalPrice(int quantity, List<PricePolicy> policies) {
        return super.finalPrice(quantity, policies) + estimateShippingCost();
    }
}
