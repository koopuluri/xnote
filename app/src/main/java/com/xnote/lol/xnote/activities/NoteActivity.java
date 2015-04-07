package com.xnote.lol.xnote.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;

import com.xnote.lol.xnote.Constants;
import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Util;
import com.xnote.lol.xnote.fragments.NoteFragment;
import com.xnote.lol.xnote.models.NoteEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by koopuluri on 1/29/15.
 *
 * Notes about the ViewPageAdapter:
 * - only used for old notes. New notes can't be swiped off screen to go to next note in order
 * because new notes aren't created until the user presses the doneButton.
 * ViewPager error fix: http://stackoverflow.com/questions/14766347/null-pointer-exception-in-fragment-on-callback-from-activity
 *
 * Really cool "bug": After adding a ScrollView in NoteFragment, when a keyboard is opened (the user
 * starts typing a note), the done button slides above the keyboard so the user can hit done while
 * typing; more intuitive than anything I thought of :/. The Android Gods have blessed us.
 */
public class NoteActivity extends Activity {
    public static final String TAG = "NoteActivity";

    NoteEngine mNoteEngine;
    NotePagerAdapter mNotePagerAdapter;
    ViewPager mViewPager;
    NoteFragment mNoteFrag;
    ImageButton mDoneButton;
    Map<Integer, NoteFragment> mNoteFragMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        // if the parent is ArticleActivity and this note is an old note:
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_xnote_navigation_up_colored);
        if (getIntent().getStringExtra(Constants.PARENT_ACTIVITY).equals(ArticleActivity.TAG)
                && getIntent().getExtras().containsKey(Constants.NOTE_ID)) {
            mNoteEngine = NoteEngine.getInstance();
            mNotePagerAdapter = new NotePagerAdapter(getFragmentManager());
            mViewPager = (ViewPager) findViewById(R.id.note_view_pager);
            mViewPager.setAdapter(mNotePagerAdapter);
            int currentNotePosition = mNoteEngine.getPositionForNoteId(getIntent()
                    .getStringExtra(Constants.NOTE_ID));
            mViewPager.setCurrentItem(currentNotePosition);
            mNoteFragMap = new HashMap<Integer, NoteFragment>();
        } else {
            mNoteEngine = null;
            // adding initial note frag.
            if (getIntent().getExtras().containsKey(Constants.NOTE_ID)) {
                mNoteFrag = NoteFragment.newInstance(getIntent().getStringExtra(Constants.NOTE_ID));
            } else {
                mNoteFrag = NoteFragment.newInstance(getIntent().getStringExtra(Constants.ARTICLE_ID),
                        getIntent().getIntExtra(Constants.START_INDEX, -1),
                        getIntent().getIntExtra(Constants.END_INDEX, -1));
            }
            if (mNoteFrag != null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.note_container, mNoteFrag, NoteFragment.TAG)
                        .commit();
            }
        }
        mDoneButton = (ImageButton) findViewById(R.id.done_button);
        mDoneButton.setColorFilter(Color.parseColor("#FFFFFFFF"));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    // Or read size directly from the view's width/height
                    int size = getResources().getDimensionPixelSize(R.dimen.round_button_diameter);
                    outline.setOval(0, 0, size, size);
                }
            };
            mDoneButton.setOutlineProvider(viewOutlineProvider);
            mDoneButton.setClipToOutline(true);
        }
        mDoneButton.setVisibility(View.VISIBLE);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentNoteFragment().done();
            }
        });
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
                    // this means that this activity is not part of this app's task,
                    // so create new task when navigating up, with synthesized (???) back stack.
                    TaskStackBuilder.create(this)
                            // Add all of activity's parents to back stack:
                            .addNextIntentWithParentStack(upIntent)
                            // navigate up to closest parent:
                            .startActivities();
                    // else just go to the parent activity that this was called from:
                } else {
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
                }
                return true;

            case R.id.note_delete_button:
                if (mNoteFrag != null) {
                    // this case is when Note launched through SearchResultsFrag, because there's not viewPager.
                    mNoteFrag.delete();
                } else {
                    // when Note launched from ArticleFrag; Viewpager exists:
                    NoteFragment noteFrag = mNoteFragMap.get(mViewPager.getCurrentItem());
                    noteFrag.delete();
                }
                return true;

            case R.id.note_action_share:
                // Add data to the intent, the receiving app will decide what to do with it.
                NoteFragment noteFrag = getCurrentNoteFragment();
                Util.share(noteFrag.getArticleTitle(), noteFrag.getNoteShareMessage(),
                    getResources().getString(R.string.note_share_message), this);
            }
        return super.onOptionsItemSelected(item);
    }


    private NoteFragment getCurrentNoteFragment() {
        if (mNoteFrag != null)
            return mNoteFrag;
        else
            return mNoteFragMap.get(mViewPager.getCurrentItem());
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
            mNoteFragMap.put(i, (NoteFragment) noteFrag);  // keeping reference to created fragments.
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
