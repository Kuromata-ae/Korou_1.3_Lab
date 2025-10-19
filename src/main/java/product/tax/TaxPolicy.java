package product.tax;
import product.Product;

public interface TaxPolicy {
    double applyTax(double priceBeforeTax, Product product);
}
