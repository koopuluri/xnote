package com.xnote.wow.xnote.fragments;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xnote.wow.xnote.ArticleImageGetter;
import com.xnote.wow.xnote.Constants;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.HtmlTagHandler;
import com.xnote.wow.xnote.HtmlTagHandlerWithoutList;
import com.xnote.wow.xnote.LinkTouchMovementMethod;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.buffers.ReadBuffer;
import com.xnote.wow.xnote.models.NoteEngine;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.models.ParseNote;
import com.xnote.wow.xnote.views.ObservableScrollView;

import java.util.List;

/**
 * Created by koopuluri on 2/22/15.
 */
public class ArticleFragment extends Fragment implements ObservableScrollView.ScrollViewListener{
    public static final String TAG = "ArticleFragment";
    Spanned mContent;
    ArticleView mArticleView;
    ObservableScrollView mScrollView;
    ReadBuffer mBuffer;
    ParseArticle mArticle;
    String mArticleId;
    boolean mInitialized;
    FrameLayout mArticleContainer;
    Spanned mTitleSpan;

    // buttons:
    ImageButton mNewNoteButton;
    NoteEngine mNoteEngine;
    List<ParseNote> mNotes;
    ProgressBar mLoadingSpinner;

    TextView mTitleView;
    TextView mTimestampView;
    TextView mNumberNotesView;

    View mDecorView;

    boolean mIsNoteSelected;
    ArticleFragmentInterface mListener;

    public interface ArticleFragmentInterface {
        public ReadBuffer getRetainedBuffer();
        public void setRetainedBuffer(ReadBuffer buffer);
        public ParseArticle getRetainedArticle();
        public void setRetainedArticle(ParseArticle article);
        public List<ParseNote> getRetainedNotes();
        public void setRetainedNotes(List<ParseNote> notes);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ArticleFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "must implement ArticleFragmentInterface.");
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
        mBuffer = mListener.getRetainedBuffer();
        mArticle = mListener.getRetainedArticle();
        mNotes = mListener.getRetainedNotes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);
        mScrollView = (ObservableScrollView) view.findViewById(R.id.read_scroll_view);
        mScrollView.setScrollViewListener(this);
        mArticleView = new ArticleView(getActivity());
        mArticleContainer = (FrameLayout) view.findViewById(R.id.article_container);

        mTitleView = (TextView) view.findViewById(R.id.article_title_text_view);
        mTimestampView = (TextView) view.findViewById(R.id.article_timestamp_text_view);
        mNumberNotesView = (TextView) view.findViewById(R.id.article_number_notes_text_view);


        // setting spinner:
        mLoadingSpinner = (ProgressBar) view.findViewById(R.id.fragment_article_loading_spinner);
        mLoadingSpinner.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mArticleView.setLayoutParams(lp);

        // BUTTONS:
//        mNewNoteButton = (ImageButton) view.findViewById(R.id.new_note_button);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        params.setMargins(0, 0, 0, getNavBarHeight());

        mNewNoteButton = (ImageButton) view.findViewById(R.id.new_note_button);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                mNewNoteButton.getLayoutParams();
        int buttonMargin = getResources().getDimensionPixelSize(R.dimen.add_button_margin);
        Log.d(TAG, "NAVBAR HEIGHT: " + getNavBarHeight());
        params.setMargins(0, 0, buttonMargin, getNavBarHeight() + buttonMargin);
        mNewNoteButton.setLayoutParams(params);

        mNewNoteButton.setColorFilter(Color.parseColor("#FFFFFFFF"));
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

        mNewNoteButton.setVisibility(View.INVISIBLE);
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
        mIsNoteSelected = false;
        // deleting exisitng articleView if it exists:
        if (mBuffer != null && mArticle != null && mNotes != null) {
            initializeFromRetained();
        } else {
            Log.d(TAG, "NOT FROM RETAINED! LAUNCHING ARTICLEINITIALIZATIONTASK!");
            new ArticleInitializeTask(this, false).execute();  // false because this is not refresh, but initialization.
        }

        mDecorView = getActivity().getWindow().getDecorView();
        showSystemUI();  // to make first-time toggling smoother.
        return view;
    }


    private int getNavBarHeight() {
        //Checks to see if NavBar is present
        //http://stackoverflow.com/a/16608481/4671651
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        Resources resources = getActivity().getResources();
        if (hasBackKey && hasHomeKey) {
            return 0;
        }
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }


    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    /**
     *
     * Use to hide toolbar if scrolling down, and make visible if scrolling up.
     */
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        mArticleView.clearFocus();  // removes a selection if there is currently one.

        if (y <= 0) {
            showSystemUI();
            return;
        }

        if (y > oldy) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    /**
     * If information is retained, then this method resets all the necessary stuff. No need to call
     * Article and Buffer Initialization tasks.
     * If this is called, then mBuffer and mArticle are already set.
     */
    public void initializeFromRetained() {
        //mArticleView.setText(mBuffer.getBuffer());
        mArticleView.setTextIsSelectable(true);
        Util.setXnoteArticleTypeFace(getActivity(), mArticleView);
        mArticleView.setMovementMethod(LinkTouchMovementMethod.getInstance());
        mArticleView.setCustomSelectionActionModeCallback(
                new TextSelectionCallback());
        mArticleView.setHighlightColor(getActivity().getResources()
                .getColor(R.color.xnote_note_color));


        // TODO: set the text for the article view!
        ViewTreeObserver vto = mArticleView.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if (!mInitialized) {
//                    mArticleView.setText(mBuffer.getBuffer());
//                    mInitialized = true;
//                    Log.d(TAG, "onGlobalLayout(), not initialized, so InitializeTask executed!");
//                }
//            }
//        });
        mArticleView.setText(mBuffer.getBuffer());
        mNoteEngine = NoteEngine.getInstance();
        mNoteEngine.initializeNotes(mNotes); // TODO: makes sure this clears before adding!
        mInitialized = true;
        mLoadingSpinner.setVisibility(View.GONE);
    }

    public void setRetainedInformation() {
        // setting all of the retained values:
        mListener.setRetainedBuffer(mBuffer);
        mListener.setRetainedNotes(mNotes);
        mListener.setRetainedArticle(mArticle);
    }


    public void redraw() {
        if (mBuffer != null) {
            mArticleView.setText(mBuffer.getBuffer());
        } else {
            Log.e(TAG, "mBuffer is null!");
        }
    }


    private void setTitleAndTimeStampView() {
        String title = "<h2>" + mArticle.getTitle() + "</h2>";
        String tstamp = Util.dateFromSeconds(mArticle.getTimestamp()).toString();
        Util.setXnoteArticleTypeFace(getActivity(), mTitleView);
        Util.setXnoteNoteTypeFace(getActivity(), mTimestampView);
        String offset = "";
        for (int i = 0; i < Constants.ARTICLE_TOP_OFFSET; i++) {
            offset += "<br>";
        }
//        mTitleView.setTextSize(Constants.ARTICLE_TITLE_FONT_SIZE);
        mTimestampView.setTextSize(Constants.ARTICLE_TSTAMP_FONT_SIZE);
        mTitleSpan = Html.fromHtml(offset + "<b>" + title + "</b>");
        mTitleView.setText(mTitleSpan);
        mTimestampView.setText(tstamp);
    }


    /**
     * @param article:
     * @param activity:
     * @return Spanned
     */
    public static Spanned htmlEscapedArticleContent(ParseArticle article, Activity activity) {
        Spanned out;
        String title = "<h2>" + article.getTitle() + "</h2>";
        String timestamp = "<p>" + Util.dateFromSeconds(article.getTimestamp()).toString();
        String offset = "";
        // adding the 4 to correctly place below the divider (look in fragment_read).
        for (int i = 0; i < Constants.ARTICLE_TOP_OFFSET + 4; i++) {
            offset += "<br>";
        }

        SpannableStringBuilder transparentTitle =  (SpannableStringBuilder)
                Html.fromHtml(title + offset);
        transparentTitle.setSpan(new ForegroundColorSpan(Color.TRANSPARENT),
                0, transparentTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

//        String content = offset + title + timestamp + article.getContent();

        try {
            out = Html.fromHtml(article.getContent(),
                    new ArticleImageGetter(article.getId(), activity),
                    new HtmlTagHandler());
        } catch (RuntimeException e) {
            // catching the "PARAGRAPH span must start at paragraph boundary" exception:
            Log.d(TAG, "Paragraph span exception caught, not handling the list items.");
            out = Html.fromHtml(article.getContent(),
                    new ArticleImageGetter(article.getId(), activity),
                    new HtmlTagHandlerWithoutList());
        }
        return new SpannableString(TextUtils.concat(transparentTitle, out));
    }


    /**
     * What to text to display when sharing an article.
     * @return
     */
    public String getArticleShareMessage() {
        String out = "";
        // TODO: add more info (original article url, user name)
        out += Constants.WEB_URL + mArticleId;
        return out;
    }


    public String getArticleTitle() {
        return mArticle.getTitle();
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
            if (getActivity() == null)
                this.cancel(true);
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            mArticle = DB.getLocalArticle(mArticleId);
            String content = mArticle.getContent();
            String title = "<h2>" + mArticle.getTitle() + "</h2>";
            String timestamp = "<p>" + Util.dateFromSeconds(mArticle.getTimestamp()).toString() + "</p>";
            content = title + timestamp + content;
            mContent = htmlEscapedArticleContent(mArticle, parent.getActivity());
            return null;
        }

        public void onPostExecute(Void _) {
            super.onPostExecute(_);
            // adding to parent scrollView.
            try {
                mArticleView.setText(mContent);
                mArticleView.setTextIsSelectable(true);  //TODO: put this aticleView formatting into single func.
                Util.setXnoteArticleTypeFace(getActivity(), mArticleView);
                mArticleView.setMovementMethod(LinkTouchMovementMethod.getInstance());
                mArticleView.setCustomSelectionActionModeCallback(
                        new TextSelectionCallback());
                mArticleView.setHighlightColor(getActivity().getResources()
                        .getColor(R.color.xnote_note_color));
                setTitleAndTimeStampView();
                if (!isRefresh) {
                    mArticleContainer.addView(mArticleView); // TODO: check!
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
                //getActivity().finish();
            } catch (NullPointerException e) {
                Log.e(TAG, "nullPointer Exception " +
                        "(probably with the activity.getAssets() with null activity: " + e);
            }
        }
    }


    private void setNumberNotesView() {
        int num = mNoteEngine.size();
        if (num == 1) {
            mNumberNotesView.setText(num + " note");
        } else {
            mNumberNotesView.setText(num + " notes");
        }
    }


    private class BufferInitializeTask extends AsyncTask<Void, Void, Void> {
        boolean isUserNew;

        @Override
        public Void doInBackground(Void... params) {
            Log.d(TAG, "InitializeTask: doInBackground()");
            if (getActivity() == null)
                return null; // TODO: for when fragment not yet attached (when changing orientation).

            mNotes = DB.getNotesForArticleLocally(mArticleId);
            Log.d(TAG, "mNotes obtained from local datastore.");

            mNoteEngine = NoteEngine.getInstance();
            mNoteEngine.initializeNotes(mNotes);
            Log.d(TAG, "value of articleView's layout in InitializeTask: " +
                    String.valueOf(mArticleView.getLayout()));
            // initializing buffer and adding notes to it.
            mBuffer = new ReadBuffer(mArticleView.getLayout(), mArticleId, getActivity(), mContent);
            for (ParseNote note : mNotes) {
                Log.d(TAG, "InitializationTask: note added with noteId: " + note.getId());
                Log.d(TAG, "InitializationTask: note added with tstamp: " + note.getTimestamp());
                mBuffer.addNoteSpan(note);  // since initializing, all notes are new!
            }
            Log.d(TAG, "InitializeTask.doInBackground() complete");
            setRetainedInformation();
            Log.d(TAG, "setRetainedInformation()");

            isUserNew = DB.isNew();
            Log.d(TAG, "isUserNew: " + String.valueOf(isUserNew));
            return null;
        }


        @Override
        public void onPostExecute(Void _) {
            redraw();  // sets text for ArticleView with mBuffer.getBuffer().
            setNumberNotesView();
            mLoadingSpinner.setVisibility(View.GONE);
        }
    }


    public void addNote(ParseNote note) {
        mBuffer.addNoteSpan(note);
        mNoteEngine.addNote(note);
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

    public void destroySelection() {
        mNewNoteButton.setVisibility(View.INVISIBLE);
        mArticleView.clearFocus();
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
            setHighlightColor(getResources().getColor(R.color.accent_color_light));
            setLineSpacing(0.0f, Constants.ARTICLE_VIEW_LINE_SPACING_MULTIPLIER);
            // left, top, right, bottom:
            setPadding(0, 18, 0, 0);
            setLinkTextColor(getActivity().getResources().getColor(R.color.xnote_color));
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
            if (mIsNoteSelected) {
                mArticleView.clearFocus();  // TODO: this is reduntant, test without.
                mIsNoteSelected = false;
                destroySelection();
            }
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
                Log.d(TAG, "note is null, getting a new note");
                note = DB.getLocalNote(noteId);
            }
            Log.d(TAG, "UpdateBufferTask: noteState: " + noteState);
            if (noteState >= 0) {
                DB.saveNote(note);
                Log.d(TAG, "note saved with noteId: " + note.getId());
            } else {
                DB.deleteNote(note.getId());
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
            setNumberNotesView();
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


    public class TextSelectionCallback implements ActionMode.Callback {
        public static final String TAG = "TextSelectionCallback";

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d(TAG, "onActionItemClicked");
            switch (item.getItemId()) {
                case R.id.action_delete_note:
                    // delete selected note:
                    int start = mArticleView.getSelectionStart();
                    int end = mArticleView.getSelectionEnd();
                    String noteId = mNoteEngine.getNoteId(start, end);
                    if (noteId == null) {
                        Log.e(TAG, "this shouldn't be null");
                        return true;
                    }
                    new UpdateBuffersWithNoteTask(noteId, -1).execute();
                    Log.d(TAG, "note deleted with id: " + noteId);
            }
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.d(TAG, "onCreateActionMode()");
            showSystemUI();
            mode.setCustomView(null);
            MenuInflater inflater = mode.getMenuInflater();
            menu.removeItem(android.R.id.selectAll);

            // --------------------------------------------------------------------
            int start = mArticleView.getSelectionStart();
            int end = mArticleView.getSelectionEnd();
            if (start < 0 || end < 0 || start >= mArticleView.getText().length() ||
                    end >= mArticleView.getText().length())
                mArticleView.clearFocus();

            if (mNoteEngine.noteExistsWithRange(start, end)) {
                Log.d(TAG, "note exists within range!");
                inflater.inflate(R.menu.article_fragment_text_selection_actions, menu);
                mNewNoteButton.setVisibility(View.INVISIBLE);
                mIsNoteSelected = true;
            } else {
                Log.d(TAG, "note doesn't exist within range! new note:");
                mNewNoteButton.setVisibility(View.VISIBLE);
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            destroySelection();
            hideSystemUI();
            Log.d(TAG, "onDestroyActionMode()");
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.d(TAG, "onPrepareActionMode()");
            return false;
        }
    }
}