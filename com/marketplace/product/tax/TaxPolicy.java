package com.marketplace.product.tax;

import com.marketplace.product.Product;

public interface TaxPolicy {
    double applyTax(Product product, int quantity, double price);
}
