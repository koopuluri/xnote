package com.xnote.wow.xnote.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;

/**
 * Created by koopuluri on 3/21/15.
 */
public class NoArticlesFragment extends Fragment {
    TextView mNoArticlesMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_article, container, false);
        mNoArticlesMessage = (TextView) view.findViewById(R.id.no_articles_message);
        mNoArticlesMessage.setText(getActivity().getResources().
                getString(R.string.no_articles_message));
        Util.setXnoteNoteTypeFace(getActivity(), mNoArticlesMessage);
        return view;
    }
}
