package product.pricing;

import product.Product;

public class FixedPromotion extends Promotion {
    private final double discountAmount;

    public FixedPromotion(String name, double discountAmount) {
        super(name);
        this.discountAmount = discountAmount;
    }

    @Override
    protected double calculateDiscount(Product product, int quantity) {
        return discountAmount * quantity;
    }

    @Override
    protected boolean isApplicable(Product product) {
        return true;
    }
}
