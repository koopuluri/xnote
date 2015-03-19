package com.xnote.wow.xnote.models;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.xnote.wow.xnote.DB;
import java.util.UUID;
/**
 * Created by koopuluri on 1/20/15.
 */
@ParseClassName(DB.ARTICLE)
public class ParseArticle extends ParseObject{
    public static final String TAG = "Article";
    public static final String TITLE = "title";
    public static final String TIMESTAMP = "Timestamp";
    public static final String CONTENT = "Content";
    public static final String ID = "Id";
    public static final String AUTHOR = "Author";
    public static final String TYPE = "Type";
    public static final String ARTICLE_URL = "ArticleUrl";
    public static final String IS_PARSED = "IsParsed";
    public static final String ICON_URL = "IconURL";
    public static final String IMAGE_ICON = "ImageIcon";
    public static final String COULD_NOT_BE_PARSED = "CouldNotBeParsed";
    public ParseArticle() {
        super();
    }
    // --------------------------------------------GETTERS------------------------------------------
    public String getTitle() {
        return getString(TITLE);
    }
    public long getTimestamp() {
        return getLong(TIMESTAMP);
    }
    public String getContent() {
        return getString(CONTENT);
    }
    public String getId() {
        return getString(ID);
    }
    public String getAuthor() { return getString(AUTHOR); }
    public String getType() { return getString(TYPE); }
    public String getArticleUrl() { return getString(ARTICLE_URL); }
    public boolean isParsed() { return getBoolean(IS_PARSED); }
    public boolean getCouldNotBeParsed() { return getBoolean(COULD_NOT_BE_PARSED); }
    public String getIconURL() { return getString(ICON_URL); }
    public ParseImage getSourceImage() { return (ParseImage) getParseObject(IMAGE_ICON);}
    // ---------------------------------------SETTERS-----------------------------------------------
    public void setId() {
        put(ID, UUID.randomUUID().toString());
    }
    public void setTitle(String t) {
        put(TITLE, t);
    }
    public void setContent(String c) {
        put(CONTENT, c);
    }
    public void setTimestamp(long seconds) {
        put(TIMESTAMP, seconds);
    }
    public void setAuthor(String author) { put(AUTHOR, author); }
    public void setType(String type) { put(TYPE, type); }
    public void setArticleUrl(String articleUrl) { put(ARTICLE_URL, articleUrl); }
    public void setIsparsed(boolean parsed) { put(IS_PARSED, parsed); }
    public void setCouldNotBeParsed(boolean parsed) { put(COULD_NOT_BE_PARSED, parsed); }
    public void setIconURL(String iconURL) { put(ICON_URL, iconURL); }
    public void setSourceImage(ParseImage image) { put(IMAGE_ICON, image); }
    @Override
    public String toString() {
        return getString(TITLE);
    }
}