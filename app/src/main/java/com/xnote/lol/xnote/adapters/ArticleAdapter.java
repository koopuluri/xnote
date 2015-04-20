package com.xnote.lol.xnote.adapters;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.models.ParseArticle;
import com.xnote.lol.xnote.models.ParseImage;

import java.util.List;
/**
 * Created by koopuluri on 2/5/15.
 */
public class ArticleAdapter  extends BaseListAdapter {
    // todo: Implement 'ViewHolder' to make scrolling quicker.
    public static final String TAG = "ArrayAdapter";

    public ArticleAdapter(Context context, List<Object> articles, Fragment parent) {
        super(context, articles, parent, R.layout.article_list_layout);
    }


    public static class ViewHolder {
        TextView articleTitleView;
        ImageView icon;
        TextView couldNotBeParsed;
        ProgressBar spinner;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(getContext(), mLayout, null);
            viewHolder = new ViewHolder();
            viewHolder.articleTitleView = (TextView)
                    convertView.findViewById(R.id.article_title_text_view);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon_image_view);
            viewHolder.couldNotBeParsed = (TextView) convertView.findViewById(R.id.could_not_be_parsed);
            viewHolder.spinner = (ProgressBar) convertView.findViewById(R.id.article_list_loading_spinner);
            convertView.setTag(viewHolder);
        }

        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (getSelectedPositions().contains(position)) {
            convertView.setBackgroundColor(parentFrag.getActivity().getResources()
                    .getColor(R.color.xnote_color_light));
        } else {
            convertView.setBackgroundColor(parentFrag.getActivity().getResources()
                    .getColor(R.color.white));
        }

        ParseArticle article = (ParseArticle) getItem(position);
        viewHolder.icon.setImageResource(0);

        if(article.getCouldNotBeParsed()) {
            viewHolder.couldNotBeParsed.setVisibility(View.VISIBLE);
            viewHolder.icon.setVisibility(View.INVISIBLE);
            viewHolder.spinner.setVisibility(View.INVISIBLE);
        } else if (!article.isParsed()) {
            viewHolder.spinner.setVisibility(View.VISIBLE);
            viewHolder.couldNotBeParsed.setVisibility(View.INVISIBLE);
            viewHolder.icon.setVisibility(View.INVISIBLE);
        } else {

            viewHolder.couldNotBeParsed.setVisibility(View.INVISIBLE);
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.spinner.setVisibility(View.GONE);
            if (article.getIconURL() == "" || article.getIconURL() == null) {
                viewHolder.icon.setImageResource(0);
            } else if (article.getSourceImage() == null) {
                // set no icon:
                viewHolder.icon.setImageResource(0);
            } else {
                // fetch the icon image:
                viewHolder.icon.setImageResource(0);
                article.getSourceImage().fetchIfNeededInBackground(
                    new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e != null || parseObject == null) {
                                viewHolder.icon.setImageResource(0);
                            } else {
                                // time to set the icon:
                                setImageViewWithIcon(viewHolder.icon, (ParseImage) parseObject);
                            }
                            viewHolder.icon.invalidate();
                        }
                    }
                );
            }
            viewHolder.icon.invalidate(); // to redraw it.
        }
        viewHolder.articleTitleView.setText(article.getTitle());
        return convertView;
    }


    private void setImageViewWithIcon(ImageView icon, ParseImage sourceImage) {
        byte[] byteArray;
        ParseFile imgFile = sourceImage.getDataFile();
        if (imgFile == null) {
            // trying to get the byteData directly (for articles saved pre-update
            // (ParseImage schema change).
            byteArray = sourceImage.getByteData();
            if (byteArray == null) {
                icon.setImageResource(0);
                return;
            }
        } else {
            try {
                byteArray = sourceImage.getDataFile().getData();
            } catch (ParseException e) {
                icon.setImageResource(0);
                return;
            }
        }
        // creating bitmap and setting to the icon view.
        if (byteArray != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            icon.setImageBitmap(bmp);
        } else {
            icon.setImageResource(0);
        }
    }
}