package com.xnote.wow.xnote.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.xnote.wow.xnote.DB;

/**
 * Created by vignesh on 3/21/15.
 * Used to send in tokens to the app
 */

@ParseClassName(DB.CONSTANT)
public class ParseConstant extends ParseObject {

    public static final String DIFFBOT_TOKEN = "DiffbotToken";

    public String getDiffbotToken() {
        return getString(DIFFBOT_TOKEN);
    }

    public void setDiffbotToken(String token) {
        put(DIFFBOT_TOKEN, token);
    }
}
