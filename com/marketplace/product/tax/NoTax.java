package com.marketplace.product.tax;

import com.marketplace.product.Product;

public class NoTax implements TaxPolicy {
    @Override
    public double applyTax(Product product, int quantity, double price) {
        return price;
    }
}
