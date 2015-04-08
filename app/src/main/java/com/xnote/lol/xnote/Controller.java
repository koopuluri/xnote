package com.xnote.lol.xnote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.SpannableString;

import com.xnote.lol.xnote.activities.ArticleActivity;
import com.xnote.lol.xnote.activities.FeedbackActivity;
import com.xnote.lol.xnote.activities.LoginSignUpActivity;
import com.xnote.lol.xnote.activities.MainActivity;
import com.xnote.lol.xnote.activities.NoteActivity;
import com.xnote.lol.xnote.activities.TutorialActivity;
import com.xnote.lol.xnote.fragments.ArticleFragment;
import com.xnote.lol.xnote.fragments.SearchFragment;

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
        intent.putExtra(Constants.ARTICLE_ID, articleId);
        intent.putExtra(Constants.PARENT_ACTIVITY, activity.toString());
        activity.startActivity(intent);
    }

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
        } else if (launchingFragmentName.equals(SearchFragment.TAG)) {
            activity.startActivity(intent);
        } else {
        }
    }



    public static void launchNoteActivity(Activity activity, String articleId, int start, int end) {
        Intent intent = new Intent(activity, NoteActivity.class);
        intent.putExtra(Constants.ARTICLE_ID, articleId);
        intent.putExtra(Constants.START_INDEX, start);
        intent.putExtra(Constants.END_INDEX, end);
        intent.putExtra(Constants.PARENT_ACTIVITY, activity.toString());
        activity.startActivityForResult(intent, Constants.NOTE_REQUEST);
    }

    /**
     * Launches NoteActivity with the new note information.
     * @param article: the underlying article (contains the word and highlight spans as well).
     * @param startIndex: the start index of text selected.
     * @param endIndex: the end index of text selected.
     */
    public static void onTextSelected(Activity activity, SpannableString article, String articleId,
                               int startIndex, int endIndex) {
        Intent intent = new Intent(activity, NoteActivity.class);
        intent.putExtra(Constants.ARTICLE_ID, articleId);
        intent.putExtra(Constants.CLIPPED_TEXT, article.subSequence(startIndex, endIndex)
                .toString());
        intent.putExtra(Constants.START_INDEX, startIndex);
        intent.putExtra(Constants.END_INDEX, endIndex);
        intent.putExtra(Constants.PARENT_ACTIVITY, activity.toString());
        activity.startActivityForResult(intent, Constants.NOTE_REQUEST);
    }

    public static void launchMainActivity(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }


    public static void launchLoginSignUpActivity(Activity activity){
        Intent intent = new Intent(activity, LoginSignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void launchSignUpFromAnonymousUser(Activity activity) {
        activity.finish();
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        //Since user has indicated he wants to signup the preference is turned on
        settings.edit().putBoolean("chosen_to_signup", true).apply();
        launchLoginSignUpActivity(activity);
    }

    public static void launchMainActivityWithoutClearingBackStack(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    public static void launchFeedbackActivity(Activity activity) {
        Intent intent = new Intent(activity, FeedbackActivity.class);
        activity.startActivity(intent);
    }
}
