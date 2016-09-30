package andytran.simplesale;


import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Andy Tran on 8/26/2015.
 */
public final class Utils {
    private static final String PAYMENT_URL = "https://venmo.com/?txn=pay";

    public Utils() {
    }

    public static String constructPaymentURL(String recipient, double amount, String note, int audience) {
        StringBuffer buf = new StringBuffer();

        try {
            buf.append("&recipients=");
            buf.append(recipient);
            buf.append("&amount=");
            buf.append(amount);
            buf.append("&note=");
            buf.append(URLEncoder.encode(note, "UTF-8"));
            buf.append("&audience=");
            buf.append(audience == SaleItem.FRIEND_MODE ? "friend" : audience == SaleItem.PRIVATE_MODE ? "private" : "public");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return PAYMENT_URL + buf.toString();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isCameraAvailable(Context context) {
        PackageManager manager = context.getPackageManager();
        return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static void deleteExternalStoragePrivateFile(Context context, String fileName) {
        File file = new File(context.getExternalFilesDir(null), fileName);
        if (file != null) {
            file.delete();
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        //keep dividing the original image size by 2 until either height or width is smaller than the requested dimension
        if (height > reqHeight || width > reqHeight) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        //return a power of 2. Example: original size is 1024 x 1024, requested size is 128 x 128, 
        //then inSampleSize is 4, i.e (2^(10-1) / 2^7 = 2^9/2^7 = 2^2 = 4)
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources resources, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(resources, resId, options);
    }

    public static LoadImageAsyncTask getLoadImageTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getLoadImageTask();
            }
        }
        return null;
    }

    public static boolean cancelPotentialWork(String path, ImageView imageView) {
        final LoadImageAsyncTask loadImageAsyncTask = getLoadImageTask(imageView);

        if (loadImageAsyncTask != null) {
            final String imagePath = loadImageAsyncTask.getImagePath();
            if (imagePath == null || !imagePath.equals(path)) {
                loadImageAsyncTask.cancel(true);
            } else {
                //The same work is already in progress
                return false;
            }
        }
        //No task associated with the ImageView, or an existing task was cancelled
        return true;
    }
}
