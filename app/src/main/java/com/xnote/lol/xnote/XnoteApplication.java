package com.xnote.lol.xnote;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.xnote.lol.xnote.models.ParseArticle;
import com.xnote.lol.xnote.models.ParseConstant;
import com.xnote.lol.xnote.models.ParseFeedback;
import com.xnote.lol.xnote.models.ParseImage;
import com.xnote.lol.xnote.models.ParseNote;
import com.xnote.lol.xnote.models.ParseUserInfo;

/**
 * Created by koopuluri on 2/11/15.
 */
public class XnoteApplication extends Application {
    public static final String TAG = "XnoteApplication";
    public static final boolean LOG_IN = true;
    public static boolean FIRST_TIME = true;

    @Override
    public void onCreate() {
        super.onCreate();
        //Initializing parse when this is the first activity launched:
        ParseObject.registerSubclass(ParseArticle.class);
        ParseObject.registerSubclass(ParseNote.class);
        ParseObject.registerSubclass(ParseImage.class);
        ParseObject.registerSubclass(ParseFeedback.class);
        ParseObject.registerSubclass(ParseConstant.class);
        ParseObject.registerSubclass(ParseUserInfo.class);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "vkam91pQ14fnKfWjMqrPGqsweUzTGD1j5ZqS5hvA",
                "BckIbDZpvzDMBnM5P1wP3w2UzF8uwcP5JZEN5rX5");
        ParseACL defaultACL = new ParseACL();
        ParseACL.setDefaultACL(defaultACL, true);
        //Get diffbot token either from the cloud or locally if cloud is
        //not available
    }
}
