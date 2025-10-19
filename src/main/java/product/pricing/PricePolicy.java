package product.pricing;

import product.Product;

public interface PricePolicy {
    double calculatePrice(Product product, int quantity);
    String name();
}
