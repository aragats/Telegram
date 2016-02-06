/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.PostsController;
import org.telegram.android.support.widget.RecyclerView;
import ru.aragats.wgo.dto.Post;
import org.telegram.ui.Cells.PostCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.LocationActivityAragats;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.PostsActivity;
import org.telegram.utils.StringUtils;

// TODO-aragats
public class PostsAdapter extends RecyclerView.Adapter {

    //TODO find other way to get postActivity.
    private PostsActivity postsActivity;
    private Context mContext;
    private String openedPostId;
    private int currentCount;

    private class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
    }

    public PostsAdapter(Context context, PostsActivity postsActivity) {
        mContext = context;
        this.postsActivity = postsActivity;
    }

    public void setOpenedPostId(String id) {
        openedPostId = id;
    }

    public boolean isDataSetChanged() {
        int current = currentCount;
        return current != getItemCount();
    }

    @Override
    public int getItemCount() {
        int count = PostsController.getInstance().posts.size();
        if (count == 0 && PostsController.getInstance().isLoadingPosts()) {
            return 0;
        }
//        if (!PostsController.getInstance().dialogsEndReached) {
//            count++;
//        }
        currentCount = count;
        return count;
    }

    public Post getItem(int i) {

        if (i < 0 || i >= PostsController.getInstance().posts.size()) {
            return null;
        }
        return PostsController.getInstance().posts.get(i);

    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == 0) {
            view = new PostCell(mContext);
        } else if (viewType == 1) {
            view = new LoadingCell(mContext);
        }
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder.getItemViewType() == 0) {
            PostCell cell = (PostCell) viewHolder.itemView;
            cell.useSeparator = (i != getItemCount() - 1);
            Post post = PostsController.getInstance().posts.get(i);
            if (AndroidUtilities.isTablet()) {
                cell.setPostSelected(!StringUtils.isEmpty(post.getId()) && post.getId().equals(openedPostId));
            }

            cell.setPostObject(post, i);

//TODO in new version. This in  onCreateViewHolder  method on ChatActivity
            //Set delegate to open photo
            ((PostCell) cell).setDelegate(new PostCell.PostCellDelegate() {
                @Override
                public void didClickedImage(PostCell cell) {
                    Post cellPost = cell.getPost();
//                    mContext - is getParentActivity form Post Activity. look at instance creation of PostAdapter
                    PhotoViewer.getInstance().setParentActivity((Activity) mContext);
//                    PhotoViewer.getInstance().openPhoto(post, postsActivity);
                    PhotoViewer.getInstance().openPhoto(cellPost, cell.getIndex(), postsActivity, postsActivity);
                }

                @Override
                public void didClickedVenue(PostCell cell) {
                    Post cellPost = cell.getPost();
                    LocationActivityAragats fragment = new LocationActivityAragats(new Bundle());
                    fragment.setPost(cellPost);
                    postsActivity.presentFragment(fragment);
                }

                @Override
                public void didPressUrl(String url) {
//                    if (url.startsWith("@")) {
//                        openProfileWithUsername(url.substring(1));
//                    } else if (url.startsWith("#")) {
//                        MessagesActivity fragment = new MessagesActivity(null);
//                        fragment.setSearchString(url);
//                        presentFragment(fragment);
//                    }
                }

                @Override
                public void didPressedOther(PostCell cell) {

                }

//                @Override
//                public void didPressedUserAvatar(PostCell cell, UserObject userObject) {
//
//                }

                @Override
                public void didPressedCancelSendButton(PostCell cell) {

                }

                @Override
                public void didLongPressed(PostCell cell) {

                }

                @Override
                public boolean canPerformActions() {
                    return postsActivity != null && postsActivity.getActionBar() != null && !postsActivity.getActionBar().isActionModeShowed();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int i) {
        if (i == PostsController.getInstance().posts.size()) {
            return 1; //LoadingCell
        }
        return 0; //PostCell
    }
}
