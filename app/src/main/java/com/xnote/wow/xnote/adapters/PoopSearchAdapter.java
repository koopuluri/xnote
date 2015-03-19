package com.xnote.wow.xnote.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.models.SearchResult;

import java.util.List;

/**
 * Created by koopuluri on 2/3/15.
 */
public class PoopSearchAdapter extends ArrayAdapter {
    public static String TAG = "SearchResultAdapter";

    public PoopSearchAdapter(Context context, List<SearchResult> results) {
        super(context, R.layout.search_result_layout, results);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.search_result_layout, null);
        }  // else: convert View is recycled and is not null:

        SearchResult result = (SearchResult) getItem(position);

        TextView titleTextview = (TextView) convertView.findViewById(R.id.title_text_view);
        TextView tstampTextView = (TextView) convertView.findViewById(R.id.tstamp_text_view);

        SpannableString titleString = new SpannableString(result.title);

        // making it bold and bigger:
        titleString.setSpan(new StyleSpan(Typeface.BOLD | Typeface.ITALIC), 0, result.title.length(), 0);
        titleTextview.setText(titleString);
        titleTextview.append("");
        tstampTextView.setText(Util.dateFromSeconds(result.tstamp));

        // adding the blank:
        return convertView;
    }
}