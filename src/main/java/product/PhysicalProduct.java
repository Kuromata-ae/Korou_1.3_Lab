package product;

import product.shipping.Shippable;
import java.util.List;
import product.pricing.PricePolicy;
import product.tax.TaxPolicy;

public class PhysicalProduct extends Product implements Shippable {
    private double weightKg;
    private double lengthCm;
    private double widthCm;
    private double heightCm;

    public PhysicalProduct() {
        super();
    }

    public PhysicalProduct(String id, String name, double price, double weightKg, double lengthCm, double widthCm, double heightCm) {
        super(id, name, price);
        trySetWeightKg(weightKg);
        trySetDimensions(lengthCm, widthCm, heightCm);
    }

    public PhysicalProduct(String id, String name, String description, double price, int quantity, Category category, double weightKg, double lengthCm, double widthCm, double heightCm) {
        super(id, name, description, price, quantity, category);
        trySetWeightKg(weightKg);
        trySetDimensions(lengthCm, widthCm, heightCm);
    }

    public boolean trySetWeightKg(double weightKg) {
        if (weightKg > 0) {
            this.weightKg = weightKg;
            return true;
        }
        return false;
    }

    public boolean trySetDimensions(double lengthCm, double widthCm, double heightCm) {
        if (lengthCm > 0 && widthCm > 0 && heightCm > 0) {
            this.lengthCm = lengthCm;
            this.widthCm = widthCm;
            this.heightCm = heightCm;
            return true;
        }
        return false;
    }

    @Override
    public double estimateShippingCost() {
        // A simple formula for shipping cost
        return weightKg * 1.5 + (lengthCm * widthCm * heightCm) / 5000;
    }

    @Override
    public String toString() {
        return super.toString() + " | PhysicalProduct{" +
                "weightKg=" + weightKg +
                ", lengthCm=" + lengthCm +
                ", widthCm=" + widthCm +
                ", heightCm=" + heightCm +
                '}';
    }

    @Override
    public double finalPrice(int quantity) {
        return super.finalPrice(quantity) + estimateShippingCost();
    }

    @Override
    public double finalPrice(int quantity, PricePolicy promotion, TaxPolicy tax) {
        return super.finalPrice(quantity, promotion, tax) + estimateShippingCost();
    }

    @Override
    public double finalPrice(int quantity, List<PricePolicy> promotions, TaxPolicy tax) {
        return super.finalPrice(quantity, promotions, tax) + estimateShippingCost();
    }
}
