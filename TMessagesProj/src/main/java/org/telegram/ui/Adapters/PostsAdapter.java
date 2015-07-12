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
import android.view.View;
import android.view.ViewGroup;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.PostsController;
import org.telegram.android.support.widget.RecyclerView;
import org.telegram.messenger.object.PostObject;
import org.telegram.ui.Cells.PostCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.PostsActivity;
import org.telegram.utils.StringUtils;

// TODO-aragats
public class PostsAdapter extends RecyclerView.Adapter {

    //TODO find other way to get postActivity.
    private PostsActivity postsActivity;
    private Context mContext;
    //TODO ???
    private boolean serverOnly;
    private String openedPostId;
    private int currentCount;

    private class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
    }

    public PostsAdapter(Context context, boolean onlyFromServer, PostsActivity postsActivity) {
        mContext = context;
        serverOnly = onlyFromServer;
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
        int count = PostsController.getInstance().postObjects.size();
        if (count == 0 && PostsController.getInstance().loadingPosts) {
            return 0;
        }
//        if (!PostsController.getInstance().dialogsEndReached) {
//            count++;
//        }
        currentCount = count;
        return count;
    }

    public PostObject getItem(int i) {

        if (i < 0 || i >= PostsController.getInstance().postObjects.size()) {
            return null;
        }
        return PostsController.getInstance().postObjects.get(i);

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
            PostObject postObject;
            postObject = PostsController.getInstance().postObjects.get(i);
            if (AndroidUtilities.isTablet()) {
                cell.setPostSelected(!StringUtils.isEmpty(postObject.getId()) && postObject.getId().equals(openedPostId));
            }

            cell.setPostObject(postObject, i, serverOnly);

//TODO in new version. This in  onCreateViewHolder  method on ChatActivity
            //Set delegate to open photo
            ((PostCell) cell).setDelegate(new PostCell.PostCellDelegate() {
                @Override
                public void didClickedImage(PostCell cell) {
                    PostObject postObject = cell.getPostObject();
//                    mContext - is getParentActivity form Post Activity. look at instance creation of PostAdapter
                    PhotoViewer.getInstance().setParentActivity((Activity)mContext);
//                    PhotoViewer.getInstance().openPhoto(postObject, postsActivity);
                    PhotoViewer.getInstance().openPhotoNew(postObject, postsActivity, postsActivity);
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
        if (i == PostsController.getInstance().postObjects.size()) {
            return 1; //LoadingCell
        }
        return 0; //PostCell
    }
}
