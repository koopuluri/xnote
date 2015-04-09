package com.xnote.lol.xnote.buffers;

import android.app.Activity;
import android.text.Layout;
import android.text.Spanned;

import com.xnote.lol.xnote.models.ParseNote;

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
        super.addNoteSpan(note);
    }
}