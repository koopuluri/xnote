package com.xnote.wow.xnote.spans;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.style.LineBackgroundSpan;
import android.util.Log;

import com.xnote.wow.xnote.NoteSpan;

/**
 * Created by koopuluri on 1/30/15.
 */
public class DepthSpan implements LineBackgroundSpan, NoteSpan {
    public static final String TAG = "DepthSpan";
    String mNoteId;
    Layout mLayout;
    int startIndex;
    int endIndex;

    public DepthSpan(Layout layout, String noteId, int startIndex, int endIndex) {
        mNoteId = noteId;
        mLayout = layout;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        Log.d(TAG, String.format("DepthSpan() with startIndex: %s, endIndex: %s",
                startIndex, endIndex));
    }


    @Override
    public void drawBackground(Canvas canvas, Paint p, int left, int right, int top, int baseline,
                               int bottom, CharSequence text, int start, int end, int lnum) {
        int originalColor = p.getColor();
//        Log.d(TAG, "--------------------------------------------------------------------------------------------------------------------------");
//        Log.d(TAG, "drawBackground() for noteId: " + mNoteId);
//        Log.d(TAG, "left: " + left);
//        Log.d(TAG, "right: " + right);
//        Log.d(TAG, "top: " + top);
//        Log.d(TAG, "baseline: " + baseline);
//        Log.d(TAG, "bottom: " + bottom);
//        Log.d(TAG, "start: " + start);
//        Log.d(TAG, "end: " + end);
//        Log.d(TAG, "lnum: " + lnum);
//
//        // NOTE: mLayout.getLineEnd(lnum) just gives 'end'.!!!!
//
//        Log.d(TAG, "mLayout.getPrimaryHorizontal(start): " + mLayout.getPrimaryHorizontal(start));
//        Log.d(TAG, "mLayout.getPrimaryHorizontal(end): " + mLayout.getPrimaryHorizontal(end));

        // setting color:
        p.setColor(Color.parseColor("#6666E0FF"));

        Rect rect = new Rect();

        // actually though:
        if (startIndex >= start && startIndex <= end) {
            if (endIndex >= start && endIndex <= end) {
                Log.d(TAG, "spans only one line");
                rect.left = getPosition(startIndex);
                rect.right = getPosition(endIndex) ;
                rect.top = top - 20;
                rect.bottom = baseline;
            } else {  // note spans multiple lines:
                Log.d(TAG, "spans multiple lines");
                rect.left = getPosition(startIndex);
                rect.right = (int) mLayout.getPrimaryHorizontal(end-1);  // test!
                rect.top = top - 20;
                rect.bottom = bottom;
            }
        } else {
            if (endIndex >= start && endIndex <= end) {  // last line of span:
                rect.left = (int) mLayout.getPrimaryHorizontal(start);
                rect.right = getPosition(endIndex);
                rect.top = top;
                rect.bottom = baseline;
            } else {  // a line in the middle of the note, no margins:
                // draw(mLayout.getLineStart(lnum), mLayout.getLineEnd(lnum), false, false);
                rect.left = (int) mLayout.getPrimaryHorizontal(start);
                rect.right = (int) mLayout.getPrimaryHorizontal(end - 1);
                rect.top = top;
                rect.bottom = bottom;
            }
        }

        canvas.drawRect(rect, p);

        // resetting color:
        p.setColor(originalColor);
    }



    /**
     *
     * @param charIndex: index of a character in the line
     * @return the position of the character in the line (screen position??).
     */
    private int getPosition(int charIndex) {
        // TODO:
        int linePosition = (int)mLayout.getPrimaryHorizontal(charIndex);  // this doesn't seem to return the right value.
        // Log.d(TAG, String.format("getPosition(%s): %s", charIndex, linePosition));
        return linePosition;
        // return 0;
    }
}