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
import com.xnote.lol.xnote.LoginSignUpInterface;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Util;

/**
 * Created by koopuluri on 3/18/15.
 */
public class LoginSyncFragment extends Fragment {
    public static final String TAG = "LoginSyncFragment";
    TextView mSyncMessage;
    ProgressBar mLoadingSpinner;
    Button mMainButton;
    boolean syncCompleted;
    LoginSignUpInterface mListener;

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
        Util.setGlobalNeedToSyncVariable(getActivity(), true);
        new LoginTask(getActivity()).execute();
        return view;
    }

    /**
     * Only executed if the user has successfuly been logged in.
     */
    private class LoginTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "LoginFragment.LoginTask";
        Activity parentActivity;

        public LoginTask(Activity activity) {
            parentActivity = activity;
        }

        @Override
        public void onPreExecute() {
            syncCompleted = false;
            mLoadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        public Void doInBackground(Void... params) {
            Util.IS_ANON = false;
            try {
                DB.sync();
                syncCompleted = true;
            } catch (com.parse.ParseException e) {
                // do nothing.
            }
            return null;
        }

        @Override
        public void onPostExecute(Void _) {
            super.onPostExecute(_);
            if (!syncCompleted) {
                mSyncMessage.setText("Syncing failed. " +
                        "Try again to sync in app settings when you have a better connection.");
                mMainButton.setVisibility(View.VISIBLE);
                mLoadingSpinner.setVisibility(View.GONE);
                Util.setGlobalNeedToSyncVariable(parentActivity, true);
                // launching the main activity:
                Controller.launchMainActivity(parentActivity);
                parentActivity.finish();
            } else {
                // setting the NEED_TO_SYNC shared preference to false.
                Util.setGlobalNeedToSyncVariable(parentActivity, false);

                // now launching the main activity:
                Controller.launchMainActivity(parentActivity);
                parentActivity.finish();
            }
        }
    }
}
