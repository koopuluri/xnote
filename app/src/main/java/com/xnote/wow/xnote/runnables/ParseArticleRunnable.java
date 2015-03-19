package com.xnote.wow.xnote.runnables;

import android.util.Log;

import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.DiffbotParser;
import com.xnote.wow.xnote.ParseArticleTask;
import com.xnote.wow.xnote.models.ParseArticle;

/**
 * Created by koopuluri on 3/11/15.
 */
public class ParseArticleRunnable implements Runnable {
    public static final String TAG = "ParseArticleRunnable";
    String mArticleId;
    ParseArticleTask mTask;
    public static final int PARSE_COMPLETED = 234;
    public static final int PARSE_NOT_COMPLETED = 789;

    public ParseArticleRunnable(ParseArticleTask parseTask) {
        mArticleId = parseTask.getArticleId();
        mTask = parseTask;
    }

    @Override
    public void run() {
        Log.d(TAG, "run().");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        DiffbotParser parser = new DiffbotParser(mArticleId);

        if (Thread.interrupted())  // checking if this thread has been interrupted before executing.
            return;
        ParseArticle updatedArticle = parser.parse();
        // setting task state to 'completed':
        if(updatedArticle != null ) {
            if(!updatedArticle.getCouldNotBeParsed()) {
                Log.d(TAG, "updateArticle: " + String.valueOf(updatedArticle));
                DB.saveArticleImmediately(updatedArticle);
                mTask.setUpdatedArticle(updatedArticle);
                mTask.handleParseState(PARSE_COMPLETED);
            } else {
                Log.d(TAG, "Error could not parse");
                DB.saveArticleImmediatelyLocally(updatedArticle);
                mTask.handleParseState(PARSE_NOT_COMPLETED);
            }
        } else {
            Log.d(TAG, "Article already parsed");
            mTask.handleParseState(PARSE_COMPLETED);
        }
    }

    public Thread getThread() {
        return Thread.currentThread();
    }

}
