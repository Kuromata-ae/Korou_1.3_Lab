package com.marketplace.product.pricing;

import com.marketplace.product.Product;

public class FixedPromotion extends Promotion {
    private final double amount;

    public FixedPromotion(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative.");
        }
        this.amount = amount;
    }

    @Override
    protected boolean isApplicable(Product product, int quantity) {
        return product.getPrice() * quantity >= amount;
    }

    @Override
    protected double applyDiscount(Product product, int quantity) {
        return product.getPrice() * quantity - amount;
    }
}
