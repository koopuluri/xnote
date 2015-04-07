package com.xnote.wow.xnote.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.LinearLayout;

import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.ParseArticleManager;
import com.xnote.wow.xnote.ParseCallback;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.fragments.ArticleListFragment;
import com.xnote.wow.xnote.fragments.SearchFragment;
import com.xnote.wow.xnote.fragments.SearchRetainedFragment;
import com.xnote.wow.xnote.fragments.SettingsFragment;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.views.SlidingTabLayout;

/**
 * Created by koopuluri on 2/28/15.
 *
 * To run a block of code only when app is first installed: use sharedPreferences:
 * http://stackoverflow.com/a/7144339
 */
public class MainActivity extends Activity implements SearchFragment.SearchListener,
                                                      ArticleListFragment.ArticleListListener {

    public static final int HEIGHT_DIFF_KEYBOARD = 500;

    public static final String TAG = "MainActivity";
    public static final String ARTICLES_TAB = "Articles";
    public static final String SEARCH_TAB = "Search";
    public static final String SETTINGS_TAB = "Settings";
    public static final String CURRENT_POSITION = "CurrentPosition";
    public static final String[] TABS = {ARTICLES_TAB, SEARCH_TAB, SETTINGS_TAB};

    PagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    ArticleListFragment mArticleListFrag;
    SettingsFragment mSettingsFrag;
    SearchFragment mSearchFrag;
    int currentPosition;
    String newArticleId;
    ParseArticleManager mArticleManager;
    SearchRetainedFragment mSearchRetained;
    SlidingTabLayout mSlidingTabLayout;
    boolean mIsKeyboardVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.main);

        getActionBar().hide();

        final View activityRootView = findViewById(R.id.main_root);
        mIsKeyboardVisible = false;
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        //r will be populated with the coordinates of your view that area still visible.
                        activityRootView.getWindowVisibleDisplayFrame(r);
                        int heightDiff = activityRootView.getRootView().getHeight()
                                - (r.bottom - r.top);
                        if (heightDiff > HEIGHT_DIFF_KEYBOARD) { // if more than 100 pixels, its probably a keyboard...
                            mIsKeyboardVisible = true;
                        } else {
                            mIsKeyboardVisible = false;
                        }
                    }
                }
        );

        mPagerAdapter = new PagerAdapter(this);
        mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);

        mArticleManager = ParseArticleManager.getInstance();
        if (savedInstanceState != null)
            currentPosition = savedInstanceState.getInt(CURRENT_POSITION);  // so the fragment that was last viewed can be given focus.

        // getting the TabLayout:
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        // getting actionBarHeight for this device (toolBar in our case):
        // http://stackoverflow.com/a/7167086/2713471
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        mSlidingTabLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                actionBarHeight
        ));

        Log.d(TAG, "end of onCreate()");
        // getting / setting the SearchRetainedFrag:
        FragmentManager fm = getFragmentManager();
        mSearchRetained = (SearchRetainedFragment) fm
                .findFragmentByTag(SearchRetainedFragment.TAG);
        if (mSearchRetained == null) {
            mSearchRetained = new SearchRetainedFragment();
            fm.beginTransaction()
                    .add(mSearchRetained, SearchRetainedFragment.TAG)
                    .commit();
        }

        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        if (isFirstRun) {
            Controller.launchTutorialActivity(this);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION, currentPosition);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // setting the searchResults in searchRetained:
//        FragmentManager fm = getFragmentManager();
//        SearchResultsFragment searchResultsFrag =
//                (SearchResultsFragment) fm.findFragmentByTag(SearchResultsFragment.TAG);
//        if (searchResultsFrag != null) {
//            // TODO: this way or through the SearchResultInterface?
//            mSearchRetained.setResults(searchResultsFrag.getCurrentSearchResultList());
//            Log.d(TAG, "set SearchResults in SearchRetained.");
//        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onStop() {
        super.onStop();
        ParseArticleManager.cancelAll();
        Log.d(TAG, "onStop().");
    }


    private class PagerAdapter extends android.support.v13.app.FragmentPagerAdapter {
        Activity mParent;

        public PagerAdapter(Activity parent) {
            super(parent.getFragmentManager());
            mParent = parent;
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(TAG, "getItem() in PageAdapter");
            if (i == 0) {
                Log.d(TAG, "ArticleListFragment selected.");
                if (mArticleListFrag == null) {
                    mArticleListFrag = ArticleListFragment.newInstance(newArticleId);
                }
                return mArticleListFrag;
            } else if (i == 1) {
                if (mSearchFrag == null) {
                    mSearchFrag = new SearchFragment();
                }
                Log.d(TAG, "SearchFragment selected.");
                return mSearchFrag;
            } else if (i == 2) {
                if (mSettingsFrag == null) {
                    mSettingsFrag = new SettingsFragment();
                }
                Log.d(TAG, "SettingsFragment selected.");
                return mSettingsFrag;
            } else {
                Log.e(TAG, "no fragment chosen, this state should not be possible!");
                return null;
            }
        }

        @Override
        public int getCount() {
            return 3;  // total
        }

        @Override
        public CharSequence getPageTitle(int pos) {
            return String.valueOf(pos);  // TODO: change this to display TAB contents.
        }
    }

    @Override
    public void onSearchItemDeleted() {
        // need to refresh ArticleListFragment:
        Log.d(TAG, "Refresh the article list fragment");
        mArticleListFrag.localRefresh();
    }

    @Override
    public boolean keyboardVisible() {
        return mIsKeyboardVisible;
    }

    @Override
    public void parseArticle(ParseArticle article) {
        final String articleId = article.getId();
        ParseArticleManager.startParsing(articleId, new ParseCallback() {
            @Override
            public void run(ParseArticle updatedArticle) {
                Log.d(TAG, "ParseArticleCallBack ArticleListFragment : " +
                        String.valueOf(mArticleListFrag));
                mArticleListFrag.onParseArticleCompleted(updatedArticle);
            }

            public void runFailed() {
                Log.d(TAG, "ParseArticleCallBack ArticleListFragment : " +
                        String.valueOf(mArticleListFrag));
                mArticleListFrag.onParseArticleFailed();
            }
        });
    }
}
