package com.marketplace.product.pricing;

import com.marketplace.product.Product;

public interface PricePolicy {
    double calculatePrice(Product product, int quantity);
}
