package com.xnote.wow.xnote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.util.Log;

import com.xnote.wow.xnote.activities.ArticleActivity;
import com.xnote.wow.xnote.activities.FeedbackActivity;
import com.xnote.wow.xnote.activities.LoginSignUpActivity;
import com.xnote.wow.xnote.activities.MainActivity;
import com.xnote.wow.xnote.activities.NoteActivity;
import com.xnote.wow.xnote.activities.TutorialActivity;
import com.xnote.wow.xnote.fragments.ArticleFragment;
import com.xnote.wow.xnote.fragments.SearchFragment;

/**
 * Launches all activites and whatnot. It's like the center point in the silk
 * route; all those passing by can stop and chat over chai and cinnamon.
 * Created by koopuluri on 2/10/15.
 */
public class Controller {
    public static final String TAG = "Controller";
    public static final String PREFS_NAME = "MyPrefsFile";


    public static void launchTutorialActivity(Activity activity) {
        Intent intent = new Intent(activity, TutorialActivity.class);
        intent.putExtra(Constants.PARENT_ACTIVITY, activity.toString());
        activity.startActivity(intent);
    }


    /**
     * Launches the ArticleActivity with given articleId.
     * @param activity: the activity that is launching ArticleActivity.
     * @param articleId
     */
    public static void launchArticleActivity(Activity activity, String articleId) {
        Intent intent = new Intent(activity, ArticleActivity.class);
        Log.d(TAG, "This is the article_id launching PoopActivity with: " + articleId);
        intent.putExtra(Constants.ARTICLE_ID, articleId);
        intent.putExtra(Constants.PARENT_ACTIVITY, activity.toString());
        activity.startActivity(intent);
    }


//    public static void launchArticleActivity(Activity activity, int noteStart, int noteEnd) {
//        Intent intent = new Intent(activity, ArticleActivity.class);
//        Log.d(TAG, "launching ArticleActivity with noteStart, noteEnd.");
//        intent.putExtra(Constants.PARENT_ACTIVITY, activity.toString());
//        intent.putExtra(Constants.START_INDEX, noteStart);
//        intent.putExtra(Constants.END_INDEX, noteEnd);
//        activity.startActivity(intent);
//    }
//

    public static void launchNoteActivity(Activity activity,
                                          String articleId,
                                          String noteId,
                                          String launchingFragmentName) {
        Intent intent = new Intent(activity, NoteActivity.class);
        intent.putExtra(Constants.NOTE_ID, noteId);
        intent.putExtra(Constants.PARENT_ACTIVITY, activity.toString());
        intent.putExtra(Constants.ARTICLE_ID, articleId);

        if (launchingFragmentName.equals(ArticleFragment.TAG)) {
            activity.startActivityForResult(intent, Constants.NOTE_REQUEST);
            Log.d(TAG, "NoteActivity launched from ArticleActivity for result");
        } else if (launchingFragmentName.equals(SearchFragment.TAG)) {
            activity.startActivity(intent);
            Log.d(TAG, "NoteActivity launched from SearchActivity not for result");
        } else {
            Log.e(TAG, String.format("Fragment '%s' shouldn't be launching NoteActivity!",
                    launchingFragmentName));
        }
    }



    public static void launchNoteActivity(Activity activity, String articleId, int start, int end) {
        Log.d(TAG, String.format("launchNote: with start: %s, end: %s, articleId: %s",
                start, end, articleId));
        Intent intent = new Intent(activity, NoteActivity.class);
        intent.putExtra(Constants.ARTICLE_ID, articleId);
        intent.putExtra(Constants.START_INDEX, start);
        intent.putExtra(Constants.END_INDEX, end);
        intent.putExtra(Constants.PARENT_ACTIVITY, activity.toString());
        activity.startActivityForResult(intent, Constants.NOTE_REQUEST);
        Log.d(TAG, "text selected and note activity launched with articleId: " + articleId);
    }

    /**
     * Launches NoteActivity with the new note information.
     * @param article: the underlying article (contains the word and highlight spans as well).
     * @param startIndex: the start index of text selected.
     * @param endIndex: the end index of text selected.
     */
    public static void onTextSelected(Activity activity, SpannableString article, String articleId,
                               int startIndex, int endIndex) {
        Log.d(TAG, String.format("article_length: %s, startIndex: %s, endIndex: %s",
                article.length(), startIndex, endIndex));
        Intent intent = new Intent(activity, NoteActivity.class);
        intent.putExtra(Constants.ARTICLE_ID, articleId);
        intent.putExtra(Constants.CLIPPED_TEXT, article.subSequence(startIndex, endIndex)
                .toString());
        intent.putExtra(Constants.START_INDEX, startIndex);
        intent.putExtra(Constants.END_INDEX, endIndex);
        intent.putExtra(Constants.PARENT_ACTIVITY, activity.toString());
        activity.startActivityForResult(intent, Constants.NOTE_REQUEST);
        Log.d(TAG, "NoteFragment launched!!!");
    }

    public static void launchMainActivity(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, "starting MainActivity from: " + activity.toString());
        activity.startActivity(intent);
    }


    public static void launchLoginSignUpActivity(Activity activity){
        Intent intent = new Intent(activity, LoginSignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, "starting LoginSignUpActivity from: " + activity.toString());
        activity.startActivity(intent);
    }

    public static void launchSignUpFromAnonymousUser(Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        //Since user has indicated he wants to signup the preference is turned on
        settings.edit().putBoolean("chosen_to_signup", true).apply();
        launchLoginSignUpActivity(activity);
    }

    public static void launchMainActivityWithoutClearingBackStack(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        Log.d(TAG, "starting MainActivity without clearing back stack from: " + activity.toString());
        activity.startActivity(intent);
    }

    public static void launchFeedbackActivity(Activity activity) {
        Intent intent = new Intent(activity, FeedbackActivity.class);
        Log.d(TAG, "starting FeedbackActivity from" + activity.toString());
        activity.startActivity(intent);
    }
}
