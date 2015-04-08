package com.xnote.lol.xnote.spans;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.NoteSpan;
import com.xnote.lol.xnote.fragments.ArticleFragment;

/**
 * Created by koopuluri on 1/26/15.
 */
public class NoteReadSpan extends ClickableSpan implements NoteSpan {
    public static final String TAG = "ClickableSpan";

    String noteId;
    TextPaint textPaint;
    Activity parentActivity;
    String mArticleId;

    public NoteReadSpan(String noteId, Activity activity, String parentName) {
        this.noteId = noteId;
        parentActivity = activity;
    }


    @Override
    public void onClick(View v) {
        // launch notefrag containing the note associated with this span.
        // NOTE: If this doesn't work, set movementMethod for the textView in ReadFragment!
        // cool vibrating effect:
        Vibrator vibrator = (Vibrator) v.getContext()
                .getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(20);
        Controller.launchNoteActivity(parentActivity, mArticleId, noteId, ArticleFragment.TAG);
    }


    @Override
    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        textPaint.setUnderlineText(false);
    }
}
