package com.xnote.wow.xnote.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.TextValidator;

/**
 *  Created by Vignesh Prasad on 03/02/2015
 *  Fragment for recovering password
 */
public class ForgotPasswordFragment extends Fragment {

    public final String TAG = "Forgot Password Fragment";
    Button mDoneButton;
    Button mBackButton;
    private EditText emailEditText;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        final Fragment thisFragment = this;
        final Activity activity = this.getActivity();

        emailEditText = (EditText) view.findViewById(R.id.email);
        emailEditText.addTextChangedListener(new TextValidator(emailEditText) {
            //Override the validator of the abstract class
            @Override
            public void validate(TextView textView, String text) {
                if (text.equals("")) {
                    textView.setError("Please enter a valid email ID");
                }
            }
        });

        mDoneButton = (Button) view.findViewById(R.id.done_button);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Done button displays a message and takes the user back to the login page.
                String email = emailEditText.getText().toString();
                ParseUser.requestPasswordResetInBackground(email,
                        new RequestPasswordResetCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setMessage("Your password details have been sent to you through email");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            FragmentManager fm = getActivity().getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fm.beginTransaction();
                                            Fragment fragment = new LoginFragment();
                                            fragmentTransaction.remove(thisFragment);
                                            fragmentTransaction.add(R.id.fragment_container, fragment);
                                            fragmentTransaction.commit();
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                } else {
                                    emailEditText.setError("Enter a valid email");
                                }
                            }
                        });
            }
        });

        mBackButton = (Button)view.findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            //Back button takes the user back to the login fragment
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                Fragment fragment = new LoginFragment();
                fragmentTransaction.remove(thisFragment);
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
            }
        });
        return view;
    }
}

