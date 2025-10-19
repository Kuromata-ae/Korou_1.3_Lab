package product.pricing;

import product.Product;

public abstract class Promotion implements PricePolicy {
    private final String name;

    public Promotion(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public final double calculatePrice(Product product, int quantity) {
        if (quantity <= 0) {
            return 0.0;
        }

        if (!isApplicable(product)) {
            return product.getPrice() * quantity;
        }

        double discount = calculateDiscount(product, quantity);
        double finalPrice = (product.getPrice() * quantity) - discount;
        return finalPrice > 0 ? finalPrice : 0;
    }

    protected abstract double calculateDiscount(Product product, int quantity);
    protected abstract boolean isApplicable(Product product);
}
