package com.xnote.wow.xnote;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by koopuluri on 2/17/15.
 */
public class FetchImageTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "FetchImageTask";
    String mImageUrlString;
    String mArticleId;
    int[] heightAndWidth;

    public FetchImageTask(String imageUrlString, int[] naturalHeightAndWidth, String articleId) {
        super();
        mImageUrlString = imageUrlString;
        mArticleId = articleId;
        heightAndWidth = naturalHeightAndWidth;
    }

    @Override
    public Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground()");
        byte[] data = DiffbotParser.downloadImage(mImageUrlString);
        DiffbotParser.saveImageFromData(mImageUrlString, data, mArticleId, heightAndWidth);
        Log.d(TAG, String.format("image downloaded and saved! with imageUrlString: %s," +
                        " and articleId: %s",
                mImageUrlString,
                mArticleId));
        return null;
    }
}
