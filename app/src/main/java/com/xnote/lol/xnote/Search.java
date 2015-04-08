package com.xnote.lol.xnote;



import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.xnote.lol.xnote.models.ParseArticle;
import com.xnote.lol.xnote.models.ParseNote;
import com.xnote.lol.xnote.models.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Does all the search stuffs.
 * Created by koopuluri on 2/2/15.
 */
public class Search {
    public static final String TAG = "Search";

    /**
     * Use compound queries: https://www.parse.com/docs/android_guide#queries-compound
     * @param textToSearch
     * @return
     */
    public static List<SearchResult> searchArticleText(final String textToSearch) {
        // full text search through articles:
        ParseQuery<ParseObject> inTitle = ParseQuery.getQuery(DB.ARTICLE);
        inTitle.whereContains(ParseArticle.TITLE, textToSearch);
        inTitle.fromLocalDatastore();

        // TODO: this doesn't work because it exceeds Parse's regex find limit (LOL. get around this).
//        ParseQuery<ParseObject> inContent = ParseQuery.getQuery(DB.ARTICLE);
//        inContent.whereContains(ParseArticle.CONTENT, textToSearch);
//        inContent.fromLocalDatastore();

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(inTitle);
//        queries.add(inContent);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        //Returns a list of articles in order of timestamp
        List<SearchResult> articles = new ArrayList<>();
        Set<String> idSet = new HashSet<String>();
        try {
            List<ParseObject> results = mainQuery.find();
            for (ParseObject obj : results) {
                ParseArticle a = (ParseArticle) obj;
                if (!a.isParsed())
                    continue;
                SearchResult result = new SearchResult();
                result.title = a.toString();
                result.tstamp = a.getTimestamp();
                result.id = a.getId();
                result.type = DB.ARTICLE;
                result.numHits = 3;  //TODO: actually calculate the number of hits of 'textToSearch' in this article!
                if (!idSet.contains(a.getId())) {
                    articles.add(result);
                    idSet.add(a.getId());
                }
            }
        } catch (ParseException e) {
        }
        return articles;
    }


    public static Map<String, List<SearchResult>> searchNoteText(final String textToSearch) {
        // full text search through articles:
        ParseQuery<ParseObject> query = ParseQuery.getQuery(DB.NOTE);
        query.whereContains(ParseNote.CONTENT, textToSearch);
        query.fromLocalDatastore();
        query.orderByDescending(ParseNote.TIMESTAMP);
        //Stores the list of notes that have a hit for a given article
        final Map<String, List<SearchResult>> out = new HashMap();
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
                List<SearchResult> notesForGivenArticle;
                if(out.containsKey(n.getArticleId())) {
                    notesForGivenArticle = (List<SearchResult>) out.get(n.getArticleId());
                } else {
                    notesForGivenArticle = new ArrayList<>();
                }
                notesForGivenArticle.add(result);
                out.put(n.getArticleId(), notesForGivenArticle);
            }
        } catch (ParseException e) {
        }
        return out;
    }

    public static List<SearchResult> search(final String textToSearch) {
        Map<String, List<SearchResult>> notesOut = searchNoteText(textToSearch);
        List<SearchResult> articlesOut = searchArticleText(textToSearch);
        List<SearchResult> results = new ArrayList<>();

        //Add the article and the notes that have a hit for that article
        for(SearchResult article : articlesOut) {
            results.add(article);
            if(notesOut.containsKey(article.id)) {
                results.addAll(notesOut.get(article.id));
                notesOut.remove(article.id);
            }
        }

        //If any notes are remaining the article is got from the database.
        for (Map.Entry<String, List<SearchResult>> entry : notesOut.entrySet()) {
            String articleId = entry.getKey();
            List<SearchResult> notesForGivenArticle = entry.getValue();
            ParseArticle a = DB.getLocalArticle(articleId);
            if(a != null) {
                SearchResult result = new SearchResult();
                result.title = a.toString();
                result.tstamp = a.getTimestamp();
                result.id = a.getId();
                result.type = DB.ARTICLE;
                result.numHits = 3;
                results.add(result);
                results.addAll(notesForGivenArticle);
            }
        }
        return results;
    }
}
