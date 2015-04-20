package com.xnote.lol.xnote.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.ParseUser;
import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Util;
import com.xnote.lol.xnote.XnoteLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *  Created by Vignesh Prasad on 03/02/2015
 */
public class SettingsFragment extends ListFragment {

    public static final String TAG = "Settings Fragment";
    public static final String LOGOUT = "Logout";
    public static final String FEEDBACK = "Feedback";
    public static final String SIGN_UP = "Sign Up";
    public static final String TUTORIAL = "Tutorial";

    ArrayList<String> mOptionsList;
    SettingsInterface mListener;


    public interface SettingsInterface {
        public XnoteLogger getLogger();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SettingsInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SettingsInteface.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //OptionsList stores the options available to the user
        //Currently only logout or login
        mOptionsList = new ArrayList<String>();
        mOptionsList.add(FEEDBACK);
        mOptionsList.add(TUTORIAL);
        if (Util.IS_ANON) {
            mOptionsList.add(SIGN_UP);
        } else {
            mOptionsList.add(LOGOUT);
        }
        final ArrayAdapter mAdapter = new ArrayAdapter(this.getActivity(),
                android.R.layout.simple_list_item_1, mOptionsList);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // when an option is selected from the Article List:
        String ch = (String) getListAdapter().getItem(position);
        if (ch.equalsIgnoreCase(LOGOUT)) {
            //User is logged out if they choose logout
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.are_you_sure_logout);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    new LogoutTask(getActivity()).execute();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (ch.equalsIgnoreCase(SIGN_UP)) {
            Controller.launchSignUpFromAnonymousUser(this.getActivity());
        } else if (ch.equalsIgnoreCase(FEEDBACK)) {
            Controller.launchFeedbackActivity(this.getActivity());
        } else if (ch.equalsIgnoreCase(TUTORIAL)) {
            Controller.launchTutorialActivity(this.getActivity());
        }
    }



    private class LogoutTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "SettingsFragment.LogoutTask";
        Activity parentActivity;

        public LogoutTask(Activity activity) {
            parentActivity = activity;
        }

        @Override
        public void onPreExecute() {
            // TODO: start spinner.
        }

        @Override
        public Void doInBackground(Void... params) {
            ParseUser.logOut();

            // analytics:
            mListener.getLogger().log("SettingsFragment.Logout", null);

            while(ParseUser.getCurrentUser() != null) {
                //TODO: MAJOR BANDAID HERE! WHY DOESNT LOGOUT WORK THE FIRST TIME?
                ParseUser.logOut();
            }
            return null;
        }

        @Override
        public void onPostExecute(Void _) {
            super.onPostExecute(_);
            Controller.launchLoginSignUpActivity(parentActivity);
            parentActivity.finish();
            // TODO: end spinner.
        }
    }
}
