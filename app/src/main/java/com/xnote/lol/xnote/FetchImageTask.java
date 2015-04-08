package com.xnote.lol.xnote;

import android.os.AsyncTask;

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
        byte[] data = DiffbotParser.downloadImage(mImageUrlString);
        DiffbotParser.saveImageFromData(mImageUrlString, data, mArticleId, heightAndWidth);
        return null;
    }
}
