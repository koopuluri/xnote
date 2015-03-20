package com.xnote.wow.xnote.buffers;

import android.app.Activity;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

import com.xnote.wow.xnote.NoteSpan;
import com.xnote.wow.xnote.fragments.ArticleFragment;
import com.xnote.wow.xnote.models.ParseNote;
import com.xnote.wow.xnote.spans.DepthSpan;
import com.xnote.wow.xnote.spans.FirstLineSpan;
import com.xnote.wow.xnote.spans.NoteReadSpan;
import com.xnote.wow.xnote.spans.SingleLineSpan;

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
    // public static final String NOTE_COLOR = "#FFF9C4";
    public static final String NOTE_COLOR = "#FFEB3B";
    public static final String SELECTION_COLOR = "#FDD835";

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

//
//    public void onTextSelected(int start, int end) {
//        // setSpanColorInRange(start, end);
//    }
//
//
//    public void onSelectionDone() {
//    }
//
//    /**
//     * HACK METHOD: If any SingleLineSpans or FirstLineSpans in range, change their colors
//     * to accent_dark
//     * @param start
//     * @param end
//     */
//    private void setSpanColorInRange(int start, int end) {
//        NoteSpan[] spans = mBuffer.getSpans(start, end, NoteSpan.class);
//        for (NoteSpan span : spans) {
//            try {
//                SingleLineSpan singleLineSpan = (SingleLineSpan) span;
//                mBuffer.setSpan(new SingleLineSpan(SELECTION_COLOR),
//                        mBuffer.getSpanStart(singleLineSpan),
//                        mBuffer.getSpanEnd(singleLineSpan),
//                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                mBuffer.removeSpan(singleLineSpan);
//            } catch (ClassCastException e) {
//                // do nothing.
//            } try {
//                FirstLineSpan firstLineSpan = (FirstLineSpan) span;
//                mBuffer.setSpan(new FirstLineSpan(SELECTION_COLOR),
//                        mBuffer.getSpanStart(firstLineSpan),
//                        mBuffer.getSpanEnd(firstLineSpan),
//                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                mBuffer.removeSpan(firstLineSpan);
//            } catch (ClassCastException e) {
//                // do nothing.
//            }
//        }
//    }
//
//
//    private void removeSelectionColoredSpans(int start, int end) {
//        if (start > mCurrentStart) {
//
//        }
//    }

    public void addNoteSpan(ParseNote note) {
        int start = note.getStartIndex();
        int end = note.getEndIndex();
        String noteId = note.getId();

        int firstLine = mLayout.getLineForOffset(start);
        int firstLineEndOffset = mLayout.getLineEnd(firstLine);
        Log.d(TAG, "addNoteReadSpan() with noteId: " + noteId);
        // NoteReadSpan span = new NoteReadSpan(R.color.blue, noteId, parentActivity);
        // FrameSpan span = new FrameSpan();
        // DepthSpan span = new DepthSpan(mLayout, noteId, Color.BLUE, start, end);
        if (mLayout.getLineForOffset(start) == mLayout.getLineForOffset(end)) {
            Log.d(TAG, "SAME LINE!");
            // this note spans single line.
            SingleLineSpan span = new SingleLineSpan(NOTE_COLOR);
            mBuffer.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            Log.d(TAG, "DIFFERENT LINES!");
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
        if (spans.length == 0) Log.d(TAG, "No note spans available to remove!");
        if (spans.length > NUM_SPANS) Log.e(TAG, "Too many spans for a single note.");
        for (NoteSpan span : spans) {
            mBuffer.removeSpan(span);
            Log.d(TAG, "a span removed");
        }
    }

    public SpannableString getBuffer() {
        return mBuffer;
    }

    public void setLayout(Layout layout) {
        mLayout = layout;
    }
}
