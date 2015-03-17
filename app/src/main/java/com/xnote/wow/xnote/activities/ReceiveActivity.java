package com.xnote.wow.xnote.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.parse.ParseUser;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.models.ParseArticle;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * This class receives article links from other apps to be used in xnote:
 * Created by koopuluri on 2/9/15.
 */
public class ReceiveActivity extends Activity {
    public static final String TAG = "ReceiveActivity";
    Intent mIntent;
    boolean mNewArticleInitialized;
    boolean isPaused;
    Button mOpenButton;
    Button mLaterButton;
    ParseArticle mNewArticle;
    Activity mThisActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        mIntent = getIntent();
        mNewArticleInitialized = false;
        mThisActivity = this;

        mNewArticle = new ParseArticle();
        mNewArticle.setId();
        mOpenButton = (Button) findViewById(R.id.receive_open_button);
//        mOpenButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Controller.launchMainActivity(thisActivity, mNewArticle.getId());
//                if (mNewArticleInitialized)
//                    finish();  // finish this activity is HandleSendText is completed.
//            }
//        });
        mLaterButton = (Button) findViewById(R.id.receive_later_button);
//        mLaterButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mNewArticleInitialized) finish();
//                else isPaused = true;
//            }
//        });
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                new HandleSendTextTask().execute();
                Log.d(TAG, "HandleSendTextTask launched: " + getIntent()
                        .getStringExtra(Intent.EXTRA_TEXT));
            } else {
                Log.d(TAG, "can't handle this type of data: " + type);
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume().");
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
                    Controller.launchMainActivity(mThisActivity, mNewArticle.getId());
                    finish();
                } else {
                    Controller.launchLoginSignUpActivity(mThisActivity);
                }
            } else {
                Log.e(TAG, "stringUrl is null after handleText().");
            }
            if (isPaused) finish();  // kill this activity if a newer one has opened up over it. of later button was clicked.
        }
    }


    /**
     * Obtains string extra that represents the article url from intent.
     * @param intent
     */
    void handleSendText(Intent intent) {
        Log.d(TAG, "handleSendText()");
        String stringUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        Log.d(TAG, "url: " + stringUrl);
        // checking if it is possible to make url from this string:
        try {
            new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "malformed url: " + stringUrl);
            return;
        }
        // creating holder article:
        mNewArticle.setTimestamp(System.currentTimeMillis());
        mNewArticle.setIsparsed(false);  // means that information for this article has not yet been parsed from Diffbot API.
        mNewArticle.setArticleUrl(stringUrl);
        // saving this placeholder article:
        DB.saveArticleImmediatelyLocally(mNewArticle);
    }
}