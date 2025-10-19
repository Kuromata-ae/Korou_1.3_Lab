package com.marketplace.product.shipping;

public interface Shippable {
    double getWeight();
    double getLength();
    double getWidth();
    double getHeight();
    double estimateShippingCost();
}
