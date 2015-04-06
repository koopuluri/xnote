package com.xnote.wow.xnote.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.parse.ParseUser;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.models.ParseFeedback;

public class FeedbackActivity extends Activity {

    public static final String TAG = "FeedbackActivity";

    ImageButton mDoneButton;
    EditText mCommentsEditText;
    Spinner mFeedbackNature;
    String spin;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_xnote_navigation_up_colored);
        activity = this;
        mFeedbackNature = (Spinner) findViewById(R.id.feedback_nature_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.feedback_nature_array, android.R.layout.simple_spinner_item);
        //Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mFeedbackNature.setAdapter(adapter);
        mFeedbackNature.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                spin = parent.getItemAtPosition(pos).toString();
            }

            public void onNothingSelected(AdapterView<?> parent) {
                spin = parent.getItemAtPosition(0).toString();
            }
        });

        mCommentsEditText = (EditText) findViewById(R.id.comments_edit_text);
        Util.setXnoteNoteTypeFace(activity, mCommentsEditText);

        mDoneButton = (ImageButton) findViewById(R.id.done_button);
        mDoneButton.setColorFilter(Color.parseColor("#FFFFFFFF"));
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Done button logs in the user if the details are correct

                ParseFeedback feedback = new ParseFeedback();
                feedback.setFeedbackType(spin);
                feedback.setComments(mCommentsEditText.getText().toString());
                if(!Util.IS_ANON) {
                    feedback.setUser(ParseUser.getCurrentUser().getUsername());
                }
                DB.saveFeedbackInBackground(feedback);
                Controller.launchMainActivity(activity);
            }
        });


    }
}
