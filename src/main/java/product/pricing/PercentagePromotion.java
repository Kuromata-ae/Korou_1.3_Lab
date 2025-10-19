package product.pricing;

import product.Product;

public class PercentagePromotion extends Promotion {
    private final double percentage;

    public PercentagePromotion(String name, double percentage) {
        super(name);
        this.percentage = percentage;
    }

    @Override
    protected double calculateDiscount(Product product, int quantity) {
        return product.getPrice() * quantity * (percentage / 100);
    }

    @Override
    protected boolean isApplicable(Product product) {
        return true;
    }
}
