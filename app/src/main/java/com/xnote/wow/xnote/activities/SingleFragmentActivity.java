package com.xnote.wow.xnote.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.xnote.wow.xnote.R;

/**
 * Created by koopuluri on 1/23/15.
 * Abstract class that is used for all single fragment activities. (Obtained from: Big Nerd Ranch: Android Programming).
 */
public abstract class SingleFragmentActivity extends Activity {
    public static final String TAG = "SingleFragmentActivity";

    protected abstract Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.single_fragment_container);
        if (fragment == null) {  // only want to add fragment if one is not already hosted by the SingleFragmentActivity.
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.single_fragment_container, fragment)
                    .addToBackStack(fragment.getTag())
                    .commit();
        }
    }
}
