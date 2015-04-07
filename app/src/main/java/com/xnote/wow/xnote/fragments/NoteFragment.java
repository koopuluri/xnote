package com.xnote.wow.xnote.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseUser;
import com.xnote.wow.xnote.Constants;
import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.models.ParseNote;

/**
 * Created by koopuluri on 3/2/15.
 * Nice EditText subtleties:
 * http://stackoverflow.com/questions/10593195/android-edittext-turns-on-keyboard-automatically-how-to-stop
 */
public class NoteFragment extends Fragment {
    public static final String TAG = "NoteFragment";

    TextView mClippedText;
    EditText mNoteEdit;
    ParseNote mNote;
    ParseArticle mArticle;
    boolean mIsOld;
    ProgressBar mLoadingSpinner;
    boolean mInitialized;

    public static NoteFragment newInstance(String noteId) {
        Bundle args = new Bundle();
        args.putString(Constants.NOTE_ID, noteId);
        NoteFragment frag = new NoteFragment();
        frag.setArguments(args);
        return frag;
    }

    public static NoteFragment newInstance(String articleId, int startIndex, int endIndex) {
        Bundle args = new Bundle();
        args.putString(Constants.ARTICLE_ID, articleId);
        args.putInt(Constants.START_INDEX, startIndex);
        args.putInt(Constants.END_INDEX, endIndex);
        NoteFragment frag = new NoteFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(Constants.NOTE_ID))
            mIsOld = true;
        else
            mIsOld = false;
        mInitialized = false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_note, container, false);
        mClippedText = (TextView) view.findViewById(R.id.note_clipped_text);
        mNoteEdit = (EditText) view.findViewById(R.id.note_edit_text);
        //mNoteEdit.requestFocus();
        mNoteEdit.clearFocus();

        Util.setXnoteNoteTypeFace(getActivity(), mClippedText);
        Util.setXnoteNoteTypeFace(getActivity(), mNoteEdit);

        mLoadingSpinner = (ProgressBar) view.findViewById(R.id.note_loading_spinner);
        new NoteInitializeTask().execute();
        return view;
    }


    public void done() {
        if (mIsOld) {
            done(0);
        } else {
            done(1);
        }
    }


    /**
     * Sharing a note message with
     *
     * - clippedText
     * - noteContent
     * - author
     * - timestamp
     * - “from xnote-Android”
     * - xnote.io link (for this user)
     *
     * @return string with above information.
     */
    public String getNoteShareMessage() {
        String out = "";
        out += (mClippedText.getText() + "\n");
        out += ("Note by " + ParseUser.getCurrentUser().get(Constants.NAME) + ": \n");
        out += ("\"" + mNoteEdit.getText() + "\"\n\n");
        out += ("original article url: \n" + mArticle.getArticleUrl() + "\n\n");
        out += "sent from Xnote-Android";
        return out;
    }


    public String getArticleTitle() {
        return mArticle.getTitle();
    }


    private class NoteInitializeTask extends AsyncTask<Void, Void, Void> {
        Spanned clipBuffer;
        @Override
        public void onPreExecute() {
            mLoadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        public Void doInBackground(Void... params) {
            Log.d(TAG, "NoteInitializationTask.doInBackground()");
            if (mIsOld) {
                Log.d(TAG, "done(): OLD_NOTE!");
                mNote = DB.getLocalNote(getArguments().getString(Constants.NOTE_ID));
                mArticle = DB.getLocalArticle(mNote.getArticleId());
                String selectedText = mNote.getSelectedText();
                clipBuffer = Html.fromHtml("<br><i>\"" + Html.fromHtml(selectedText).toString()
                        + "\"</i><br>");
            } else {
                Log.d(TAG, "done(): NEW NOTE!");
                mArticle = DB.getLocalArticle(getArguments().getString(Constants.ARTICLE_ID));
                mNote = new ParseNote();
                mNote.setStartIndex(getArguments().getInt(Constants.START_INDEX));
                mNote.setEndIndex(getArguments().getInt(Constants.END_INDEX));
                mNote.setArticleId(mArticle.getId());
                mNote.setTimestamp(System.currentTimeMillis());
                Log.d(TAG, "Timestamp for note : " + Util.dateFromSeconds(mNote.getTimestamp()));
                mNote.setId();

                Spanned articleBuffer = ArticleFragment.htmlEscapedArticleContent(
                        mArticle,
                        getActivity());
                Spanned selectedBuffer = (Spanned)
                        articleBuffer.subSequence(mNote.getStartIndex(), mNote.getEndIndex());

                clipBuffer = Html.fromHtml("<br><i>\"" + selectedBuffer.toString() + "\"</i><br>");
                String selectedHtml = Html.toHtml(selectedBuffer);
                Log.d(TAG, "selectedHtml: " + selectedHtml);
                mNote.setSelectedText(selectedHtml);
            }
            return null;
        }


        @Override
        public void onPostExecute(Void _) {
            super.onPostExecute(_);
            mLoadingSpinner.setVisibility(View.GONE);
            mClippedText.setText(clipBuffer);
            if (mIsOld) {
                mNoteEdit.setText(mNote.getContent());
                mNoteEdit.setSelection(mNoteEdit.getText().length());
            }
            else mNoteEdit.setHint(R.string.note_hint);
            mInitialized = true;
        }
    }

    public String getNoteId() {
        return getArguments().getString(Constants.NOTE_ID);
    }

    @Override
    public String toString() {
        return TAG;
    }

    public void delete() {
        done(-1);
    }

    private void done(int noteState) {
        if (!mInitialized)
            return;
        mNote.setContent(mNoteEdit.getText().toString());
        Log.d(TAG, "done(): noteId: " + mNote.getId());
        Log.d(TAG, "done(): tstamp: " + Util.dateFromSeconds(mNote.getTimestamp()));
        Intent intent = new Intent();
        intent.putExtra(Constants.NOTE_ID, mNote.getId());
        intent.putExtra(Constants.START_INDEX, mNote.getStartIndex());
        intent.putExtra(Constants.END_INDEX, mNote.getEndIndex());
        intent.putExtra(Constants.NOTE_CONTENT, mNote.getContent());
        intent.putExtra(Constants.NOTE_TIMESTAMP, mNote.getTimestamp());
        intent.putExtra(Constants.ARTICLE_ID, mNote.getArticleId());
        intent.putExtra(Constants.NOTE_SELECTED_TEXT, mNote.getSelectedText());
        // note state determines whether this note previously existed and was modified, or it is new, or it was deleted.
        intent.putExtra(Constants.NOTE_STATE, noteState);
        // performing harakiri for the betterment of the other notes. :(
        Log.d(TAG, "about to return the result in done()");
        getActivity().setResult(getActivity().RESULT_OK, intent);
        Log.d(TAG, "result set!");
        getActivity().finish();  // (^(TT)^)
        Log.d(TAG, "finished!");
    }
}
