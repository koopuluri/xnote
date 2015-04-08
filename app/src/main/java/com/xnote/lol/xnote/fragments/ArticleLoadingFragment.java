package com.xnote.lol.xnote.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.xnote.lol.xnote.R;

/**
 * A blank fragment with a loading animation in the middle.
 * Created by koopuluri on 2/25/15.
 */
public class ArticleLoadingFragment extends Fragment {
    public static final String TAG = "ArticleLoadingFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_article_load, container, false);
        ProgressBar loadingBar = (ProgressBar) view.findViewById(
                R.id.loading_article_fragment_progress_bar);
        loadingBar.setVisibility(View.VISIBLE);
        return view;
    }
}
