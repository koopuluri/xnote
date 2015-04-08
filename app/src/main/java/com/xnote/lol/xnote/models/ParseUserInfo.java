package com.xnote.lol.xnote.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.xnote.lol.xnote.DB;

/**
 * Created by koopuluri on 3/22/15.
 */
@ParseClassName(DB.PARSE_USER_INFO)
public class ParseUserInfo extends ParseObject {
    public static final String NEW = "NEW";

    public void setIsNew(boolean isNew) {
        put(NEW, isNew);
    }

    public boolean getIsNew() {
        return getBoolean(NEW);
    }
}
