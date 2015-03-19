package com.xnote.wow.xnote.spans;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

import com.xnote.wow.xnote.NoteSpan;
import com.xnote.wow.xnote.buffers.BaseBuffer;

/**
 * This hack shouldn't be required to get the desired functionality.
 * If there is a more straightforward way, then Android really needs better documentation / tutorials.
 * Created by koopuluri on 2/16/15.
 */
public class FirstLineSpan extends ReplacementSpan implements NoteSpan {
    int mWidth;
    String mColorString;

    public FirstLineSpan(String color) {
        mColorString = color;
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
        paint.setColor(Color.parseColor(mColorString));
        canvas.drawRect(x, top + 10, x + mWidth, bottom, paint);
        // drawText(CharSequence text, int start, int end, float x, float y, Paint paint)

        // drawing text in black:
        paint.setColor(Color.parseColor("#000000"));
        canvas.drawText(text, start, end, x, y, paint);

        // resetting original color.
        paint.setColor(originalColor);
    }

//    @Override
//    public void updateDrawState(TextPaint p) {
//        super.updateDrawState(p);
//        p.bgColor = Color.parseColor(BaseBuffer.NOTE_COLOR);
//    }
//
//    @Override
//    public void updateMeasureState(TextPaint p) {
//        super.updateMeasureState(p);
//        p.bgColor = Color.parseColor(BaseBuffer.NOTE_COLOR);
//    }
}
