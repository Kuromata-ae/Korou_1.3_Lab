package product;

public class DigitalProduct extends Product {
    private double downloadSizeMb;
    private String licenseKey;

    public DigitalProduct() {
        super();
    }

    public DigitalProduct(String id, String name, double price, double downloadSizeMb) {
        super(id, name, price);
        trySetDownloadSizeMb(downloadSizeMb);
    }

    public DigitalProduct(String id, String name, String description, double price, int quantity, Category category, double downloadSizeMb, String licenseKey) {
        super(id, name, description, price, quantity, category);
        trySetDownloadSizeMb(downloadSizeMb);
        trySetLicenseKey(licenseKey);
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
    public String toString() {
        return super.toString() + " | DigitalProduct{" +
                "downloadSizeMb=" + downloadSizeMb +
                ", licenseKey='" + licenseKey + '\'' +
                '}';
    }
}
