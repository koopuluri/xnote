package com.xnote.wow.xnote;

import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by koopuluri on 2/23/15.
 */
public class TextSelectionCallback implements ActionMode.Callback {
    public static final String TAG = "TextSelectionCallback";

    OnTextSelectionListener mListener;

    public interface OnTextSelectionListener {
        public void onTextSelectionCreate();
        public void onTextSelectionDestroy();
    }

    public TextSelectionCallback(OnTextSelectionListener listener) {
        super();
        mListener = listener;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Log.d(TAG, "onActionItemClicked");
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        Log.d(TAG, "onCreateActionMode()");
        mListener.onTextSelectionCreate();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mListener.onTextSelectionDestroy();
        Log.d(TAG, "onDestroyActionMode()");
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Log.d(TAG, "onPrepareActionMode()");
        return true;
    }
}
