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
import android.widget.BaseAdapter;

import org.telegram.android.LocaleController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.EmptyCell;

import ru.aragats.wgo.R;

public class DrawerLayoutAdapter extends BaseAdapter {

    private Context mContext;

    public DrawerLayoutAdapter(Context context) {
        mContext = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return !(i == 0 || i == 1 || i == 4);
    }

    //TODO-aragats
    @Override
    public int getCount() {
        return UserConfig.isClientActivated() ? 10 : 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);
        if (type == 0) {
            if (view == null) {
                view = new DrawerProfileCell(mContext);
            }
//            ((DrawerProfileCell) view).setUser(PostsController.getInstance().getUser(UserConfig.getClientUserId()));
            ((DrawerProfileCell) view).setUser();
        } else if (type == 1) {
            if (view == null) {
                view = new EmptyCell(mContext, 8);
            }
        } else if (type == 2) {
            if (view == null) {
                view = new DividerCell(mContext);
            }
        } else if (type == 3) {
            if (view == null) {
                view = new DrawerActionCell(mContext);
            }
            DrawerActionCell actionCell = (DrawerActionCell) view;
            if (i == 2) {
                //TODO-aragats
//                actionCell.setTextAndIcon(LocaleController.getString("AragatsPosts", R.string.AragatsPosts), R.drawable.menu_broadcast);
                actionCell.setTextAndIcon(LocaleController.getString("Posts", R.string.Posts), R.drawable.menu_broadcast);
            } else if (i == 3) {
                actionCell.setTextAndIcon(LocaleController.getString("MyPosts", R.string.MyPosts), R.drawable.ic_attach_gallery);
            } else if (i == 5) {
                actionCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite);
            } else if (i == 6) {
                actionCell.setTextAndIcon(LocaleController.getString("AppFaq", R.string.AppFaq), R.drawable.menu_help);
            } else if (i == 8) {
                actionCell.setTextAndIcon(LocaleController.getString("VK Posts", R.string.VKPosts), R.drawable.menu_broadcast);
            } else if (i == 9) {
                actionCell.setTextAndIcon(LocaleController.getString("VK Photos", R.string.VKPhotos), R.drawable.menu_broadcast);
            }
        }

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return 1;
        } else if (i == 4 || i == 7) {
            return 2;
        }
        return 3;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return !UserConfig.isClientActivated();
    }
}
