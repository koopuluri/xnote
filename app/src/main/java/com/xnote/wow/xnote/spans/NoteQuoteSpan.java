package com.xnote.wow.xnote.spans;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.QuoteSpan;

import com.xnote.wow.xnote.R;

/**
 * Created by koopuluri on 3/17/15.
 */
public class NoteQuoteSpan extends QuoteSpan {
    private static final int STRIPE_WIDTH = 5;

    public NoteQuoteSpan() {
        super(R.color.accent_color_dark);
    }

    @Override
    public void drawLeadingMargin (Canvas c, Paint p, int x, int dir, int top, int baseline,
                                   int bottom, CharSequence text, int start, int end, boolean first,
                                   Layout layout) {
        // setting a gap between the quotespan and the quoted text:
        Paint.Style style = p.getStyle();
        int color = p.getColor();
        p.setStyle(Paint.Style.FILL);
        p.setColor(getColor());
        c.drawRect(x, top + 3, x + dir * STRIPE_WIDTH, bottom + 3, p);
        p.setStyle(style);
        p.setColor(color);
    }
}
