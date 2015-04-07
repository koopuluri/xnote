package com.xnote.lol.xnote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.xnote.lol.xnote.models.ParseImage;

/**
 * Created by koopuluri on 2/17/15.
 */
public class ArticleImageGetter implements Html.ImageGetter {
    public static final String TAG = "ArticleImageGetter";
    String mArticleId;
    Activity activity;

    public ArticleImageGetter(String articleId, Activity activity) {
        mArticleId = articleId;
        this.activity = activity;
    }

    @Override
    /**
     * uses: com/xnote/wow/xnote/ArticleImageGetter.java:23
     * by author: Gabriel Negut to get Bitmap from byte[] in android
     * @param: source: the text in the html's <img tags's "src" attribute.
     * @return: a drawable containing the bitmap for the image (if image has been saved), else null.
     */
    public Drawable getDrawable(String source) {
        ParseImage image = DB.getImage(source);
        //Checking for natural width along with null to ensure that parse images without
        //any content are not returned. Empty parse images could be returned if there is
        //any error while downloading the image - perhaps if the image is too big.
        if((image != null) && (image.getNaturalWidth() != 0) && (!image.getError())) {
            //Get screenwidth to zoom in images to fit phone size
            //http://stackoverflow.com/a/9316553/4671651
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) activity.getApplicationContext().
                    getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            //Subtracting padding on either side so image fits
            width = width - 2 * (Constants.PADDING);
            byte[] data = image.getData();
            // Generating bitmap from image byte[] data.
            Bitmap bmp;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;  // so that it does not make a copy of the byte[].
            options.inSampleSize = calculateInSampleSize(image, width, Integer.MAX_VALUE );
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            // returning a drawable:
            Drawable drawable = new BitmapDrawable(bmp);  // TODO: don't use a deprecated constructor.
            drawable.setBounds(0, 0, width,
                (image.getNaturalHeight() * width / image.getNaturalWidth()));
            return drawable;
        } else {
            return null;
        }
    }

    //Method to calculate the scaling factor for the images
    //http://stackoverflow.com/a/20441760/4671651
    public static int calculateInSampleSize(ParseImage image,
                                           int reqWidth, int reqHeight) {
        // Raw height and width of image
        int height = image.getNaturalHeight();
        int width = image.getNaturalWidth();
        int inSampleSize = 1;
        while (height > reqHeight || width > reqWidth) {
            inSampleSize *= 2;
            height /= 2;
            width /= 2;
        }
        return inSampleSize;
    }
}
