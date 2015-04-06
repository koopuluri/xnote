package com.xnote.wow.xnote.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.xnote.wow.xnote.R;

/**
 * Created by koopuluri on 4/5/15.
 */
public class TutorialActivity extends Activity {

    String[] TEXTS = {
            "Save articles to xnote.",
            "Highlight to take notes.",
            "Search through all notes and articles."};

    int[] IMAGES = {
            R.drawable.save_tutorial,
            R.drawable.highlight_tutorial,
            R.drawable.search_tutorial};

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

    private void next() {
        if (position >= TEXTS.length-1) {
            // goes to MainActivity as this is launched from it.
            finish();
            return;
        }
        position++;
        // display the next tutorial item:
        mText.setText(TEXTS[position]);
        mImage.setImageResource(IMAGES[position]);
    }
}
