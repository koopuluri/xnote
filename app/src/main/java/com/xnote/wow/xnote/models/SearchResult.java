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

    public SearchResult clone() {
        SearchResult r = new SearchResult();
        r.title = this.title;
        r.numHits = this.numHits;
        r.tstamp = this.tstamp;
        r.type = this.type;
        r.id = this.id;
        r.articleId = this.articleId;
        return r;
    }
}
