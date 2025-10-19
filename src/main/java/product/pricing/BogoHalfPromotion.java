package product.pricing;

import product.DigitalProduct;
import product.Product;

public class BogoHalfPromotion extends Promotion {
    public BogoHalfPromotion(String name) {
        super(name);
    }

    @Override
    protected double calculateDiscount(Product product, int quantity) {
        if (quantity < 2) {
            return 0;
        }
        int pairs = quantity / 2;
        return pairs * (product.getPrice() / 2);
    }

    @Override
    protected boolean isApplicable(Product product) {
        return !(product instanceof DigitalProduct);
    }
}
