package com.xnote.wow.xnote.adapters;

import android.app.Fragment;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xnote.wow.xnote.DB;
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
        LinearLayout noteLayout = (LinearLayout) view.findViewById(R.id.note_text_layout);
        String titleString = result.title;
        if(titleString.length() > 90) {
            titleString = titleString.substring(0, 85 ) + "...";
        }
        titleTextview.setText(titleString);
        if(result.type.equalsIgnoreCase(DB.NOTE)) {
            noteLayout.setVisibility(View.VISIBLE);
            view.findViewById(R.id.adapter_item).setPadding(60, 0, 0, 0);
        } else {
            view.findViewById(R.id.adapter_item).setPadding(0, 0, 0, 0);
            tstampTextView.setText(Util.dateFromSeconds(result.tstamp));
            noteLayout.setVisibility(View.INVISIBLE);
        }
        return view;
    }
}
