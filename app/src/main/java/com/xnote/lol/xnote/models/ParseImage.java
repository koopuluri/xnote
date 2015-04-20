package com.xnote.lol.xnote.models;


import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.xnote.lol.xnote.DB;

/**
 * Created by koopuluri on 2/17/15.
 */

@ParseClassName(DB.IMAGE)
public class ParseImage extends ParseObject {
    public static final String URL = "ParseImageUrl";
    public static final String DATA = "ParseImageData";
    public static final String DATA_FILE = "ParseImageDataFile";
    public static final String ARTICLE_ID = "ParseImageArticleId";
    public static final String NATURAL_WIDTH = "ParseImageNaturalWidth";
    public static final String NATURAL_HEIGHT = "ParseImageNaturalHeight";
    public static final String ERROR = "Error";
    public ParseImage() {
        super();
    }

    public String getUrl() { return getString(URL); }
    public ParseFile getDataFile() { return getParseFile(DATA_FILE); }
    public byte[] getByteData() { return getBytes(DATA); }
    public String getArticleId() { return getString(ARTICLE_ID); }
    public int getNaturalWidth() { return getInt(NATURAL_WIDTH); }
    public int getNaturalHeight() { return getInt(NATURAL_HEIGHT); }
    public boolean getError() { return getBoolean(ERROR); }

    public void setUrl(String url) { put(URL, url); }
    public void setDataFile(ParseFile file) { put(DATA_FILE, file); }
    public void setArticleId(String articleId) { put(ARTICLE_ID, articleId); }
    public void setNaturalWidth(int width) { put(NATURAL_WIDTH, width); }
    public void setNaturalHeight(int height) { put(NATURAL_HEIGHT, height); }
    public void setError(boolean bool) { put(ERROR, bool); }
}
