package com.xnote.wow.xnote.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.xnote.wow.xnote.DB;

/**
 * Created by vignesh on 3/19/15.
 */

@ParseClassName(DB.FEEDBACK)
public class ParseFeedback extends ParseObject {
    public static final String FEEDBACK_TYPE = "FeedbackType";
    public static final String COMMENTS = "Comments";

    //-----------------GETTERS---------------------------
    public String getFeedbackType() {
        return getString(FEEDBACK_TYPE);
    }

    public String getComments() {
        return getString(COMMENTS);
    }

    //-----------------SETTERS---------------------------
    public void setFeedbackType(String feedbackType) {
        put(FEEDBACK_TYPE, feedbackType);
    }

    public void setComments(String comments) {
        put(COMMENTS, comments);
    }
}
