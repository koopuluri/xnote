package com.xnote.wow.xnote.adapters;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.R;
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
        TextView hitsTextView = (TextView) view.findViewById(R.id.hits_text_view);
        TextView tstampTextView = (TextView) view.findViewById(R.id.tstamp_text_view);
        TextView blankTextView = (TextView) view.findViewById(R.id.blank);
        SpannableString titleString = new SpannableString(result.title);
        // making it bold and bigger:
        titleString.setSpan(new StyleSpan(Typeface.BOLD | Typeface.ITALIC), 0, result.title.length(), 0);
        titleTextview.setText(titleString);
        titleTextview.append("");
        hitsTextView.setText("hits: " + result.numHits);
        tstampTextView.setText(Util.dateFromSeconds(result.tstamp));
        // adding the blank:
        blankTextView.setText("");
        return view;
    }
}
