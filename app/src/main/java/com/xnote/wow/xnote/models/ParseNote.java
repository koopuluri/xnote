package com.xnote.wow.xnote.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.xnote.wow.xnote.DB;

import java.util.UUID;

/**
 * Created by koopuluri on 2/5/15.
 */
@ParseClassName(DB.NOTE)
public class ParseNote extends ParseObject {
    public static final String TAG = "ParseNote";
    public static final String ARTICLE_ID = "articleId";
    public static final String CONTENT = "content";
    public static final String START_INDEX = "startIndex";
    public static final String END_INDEX = "endIndex";
    public static final String TIMESTAMP = "timestamp";
    public static final String ID = "id";
    public static final String SELECTED_TEXT = "selected_text";

    public ParseNote() {
        super();
    }


    // -----------------------------------GETTERS--------------------------------------------------
    public String getArticleId() {
        return getString(ARTICLE_ID);
    }

    public int getStartIndex() {
        return getInt(START_INDEX);
    }

    public int getEndIndex() {
        return getInt(END_INDEX);
    }

    public String getContent() {
        return getString(CONTENT);
    }

    public long getTimestamp() {
        return getLong(TIMESTAMP);
    }

    public String getId() {
        return getString(ID);
    }

    public String getSelectedText() { return getString(SELECTED_TEXT); }

    @Override
    public String toString() {
        return getContent();
    }

    // ------------------------------------------SETTERS------------------------------------------

    public void setId() {
        put(ID, UUID.randomUUID().toString());
    }

    public void setId(String id) { put(ID, id); }

    public void setArticleId(String id) {
        put(ARTICLE_ID, id);
    }

    public void setContent(String content) {
        put(CONTENT, content);
    }

    public void setStartIndex(int start) {
        put(START_INDEX, start);
    }

    public void setEndIndex(int end) {
        put(END_INDEX, end);
    }

    public void setTimestamp(long seconds) { put(TIMESTAMP, seconds); }

    public void setSelectedText(String selectedText) { put(SELECTED_TEXT, selectedText); }
}
