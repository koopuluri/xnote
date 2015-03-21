package com.xnote.wow.xnote.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;

import com.xnote.wow.xnote.R;

/**
 * Created by koopuluri on 2/28/15.
 */
public class SearchFragment extends Fragment {
    public static final String TAG = "SearchFragment";

    SearchView mSearchView;
    SearchResultsFragment mSearchResultsFrag;
    FrameLayout mSearchResultsContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search, container, false);

        // search related views:
        mSearchView = (SearchView) view.findViewById(R.id.search_view);
        mSearchResultsContainer = (FrameLayout) view.findViewById(R.id.search_results_container);

        // setting search functionality:
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newQuery) {
                if (mSearchResultsFrag == null) {
                    addSearchResultsFragment(newQuery);
                }
                else {
                    mSearchResultsFrag.updateResults(newQuery);
                }
                return true;
            }
        });
        return view;
    }

    public void addSearchResultsFragment(String query) {
        FragmentManager fm = getFragmentManager();
        SearchResultsFragment searchResultsFrag =
                (SearchResultsFragment) SearchResultsFragment.newInstance(query);

        if (mSearchResultsFrag != null) {
            // no need to add, but just update:
            mSearchResultsFrag.updateResults(query);
            return;
        }

        fm.beginTransaction()
                .add(R.id.search_results_container, searchResultsFrag, SearchResultsFragment.TAG)
                .addToBackStack(SearchResultsFragment.TAG)
                .commit();
        mSearchResultsFrag = searchResultsFrag;

    }
}