package andytran.simplesale;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andy Tran on 6/21/2015.
 */
public class AddItemActivity extends AppCompatActivity implements View.OnClickListener, ModeChoiceDialogFragment.ModeChoiceListener {
    private static final int IMAGE_CAPTURE_REQUEST = 1;

    private EditText itemPriceEditText;
    private EditText itemDescriptionEditText;
    private ImageView removeImage;
    private ImageView itemImage;
    private Button addItemButton;
    private ImageView modeImage;

    //to check if the user has taken a image or not
    private boolean isNewPicture = false;
    private String picName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        itemPriceEditText = (EditText) findViewById(R.id.item_price_input);
        itemDescriptionEditText = (EditText) findViewById(R.id.item_description_input);
        removeImage = (ImageView) findViewById(R.id.item_image_remove);
        itemImage = (ImageView) findViewById(R.id.item_image_input);
        addItemButton = (Button) findViewById(R.id.add_item_btn);
        modeImage = (ImageView) findViewById(R.id.choose_mode_image);

        removeImage.setVisibility(ImageView.INVISIBLE);
        modeImage.setTag(SaleItem.PRIVATE_MODE);
        removeImage.setOnClickListener(this);
        itemImage.setOnClickListener(this);
        addItemButton.setOnClickListener(this);
        modeImage.setOnClickListener(this);
        itemPriceEditText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    itemPriceEditText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");
                    double parsed = Double.parseDouble(cleanString);
                    String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));

                    current = formatted;
                    itemPriceEditText.setText(formatted);
                    itemPriceEditText.setSelection(formatted.length());
                    itemPriceEditText.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                if (picName != null)
                    Utils.deleteExternalStoragePrivateFile(this, picName);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == addItemButton) {
            if (itemPriceEditText.getText().length() == 0) {
                Toast.makeText(AddItemActivity.this, "Please enter the price", Toast.LENGTH_SHORT).show();
                return;
            }

            //check if internet is available
            if (!Utils.isNetworkAvailable(this)) {
                Toast.makeText(AddItemActivity.this, "Internet not available", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent();
            NumberFormat format = NumberFormat.getCurrencyInstance();
            try {
                String note = itemDescriptionEditText.getText().toString();
                double amount = format.parse(itemPriceEditText.getText().toString()).doubleValue();
                int audience = (int) modeImage.getTag();

                if (amount == 0) {
                    Toast.makeText(AddItemActivity.this, "Price cannot be zero.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isNewPicture)
                    intent.putExtra("EXTRA_IMAGE_PATH", (String) null);
                else intent.putExtra("EXTRA_IMAGE_PATH", this.picName);

                intent.putExtra("EXTRA_ITEM_DESCRIPTION", note);
                intent.putExtra("EXTRA_ITEM_PRICE", amount);
                intent.putExtra("EXTRA_ITEM_MODE", audience);

                setResult(RESULT_OK, intent);
                finish();
            } catch (Exception e) {
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        } else if (v == modeImage) {
            DialogFragment dialog = new ModeChoiceDialogFragment();
            dialog.show(getFragmentManager(), "ModeChoice");
        } else if (v == removeImage){
            if(isNewPicture) {
                Utils.deleteExternalStoragePrivateFile(this, picName);
                itemImage.setImageResource(R.drawable.img_placeholder);

                isNewPicture = false;
                removeImage.setVisibility(ImageView.INVISIBLE);
            }
        } else if (v == itemImage) {
            if (!Utils.isCameraAvailable(this)) {
                Toast.makeText(this, "No available camera!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isNewPicture) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                this.picName = "pic_" + timeStamp + ".png";
            }

            //save to directory private to this app only
            File dir = getExternalFilesDir(null);
            File file = new File(dir, picName);

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE_REQUEST) {
            if (resultCode == RESULT_OK) {
                File picFile = new File(getExternalFilesDir(null), this.picName);
                Bitmap bmp = Utils.decodeSampledBitmapFromFile(picFile.getAbsolutePath(), 100, 100);
                itemImage.setImageBitmap(bmp);

                //indicate the user has taken a new item picture
                isNewPicture = true;
                removeImage.setVisibility(ImageView.VISIBLE);
            } else {
                //when resultCode == RESULT_CANCELED
                Utils.deleteExternalStoragePrivateFile(this, picName);
            }
        }
    }

    @Override
    public void onItemChosen(int which) {
        switch (which) {
            case 0:
                modeImage.setImageResource(R.drawable.ic_globe);
                modeImage.setTag(SaleItem.PUBLIC_MODE);
                break;
            case 1:
                modeImage.setImageResource(R.drawable.ic_friends);
                modeImage.setTag(SaleItem.FRIEND_MODE);
                break;
            default:
                modeImage.setImageResource(R.drawable.ic_lock);
                modeImage.setTag(SaleItem.PRIVATE_MODE);
        }
    }
}

