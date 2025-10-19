package product.tax;

import product.Product;

public class FlatVat implements TaxPolicy {
    private final double vatRate;

    public FlatVat(double vatRate) {
        this.vatRate = vatRate;
    }

    @Override
    public double applyTax(double priceBeforeTax, Product product) {
        return priceBeforeTax * (1 + vatRate / 100);
    }
}
