package com.xnote.lol.xnote.activities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.viewpagerindicator.CirclePageIndicator;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.XnoteLogger;
import com.xnote.lol.xnote.fragments.TutorialFragment;


/**
 * Created by koopuluri on 4/5/15.
 */
public class TutorialActivity extends Activity {
    int position;
    PagerAdapter mPagerAdapter;
    ViewPager mViewPager;

    public static final String TAG = "TUTORIAL_ACTIVITY";

    XnoteLogger logger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new XnoteLogger(getApplicationContext());
        logger.log("TutorialActivity.onCreate", null);
        setContentView(R.layout.activity_tutorial);
        position = 0;
        mPagerAdapter = new PagerAdapter(this);
        mViewPager = (ViewPager)findViewById(R.id.view_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        CirclePageIndicator cpi = (CirclePageIndicator) findViewById(R.id.circle_page_indicator);
        cpi.setRadius(20);
        cpi.setFillColor(getResources().getColor(R.color.xnote_color));
        cpi.setViewPager(mViewPager);
    }

    @Override
    public void onDestroy() {
        logger.log("TutorialActivity.onDestroy", null);
        logger.flush();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tutorial_actions, menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // respond to action-up button:

            case R.id.action_cancel:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PagerAdapter extends android.support.v13.app.FragmentPagerAdapter {

        Activity mParent;
        public PagerAdapter(Activity parent) {
            super(parent.getFragmentManager());
            mParent = parent;
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 4;  // total
        }

        @Override
        public CharSequence getPageTitle(int pos) {
            return String.valueOf(pos);
        }
    }

}
