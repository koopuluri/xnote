package com.xnote.lol.xnote.fragments;

import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.xnote.lol.xnote.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link TutorialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TutorialFragment extends Fragment {

    private static final String POSITION = "position";
    private int position;
    private static final String TAG = "TutorialFragment";

    String[] TEXTS = {
            "When you come across an article on your device that " +
                    "you'd like to save, select the share option and \"Add to xnote\".",
            "Highlight to take notes.",
            "Share an individual note...",
            "... or an entire article containing all of your notes." +
                    " The recipient will receive a url at xnote.io with your annotated article."};

    int[] IMAGES = {
            R.anim.save_tutorial,
            R.anim.annotate_tutorial,
            R.anim.noteshare_tutorial,
            R.drawable.articleshare_tutorial};

    TextView mTextView;
    ImageView mImageView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @param position Parameter position.
     * @return A new instance of fragment TutorialFragment.
     */
    public static TutorialFragment newInstance(int position) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
        mTextView = (TextView)view.findViewById(R.id.tutorial_content);
        mTextView.setText(TEXTS[position]);
        mImageView = (ImageView) view.findViewById(R.id.tutorial_image);
        //TODO: Render super low quality images in case of out of memory errors
        mImageView.setImageResource(IMAGES[position]);
        try {
            AnimationDrawable tutorialAnimation = (AnimationDrawable) mImageView.getDrawable();
            tutorialAnimation.start();
        } catch(OutOfMemoryError e) {
            // do nothing.
        } catch (ClassCastException ex) {
            // do nothing.
        }
        return view;
    }
}
