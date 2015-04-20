package com.xnote.lol.xnote.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.parse.ParseUser;
import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.DB;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Util;
import com.xnote.lol.xnote.XnoteLogger;
import com.xnote.lol.xnote.models.ParseArticle;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    XnoteLogger logger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        mIntent = getIntent();
        mNewArticleInitialized = false;
        mThisActivity = this;
        mNewArticle = new ParseArticle();
        mNewArticle.setId();

        logger = new XnoteLogger(getApplicationContext());

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
            logger.flush();
            if (mNewArticle.getArticleUrl() != null) {
                if(ParseUser.getCurrentUser() != null) {
                    boolean need_to_sync = Util.getGlobalNeedToSyncVariable(mThisActivity);
                    if (need_to_sync) {
                        Controller.launchLoginSignUpActivity(mThisActivity);
                    }
                    Controller.launchMainActivity(mThisActivity);
                } else {
                    Controller.launchLoginSignUpActivity(mThisActivity);
                }
                finish();
            } else {
                Log.e(TAG, "FUUUUUUUUUCK.");

                // toast to tell user that article url couldn't be found:
                Toast.makeText(getApplicationContext(),
                        "couldn't find article url.",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
            if (isPaused) finish();  // kill this activity if a newer one has opened up over it. of later button was clicked.
        }
    }


    /**
     * Obtains string extra that represents the article url from intent.
     * @param intent
     */
    boolean handleSendText(Intent intent) {
        String inputText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String urlString;

        try {
            JSONObject props = new JSONObject();
            props.put("InputText", inputText);
            logger.log("ReceiveActivity.Input", props);
        } catch (JSONException e) {
            // do nothing.
        }
        // checking if it is possible to make url from this string:
        try {
            new URL(inputText);
            urlString = inputText;
        } catch (MalformedURLException e) {
            // ok try and get url from this text:
            urlString = pullFirstLink(inputText);
            try {  // once again trying to see if this is a valid url:
                new URL(urlString);
            } catch (MalformedURLException ex) {
                // analytics:
                JSONObject obj = new JSONObject();
                try {
                    obj.put("InputText", inputText);
                    obj.put("ExtractedUrl", urlString);
                } catch (JSONException jsonException) {
                    // do nothing.
                }
                logger.log("ReceiveActivity.Fail", obj);

                return false;  // urlString == "" case is handled here.;
            }
        }
        // creating holder article:
        mNewArticle.setTimestamp(System.currentTimeMillis());
        mNewArticle.setIsparsed(false);  // means that information for this article has not yet been parsed from Diffbot API.
        mNewArticle.setArticleUrl(urlString);
        // saving this placeholder article:
        DB.saveArticleImmediatelyLocally(mNewArticle);

        // analytics:

        JSONObject obj = new JSONObject();
        try {
            obj.put("InputText", inputText);
            obj.put("ExtractedUrl", urlString);
            obj.put("NewArticleId", mNewArticle.getId());
        } catch (JSONException jsonException) {
            // do nothing.
        }
        logger.log("ReceiveActivity.ArticleAdded", obj);
        return true;
    }


    /**
     * http://blog.houen.net/java-get-url-from-string/
     * @param text
     * @return
     */
    private String pullFirstLink(String text) {
        ArrayList<String> links = new ArrayList();

        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while(m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }
            //links.add(urlStr);
            return urlStr;
        }
        return "";
    }
}