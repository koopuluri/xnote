package com.xnote.wow.xnote.adapters;

import android.app.Fragment;
import android.content.Context;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.models.SearchResult;

import java.util.Arrays;
import java.util.List;

/**
 * Created by koopuluri on 2/3/15.
 */
public class SearchResultAdapter extends BaseListAdapter {
    public static String TAG = "SearchResultAdapter";

    List<SearchResult> resultList;

    public SearchResultAdapter(Context context, List<Object> results, Fragment parent) {
        super(context, results, parent, R.layout.search_result_layout);
        resultList = (List<SearchResult>) (List<?>) results;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        SearchResult result = (SearchResult) getItem(position);
        TextView titleTextview = (TextView) view.findViewById(R.id.title_text_view);
        TextView tstampTextView = (TextView) view.findViewById(R.id.tstamp_text_view);
        SpannableString titleString = new SpannableString(result.title);
        titleTextview.setText(titleString);
        tstampTextView.setText(Util.dateFromSeconds(result.tstamp));
        if(result.type.equalsIgnoreCase(DB.NOTE)) {
            view.findViewById(R.id.adapter_item).setPadding(30, 0, 0, 0);
        } else {
            view.findViewById(R.id.adapter_item).setPadding(0, 0, 0, 0);
        }
        return view;
    }

    @Override
    public void addAll(Object... items) {
        super.addAll(items);
        resultList.addAll((List<SearchResult>) (List<?>) Arrays.asList(items));
    }

    @Override
    public void add(Object item) {
        super.add(item);
        resultList.add((SearchResult) item);
    }

    @Override
    public void clear() {
        super.clear();
        resultList.clear();
    }

    public List<SearchResult> getSearchResultList() {
        return resultList;
    }
}
