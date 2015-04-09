package com.xnote.lol.xnote.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.xnote.lol.xnote.Constants;
import com.xnote.lol.xnote.LoginSignUpInterface;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.TextValidator;


/**
 * Created by Vignesh Prasad on 03/02/2015
 * The sign up fragment signs up a user
 */
public class SignUpFragment extends Fragment {

    public static final String TAG = "Sign Up Fragment";
    Button mDoneButton;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView mLoginTextView;

    //The boolean variables keep track of preliminary validation done without using Parse
    //This allows us to make Parse calls only when necessary and also gives us freedom to define our own
    //constraints for the text field.
    public Boolean emailIsValid = true;
    public Boolean passwordIsValid = true;
    public Boolean nameIsValid = true;
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        final Fragment thisFragment = this;
        nameEditText = (EditText)view.findViewById(R.id.name);
        nameEditText.addTextChangedListener(new TextValidator(nameEditText) {
            //Override the validator of the abstract class
            @Override
            public void validate(TextView textView, String text) {
                nameIsValid = true;
                if(text.equals("")) {
                    textView.setError("Please enter a value for name");
                    //The boolean variable associated with name is set to false if the value
                    //is not a valid value. It cannot be changed to true unless the user changes
                    //the value in this field.
                    nameIsValid = false;
                }
            }
        });

        emailEditText = (EditText)view.findViewById(R.id.email);
        emailEditText.addTextChangedListener(new TextValidator(emailEditText) {
            //Override the validator of the abstract class
            @Override
            public void validate(TextView textView, String text) {
                emailIsValid = true;
                if(text.equals("")) {
                    emailIsValid = false;
                    textView.setError("Please enter a value for email");
                }
            }
        });

        passwordEditText = (EditText)view.findViewById(R.id.password);
        passwordEditText.addTextChangedListener(new TextValidator(passwordEditText) {
            //Override the validator of the abstract class
            @Override
            public void validate(TextView textView, String text) {
                passwordIsValid = true;
                if(text.length() < 8) {
                    passwordIsValid = false;
                    textView.setError("Password must be at least 8 characters in length");
                }
            }
        });

        mDoneButton = (Button)view.findViewById(R.id.done_button);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Done button registers the user through parse and takes them to application home
                //Logging out in case anonymous user is logged in
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if(nameIsValid && emailIsValid && passwordIsValid) {
                    ParseUser user = new ParseUser();
                    user.setUsername(email);
                    user.setPassword(password);
                    user.setEmail(email);
                    user.put(Constants.NAME, name);
                    user.signUpInBackground(new SignUpCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                               mListener.openSignUpSync(thisFragment);
                            } else if (e.getCode() == ParseException.USERNAME_TAKEN) {
                                emailEditText.setError("Email is already associated with an account");
                            } else if (e.getCode() == ParseException.INVALID_EMAIL_ADDRESS) {
                                emailEditText.setError("The email address you have entered is invalid");
                            } else {
                                emailEditText.setError(e.toString());
                            }
                        }
                    });
                }
            }
        });

        mLoginTextView = (TextView)view.findViewById(R.id.login_textview);
        mLoginTextView.setOnClickListener(new View.OnClickListener() {
            //Opens up the signup fragment to signup a new user
            public void onClick(View v) {
                mListener.openLogin(thisFragment);
            }
        });

        return view;
    }
}
