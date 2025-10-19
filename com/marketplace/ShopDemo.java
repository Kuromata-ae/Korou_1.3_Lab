package com.marketplace;

import com.marketplace.product.DigitalProduct;
import com.marketplace.product.PhysicalProduct;
import com.marketplace.product.Product;
import com.marketplace.product.pricing.BogoHalfPromotion;
import com.marketplace.product.pricing.FixedPromotion;
import com.marketplace.product.pricing.PercentagePromotion;
import com.marketplace.product.pricing.PricePolicy;
import com.marketplace.product.tax.FlatVat;
import com.marketplace.product.tax.NoTax;
import com.marketplace.product.tax.TaxPolicy;

import java.util.Arrays;
import java.util.List;

public class ShopDemo {
    public static void main(String[] args) {
        System.out.println("--- Phase 1 & 2: Product Creation and Validation ---");
        Product laptop = new Product("laptop-01", "Laptop", 1200.50);
        Product smartphone = Product.of("phone-01", "Smartphone", 800.00);
        Product headphones = new Product("head-01", "Headphones", "Noise-cancelling headphones", 150.75, 50, "Electronics");
        Product freeMouse = Product.freeSample("mouse-01", "Free Mouse");

        System.out.println("Initial product count: " + Product.getInstanceCount());
        System.out.println("Product: " + laptop.getName() + ", Price: " + laptop.getPrice());
        System.out.println("Free sample: " + freeMouse.getName() + " with description: " + freeMouse.getDescription());

        // Demonstrate validation
        System.out.println("\nAttempting to set invalid price...");
        boolean priceSet = laptop.trySetPrice(-50.0);
        System.out.println("Price set successfully? " + priceSet);
        System.out.println("Laptop price remains: " + laptop.getPrice());

        System.out.println("\n--- Phase 3: Product Specialization ---");
        PhysicalProduct book = new PhysicalProduct("book-01", "The Java Guide", 45.99, 0.8, 0.2, 0.15, 0.05);
        DigitalProduct software = new DigitalProduct("soft-01", "IDE Pro", 99.99, 512, "LICENSE-KEY-12345");

        System.out.println("Physical Product: " + book.getName() + ", Weight: " + book.getWeight() + "kg");
        System.out.println("Shipping cost for book: " + book.estimateShippingCost());
        System.out.println("Digital Product: " + software.getName() + ", Download Size: " + software.getDownloadSizeMb() + "MB");
        System.out.println("Total products created: " + Product.getInstanceCount());

        System.out.println("\n--- Phase 4 & 5: Pricing, Promotions, and Taxes ---");
        // Promotions
        PricePolicy tenPercentOff = new PercentagePromotion(10);
        PricePolicy fiveDollarsOff = new FixedPromotion(5);
        PricePolicy bogo = new BogoHalfPromotion();
        List<PricePolicy> promotions = Arrays.asList(tenPercentOff, fiveDollarsOff, bogo);

        // Taxes
        TaxPolicy noTax = new NoTax();
        TaxPolicy vat = new FlatVat(20); // 20% VAT

        // --- Laptop Pricing ---
        System.out.println("\n--- Laptop (Standard Product) ---");
        int laptopQuantity = 2;
        System.out.println("Laptop base price for " + laptopQuantity + ": " + laptop.finalPrice(laptopQuantity));
        System.out.println("Laptop with 10% off: " + laptop.finalPrice(laptopQuantity, tenPercentOff));
        System.out.println("Laptop with BOGO: " + laptop.finalPrice(laptopQuantity, bogo));
        System.out.println("Laptop best price from promotions: " + laptop.finalPrice(laptopQuantity, promotions));
        System.out.println("Laptop best price with 20% VAT: " + laptop.finalPrice(laptopQuantity, promotions, vat));

        // --- Book Pricing (Physical) ---
        System.out.println("\n--- Book (Physical Product) ---");
        int bookQuantity = 3;
        System.out.println("Book base price for " + bookQuantity + " (incl. shipping): " + book.finalPrice(bookQuantity));
        System.out.println("Book with BOGO (incl. shipping): " + book.finalPrice(bookQuantity, bogo));
        System.out.println("Book best price (incl. shipping) with No Tax: " + book.finalPrice(bookQuantity, promotions, noTax));

        // --- Software Pricing (Digital) ---
        System.out.println("\n--- Software (Digital Product) ---");
        int softwareQuantity = 2;
        System.out.println("Software base price for " + softwareQuantity + ": " + software.finalPrice(softwareQuantity));
        System.out.println("Software with BOGO (should be ignored): " + software.finalPrice(softwareQuantity, bogo));
        System.out.println("Software best price from promotions: " + software.finalPrice(softwareQuantity, promotions));
        System.out.println("Software best price with 20% VAT: " + software.finalPrice(softwareQuantity, promotions, vat));
    }
}
