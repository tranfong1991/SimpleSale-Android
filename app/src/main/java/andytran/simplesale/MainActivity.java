package andytran.simplesale;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;


public class MainActivity extends ListActivity {
    final static private int ADD_ITEM_REQUEST = 1;

    private SaleItemAdapter adapter;
    private FloatingActionButton addItemBtn;
    private ArrayList<SaleItem> saleItems = new ArrayList<>();
    private SaleItemsDbHelper dbHelper = new SaleItemsDbHelper(this);
    private LruCache<String, Bitmap> mMemoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //use 1/8th of the available memory for cache
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        //check if user already enters Venmo username
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String venmoUserName = pref.getString("venmoUserName", null);
        if (venmoUserName == null) {
            DialogFragment dialog = new VenmoUserNameDialogFragment();
            dialog.show(getFragmentManager(), "VenmoUserName");
        }

        adapter = new SaleItemAdapter(this, saleItems);
        setListAdapter(adapter);
        getListView().setTextFilterEnabled(true);

        //read from db about sale items and populate saleItems array list
        populateListViewFromDb();

        addItemBtn = (FloatingActionButton) findViewById(R.id.add_item_fab);
        addItemBtn.attachToListView(this.getListView());
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivityForResult(intent, ADD_ITEM_REQUEST);
            }
        });

        ListView listView = getListView();
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Delete this item?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SaleItem clickedItem = saleItems.get(position);

                        if (!clickedItem.getImagePath().equals("DEFAULT")) {
                            mMemoryCache.remove(clickedItem.getImagePath());
                            Utils.deleteExternalStoragePrivateFile(MainActivity.this, clickedItem.getImagePath());
                        }

                        if (clickedItem.getQrCodePath() != null)
                            deleteFile(clickedItem.getQrCodePath());

                        deleteSaleItemFromDb(clickedItem);
                        saleItems.remove(position);

                        //a work-around to show list item when deleted after filtered
                        adapter.getFilter().filter("");

                        //no need for notify because filter already does
                        //adapter.notifyDataSetChanged();

                        Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ITEM_REQUEST) {
            if (resultCode == RESULT_OK) {
                String description = data.getStringExtra("EXTRA_ITEM_DESCRIPTION");
                String imagePath = data.getStringExtra("EXTRA_IMAGE_PATH");
                double price = data.getDoubleExtra("EXTRA_ITEM_PRICE", 0);
                int mode = data.getIntExtra("EXTRA_ITEM_MODE", 0);

                SaleItem item = new SaleItem(description, price, mode);
                item.setImagePath(imagePath);

                saleItems.add(item);
                addSaleItemToDb(item);

                adapter.getFilter().filter("");
                Toast.makeText(MainActivity.this, "Item added", Toast.LENGTH_SHORT).show();

                new RetrieveInfoAsyncTask(this).execute(item, dbHelper);

                //no need for notify because filter already does
                //adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        //open QRCodeActivity to display QR code and url
        Intent intent = new Intent(MainActivity.this, QRCodeActivity.class);
        SaleItem clickedItem = adapter.getItem(position);

        if (clickedItem.getQrCodePath() == null || clickedItem.getPaymentURL() == null) {
            Toast.makeText(this, "Retrieving information... Please come back later.", Toast.LENGTH_LONG).show();
            return;
        }

        //pass in the path to the qr code and the url
        intent.putExtra("EXTRA_QR_CODE_PATH", clickedItem.getQrCodePath());
        intent.putExtra("EXTRA_PAYMENT_URL", clickedItem.getPaymentURL());
        intent.putExtra("EXTRA_ITEM_DESCRIPTION", clickedItem.getDescription());
        intent.putExtra("EXTRA_ITEM_PRICE", String.format("%.2f", clickedItem.getPrice()));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void populateListViewFromDb() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                SaleItemContract.SaleItemEntry._ID,
                SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_DESC,
                SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_PRICE,
                SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_MODE,
                SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_QR_CODE_PATH,
                SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_PAYMENT_URL,
                SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_IMAGE_PATH
        };
        Cursor cursor = db.query(SaleItemContract.SaleItemEntry.TABLE_NAME, projection, null, null, null, null, null);

        //check if the table is empty
        if (!cursor.moveToFirst())
            return;

        do {
            long id = cursor.getLong(cursor.getColumnIndex(SaleItemContract.SaleItemEntry._ID));
            double price = cursor.getDouble(cursor.getColumnIndex(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_PRICE));
            int mode = cursor.getInt(cursor.getColumnIndex(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_MODE));
            String description = cursor.getString(cursor.getColumnIndex(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_DESC));
            String qrCodePath = cursor.getString(cursor.getColumnIndex(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_QR_CODE_PATH));
            String paymentURL = cursor.getString(cursor.getColumnIndex(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_PAYMENT_URL));
            String imagePath = cursor.getString(cursor.getColumnIndex(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_IMAGE_PATH));

            SaleItem item = new SaleItem(description, price, mode);
            item.setQrCodePath(qrCodePath);
            item.setPaymentURL(paymentURL);
            item.setImagePath(imagePath);
            item.setId(id);

            saleItems.add(item);
        } while (cursor.moveToNext());

        adapter.notifyDataSetChanged();
    }

    private void addSaleItemToDb(SaleItem saleItem) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_DESC, saleItem.getDescription());
        values.put(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_PRICE, saleItem.getPrice());
        values.put(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_MODE, saleItem.getMode());
        values.put(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_IMAGE_PATH, saleItem.getImagePath());

        long newRowId = db.insert(SaleItemContract.SaleItemEntry.TABLE_NAME, null, values);
        saleItem.setId(newRowId);
    }

    private void deleteSaleItemFromDb(SaleItem saleItem) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SaleItemContract.SaleItemEntry.TABLE_NAME,
                SaleItemContract.SaleItemEntry._ID + " = " + saleItem.getId(),
                null);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}