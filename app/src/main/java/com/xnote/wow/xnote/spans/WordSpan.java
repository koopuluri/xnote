package com.xnote.wow.xnote.spans;

import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

/**
 * Created by koopuluri on 1/17/15.
 */
public class WordSpan extends CharacterStyle {

    public SpannableString str;
    public int start;
    public int end;

    public WordSpan(SpannableString str, int i, int j) {
        this.str = str;
        start = i;
        end = j;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setUnderlineText(false);
        tp.setAntiAlias(true);

    }
}
