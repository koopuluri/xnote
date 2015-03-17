package com.xnote.wow.xnote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.xnote.wow.xnote.models.ParseArticle;

import java.text.SimpleDateFormat;

/**
 * Created by koopuluri on 1/29/15.
 */
public class Util {
    public static final String TAG = "Util";
    public static boolean IS_ANON = false;

    public static String dateFromSeconds(long seconds) {
        // http://stackoverflow.com/a/9754625/2713471:
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new java.util.Date(seconds));
    }

    /**
     * Copies contents of b into a.
     *
     * @param a
     * @param b
     */
    public static void copyOverArticle(ParseArticle a, ParseArticle b) {
        a.setContent(b.getContent());
        // a.setAuthor(b.getAuthor());
//        a.setArticleUrl(b.getArticleUrl());
//        a.setTitle(b.getTitle());
//        a.setSourceIconUrl(b.getSourceIconUrl());
    }

    /**
     * from: http://stackoverflow.com/a/4239019/2713471
     *
     * @return
     */
    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * @param activity: the activity that contains the textView
     * @param tv: textView to stylize.
     */
    public static void setXnoteTypeFace(Activity activity, TextView tv) {
        Typeface normalTypeface = Typeface.createFromAsset(activity.getAssets(),
                "Dual-300.ttf");
        tv.setTypeface(normalTypeface);
        tv.setElegantTextHeight(true);
        tv.setIncludeFontPadding(true);
        tv.setLineSpacing(0.0f, 1.2f);
        tv.setLetterSpacing(-0.02f);
        tv.setPadding(30, 0, 30, 0);
        tv.setTextSize(16);
        tv.setVisibility(View.VISIBLE);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setPadding(Constants.ARTICLE_PADDING, 0, Constants.ARTICLE_PADDING, 0);
        Log.d(TAG, "xnoteTypeFaceSet.");
    }
}
