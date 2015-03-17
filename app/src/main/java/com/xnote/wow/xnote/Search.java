package com.xnote.wow.xnote;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.models.ParseNote;
import com.xnote.wow.xnote.models.SearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Does all the search stuffs.
 * Created by koopuluri on 2/2/15.
 */
public class Search {
    public static final String TAG = "Search";

    public static List<SearchResult> searchArticleText(final String textToSearch) {
        // full text search through articles:
        ParseQuery<ParseObject> query = ParseQuery.getQuery(DB.ARTICLE);
        query.whereContains(ParseArticle.CONTENT, textToSearch);
        query.fromLocalDatastore();
        query.orderByDescending(ParseArticle.TIMESTAMP);
        final List<SearchResult> out = new ArrayList<>();
        try {
            List<ParseObject> results = query.find();
            for (ParseObject obj : results) {
                ParseArticle a = (ParseArticle) obj;
                SearchResult result = new SearchResult();
                result.title = a.toString();
                result.tstamp = a.getTimestamp();
                result.id = a.getId();
                result.type = DB.ARTICLE;
                result.numHits = 3;  //TODO: actually calculate the number of hits of 'textToSearch' in this article!
                out.add(result);
                Log.d(TAG, "searchArticleText(): result added to outList: " + result.title + ", " + result.id);
            }
        } catch (ParseException e) {
            Log.e(TAG, "searchArticleText(): unable to get results: " + e);
        }

        return out;
    }


    public static List<SearchResult> searchNoteText(final String textToSearch) {
        // full text search through articles:
        ParseQuery<ParseObject> query = ParseQuery.getQuery(DB.NOTE);
        query.whereContains(ParseNote.CONTENT, textToSearch);
        query.fromLocalDatastore();
        query.orderByDescending(ParseNote.TIMESTAMP);
        final List<SearchResult> out = new ArrayList<>();
        try {
            List<ParseObject> results = query.find();
            for (ParseObject obj : results) {
                ParseNote n = (ParseNote) obj;
                SearchResult result = new SearchResult();
                result.title = n.toString();
                result.tstamp = n.getTimestamp();
                result.articleId = n.getArticleId();
                result.id = n.getId();
                result.type = DB.NOTE;
                result.numHits = 3;  //TODO: actually calculate the number of hits of 'textToSearch' in this article!
                out.add(result);
                Log.d(TAG, "searchNoteText(): result added to outList: " + result.title + ", " + result.id);
            }
        } catch (ParseException e) {
            Log.e(TAG, "searchNoteText(): unable to get results: " + e);
        }
        return out;
    }
}
