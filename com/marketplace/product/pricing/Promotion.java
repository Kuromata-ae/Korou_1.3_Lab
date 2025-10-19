package com.marketplace.product.pricing;

import com.marketplace.product.Product;

public abstract class Promotion implements PricePolicy {

    @Override
    public double calculatePrice(Product product, int quantity) {
        if (isApplicable(product, quantity)) {
            return applyDiscount(product, quantity);
        }
        return product.getPrice() * quantity;
    }

    protected abstract boolean isApplicable(Product product, int quantity);
    protected abstract double applyDiscount(Product product, int quantity);
}
