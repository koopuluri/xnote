package com.xnote.lol.xnote;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import com.xnote.lol.xnote.spans.NoteReadSpan;
import com.xnote.lol.xnote.spans.WordSpan;

// adapted from: http://stackoverflow.com/a/7292485ss44
public class LinkTouchMovementMethod extends LinkMovementMethod {
    public static final String TAG = "LinkTouchMovementMethod";
    String articleId;
    Activity parentActivity;

    // the start and end spans of the highlight:
    int startSpan;
    int endSpan;

    public LinkTouchMovementMethod(Activity activity, String articleId) {
        super();
        parentActivity = activity;
        this.articleId = articleId;
    }

    private void setHighlightSpan(Spannable buffer) {
        if (startSpan > endSpan) {
            buffer.setSpan(new BackgroundColorSpan(0xFFFFFF00), endSpan, startSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return;
        }

        buffer.setSpan(new BackgroundColorSpan(0xFFFFFF00), startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    /**
     * Removes all BackgroundColorSpans (highlights) that are not NoteSpans (used to represent previously highlighted text).
     * @param buffer
     */
    private static void removeHighlightSpans(Spannable buffer) {
        // CHANGE: ONLY GET SPANS FROM STARTSPAN --> ENDSPAN (OR OTHER WAY IF ENDSPAN < STARTSPAN)
        BackgroundColorSpan[] spans = buffer.getSpans(0, buffer.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : spans) {
            // skip if noteSpan:
            if (span.getClass().equals(NoteReadSpan.class)) {
               continue;
            }
            else {
                // remove:
                buffer.removeSpan(span);
            }
        }
    }


    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();

        x += widget.getScrollX();
        y += widget.getScrollY();

        x -= 45;
        y -= 45;

        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        WordSpan[] spans = buffer.getSpans(off, off, WordSpan.class);
        if (spans.length == 0) {
            // this might be a noteAnnotateSpan instead of a wordSpan:
            NoteReadSpan[] noteSpans = buffer.getSpans(off, off, NoteReadSpan.class);
            if (noteSpans.length != 0) {
                startSpan = -1;
                endSpan = -1;
                return true;  // don't highlight.
            }
            else {
                return true;  // no spans at touched region.
            }
        }

        // wordSpan found:
        WordSpan touchedSpan = spans[0];

        // now checking if this is coming out of a touchedSpan:
        if (startSpan < 0 && endSpan < 0) {
            startSpan = touchedSpan.start;
            endSpan = touchedSpan.end;
        }

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                // set the start and end WordSpan:
                startSpan = touchedSpan.start;
                endSpan = touchedSpan.end;
                setHighlightSpan(buffer);
                break;

            case MotionEvent.ACTION_MOVE:
                // change the endSpan and reset the highlightSpan:
                endSpan = touchedSpan.end;
                removeHighlightSpans(buffer);
                setHighlightSpan(buffer);
                widget.invalidate();
                break;

            case MotionEvent.ACTION_UP:
                Vibrator vibrator = (Vibrator) widget.getContext()
                        .getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(20);

                // THE FOLLOWING LINE COULD POOP, EXERCISE CAUTION!!

                // remove the highlight span there:
                removeHighlightSpans(buffer);

                if (startSpan <= endSpan)
                    Controller.onTextSelected(parentActivity, (SpannableString) buffer, articleId, startSpan, endSpan);
                else
                    Controller.onTextSelected(parentActivity, (SpannableString) buffer, articleId, endSpan, startSpan);
                break;
        }
        return true;
    }
}