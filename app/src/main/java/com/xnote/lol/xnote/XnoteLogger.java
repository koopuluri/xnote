package com.xnote.lol.xnote;

import android.content.Context;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

/**
 * Created by koopuluri on 4/16/15.
 */
public class XnoteLogger {
    MixpanelAPI mixpanel;

    public XnoteLogger(Context context) {
        mixpanel = MixpanelAPI.getInstance(context, Constants.MIXPANEL_TOKEN);
    }

    public void log(String eventName, JSONObject obj) {
        if (obj == null) {
            // initializing:
            obj = new JSONObject();
        }
        // now adding the global properties:
        try {
            obj.put("timestamp", System.currentTimeMillis());
            obj.put("SqlTimestamp", new Timestamp(System.currentTimeMillis()));
        } catch (JSONException e) {
            // do nothing.
        }
        mixpanel.track(eventName, obj);
    }

    public void deleteArticle(String articleId, String eventName) {
        mixpanel.getPeople().increment(Constants.NUMBER_ARTICLES, -1);
        JSONObject obj = new JSONObject();
        try {
            obj.put("ArticleId", articleId);
        } catch (JSONException e) {
            // do nothing.
        }
        log(eventName, obj);
    }

    public void addArticle(String articleId, String eventName) {
        mixpanel.getPeople().increment(Constants.NUMBER_ARTICLES, 1);
        JSONObject obj = new JSONObject();
        try {
            obj.put("ArticleId", articleId);
        } catch (JSONException e) {
            // do nothing.
        }
        log(eventName, obj);
    }

    public void deleteNote(String noteId, String eventName) {
        mixpanel.getPeople().increment(Constants.NUMBER_NOTES, -1);
        JSONObject obj = new JSONObject();
        try {
            obj.put("NoteId", noteId);
        } catch (JSONException e) {
            // do nothing.
        }
        log(eventName, obj);
    }

    public void addNote(String noteId, String eventName) {
        mixpanel.getPeople().increment(Constants.NUMBER_NOTES, 1);
        JSONObject obj = new JSONObject();
        try {
            obj.put("NoteId", noteId);
        } catch (JSONException e) {
            // do nothing.
        }
        log(eventName, obj);
    }

    public void flush() {
        mixpanel.flush();
    }

    public MixpanelAPI.People getPeople() {
        return mixpanel.getPeople();
    }

    public void identify(String id) {
        mixpanel.identify(id);
    }
}