package com.xnote.wow.xnote.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.xnote.wow.xnote.Constants;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.XnoteApplication;

/**
 * Created by koopuluri on 4/5/15.
 */
public class TutorialActivity extends Activity {

    String[] TEXTS = {
            "When you come across an article on your browser that " +
                    "you'd like to save, select the share option and \"Add to xnote\".",
            "Highlight to take notes.",
            "Search through all notes and articles.",
            "Share an individual note.",
            "You can even share an entire article containing all of your notes." +
                    " The recipient will receive a url at xnote.io with your annotated article."};

    int[] IMAGES = {
            R.drawable.xnote_save_tutorial,
            R.drawable.xnote_highlight_tutorial,
            R.drawable.xnote_search_tutorial,
            R.drawable.xnote_note_share_tutorial,
            R.drawable.xnote_article_sharetutorial};

    TextView mText;
    ImageView mImage;
    ImageButton mNextButton;
    int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        position = 0;

        mText = (TextView) findViewById(R.id.tutorial_content);
        //mText.setText(TEXTS[position]);

        mImage = (ImageView) findViewById(R.id.tutorial_image);
        //mImage.setImageResource(IMAGES[position]);

        mNextButton = (ImageButton)  findViewById(R.id.tutorial_next_button);
        mNextButton.setColorFilter(Color.parseColor("#FFFFFFFF"));
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(20);
                next();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tutorial_actions, menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // respond to action-up button:

            case R.id.action_cancel:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void next() {
        if (position >= TEXTS.length) {
            // goes to MainActivity as this is launched from it.
            SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
            // Code to run once
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", false);
            editor.apply();
            finish();
            return;
        }

        // display the next tutorial item:
        mText.setText(TEXTS[position]);
        mImage.setImageResource(IMAGES[position]);
        position++;
    }
}
