package com.xnote.lol.xnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.xnote.lol.xnote.models.ParseArticle;

import java.text.SimpleDateFormat;

/**
 * Created by koopuluri on 1/29/15.
 */
public class Util {
    public static final String TAG = "Util";
    public static boolean IS_ANON = false;

    public static String dateFromSeconds(long seconds) {
        // http://stackoverflow.com/a/9754625/2713471:
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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
     * @TargetApi
     * @param activity: the activity that contains the textView
     * @param tv: textView to stylize.
     */
    public static void setXnoteArticleTypeFace(Activity activity, TextView tv) {
        Typeface normalTypeface = Typeface.createFromAsset(activity.getAssets(),
                "CrimsonText-Regular.ttf");
        tv.setTypeface(normalTypeface);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tv.setElegantTextHeight(true);
            tv.setLetterSpacing(-0.02f);
        }
        tv.setIncludeFontPadding(true);
        tv.setLineSpacing(0.0f, 1.2f);
        tv.setPadding(30, 0, 30, 0);
        tv.setTextSize(16);
        tv.setVisibility(View.VISIBLE);
        tv.setTextColor(activity.getResources().getColor(R.color.text_color_primary));
        tv.setPadding(Constants.PADDING, 0, Constants.PADDING, 0);
    }


    /**
     * @TargetApi
     * @param activity: the activity that contains the textView
     * @param tv: textView to stylize.
     */
    public static void setXnoteNoteTypeFace(Activity activity, TextView tv) {
        Typeface normalTypeface = Typeface.createFromAsset(activity.getAssets(),
                "Dual-300.ttf");
        tv.setTypeface(normalTypeface);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tv.setElegantTextHeight(true);
            tv.setLetterSpacing(-0.02f);
        }
        tv.setIncludeFontPadding(true);
        tv.setLineSpacing(0.0f, 1.2f);
        tv.setPadding(30, 0, 30, 0);
        tv.setTextSize(16);
        tv.setVisibility(View.VISIBLE);
        tv.setTextColor(activity.getResources().getColor(R.color.text_color_primary));
        tv.setPadding(Constants.PADDING, 0, Constants.PADDING, 0);
    }



    public static void share(String subjectText, String extraText, String userMessage,
                             final Activity launchActivity) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        // Add data to the intent, the receiving app will decide what to do with it.
        intent.putExtra(Intent.EXTRA_SUBJECT, subjectText);
        intent.putExtra(Intent.EXTRA_TEXT, extraText);
        if(Util.IS_ANON) {
                AlertDialog.Builder builder = new AlertDialog.Builder(launchActivity);
                builder.setMessage(R.string.share_disabled_message);
                builder.setPositiveButton("Sign Up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Controller.launchSignUpFromAnonymousUser(launchActivity);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
        } else {
            launchActivity.startActivity(Intent.createChooser(intent, userMessage));
        }
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getCurrentFocus().getWindowToken(), 0);
    }
}
