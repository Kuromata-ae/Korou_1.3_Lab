package product.tax;

import product.Product;

public class NoTax implements TaxPolicy {
    @Override
    public double applyTax(double priceBeforeTax, Product product) {
        return priceBeforeTax;
    }
}
