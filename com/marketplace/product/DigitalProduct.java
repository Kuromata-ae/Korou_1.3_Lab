package com.marketplace.product;

import com.marketplace.product.pricing.BogoHalfPromotion;
import com.marketplace.product.pricing.PricePolicy;
import java.util.List;

public class DigitalProduct extends Product {
    private double downloadSizeMb;
    private String licenseKey;

    public DigitalProduct(String id, String name, double price, double downloadSizeMb, String licenseKey) {
        super(id, name, price);
        if (!trySetDownloadSizeMb(downloadSizeMb)) {
            throw new IllegalArgumentException("Invalid download size");
        }
        trySetLicenseKey(licenseKey); // licenseKey is optional, so no exception
    }

    public DigitalProduct(String id, String name, double price, double downloadSizeMb) {
        this(id, name, price, downloadSizeMb, null);
    }

    public double getDownloadSizeMb() {
        return downloadSizeMb;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public boolean trySetDownloadSizeMb(double downloadSizeMb) {
        if (downloadSizeMb > 0) {
            this.downloadSizeMb = downloadSizeMb;
            return true;
        }
        return false;
    }

    public boolean trySetLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
        return true;
    }

    @Override
    public double finalPrice(int quantity, PricePolicy policy) {
        if (policy instanceof BogoHalfPromotion) {
            return finalPrice(quantity); // Ignore BogoHalf policy
        }
        return super.finalPrice(quantity, policy);
    }

    @Override
    public double finalPrice(int quantity, List<PricePolicy> policies) {
        double bestPrice = finalPrice(quantity);
        for (PricePolicy policy : policies) {
            if (!(policy instanceof BogoHalfPromotion)) {
                bestPrice = Math.min(bestPrice, finalPrice(quantity, policy));
            }
        }
        return bestPrice;
    }
}
