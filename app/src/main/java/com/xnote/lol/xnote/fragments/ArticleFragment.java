package com.xnote.lol.xnote.fragments;

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

import com.xnote.lol.xnote.ArticleImageGetter;
import com.xnote.lol.xnote.Constants;
import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.DB;
import com.xnote.lol.xnote.HtmlTagHandler;
import com.xnote.lol.xnote.HtmlTagHandlerWithoutList;
import com.xnote.lol.xnote.LinkTouchMovementMethod;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Util;
import com.xnote.lol.xnote.XnoteLogger;
import com.xnote.lol.xnote.buffers.ReadBuffer;
import com.xnote.lol.xnote.models.NoteEngine;
import com.xnote.lol.xnote.models.ParseArticle;
import com.xnote.lol.xnote.models.ParseNote;
import com.xnote.lol.xnote.views.ObservableScrollView;

import org.json.JSONException;
import org.json.JSONObject;

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

    // timing article initialization:
    long t0;
    long t1;

    boolean mIsNoteSelected;
    ArticleFragmentInterface mListener;

    public interface ArticleFragmentInterface {
        public ReadBuffer getRetainedBuffer();
        public void setRetainedBuffer(ReadBuffer buffer);
        public ParseArticle getRetainedArticle();
        public void setRetainedArticle(ParseArticle article);
        public List<ParseNote> getRetainedNotes();
        public void setRetainedNotes(List<ParseNote> notes);
        public XnoteLogger getLogger();
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
        t0 = System.currentTimeMillis();
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

        mNewNoteButton = (ImageButton) view.findViewById(R.id.new_note_button);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                mNewNoteButton.getLayoutParams();
        int buttonMargin = getResources().getDimensionPixelSize(R.dimen.add_button_margin);
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
                int start = mArticleView.getSelectionStart();
                int end = mArticleView.getSelectionEnd();
                if (end - start > 0 && !mNoteEngine.notesPresentWithinRange(start, end)) {
                    // launch note activity:
                    Controller.launchNoteActivity(getActivity(), mArticle.getId(),
                            start, end);
                    ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(20);
                } else {
                    // do nothing.
                }
            }
        });
        mIsNoteSelected = false;
        // deleting exisitng articleView if it exists:
        if (mBuffer != null && mArticle != null && mNotes != null) {
            initializeFromRetained();
        } else {
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
            // do nothing.
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
    public static Spanned htmlEscapedArticleContent(ParseArticle article,
                                                    Activity activity,
                                                    XnoteLogger logger) {
        Spanned out;
        String title = "<b><h2>" + article.getTitle() + "</h2></b>";
        String beforeTitleOffset = "";
        // adding the 4 to correctly place below the divider (look in fragment_read).
        for (int i = 0; i < Constants.ARTICLE_TOP_OFFSET; i++) {
            beforeTitleOffset += "<br>";
        }

        String afterTitleOffset = "";
        for (int i = 0; i < Constants.ARTICLE_ADDITIONAL_OFFSET; i++) {
            afterTitleOffset += "<br>";
        }

        SpannableStringBuilder transparentTitle =  (SpannableStringBuilder)
                Html.fromHtml(beforeTitleOffset + title);
        transparentTitle.setSpan(new ForegroundColorSpan(Color.TRANSPARENT),
                0, transparentTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        try {
            out = Html.fromHtml(afterTitleOffset + article.getContent(),
                    new ArticleImageGetter(article.getId(), activity),
                    new HtmlTagHandler());
        } catch (RuntimeException e) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("ArticleId", article.getId());
                obj.put("ArticleTitle", article.getTitle());
                obj.put("RuntimeException", e);
            } catch (JSONException ex) {
                // do nothing.
            }
            logger.log("ArticleFragment.htmlEscapedContent.RuntimeException", obj);
            // catching the "PARAGRAPH span must start at paragraph boundary" exception:
            out = Html.fromHtml(afterTitleOffset + article.getContent(),
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
            if (getActivity() == null) {
                this.cancel(true);
            }
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            mArticle = DB.getLocalArticle(mArticleId);
            String content = mArticle.getContent();
            String title = "<h2>" + mArticle.getTitle() + "</h2>";
            String timestamp = "<p>" + Util.dateFromSeconds(mArticle.getTimestamp()).toString() + "</p>";
            content = title + timestamp + content;
            mContent = htmlEscapedArticleContent(mArticle, parent.getActivity(),
                    mListener.getLogger());
            mArticle.setAndroidEscapedContent(Html.toHtml(mContent));
            DB.saveArticle(mArticle);  // saving the androidEscapedContent.
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
                            }
                        }
                    });
                } else {
                    // mInitialized = false; // this is so that onGlobalLayout() is executed in articleView's view tree observer.
                    // that means that BufferInitializeTask will be executed.
                    new BufferInitializeTask().execute();
                }
            } catch (IllegalStateException e) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("ArticleId", mArticleId);
                    obj.put("ArticleTitle", mArticle.getTitle());
                } catch (JSONException jsonException) {
                    // do nothing.
                }
                mListener.getLogger().log("ArticleFragment.ArticleInitialization" +
                        ".IllegalStateException", obj);

                try {
                    getActivity().finish();
                } catch (NullPointerException nullPointer) {
                    // do nothing... happens when quickly opened and closed sometimes.
                }
            } catch (NullPointerException e) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("ArticleId", mArticleId);
                    obj.put("ArticleTitle", mArticle.getTitle());
                } catch (JSONException jsonException) {
                    // do nothing.
                }
                mListener.getLogger().log("ArticleFragment.ArticleInitialization" +
                        ".NullPointerException", obj);
                try {
                    getActivity().finish();
                } catch (NullPointerException nullPointer) {
                    // do nothing... happens when quickly opened and closed sometimes.
                }
            }
        }
    }


    private void setNumberNotesView() {
        try {
            int num = mNoteEngine.size();
            if (num == 1) {
                mNumberNotesView.setText(num + " note");
            } else {
                mNumberNotesView.setText(num + " notes");
            }
        } catch (NullPointerException e) {
            mListener.getLogger().log("ArticleFragment.NoteEngine.Nullpointer", null);
        }
    }


    private class BufferInitializeTask extends AsyncTask<Void, Void, Void> {
        boolean isUserNew;

        @Override
        public Void doInBackground(Void... params) {
            if (getActivity() == null)
                return null; // TODO: for when fragment not yet attached (when changing orientation).

            mNotes = DB.getNotesForArticleLocally(mArticleId);
            mNoteEngine = NoteEngine.getInstance();
            mNoteEngine.initializeNotes(mNotes);
            // initializing buffer and adding notes to it.
            mBuffer = new ReadBuffer(mArticleView.getLayout(), mArticleId, getActivity(), mContent);
            for (ParseNote note : mNotes) {
                mBuffer.addNoteSpan(note);  // since initializing, all notes are new!
            }
            setRetainedInformation();
            isUserNew = DB.isNew();
            return null;
        }


        @Override
        public void onPostExecute(Void _) {
            redraw();  // sets text for ArticleView with mBuffer.getBuffer().
            setNumberNotesView();
            mLoadingSpinner.setVisibility(View.GONE);
            t1 = System.currentTimeMillis();

            // analytics:
            JSONObject obj = new JSONObject();
            try {
                obj.put("timeTaken", t1 - t0);
                obj.put("ArticleId", mArticleId);
                obj.put("ArticleTitle", mArticle.getTitle());
            } catch (JSONException e) {
                // do nothing
            }
            mListener.getLogger().log("ArticleFragment.InitializationTime", obj);
        }
    }


    public void addNote(ParseNote note) {
        mBuffer.addNoteSpan(note);
        mNoteEngine.addNote(note);
    }


    public void removeNote(ParseNote note) {
        //  mBuffer.removeNoteSpan(note);
        mBuffer.removeNoteSpan(note);
        // mNoteEngine.removeNote(startIndex, endIndex, noteId);
        mNoteEngine.removeNote(note);
    }


    public void addNoteFromNoteActivity(ParseNote note, int state) {

        // analytics:
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
                note = DB.getLocalNote(noteId);
            }
            if (noteState >= 0) {
                // analytics:
                if (noteState > 0) {
                    mListener.getLogger().getPeople().increment("numberNotes", 1);
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("noteId", note.getId());
                        obj.put("ArticleId", mArticleId);
                        obj.put("ArticleTitle", mArticle.getTitle());
                    } catch (JSONException e) {
                        // do nothing.
                    }
                    mListener.getLogger().log("ArticleFragment.NoteAdded", obj);
                    mListener.getLogger().getPeople().increment(Constants.NUMBER_NOTES, 1);
                }

                // actually saving it:
                DB.saveNote(note);
            } else {
                // analytics:
                mListener.getLogger().getPeople().increment("numberNotes", -1);
                JSONObject obj = new JSONObject();
                try {
                    obj.put("noteId", note.getId());
                    obj.put("ArticleId", mArticleId);
                    obj.put("ArticleTitle", mArticle.getTitle());
                } catch (JSONException e) {
                    // do nothing.
                }
                mListener.getLogger().log("ArticleFragment.NoteDeleted", obj);
                mListener.getLogger().getPeople().increment(Constants.NUMBER_NOTES, -1);

                // actually deleting it:
                DB.deleteNote(note.getId());
            }

            updateBuffersWithNote(note, noteState);
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
            switch (item.getItemId()) {
                case R.id.action_delete_note:
                    // delete selected note:
                    int start = mArticleView.getSelectionStart();
                    int end = mArticleView.getSelectionEnd();
                    String noteId = mNoteEngine.getNoteId(start, end);
                    if (noteId == null) {
                        return true;
                    }
                    new UpdateBuffersWithNoteTask(noteId, -1).execute();
            }
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            showSystemUI();
            mode.setCustomView(null);
            MenuInflater inflater = mode.getMenuInflater();
            menu.removeItem(android.R.id.selectAll);

            // --------------------------------------------------------------------
            int start = mArticleView.getSelectionStart();
            int end = mArticleView.getSelectionEnd();

            // analytics:
            try {
                JSONObject props = new JSONObject();
                props.put("selectionStart", start);
                props.put("ArticleId", mArticleId);
                props.put("ArticleTitle", mArticle.getTitle());
                mListener.getLogger().log("ArticleFragment.ArticleHighlight", props);
            } catch (JSONException e) {
                // do nothing.
            }


            if (start < 0 || end < 0 || start >= mArticleView.getText().length() ||
                    end >= mArticleView.getText().length())
                mArticleView.clearFocus();

            if (mNoteEngine.noteExistsWithRange(start, end)) {
                inflater.inflate(R.menu.article_fragment_text_selection_actions, menu);
                mNewNoteButton.setVisibility(View.INVISIBLE);
                mIsNoteSelected = true;
            } else {
                mNewNoteButton.setVisibility(View.VISIBLE);
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            destroySelection();
            hideSystemUI();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }
}