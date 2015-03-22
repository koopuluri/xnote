package com.xnote.wow.xnote.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Spanned;

import com.xnote.wow.xnote.buffers.ReadBuffer;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.models.ParseNote;

import java.util.List;

/**
 * Created by koopuluri on 3/21/15.
 */
public class ArticleRetainedFragment extends Fragment {
    public static final String TAG = "ArticleRetainedFragment";
    // data object we want to retain
    private ReadBuffer mBuffer;
    private ParseArticle mArticle;
    private List<ParseNote> mNotes;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setArticleBuffer(ReadBuffer buffer) {
        mBuffer = buffer;
    }

    public ReadBuffer getArticleBuffer() {
        return mBuffer;
    }

    public void setArticle(ParseArticle article) {
        mArticle = article;
    }

    public ParseArticle getArticle() {
        return mArticle;
    }


    public void setNotes(List<ParseNote> notes) {
        mNotes = notes;
    }

    public List<ParseNote> getNotes() {
        return mNotes;
    }
}
