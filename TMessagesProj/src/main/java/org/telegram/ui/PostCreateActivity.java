/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.MediaController;
import org.telegram.android.NotificationCenter;
import org.telegram.android.PostsController;
import org.telegram.android.location.LocationManagerHelper;
import org.telegram.android.support.widget.LinearLayoutManager;
import org.telegram.android.support.widget.RecyclerView;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.TLRPC;

import ru.aragats.wgo.dto.Coordinates;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.Venue;

import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.PostMediaCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PostCreateActivityEnterView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.utils.CollectionUtils;
import org.telegram.utils.Constants;
import org.telegram.utils.NotificationEnum;
import org.telegram.utils.Permissions;
import org.telegram.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.aragats.wgo.ApplicationLoader;
import ru.aragats.wgo.R;

//TODO-aragats new
public class PostCreateActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {

    private ArrayList<PostMediaCell> postMediaCellsCache = new ArrayList<>();

    private PostCreateActivityEnterView postCreateActivityEnterView;
    private ActionBarMenuItem menuItem;
    private RecyclerListView postListView;
    private LinearLayoutManager postLayoutManager;
    private PostCreateActivityAdapter postCreateAdapter;
    //TODO BackupImageView contain ImageReceiver. So it more advanced. staff. It surrounds ImageReceiver
    private BackupImageView avatarImageView;
    private FrameLayout emptyViewContainer;
    private TextView venueNameTextView;
    private TextView addressTextView;
    private FrameLayout avatarContainer;

    private ProgressDialog progressDialog;
    private ActionBarMenuItem doneItem;


    private Post selectedObject;
    private boolean wasPaused = false;
    private Runnable waitingForCharaterEnterRunnable;

    private boolean openAnimationEnded = false;


    //TODO - here are post for ???
    // TODO make post insted of list of posts.
    protected ArrayList<Post> posts = new ArrayList<>();
    protected Venue venue;
    //gps coordinates
    protected Coordinates userCoordinates;


    private String currentPicturePath;

    private final static int done_button = 1;
    private final static int chat_menu_attach = 2;
    private final static int attach_photo = 3;
    private final static int attach_gallery = 4;
    private final static int attach_location = 5;

    private final static int id_chat_compose_panel = 1000;


    private PhotoPickerWrapper photoPickerWrapper;
    //handler
    private Handler handler = new Handler();

    private Runnable refreshProgressRun = new Runnable() {
        @Override
        public void run() {
            if (photoPickerWrapper != null && photoPickerWrapper.isLoading() && progressDialog != null) {
                progressDialog.show();
            }
        }
    };


    RecyclerListView.OnItemLongClickListener onItemLongClickListener = new RecyclerListView.OnItemLongClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (!actionBar.isActionModeShowed()) {
//                createMenu(view, false);
            }
        }
    };

    RecyclerListView.OnItemClickListener onItemClickListener = new RecyclerListView.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (actionBar.isActionModeShowed()) {
                return;
            }
//            createMenu(view, true);
        }
    };

//    public interface PostCreateActivityDelegate {
//        void didSelectDialog(PostsActivity fragment, long dialog_id, boolean param);
//    }


    public PostCreateActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        if (!LocationManagerHelper.getInstance().isLocationServiceEnabled()) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.locationServiceDisabled);
        }
        //TODO-temp
//        PostsController.getInstance().loadCurrentVenue("location");
        userCoordinates = LocationManagerHelper.convertLocationToCoordinates(LocationManagerHelper.getInstance().getLastLocation());
//        if (userCoordinates != null && venue == null) {
//            venue = new Venue();
//            venue.setCoordinates(userCoordinates);
//        }

//        if(0==0) {
//            return true;
//        }
        //

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.newPostSaved);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.savePostError);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceivedNewPosts);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.invalidPost);

        if (getArguments() != null) {
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            String text = (String) super.arguments.get("text");
            String image = (String) super.arguments.get("image");
            if (!StringUtils.isEmpty(text)) {
                editor.putString(Constants.PREF_NEW_POST_TEXT, text.trim());
            }
            if (!StringUtils.isEmpty(image)) {
                editor.putString(Constants.PREF_NEW_POST_PHOTO, image);
            }
            editor.commit();
        }

        createPhotoPickerWrapper();
        super.onFragmentCreate();

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
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.newPostSaved);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.savePostError);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceivedNewPosts);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.invalidPost);


        if (!AndroidUtilities.isTablet() && getParentActivity() != null) {
            getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
        AndroidUtilities.unlockOrientation(getParentActivity());

        //save venue before to destroy it
        PostsController.getInstance().setLastVenue(venue);
        destroyPhotoPickerWrapper();
    }


    private Post buildPost() {
        Post post = null;
        if (!CollectionUtils.isEmpty(posts)) {
            post = posts.get(0);
            post.setVenue(venue);

//                        //TODO invent better way. save prefix and suffix also ??  to build my custom utl instead of replacing from existing.
//                        Image image = post.getVenue().getImage();
//                        if(image != null && !StringUtils.isEmpty(image.getUrl())) {
//                            image.setUrl(image.getUrl().replace("_64", "_bg_64"));
//                            post.getVenue().setImage(image);
//                        }
//            post.setCoordinates(userCoordinates); // TODO immer userCoordinates. So does not matter where post is attached ???
            String text = postCreateActivityEnterView.getFieldTrimmedText();
            post.setText(text);
        }


        return post;

    }

    // pass only valid post with venue and venue coordinates.
    private void reBuildValidPost(Post post) {
        if (post == null) {
            return;
        }
        // TODO not set set it in the server side. Take from venue. because if I set it here and I will have exception then I will have incorrect center of circle.
//        post.setCoordinates(venue.getCoordinates());
        // if venue is not from Foursquare.
        if (StringUtils.isEmpty(post.getVenue().getName())) {
            List<Double> coordinates = post.getVenue().getCoordinates().getCoordinates();
            String defaultAddress = String.format(Locale.US, "(%f,%f)", coordinates.get(1), coordinates.get(0));
            String address = LocationManagerHelper.getInstance().getAddress(getParentActivity(),
                    coordinates, defaultAddress);
            post.getVenue().setAddress(address);
        }

    }

    @Override
    public View createView(final Context context, LayoutInflater inflater) {

        for (int a = 0; a < 4; a++) {
            postMediaCellsCache.add(new PostMediaCell(context));
        }


        ResourceLoader.loadRecources(context);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                if (id == -1) {
                    //back button
                    if (postCreateActivityEnterView != null) {
                        postCreateActivityEnterView.hideEmojiPopup();
                    }
                    finishFragment();
                } else if (id == done_button) {
                    Post post = buildPost();
                    boolean valid = validatePost(post, false);
                    if (valid) {
                        progressDialog.show();
                        reBuildValidPost(post);
                        PostsController.getInstance().addPost(post);
                    }
                } else if (id == attach_photo) {
                    attachPhotoHandle();
                } else if (id == attach_gallery) {
                    openPhotoPicker();
                } else if (id == attach_location) {
                    openLocationChooser();
                }
            }
        });


        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);

        //Send button
//        FrameLayout sendButtonContainer = new FrameLayoutFixed(context);
//        sendButtonContainer.setBackgroundResource(R.drawable.bar_selector);
//        sendButtonContainer.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
//        actionBar.addView(sendButtonContainer, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.RIGHT, 56, 0, 40, 0));
//
//        TextView sendTextView = new TextView(context);
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


        venueNameTextView = new TextView(context);
        venueNameTextView.setTextColor(0xffffffff);
        venueNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        venueNameTextView.setLines(1);
        venueNameTextView.setMaxLines(1);
        venueNameTextView.setSingleLine(true);
        venueNameTextView.setEllipsize(TextUtils.TruncateAt.END);
        venueNameTextView.setGravity(Gravity.LEFT);
        venueNameTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        venueNameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        venueNameTextView.setText(LocaleController.getString("Place", R.string.Place));
        avatarContainer.addView(venueNameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 54, 0, 0, 22));

        addressTextView = new TextView(context);
        addressTextView.setTextColor(0xffd7e8f7);
        addressTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        addressTextView.setLines(1);
        addressTextView.setMaxLines(1);
        addressTextView.setSingleLine(true);
        addressTextView.setEllipsize(TextUtils.TruncateAt.END);
        addressTextView.setGravity(Gravity.LEFT);
        avatarContainer.addView(addressTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 54, 0, 0, 4));


        ActionBarMenu menu = actionBar.createMenu();

//        R.drawable.ic_send  - make it white icon then I can use it here. !!
//        R.drawable.ic_done
//        R.drawable.ic_done_gray is from ic_ab_done_gray
        doneItem = menu.addItemWithWidth(done_button, R.drawable.ic_done_gray, AndroidUtilities.dp(56));

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
        menuItem.addSubItem(attach_location, LocaleController.getString("ChatLocation", R.string.Location), R.drawable.ic_attach_location);
        menuItem.setShowFromBottom(true);
        menuItem.setBackgroundDrawable(null);


        final ActionBarMenu actionMode = actionBar.createActionMode();
        actionMode.addItem(-2, R.drawable.ic_ab_back_grey, R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54));

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
        emptyView.setText(LocaleController.getString("addPhotoHint", R.string.AddPhotoHint));
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(0xffffffff);
//        emptyView.setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_black : R.drawable.system_blue);
        emptyView.setBackgroundResource(R.drawable.system_blue);
        emptyView.setPadding(AndroidUtilities.dp(7), AndroidUtilities.dp(1), AndroidUtilities.dp(7), AndroidUtilities.dp(1));
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoPicker();
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

        postCreateActivityEnterView = new PostCreateActivityEnterView(getParentActivity(), contentView, this);
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
                updateDoneIcon(); // TODO it is run to often
//                if (mentionsAdapter != null) {
//                    mentionsAdapter.searchUsernameOrHashtag(text.toString(), postCreateActivityEnterView.getCursorPosition(), messages);
//                }
                if (waitingForCharaterEnterRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(waitingForCharaterEnterRunnable);
                    waitingForCharaterEnterRunnable = null;
                }
            }

            @Override
            public void needSendTyping() {
            }

            @Override
            public void onAttachButtonHidden() {

            }

            @Override
            public void onAttachButtonShow() {

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

//        if (loading && posts.isEmpty()) {
//            progressView.setVisibility(View.VISIBLE);
//            postListView.setEmptyView(null);
//        } else {
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
        if (venueNameTextView == null) {
            return;
        }
        String name = LocaleController.getString("CurrentLocation", R.string.CurrentLocation);
        if (venue != null && !StringUtils.isEmpty(venue.getName())) {
            name = venue.getName();
        }
        venueNameTextView.setText(name);

    }

    private void updateTitleIcons() {
        int leftIcon = 0;
//        int rightIcon = MessagesController.getInstance().isDialogMuted(dialog_id) ? R.drawable.mute_fixed : 0;
        int rightIcon = 0;
        venueNameTextView.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, rightIcon, 0);

    }

    private void updateSubtitle() {
        if (addressTextView == null) {
            return;
        }
//        CharSequence addressString = "printing";
        CharSequence addressString = LocaleController.getString("Address", R.string.Address);
        if (venue != null) {
            if (!StringUtils.isEmpty(venue.getAddress())) {
                addressString = venue.getAddress();
            } else if (venue.getCoordinates() != null && !CollectionUtils.isEmpty(venue.getCoordinates().getCoordinates(), 2)) {
                List<Double> coordinates = venue.getCoordinates().getCoordinates();
                addressString = String.format(Locale.US, "(%f,%f)", coordinates.get(1), coordinates.get(0));
            }
        } else if (userCoordinates != null && !CollectionUtils.isEmpty(userCoordinates.getCoordinates())) {
            addressString = String.format(Locale.US, "(%f,%f)",
                    userCoordinates.getCoordinates().get(1), userCoordinates.getCoordinates().get(0));
        }
//        addressString = TextUtils.replace(addressString, new String[]{"..."}, new String[]{""});
        addressTextView.setText(addressString);
    }

    private void checkAndUpdateAvatar() {
//        ImageView imageView = new ImageView(this.getParentActivity());
//        imageView.setImageResource(R.drawable.pin);


        if (avatarImageView != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
//            avatarImageView.setImage(newPhoto, "50_50", avatarDrawable);
//            avatarImageView.setImage("/storage/emulated/0/Telegram/WGO Images/730111210_6623.jpg", "50_50", avatarDrawable);
            //TODO probably save venue. into field in Activity.
            if (venue != null && venue.getIcon() != null && !StringUtils.isEmpty(venue.getIcon().getUrl())) {
                avatarImageView.setImage(venue.getIcon().getUrl(), "50_50", avatarDrawable);
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
                });
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
        if (id == NotificationCenter.didReceivedNewPosts) {
            // add new item and then notifyDataSetChanged
            if (this.postCreateAdapter != null) {
                postCreateAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.newPostSaved) {
//            progressDialog.hide();
            //clear
//            venue = null;
//            if (posts != null) {
//                posts.clear();
//            }
//            if (postCreateActivityEnterView != null) {
//                postCreateActivityEnterView.setFieldText("");
//            }
            progressDialog.dismiss();
            if (postCreateActivityEnterView != null) {
                postCreateActivityEnterView.hideEmojiPopup();
            }
            clearStates();
            finishFragment();

        } else if (id == NotificationCenter.savePostError) {
//            progressDialog.hide();
            //clear
//            venue = null;
//            if (posts != null) {
//                posts.clear();
//            }
//            if (postCreateActivityEnterView != null) {
//                postCreateActivityEnterView.setFieldText("");
//            }
            progressDialog.dismiss();
            Toast.makeText(getParentActivity(), "Error in saving post", Toast.LENGTH_LONG).show();
        } else if (id == NotificationCenter.invalidPost) {
            progressDialog.dismiss();
            NotificationEnum reason = NotificationEnum.INVALID_POST;
            if (args != null && args.length > 0) {
                reason = (NotificationEnum) args[0];
            }
            String message = "Invalid post. Some parameters are missing.";
            switch (reason) {
                case PHOTO_DATE_OLD:
                    //date
                    message = "You picked old photo. Please use recent photo from the last 7 days.";
                    break;
                case POST_TEXT_EMPTY:
                    message = "Empty text. Please describe the situation.";
                    break;
                case POST_TEXT_LENGTH:
                    message = "The text is too long. Please maximum 140 characters.";
                    break;
                case PHOTO_NOT_SET:
                    message = "The photo is not chosen. Please choose appropriate photo.";
                    break;
                case VENUE_NOT_SET:
                    message = "The venue or coordinates is not chosen. Please choose appropriate venue or coordinates.";
                    break;

            }
            Toast.makeText(getParentActivity(), message, Toast.LENGTH_LONG).show();
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
        String lastMessageText = preferences.getString(Constants.PREF_NEW_POST_TEXT, null);
        if (lastMessageText != null) {
            preferences.edit().remove(Constants.PREF_NEW_POST_TEXT).commit();
            postCreateActivityEnterView.setFieldText(lastMessageText);
        }
        String lastPhotoURL = preferences.getString(Constants.PREF_NEW_POST_PHOTO, null);
        if (!StringUtils.isEmpty(lastPhotoURL) && posts.isEmpty()) {
            preferences.edit().remove(Constants.PREF_NEW_POST_PHOTO).commit();
            ArrayList<String> photos = new ArrayList<>();
            photos.add(lastPhotoURL);
            didSelectPhotos(photos); // set venue
        }
        // Location service works.
        if (userCoordinates != null) {
            Venue lastSavedVenue = PostsController.getInstance().getLastVenue();
            Post post = getPost();
            if (lastSavedVenue != null && lastSavedVenue.getCoordinates() != null) {
                // venue is not set from photo.
                if (venue == null && LocationManagerHelper.convertCoordinatesToLocation(userCoordinates)
                        .distanceTo(LocationManagerHelper.convertCoordinatesToLocation(lastSavedVenue.getCoordinates())) <= Constants.RADIUS_4SQUARE) { //800 -> 1000// 1,25* Radius. actually 4square return venues far than 800 meters.  up to 1600 meters.
                    venue = PostsController.getInstance().getLastVenue();

                }
                // venue from photo Additional check post.getCoordinates equals ....
                else if (venue != null && post != null && post.getCoordinates() != null
                        && LocationManagerHelper.convertCoordinatesToLocation(post.getCoordinates())
                        .distanceTo(LocationManagerHelper.convertCoordinatesToLocation(lastSavedVenue.getCoordinates())) <= Constants.RADIUS_4SQUARE) {
                    venue = PostsController.getInstance().getLastVenue();
                }
            } else if (venue == null) {
                venue = new Venue();
                venue.setCoordinates(userCoordinates);
            }
        }
        PostsController.getInstance().setLastVenue(null);
        updateIcons();


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
            if (text != null || !posts.isEmpty() || venue != null) {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                //TODO save text before pause.
                if (text != null) {
                    editor.putString(Constants.PREF_NEW_POST_TEXT, text);
                } else {
                    editor.remove(Constants.PREF_NEW_POST_TEXT);
                }
                if (!posts.isEmpty() && posts.get(0) != null && !StringUtils.isEmpty(posts.get(0).getImage().getUrl())) {
                    editor.putString(Constants.PREF_NEW_POST_PHOTO, posts.get(0).getPreviewImage().getUrl());
                } else {
                    editor.remove(Constants.PREF_NEW_POST_PHOTO);
                }
                editor.commit();
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
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(Object postObject, int index) {
        if (postObject == null || !(postObject instanceof Post)) {
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
                        && post.getId().equals(((Post) postObject).getId())) {
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
    public Bitmap getThumbForPhoto(int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(int index) {
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
//                view.findViewById(R.id.progressLayout).setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_loader2 : R.drawable.system_loader1);
                view.findViewById(R.id.progressLayout).setBackgroundResource(R.drawable.system_loader1);
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
                        venue = new Venue();
                        userCoordinates = LocationManagerHelper.convertLocationToCoordinates(LocationManagerHelper.getInstance().getLastLocation());
                        if (userCoordinates == null) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.undefinedLocation);
                        }
                        venue.setCoordinates(userCoordinates);
                        updateIcons();
                        postCreateAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void didLongPressed(PostMediaCell cell) {
//                        createMenu(cell, false);
                    }

                    @Override
                    public boolean canPerformActions() {
                        return actionBar != null && !actionBar.isActionModeShowed();
                    }

                    @Override
                    public void didPressUrl(String url) {
//                        if (url.startsWith("@")) {
//                            MessagesController.openByUserName(url.substring(1), PostCreateActivity.this, 0);
//                        } else if (url.startsWith("#")) {
//                            MessagesActivity fragment = new MessagesActivity(null);
//                            fragment.setSearchString(url);
//                            presentFragment(fragment);
//                        }
                    }

                    @Override
                    public void needOpenWebView(String url, String title, String originalUrl, int w, int h) {
//                        BottomSheet.Builder builder = new BottomSheet.Builder(mContext);
//                        builder.setCustomView(new WebFrameLayout(mContext, builder.create(), title, originalUrl, url, w, h));
//                        builder.setOverrideTabletWidth(true);
//                        showDialog(builder.create());
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
//                        PhotoViewer.getInstance().openPhoto(post, PostCreateActivity.this);
//                        PhotoViewer.getInstance().openPhoto(post, PostCreateActivity.this);

                        //TODO open location view
//                                LocationActivity fragment = new LocationActivity();
//                                fragment.setMessageObject(post);
//                                presentFragment(fragment);

                    }

                    @Override
                    public void didPressedOther(PostMediaCell cell) {
//                        createMenu(cell, true);
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
        Activity activity = getParentActivity();
        //TODO check both permissions
        if (!Permissions.locationPermitted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(Permissions.LOCATION_PERMISSION_GROUP, Permissions.LOCATION_REQUEST_CODE);
            return;
        }
        if (!isGoogleMapsInstalled()) {
            return;
        }
        Bundle args = new Bundle();
        args.putBoolean(Constants.RESTRICTED_AREA, true);
        args.putInt(Constants.RADIUS_ARG, Constants.RADIUS_4SQUARE);
        args.putBoolean(Constants.SEARCH_PLACES_ENABLE_ARG, true);
        LocationActivityAragats fragment = new LocationActivityAragats(args);
        Post post = getPost();
        //coordinates of image
        if (post != null && post.getCoordinates() != null) {
            fragment.setCustomLocation(LocationManagerHelper.convertCoordinatesToLocation(post.getCoordinates()));
        }
        if (venue != null && venue.getCoordinates() != null) {
            fragment.setVenueChosenLocation(LocationManagerHelper.convertCoordinatesToLocation(venue.getCoordinates()));
        }
        fragment.setDelegate(new LocationActivityAragats.LocationActivityDelegate() {
            @Override
            public void didSelectLocation(TLRPC.MessageMedia location) {
                Coordinates coordinates = new Coordinates();
                coordinates.setCoordinates(Arrays.asList(location.geo._long, location.geo.lat));
                coordinates.setType("Point");
//                PostCreateActivity.this.userCoordinates = coordinates; // TODO I do not need to change userCoordinates.
                Venue venue = new Venue();
                if (location.geoPlace != null) {
                    Coordinates placeCoordinates = new Coordinates();
                    placeCoordinates.setCoordinates(Arrays.asList(location.geoPlace._long, location.geoPlace.lat));
                    placeCoordinates.setType("Point");
                    venue.setCoordinates(placeCoordinates);
                } else {
                    venue.setCoordinates(coordinates);
                }
                venue.setFoursquareId(location.venue_id);
                if (!StringUtils.isEmpty(location.iconUrl)) {
                    Image image = new Image();
                    image.setUrl(location.iconUrl);
                    venue.setIcon(image);
                }
                venue.setName(location.title);
                venue.setAddress(location.address);
//                if (StringUtils.isEmpty(venue.getAddress())) {
////                    String address = LocationManagerHelper.getInstance().getAddress(getParentActivity(),
////                            location.geo._long, location.geo.lat, location.geo._long + ", " + location.geo.lat);
//                    String address = String.format(Locale.US, "(%f,%f)", location.geo.lat, location.geo._long);
//                    venue.setAddress(address);
//                }
                PostCreateActivity.this.venue = venue;

//                location.iconUrl;
//                Toast.makeText(getParentActivity(), location.venue_id + " " + location.geo.lat + " " + location.geo._long, Toast.LENGTH_LONG).show();
//                            SendMessagesHelper.getInstance().sendMessage(location, dialog_id, replyingMessageObject);
//                            moveScrollToLastMessage();
//                            showReplyPanel(false, null, null, null, false, true);
//                            if (paused) {
//                                scrollToTopOnResume = true;
//                            }
                updateIcons();
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
//            long megaBytes = (file.length()/1024)/1024;
            long bytes = file.length();

            BitmapFactory.Options options = new BitmapFactory.Options();
            // TODO THIS Do not allow decode the file.
//            options.inJustDecodeBounds = true;

//Returns null, sizes are in the options variable
            Bitmap bitmap = BitmapFactory.decodeFile(photoUrl, options);
            int width = options.outWidth;
            int height = options.outHeight;
//If you want, the MIME type will also be decoded (if possible)
            String type = options.outMimeType;
//            String type = getMimeType(photoUrl);

//            Java 7
//            MediaStore.Files.probeContentType(path);

            //
//            bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);

// Create post
            Post post = new Post();
//            post.setId(PostServiceMock.generateString("1234567890", 5));
            Image image = new Image();
            image.setUrl(photoUrl);
            image.setWidth(width);
            image.setHeight(height);
            image.setBitmap(bitmap);
            image.setType(type);
            image.setSize(bytes);
//            image = ImageServiceMock.getRandomImage();
            List<Image> images = new ArrayList<>();
            images.add(image); // preview
            images.add(image); // original
            post.setImages(images);
//            post.setVenue(VenueServiceMock.getRandomVenue());
            //TODO-temp
//            PostCreateActivity.this.post = new PostObject(post);
            //DELETE ALL to store only one

            updatePost(post);
            PostCreateActivity.this.posts.clear();
            if (validateDate(post.getCreatedDate())) {
                PostCreateActivity.this.posts.add(post);
                if (post.getCoordinates() != null) {
                    venue = new Venue();
                    venue.setCoordinates(post.getCoordinates());
                    updateIcons();
                }
            } else {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.invalidPost, NotificationEnum.PHOTO_DATE_OLD);
            }
            postCreateAdapter.notifyDataSetChanged();
        }
    }

    private boolean validatePost(Post post, boolean withNotification) {
        if (post == null) {
            if (withNotification) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.invalidPost, NotificationEnum.INVALID_POST);
            }
            return false;
        }
        if (StringUtils.isEmpty(post.getText())) {
            if (withNotification) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.invalidPost, NotificationEnum.POST_TEXT_EMPTY);
            }
            return false;
        }
        if (!validateText(post.getText())) {
            if (withNotification) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.invalidPost, NotificationEnum.POST_TEXT_LENGTH);
            }
            return false;
        }
        if (!validateDate(post.getCreatedDate())) {
            if (withNotification) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.invalidPost, NotificationEnum.PHOTO_DATE_OLD);
            }
            return false;
        }
        if (!validateImage(post.getImage())) {
            if (withNotification) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.invalidPost, NotificationEnum.PHOTO_NOT_SET);
            }
            return false;
        }
        if (!validateVenue(post.getVenue())) {
            if (withNotification) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.invalidPost, NotificationEnum.VENUE_NOT_SET);
            }
            return false;
        }
        return true;
    }

    // only photo based on MAX DATE_SHIFT. Think about accepting photo withou date.
    private boolean validateDate(long date) {
        return date != 0 && (new Date().getTime() - date) < Constants.MAX_DATE_SHIFT;
    }

    private boolean validateText(String text) {
        return !StringUtils.isEmpty(text) && (text.trim().length() <= Constants.MAX_TEXT_LENGTH);
    }

    private boolean validateImage(Image image) {
        return image != null && !StringUtils.isEmpty(image.getUrl());
    }

    private boolean validateVenue(Venue venue) {
        return venue != null && venue.getCoordinates() != null
                && !CollectionUtils.isEmpty(venue.getCoordinates().getCoordinates())
                && venue.getCoordinates().getCoordinates().size() == 2;
    }


    private void updatePost(Post post) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(post.getImage().getUrl());
        } catch (IOException e) {
            //TODO handle exception.
            e.printStackTrace();
            return;
        }
//        new Date(new File(post.getImage().getUrl()).lastModified())
        String datetime = exif.getAttribute(ExifInterface.TAG_DATETIME); // could be null
        if (!StringUtils.isEmpty(datetime)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT_PATTERN, Locale.getDefault());
            Date date;
            try {
                date = simpleDateFormat.parse(datetime);
            } catch (ParseException e) {
                //TODO handle exception
                e.printStackTrace();
                return;
            }
            post.setCreatedDate(date.getTime());
        }
        float[] coords = new float[2];
        boolean result = exif.getLatLong(coords);
        if (result) {
            Coordinates coordinates = new Coordinates();
            coordinates.setType(Constants.POINT);
            coordinates.setCoordinates(Arrays.asList((double) coords[1], (double) coords[0]));
            post.setCoordinates(coordinates); // coordinate of image
        }

    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    private void attachGalleryHandle() {
        //                    //TODO-TEMP
        PhotoAlbumPickerActivity fragment = new PhotoAlbumPickerActivity(true);
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


    private void openPhotoPicker() {
        Activity activity = getParentActivity();
        if (!Permissions.storagePermitted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(Permissions.STORAGE_PERMISSION_GROUP, Permissions.STORAGE_REQUEST);
            return;
        }
        Permissions.storagePermitted = true;
        if (photoPickerWrapper != null) {
            startProgressView();
            photoPickerWrapper.openPhotoPicker();
        }
    }


    private void destroyPhotoPickerWrapper() {
        if (photoPickerWrapper != null) {
            photoPickerWrapper.onFragmentDestroy();
        }
    }

    private void createPhotoPickerWrapper() {
        //                    //TODO-TEMP
        if (photoPickerWrapper != null) {
            return;
        }
        photoPickerWrapper = new PhotoPickerWrapper(this, true);
        photoPickerWrapper.setDelegate(new PhotoPickerWrapper.PhotoPickerWrapperActivityDelegate() {
            @Override
            public void didSelectPhotos(ArrayList<String> photos, ArrayList<String> captions, ArrayList<MediaController.SearchImage> webPhotos) {
                PostCreateActivity.this.didSelectPhotos(photos);
            }

            @Override
            public void didPhotoAlbumLoaded() {
                stopProgressView();
            }

            @Override
            public boolean didSelectVideo(String path) {
                // look at example in original PhotoAlbumActivity
                return false;
            }

        });

    }

    private boolean attachPhotoHandle() {
        Activity activity = getParentActivity();
        if (!Permissions.cameraPermitted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(Permissions.CAMERA_PERMISSION_GROUP, Permissions.CAMERA_REQUEST);
            return false;
        }
        Permissions.cameraPermitted = true;
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


    private void updateIcons() {
        updateVenue();
        updateDoneIcon();
    }

    private void updateDoneIcon() {
        doneItem.setIcon(R.drawable.ic_done_gray); // default
        doneItem.setEnabled(false);
        Post post = buildPost();
        boolean valid = validatePost(post, false);
        if (valid) {
            doneItem.setIcon(R.drawable.ic_done);
            doneItem.setEnabled(true);
        }
    }

    private void updateVenue() {
        if (venue != null) {
            updateTitle();
            updateSubtitle();
            checkAndUpdateAvatar();
        }
    }

    private void clearStates() {
        posts.clear();
        venue = null;
        userCoordinates = null;
        PostsController.getInstance().setLastVenue(null);
        postCreateActivityEnterView.setFieldText("");
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        preferences.edit().remove(Constants.PREF_NEW_POST_TEXT).commit();
        preferences.edit().remove(Constants.PREF_NEW_POST_PHOTO).commit();

    }

    private Post getPost() {
        if (!CollectionUtils.isEmpty(posts)) {
            return posts.get(0);
        }
        return null;
    }

    private void startProgressView() {
        handler.postDelayed(refreshProgressRun, Constants.PROGRESS_DIALOG_TIMEOUT);
    }

    private void stopProgressView() {
        handler.removeCallbacks(refreshProgressRun);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
