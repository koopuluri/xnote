package com.xnote.wow.xnote.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.xnote.wow.xnote.Constants;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.fragments.NoteFragment;
import com.xnote.wow.xnote.models.NoteEngine;

/**
 * Created by koopuluri on 1/29/15.
 *
 * Notes about the ViewPageAdapter:
 * - only used for old notes. New notes can't be swiped off screen to go to next note in order
 * because new notes aren't created until the user presses the doneButton.
 */
public class NoteActivity extends Activity {
    public static final String TAG = "NoteActivity";

    NoteEngine mNoteEngine;
    NotePagerAdapter mNotePagerAdapter;
    ViewPager mViewPager;
    NoteFragment mNoteFrag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // if the parent is ArticleActivity and this note is an old note:
        if (getIntent().getStringExtra(Constants.PARENT_ACTIVITY).equals(ArticleActivity.TAG)
                && getIntent().getExtras().containsKey(Constants.NOTE_ID)) {
            Log.d(TAG, "parent is: " + ArticleActivity.TAG);
            mNoteEngine = NoteEngine.getInstance();
            mNotePagerAdapter = new NotePagerAdapter(getFragmentManager());
            mViewPager = (ViewPager) findViewById(R.id.note_view_pager);
            mViewPager.setAdapter(mNotePagerAdapter);
            int currrentNotePosition = mNoteEngine.getPositionForNoteId(getIntent()
                    .getStringExtra(Constants.NOTE_ID));
            mViewPager.setCurrentItem(currrentNotePosition);
        } else {
            mNoteEngine = null;
            // adding initial note frag.
            if (getIntent().getExtras().containsKey(Constants.NOTE_ID)) {
                Log.d(TAG, "Old note, but parent not ArticleActivity");
                mNoteFrag = NoteFragment.newInstance(getIntent().getStringExtra(Constants.NOTE_ID));
            } else {
                Log.d(TAG, "New Note.");
                mNoteFrag = NoteFragment.newInstance(getIntent().getStringExtra(Constants.ARTICLE_ID),
                        getIntent().getIntExtra(Constants.START_INDEX, -1),
                        getIntent().getIntExtra(Constants.END_INDEX, -1));
            }
            if (mNoteFrag != null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.note_container, mNoteFrag, NoteFragment.TAG)
                        .commit();
                Log.d(TAG, "NoteFragment added to note_container");
            }
        }
    }


    @Override
    public String toString() {
        return TAG;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_activity_actions, menu);
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
                } else {
                    Log.d(TAG, "NavUtils.navigateUpFromSameTask(this)");
                    // http://stackoverflow.com/a/16147110/2713471:
                    // launching intent based on parent:
                    if (getIntent().getStringExtra(Constants.PARENT_ACTIVITY).equals(ArticleActivity.TAG)) {
                        Intent intent = NavUtils.getParentActivityIntent(this);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra(Constants.ARTICLE_ID, getIntent().getStringExtra(
                                Constants.ARTICLE_ID));
                        NavUtils.navigateUpTo(this, intent);
                    } else {  // need to launch a parent PoopActivity:
                        Controller.launchArticleActivity(this,
                                getIntent().getStringExtra(Constants.ARTICLE_ID));  // mNote is not null here because note
                        // is loaded from DB as it was previously created. (It had to be
                        // previously created otherwise it couldn't show up as a search result).
                    }
                    return true;
                }
                return true;

            case R.id.note_delete_button:
                Log.d(TAG, "note to be deleted!");
                if (mNoteFrag != null) {
                    // this case is when Note launched through SearchResultsFrag, because there's not viewPager.
                    Log.d(TAG, "note deleted (launched from SearchResultsFrag.");
                    mNoteFrag.delete();
                } else {
                    // when Note launched from ArticleFrag; Viewpager exists:
                    NoteFragment noteFrag = (NoteFragment) mNotePagerAdapter.
                                                           getItem(mViewPager.getCurrentItem());
                    noteFrag.delete();
                    Log.d(TAG, "note deleted at position: " + mViewPager.getCurrentItem());
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    private class NotePagerAdapter extends FragmentStatePagerAdapter {
        public NotePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            String noteId = mNoteEngine.getNoteIdAtPos(i);
            Fragment noteFrag = NoteFragment.newInstance(noteId);
            return noteFrag;
        }

        @Override
        public int getCount() {
            return mNoteEngine.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }
}
