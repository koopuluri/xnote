package com.xnote.wow.xnote.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.xnote.wow.xnote.Constants;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.fragments.ArticleFragment;
import com.xnote.wow.xnote.models.ParseNote;

/**
 * Created by koopuluri on 2/22/15.
 */
public class ArticleActivity extends Activity implements ArticleFragment.OnArticleLoaded {
    public static final String TAG = "ArticleActivity";

    ArticleFragment mArticleFrag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poop);

        // initializing articleFragment with tis articleText:
        FragmentManager fm = getFragmentManager();
        mArticleFrag = (ArticleFragment) ArticleFragment.newInstance(
                getIntent().getStringExtra(Constants.ARTICLE_ID));


        fm.beginTransaction()
                .add(R.id.poop_fragment_container, mArticleFrag, ArticleFragment.TAG)
                        // .add(R.id.poop_fragment_container, mLoadingFrag, ArticleLoadingFragment.TAG)
                .commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        if (requestCode == Constants.NOTE_REQUEST) {
            // time to update buffers:
            Log.d(TAG, "NoteActivity attempted the note that was requested...");
            if (resultCode == RESULT_OK) {
                // unpack the data received:
                ParseNote note = new ParseNote();
                note.setId(data.getStringExtra(Constants.NOTE_ID));
                note.setContent(data.getStringExtra(Constants.NOTE_CONTENT));
                note.setTimestamp(data.getLongExtra(Constants.NOTE_TIMESTAMP, System.currentTimeMillis()));
                note.setStartIndex(data.getIntExtra(Constants.START_INDEX, 0));
                note.setEndIndex(data.getIntExtra(Constants.END_INDEX, 0));
                note.setArticleId(data.getStringExtra(Constants.ARTICLE_ID));
                // Log.d(TAG, "onActivityResult() with noteId: " + noteId);
                int noteState = data.getIntExtra(Constants.NOTE_STATE, 0);
                // ---------------------------------- PUT IN BACKGROUND!!--------------------------
                if (noteState != 0)  // TODO: remove this check, needs to save note regardless of noteState!
                    mArticleFrag.addNoteFromNoteActivity(note, noteState);
                // ---------------------------------------------------------------------------------
                // Log.d(TAG, "Launched updateBufferTask; " +
                // "Result from NoteActivity OK. buffers have been updated!");
            }
            else {
                Log.e(TAG, "result not OK from NoteActivity's done() maybe it was unexpectedly killed! :(");
            }
        }
        else {
            Log.d(TAG, "NoteActivity did not return the NOTE_REQUEST.");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.poop_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // respond to action-up button:
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    Log.d(TAG, "activity not part of this app's stack");
                    // this means that this activity is not part of this app's task,
                    // so create new task when navigating up, with synthesized (???) back stack.
                    TaskStackBuilder.create(this)
                            // Add all of activity's parents to back stack:
                            .addNextIntentWithParentStack(upIntent)
                                    // navigate up to closest parent:
                            .startActivities();
                    // else just go to the parent activity that this was called from:
                    Log.d(TAG, "ran TaskStackBuilder.create...");
                }
                return true;

            case R.id.action_share:
                Util.share(mArticleFrag.getArticleTitle(), mArticleFrag.getArticleShareMessage(),
                            getResources().getString(R.string.article_share_message), this);
                Log.d(TAG, "article shared: " + mArticleFrag.getArticleTitle());
                return true;

            case R.id.article_refresh:
                Log.d(TAG, "article refresh.");
                mArticleFrag.refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String toString() {
        return TAG;
    }

    @Override
    public void onArticleFragmentInitialized() {
//        if (mLoadingFrag != null) {
//            FragmentManager fm = getFragmentManager();
//            fm.beginTransaction()
//                    .remove(mLoadingFrag)
//                    .commit();
//        } else {
//            Log.e(TAG, "mLoadingFrag should not be null!");
//        }
    }
}