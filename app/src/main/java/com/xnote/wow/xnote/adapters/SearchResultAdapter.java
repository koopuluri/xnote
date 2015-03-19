package com.xnote.wow.xnote.adapters;

import android.app.Fragment;
import android.content.Context;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.models.SearchResult;

import java.util.List;

/**
 * Created by koopuluri on 2/3/15.
 */
public class SearchResultAdapter extends BaseListAdapter {
    public static String TAG = "SearchResultAdapter";

    public SearchResultAdapter(Context context, List<Object> results, Fragment parent) {
        super(context, results, parent, R.layout.search_result_layout);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        SearchResult result = (SearchResult) getItem(position);
        TextView titleTextview = (TextView) view.findViewById(R.id.title_text_view);
        TextView tstampTextView = (TextView) view.findViewById(R.id.tstamp_text_view);
        TextView noteOrArticleTextView =(TextView) view.findViewById(R.id.note_or_article_text_view);
        SpannableString titleString = new SpannableString(result.title);
        // making it bold and bigger:
        titleTextview.setText(titleString);
        titleTextview.append("");
        tstampTextView.setText(Util.dateFromSeconds(result.tstamp));
        noteOrArticleTextView.setText(result.type);
        // adding the blank:
        return view;
    }
}
