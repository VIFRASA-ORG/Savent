package Helper;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.widget.ImageView;
import java.io.FileNotFoundException;

/**
 * Classe Helper con alcuni metodi per ridimensioneare le immagini scaricate da Firebase
 * in maniera tale da rientrare nelle dimensioni massime definite nella ImageView.
 *
 * Se non utilizzati, le immagini più grandi del consentito caricate nelle ImageView
 * non veranno visualizzate.
 */
public class ImageHelper {

    /**
     * Calcola il valore inSampleSize, valore per scalare l'immagine bitmap passata come parametro.
     *
     * @param options opzioni BitmapFactory.
     * @param reqWidth larghezza dell'imageView dove si vuole visualizzare l'immagine.
     * @param reqHeight altezza dell'imageView dove si vuole visualizzare l'immagine.
     * @return valore di inSampleSize per scalare l'immagine.
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
     * Metodo che comprime l'immagine passata come parametro in maniera tale
     * da risparmiare memoria e farla entrare nell'ImageView.
     *
     * @param cR content resolver.
     * @param image immagine che si vuole comprimere nell'imageView.
     * @param destination imageView di destinazione dell'immagine.
     * @return
     */
    public static final Bitmap decodeSampledBitmapFromUri(ContentResolver cR, Uri image, ImageView destination ) {

        int reqWidth, reqHeight;
        reqHeight = destination.getHeight();
        reqWidth = destination.getWidth();

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Rect c = new Rect();
        c.set(0,0,reqHeight,reqHeight);

        try {
            Bitmap b = BitmapFactory.decodeStream(cR.openInputStream(image),c,options);
        } catch (FileNotFoundException e) {
            return null;
        }

        // Calcolo l'inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decodifico la bitma con il nuovo inSampleSize.
        options.inJustDecodeBounds = false;
        try {
            return BitmapFactory.decodeStream(cR.openInputStream(image),c,options);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

}
