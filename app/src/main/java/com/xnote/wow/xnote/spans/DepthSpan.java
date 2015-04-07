package com.xnote.wow.xnote.spans;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.style.LineBackgroundSpan;

import com.xnote.wow.xnote.NoteSpan;
import com.xnote.wow.xnote.buffers.BaseBuffer;

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
    }


    @Override
    public void drawBackground(Canvas canvas, Paint p, int left, int right, int top, int baseline,
                               int bottom, CharSequence text, int start, int end, int lnum) {
        int originalColor = p.getColor();
        // setting color:
        p.setColor(Color.parseColor(BaseBuffer.NOTE_COLOR));

        Rect rect = new Rect();

        // actually though:
        if (startIndex >= start && startIndex <= end) {
            if (endIndex >= start && endIndex <= end) {
                rect.left = getPosition(startIndex);
                rect.right = getPosition(endIndex) ;
                rect.top = top; // TODO: removed "-20" and the result is the same !?
                rect.bottom = baseline;
            } else {  // note spans multiple lines:
                rect.left = getPosition(startIndex);
                rect.right = (int) mLayout.getPrimaryHorizontal(end-1);  // test!
                rect.top = top;
                rect.bottom = bottom;
            }
        } else {
            if (endIndex >= start && endIndex <= end) {  // last line of span:
                rect.left = (int) mLayout.getPrimaryHorizontal(start);
                rect.right = getPosition(endIndex);
                rect.top = top;
                rect.bottom = baseline + (bottom - baseline) / 2;
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
        return linePosition;
        // return 0;
    }
}