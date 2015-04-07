package com.xnote.lol.xnote.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.DB;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Search;
import com.xnote.lol.xnote.adapters.SearchResultAdapter;
import com.xnote.lol.xnote.models.SearchResult;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by koopuluri on 2/2/15.
 */
public class SearchResultsFragment extends ListFragment {
    public static final String TAG = "SearchResultsFragment";
    public static final String QUERY = "SearchResultsFragment.Query";

    String mQuery;
    SearchResultAdapter mAdapter;
    boolean mInitialized;
    OnItemDeleted mListener;

    public static Fragment newInstance(String query) {
        Bundle args = new Bundle();
        args.putString(QUERY, query);
        Fragment frag = new SearchResultsFragment();
        frag.setArguments(args);
        return frag;
    }


    public interface OnItemDeleted {
        public void onItemDeleted();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnItemDeleted) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement onItemDeleted().");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mQuery = args.getString(QUERY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // now retain the results:
        setRetainedResults(getCurrentSearchResultList());
    }

    private void setRetainedResults(List<SearchResult> results) {
        FragmentManager fm = getFragmentManager();
        SearchRetainedFragment searchRetainedFragment =
                (SearchRetainedFragment) fm.findFragmentByTag(SearchRetainedFragment.TAG);
        if (searchRetainedFragment != null) { // TODO: make sure this is always true! (it must be...)
            searchRetainedFragment.setResults(results);
        }
    }

    private List<SearchResult> getRetainedResults() {
        FragmentManager fm = getFragmentManager();
        SearchRetainedFragment searchRetainedFragment =
                (SearchRetainedFragment) fm.findFragmentByTag(SearchRetainedFragment.TAG);
        if (searchRetainedFragment != null) { // TODO: make sure this is always true! (it must be...)
            return searchRetainedFragment.getSearchResults();
        } else {
            return null;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        new UpdateSearchResultsTask();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);
        SearchView searchView = (SearchView) getActivity().findViewById(R.id.search_view);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) container.getLayoutParams();
        lp.topMargin = searchView.getHeight();
        container.setLayoutParams(lp);


        mAdapter = new SearchResultAdapter(getActivity(), new ArrayList<Object>(), this);  // initializing with an empty list.
        List<SearchResult> retainedResults = getRetainedResults();
        if (retainedResults != null) {
            mAdapter.addAll(retainedResults);
            setListAdapter(mAdapter);
            return view;
        }
        setListAdapter(mAdapter);
        mInitialized = false;
        new UpdateSearchResultsTask().execute();
        return view;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // when an article is selected from the Article List:
        SearchResult result = (SearchResult) getListAdapter().getItem(position);
        if (result.type.equals(DB.ARTICLE)) {
            // launching ArticleActivity with the note-id as an EXTRA for the intent:
            Controller.launchArticleActivity(getActivity(), result.id);
        } else if (result.type.equals(DB.NOTE)) {
            Controller.launchNoteActivity(getActivity(),
                                            result.articleId,
                                            result.id,
                                            SearchResultsFragment.TAG);
        } else {
            // do nothing.
        }
    }


    public void updateResults(String newQuery) {
        mQuery = newQuery;
        new UpdateSearchResultsTask().execute();
    }


    private class UpdateSearchResultsTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = "SearchResultsFragment.UpdateSearchResultsTask";

        private List<SearchResult> resultList;
        @Override
        protected void onPreExecute() {
            // TODO: launch spinner.
            mAdapter.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            resultList = new ArrayList<SearchResult>();
            if (!mQuery.equals("")) {
                resultList.addAll(Search.search(mQuery));
            }
            resultList.addAll(Search.search(mQuery));
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            super.onPostExecute(_);
            // TODO: turn off spinner.
            mAdapter.addAll(resultList);
            mAdapter.notifyDataSetChanged();
            if (!mInitialized) {
                ListView listView = getListView();
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                listView.setMultiChoiceModeListener(new ModeListener());
                mInitialized = true;
            }
        }
    }


    private class DeleteSelectedItemsTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = "DeleteSelectedItemsTask";
        List<Integer> selectedPositions;
        List<SearchResult> copiedItemList;
        @Override
        protected void onPreExecute() {
            selectedPositions = mAdapter.getSelectedPositions();
            copiedItemList = new ArrayList<SearchResult>();
            List<SearchResult> list = mAdapter.getSearchResultList();
            for (SearchResult r : list) {
                copiedItemList.add(r.clone());
            }
            mAdapter.removeItemsAtIndices(selectedPositions);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int pos : selectedPositions) {
                SearchResult result = copiedItemList.get(pos);
                if (result.type.equals(DB.ARTICLE)) {
                    DB.deleteArticle(result.id);
                } else if (result.type.equals(DB.NOTE)) {
                    DB.deleteNote(result.id);
                } else {
                    // do nothing.
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            super.onPostExecute(_);
            mListener.onItemDeleted();
        }
    }


    public List<SearchResult> getCurrentSearchResultList() {
        return mAdapter.getSearchResultList();
    }


    /**
     * Code obtained from: http://developer.android.com/guide/topics/ui/menus.html#CAB.
     */
    private class ModeListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            if (checked) {
                mAdapter.addSelection(position);
            } else {
                mAdapter.removeSelection(position);
            }

            // notifiying change in views:
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    new DeleteSelectedItemsTask().execute();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.list_menu_selection, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Here you can make any necessary updates to the activity when
            // the CAB is removed. By default, selected items are deselected/unchecked.
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }
    }
}