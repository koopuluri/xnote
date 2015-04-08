package com.xnote.lol.xnote.models;

/**
 * Created by koopuluri on 2/18/15.
 */
public class DiffbotImageInfo {
    private String mImageUrl;
    private int[] mImageHeightAndWidth;

    public DiffbotImageInfo(String url, int[] heightAndWidth) {
        mImageUrl = url;
        mImageHeightAndWidth = heightAndWidth;
    }


    public String getUrl() { return mImageUrl; }
    public int[] getHeightAndWidth() { return mImageHeightAndWidth; }
}
