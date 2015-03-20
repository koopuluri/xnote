package com.xnote.wow.xnote;
import android.util.Log;

import com.xnote.wow.xnote.models.DiffbotImageInfo;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.models.ParseImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by vignesh on 2/9/15.
 */
public class DiffbotParser {
    public static final String TAG = "DiffbotParser";
    private static final String USER_AGENT = "Mozilla/5.0";
    String mArticleId;
    ParseArticle mArticle;
    public DiffbotParser(String articleId) {
        mArticleId = articleId;
        mArticle = DB.getLocalArticle(mArticleId);
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
     * @return updated ParseArticle after parsing through Diffbot API.
     */
    public ParseArticle parse() {
        List<DiffbotImageInfo> images = parseArticle(mArticle.getArticleUrl());  // article's content has been set after this call!
        if (images == null) {
            Log.e(TAG, "could not parse article! parseArticle(...) returned null ==> images=null");
            mArticle.setCouldNotBeParsed(true);
            return mArticle;
        }
        Log.d(TAG, "length of images: " + images.size());
        // downloading and saving images in db.
        for (DiffbotImageInfo img : images) {
            if (img == null) {
                Log.e(TAG, "img is null in images!");
                continue;
            }
            if (DB.getImage(img.getUrl()) == null) {
                int height = img.getHeightAndWidth()[0];
                int width = img.getHeightAndWidth()[1];
                if((height <= 2048) && (width <= 2048)) {
                    downloadAndSaveImage(img.getUrl(), mArticle.getId(), img.getHeightAndWidth());
                    Log.d(TAG, "Image saved successfully I hope");
                } else {
                    Log.d(TAG, "Image is too large");
                }
            }
            else Log.d(TAG, "image already exists in db: " + img.getUrl());
        }
        if (mArticle.isParsed()) {
            Log.d(TAG, "article has been parsed somewhere else (?).");
            return null;
        }
        mArticle.setIsparsed(true);
        mArticle.setCouldNotBeParsed(false);
        return mArticle;
    }
    /**
     *
     * @param articleURL: url for article to be parsed.
     * @return ParseArticle object representing article.
     */
    private List<DiffbotImageInfo> parseArticle(String articleURL) {
        String url = "http://api.diffbot.com/v3/article";
        String token = "9fa0294020c516932daf9ad7d45cd36b"; //TODO: Put this somewhere and get it
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
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException when attempting to read the diffbot response: " + e);
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
    private List<DiffbotImageInfo> jsonToArticle(String diffbotResponse) {
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
            String iconURL;
            try {
                iconURL = tags.getString("icon");
            } catch(JSONException e) {
                iconURL = "";
            }
            mArticle.setIconURL(iconURL);
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
    // ----------------------------IMAGE PARSING STATIC FUNCS:---------------------------------------------
    /**
     * http://stackoverflow.com/a/1478544/2713471 --> downloading images from url.
     * @param imageUrlString
     * @return
     */
    public static byte[] downloadImage(String imageUrlString) {
        URL imgUrl;
        try {
            imgUrl = new URL(imageUrlString);
        } catch (IOException e) {
            Log.e(TAG, "IOException with imageUrlString: " + imageUrlString + "\nexception: " + e);
            return null;
        }
        // fetching image data from imgUrl:
        try {
            // Read the image ...
            InputStream inputStream = imgUrl.openStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            inputStream.close();
            // Here's the content of the image...
            return output.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "io exception when downoading image from URL: " + e);
        }
        // couldn't fetch.
        return null;
    }
    /**
     * Given image data, creates and pins (and saves if possible) a ParseImage that represents image.
     * @param imgData
     */
    public static void saveImageFromData(String imgUrlString, byte[] imgData, String articleId,
                                         int[] heightAndWidth) {
        ParseImage image = new ParseImage();
        image.setArticleId(articleId);
        image.setData(imgData);
        image.setUrl(imgUrlString);
        image.setNaturalHeight(heightAndWidth[0]);
        image.setNaturalWidth(heightAndWidth[1]);
        // saving image:
        DB.saveImage(image);
        Log.d(TAG, "saveImageFromData() ParseImage created and saved for imgUrlString: " +
                imgUrlString);
    }
}