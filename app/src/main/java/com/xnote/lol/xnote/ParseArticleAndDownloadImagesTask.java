package com.xnote.lol.xnote;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.xnote.lol.xnote.models.DiffbotImageInfo;
import com.xnote.lol.xnote.models.ParseArticle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Uuuggghh... So messy.
 * Created by koopuluri on 2/28/15.
 */
public class ParseArticleAndDownloadImagesTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "ParseArticleAndDownloadImagesTask";
    public static int NUM_TASKS = 0;

    private final String USER_AGENT = "Mozilla/5.0";

    String mArticleUrl;
    ParseArticle mArticle;
    OnParseArticleTaskCompleted mListener;
    Activity parentActivity;

    public interface OnParseArticleTaskCompleted {
        public void onParseArticleCompleted(ParseArticle article);
    }

    public ParseArticleAndDownloadImagesTask(ParseArticle article, OnParseArticleTaskCompleted listener,
                                             Activity activity) {
        mArticle = article;
        mArticleUrl = mArticle.getArticleUrl();
        mListener = listener;
        parentActivity = activity;
    }

    @Override
    public void onPreExecute() {
        NUM_TASKS++;
    }


    @Override
    /**
     * Gets information from articleUrl using the Diffbot API; saves the article.
     * For each image in the article, launches FetchImageTask, which downloads and saves the image.
     */
    public Void doInBackground(Void... params) {
        List<DiffbotImageInfo> images = parseArticle(mArticleUrl);  // article's content has been set after this call!

        if (images == null) {
            Log.e(TAG, "could not parse article! parseArticle(...) returned null ==> images=null");
            return null;
        }

        // TODO: elegantly handle exceptions here!
        //saving article:
        Log.d(TAG, "saveArticle() called in ParseArticleTask()");
        Log.d(TAG, "length of images: " + images.size());
        // downloading and saving images in db.
        for (DiffbotImageInfo img : images) {
            if (img == null) {
                Log.e(TAG, "img is null in images!");
                continue;
            }

            if (DB.getImage(img.getUrl()) == null)
                downloadAndSaveImage(img.getUrl(), mArticle.getId(), img.getHeightAndWidth());
            else Log.d(TAG, "image already exists in db: " + img.getUrl());
        }

        if (mArticle.isParsed()) {
            Log.d(TAG, "article has been parsed somewhere else (?).");
            return null;
        }

        mArticle.setIsparsed(true);

        DB.saveArticle(mArticle);
        Log.d(TAG, "Util.saveArticleDisplayDialog() called on mArticle after parsing it.");

        try {
            mArticle.save();
            Log.d(TAG, "saved article in ParseArticleTask with id: " + mArticle.getId());
        } catch (ParseException e) {
            Log.d(TAG, "could not save article with id: " + mArticle.getId());
        }

        try {
            ParseQuery query = ParseQuery.getQuery(DB.ARTICLE);
            ParseObject obj = query.get(mArticle.getObjectId());
            Log.d(TAG, "retreived object from cloud after saving... poop. " + String.valueOf(obj));
        } catch (ParseException e) {
            Log.d(TAG , "could not get the article in background right after save... poop.");
        }

        return null;
    }

    @Override
    public void onPostExecute(Void _) {
        super.onPostExecute(_);
        mListener.onParseArticleCompleted(mArticle);
    }


    private void downloadAndSaveImage(String imageUrlString, String articleId, int[] heightAndWidth) {
        byte[] data = DiffbotParser.downloadImage(imageUrlString);
        DiffbotParser.saveImageFromData(imageUrlString, data, articleId, heightAndWidth);
        Log.d(TAG, String.format("image downloaded and saved! with imageUrlString: %s," +
                        " and articleId: %s",
                imageUrlString,
                articleId));
    }


    /**
     *
     * @param articleURL: url for article to be parsed.
     * @return ParseArticle object representing article.
     */
    public List<DiffbotImageInfo> parseArticle(String articleURL) {
        String url = "http://api.diffbot.com/v3/article";
        // String token = "0050ff99a5e2353d861f3991fe77b9ec";  // any way to not put this out in the open?
        String token = "a4bdd5e4ad6673088a9b88145a1d409e";  // new token; expires around March 12.
        String query;
        String query2;
        try {
            query2 = URLEncoder.encode(articleURL, "UTF-8");
            query = URLEncoder.encode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "url could not be encoded with exception: " + e);
            return null;
        }

        //http://stackoverflow.com/q/18150818a
        //Creating link for HTTP Get
        url += "?token=" + query + "&url=" + query2;
        // BASE_URL + '?token=' + url_encode(TOKEN) + '&url=' + url_encode(ARTICLE_URL)
        URL obj;
        try {
            obj = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "url cannot be created from the text shared with xnote: " + e);
            return null;
        }

        HttpURLConnection con;
        try {
            con = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            Log.d(TAG, "IO exception when opening a connection with url: " + e);
            // TODO: check if there is no connection:
            // if not, then store article url to be parsed later, and let the
            // user know that the article cannot be parsed as there's no internet
            // connection.
            return null;
        }

        try {
            // optional default is GET
            con.setRequestMethod("GET");
        } catch(java.net.ProtocolException e) {
            Log.e(TAG, "Setting request method to GET for HTTP connection: " + e);
            return null;
        }

        StringBuffer response;
        try {
            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();
            Log.d(TAG, "Sending 'GET' request to URL : " + url);
            Log.d(TAG, "Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException when attmepting to read the diffbot response: " + e);
            return null;
        }

        //Documentation on JSON website to get object structure
        //http://www.diffbot.com/products/automatic/
        return jsonToArticle(response.toString());
    }


    /**
     * @param diffbotResponse: the string response returned by diffbot API
     * @return ParseArticle with content as the articleText in html.
     */
    private  List<DiffbotImageInfo> jsonToArticle(String diffbotResponse) {
        JSONObject tags;
        try {
            JSONObject json = new JSONObject(diffbotResponse);
            JSONArray objects = json.getJSONArray("objects");
            tags = objects.getJSONObject(0);
        } catch (JSONException e) {
            Log.e(TAG, "json couldnt be parsed, no 'objects' key: " + e);
            return null;
        }

        // getting images' info from diffbot's json:
        JSONArray imageInfos;
        List<DiffbotImageInfo> images;
        try {
            imageInfos = tags.getJSONArray("images");
            images = new ArrayList<>();
            for (int i = 0; i < imageInfos.length(); i++) {
                String imgUrl = imageInfos.getJSONObject(i).getString("url");
                int[] imgHeightAndWidth = {imageInfos.getJSONObject(i).getInt("naturalHeight"),
                        imageInfos.getJSONObject(i).getInt("naturalWidth")
                };
                // adding to the images list that's going to be returned.
                images.add(new DiffbotImageInfo(imgUrl, imgHeightAndWidth));
            }
        } catch (JSONException e) {
            Log.d(TAG, "Cannot get images from article!: " + e);
            images = new ArrayList<>();
        }

        // extracting the content from diffbot's json:
        try {
            String content = tags.getString("html");
            Log.d(TAG, "tags.getString('html'): " + content);
            String title = tags.getString("title");

            // setting content for the parse article
            mArticle.setContent(tags.getString("html"));
            mArticle.setTitle(tags.getString("title"));

            // returning information about the images in this article.
            return images;

        } catch (JSONException e) {
            Log.e(TAG, "jsonToArticle: couldn't parse json: " + e);
            return null;  // nothing could be returned.
        }
    }
}
