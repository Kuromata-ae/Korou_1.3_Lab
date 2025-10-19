package product;

import product.pricing.*;
import product.tax.*;
import java.util.ArrayList;
import java.util.List;

public class ShopDemo6 {
    public static void main(String[] args) {
        // Object Creation
        List<Product> products = new ArrayList<>();
        products.add(new PhysicalProduct("P001", "Laptop", "A powerful laptop", 1200.00, 1, new Category("Electronics"), 2.5, 35, 25, 2));
        products.add(new DigitalProduct("D001", "Software License", "A license for a software", 150.00, 1, new Category("Software"), 500, "ABC-123-XYZ"));

        // Policy Creation
        List<PricePolicy> promotions = new ArrayList<>();
        promotions.add(new PercentagePromotion("10% Off", 10));
        promotions.add(new FixedPromotion("50 Off", 50));
        promotions.add(new BogoHalfPromotion("BOGO Half Off"));

        TaxPolicy flatVat = new FlatVat(12);
        TaxPolicy reducedDigitalVat = new ReducedDigitalVat(12, 7);
        TaxPolicy noTax = new NoTax();

        // Iterative Testing
        for (Product product : products) {
            for (int quantity = 1; quantity <= 2; quantity++) {
                System.out.println("======================================================");
                System.out.println("Product: " + product.getName() + " | Base Price: " + product.getPrice() + " | Quantity: " + quantity);
                System.out.println("------------------------------------------------------");

                // Individual Promotions
                for (PricePolicy promotion : promotions) {
                    double finalPrice = product.finalPrice(quantity, promotion, flatVat);
                    System.out.printf("Final Price with %s and Flat VAT: %.2f\n", promotion.name(), finalPrice);
                }

                // Best of all promotions
                double bestPrice = product.finalPrice(quantity, promotions, flatVat);
                System.out.printf("Final Price with best promotion and Flat VAT: %.2f\n", bestPrice);

                // Demonstrate ReducedDigitalVat
                if (product instanceof DigitalProduct) {
                    double reducedVatPrice = product.finalPrice(quantity, promotions, reducedDigitalVat);
                    System.out.printf("Final Price with best promotion and Reduced Digital VAT: %.2f\n", reducedVatPrice);
                }
                 System.out.println("======================================================\n");
            }
        }

        // Final Count
        System.out.println("Total products created: " + Product.getCreatedCount());
    }
}
