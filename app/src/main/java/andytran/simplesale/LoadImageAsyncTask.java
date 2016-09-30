package andytran.simplesale;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Andy Tran on 9/4/2015.
 */
public class LoadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private Context context;
    private WeakReference<ImageView> imageViewReference;
    private String imagePath;

    public LoadImageAsyncTask(Context context, ImageView imageView) {
        this.context = context;
        imageViewReference = new WeakReference<>(imageView);
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        imagePath = params[0];
        File f = new File(context.getExternalFilesDir(null), imagePath);
        Bitmap bmp = Utils.decodeSampledBitmapFromFile(f.getAbsolutePath(), 50, 50);

        final MainActivity mainActivity = (MainActivity) context;
        mainActivity.addBitmapToMemoryCache(imagePath, bmp);

        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final LoadImageAsyncTask loadImageAsyncTask = Utils.getLoadImageTask(imageView);

            if (this == loadImageAsyncTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
