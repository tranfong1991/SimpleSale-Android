package andytran.simplesale;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Andy Tran on 8/30/2015.
 */
public class SaleItemsDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "SimpleSale.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SaleItemContract.SaleItemEntry.TABLE_NAME + " (" +
                    SaleItemContract.SaleItemEntry._ID + " INTEGER PRIMARY KEY," +
                    SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_DESC + TEXT_TYPE + COMMA_SEP +
                    SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_PRICE + REAL_TYPE + COMMA_SEP +
                    SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_MODE + INT_TYPE + COMMA_SEP +
                    SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_QR_CODE_PATH + TEXT_TYPE + COMMA_SEP +
                    SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_PAYMENT_URL + TEXT_TYPE + COMMA_SEP +
                    SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_IMAGE_PATH + TEXT_TYPE + ")";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SaleItemContract.SaleItemEntry.TABLE_NAME;

    public SaleItemsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
