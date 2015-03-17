package com.xnote.wow.xnote.models;

/**
 * Created by koopuluri on 2/3/15.
 */
public class SearchResult {
    public String title;
    public int numHits;
    public long tstamp;
    public String type;
    public String id;
    public String articleId; // only used if (type == "Note")
}
