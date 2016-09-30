package andytran.simplesale;

/**
 * Created by Andy Tran on 6/17/2015.
 */
public class SaleItem {
    static final public int PRIVATE_MODE = 0;
    static final public int FRIEND_MODE = 1;
    static final public int PUBLIC_MODE = 2;

    private String description;
    private String qrCodePath;
    private String paymentURL;
    private String imagePath;
    private double price;
    private int mode;
    private long id;

    public SaleItem() {
        this.description = null;
        this.qrCodePath = null;
        this.paymentURL = null;
        this.imagePath = "DEFAULT";
        this.price = 0;
        this.mode = PRIVATE_MODE;
    }

    public SaleItem(String description, double price, int mode) {
        this();
        this.description = description;
        this.price = price;
        this.mode = mode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null)
            return;
        this.description = description;
    }

    public String getQrCodePath() {
        return qrCodePath;
    }

    public void setQrCodePath(String qrCodePath) {
        if (qrCodePath == null)
            return;
        this.qrCodePath = qrCodePath;
    }

    public String getPaymentURL() {
        return paymentURL;
    }

    public void setPaymentURL(String paymentURL) {
        if (paymentURL == null)
            return;
        this.paymentURL = paymentURL;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        if (imagePath == null)
            return;
        this.imagePath = imagePath;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0)
            return;
        this.price = price;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        if (mode != PRIVATE_MODE && mode != PUBLIC_MODE && mode != FRIEND_MODE)
            return;
        this.mode = mode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id < 0)
            return;
        this.id = id;
    }
}
