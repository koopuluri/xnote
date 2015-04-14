package com.xnote.lol.xnote.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseUser;
import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.DB;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.models.ParseArticle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class receives article links from other apps to be used in xnote:
 * Created by koopuluri on 2/9/15.
 */
public class ReceiveActivity extends Activity {
    public static final String TAG = "ReceiveActivity";
    Intent mIntent;
    boolean mNewArticleInitialized;
    boolean isPaused;
    ParseArticle mNewArticle;
    Activity mThisActivity;
    //TextView mSaveMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        mIntent = getIntent();
        mNewArticleInitialized = false;
        mThisActivity = this;
        mNewArticle = new ParseArticle();
        mNewArticle.setId();
        new HandleSendTextTask().execute();
    }


    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
    }

    private class HandleSendTextTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            handleSendText(mIntent);
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            super.onPostExecute(_);
            mNewArticleInitialized = true;
            if (mNewArticle.getArticleUrl() != null) {
                if(ParseUser.getCurrentUser() != null) {
                    Controller.launchMainActivity(mThisActivity);
                    finish();
                } else {
                    Controller.launchLoginSignUpActivity(mThisActivity);
                    finish();
                }
            } else {
            }
            if (isPaused) finish();  // kill this activity if a newer one has opened up over it. of later button was clicked.
        }
    }


    /**
     * Obtains string extra that represents the article url from intent.
     * @param intent
     */
    void handleSendText(Intent intent) {
        String inputText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String urlString;
        // checking if it is possible to make url from this string:
        try {
            new URL(inputText);
            urlString = inputText;
        } catch (MalformedURLException e) {
            // ok try and get url from this text:
            List<String> links = pullLinks(inputText);
            if (links.size() != 0) {
                urlString = links.get(0);
            } else {
                finish();
                return;
            }
        }
        Log.d(TAG, "urlText: " + urlString);
        Log.d(TAG, "original article url: " + inputText);
        // creating holder article:
        mNewArticle.setTimestamp(System.currentTimeMillis());
        mNewArticle.setIsparsed(false);  // means that information for this article has not yet been parsed from Diffbot API.
        mNewArticle.setArticleUrl(urlString);
        // saving this placeholder article:
        DB.saveArticleImmediatelyLocally(mNewArticle);
    }

    /**
     * http://blog.houen.net/java-get-url-from-string/
     * @param text
     * @return
     */
    private ArrayList<String> pullLinks(String text) {
        ArrayList<String> links = new ArrayList();

        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while(m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }
            links.add(urlStr);
        }
        return links;
    }
}