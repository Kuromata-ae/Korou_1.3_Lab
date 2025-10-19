package com.marketplace.product.pricing;

import com.marketplace.product.Product;

public class BogoHalfPromotion extends Promotion {
    @Override
    protected boolean isApplicable(Product product, int quantity) {
        return quantity >= 2;
    }

    @Override
    protected double applyDiscount(Product product, int quantity) {
        int fullPriceItems = (quantity / 2) + (quantity % 2);
        int halfPriceItems = quantity / 2;
        return (fullPriceItems * product.getPrice()) + (halfPriceItems * product.getPrice() * 0.5);
    }
}
