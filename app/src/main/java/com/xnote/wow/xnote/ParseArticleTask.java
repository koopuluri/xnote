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
                outState = ParseArticleManager.TASK_COMPLETED;
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
            mCallback.run(mUpdatedArticle);
        else
            Log.e(TAG, "updatedArticle is null.");
    }

    public String getArticleId() {
        return mArticleId;
    }

    public void setUpdatedArticle(ParseArticle updatedArticle) {
        Log.d(TAG, "article is updated");
        mUpdatedArticle = updatedArticle;
    }
}