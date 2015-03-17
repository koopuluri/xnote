package com.xnote.wow.xnote.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseUser;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;

/**
 * Created by Vignesh Prasad 03/02/2015
 * The welcome fragment is shown only in the first use of the application
 * Gives the user an option to register for the app or skip registration
 */
public class WelcomeFragment extends Fragment {

    Button mSignUpButton;
    TextView mContinueTextView;
    TextView mContinueToLogin;

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
                FragmentManager fm = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                Fragment fragment = new SignUpFragment();
                fragmentTransaction.remove(thisFragment);
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
            }
        });
        mContinueTextView = (TextView)view.findViewById(R.id.continue_textview);
        mContinueTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //If user chooses to continue without registering
                //They are taken directly to the first screen of the application
                //Enable automatic user in Parse so that their data can be stored
                Util.IS_ANON = true;
                ParseUser.enableAutomaticUser();
                ParseUser.getCurrentUser().saveInBackground();
                Controller.launchMainActivityWithoutClearingBackStack(getActivity());
                settings.edit().putBoolean("chosen_to_signup", false).apply();
                settings.edit().putBoolean("my_first_time", false).apply();
            }
        });

        mContinueToLogin = (TextView)view.findViewById(R.id.continue_to_login_textview);
        mContinueToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                Fragment fragment = new LoginFragment();
                fragmentTransaction.remove(thisFragment);
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
                settings.edit().putBoolean("chosen_to_signup", false).apply();
                settings.edit().putBoolean("my_first_time", false).apply();
            }
        });
        return view;
    }
}