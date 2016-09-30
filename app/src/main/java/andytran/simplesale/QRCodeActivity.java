package andytran.simplesale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class QRCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        ImageView qrCodeImage = (ImageView) findViewById(R.id.qr_code_image);
        TextView paymentURL = (TextView) findViewById(R.id.payment_url);
        TextView itemDescription = (TextView) findViewById(R.id.item_description_output);
        TextView itemPrice = (TextView) findViewById(R.id.item_price_output);
        Bundle extra = getIntent().getExtras();
        try {
            InputStream input = openFileInput(extra.getString("EXTRA_QR_CODE_PATH"));
            Bitmap bmp = BitmapFactory.decodeStream(input);
            qrCodeImage.setImageBitmap(bmp);

            paymentURL.setText(extra.getString("EXTRA_PAYMENT_URL"));
            itemDescription.setText(extra.getString("EXTRA_ITEM_DESCRIPTION"));
            itemPrice.setText("$" + extra.getString("EXTRA_ITEM_PRICE"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
