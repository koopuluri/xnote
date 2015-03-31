package com.xnote.wow.xnote.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.xnote.wow.xnote.ParseArticleManager;
import com.xnote.wow.xnote.ParseCallback;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.fragments.ArticleListFragment;
import com.xnote.wow.xnote.fragments.SearchFragment;
import com.xnote.wow.xnote.fragments.SearchRetainedFragment;
import com.xnote.wow.xnote.fragments.SettingsFragment;
import com.xnote.wow.xnote.models.ParseArticle;

/**
 * Created by koopuluri on 2/28/15.
 */
public class MainActivity extends Activity implements SearchFragment.OnItemDeleted,
                                                      ArticleListFragment.ArticleListListener {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPagerAdapter = new PagerAdapter(this);
        mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);

        final Activity me = this;

        mArticleManager = ParseArticleManager.getInstance();
        if (savedInstanceState != null)
            currentPosition = savedInstanceState.getInt(CURRENT_POSITION);  // so the fragment that was last viewed can be given focus.
        // action bar shii:
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                        currentPosition = position;
                        if (currentPosition != 1) {
                            try {
                                Util.hideKeyboard(me);
                            } catch (NullPointerException e) {
                                Log.e(TAG, "getWindowToken() on the activity's getWindow() returns null" +
                                        "so not hiding the keyboard: " + e);
                            }
                        }
                    }
                });
        // specifying that tabs should be displayed in actionbar:
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // creating a Tab listener called when user changes tabs:
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };
        // add 3 tabs specifying tab's next and tabListener??
        for (String tabName : TABS) {
            ActionBar.Tab tab = actionBar.newTab();
            //tab.setText(tabName);
            tab.setTabListener(tabListener);
            tab.setIcon(getTabIconImage(tabName));
            actionBar.addTab(tab);
        }
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
    public void onItemDeleted() {
        // need to refresh ArticleListFragment:
        Log.d(TAG, "Refresh the article list fragment");
        mArticleListFrag.localRefresh();
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


    private int getTabIconImage(String tabName) {
        switch(tabName) {
            case(SEARCH_TAB):
                return R.drawable.ic_xnote_search;
            case(ARTICLES_TAB):
                return R.drawable.ic_xnote_article_list;
            case(SETTINGS_TAB):
                return R.drawable.ic_xnote_settings;
        }
        return R.drawable.ic_launcher;
    }
}
