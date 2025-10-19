package com.marketplace.product.pricing;

import com.marketplace.product.Product;

public class PercentagePromotion extends Promotion {
    private final double percentage;

    public PercentagePromotion(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100.");
        }
        this.percentage = percentage;
    }

    @Override
    protected boolean isApplicable(Product product, int quantity) {
        return true; // Applicable to all products
    }

    @Override
    protected double applyDiscount(Product product, int quantity) {
        return product.getPrice() * quantity * (1 - percentage / 100.0);
    }
}
