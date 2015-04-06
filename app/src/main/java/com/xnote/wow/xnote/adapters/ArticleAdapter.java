package com.xnote.wow.xnote.adapters;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.models.ParseImage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
/**
 * Created by koopuluri on 2/5/15.
 */
public class ArticleAdapter  extends BaseListAdapter {
    public static final String TAG = "ArrayAdapter";
    public static final String IMAGE_VIEW = "ImageView";
    TextView mArticleTitleTv;
    ImageView mIcon;
    public ArticleAdapter(Context context, List<Object> articles, Fragment parent) {
        super(context, articles, parent, R.layout.article_list_layout);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ParseArticle article = (ParseArticle) getItem(position);
        mArticleTitleTv = (TextView) view.findViewById(R.id.article_title_text_view);
        mIcon = (ImageView) view.findViewById(R.id.icon_image_view);
        if(article.getCouldNotBeParsed()) {
            view.findViewById(R.id.could_not_be_parsed).setVisibility(View.VISIBLE);
            view.findViewById(R.id.icon_image_view).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.article_list_loading_spinner).setVisibility(View.INVISIBLE);
            Log.d(TAG, "error text being displayed");
        } else if (!article.isParsed()) {
            view.findViewById(R.id.article_list_loading_spinner).setVisibility(View.VISIBLE);
            TextView errorText = (TextView) view.findViewById(R.id.could_not_be_parsed);
            errorText.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.icon_image_view).setVisibility(View.INVISIBLE);
            Log.d(TAG, "article hasn't completed parsing yet, spinner set for article list item");
        } else {
            TextView errorText = (TextView) view.findViewById(R.id.could_not_be_parsed);
            errorText.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.icon_image_view).setVisibility(View.VISIBLE);
            view.findViewById(R.id.article_list_loading_spinner).setVisibility(View.GONE);
            ParseImage image = article.getSourceImage();
            //If there is no image then we need to download the image
            if(image == null) {
                String iconURL = article.getIconURL();
                if(iconURL != null) {
                    if ((iconURL.equals(""))) {
                        //mIcon.setImageResource(R.drawable.icon_for_adapter);
                        // set no icon!!
                    } else {
                        new DownloadImageTask(article, mIcon).execute(iconURL);
                    }
                }
            } else {
                try {
                    setImageViewWithIcon(mIcon, article.getSourceImage());
                } catch(IllegalStateException e) {
                    new FetchArticleImageTask(article, mIcon).execute();
                }
            }
        }
        mArticleTitleTv.setText(article.getTitle());
        return view;
    }


    private void setImageViewWithIcon(ImageView icon, ParseImage sourceImage) {
        byte[] byteArray = sourceImage.getData();
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        mIcon.setImageBitmap(bmp);
        notifyDataSetChanged();
    }


    private class FetchArticleImageTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = "FetchArticleImageTask";
        ParseArticle article;
        ParseImage sourceImage;
        ImageView icon;

        public FetchArticleImageTask(ParseArticle article, ImageView iconContainer) {
            this.article = article;
            icon = iconContainer;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                sourceImage = (ParseImage) article.getSourceImage().fetchIfNeeded();
            } catch (com.parse.ParseException e) {
                Log.e(TAG, "could not fetchIfNeeded() the sourceImage: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            super.onPostExecute(_);
            // now notifying datasetChanged and setting this sourceImage;
            if (sourceImage != null) {
                setImageViewWithIcon(icon, sourceImage);
            } else {
                Log.d(TAG, "fetching sourceImage from local datastore was not successful.");
            }
        }
    }


    //The Download Image Task is from the below link
    //http://stackoverflow.com/a/9288544/4671651
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        public final String TAG = "DownloadImageTaskArticleAdapter";
        ImageView bmImage;
        ParseArticle article;
        public DownloadImageTask(ParseArticle article, ImageView bmImage) {
            this.bmImage = bmImage;
            this.article = article;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            //Getting image from imageView so we can store it in parse image
            //The parse image is in turn stored in the article so it can be
            //accessed easily
            //http://stackoverflow.com/questions/9042932/getting-image-from-imageview
            //The check is here in case the image could not be downloaded and was set to null
            if(mIcon11 != null) {
                ParseImage image1 = new ParseImage();
                DB.saveImage(image1);
                Bitmap bitmap = mIcon11;
                Log.d(TAG, String.valueOf(bitmap));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Log.d(TAG, String.valueOf(stream));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] imageInByte = stream.toByteArray();
                Log.d(TAG, "IS THIS TOO BIG? : " + imageInByte.length);
                image1.setData(imageInByte);
                article.setSourceImage(image1);
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap mIcon11) {
            bmImage.setImageBitmap(mIcon11);
        }
    }
}