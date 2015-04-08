package com.xnote.lol.xnote;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
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
        switch (item.getItemId()) {
            case R.id.action_delete_note:
                // delete note if
        }
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.article_fragment_text_selection_actions, menu);
        menu.removeItem(android.R.id.selectAll);
        mListener.onTextSelectionCreate();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mListener.onTextSelectionDestroy();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }
}
