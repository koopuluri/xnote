package com.xnote.wow.xnote.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by koopuluri on 1/13/15.
 */
public class NoteView extends TextView {
    public static final String TAG = "NoteView";

    public interface TextSelectionChangedListener {
        public void onTextSelectionChanged(int newStart, int newEnd);
    }

    private Rect rect;
    private Canvas canvas;
    private Paint paint;
    TextSelectionChangedListener mListener;

    public NoteView(Context context) {
        super(context);
        rect = new Rect(0, 0, 0, 0);
        canvas = new Canvas();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        mListener = (TextSelectionChangedListener) context;
    }

    public NoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new Rect(0, 0, 0, 0);
        canvas = new Canvas();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        mListener = (TextSelectionChangedListener) context;

    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(rect, paint);
    }


    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        mListener.onTextSelectionChanged(selStart, selEnd);
    }
}
