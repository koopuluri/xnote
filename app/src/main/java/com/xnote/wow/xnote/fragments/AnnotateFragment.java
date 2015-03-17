package com.xnote.wow.xnote.fragments;

/**
 * Created by koopuluri on 1/28/15.
 */
//public class AnnotateFragment extends BaseArticleFragment {
//    public static final String TAG = "AnnotateFragment";
//
//    NoteView mNoteView;
//
//    public AnnotateFragment() {
//        super(R.layout.fragment_annotate, R.id.note_annotation_view);
//    }
//
//    public static Fragment newInstance(Spanned content) {
//        Log.d(TAG, "newInstance()");
//        Fragment fragment = new AnnotateFragment();
//        Bundle args = new Bundle();
//        args.putCharSequence(BaseArticleFragment.CONTENT, content);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        Log.d(TAG, "onCreateView()");
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        PoopActivity parent = (PoopActivity) getActivity(); // TODO: remove this reference and pass articleId as args!
//        mNoteView = getNoteView();
//
//        // the following are unique to Annotate Mode:
//        mNoteView.setMovementMethod(new LinkTouchMovementMethod(getActivity(),
//                parent.getArticleId()));
//        return view;
//    }
//
//    @Override
//    public void initializeBuffersWithNotes(final List<ParseNote> notes) {
//        Log.d(TAG, "initializeBuffesWithNotes()");
//        if (mNoteView == null) {
//            Log.e(TAG, "mNoteView is null!?: " + String.valueOf(mNoteView));
//        }
////
////
////        ViewTreeObserver vto = mNoteView.getViewTreeObserver();
////        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
////            @Override
////            public void onGlobalLayout() {
////                if (!onInitializedListener.isAnnotateFragmentInitialized()) {
////                    mBuffer = new AnnotateBuffer(mNoteView.getLayout(), getActivity(), mContent);
////                    // initializing:
////                    onInitializedListener.initializeAnnotateFragment();
////                    // calling parent method:
////                    globalLayoutListenerMethod(notes);
////                } else {
////                    Log.d(TAG, "onGlobalLayout() already initialized for AnnotateFragment");
////                }
////            }
////        });
//    }
//
//    public void setScrollY(int scrollY) {
//        mNoteView.scrollTo(mNoteView.getScrollX(), scrollY);
//    }
//}