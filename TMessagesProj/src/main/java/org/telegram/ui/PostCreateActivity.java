/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.AnimationCompat.AnimatorSetProxy;
import org.telegram.android.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.MediaController;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.android.PostsController;
import org.telegram.android.support.widget.LinearLayoutManager;
import org.telegram.android.support.widget.RecyclerView;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.dto.Coordinates;
import org.telegram.messenger.dto.Image;
import org.telegram.messenger.dto.Post;
import org.telegram.messenger.dto.Venue;
import org.telegram.messenger.service.mock.PostServiceMock;
import org.telegram.messenger.service.mock.VenueServiceMock;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.Cells.PostMediaCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PostCreateActivityEnterView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.WebFrameLayout;
import org.telegram.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

//TODO-aragats new
public class PostCreateActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, MessagesActivity.MessagesActivityDelegate,
        PostPhotoViewerProvider {

    private ArrayList<PostMediaCell> postMediaCellsCache = new ArrayList<>();

    private FrameLayout progressView;
    private FrameLayout bottomOverlay;
    //TODO aragats
    private PostCreateActivityEnterView postCreateActivityEnterView;
    private ActionBarMenuItem menuItem;
    private ActionBarMenuItem attachItem;
    private ActionBarMenuItem headerItem;
    private TextView addContactItem;
    //TODO list of. aragats
    private RecyclerListView postListView;
    private LinearLayoutManager postLayoutManager;
    //TODO-aragats
    private PostCreateActivityAdapter postCreateAdapter;
    private BackupImageView avatarImageView;
    private TextView bottomOverlayChatText;
    private FrameLayout bottomOverlayChat;
    private FrameLayout emptyViewContainer;
    private ArrayList<View> actionModeViews = new ArrayList<>();
    private TextView nameTextView;
    private TextView onlineTextView;
    private FrameLayout avatarContainer;
    private TextView bottomOverlayText;
    private TextView muteItem;


    // Send button!
    private TextView sendTextView;
    private FrameLayout sendButtonContainer;

    private Post selectedObject;
    private boolean wasPaused = false;
    private Runnable waitingForCharaterEnterRunnable;

    private boolean openAnimationEnded = false;


    //TODO - here are post for ???
    protected ArrayList<Post> posts = new ArrayList<>();
    protected Venue venue;

    private boolean loading = false;


    private String currentPicturePath;

    private Rect scrollRect = new Rect();

    private final static int done_button = 1;
    private final static int chat_menu_attach = 5;
    private final static int attach_photo = 6;
    private final static int attach_gallery = 7;
    private final static int attach_location = 10;
    private final static int mute = 14;

    private final static int id_chat_compose_panel = 1000;

    RecyclerListView.OnItemLongClickListener onItemLongClickListener = new RecyclerListView.OnItemLongClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (!actionBar.isActionModeShowed()) {
                createMenu(view, false);
            }
        }
    };

    RecyclerListView.OnItemClickListener onItemClickListener = new RecyclerListView.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (actionBar.isActionModeShowed()) {
                return;
            }
            createMenu(view, true);
        }
    };

    public PostCreateActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        //TODO-temp
        PostsController.getInstance().loadCurrentVenue("location");

//        if(0==0) {
//            return true;
//        }
        //

        //TODO notification
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceivedNewMessages);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesRead);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesDeleted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatInfoDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesReadEncrypted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.removeAllMessagesFromDialog);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioProgressDidChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidReset);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.screenshotTook);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.blockedUsersDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileNewChunkAvailable);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didCreatedNewDeleteTask);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidStarted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateMessageMedia);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.replaceMessagesObjects);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didLoadedReplyMessages);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceivedWebpages);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceivedWebpagesInUpdates);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesReadContent);

        super.onFragmentCreate();

        loading = true;

//        if (AndroidUtilities.isTablet()) {
//            NotificationCenter.getInstance().postNotificationName(NotificationCenter.openedChatChanged, dialog_id, false);
//        }

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (postCreateActivityEnterView != null) {
            postCreateActivityEnterView.onDestroy();
        }
        //TODO notification
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceivedNewMessages);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesRead);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesDeleted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesReadEncrypted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.removeAllMessagesFromDialog);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioProgressDidChanged);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidReset);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.screenshotTook);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.blockedUsersDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileNewChunkAvailable);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didCreatedNewDeleteTask);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidStarted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateMessageMedia);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.replaceMessagesObjects);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didLoadedReplyMessages);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceivedWebpages);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceivedWebpagesInUpdates);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesReadContent);


        if (!AndroidUtilities.isTablet() && getParentActivity() != null) {
            getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
        AndroidUtilities.unlockOrientation(getParentActivity());
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {

        for (int a = 0; a < 4; a++) {
            postMediaCellsCache.add(new PostMediaCell(context));
        }


        ResourceLoader.loadRecources(context);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                if (id == attach_photo || id == attach_gallery) {
//                    String action;
//                    if (id == attach_photo || id == attach_gallery) {
//                        action = "pm_upload_photo";
//                    } else {
//                        action = "pm_upload_document";
//                    }
//
//                    if (!MessagesController.isFeatureEnabled(action, PostCreateActivity.this)) {
//                        return;
//                    }
                }
                if (id == -1) {
                    if (postCreateActivityEnterView != null) {
                        postCreateActivityEnterView.hideEmojiPopup();
                    }
                    finishFragment();
                } else if (id == done_button) {
                    Toast.makeText(((Context) getParentActivity()), "DONE BUTTON is CLICKED", Toast.LENGTH_SHORT).show();

                } else if (id == attach_photo) {
                    attachPhotoHandle();
                } else if (id == attach_gallery) {
                    attachGalleryHandle();
                } else if (id == attach_location) {
                    openLocationChooser();
                }
            }
        });

//        sendButtonContainer = new FrameLayoutFixed(context);
//        sendButtonContainer.setBackgroundResource(R.drawable.bar_selector);
//        sendButtonContainer.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
//        actionBar.addView(sendButtonContainer, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.RIGHT, 56, 0, 40, 0));
//
//        sendTextView = new TextView(context);
//        sendTextView.setTextColor(0xffffffff);
//        sendTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//        sendTextView.setLines(1);
//        sendTextView.setMaxLines(1);
//        sendTextView.setSingleLine(true);
//        sendTextView.setEllipsize(TextUtils.TruncateAt.END);
//        sendTextView.setGravity(Gravity.LEFT);
//        sendTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4));
//        sendTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        sendTextView.setText("Send");
//        sendButtonContainer.addView(sendTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 54, 0, 0, 22));
//


        avatarContainer = new FrameLayoutFixed(context);
        avatarContainer.setBackgroundResource(R.drawable.bar_selector);
        avatarContainer.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
        actionBar.addView(avatarContainer, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 56, 0, 40, 0));
        avatarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationChooser();
//                Toast.makeText(((Context) getParentActivity()), "AVATAR CLICKED", Toast.LENGTH_SHORT).show();
            }
        });

//        avatarContainer.setEnabled(false);

        avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(21));
        avatarContainer.addView(avatarImageView, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 0, 3, 0, 0));


        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setText("Venue name");
        avatarContainer.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 54, 0, 0, 22));

        onlineTextView = new TextView(context);
        onlineTextView.setTextColor(0xffd7e8f7);
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(Gravity.LEFT);
        avatarContainer.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 54, 0, 0, 4));


        ActionBarMenu menu = actionBar.createMenu();

//        R.drawable.ic_send  - make it white icon then I can use it here. !!
//        R.drawable.ic_done
        menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

//        headerItem = menu.addItem(0, R.drawable.ic_ab_other);
//        //TODO mute temp
//        muteItem = headerItem.addSubItem(mute, null, 0);
//        ((LinearLayout.LayoutParams) headerItem.getLayoutParams()).setMargins(0, 0, AndroidUtilities.dp(-48), 0);
//
        updateTitle();
        updateSubtitle();
        updateTitleIcons();
//
//        attachItem = menu.addItem(chat_menu_attach, R.drawable.ic_ab_other);
//        attachItem.addSubItem(attach_photo, LocaleController.getString("ChatTakePhoto", R.string.ChatTakePhoto), R.drawable.ic_attach_photo);
//        attachItem.addSubItem(attach_gallery, LocaleController.getString("ChatGallery", R.string.ChatGallery), R.drawable.ic_attach_gallery);
//        attachItem.addSubItem(attach_location, LocaleController.getString("ChatLocation", R.string.ChatLocation), R.drawable.ic_attach_location);
//        attachItem.setVisibility(View.INVISIBLE);

        menuItem = menu.addItem(chat_menu_attach, R.drawable.ic_ab_attach);
        menuItem.addSubItem(attach_photo, LocaleController.getString("ChatTakePhoto", R.string.ChatTakePhoto), R.drawable.ic_attach_photo);
        menuItem.addSubItem(attach_gallery, LocaleController.getString("ChatGallery", R.string.ChatGallery), R.drawable.ic_attach_gallery);
        menuItem.addSubItem(attach_location, LocaleController.getString("ChatLocation", R.string.ChatLocation), R.drawable.ic_attach_location);
        menuItem.setShowFromBottom(true);
        menuItem.setBackgroundDrawable(null);

        actionModeViews.clear();

        final ActionBarMenu actionMode = actionBar.createActionMode();
        actionModeViews.add(actionMode.addItem(-2, R.drawable.ic_ab_back_grey, R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54)));

        checkActionBarMenu();

        fragmentView = new SizeNotifierFrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightMode = MeasureSpec.getMode(heightMeasureSpec);
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);
                heightSize -= getPaddingBottom();

                int inputFieldHeight = 0;

                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == postCreateActivityEnterView) {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        inputFieldHeight = child.getMeasuredHeight();
                        break;
                    }
                }
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() == GONE || child == postCreateActivityEnterView) {
                        continue;
                    }

                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    if (child == postListView) {
                        int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                        int contentHeightSpec = MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), heightSize - inputFieldHeight + AndroidUtilities.dp(2)), MeasureSpec.EXACTLY);
                        child.measure(contentWidthSpec, contentHeightSpec);
                    } else {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                final int count = getChildCount();

                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        continue;
                    }
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.LEFT;
                    }

                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = r - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = lp.topMargin;
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = ((b - getPaddingBottom()) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = ((b - getPaddingBottom()) - t) - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                    }

                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }

                notifyHeightChanged();
            }
        };


        SizeNotifierFrameLayout contentView = (SizeNotifierFrameLayout) fragmentView;

        contentView.setBackgroundImage(ApplicationLoader.getCachedWallpaper());

        emptyViewContainer = new FrameLayout(context);
        emptyViewContainer.setPadding(0, 0, 0, AndroidUtilities.dp(48));
        emptyViewContainer.setVisibility(View.INVISIBLE);
        contentView.addView(emptyViewContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        emptyViewContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        TextView emptyView = new TextView(context);
        emptyView.setText(LocaleController.getString("NoMessages", R.string.NoMessages));
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(0xffffffff);
        emptyView.setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_black : R.drawable.system_blue);
        emptyView.setPadding(AndroidUtilities.dp(7), AndroidUtilities.dp(1), AndroidUtilities.dp(7), AndroidUtilities.dp(1));
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachGalleryHandle();
            }
        });

        emptyView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return attachPhotoHandle();
            }
        });

        emptyViewContainer.addView(emptyView, new FrameLayout.LayoutParams(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));


        if (postCreateActivityEnterView != null) {
            postCreateActivityEnterView.onDestroy();
        }

        postListView = new RecyclerListView(context);
        postListView.setVerticalScrollBarEnabled(true);
        postListView.setAdapter(postCreateAdapter = new PostCreateActivityAdapter(context));
        postListView.setClipToPadding(false);
        postListView.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(3));
        postListView.setItemAnimator(null);
        postListView.setLayoutAnimation(null);
        postLayoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        postLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        postLayoutManager.setStackFromEnd(true);
        postListView.setLayoutManager(postLayoutManager);
        contentView.addView(postListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        postListView.setOnItemLongClickListener(onItemLongClickListener);
        postListView.setOnItemClickListener(onItemClickListener);
        postListView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //TODO do i need it ?
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) {
                    updateVisibleRows();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = postLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(postLayoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                if (visibleItemCount > 0) {
                    int totalItemCount = postCreateAdapter.getItemCount();

                }
            }
        });
        postListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        postListView.setOnInterceptTouchListener(new RecyclerListView.OnInterceptTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                if (actionBar.isActionModeShowed()) {
                    return false;
                }
                return false;
            }
        });

        progressView = new FrameLayout(context);
        progressView.setVisibility(View.INVISIBLE);
        contentView.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 0, 0, 0, 48));

        View view = new View(context);
        view.setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_loader2 : R.drawable.system_loader1);
        progressView.addView(view, LayoutHelper.createFrame(36, 36, Gravity.CENTER));

        ProgressBar progressBar = new ProgressBar(context);
        try {
            progressBar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.loading_animation));
        } catch (Exception e) {
            //don't promt
        }
        progressBar.setIndeterminate(true);
        AndroidUtilities.setProgressBarAnimationDuration(progressBar, 1500);
        progressView.addView(progressBar, LayoutHelper.createFrame(32, 32, Gravity.CENTER));

        postCreateActivityEnterView = new PostCreateActivityEnterView(getParentActivity(), contentView, this, true);
//        postCreateActivityEnterView.setDialogId(dialog_id);
        postCreateActivityEnterView.addToAttachLayout(menuItem);
        postCreateActivityEnterView.setId(id_chat_compose_panel);
        contentView.addView(postCreateActivityEnterView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));
        postCreateActivityEnterView.setDelegate(new PostCreateActivityEnterView.PostCreateActivityEnterViewDelegate() {
            @Override
            public void onMessageSend(String message) {
//                moveScrollToLastMessage();
            }

            @Override
            public void onTextChanged(final CharSequence text, boolean bigChange) {
//                if (mentionsAdapter != null) {
//                    mentionsAdapter.searchUsernameOrHashtag(text.toString(), postCreateActivityEnterView.getCursorPosition(), messages);
//                }
                if (waitingForCharaterEnterRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(waitingForCharaterEnterRunnable);
                    waitingForCharaterEnterRunnable = null;
                }
                if (postCreateActivityEnterView.isMessageWebPageSearchEnabled()) {
                    if (bigChange) {
//                        searchLinks(text, true);
                    } else {
                        waitingForCharaterEnterRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (this == waitingForCharaterEnterRunnable) {
//                                    searchLinks(text, false);
                                    waitingForCharaterEnterRunnable = null;
                                }
                            }
                        };
                        AndroidUtilities.runOnUIThread(waitingForCharaterEnterRunnable, 3000);
                    }
                }
            }

            @Override
            public void needSendTyping() {
            }

            @Override
            public void onAttachButtonHidden() {
                if (attachItem != null) {
                    attachItem.setVisibility(View.VISIBLE);
                }
                if (headerItem != null) {
                    headerItem.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAttachButtonShow() {
                if (attachItem != null) {
                    attachItem.setVisibility(View.INVISIBLE);
                }
                if (headerItem != null) {
                    headerItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onWindowSizeChanged(int size) {
                if (size < AndroidUtilities.dp(72) + AndroidUtilities.getCurrentActionBarHeight()) {
                } else {
                }
//                updateMessagesVisisblePart();
            }
        });

        //TODO - I can use it to delete add photo  and location.
//        FrameLayout replyLayout = new FrameLayout(context);
//        replyLayout.setClickable(true);
//        postCreateActivityEnterView.addTopView(replyLayout, AndroidUtilities.dp(48));

//        View lineView = new View(context);
//        lineView.setBackgroundColor(0xffe8e8e8);
//        replyLayout.addView(lineView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM | Gravity.LEFT));

//        TODO allow stickers.
//        postCreateActivityEnterView.setAllowStickers(true);


        bottomOverlay = new FrameLayout(context);
        bottomOverlay.setBackgroundColor(0xffffffff);
        bottomOverlay.setVisibility(View.INVISIBLE);
        bottomOverlay.setFocusable(true);
        bottomOverlay.setFocusableInTouchMode(true);
        bottomOverlay.setClickable(true);
        contentView.addView(bottomOverlay, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM));

        bottomOverlayText = new TextView(context);
        bottomOverlayText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        bottomOverlayText.setTextColor(0xff7f7f7f);
        bottomOverlay.addView(bottomOverlayText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        bottomOverlayChat = new FrameLayout(context);
        bottomOverlayChat.setBackgroundColor(0xfffbfcfd);
        bottomOverlayChat.setVisibility(View.INVISIBLE);
        contentView.addView(bottomOverlayChat, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM));
        bottomOverlayChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.getString("AreYouSureUnblockContact", R.string.AreYouSureUnblockContact));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                    MessagesController.getInstance().unblockUser(currentUser.id);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });

        bottomOverlayChatText = new TextView(context);
        bottomOverlayChatText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        bottomOverlayChatText.setTextColor(0xff3e6fa1);
        bottomOverlayChat.addView(bottomOverlayChatText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

//        if (loading && posts.isEmpty()) {
//            progressView.setVisibility(View.VISIBLE);
//            postListView.setEmptyView(null);
//        } else {
        progressView.setVisibility(View.INVISIBLE);
        postListView.setEmptyView(emptyViewContainer);
//        }

        updateBottomOverlay();

        return fragmentView;
    }

    private boolean searchForHttpInText(CharSequence string) {
        int len = string.length();
        int seqLen = 0;
        for (int a = 0; a < len; a++) {
            char ch = string.charAt(a);
            if (seqLen == 0 && (ch == 'h' || ch == 'H')) {
                seqLen++;
            } else if ((seqLen == 1 || seqLen == 2) && (ch == 't' || ch == 'T')) {
                seqLen++;
            } else if (seqLen == 3 && (ch == 'p' || ch == 'P')) {
                seqLen++;
            } else if (seqLen == 4 && (ch == 's' || ch == 'S')) {
                seqLen++;
            } else if ((seqLen == 4 || seqLen == 5) && ch == ':') {
                seqLen++;
            } else if ((seqLen == 5 || seqLen == 6 || seqLen == 7) && ch == '/') {
                if (seqLen == 6 || seqLen == 7) {
                    return true;
                }
                seqLen++;
            } else if (seqLen != 0) {
                seqLen = 0;
            }
        }
        return false;
    }

    //TODO-future
//    private void searchLinks(CharSequence charSequence, boolean force) {
//    }

    private void moveScrollToLastPost() {
//        if (postListView != null) {
//            postLayoutManager.scrollToPositionWithOffset(posts.size() - 1, -100000 - postListView.getPaddingTop());
//        }
    }


    private void checkActionBarMenu() {

        if (menuItem != null) {
            menuItem.setVisibility(View.VISIBLE);
        }
//        if (timeItem != null) {
//            timeItem.setVisibility(View.VISIBLE);
//        }
//        if (timeItem2 != null) {
//            timeItem2.setVisibility(View.VISIBLE);
//        }

        checkAndUpdateAvatar();
    }


    private int getPostType(Post post) {
        if (post == null) {
            return -1;
        } else {
            return -1;
        }

    }


    private void updateActionModeTitle() {
        if (!actionBar.isActionModeShowed()) {
            return;
        }
//        if (!selectedMessagesIds.isEmpty()) {
//            selectedMessagesCountTextView.setText(String.format("%d", selectedMessagesIds.size()));
//        }
    }

    private void updateTitle() {
        if (nameTextView == null) {
            return;
        }
        String name = "Current location";
        if (venue != null) {
            if (!StringUtils.isEmpty(venue.getName())) {
                name = venue.getName();
            }
            nameTextView.setText(name);
        }
    }

    private void updateTitleIcons() {
        int leftIcon = 0;
//        int rightIcon = MessagesController.getInstance().isDialogMuted(dialog_id) ? R.drawable.mute_fixed : 0;
        int rightIcon = 0;
        nameTextView.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, rightIcon, 0);

        if (muteItem != null) {
            if (rightIcon != 0) {
                muteItem.setText(LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications));
            } else {
                muteItem.setText(LocaleController.getString("MuteNotifications", R.string.MuteNotifications));
            }
        }
    }

    private void updateSubtitle() {
        if (onlineTextView == null) {
            return;
        }
//        CharSequence addressString = "printing";
        CharSequence addressString = "address";
        if (venue != null && !StringUtils.isEmpty(venue.getAddress())) {
            addressString = venue.getAddress();
        }
        addressString = TextUtils.replace(addressString, new String[]{"..."}, new String[]{""});
        onlineTextView.setText(addressString);
    }

    private void checkAndUpdateAvatar() {
//        ImageView imageView = new ImageView(this.getParentActivity());
//        imageView.setImageResource(R.drawable.pin);


        if (avatarImageView != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
//            avatarImageView.setImage(newPhoto, "50_50", avatarDrawable);
//            avatarImageView.setImage("/storage/emulated/0/Telegram/Telegram Images/730111210_6623.jpg", "50_50", avatarDrawable);
            //TODO probably save venue. into field in Activity.
            if (venue != null && venue.getPreviewImage() != null && !StringUtils.isEmpty(venue.getPreviewImage().getUrl())) {
                avatarImageView.setImage(venue.getPreviewImage().getUrl(), "50_50", avatarDrawable);
            } else {
                avatarImageView.setImageResource(R.drawable.pin);
//                    avatarImageView.setImage(PostsController.getInstance().getCurrentVenue().getVenuePreviewImageUrl(), "50_50", avatarDrawable);
            }

        }
    }

    //TODO-aragats handle response from camera.
    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0) {
                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                final ArrayList<Object> arrayList = new ArrayList<>();
                int orientation = 0;
                try {
                    ExifInterface ei = new ExifInterface(currentPicturePath);
                    int exif = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    switch (exif) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            orientation = 90;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            orientation = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            orientation = 270;
                            break;
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
                arrayList.add(new MediaController.PhotoEntry(0, 0, 0, currentPicturePath, orientation, false));

                //TODO-aragats
                PhotoViewer.getInstance().openPhotoForSelect(arrayList, 0, 2, new PhotoViewer.EmptyPhotoViewerProvider() {
                    @Override
                    public void sendButtonPressed(int index) {
                        MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) arrayList.get(0);
                        //TODO works the second if. what is the difference. Probably I should delete on of them
                        if (photoEntry.imagePath != null) {
//                            SendMessagesHelper.prepareSendingPhoto(photoEntry.imagePath, null, dialog_id, replyingMessageObject, photoEntry.caption);
                            ArrayList<String> photos = new ArrayList<String>();
                            photos.add(photoEntry.imagePath);
                            didSelectPhotos(photos);

                        } else if (photoEntry.path != null) {
//                            SendMessagesHelper.prepareSendingPhoto(photoEntry.path, null, dialog_id, replyingMessageObject, photoEntry.caption);
                            ArrayList<String> photos = new ArrayList<String>();
                            photos.add(photoEntry.path);
                            didSelectPhotos(photos);
                        }
                    }
                }, null);
                AndroidUtilities.addMediaToGallery(currentPicturePath);
                currentPicturePath = null;
            }
        }
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (currentPicturePath != null) {
            args.putString("path", currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        currentPicturePath = args.getString("path");
    }


    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, final Object... args) {
        //TODO look at original implementation
        if (id == NotificationCenter.didReceivedNewMessages) {
            // add new item and then notifyDataSetChanged
            if (this.postCreateAdapter != null) {
                postCreateAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onOpenAnimationStart() {
        NotificationCenter.getInstance().setAnimationInProgress(true);
    }

    @Override
    protected void onOpenAnimationEnd() {
        NotificationCenter.getInstance().setAnimationInProgress(false);
        openAnimationEnded = true;
        int count = postListView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = postListView.getChildAt(a);
//            if (view instanceof ChatMediaCell) {
//                ChatMediaCell cell = (ChatMediaCell) view;
//                cell.setAllowedToSetPhoto(true);
//            }
        }

    }

    private void updateBottomOverlay() {
        if (bottomOverlayChatText != null) {
            bottomOverlayChatText.setText(LocaleController.getString("DeleteThisChat", R.string.DeleteThisChat));
        }

        if (bottomOverlayChat != null) {
            bottomOverlayChat.setVisibility(View.INVISIBLE);
        }
        if (muteItem != null) {
            muteItem.setVisibility(View.VISIBLE);
        }
//        postCreateActivityEnterView.setFieldFocused(false);

    }


    @Override
    public void onResume() {
        super.onResume();

        if (!AndroidUtilities.isTablet()) {
            getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        checkActionBarMenu();
//        moveScrollToLastMessage();

        if (wasPaused) {
            wasPaused = false;
            if (postCreateAdapter != null) {
                postCreateAdapter.notifyDataSetChanged();
            }
        }


        fixLayout(true);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        String lastMessageText = preferences.getString("new_post_text", null);
        if (lastMessageText != null) {
            preferences.edit().remove("new_post_text").commit();
            postCreateActivityEnterView.setFieldText(lastMessageText);
        }
        String lastPhotoURL = preferences.getString("new_post_photo", null);
        if (!StringUtils.isEmpty(lastPhotoURL) && posts.isEmpty()) {
            preferences.edit().remove("new_post_photo").commit();
            ArrayList<String> photos = new ArrayList<>();
            photos.add(lastPhotoURL);
            didSelectPhotos(photos);
        }
        venue = PostsController.getInstance().getCurrentVenue();
        PostsController.getInstance().setCurrentVenue(null);
        updateVenue();


        if (bottomOverlayChat.getVisibility() != View.VISIBLE) {
            postCreateActivityEnterView.setFieldFocused(true);
        }

        postListView.setOnItemLongClickListener(onItemLongClickListener);
        postListView.setOnItemClickListener(onItemClickListener);
        postListView.setLongClickable(true);
    }

    @Override
    public void onBeginSlide() {
        super.onBeginSlide();
        postCreateActivityEnterView.hideEmojiPopup();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (menuItem != null) {
            menuItem.closeSubMenu();
        }
        wasPaused = true;
        if (postCreateActivityEnterView != null) {
            postCreateActivityEnterView.hideEmojiPopup();
            String text = postCreateActivityEnterView.getFieldText();
            if (text != null || !posts.isEmpty()) {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                //TODO save text before pause.
                if (text != null) {
                    editor.putString("new_post_text", text);
                }
                if (!posts.isEmpty()) {
                    editor.putString("new_post_photo", posts.get(0).getImage().getUrl());
                }
                editor.commit();
                PostsController.getInstance().setCurrentVenue(venue);
            }
            postCreateActivityEnterView.setFieldFocused(false);
        }
//        MessagesController.getInstance().cancelTyping(0, dialog_id);

    }

    //TODO - detect screenshot
    private void updateInformationForScreenshotDetector() {

    }

    private void fixLayout(final boolean resume) {
        if (avatarContainer != null) {
            avatarContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (avatarContainer != null) {
                        avatarContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    if (getParentActivity() == null) {
                        return false;
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (AndroidUtilities.isSmallTablet() && getParentActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
                        } else {
                            actionBar.setBackButtonImage(R.drawable.ic_close_white);
                        }
                    }
                    int padding = (AndroidUtilities.getCurrentActionBarHeight() - AndroidUtilities.dp(48)) / 2;
                    avatarContainer.setPadding(avatarContainer.getPaddingLeft(), padding, avatarContainer.getPaddingRight(), padding);
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) avatarContainer.getLayoutParams();
                    layoutParams.topMargin = (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
                    avatarContainer.setLayoutParams(layoutParams);
                    return false;
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        fixLayout(false);
    }

    public void createMenu(View v, boolean single) {
        //TODO MOCK
        if (true) {
            return;
        }
        if (actionBar.isActionModeShowed()) {
            return;
        }

        Post post = null;
        if (v instanceof PostMediaCell) {
            post = ((PostMediaCell) v).getPost();
        }
        if (post == null) {
            return;
        }
        int type = getPostType(post);

        type = 1;
        selectedObject = null;
        actionBar.hideActionMode();

//        if (single || type < 2 || type == 20) {
//            if (type >= 0) {
        selectedObject = post;
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

        CharSequence[] items = null;
        int[] options = null;

        if (type == 0) {
            items = new CharSequence[]{LocaleController.getString("Retry", R.string.Retry), LocaleController.getString("Delete", R.string.Delete)};
            options = new int[]{0, 1};
        } else if (type == 1) {
            items = new CharSequence[]{LocaleController.getString("Delete", R.string.Delete)};
            options = new int[]{1};
        } else if (type == 20) {
            items = new CharSequence[]{LocaleController.getString("Retry", R.string.Retry), LocaleController.getString("Copy", R.string.Copy), LocaleController.getString("Delete", R.string.Delete)};
            options = new int[]{0, 3, 1};
        }

        final int[] finalOptions = options;
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (finalOptions == null || selectedObject == null || i < 0 || i >= finalOptions.length) {
                    return;
                }
                processSelectedOption(finalOptions[i]);
            }
        });

        builder.setTitle(LocaleController.getString("Message", R.string.Message));
        showDialog(builder.create());
//            }
//            return;
//        }
        actionBar.showActionMode();

        if (Build.VERSION.SDK_INT >= 11) {
            AnimatorSetProxy animatorSet = new AnimatorSetProxy();
            ArrayList<Object> animators = new ArrayList<>();
            for (int a = 0; a < actionModeViews.size(); a++) {
                View view = actionModeViews.get(a);
                AndroidUtilities.clearDrawableAnimation(view);
                if (a < 1) {
                    animators.add(ObjectAnimatorProxy.ofFloat(view, "translationX", -AndroidUtilities.dp(56), 0));
                } else {
                    animators.add(ObjectAnimatorProxy.ofFloat(view, "scaleY", 0.1f, 1.0f));
                }
            }
            animatorSet.playTogether(animators);
            animatorSet.setDuration(250);
            animatorSet.start();
        }

        updateActionModeTitle();
        updateVisibleRows();
    }

    private void processSelectedOption(int option) {
        if (selectedObject == null) {
            return;
        }
        if (option == 1) {
            final Post finalSelectedObject = selectedObject;
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setMessage(LocaleController.formatString("AreYouSureDeleteMessages", R.string.AreYouSureDeleteMessages, LocaleController.formatPluralString("messages", 1)));
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ArrayList<String> ids = new ArrayList<>();
                    ids.add(finalSelectedObject.getId());
                    ArrayList<Long> random_ids = null;
//                    MessagesController.getInstance().deleteMessages(ids, random_ids, currentEncryptedChat);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
            //copy text
        }
        selectedObject = null;
    }

    @Override
    public void didSelectDialog(MessagesActivity activity, long did, boolean param) {
        return;
    }

    @Override
    public boolean onBackPressed() {
        if (actionBar.isActionModeShowed()) {
//            selectedMessagesIds.clear();
//            selectedMessagesCanCopyIds.clear();
            actionBar.hideActionMode();
            updateVisibleRows();
            return false;
        } else if (postCreateActivityEnterView.isEmojiPopupShowing()) {
            postCreateActivityEnterView.hideEmojiPopup();
            return false;
        }
        return true;
    }

    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = ApplicationLoader.applicationContext.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            if (getParentActivity() == null) {
                return false;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setMessage("Install Google Maps?");
            builder.setCancelable(true);
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
                        getParentActivity().startActivityForResult(intent, 500);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
            return false;
        }
    }

    private void updateVisibleRows() {
        if (postListView == null) {
            return;
        }
        int count = postListView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = postListView.getChildAt(a);
            if (view instanceof PostMediaCell) {
                PostMediaCell cell = (PostMediaCell) view;

                boolean disableSelection = false;
                boolean selected = false;
                if (actionBar.isActionModeShowed()) {
//                    if (selectedMessagesIds.containsKey(cell.getMessageObject().getId())) {
//                        view.setBackgroundColor(0x6633b5e5);
//                        selected = true;
//                    } else {
//                        view.setBackgroundColor(0);
//                    }
                    disableSelection = true;
                } else {
                    view.setBackgroundColor(0);
                }

//                cell.setMessageObject(cell.getMessageObject());
//                cell.setCheckPressed(!disableSelection, disableSelection && selected);
//                cell.setHighlighted(highlightMessageId != Integer.MAX_VALUE && cell.getMessageObject() != null && cell.getMessageObject().getId() == highlightMessageId);
            }
        }
    }


    @Override
    public void updatePhotoAtIndex(int index) {

    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(Post postObject) {
        if (postObject == null) {
            return null;
        }
        int count = postListView.getChildCount();

        for (int a = 0; a < count; a++) {
            Post postToOpen = null;
            ImageReceiver imageReceiver = null;
            View view = postListView.getChildAt(a);
            if (view instanceof PostMediaCell) {
                PostMediaCell cell = (PostMediaCell) view;
                Post post = cell.getPost();
                if (post != null && !StringUtils.isEmpty(post.getId())
                        && post.getId().equals(postObject.getId())) {
                    postToOpen = post;
                    imageReceiver = cell.getPhotoImage();
                }
            }

            if (postToOpen != null) {
                int coords[] = new int[2];
                view.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                object.parentView = postListView;
                object.imageReceiver = imageReceiver;
                object.thumb = imageReceiver.getBitmap();
                object.radius = imageReceiver.getRoundRadius();
                return object;
            }
        }
        return null;
    }

    @Override
    public Bitmap getThumbForPhoto(Post post, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(Post post) {
    }

    @Override
    public void willHidePhotoViewer() {
    }

    @Override
    public boolean isPhotoChecked(int index) {
        return false;
    }

    @Override
    public void setPhotoChecked(int index) {
    }

    @Override
    public void cancelButtonPressed() {
    }

    @Override
    public void sendButtonPressed(int index) {
    }

    @Override
    public int getSelectedCount() {
        return 0;
    }

    public class PostCreateActivityAdapter extends RecyclerView.Adapter {

        private Context mContext;

        public PostCreateActivityAdapter(Context context) {
            mContext = context;
        }

        private class Holder extends RecyclerView.ViewHolder {

            public Holder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        @Override
        public long getItemId(int i) {
            return RecyclerListView.NO_ID;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == 1) {
                if (!postMediaCellsCache.isEmpty()) {
                    view = postMediaCellsCache.get(0);
                    postMediaCellsCache.remove(0);
                } else {
                    view = new PostMediaCell(mContext);
                }
            } else if (viewType == 5) {
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.chat_loading_layout, parent, false);
                view.findViewById(R.id.progressLayout).setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_loader2 : R.drawable.system_loader1);
            } else if (viewType == 6) {
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.chat_unread_layout, parent, false);
            }

            if (view instanceof PostMediaCell) {
                ((PostMediaCell) view).setDelegate(new PostMediaCell.PostMediaCellDelegate() {
                    @Override
                    public void didPressedUserAvatar(PostMediaCell cell) {

                    }

                    @Override
                    public void didPressedCancelSendButton(PostMediaCell cell) {
                        Post post = cell.getPost();
                        posts.remove(post);
                        posts.clear();
                        postCreateAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void didLongPressed(PostMediaCell cell) {
                        createMenu(cell, false);
                    }

                    @Override
                    public boolean canPerformActions() {
                        return actionBar != null && !actionBar.isActionModeShowed();
                    }

                    @Override
                    public void didPressUrl(String url) {
                        if (url.startsWith("@")) {
                            MessagesController.openByUserName(url.substring(1), PostCreateActivity.this, 0);
                        } else if (url.startsWith("#")) {
                            MessagesActivity fragment = new MessagesActivity(null);
                            fragment.setSearchString(url);
                            presentFragment(fragment);
                        }
                    }

                    @Override
                    public void needOpenWebView(String url, String title, String originalUrl, int w, int h) {
                        BottomSheet.Builder builder = new BottomSheet.Builder(mContext);
                        builder.setCustomView(new WebFrameLayout(mContext, builder.create(), title, originalUrl, url, w, h));
                        builder.setOverrideTabletWidth(true);
                        showDialog(builder.create());
                    }

                    @Override
                    public void didPressReplyMessage(PostMediaCell cell, int id) {
//                        scrollToMessageId(id, cell.getMessageObject().getId(), true);
                    }

                    //TODO Now I do not use it in PostMedia, but I can. Look at PostMedia.didClickedImage
                    @Override
                    public void didClickedImage(PostMediaCell cell) {
                        Post post = cell.getPost();

                        PhotoViewer.getInstance().setParentActivity(getParentActivity());
                        PhotoViewer.getInstance().openPhoto(post, PostCreateActivity.this);

                        //TODO open location view
//                                LocationActivity fragment = new LocationActivity();
//                                fragment.setMessageObject(post);
//                                presentFragment(fragment);

                    }

                    @Override
                    public void didPressedOther(PostMediaCell cell) {
                        createMenu(cell, true);
                    }
                });
                ((PostMediaCell) view).setAllowedToSetPhoto(openAnimationEnded);


            }

            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = holder.getItemViewType();
            if (viewType == 5) {
                holder.itemView.findViewById(R.id.progressLayout).setVisibility(2 > 1 ? View.VISIBLE : View.INVISIBLE);
                return;
            }

//            PostObject post = posts.get(posts.size() - position - (!endReached ? 0 : 1));
            Post post = posts.get(position);
            View view = holder.itemView;

//            int type = post.contentType;
            int type = 1;

            boolean selected = false;
            boolean disableSelection = false;
            if (actionBar.isActionModeShowed()) {
                view.setBackgroundColor(0);
                disableSelection = true;
            } else {
                view.setBackgroundColor(0);
            }


            if (view instanceof PostMediaCell) {
                PostMediaCell cell = (PostMediaCell) view;
                cell.setPost(post);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
//            int offset = 1;
//            if (posts.size() != 0) {
//                offset = 0;
//                if (position == 0) {
//                    return 5;
//                }
//            }
//            if (position == (posts.size() + 1 - offset)) {
//                return 5;
//            }
//            PostObject post = posts.get(posts.size() - position - offset);
////            return post.contentType;
//            return 1;
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        }

        public void updateRowWithPost(Post post) {
            int index = posts.indexOf(post);
            if (index == -1) {
                return;
            }
//            notifyItemChanged(posts.size() - (!endReached ? 0 : 1) - index);
            notifyItemChanged(posts.size() - index);
        }

        public void removePost(Post post) {
            int index = posts.indexOf(post);
            if (index == -1) {
                return;
            }
            posts.remove(index);
//            notifyItemRemoved(posts.size() - (!endReached ? 0 : 1) - index);
            notifyItemRemoved(posts.size() - index);
        }
    }


    private void openLocationChooser() {
        if (!isGoogleMapsInstalled()) {
            return;
        }
        LocationActivityAragats fragment = new LocationActivityAragats();
        fragment.setDelegate(new LocationActivityAragats.LocationActivityDelegate() {
            @Override
            public void didSelectLocation(TLRPC.MessageMedia location) {
                Venue venue = new Venue();
                Coordinates coordinates = new Coordinates();
                coordinates.setCoordinates(Arrays.asList(location.geo._long, location.geo.lat));
                coordinates.setType("Point");
                venue.setCoordinates(coordinates);
                venue.setFoursquareId(location.venue_id);
                Image image = new Image();
                image.setUrl(location.iconUrl);
                venue.setImage(image);
                venue.setPreviewImage(image);
                venue.setName(location.title);
                venue.setAddress(location.address);
                if (StringUtils.isEmpty(venue.getAddress())) {
                    venue.setAddress(location.geo._long + ", " + location.geo.lat);
                }
                PostCreateActivity.this.venue = venue;
                PostsController.getInstance().setCurrentVenue(venue);
//                location.iconUrl;
//                Toast.makeText(getParentActivity(), location.venue_id + " " + location.geo.lat + " " + location.geo._long, Toast.LENGTH_LONG).show();
//                            SendMessagesHelper.getInstance().sendMessage(location, dialog_id, replyingMessageObject);
//                            moveScrollToLastMessage();
//                            showReplyPanel(false, null, null, null, false, true);
//                            if (paused) {
//                                scrollToTopOnResume = true;
//                            }
                updateVenue();
            }
        });
        presentFragment(fragment);
    }


    private void didSelectPhotos(ArrayList<String> photos) {
        //TODO set Photo after selecting it.
//                                SendMessagesHelper.prepareSendingPhotos(photos, null, dialog_id);

        //TODO get image width and height
//                                BufferedImage bimg = ImageIO.read(new File(photos.get(0)));
//                                int width          = bimg.getWidth();
//                                int height         = bimg.getHeight();

        if (photos != null && !photos.isEmpty()) {
            //
            String photoUrl = photos.get(0);
            if (StringUtils.isEmpty(photoUrl)) {
                return;
            }
            File file = new File(photoUrl);
            if (!file.exists()) {
                return;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            // TODO THIS Do not allow decode the file.
//            options.inJustDecodeBounds = true;

//Returns null, sizes are in the options variable
            Bitmap bitmap = BitmapFactory.decodeFile(photoUrl, options);
            int width = options.outWidth;
            int height = options.outHeight;
//If you want, the MIME type will also be decoded (if possible)
            String type = options.outMimeType;


            //
            bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);


            Post post = new Post();
            post.setId(PostServiceMock.generateString("1234567890", 5));
            Image image = new Image();
            image.setUrl(photoUrl);
            image.setWidth(width);
            image.setHeight(height);
            image.setBitmap(bitmap);
//            image = ImageServiceMock.getRandomImage();
            post.setImage(image);
            post.setPreviewImage(image);
            post.setVenue(VenueServiceMock.getRandomVenue());
            //TODO-temp
//            PostCreateActivity.this.post = new PostObject(post);
            //DELETE ALL to store only one
            PostCreateActivity.this.posts.clear();
            PostCreateActivity.this.posts.add(post);

            postCreateAdapter.notifyDataSetChanged();
        }
    }


    private void attachGalleryHandle() {
        //                    //TODO-TEMP
        PhotoAlbumPickerActivity fragment = new PhotoAlbumPickerActivity(true, null);
        fragment.setDelegate(new PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate() {
            @Override
            public void didSelectPhotos(ArrayList<String> photos, ArrayList<String> captions, ArrayList<MediaController.SearchImage> webPhotos) {
//                            SendMessagesHelper.prepareSendingPhotos(photos, null, dialog_id, replyingMessageObject, captions);
//                            SendMessagesHelper.prepareSendingPhotosSearch(webPhotos, dialog_id, replyingMessageObject);
//                            showReplyPanel(false, null, null, null, false, true);
//                            System.out.println();
                PostCreateActivity.this.didSelectPhotos(photos);

            }

            @Override
            public void startPhotoSelectActivity() {
                //TODO-was ?
                try {
                    Intent videoPickerIntent = new Intent();
                    videoPickerIntent.setType("video/*");
                    videoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                    videoPickerIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (1024 * 1024 * 1536));

                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    Intent chooserIntent = Intent.createChooser(photoPickerIntent, null);
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{videoPickerIntent});

                    startActivityForResult(chooserIntent, 1);
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }

            @Override
            public boolean didSelectVideo(String path) {
//                            if (Build.VERSION.SDK_INT >= 16) {
//                                return !openVideoEditor(path, true, true);
//                            } else {
//                                SendMessagesHelper.prepareSendingVideo(path, 0, 0, 0, 0, null, dialog_id, replyingMessageObject);
//                                showReplyPanel(false, null, null, null, false, true);
//                                return true;
//                            }

                return false;
            }
        });
        presentFragment(fragment);
    }


    private boolean attachPhotoHandle() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File image = AndroidUtilities.generatePicturePath();
            if (image != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                currentPicturePath = image.getAbsolutePath();
            }
            startActivityForResult(takePictureIntent, 0);
            return true;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
            return false;
        }
    }


    private void updateVenue() {
        if (venue != null) {
            updateTitle();
            updateSubtitle();
            checkAndUpdateAvatar();

        }

    }

}
