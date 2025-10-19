package com.marketplace.product.tax;

import com.marketplace.product.Product;

public class FlatVat implements TaxPolicy {
    private final double rate;

    public FlatVat(double rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("Rate must be non-negative.");
        }
        this.rate = rate;
    }

    @Override
    public double applyTax(Product product, int quantity, double price) {
        return price * (1 + rate / 100.0);
    }
}
