package andytran.simplesale;

import android.provider.BaseColumns;

/**
 * Created by Andy Tran on 8/30/2015.
 */
public final class SaleItemContract {
    public SaleItemContract() {
    }

    public static abstract class SaleItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "saleitems";
        public static final String COLUMN_NAME_ITEM_DESC = "description";
        public static final String COLUMN_NAME_ITEM_PRICE = "price";
        public static final String COLUMN_NAME_ITEM_MODE = "mode";
        public static final String COLUMN_NAME_ITEM_QR_CODE_PATH = "qrcodepath";
        public static final String COLUMN_NAME_ITEM_PAYMENT_URL = "paymenturl";
        public static final String COLUMN_NAME_ITEM_IMAGE_PATH = "imagepath";
    }
}
