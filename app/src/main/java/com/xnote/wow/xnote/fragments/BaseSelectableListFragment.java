package com.xnote.wow.xnote.fragments;

import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.adapters.ArticleAdapter;
import com.xnote.wow.xnote.adapters.BaseListAdapter;
import com.xnote.wow.xnote.adapters.SearchResultAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koopuluri on 3/2/15.
 */
public abstract class BaseSelectableListFragment extends ListFragment {
    public static final String TAG = "BSLFrag";

    BaseListAdapter mAdapter;
    List<Object> mList;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String mChildName;
    View mContainer;
    TextView mNoArticlesMessage;

    public BaseSelectableListFragment(String childName) {
        super();
        mChildName = childName;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mList = new ArrayList<>();
        if (mChildName.equals(ArticleListFragment.TAG)) {
            mAdapter = new ArticleAdapter(getActivity(), mList, this);
        } else if (mChildName.equals(SearchResultsFragment.TAG)) {
            mAdapter = new SearchResultAdapter(getActivity(), mList, this);
        } else {
            Log.e(TAG, "unkown child fragment.");
        }
        setListAdapter(mAdapter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // View view  = super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_article_list, container, false);
        mNoArticlesMessage = (TextView) view.findViewById(R.id.no_articles_message); // TODO: take this out of this base class
        mNoArticlesMessage.setVisibility(View.GONE);


        // wrapping fragment's contentView with SwipeRefreshLayout:
        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());
        // adding list fragment's content view to SwipeRefreshLayout, making sure it fills it up:
        mSwipeRefreshLayout.addView(view,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSwipeRefreshLayout.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        mContainer = container;
        return mSwipeRefreshLayout;
    }

    protected View getParentContainer() {
        return mContainer;
    }

    protected BaseListAdapter getAdapter() {
        return mAdapter;
    }

    protected List<Object> getItemList() {
        return mList;
    }

    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    protected TextView getNoMessageView() {
        return mNoArticlesMessage;
    }


    /**
     * Deletes both articles and notes that are selected.
     */
    protected abstract void deleteSelectedItems();



    /**
     * Sub-class of {@link android.support.v4.widget.SwipeRefreshLayout} for use in this
     * {@link android.support.v4.app.ListFragment}. The reason that this is needed is because
     * {@link android.support.v4.widget.SwipeRefreshLayout} only supports a single child, which it
     * expects to be the one which triggers refreshes. In our case the layout's child is the content
     * view returned from
     * {@link android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     * which is a {@link android.view.ViewGroup}.
     *
     * <p>To enable 'swipe-to-refresh' support via the {@link android.widget.ListView} we need to
     * override the default behavior and properly signal when a gesture is possible. This is done by
     * overriding {@link #canChildScrollUp()}.
     */
    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        /**
         * As mentioned above, we need to override this method to properly signal when a
         * 'swipe-to-refresh' is possible.
         *
         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
         */
        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            if (listView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(listView);
            } else {
                return false;
            }
        }
    }


    /**
     * Utility method to check whether a {@link ListView} can scroll up from it's current position.
     * Handles platform version differences, providing backwards compatible functionality where
     * needed.
     */
    private static boolean canListViewScrollUp(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // For ICS and above we can call canScrollVertically() to determine this
            return ViewCompat.canScrollVertically(listView, -1);
        } else {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            return listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
        }
    }



    /**
     * Code obtained from: http://developer.android.com/guide/topics/ui/menus.html#CAB.
     */
    private class ModeListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            if (checked) {
                mAdapter.addSelection(position);
            } else {
                mAdapter.removeSelection(position);
            }

            // notifiying change in views:
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    deleteSelectedItems();
                    Log.d(TAG, "menu clicked.");
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.list_menu_selection, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Here you can make any necessary updates to the activity when
            // the CAB is removed. By default, selected items are deselected/unchecked.
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }
    }

    protected ModeListener getModeListener() {
        return new ModeListener();
    }
}
