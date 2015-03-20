package com.xnote.wow.xnote;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.models.ParseFeedback;
import com.xnote.wow.xnote.models.ParseImage;
import com.xnote.wow.xnote.models.ParseNote;

/**
 * Created by koopuluri on 2/11/15.
 */
public class XnoteApplication extends Application {
    public static final String TAG = "XnoteApplication";
    public static final boolean LOG_IN = true;

    @Override
    public void onCreate() {
        super.onCreate();
        // initializing parse when this is the first activity launched:
        ParseObject.registerSubclass(ParseArticle.class);
        ParseObject.registerSubclass(ParseNote.class);
        ParseObject.registerSubclass(ParseImage.class);
        ParseObject.registerSubclass(ParseFeedback.class);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "vkam91pQ14fnKfWjMqrPGqsweUzTGD1j5ZqS5hvA",
                "BckIbDZpvzDMBnM5P1wP3w2UzF8uwcP5JZEN5rX5");
        ParseACL defaultACL = new ParseACL();
        ParseACL.setDefaultACL(defaultACL, true);

    }
}
