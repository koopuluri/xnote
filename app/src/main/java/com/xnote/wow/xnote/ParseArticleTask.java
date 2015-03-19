package com.xnote.wow.xnote;

import android.util.Log;

import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.runnables.ParseArticleRunnable;


public class ParseArticleTask {
    public static final String TAG = "ParseArticleTask";

    String mArticleId;
    ParseArticleManager sArticleManager;
    ParseCallback mCallback;
    ParseArticle mUpdatedArticle;

    public ParseArticleTask(String id, ParseCallback callback) {
        mArticleId = id;
        sArticleManager = ParseArticleManager.getInstance();
        mCallback = callback;
        mUpdatedArticle = null;
    }

    public void handleParseState(int state) {
        int outState = ParseArticleManager.TASK_NOT_COMPLETED;
        switch(state) {
            case ParseArticleRunnable.PARSE_COMPLETED:
                Log.d(TAG, "handleParseState()  : PARSE_COMPLETED state");
                outState = ParseArticleManager.TASK_COMPLETED;
                break;
            case ParseArticleRunnable.PARSE_NOT_COMPLETED:
                Log.d(TAG, "handleParseState()  : PARSE_NOT_COMPLETED state");
                outState = ParseArticleManager.TASK_NOT_COMPLETED;
                break;
        }
        // calls generalized state method:
        handleState(outState);
    }

    void handleState(int state) {
        sArticleManager.handleState(this, state);
    }

    public void onArticleParsed() {
        // what should be executed on the main thread after an article has been parsed:
        Log.d(TAG, "onArticleParsed().");
        if (mUpdatedArticle != null)
            try {
                mCallback.run(mUpdatedArticle);
            } catch(NullPointerException e) {
                //This happens if the article fragment is killed before parsing is done
                //Shouldnt be a problem cause on resume will handle that for us
                Log.d(TAG, "Article fragment was killed" + e);
            }
        else
            Log.e(TAG, "updatedArticle is null.");
    }

    public void onArticleFailed() {
        // what should be executed on the main thread after an article has been parsed:
        Log.d(TAG, "onArticleFailed().");
        try {
            mCallback.runFailed();
        } catch(NullPointerException e) {
            //This happens if the article fragment is killed before parsing is done
            //Shouldnt be a problem cause on resume will handle that for us
            Log.d(TAG, "Article fragment was killed" + e);
        }
    }

    public String getArticleId() {
        return mArticleId;
    }

    public void setUpdatedArticle(ParseArticle updatedArticle) {
        Log.d(TAG, "article is updated");
        mUpdatedArticle = updatedArticle;
    }
}