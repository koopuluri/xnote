package com.xnote.wow.xnote.spans;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.NoteSpan;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.fragments.ArticleFragment;

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
        Log.d(TAG, "launching noteFrag through clicking on a ReadSpan");
        // cool vibrating effect:
        Vibrator vibrator = (Vibrator) v.getContext()
                .getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(20);
        Controller.launchNoteActivity(parentActivity, mArticleId, noteId, ArticleFragment.TAG);
    }


    @Override
    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        // textPaint.setColor(ds.linkColor);
        // textPaint.bgColor = android.R.color.transparent;  // can't see this! But can touch this.
        //Remove default underline associated with spans
        //textPaint.bgColor = R.color.accent_color_light;
        textPaint.setUnderlineText(false);
    }
}
