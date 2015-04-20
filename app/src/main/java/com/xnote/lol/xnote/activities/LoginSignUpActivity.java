package com.xnote.lol.xnote.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.xnote.lol.xnote.Constants;
import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.LoginSignUpInterface;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Util;
import com.xnote.lol.xnote.XnoteLogger;
import com.xnote.lol.xnote.fragments.ForgotPasswordFragment;
import com.xnote.lol.xnote.fragments.LoginFragment;
import com.xnote.lol.xnote.fragments.LoginSyncFragment;
import com.xnote.lol.xnote.fragments.SignUpFragment;
import com.xnote.lol.xnote.fragments.SignUpSyncFragment;
import com.xnote.lol.xnote.fragments.WelcomeFragment;


/**
 * Created by Vignesh Prasad on 03/02/2015
 * The activity that controls all login and registration activity
 * It opens up fragments based on whether the user has used the application before
 * and depending on whether the user log in information is stored
 */
public class LoginSignUpActivity extends Activity implements LoginSignUpInterface {
    public final String TAG = "LoginSignUpActivity";
    XnoteLogger logger;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new XnoteLogger(getApplicationContext());
        setContentView(R.layout.activity_loginsignup);
        //Activity variable is used so that inner classes can refer to current activity
        final Activity activity = this;
        // Creating SharedPreferences to check if the app is being used the first time
        // Also checks if the user has chosen to continue without registering
        // http://stackoverflow.com/a/13237848
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Fragment manager stuff to call the necessary fragments when needed
        if (settings.getBoolean("my_first_time", true)) {
            // the app is being launched for first time show the registration information
            // record the fact that the app has been started at least once
            // used apply rather than commit so it runs in the background
            // Note my first time is updated in hte fragment
            settings.edit().putBoolean("chosen_to_signup", false).apply();
            this.openWelcome(null);
        } else {
            //Check if the user has logged in before and the details are on the cache
            ParseUser currentUser = ParseUser.getCurrentUser();
            if(settings.getBoolean("chosen_to_signup", true)) {
                //Anonymous user has indicated that she wants to sign up
                settings.edit().putBoolean("chosen_to_signup", false).apply();
                this.openSignUp(null);
            } else if ((currentUser != null) &&
                    (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser()))) {
                //If login details are in the cache and user is not anonymous
                // now check if sync needs to be performed (this would be the case if the
                // app was closed during a sync.
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                boolean need_to_sync = prefs.getBoolean(Constants.NEED_TO_SYNC, false);
                if (need_to_sync) {
                    openLoginSync(null);  // opening up the sync fragment.
                    logger.log("LoginSignUpActivity.ReSyncing", null);
                    return;
                }

                // analytics:
                logger.getPeople().identify(currentUser.getObjectId());
                logger.identify(currentUser.getObjectId());
                logger.flush();
                Util.IS_ANON = false;
                Controller.launchMainActivity(activity);
                finish();
            } else {
                //Check if the user has indicated anonymous user preference before
                if ((currentUser != null) &&
                        (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser()))) {
                    Util.IS_ANON = true;
                    ParseAnonymousUtils.logIn(new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            // tracking the logged in user:
                            logger.identify(parseUser.getObjectId());
                            logger.getPeople().identify(parseUser.getObjectId());
                            logger.flush();
                        }
                    });
                    ParseUser.getCurrentUser().saveInBackground();
                    Controller.launchMainActivity(activity);
                    finish();
                } else {
                    // If user is not anonymous then the user must be asked to login
                    settings.edit().putBoolean("chosen_to_signup", false).apply();
                    this.openLogin(null);
                }
            }
        }
    }


    @Override
    public void openLoginSync(Fragment frag) {
        // need to change fragment to LoginSyncFragment.java:
        // toggle the global constant so that if the sync stops midway, the sync can be restarted
        // when the application is opened again.

        // setting the shared preference global var:
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean(Constants.NEED_TO_SYNC, true);
        editor.apply();

        if (frag != null) {
            getFragmentManager().beginTransaction()
                    .remove(frag)
                    .add(R.id.fragment_container, new LoginSyncFragment())
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new LoginSyncFragment())
                    .commit();
        }
    }


    @Override
    public void openSignUpSync(Fragment frag) {
        // need to change fragment to SignUpSyncFragment.java:
        if (frag != null) {
            getFragmentManager().beginTransaction()
                    .remove(frag)
                    .add(R.id.fragment_container, new SignUpSyncFragment())
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new SignUpSyncFragment())
                    .commit();
        }
    }


    @Override
    public void openSignUp(Fragment frag) {
        if(frag != null) {
            getFragmentManager().beginTransaction()
                    .remove(frag)
                    .add(R.id.fragment_container, new SignUpFragment())
                    .addToBackStack(SignUpFragment.TAG)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new SignUpFragment())
                    .commit();
        }
    }


    @Override
    public void openForgotPassword(Fragment frag) {
        if(frag != null) {
            getFragmentManager().beginTransaction()
                    .remove(frag)
                    .add(R.id.fragment_container, new ForgotPasswordFragment())
                    .addToBackStack(ForgotPasswordFragment.TAG)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new ForgotPasswordFragment())
                    .commit();
        }
    }


    @Override
    public void openLogin(Fragment frag) {
        if(frag != null) {
            getFragmentManager().beginTransaction()
                    .remove(frag)
                    .add(R.id.fragment_container, new LoginFragment())
                    .addToBackStack(LoginFragment.TAG)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
    }


    @Override
    public void openWelcome(Fragment frag) {
        if(frag != null) {
            getFragmentManager().beginTransaction()
                    .remove(frag)
                    .add(R.id.fragment_container, new WelcomeFragment())
                    .addToBackStack(WelcomeFragment.TAG)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new WelcomeFragment())
                    .commit();
        }
    }

    @Override
    public XnoteLogger getLogger() {
        return logger;
    }
}
