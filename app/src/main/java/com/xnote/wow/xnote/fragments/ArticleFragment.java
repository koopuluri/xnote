package com.xnote.wow.xnote.fragments;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xnote.wow.xnote.ArticleImageGetter;
import com.xnote.wow.xnote.Constants;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.HtmlTagHandler;
import com.xnote.wow.xnote.HtmlTagHandlerWithoutList;
import com.xnote.wow.xnote.LinkTouchMovementMethod;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.TextSelectionCallback;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.buffers.ReadBuffer;
import com.xnote.wow.xnote.models.NoteEngine;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.models.ParseNote;

import java.util.List;

/**
 * Created by koopuluri on 2/22/15.
 */
public class ArticleFragment extends Fragment implements
        TextSelectionCallback.OnTextSelectionListener {
    public static final String TAG = "ArticleFragment";
    Spanned mContent;
    ArticleView mArticleView;
    ScrollView mScrollView;
    ReadBuffer mBuffer;
    OnArticleLoaded mListener;
    LinearLayout mNoteSelectionView;
    ParseArticle mArticle;
    String mArticleId;
    boolean mInitialized;

    // buttons:
    ImageButton mNewNoteButton;
    ImageButton mDeleteNoteButton;
    NoteEngine mNoteEngine;
    List<ParseNote> mNotes;
    ProgressBar mLoadingSpinner;


    public interface OnArticleLoaded {
        public void onArticleFragmentInitialized();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnArticleLoaded) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ArticleFragmentInterface");
        }
    }


    public static Fragment newInstance(String articleId) {
        Bundle args = new Bundle();
        args.putString(Constants.ARTICLE_ID, articleId);
        Fragment frag = new ArticleFragment();
        frag.setArguments(args);
        return frag;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArticleId = getArguments().getString(Constants.ARTICLE_ID);
        mInitialized = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);
        mScrollView = (ScrollView) view.findViewById(R.id.read_scroll_view);
        mArticleView = new ArticleView(getActivity());

        // setting spinner:
        mLoadingSpinner = (ProgressBar) view.findViewById(R.id.fragment_article_loading_spinner);
        mLoadingSpinner.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mArticleView.setLayoutParams(lp);
        mNoteSelectionView = (LinearLayout) view.findViewById(R.id.note_selection_options);

        // BUTTONS:
        mNewNoteButton = (ImageButton) view.findViewById(R.id.new_note_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    // Or read size directly from the view's width/height
                    int size = getResources().getDimensionPixelSize(R.dimen.round_button_diameter);
                    outline.setOval(0, 0, size, size);
                }
            };
            mNewNoteButton.setOutlineProvider(viewOutlineProvider);
            mNewNoteButton.setClipToOutline(true);
        }
        mNewNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "newNoteButton onClick()");
                int start = mArticleView.getSelectionStart();
                int end = mArticleView.getSelectionEnd();
                if (end - start > 0 && !mNoteEngine.notesPresentWithinRange(start, end)) {
                    // launch note activity:
                    if (mArticle.getId() == null)
                        Log.e(TAG, "mArticle.getId() is null!");
                    Controller.launchNoteActivity(getActivity(), mArticle.getId(),
                            start, end);
                    ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(20);
                } else {
                    Log.d(TAG, String.format("Other notes in Range of this new note region: %s, %s", String.valueOf(start),
                            String.valueOf(end)));
                }
            }
        });


        mDeleteNoteButton = (ImageButton) view.findViewById(R.id.delete_note_button);
        mDeleteNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = mArticleView.getSelectionStart();
                int end = mArticleView.getSelectionEnd();
                if (mNoteEngine.noteExistsWithRange(start, end)) {
                    new UpdateBuffersWithNoteTask(mNoteEngine.getNoteId(start, end), -1).execute();
                    Log.d(TAG, "updateBuffer with note task launched to remove note: "
                            + mNoteEngine.getNoteId(start, end));
                } else {
                    Log.d(TAG, "Must select existing note to delete it!");
                }
            }
        });

        new ArticleInitializeTask(this, false).execute();  // false because this is not refresh, but initialization.
        return view;
    }

    public void redraw() {
        if (mBuffer != null) {
            mArticleView.setText(mBuffer.getBuffer());
        } else {
            Log.e(TAG, "mBuffer is null!");
        }
    }

    /**
     * @param content: the html string that needs to be converted to a Spanned.
     * @param articleId:
     * @return Spanned
     */
    public static Spanned htmlEscapedContent(String content, String articleId, Activity activity) {
        Spanned out;
        try {
            out = Html.fromHtml(content,
                    new ArticleImageGetter(articleId, activity),
                    new HtmlTagHandler());
        } catch (RuntimeException e) {
            // catching the "PARAGRAPH span must start at paragraph boundary" exception:
            Log.d(TAG, "Paragraph span exception caught, not handling the list items.");
            out = Html.fromHtml(content,
                    new ArticleImageGetter(articleId, activity),
                    new HtmlTagHandlerWithoutList());
        }
        return out;
    }


    public void refresh() {
        new ArticleInitializeTask(this, true).execute();
    }


    private class ArticleInitializeTask extends AsyncTask<Void, Void, Void> {
        ArticleFragment parent;
        boolean isRefresh;

        public ArticleInitializeTask(ArticleFragment parentFragment,
                                     boolean isRefresh) {
            parent = parentFragment;
            this.isRefresh = isRefresh;
        }

        @Override
        public Void doInBackground(Void... params) {
            Log.d(TAG, "ArticleInitializeTask.doInBackground()");
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            mArticle = DB.getLocalArticle(mArticleId);
            String content = mArticle.getContent();
            String title = "<h2>" + mArticle.getTitle() + "</h2>";
            String timestamp = "<p>" + Util.dateFromSeconds(mArticle.getTimestamp()).toString() + "</p>";
            content = title + timestamp + content;
            mContent = htmlEscapedContent(content, mArticleId, parent.getActivity());
            return null;
        }

        public void onPostExecute(Void _) {
            super.onPostExecute(_);
            // adding to parent scrollView.
            try {
                mArticleView.setText(mContent);
                mArticleView.setTextIsSelectable(true);
                Typeface normalTypeface = Typeface.createFromAsset(parent.getActivity().getAssets(),
                        "Dual-300.ttf");
                mArticleView.setTypeface(normalTypeface);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mArticleView.setElegantTextHeight(true);
                    mArticleView.setLetterSpacing(-0.02f);
                }
                mArticleView.setIncludeFontPadding(true);
                mArticleView.setLineSpacing(0.0f, 1.2f);
                mArticleView.setPadding(Constants.PADDING, 0, Constants.PADDING, 0);
                mArticleView.setTextSize(16);
                mArticleView.setVisibility(View.VISIBLE);
                mArticleView.setTextColor(Color.parseColor("#000000"));
                mArticleView.setBackgroundColor(getResources().getColor(android.R.color.white));
                mArticleView.setMovementMethod(LinkTouchMovementMethod.getInstance());
                mArticleView.setCustomSelectionActionModeCallback(
                        new TextSelectionCallback(parent));
                if (!isRefresh) {
                    mScrollView.addView(mArticleView);
                    ViewTreeObserver vto = mArticleView.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (!mInitialized) {
                                new BufferInitializeTask().execute();
                                mInitialized = true;
                                Log.d(TAG, "onGlobalLayout(), not initialized, so InitializeTask executed!");
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "refreshed, mInitialized set to false!");
                    // mInitialized = false; // this is so that onGlobalLayout() is executed in articleView's view tree observer.
                    // that means that BufferInitializeTask will be executed.
                    new BufferInitializeTask().execute();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "article not attached to activity?: " + e);
                getActivity().finish();
            }
        }
    }


    private class BufferInitializeTask extends AsyncTask<Void, Void, Void> {

        @Override
        public Void doInBackground(Void... params) {
            Log.d(TAG, "InitializeTask: doInBackground()");

            if (Util.isNetworkAvailable(getActivity()) && (!Util.IS_ANON)) {
                mNotes = DB.getNotesForArticleFromCloud(mArticleId);
                Log.d(TAG, "mNotes obtained from cloud.");
            } else {
                mNotes = DB.getNotesForArticleLocally(mArticleId);
                Log.d(TAG, "mNotes obtained from local datastore.");
            }

            mNoteEngine = NoteEngine.getInstance();
            mNoteEngine.initializeNotes(mNotes);
            Log.d(TAG, "value of articleView's layout in InitializeTask: " +
                    String.valueOf(mArticleView.getLayout()));
            // initializing buffer and adding notes to it.
            mBuffer = new ReadBuffer(mArticleView.getLayout(), mArticleId, getActivity(), mContent);
            for (ParseNote note : mNotes) {
                Log.d(TAG, "InitializationTask: note added with noteId: " + note.getId());
                mBuffer.addNoteSpan(note);  // since initializing, all notes are new!
            }

            Log.d(TAG, "InitializeTask.doInBackground() complete");
            return null;
        }


        @Override
        public void onPostExecute(Void _) {
            redraw();  // sets text for ArticleView with mBuffer.getBuffer().
            mListener.onArticleFragmentInitialized();
            mLoadingSpinner.setVisibility(View.GONE);
        }
    }


    public void addNote(ParseNote note) {
        mBuffer.addNoteSpan(note);
        mNoteEngine.addNote(note);
        // TODO:
        Log.d(TAG, "addNote()");
    }


    public void removeNote(ParseNote note) {
       //  mBuffer.removeNoteSpan(note);
        mBuffer.removeNoteSpan(note);
        // mNoteEngine.removeNote(startIndex, endIndex, noteId);
        mNoteEngine.removeNote(note);
        Log.d(TAG, "remove note");
    }


    public void addNoteFromNoteActivity(ParseNote note, int state) {
        Log.d(TAG, "addNoteFromNoteActivity with state: " + state);
        new UpdateBuffersWithNoteTask(note, state).execute();
    }


    @Override
    public void onTextSelectionCreate() {
        int start = mArticleView.getSelectionStart();
        int end = mArticleView.getSelectionEnd();
        mNoteSelectionView.setVisibility(View.VISIBLE);
        if (mNoteEngine.noteExistsWithRange(start, end)) {
            Log.d(TAG, "note exists within range!");
            // display edit, delete options:
            mDeleteNoteButton.setVisibility(View.VISIBLE);
            mNewNoteButton.setVisibility(View.INVISIBLE);
        } else {
            Log.d(TAG, "note doesn't exist within range! new note:");
            mNewNoteButton.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onTextSelectionDestroy() {
        mNoteSelectionView.setVisibility(View.GONE);
        mDeleteNoteButton.setVisibility(View.INVISIBLE);
        mNewNoteButton.setVisibility(View.INVISIBLE);
    }


    /**
     * The ArticleView:
     */
    public class ArticleView extends TextView {
        private Rect rect;
        private Canvas canvas;
        private Paint paint;

        private void initialize() {
            rect = new Rect(0, 0, 0, 0);
            canvas = new Canvas();
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#000000"));
            setHighlightColor(Color.parseColor("#9CCC65"));
        }

        public ArticleView(Context context) {
            super(context);
            initialize();
        }

        public ArticleView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initialize();
        }


        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(rect, paint);
        }


        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            // TODO: something.
        }
    }


    /**
     * This is called when the user is finished with the NoteActivity. Once the note activity is done,
     * 3 states are possible:
     *   1.) It could have been a completely new not.
     *   2.) A previously existing note may have been modified.
     *   3.) a note could have been deleted.
     *
     * This background task, takes the noteId and what to do with it, and updates the buffers in
     * both Read and Annotate fragments to represent this change.
     *
     * Why put in Background? because note has to be fetched from db, and spans have to be added to
     * buffers which could take time.
     * (Make sure the latter actually takes a prohibitive amount of time. Why?).
     */
    private class UpdateBuffersWithNoteTask extends AsyncTask<Void, Void, Void> {
        protected final String TAG = "UpdateBuffersNoteTask";

        int noteState;
        ParseNote note;
        String noteId;

        public UpdateBuffersWithNoteTask(ParseNote note, int state) {
            super();
            this.note = note;
            noteState = state;
            Log.d(TAG, "UpdateBufferswithNoteTask: state: " + state);
        }


        public UpdateBuffersWithNoteTask(String noteId, int state) {
            super();
            this.noteId = noteId;
            noteState = state;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // get note from db:
            // note = DB.getNote(noteId);
            if (note == null) {
                Log.e(TAG, "note can'e be null mofo!");
                note = DB.getNote(noteId);
            }

            Log.d(TAG, "UpdateBufferTask: note obtained: " + note.getStartIndex() + ", " + note.getEndIndex());
            Log.d(TAG, "UpdateBufferTask: noteState: " + noteState);

            if (noteState > 0) {
                DB.saveNote(note);
                Log.d(TAG, "note saved with noteId: " + note.getId());
            } else {
                DB.deleteNote(note);
                Log.d(TAG, "note removed with noteId: " + note.getId());
            }

            updateBuffersWithNote(note, noteState);
            Log.d(TAG, "note obtained, and BuffersUpdated in doInBackground()");
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            super.onPostExecute(_);
            // updating buffers with the note:
            redraw();
        }
    }


    private void updateBuffersWithNote(ParseNote note, int noteState) {
        if (noteState > 0) {
            addNote(note);
        } else if (noteState < 0) {
            removeNote(note);
        } else {
            // nothing needs to be done for an updated note.
        }
    }
}
