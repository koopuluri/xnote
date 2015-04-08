package com.xnote.lol.xnote;

import android.app.Fragment;

/**
 * Created by vignesh on 3/19/15.
 */
public interface LoginSignUpInterface {
    public void openLoginSync(Fragment frag);
    public void openSignUp(Fragment frag);
    public void openForgotPassword(Fragment frag);
    public void openLogin(Fragment frag);
    public void openWelcome(Fragment frag);
}