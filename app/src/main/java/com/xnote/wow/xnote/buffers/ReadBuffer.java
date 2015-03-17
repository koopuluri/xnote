package com.xnote.wow.xnote.buffers;

import android.app.Activity;
import android.text.Layout;
import android.text.Spanned;
import android.util.Log;

import com.xnote.wow.xnote.models.ParseNote;

/**
 * Created by koopuluri on 1/27/15.
 */
public class ReadBuffer extends BaseBuffer {

    public static final String TAG = "ReadBuffer";


    public ReadBuffer(Layout textLayout, String articleId, Activity parent, Spanned content) {
        super(textLayout, articleId, parent, content);
    }

    @Override
    public void addNoteSpan(ParseNote note){
        Log.d(TAG, "addNoteSpan() called on note: " + note.getStartIndex() + ", " + note.getEndIndex());
        super.addNoteSpan(note);
    }
}