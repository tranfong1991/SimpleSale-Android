package andytran.simplesale;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by Andy Tran on 9/5/2015.
 * Displays a placeholder image while async task loads
 */
public class AsyncDrawable extends BitmapDrawable {
    private final WeakReference<LoadImageAsyncTask> loadImageAsyncTaskWeakReference;

    public AsyncDrawable(Resources res, Bitmap bitmap, LoadImageAsyncTask task) {
        super(res, bitmap);
        this.loadImageAsyncTaskWeakReference = new WeakReference<>(task);
    }

    public LoadImageAsyncTask getLoadImageTask() {
        return loadImageAsyncTaskWeakReference.get();
    }
}
