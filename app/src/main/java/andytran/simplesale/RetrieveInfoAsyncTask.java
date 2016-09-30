package andytran.simplesale;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Andy Tran on 8/31/2015.
 * Used to retrieve bitly url and QR code image
 */
public class RetrieveInfoAsyncTask extends AsyncTask<Object, Void, Void> {
    private static final String BITLY_ACCESS_TOKEN = "fd46ad5065c863be75ca5b6c83297ca228cc7657";
    private static final String BITLY_API_URL = "https://api-ssl.bitly.com/v3/shorten?format=txt&access_token=" + BITLY_ACCESS_TOKEN + "&longurl=";
    private static final String QR_CODE_API_URL = "http://api.qrserver.com/v1/create-qr-code/?size=500x500&data=";

    private Context context;
    private boolean errorDownloadingBitly = false;
    private boolean errorDownloadingQRCode = false;

    public RetrieveInfoAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (!Utils.isNetworkAvailable(this.context)) {
            Toast.makeText(this.context, "Internet not available", Toast.LENGTH_SHORT).show();
            return null;
        }

        SaleItem item = (SaleItem) params[0];
        SaleItemsDbHelper dbHelper = (SaleItemsDbHelper) params[1];
        try {
            //create payment url
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            String venmoUserName = pref.getString("venmoUserName", null);
            String paymentURL = Utils.constructPaymentURL(venmoUserName, item.getPrice(), item.getDescription(), item.getMode());
            String encodedPaymentURL = URLEncoder.encode(paymentURL, "UTF-8");

            //compress payment url using bitly API
            String compressedPaymentURL = downloadBitlyURL(BITLY_API_URL + encodedPaymentURL);
            if (compressedPaymentURL == null) {
                errorDownloadingBitly = true;
                return null;
            }

            String encodedURL = URLEncoder.encode(compressedPaymentURL, "UTF-8");
            String qrCodeURL = QR_CODE_API_URL + encodedURL;

            //file name is generated based on time stamp to avoid name collision
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String qrCodePath = "qr_" + timeStamp;

            if (!downloadAndSaveQRCode(qrCodeURL, qrCodePath)) {
                errorDownloadingQRCode = true;
                return null;
            }

            //put newly retrieved info into db
            updateSaleItemInfoInDb(item.getId(), compressedPaymentURL, qrCodePath, dbHelper);

            item.setPaymentURL(compressedPaymentURL);
            item.setQrCodePath(qrCodePath);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (errorDownloadingBitly)
            Toast.makeText(context, "ERROR downloading compressed URL. Please check connection.", Toast.LENGTH_LONG).show();
        if (errorDownloadingQRCode)
            Toast.makeText(context, "ERROR downloading QR code. Please check connection.", Toast.LENGTH_LONG).show();
    }

    private void updateSaleItemInfoInDb(long id, String paymentURL, String qrCodePath, SaleItemsDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_PAYMENT_URL, paymentURL);
        values.put(SaleItemContract.SaleItemEntry.COLUMN_NAME_ITEM_QR_CODE_PATH, qrCodePath);

        db.update(SaleItemContract.SaleItemEntry.TABLE_NAME,
                values,
                SaleItemContract.SaleItemEntry._ID + " = " + id,
                null);
    }

    private String downloadBitlyURL(String url) {
        String compressedURL = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet();
            httpGet.setURI(new URI(url));
            HttpResponse response = client.execute(httpGet);

            compressedURL = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return compressedURL;
    }

    private boolean downloadAndSaveQRCode(String qrCodeURL, String qrCodePath) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(qrCodeURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return false;

            input = connection.getInputStream();
            output = context.openFileOutput(qrCodePath, Context.MODE_PRIVATE);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (connection != null)
                connection.disconnect();
        }
        return true;
    }
}
