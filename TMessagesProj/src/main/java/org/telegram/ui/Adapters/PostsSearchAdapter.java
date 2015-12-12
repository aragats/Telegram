/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.android.LocaleController;
import org.telegram.android.NotificationCenter;
import org.telegram.android.support.widget.RecyclerView;
import ru.aragats.wgo.rest.dto.Post;
import ru.aragats.wgo.rest.dto.PostResponse;
import ru.aragats.wgo.rest.mock.PostServiceMock;
import org.telegram.ui.Cells.GreySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import ru.aragats.wgo.R;

// TODO-aragats
public class PostsSearchAdapter extends BaseSearchAdapterRecycler {

    private Context mContext;
    //TODO important search timer!!!
    private Timer searchTimer;
    private ArrayList<Object> searchResult = new ArrayList<>();
    private ArrayList<String> searchResultHashtags = new ArrayList<>();
    //    private String lastSearchText;
    private long reqId = 0;
    private int lastReqId;
    private PostsActivitySearchAdapterDelegate delegate;
    private int needPostsSearch;
    private boolean postsSearchEndReached;
    private String lastPostsSearchString;
    private int lastSearchId = 0;

    private class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
    }


    public interface PostsActivitySearchAdapterDelegate {
        void searchStateChanged(boolean searching);
    }

    public PostsSearchAdapter(Context context, int messagesSearch) {
        mContext = context;
        needPostsSearch = messagesSearch;
    }

    public void setDelegate(PostsActivitySearchAdapterDelegate delegate) {
        this.delegate = delegate;
    }

    public boolean isPostsSearchEndReached() {
        return postsSearchEndReached; //TODO was it?
    }

    public void loadMoreSearchPosts() {
//        searchPostsInternal(lastPostsSearchString);
        searchPostsInternal(lastPostsSearchString, this.searchResult.size(), 20);
    }

    public String getLastSearchString() {
        return lastPostsSearchString;
    }


    private void searchPostsInternal(final String query, final int offset, final int count) {

        if (needPostsSearch == 0) {
            return;
        }
        lastPostsSearchString = query;
        //TODO move to controller ?
        PostResponse response = PostServiceMock.getPosts("location", query, offset, count);
        searchResult.addAll(response.getPosts());
        //TODO notify Activity to run postsAdapter.notifyDataSetChanged();
        if (!response.getPosts().isEmpty()) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.postsNeedReload);
        }


//        if (reqId != 0) {
//            ConnectionsManager.getInstance().cancelRpc(reqId, true);
//            reqId = 0;
//        }
        if (query == null || query.length() == 0) {
            lastReqId = 0;
            lastPostsSearchString = null;
//            notifyDataSetChanged();
            if (delegate != null) {
                delegate.searchStateChanged(false);
            }
            return;
        }
//        notifyDataSetChanged();
        if (delegate != null) {
            delegate.searchStateChanged(true);
        }


    }

    private void searchPostsInternal(final String query, final int searchId) {

    }


    private void searchPostsInternal(final String query) {

    }

    private void updateSearchResults(final ArrayList<Object> result, final ArrayList<CharSequence> names, final int searchId) {
        searchResult = result;
        notifyDataSetChanged();
    }

    public String getLastSearchText() {
        return lastPostsSearchString;
    }

    public boolean isGlobalSearch(int i) {
        return i > searchResult.size();
    }

    @Override
    public void clearRecentHashtags() {
        super.clearRecentHashtags();
        searchResultHashtags.clear();
        notifyDataSetChanged();
    }

    @Override
    protected void setHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
        super.setHashtags(arrayList, hashMap);
        for (HashtagObject hashtagObject : arrayList) {
            searchResultHashtags.add(hashtagObject.hashtag);
        }
        if (delegate != null) {
            delegate.searchStateChanged(false);
        }
        notifyDataSetChanged();
    }


    public void searchPosts(final String query) {
        if (query != null && lastPostsSearchString != null && query.equals(lastPostsSearchString)) {
            return;
        }

        // mock
        searchResult.clear();
        //TODO move to controller.
        PostResponse response = PostServiceMock.getPosts("location", query, 0, 20);
        searchResult.addAll(response.getPosts());
        //TODO notify Activity to run postsAdapter.notifyDataSetChanged();
        if (!response.getPosts().isEmpty()) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.postsNeedReload);
        }
        if (delegate != null) {
            delegate.searchStateChanged(false);
        }

        lastPostsSearchString = query;

        //TODO mock a
        if (true) {
            return;
        }


////        try {
////            if (searchTimer != null) {
////                searchTimer.cancel();
////            }
////        } catch (Exception e) {
////            FileLog.e("tmessages", e);
////        }
//        if (query == null || query.length() == 0) {
//            searchResult.clear();
//            searchPostsInternal(null);
////            queryServerSearch(null);
//            notifyDataSetChanged();
//        } else {
////            final int searchId = ++lastSearchId;
////            searchTimer = new Timer();
//            searchPostsInternal(query, 0);
////            queryServerSearch(query);
//            searchPostsInternal(query);
//
//        }
    }

//    public void searchPosts(final String query, final boolean serverOnly) {
//        if (query != null && lastSearchText != null && query.equals(lastSearchText)) {
//            return;
//        }
//        try {
//            if (searchTimer != null) {
//                searchTimer.cancel();
//            }
//        } catch (Exception e) {
//            FileLog.e("tmessages", e);
//        }
//        if (query == null || query.length() == 0) {
//            hashtagsLoadedFromDb = false;
//            searchResult.clear();
//            searchResultHashtags.clear();
//            if (needPostsSearch != 2) {
//                queryServerSearch(null);
//            }
//            searchPostsInternal(null);
//            notifyDataSetChanged();
//        } else {
//            if (query.startsWith("#") && query.length() == 1) {
//                postsSearchEndReached = true;
//                if (!hashtagsLoadedFromDb) {
//                    loadRecentHashtags();
//                    if (delegate != null) {
//                        delegate.searchStateChanged(true);
//                    }
//                    notifyDataSetChanged();
//                    return;
//                }
//                searchResultHashtags.clear();
//                for (HashtagObject hashtagObject : hashtags) {
//                    searchResultHashtags.add(hashtagObject.hashtag);
//                }
//                if (delegate != null) {
//                    delegate.searchStateChanged(false);
//                }
//                notifyDataSetChanged();
//                return;
//            } else {
//                searchResultHashtags.clear();
//            }
//            final int searchId = ++lastSearchId;
//            searchTimer = new Timer();
//            searchTimer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    try {
//                        searchTimer.cancel();
//                        searchTimer = null;
//                    } catch (Exception e) {
//                        FileLog.e("tmessages", e);
//                    }
//                    searchPostsInternal(query, serverOnly, searchId);
//                    AndroidUtilities.runOnUIThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (needPostsSearch != 2) {
//                                queryServerSearch(query);
//                            }
//                            searchPostsInternal(query);
//                        }
//                    });
//                }
//            }, 200, 300);
//        }
//    }

    @Override
    public int getItemCount() {
        if (!searchResultHashtags.isEmpty()) {
            return searchResultHashtags.size() + 1;
        }
        return searchResult.size();
    }

    public Object getItem(int i) {
        if (!searchResultHashtags.isEmpty()) {
            return searchResultHashtags.get(i - 1);
        }
        int localCount = searchResult.size();
        if (i >= 0 && i < localCount) {
            return searchResult.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        //TODO fix it. I use 0 and 2 for PostCell. 0 - was ProfileCell
        switch (viewType) {
            case 0:
                view = new PostCell(mContext);
//                view.setBackgroundResource(R.drawable.list_selector);
                break;
            case 1:
                view = new GreySectionCell(mContext);
                break;
            case 2:
                view = new PostCell(mContext);
                break;
            case 3:
                view = new LoadingCell(mContext);
                break;
            case 4:
                //TODO WAS ?
                view = new HashtagSearchCell(mContext);
                break;
        }
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //TODO
        // onCreateViewHolder befor that. we create crate and put item view of PostCell or other
        switch (holder.getItemViewType()) {
            case 0: {
//                PostCell cell = (PostCell) holder.itemView;
//                cell.useSeparator = (position != getItemCount() - 1);
//                PostObject post = (PostObject) searchResult.get(position);
                //TODO open ?? wie in PostsAdapter.
//                if (AndroidUtilities.isTablet()) {
//                    cell.setPostSelected(!StringUtils.isEmpty(post.getId()) && post.getId().equals(openedPostId));
//                }
//                cell.setPost(post, position, true);

                //Copied from 2.
                PostCell cell = (PostCell) holder.itemView;
                cell.useSeparator = (position != getItemCount() - 1);
                Post post = (Post) getItem(position);
                cell.setPost(post.getId(), post, post.getCreatedDate());
                break;
            }
            case 1: {
                GreySectionCell cell = (GreySectionCell) holder.itemView;
                if (!searchResultHashtags.isEmpty()) {
                    cell.setText(LocaleController.getString("Hashtags", R.string.Hashtags).toUpperCase());
                } else {
                    cell.setText(LocaleController.getString("SearchMessages", R.string.SearchMessages));
                }
                break;
            }
            case 2: {
                PostCell cell = (PostCell) holder.itemView;
                cell.useSeparator = (position != getItemCount() - 1);
                Post post = (Post) getItem(position);
                cell.setPost(post.getId(), post, post.getCreatedDate());
                break;
            }
            case 3: {
                break;
            }
            case 4: {
                HashtagSearchCell cell = (HashtagSearchCell) holder.itemView;
                cell.setText(searchResultHashtags.get(position - 1));
                cell.setNeedDivider(position != searchResultHashtags.size());
                break;
            }
        }
    }

    //    getItemViewType() without parameters in holder ?
    @Override
    public int getItemViewType(int i) {
        //TODO hashtag. delete probably
        if (!searchResultHashtags.isEmpty()) {
            return i == 0 ? 1 : 4;
        }
        int localCount = searchResult.size();
        if (i >= 0 && i < localCount) {
            return 0;
        }
        return 1;
    }
}
