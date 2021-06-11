package Helper;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;

public class ImageHelper {

    /**
     * Return the inSampleSize of the bitmap, that is the scale ration for that image
     *
     * @param options BitmapFactory options
     * @param reqWidth width of the imageView where you want to display the image
     * @param reqHeight height of the imageView where you want to display the image
     * @return the inSampleSize value
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Compress the image given to fit into the imageView.
     *
     * @param res manager of the application resources
     * @param resId the image resource id
     * @param destination destination imageView where you want to display the image
     * @return the compressed bitmap
     */
    public static final Bitmap decodeSampledBitmapFromResource(Resources res, int resId, ImageView destination) {
        int reqWidth, reqHeight;
        reqHeight = destination.getHeight();
        reqWidth = destination.getWidth();

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Compress the image given to fit into the imageView.
     *
     * @param cR the content model
     * @param image the image to compress
     * @param destination imageView where you want to display the image
     * @return the compressed bitmap
     */
    public static final Bitmap decodeSampledBitmapFromUri(ContentResolver cR, Uri image, ImageView destination ) {

        int reqWidth, reqHeight;
        reqHeight = destination.getHeight();
        reqWidth = destination.getWidth();

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Rect c = new Rect();
        c.set(0,0,reqHeight,reqHeight);

        try {
            Bitmap b = BitmapFactory.decodeStream(cR.openInputStream(image),c,options);
        } catch (FileNotFoundException e) {
            return null;
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        try {
            return BitmapFactory.decodeStream(cR.openInputStream(image),c,options);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

}
