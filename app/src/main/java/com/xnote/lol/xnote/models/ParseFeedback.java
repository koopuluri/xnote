package com.xnote.lol.xnote.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.xnote.lol.xnote.DB;

/**
 * Created by vignesh on 3/19/15.
 */

@ParseClassName(DB.FEEDBACK)
public class ParseFeedback extends ParseObject {
    public static final String FEEDBACK_TYPE = "FeedbackType";
    public static final String COMMENTS = "Comments";
    public static final String USER = "User";

    //-----------------GETTERS---------------------------
    public String getFeedbackType() {
        return getString(FEEDBACK_TYPE);
    }

    public String getComments() {
        return getString(COMMENTS);
    }

    public String getUser() { return getString(USER); }

    //-----------------SETTERS---------------------------
    public void setFeedbackType(String feedbackType) {
        put(FEEDBACK_TYPE, feedbackType);
    }

    public void setComments(String comments) {
        put(COMMENTS, comments);
    }

    public void setUser(String username) { put(USER, username); }
}
