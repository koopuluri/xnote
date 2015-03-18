package com.xnote.wow.xnote.spans;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import com.xnote.wow.xnote.NoteSpan;
import com.xnote.wow.xnote.buffers.BaseBuffer;

/**
 * Created by koopuluri on 2/16/15.
 */
public class SingleLineSpan extends ReplacementSpan implements NoteSpan {

    int mWidth;
    String mNoteId;

    public SingleLineSpan(String noteId) {
        super();
        mNoteId = noteId;
    }


    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        //return text with relative to the Paint
        mWidth = (int) paint.measureText(text, start, end);
        return mWidth;
    }


    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        //draw the frame with custom Paint
        int originalColor = paint.getColor();
        paint.setColor(Color.parseColor(BaseBuffer.NOTE_COLOR));
        canvas.drawRect(x, top + 10, x + mWidth, bottom - 10, paint);
        // drawText(CharSequence text, int start, int end, float x, float y, Paint paint)
        paint.setColor(Color.parseColor("#000000"));
        canvas.drawText(text, start, end, x, y, paint);
        // resetting original color:
        paint.setColor(originalColor);
    }

    public void reDrawWithNewColor(int newColor) {

    }
}