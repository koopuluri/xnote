package com.xnote.wow.xnote.adapters;

import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.xnote.wow.xnote.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koopuluri on 3/2/15.
 */
public abstract class BaseListAdapter extends ArrayAdapter {
    public static final String TAG = "BaseListAdapter";

    Fragment parentFrag;
    List<Integer> mSelectedPositions;
    int mLayout;

    public BaseListAdapter(Context context, List<Object> articles, Fragment parent,
                           int layout) {
        super(context, layout, articles);
        mSelectedPositions = new ArrayList<>();
        parentFrag = parent;
        mLayout = layout;

    }

    public void addSelection(int position) {
        mSelectedPositions.add(position);
        Log.d(TAG, "selection added at position: " + position);
    }

    public void removeSelection(int position) {
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            if (mSelectedPositions.get(i) == position) {
                mSelectedPositions.remove(i);
                return;
            }
        }
    }


    public void removeItemsAtIndices(List<Integer> indices) {
        List<Object> retainedItems = new ArrayList<Object>();
        for (int i = 0; i < getCount(); i++) {
            if (!indices.contains(i)) {
                retainedItems.add(getItem(i));
            }
        }
        clear();
        addAll(retainedItems);
    }

    public List<Integer> getSelectedPositions() {
        Log.d(TAG, "selected positions: " + String.valueOf(mSelectedPositions));
        return mSelectedPositions;
    }

    @Override
    public void clear() {
        super.clear();
        mSelectedPositions = new ArrayList<>();
    }



    public View getView(int pos, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null)
            view = View.inflate(getContext(), mLayout, null);
        else view = convertView;

        if (mSelectedPositions.contains(pos)) {
            Log.d(TAG, "getView(); this is a selected position.");
            view.setBackgroundColor(parentFrag.getActivity().getResources().getColor(R.color.xnote_color_light));
        } else {
            view.setBackgroundColor(parentFrag.getActivity().getResources().getColor(R.color.white));
        }

        return view;
    }
}
