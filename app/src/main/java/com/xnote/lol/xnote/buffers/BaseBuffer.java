package com.xnote.lol.xnote.buffers;

import android.app.Activity;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;

import com.xnote.lol.xnote.NoteSpan;
import com.xnote.lol.xnote.fragments.ArticleFragment;
import com.xnote.lol.xnote.models.ParseNote;
import com.xnote.lol.xnote.spans.DepthSpan;
import com.xnote.lol.xnote.spans.FirstLineSpan;
import com.xnote.lol.xnote.spans.NoteReadSpan;
import com.xnote.lol.xnote.spans.SingleLineSpan;

/**
 * There's quite a bit of nonsense going on here that, I don't know why, works.
 * The main motivation behind this nonsense is to create boundaries for notes so that when
 * multiple notes are set next to each other, the user can distinguish between them.
 *
 * Created by koopuluri on 2/18/15.
 */
public abstract class BaseBuffer {
    public static final String TAG = "BaseBuffer";
    public static final int NUM_SPANS = 4;  // this is the number of spans in the buffer for one note.
    public static final String NOTE_COLOR = "#D9FFFF00";  // using 50% transparency

    Layout mLayout;
    Activity mActivity;
    SpannableString mBuffer;
    String mArticleId;

    public BaseBuffer(Layout textLayout, String articleId, Activity parent, Spanned content) {
        mLayout = textLayout;
        mArticleId = articleId;
        mActivity = parent;
        mBuffer = new SpannableString(content);
    }

    public void addNoteSpan(ParseNote note) {
        int start = note.getStartIndex();
        int end = note.getEndIndex();
        String noteId = note.getId();

        int firstLine = mLayout.getLineForOffset(start);
        int firstLineEndOffset = mLayout.getLineEnd(firstLine);
        if (mLayout.getLineForOffset(start) == mLayout.getLineForOffset(end)) {
            // this note spans single line.
            SingleLineSpan span = new SingleLineSpan(NOTE_COLOR);
            mBuffer.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            // adding first line separately:
            if (firstLine != 0) {
                // KIDS, DON'T TRY THIS AT HOME! If firstLine is first line of the text then don't add FirstLineSpan,
                // DepthSpan will take care of all the business.
                FirstLineSpan firstLineSpan = new FirstLineSpan(NOTE_COLOR);
                mBuffer.setSpan(firstLineSpan, start, firstLineEndOffset-1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            DepthSpan span = new DepthSpan(mLayout, noteId, start, end);
            mBuffer.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // adding the transparent clickableSpan layer:
        mBuffer.setSpan(new NoteReadSpan(noteId, mActivity, ArticleFragment.TAG),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void removeNoteSpan(ParseNote note) {
        NoteSpan[] spans = mBuffer.getSpans(note.getStartIndex(), note.getEndIndex(), NoteSpan.class);
        for (NoteSpan span : spans) {
            mBuffer.removeSpan(span);
        }
    }

    public SpannableString getBuffer() {
        return mBuffer;
    }

    public void setLayout(Layout layout) {
        mLayout = layout;
    }
}
