package product.tax;

import product.DigitalProduct;
import product.Product;

public class ReducedDigitalVat implements TaxPolicy {
    private final double vatRate;
    private final double digitalVatRate;

    public ReducedDigitalVat(double vatRate, double digitalVatRate) {
        this.vatRate = vatRate;
        this.digitalVatRate = digitalVatRate;
    }

    @Override
    public double applyTax(double priceBeforeTax, Product product) {
        if (product instanceof DigitalProduct) {
            return priceBeforeTax * (1 + digitalVatRate / 100);
        }
        return priceBeforeTax * (1 + vatRate / 100);
    }
}
