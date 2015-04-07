package com.xnote.lol.xnote.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.DB;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Util;

import java.text.ParseException;

/**
 * Created by koopuluri on 3/18/15.
 */
public class LoginSyncFragment extends Fragment {
    public static final String TAG = "LoginSyncFragment";
    TextView mSyncMessage;
    ProgressBar mLoadingSpinner;
    Button mMainButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_sync, container, false);
        mSyncMessage = (TextView) view.findViewById(R.id.sync_message);
        mLoadingSpinner = (ProgressBar) view.findViewById(R.id.sync_loading_spinner);
        mMainButton = (Button) view.findViewById(R.id.main_activity_button);
        mMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // launch main Activity:
                Controller.launchMainActivity(getActivity());
                getActivity().finish();
            }
        });
        mMainButton.setVisibility(View.GONE);
        mSyncMessage.setText("Login successful, now syncing your articles and notes...");
        Util.setXnoteNoteTypeFace(getActivity(), mSyncMessage);
        new LoginTask(getActivity()).execute();
        return view;
    }

    /**
     * Only executed if the user has successfuly been logged in.
     */
    private class LoginTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "LoginFragment.LoginTask";
        Activity parentActivity;
        boolean mSuccessful;

        public LoginTask(Activity activity) {
            parentActivity = activity;
            mSuccessful = false;
        }

        @Override
        public void onPreExecute() {
            mLoadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        public Void doInBackground(Void... params) {
            Util.IS_ANON = false;
            try {
                DB.sync();
                mSuccessful = true;
            } catch (com.parse.ParseException e) {
                // do nothing.
            }
            return null;
        }

        @Override
        public void onPostExecute(Void _) {
            super.onPostExecute(_);
            if (!mSuccessful) {
                mSyncMessage.setText("syncing failed. " +
                        "Try again to refresh in app when you have a better connection.");
                mMainButton.setVisibility(View.VISIBLE);
                mLoadingSpinner.setVisibility(View.GONE);
            } else {
                Controller.launchMainActivity(parentActivity);
                parentActivity.finish();
            }
        }
    }
}
