package com.xnote.lol.xnote;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.xnote.lol.xnote.models.DiffbotImageInfo;
import com.xnote.lol.xnote.models.ParseArticle;
import com.xnote.lol.xnote.models.ParseImage;

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


    private void downloadAndSaveImage(String imageUrlString, ParseArticle article, int[] heightAndWidth) {
        byte[] data = DiffbotParser.downloadImage(imageUrlString);
        if (heightAndWidth != null) {
            DiffbotParser.saveImageFromData(imageUrlString, data, article.getId(), heightAndWidth);
        } else {
            DiffbotParser.saveIconFromData(imageUrlString, data, article);
        }
    }


    /**
     *
     * @return updated ParseArticle after parsing through Diffbot API.
     */
    public ParseArticle parse() {
        List<DiffbotImageInfo> images = parseArticle(mArticle.getArticleUrl());
        // article's content has been set after this call!
        if (images == null) {
            mArticle.setCouldNotBeParsed(true);
            return mArticle;
        }
        // downloading and saving images in db.
        for (DiffbotImageInfo img : images) {
            if (img == null) {
                continue;
            }
            if (DB.getImage(img.getUrl()) == null) {
                int height = img.getHeightAndWidth()[0];
                int width = img.getHeightAndWidth()[1];
                if((height <= 2048) && (width <= 2048)) {  // todo: what the fuck is this!?
                    downloadAndSaveImage(img.getUrl(), mArticle, img.getHeightAndWidth());
                } else {
                    // do nothing? So don't download the image?
                }
            }
        }

        // now handling the icon url:
        if (!(mArticle.getIconURL() == "" || mArticle.getIconURL() == null)) {
            ParseImage existingImage = DB.getImage(mArticle.getIconURL());
            if (existingImage == null) {
                // then this icon needs to be saved!
                downloadAndSaveImage(mArticle.getIconURL(), mArticle, null);
            } else {
                // icon already exists... needs to be associated with this new article:
                mArticle.setSourceImage(existingImage);
            }
        } else {
            // do nothing.
        }

        if (mArticle.isParsed()) {
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
        String token = DB.getConstantCloud();
        String query;
        String query2;
        try {
            query2 = URLEncoder.encode(articleURL, "UTF-8");
            query = URLEncoder.encode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        //http://stackoverflow.com/q/18150818a
        //Creating link for HTTP Get
        url += "?token=" + query + "&url=" + query2;
        URL obj;
        try {
            obj = new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            // if not, then store article url to be parsed later, and let the
            // user know that the article cannot be parsed as there's no internet
            // connection.
            return null;
        }
        try {
            // optional default is GET
            con.setRequestMethod("GET");
        } catch(java.net.ProtocolException e) {
            return null;
        }
        StringBuffer response;
        try {
            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();  // TODO: use this for something!
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
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
                if (imgUrl.contains(".gif"))
                    continue;  // todo: (LOW ASS PRIORITY): support gifs.
                int[] imgHeightAndWidth = {imageInfos.getJSONObject(i).getInt("naturalHeight"),
                        imageInfos.getJSONObject(i).getInt("naturalWidth")
                };
                // adding to the images list that's going to be returned.
                images.add(new DiffbotImageInfo(imgUrl, imgHeightAndWidth));
            }
        } catch (JSONException e) {
            images = new ArrayList<>();
        }
        // extracting the content from diffbot's json:
        try {
            String content = tags.getString("html");
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
            // couldn't fetch... returning null.
        }
        return null;
    }

    private static void saveIconFromData(String iconUrl, byte[] imgData, ParseArticle article) {
        final ParseImage img = new ParseImage();
        img.setArticleId(article.getId());
        img.setUrl(iconUrl);
        if (imgData != null) {
            ParseFile dataFile = new ParseFile(imgData);
            img.setDataFile(dataFile);
            article.setSourceImage(img);
            try {
                dataFile.save();
                article.pin();
            } catch (ParseException e) {
                return;
            }
            DB.saveImage(img);

//            // now saving the dataFile:
//            dataFile.saveInBackground(new SaveCallback() {
//                @Override
//                public void done(ParseException e) {
//                    // save the image:
//                    Log.d(TAG, "POOOOOOOOOOOOOP");
//                    DB.saveImage(img);
//                }
//            });
        } else {
            img.setError(true); // now article.getSourceImage() will return null!
            DB.saveImage(img);
        }
    }

    /**
     * Given image data, creates and pins (and saves if possible) a ParseImage that represents image.
     * @param imgData
     */
    public static void saveImageFromData(String imgUrlString, byte[] imgData, String articleId,
                                         int[] heightAndWidth) {
        if(imgData != null) {
            final ParseImage image = new ParseImage();
            image.setArticleId(articleId);
            ParseFile dataFile = new ParseFile(imgData);
            image.setDataFile(dataFile);  // parseFile associated with the ParseImage.
            image.setUrl(imgUrlString);
            if (heightAndWidth != null) {
                image.setNaturalHeight(heightAndWidth[0]);
                image.setNaturalWidth(heightAndWidth[1]);
            }
            image.setError(false);
            // saving image:
            try{
                dataFile.save();
            } catch (ParseException e) {
                return;
            }

            DB.saveImage(image);
//            dataFile.saveInBackground(new SaveCallback() {
//                @Override
//                public void done(ParseException e) {
//                    // pin the image:
//                    Log.d(TAG, "WAAAAAAAAZAAAAAAAAA");
//                    DB.saveImage(image);
//                }
//            });
        } else {
            ParseImage image = new ParseImage();
            image.setArticleId(articleId);
            image.setUrl(imgUrlString);
            image.setError(true);
            DB.saveImage(image);
        }
    }
}