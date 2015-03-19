package com.xnote.wow.xnote.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseUser;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.LoginSignUpInterface;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;

/**
 * Created by Vignesh Prasad 03/02/2015
 * The welcome fragment is shown only in the first use of the application
 * Gives the user an option to register for the app or skip registration
 */
public class WelcomeFragment extends Fragment {


    public static final String TAG = "WelcomeFragment";
    Button mSignUpButton;
    TextView mContinueTextView;
    TextView mContinueToLogin;
    private LoginSignUpInterface mListener;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (LoginSignUpInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        // Used to update whether user has used this app and made a choice before
        // My first time is changed in this fragment to indicate this
        final String PREFS_NAME = "MyPrefsFile";
        final SharedPreferences settings = this.getActivity().getSharedPreferences(PREFS_NAME, 0);

        final Fragment thisFragment = this;
        mSignUpButton = (Button)view.findViewById(R.id.signup_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Register button closes the current fragment and opens up a new fragment
                settings.edit().putBoolean("my_first_time", false).apply();
                settings.edit().putBoolean("chosen_to_signup", false).apply();
                mListener.openSignUp(thisFragment);
            }
        });
        mContinueTextView = (TextView)view.findViewById(R.id.continue_textview);
        mContinueTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //If user chooses to continue without registering
                //They are taken directly to the first screen of the application
                //Enable automatic user in Parse so that their data can be stored
                Util.IS_ANON = true;
                settings.edit().putBoolean("chosen_to_signup", false).apply();
                settings.edit().putBoolean("my_first_time", false).apply();
                ParseUser.enableAutomaticUser();
                ParseUser.getCurrentUser().saveInBackground();
                Controller.launchMainActivityWithoutClearingBackStack(getActivity());
            }
        });

        mContinueToLogin = (TextView)view.findViewById(R.id.continue_to_login_textview);
        mContinueToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                settings.edit().putBoolean("chosen_to_signup", false).apply();
                settings.edit().putBoolean("my_first_time", false).apply();
                mListener.openLogin(thisFragment);
            }
        });
        return view;
    }
}