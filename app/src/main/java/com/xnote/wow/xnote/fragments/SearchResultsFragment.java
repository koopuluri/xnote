package com.xnote.wow.xnote.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Search;
import com.xnote.wow.xnote.adapters.SearchResultAdapter;
import com.xnote.wow.xnote.models.SearchResult;

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
        Log.d(TAG, "onCreate() complete, listAdapter set.");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
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
            Log.d(TAG, String.format("Article '%s' was clicked!", result.title));

            // launching ArticleActivity with the note-id as an EXTRA for the intent:
            Controller.launchArticleActivity(getActivity(), result.id);
        } else if (result.type.equals(DB.NOTE)) {
            Log.d(TAG, String.format("Note with note.id: '%s', was clicked!", result.id));
            Controller.launchNoteActivity(getActivity(),
                                            result.articleId,
                                            result.id,
                                            SearchResultsFragment.TAG);
        } else {
            Log.e(TAG, "Result type is neither an Article or Note; incompatible result type.");
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
            Log.d(TAG, "begun.");
            resultList = new ArrayList<SearchResult>();

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
                Log.d(TAG, "list view ModeListener set.");
                mInitialized = true;
            }
            Log.d(TAG, "completed.");
        }
    }

    private class DeleteSelectedItemsTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = "DeleteSelectedItemsTask";
        List<Integer> selectedPositions;

        @Override
        protected void onPreExecute() {
            selectedPositions = mAdapter.getSelectedPositions();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int pos : selectedPositions) {
                SearchResult result = (SearchResult) mAdapter.getItem(pos);
                if (result.type.equals(DB.ARTICLE)) {
                    DB.deleteArticle(result.id);
                    Log.d(TAG, "deleteSelectedItems(), article deleted with id: " + result.id);
                } else if (result.type.equals(DB.NOTE)) {
                    DB.deleteNote(result.id);
                    Log.d(TAG, "deleteSelectedItems(), note deleted with id: " + result.id);
                } else {
                    Log.e (TAG, "incorrect type for SearchResult.");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            super.onPostExecute(_);
            mAdapter.removeItemsAtIndices(selectedPositions);
            mAdapter.notifyDataSetChanged();
            mListener.onItemDeleted();
        }
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
                    Log.d(TAG, "menu_delete clicked. time to delete the selected articles");
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