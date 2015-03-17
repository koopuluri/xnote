package com.xnote.wow.xnote;

import android.util.Log;
import android.view.MenuItem;

/**
 * Handles action click event. Same responses need to be used across
 * multiple activities, hence aggregating in this class.
 * Created by koopuluri on 2/11/15.
 */
public class ActionBarClickController {
    public static final String TAG = "ActionBarController";

    public static void handleAction(MenuItem item) {
        Log.d(TAG, "handleAction() with item: " + item.toString());
    }
}
