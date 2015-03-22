package com.xnote.wow.xnote.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.parse.ParseException;
import com.xnote.wow.xnote.Constants;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.models.ParseArticle;

import java.util.List;

/**
 * SwipeLayout code from: https://developer.android.com/samples/SwipeRefreshListFragment/src/
 * com.example.android.swiperefreshlistfragment/SwipeRefreshListFragment.html
 *
 * Created by koopuluri on 1/23/15.
 */
public class ArticleListFragment extends BaseSelectableListFragment  {
    public static final String TAG = "ArticleListFragment";
    boolean mInitialized;
    ArticleListListener mListener;
    boolean mContentViewSet;

    public interface ArticleListListener {
        public void parseArticle(ParseArticle article);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ArticleListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement ArticleListListener.");
        }
    }

    public ArticleListFragment() {
        super(ArticleListFragment.TAG);
        mContentViewSet = false;
    }

    public static ArticleListFragment newInstance(String newArticleId) {
        ArticleListFragment frag = new ArticleListFragment();
        Bundle args = new Bundle();
        args.putString(Constants.NEW_ARTICLE_ID, newArticleId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getActivity().getActionBar().setDisplayShowHomeEnabled(false);
        getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        mInitialized = false;
        getSwipeRefreshLayout().setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                List<Object> itemList = getItemList();
                for (Object obj : itemList) {
                    ParseArticle a = (ParseArticle) obj;
                    if (!a.isParsed()) {
                        // do nothing:
                        mSwipeRefreshLayout.setRefreshing(false);
                        Log.d(TAG, "onRefresh() is cancelling.");
                        return;
                    }
                }
                refresh();
            }
        });

        new ArticleListInitializationTask(getActivity(), false).execute();
        Log.d(TAG, "launched ArticleListInitializationTask!");
        mContentViewSet = true;
        return getSwipeRefreshLayout();
    }


    public void refresh() {
        List<Object> itemList = getItemList();
        for (Object obj : itemList) {
            ParseArticle a = (ParseArticle) obj;
            if (!a.isParsed()) {
                Log.d(TAG, "there's an article that's not parsed, so not refreshing.");
                mSwipeRefreshLayout.setRefreshing(false);
                return;
            }
        }
        new ArticleListInitializationTask(getActivity(), true).execute();
    }


    public void localRefresh() {
        new ArticleListInitializationTask(getActivity(), false).execute();
    }


    //TODO: Check if this is doing anything
    @Override
    public void onConfigurationChanged(Configuration config) {
        List<Object> itemList = getItemList();
        for (Object obj : itemList) {
            ParseArticle a = (ParseArticle) obj;
            if (!a.isParsed()) {
                Log.d(TAG, "there's an article that's not parsed, so not changing configuration.");
                return;
            }
        }
        super.onConfigurationChanged(config);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // when an article is selected from the Article List:
        ParseArticle a = (ParseArticle) getListAdapter().getItem(position);
        Log.d(TAG, String.format("article '%s' was clicked!", a.getTitle()));
        if (a.isParsed()) {
            Controller.launchArticleActivity(getActivity(), a.getId());
            Log.d(TAG, "article already parsed, launching ArticleActivity with articleId: "
                    + a.getId());
        } else {
            Log.d(TAG, "article not loaded, doing nothing with this click. " + a.getId());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        new ArticleListInitializationTask(getActivity(), false).execute();
    }



    private List<ParseArticle> initializeAdapterList(Boolean fromCloud) {
        Log.d(TAG, "initializeAdapter");
        List<ParseArticle> articleList;
        if((fromCloud) && (Util.isNetworkAvailable(getActivity())) && (!Util.IS_ANON)) {
            //Don't need to check if any article is being parsed
            //because the check is being made in the refresh method
            //Local database is cleared and is synced with the cloud
            Log.d(TAG, "Clearing local datastore and syncing from cloud");
            try {
                DB.sync();
            } catch (ParseException e) {
                Log.d(TAG, "sync failed.");
                Toast.makeText(getActivity(), "Refresh failed. Check connection.",
                        Toast.LENGTH_SHORT)
                .show();
            }
            articleList = DB.getArticlesLocally();
        } else {
            articleList = DB.getArticlesLocally();
            Log.d(TAG, "Article List: " + String.valueOf(articleList));
            for (ParseArticle a : articleList) {
                if (!a.isParsed()) {
                    a.setCouldNotBeParsed(false);
                    mListener.parseArticle(a);
                    Log.d(TAG, "article is not parsed, telling parent Activity to parse article "
                            + a.toString());
                }
            }
            Log.d(TAG, "initializeAdapter, articleList output: " + String.valueOf(articleList));
        }
        return articleList;
    }

    public void onParseArticleCompleted(ParseArticle updatedArticle) {
        List<Object> itemList = getItemList();
        for (int i = 0; i < itemList.size(); i++) {
            ParseArticle a = (ParseArticle) itemList.get(i);
            if (a.getId().equals(updatedArticle.getId())) {
                // replace:
                itemList.set(i, updatedArticle);
                // since itemList points to adapter's internal mList, this should work.
                mAdapter.notifyDataSetChanged();
                return;
            }
        }
        Log.e(TAG, "article wasn't in the list!");
        // new ArticleListInitializationTask(false);
    }

    public void onParseArticleFailed() {
        mAdapter.notifyDataSetChanged();
    }


    @Override
    protected void deleteSelectedItems() {
        List<Integer> selectedItemPositions = getAdapter().getSelectedPositions();
        Log.d(TAG, "selected positions: " + String.valueOf(selectedItemPositions));
        List<Object> itemList = getItemList();
        Log.d(TAG, "selected item list: " + String.valueOf(itemList));
        for (int pos : selectedItemPositions) {
            try {
                ParseArticle a = (ParseArticle) itemList.get(pos);
                DB.deleteArticleInBackground(a); // article is deleted in background.
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, String.format("index out of bounds, length of itemList: %s," +
                                " selected item positions: %s",
                        itemList.size(),
                        String.valueOf(selectedItemPositions)));
            }
        }
        mAdapter.removeItemsAtIndices(selectedItemPositions);
        // now either resetting the adapter or displaying the no_articles_message.
        if (getItemList().size() == 0) {
            showNoArticleMessage();
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }


    private class ArticleListInitializationTask extends AsyncTask<Void, Void, Void> {
        List<ParseArticle> articles;
        final Activity activity;
        Boolean fromCloud;

        public ArticleListInitializationTask(Activity activity, Boolean fromCloud) {
            this.activity = activity;
            this.fromCloud = fromCloud;
        }


        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }


        @Override
        protected Void doInBackground(Void... params) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            articles = initializeAdapterList(fromCloud);
            Log.d(TAG, "number of articles in ArticleInitTAsk.doInBackground(): " + String.valueOf(articles));
            return null;
        }


        @Override
        protected void onPostExecute(Void _) {
            super.onPostExecute(_);

            mAdapter.clear();
            mAdapter.addAll(articles);
            mAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            if(Util.IS_ANON) {
                if(articles.size() > Constants.TRIAL_ARTICLES) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
                    builder.setMessage(R.string.trial_expired_message);
                    builder.setPositiveButton("Sign Up", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Controller.launchSignUpFromAnonymousUser(activity);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            if (!mInitialized && mContentViewSet) {  // because there was a weird bug where swiping between fragments quickly
                // threw error: java.lang.IllegalStateException: Content view not yet created due to the line below.
                ListView listView = getListView();
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                listView.setMultiChoiceModeListener(getModeListener());  // TODO: why is this here? can't these 3 lines be placed in onCreateView?
                mInitialized = true;
            }
            if (articles.size() == 0) {
                if (mInitialized) {
                    // need to make listView invisible and display the message.
                    showNoArticleMessage();
                }
            } else {
                if (getListView().getVisibility() == View.GONE) {
                    showArticleList();
                }
            }
        }
    }

    public void showArticleList() {
        getListView().setVisibility(View.VISIBLE);
        getNoMessageView().setVisibility(View.GONE);
    }

    public void showNoArticleMessage() {
        getListView().setVisibility(View.GONE);
        getNoMessageView().setVisibility(View.VISIBLE);
    }
}
