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
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.android.AnimationCompat.AnimatorSetProxy;
import org.telegram.android.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.android.AnimationCompat.ViewProxy;
import org.telegram.android.ContactsController;
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.MediaController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.android.NotificationsController;
import org.telegram.android.PostsController;
import org.telegram.android.SendMessagesHelper;
import org.telegram.android.support.widget.LinearLayoutManager;
import org.telegram.android.support.widget.RecyclerView;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.RPCRequest;
import org.telegram.messenger.TLObject;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.Adapters.MentionsAdapter;
import org.telegram.ui.Cells.ChatActionCell;
import org.telegram.ui.Cells.ChatAudioCell;
import org.telegram.ui.Cells.ChatBaseCell;
import org.telegram.ui.Cells.ChatMediaCell;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.SendingFileExDrawable;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TimerDrawable;
import org.telegram.ui.Components.WebFrameLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

//TODO-aragats new
public class PostCreateActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, MessagesActivity.MessagesActivityDelegate,
        PhotoViewer.PhotoViewerProvider {

    protected TLRPC.Chat currentChat;
    protected TLRPC.User currentUser;

    private ArrayList<ChatMediaCell> chatMediaCellsCache = new ArrayList<>();

    private FrameLayout progressView;
    private FrameLayout bottomOverlay;
    //TODO aragats
    private ChatActivityEnterView postCreateActivityEnterView;
    private ImageView timeItem;
    private View timeItem2;
    private TimerDrawable timerDrawable;
    private ActionBarMenuItem menuItem;
    private ActionBarMenuItem attachItem;
    private ActionBarMenuItem headerItem;
    private TextView addContactItem;
    //TODO list of. aragats
    private RecyclerListView postListView;
    private LinearLayoutManager postLayoutManager;
    //TODO-aragats
    private ChatActivityAdapter postCreateAdapter;
    private BackupImageView avatarImageView;
    private TextView bottomOverlayChatText;
    private FrameLayout bottomOverlayChat;
    private SendingFileExDrawable sendingFileDrawable;
    private FrameLayout emptyViewContainer;
    private ArrayList<View> actionModeViews = new ArrayList<>();
    private TextView nameTextView;
    private TextView onlineTextView;
    private FrameLayout avatarContainer;
    private TextView bottomOverlayText;
    private TextView muteItem;
    private ImageView pagedownButton;
    private MentionsAdapter mentionsAdapter;
    private ListView mentionListView;
    private AnimatorSetProxy mentionListAnimation;


    private MessageObject selectedObject;
    private boolean wasPaused = false;
    private long linkSearchRequestId;
    private TLRPC.WebPage foundWebPage;
    private Runnable waitingForCharaterEnterRunnable;

    private boolean openAnimationEnded = false;

    private int readWithDate = 0;
    private int readWithMid = 0;
    private boolean scrollToTopOnResume = false;
    private boolean scrollToTopUnReadOnResume = false;

    private HashMap<Integer, MessageObject> messagesDict = new HashMap<>();
    protected ArrayList<MessageObject> messages = new ArrayList<>();
    private int maxMessageId = Integer.MAX_VALUE;
    private int minMessageId = Integer.MIN_VALUE;
    private int maxDate = Integer.MIN_VALUE;
    private boolean endReached = false;
    private boolean loading = false;
    private boolean cacheEndReaced = false;
    private boolean firstLoading = true;
    private int loadsCount = 0;

    private boolean needSelectFromMessageId;
    private int returnToMessageId = 0;

    private int minDate = 0;
    private boolean first = true;
    private int unread_to_load = 0;
    private int first_unread_id = 0;
    private int last_message_id = 0;
    private int first_message_id = 0;
    private boolean forward_end_reached = true;
    private boolean loadingForward = false;
    private MessageObject unreadMessageObject = null;
    private MessageObject scrollToMessage = null;
    private int highlightMessageId = Integer.MAX_VALUE;
    private boolean scrollToMessageMiddleScreen = false;

    private String currentPicturePath;

    private Rect scrollRect = new Rect();

    protected TLRPC.ChatParticipants info = null;
    private int onlineCount = -1;


    private Runnable openSecretPhotoRunnable = null;
    private float startX = 0;
    private float startY = 0;

    private final static int copy = 1;
    private final static int forward = 2;
    private final static int delete = 3;
    private final static int chat_enc_timer = 4;
    private final static int chat_menu_attach = 5;
    private final static int attach_photo = 6;
    private final static int attach_gallery = 7;
    private final static int attach_video = 8;
    private final static int attach_document = 9;
    private final static int attach_location = 10;
    private final static int clear_history = 11;
    private final static int delete_chat = 12;
    private final static int share_contact = 13;
    private final static int mute = 14;
    private final static int reply = 15;

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


        sendingFileDrawable = new SendingFileExDrawable();
        sendingFileDrawable.setIsChat(currentChat != null);

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
        MediaController.getInstance().stopAudio();
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {

        for (int a = 0; a < 4; a++) {
            chatMediaCellsCache.add(new ChatMediaCell(context));
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
                } else if (id == attach_photo) {
                    try {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File image = AndroidUtilities.generatePicturePath();
                        if (image != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                            currentPicturePath = image.getAbsolutePath();
                        }
                        startActivityForResult(takePictureIntent, 0);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                } else if (id == attach_gallery) {
//                    //TODO-TEMP
//                    PhotoAlbumPickerActivity fragment = new PhotoAlbumPickerActivity(false, PostCreateActivity.this);
//                    fragment.setDelegate(new PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate() {
//                        @Override
//                        public void didSelectPhotos(ArrayList<String> photos, ArrayList<String> captions, ArrayList<MediaController.SearchImage> webPhotos) {
//                            SendMessagesHelper.prepareSendingPhotos(photos, null, dialog_id, replyingMessageObject, captions);
//                            SendMessagesHelper.prepareSendingPhotosSearch(webPhotos, dialog_id, replyingMessageObject);
//                            showReplyPanel(false, null, null, null, false, true);
//                        }
//
//                        @Override
//                        public void startPhotoSelectActivity() {
//                            try {
//                                Intent videoPickerIntent = new Intent();
//                                videoPickerIntent.setType("video/*");
//                                videoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
//                                videoPickerIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (1024 * 1024 * 1536));
//
//                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                                photoPickerIntent.setType("image/*");
//                                Intent chooserIntent = Intent.createChooser(photoPickerIntent, null);
//                                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{videoPickerIntent});
//
//                                startActivityForResult(chooserIntent, 1);
//                            } catch (Exception e) {
//                                FileLog.e("tmessages", e);
//                            }
//                        }
//
//                        @Override
//                        public boolean didSelectVideo(String path) {
//                            if (Build.VERSION.SDK_INT >= 16) {
//                                return !openVideoEditor(path, true, true);
//                            } else {
//                                SendMessagesHelper.prepareSendingVideo(path, 0, 0, 0, 0, null, dialog_id, replyingMessageObject);
//                                showReplyPanel(false, null, null, null, false, true);
//                                return true;
//                            }
//                        }
//                    });
//                    presentFragment(fragment);
                } else if (id == attach_location) {
                    if (!isGoogleMapsInstalled()) {
                        return;
                    }
                    LocationActivity fragment = new LocationActivity();
                    fragment.setDelegate(new LocationActivity.LocationActivityDelegate() {
                        @Override
                        public void didSelectLocation(TLRPC.MessageMedia location) {
                            Toast.makeText(getParentActivity(), location.venue_id + " " + location.geo.lat + " " + location.geo._long, Toast.LENGTH_LONG).show();
//                            SendMessagesHelper.getInstance().sendMessage(location, dialog_id, replyingMessageObject);
//                            moveScrollToLastMessage();
//                            showReplyPanel(false, null, null, null, false, true);
//                            if (paused) {
//                                scrollToTopOnResume = true;
//                            }
                        }
                    });
                    presentFragment(fragment);
                }
            }
        });

        avatarContainer = new FrameLayoutFixed(context);
        avatarContainer.setBackgroundResource(R.drawable.bar_selector);
        avatarContainer.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
        actionBar.addView(avatarContainer, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 56, 0, 40, 0));
        avatarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(((Context) getParentActivity()), "AVATAR CLICKED", Toast.LENGTH_SHORT).show();
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
        nameTextView.setText("Venue Name");
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

        headerItem = menu.addItem(0, R.drawable.ic_ab_other);
        //TODO mute temp
        muteItem = headerItem.addSubItem(mute, null, 0);
        ((LinearLayout.LayoutParams) headerItem.getLayoutParams()).setMargins(0, 0, AndroidUtilities.dp(-48), 0);

        updateTitle();
        updateSubtitle();
        updateTitleIcons();

        attachItem = menu.addItem(chat_menu_attach, R.drawable.ic_ab_other);
        attachItem.addSubItem(attach_photo, LocaleController.getString("ChatTakePhoto", R.string.ChatTakePhoto), R.drawable.ic_attach_photo);
        attachItem.addSubItem(attach_gallery, LocaleController.getString("ChatGallery", R.string.ChatGallery), R.drawable.ic_attach_gallery);
        attachItem.addSubItem(attach_location, LocaleController.getString("ChatLocation", R.string.ChatLocation), R.drawable.ic_attach_location);
        attachItem.setVisibility(View.INVISIBLE);

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

                    if (child == mentionListView) {
                        childTop -= postCreateActivityEnterView.getMeasuredHeight() - AndroidUtilities.dp(2);
                    } else if (child == pagedownButton) {
                        childTop -= postCreateActivityEnterView.getMeasuredHeight();
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
        if (currentUser != null && currentUser.id != 777000 && currentUser.id != 429000 && (currentUser.id / 1000 == 333 || currentUser.id % 1000 == 0)) {
            emptyView.setText(LocaleController.getString("GotAQuestion", R.string.GotAQuestion));
        } else {
            emptyView.setText(LocaleController.getString("NoMessages", R.string.NoMessages));
        }
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(0xffffffff);
        emptyView.setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_black : R.drawable.system_blue);
        emptyView.setPadding(AndroidUtilities.dp(7), AndroidUtilities.dp(1), AndroidUtilities.dp(7), AndroidUtilities.dp(1));
        emptyViewContainer.addView(emptyView, new FrameLayout.LayoutParams(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));


        if (postCreateActivityEnterView != null) {
            postCreateActivityEnterView.onDestroy();
        }

        postListView = new RecyclerListView(context);
        postListView.setVerticalScrollBarEnabled(true);
        postListView.setAdapter(postCreateAdapter = new ChatActivityAdapter(context));
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
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING && highlightMessageId != Integer.MAX_VALUE) {
                    highlightMessageId = Integer.MAX_VALUE;
                    updateVisibleRows();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = postLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(postLayoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                if (visibleItemCount > 0) {
                    int totalItemCount = postCreateAdapter.getItemCount();
                    if (firstVisibleItem <= 10) {
                        if (!endReached && !loading) {
                            loading = true;
                        }
                    }
                    if (firstVisibleItem + visibleItemCount >= totalItemCount - 6) {
                        if (!forward_end_reached && !loadingForward) {
                            loadingForward = true;
                        }
                    }
                    if (firstVisibleItem + visibleItemCount == totalItemCount && forward_end_reached) {
                        showPagedownButton(false, true);
                    }
                }
                updateMessagesVisisblePart();
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

        mentionListView = new ListView(context);
        mentionListView.setBackgroundResource(R.drawable.compose_panel);
        mentionListView.setVisibility(View.GONE);
        mentionListView.setPadding(0, AndroidUtilities.dp(2), 0, 0);
        mentionListView.setClipToPadding(true);
        mentionListView.setDividerHeight(0);
        mentionListView.setDivider(null);
        if (Build.VERSION.SDK_INT > 8) {
            mentionListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        }
        contentView.addView(mentionListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 110, Gravity.LEFT | Gravity.BOTTOM));

        mentionListView.setAdapter(mentionsAdapter = new MentionsAdapter(context, false, new MentionsAdapter.MentionsAdapterDelegate() {
            @Override
            public void needChangePanelVisibility(boolean show) {
                if (show) {
                    FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) mentionListView.getLayoutParams();
                    int height = 36 * Math.min(3, mentionsAdapter.getCount()) + (mentionsAdapter.getCount() > 3 ? 18 : 0);
                    layoutParams3.height = AndroidUtilities.dp(2 + height);
                    layoutParams3.topMargin = -AndroidUtilities.dp(height);
                    mentionListView.setLayoutParams(layoutParams3);

                    if (mentionListAnimation != null) {
                        mentionListAnimation.cancel();
                        mentionListAnimation = null;
                    }

                    if (mentionListView.getVisibility() == View.VISIBLE) {
                        ViewProxy.setAlpha(mentionListView, 1.0f);
                        return;
                    }
                } else {
                    if (mentionListAnimation != null) {
                        mentionListAnimation.cancel();
                        mentionListAnimation = null;
                    }

                    if (mentionListView.getVisibility() == View.GONE) {
                        return;
                    }
                }
            }
        }));
        mentionsAdapter.setChatInfo(info);
        mentionsAdapter.setNeedUsernames(currentChat != null);

        mentionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = mentionsAdapter.getItem(position);
                int start = mentionsAdapter.getResultStartPosition();
                int len = mentionsAdapter.getResultLength();
                if (object instanceof TLRPC.User) {
                    TLRPC.User user = (TLRPC.User) object;
                    if (user != null) {
                        postCreateActivityEnterView.replaceWithText(start, len, "@" + user.username + " ");
                    }
                } else if (object instanceof String) {
                    postCreateActivityEnterView.replaceWithText(start, len, object + " ");
                }
            }
        });

        mentionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = mentionsAdapter.getItem(position);
                if (object instanceof String) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                    builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mentionsAdapter.clearRecentHashtags();
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                    return true;
                }
                return false;
            }
        });


        postCreateActivityEnterView = new ChatActivityEnterView(getParentActivity(), contentView, this, true);
//        postCreateActivityEnterView.setDialogId(dialog_id);
        postCreateActivityEnterView.addToAttachLayout(menuItem);
        postCreateActivityEnterView.setId(id_chat_compose_panel);
        contentView.addView(postCreateActivityEnterView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));
        postCreateActivityEnterView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate() {
            @Override
            public void onMessageSend(String message) {
                moveScrollToLastMessage();
                if (mentionsAdapter != null) {
                    mentionsAdapter.addHashtagsFromMessage(message);
                }
            }

            @Override
            public void onTextChanged(final CharSequence text, boolean bigChange) {
                if (mentionsAdapter != null) {
                    mentionsAdapter.searchUsernameOrHashtag(text.toString(), postCreateActivityEnterView.getCursorPosition(), messages);
                }
                if (waitingForCharaterEnterRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(waitingForCharaterEnterRunnable);
                    waitingForCharaterEnterRunnable = null;
                }
                if (postCreateActivityEnterView.isMessageWebPageSearchEnabled()) {
                    if (bigChange) {
                        searchLinks(text, true);
                    } else {
                        waitingForCharaterEnterRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (this == waitingForCharaterEnterRunnable) {
                                    searchLinks(text, false);
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
                    if (mentionListView != null && mentionListView.getVisibility() == View.VISIBLE) {
                        mentionListView.clearAnimation();
                        mentionListView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mentionListView != null && mentionListView.getVisibility() == View.INVISIBLE) {
                        mentionListView.clearAnimation();
                        mentionListView.setVisibility(View.VISIBLE);
                    }
                }
                updateMessagesVisisblePart();
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

        pagedownButton = new ImageView(context);
        pagedownButton.setVisibility(View.INVISIBLE);
        pagedownButton.setImageResource(R.drawable.pagedown);
        contentView.addView(pagedownButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 6, 4));
        pagedownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (returnToMessageId > 0) {
                    scrollToMessageId(returnToMessageId, 0, true);
                } else {
                    scrollToLastMessage();
                }
            }
        });

        if (loading && messages.isEmpty()) {
            progressView.setVisibility(View.VISIBLE);
            postListView.setEmptyView(null);
        } else {
            progressView.setVisibility(View.INVISIBLE);
            postListView.setEmptyView(emptyViewContainer);
        }

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

    private void searchLinks(CharSequence charSequence, boolean force) {
        if (linkSearchRequestId != 0) {
            ConnectionsManager.getInstance().cancelRpc(linkSearchRequestId, true);
            linkSearchRequestId = 0;
        }
        if (force && foundWebPage != null) {
            if (foundWebPage.url != null) {
                int index = TextUtils.indexOf(charSequence, foundWebPage.url);
                char lastChar;
                boolean lenEqual;
                if (index == -1) {
                    index = TextUtils.indexOf(charSequence, foundWebPage.display_url);
                    lenEqual = index != -1 && index + foundWebPage.display_url.length() == charSequence.length();
                    lastChar = index != -1 && !lenEqual ? charSequence.charAt(index + foundWebPage.display_url.length()) : 0;
                } else {
                    lenEqual = index != -1 && index + foundWebPage.url.length() == charSequence.length();
                    lastChar = index != -1 && !lenEqual ? charSequence.charAt(index + foundWebPage.url.length()) : 0;
                }
                if (index != -1 && (lenEqual || lastChar == ' ' || lastChar == ',' || lastChar == '.' || lastChar == '!' || lastChar == '/')) {
                    return;
                }
            }
        }
        if (charSequence.length() < 13 || !searchForHttpInText(charSequence)) {
            return;
        }
        final TLRPC.TL_messages_getWebPagePreview req = new TLRPC.TL_messages_getWebPagePreview();
        if (charSequence instanceof String) {
            req.message = (String) charSequence;
        } else {
            req.message = charSequence.toString();
        }
        linkSearchRequestId = ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        linkSearchRequestId = 0;
                        if (error == null) {
                            if (response instanceof TLRPC.TL_messageMediaWebPage) {
                                foundWebPage = ((TLRPC.TL_messageMediaWebPage) response).webpage;
                                if (foundWebPage instanceof TLRPC.TL_webPage || foundWebPage instanceof TLRPC.TL_webPagePending) {
                                    if (foundWebPage instanceof TLRPC.TL_webPagePending) {
                                    }
                                } else {
                                    if (foundWebPage != null) {
                                        foundWebPage = null;
                                    }
                                }
                            } else {
                                if (foundWebPage != null) {
                                    foundWebPage = null;
                                }
                            }
                        }
                    }
                });
            }
        });
        ConnectionsManager.getInstance().bindRequestToGuid(linkSearchRequestId, classGuid);
    }

    private void moveScrollToLastMessage() {
        if (postListView != null) {
            postLayoutManager.scrollToPositionWithOffset(messages.size() - 1, -100000 - postListView.getPaddingTop());
        }
    }


    private void scrollToLastMessage() {
        if (forward_end_reached && first_unread_id == 0) {
            postLayoutManager.scrollToPositionWithOffset(messages.size() - 1, -100000 - postListView.getPaddingTop());
        } else {
            messages.clear();
            messagesDict.clear();
            progressView.setVisibility(View.VISIBLE);
            postListView.setEmptyView(null);
            maxMessageId = Integer.MAX_VALUE;
            minMessageId = Integer.MIN_VALUE;

            maxDate = Integer.MIN_VALUE;
            minDate = 0;
            forward_end_reached = true;
            loading = true;
            needSelectFromMessageId = false;
            postCreateAdapter.notifyDataSetChanged();
//            MessagesController.getInstance().loadMessages(dialog_id, 30, 0, true, 0, classGuid, 0, 0, 0, true);
        }
    }

    private void updateMessagesVisisblePart() {
        if (postListView == null) {
            return;
        }
        int count = postListView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = postListView.getChildAt(a);
            if (view instanceof ChatMessageCell) {
                ChatMessageCell messageCell = (ChatMessageCell) view;
                messageCell.getLocalVisibleRect(scrollRect);
                messageCell.setVisiblePart(scrollRect.top, scrollRect.bottom - scrollRect.top);
            }
        }
    }

    private void scrollToMessageId(int id, int fromMessageId, boolean select) {
        returnToMessageId = fromMessageId;
        needSelectFromMessageId = select;

        MessageObject object = messagesDict.get(id);
        boolean query = false;
        if (object != null) {
            int index = messages.indexOf(object);
            if (index != -1) {
                if (needSelectFromMessageId) {
                    highlightMessageId = id;
                } else {
                    highlightMessageId = Integer.MAX_VALUE;
                }
                final int yOffset = Math.max(0, (postListView.getHeight() - object.getApproximateHeight()) / 2);
                if (messages.get(messages.size() - 1) == object) {
                    postLayoutManager.scrollToPositionWithOffset(0, AndroidUtilities.dp(-11) + yOffset);
                } else {
                    postLayoutManager.scrollToPositionWithOffset(messages.size() - messages.indexOf(object), AndroidUtilities.dp(-11) + yOffset);
                }
                updateVisibleRows();
                showPagedownButton(true, true);
            } else {
                query = true;
            }
        } else {
            query = true;
        }

        if (query) {
            messagesDict.clear();
            messages.clear();
            maxMessageId = Integer.MAX_VALUE;
            minMessageId = Integer.MIN_VALUE;
            maxDate = Integer.MIN_VALUE;
            endReached = false;
            loading = false;
            cacheEndReaced = false;
            firstLoading = true;
            loadsCount = 0;
            minDate = 0;
            first = true;
            unread_to_load = 0;
            first_unread_id = 0;
            last_message_id = 0;
            first_message_id = 0;
            forward_end_reached = true;
            loadingForward = false;
            unreadMessageObject = null;
            scrollToMessage = null;
            highlightMessageId = Integer.MAX_VALUE;
            scrollToMessageMiddleScreen = false;
            loading = true;
//            MessagesController.getInstance().loadMessages(dialog_id, AndroidUtilities.isTablet() ? 30 : 20, startLoadFromMessageId, true, 0, classGuid, 3, 0, 0, false);
            postCreateAdapter.notifyDataSetChanged();
            progressView.setVisibility(View.VISIBLE);
            postListView.setEmptyView(null);
            emptyViewContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void showPagedownButton(boolean show, boolean animated) {
        if (pagedownButton == null) {
            return;
        }
        if (show) {
            if (pagedownButton.getVisibility() == View.INVISIBLE) {
                if (animated) {
                    pagedownButton.setVisibility(View.VISIBLE);
                    ViewProxy.setAlpha(pagedownButton, 0);
                    ObjectAnimatorProxy.ofFloatProxy(pagedownButton, "alpha", 1.0f).setDuration(200).start();
                } else {
                    pagedownButton.setVisibility(View.VISIBLE);
                }
            }
        } else {
            returnToMessageId = 0;
            if (pagedownButton.getVisibility() == View.VISIBLE) {
                if (animated) {
                    ObjectAnimatorProxy.ofFloatProxy(pagedownButton, "alpha", 0.0f).setDuration(200).addListener(new AnimatorListenerAdapterProxy() {
                        @Override
                        public void onAnimationEnd(Object animation) {
                            pagedownButton.setVisibility(View.INVISIBLE);
                        }
                    }).start();
                } else {
                    pagedownButton.setVisibility(View.INVISIBLE);
                }
            }
        }
    }


    private void checkActionBarMenu() {

        if (menuItem != null) {
            menuItem.setVisibility(View.VISIBLE);
        }
        if (timeItem != null) {
            timeItem.setVisibility(View.VISIBLE);
        }
        if (timeItem2 != null) {
            timeItem2.setVisibility(View.VISIBLE);
        }


//        if (timerDrawable != null) {
//            timerDrawable.setTime(currentEncryptedChat.ttl);
//        }

        checkAndUpdateAvatar();
    }

    private int updateOnlineCount() {
        if (info == null) {
            return 0;
        }
        onlineCount = 0;
        int currentTime = ConnectionsManager.getInstance().getCurrentTime();
        for (TLRPC.TL_chatParticipant participant : info.participants) {
            TLRPC.User user = MessagesController.getInstance().getUser(participant.user_id);
            if (user != null && user.status != null && (user.status.expires > currentTime || user.id == UserConfig.getClientUserId()) && user.status.expires > 10000) {
                onlineCount++;
            }
        }
        return onlineCount;
    }

    private int getMessageType(MessageObject messageObject) {
        if (messageObject == null) {
            return -1;
        }else {
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
        if (currentChat != null) {
            nameTextView.setText(currentChat.title);
        } else if (currentUser != null) {
            if (currentUser.id / 1000 != 777 && currentUser.id / 1000 != 333 && ContactsController.getInstance().contactsDict.get(currentUser.id) == null && (ContactsController.getInstance().contactsDict.size() != 0 || !ContactsController.getInstance().isLoadingContacts())) {
                if (currentUser.phone != null && currentUser.phone.length() != 0) {
                    nameTextView.setText(PhoneFormat.getInstance().format("+" + currentUser.phone));
                } else {
                    if (currentUser instanceof TLRPC.TL_userDeleted) {
                        nameTextView.setText(LocaleController.getString("HiddenName", R.string.HiddenName));
                    } else {
                        nameTextView.setText(ContactsController.formatName(currentUser.first_name, currentUser.last_name));
                    }
                }
            } else {
                nameTextView.setText(ContactsController.formatName(currentUser.first_name, currentUser.last_name));
            }
        }
    }

    private void updateTitleIcons() {
        int leftIcon = 0;
//        int rightIcon = MessagesController.getInstance().isDialogMuted(dialog_id) ? R.drawable.mute_fixed : 0;
        int rightIcon = 0;
        nameTextView.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, rightIcon, 0);

        if (rightIcon != 0) {
            muteItem.setText(LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications));
        } else {
            muteItem.setText(LocaleController.getString("MuteNotifications", R.string.MuteNotifications));
        }
    }

    private void updateSubtitle() {
        if (onlineTextView == null) {
            return;
        }
        CharSequence printString = "printing";
        printString = TextUtils.replace(printString, new String[]{"..."}, new String[]{""});
        onlineTextView.setText(printString);
    }

    private void checkAndUpdateAvatar() {

        if (avatarImageView != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
//            avatarImageView.setImage(newPhoto, "50_50", avatarDrawable);
//            avatarImageView.setImage("/storage/emulated/0/Telegram/Telegram Images/730111210_6623.jpg", "50_50", avatarDrawable);
            //TODO probably save venueObject. into field in Activity.
            if (avatarImageView != null) {
                avatarImageView.setImage(PostsController.getInstance().getCurrentVenueObject().getVenuePreviewImageUrl(), "50_50", avatarDrawable);
            }
        }
    }


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

                //TODO-temp
//                PhotoViewer.getInstance().openPhotoForSelect(arrayList, 0, 2, new PhotoViewer.EmptyPhotoViewerProvider() {
//                    @Override
//                    public void sendButtonPressed(int index) {
//                        MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) arrayList.get(0);
//                        if (photoEntry.imagePath != null) {
//                            SendMessagesHelper.prepareSendingPhoto(photoEntry.imagePath, null, dialog_id, replyingMessageObject, photoEntry.caption);
//                            showReplyPanel(false, null, null, null, false, true);
//                        } else if (photoEntry.path != null) {
//                            SendMessagesHelper.prepareSendingPhoto(photoEntry.path, null, dialog_id, replyingMessageObject, photoEntry.caption);
//                            showReplyPanel(false, null, null, null, false, true);
//                        }
//                    }
//                }, this);
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
            if (view instanceof ChatMediaCell) {
                ChatMediaCell cell = (ChatMediaCell) view;
                cell.setAllowedToSetPhoto(true);
            }
        }

        if (currentUser != null) {
            MessagesController.getInstance().loadFullUser(MessagesController.getInstance().getUser(currentUser.id), classGuid);
        }
    }

    private void updateBottomOverlay() {
        bottomOverlayChatText.setText(LocaleController.getString("DeleteThisChat", R.string.DeleteThisChat));

        bottomOverlayChat.setVisibility(View.INVISIBLE);
        muteItem.setVisibility(View.VISIBLE);
//        postCreateActivityEnterView.setFieldFocused(false);

    }


    @Override
    public void onResume() {
        super.onResume();

        if (!AndroidUtilities.isTablet()) {
            getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        checkActionBarMenu();

//        NotificationsController.getInstance().setOpennedDialogId(dialog_id);
        if (scrollToTopOnResume) {
            if (scrollToTopUnReadOnResume && scrollToMessage != null) {
                if (postListView != null) {
                    final int yOffset = scrollToMessageMiddleScreen ? Math.max(0, (postListView.getHeight() - scrollToMessage.getApproximateHeight()) / 2) : 0;
                    postLayoutManager.scrollToPositionWithOffset(messages.size() - messages.indexOf(scrollToMessage), -postListView.getPaddingTop() - AndroidUtilities.dp(7) + yOffset);
                }
            } else {
                moveScrollToLastMessage();
            }
            scrollToTopUnReadOnResume = false;
            scrollToTopOnResume = false;
            scrollToMessage = null;
        }
        if (wasPaused) {
            wasPaused = false;
            if (postCreateAdapter != null) {
                postCreateAdapter.notifyDataSetChanged();
            }
        }

        fixLayout(true);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
//        String lastMessageText = preferences.getString("dialog_" + dialog_id, null);
        String lastMessageText = "last printed text";
        if (lastMessageText != null) {
//            preferences.edit().remove("dialog_" + dialog_id).commit();
            postCreateActivityEnterView.setFieldText(lastMessageText);
        }
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
        NotificationsController.getInstance().setOpennedDialogId(0);
        if (postCreateActivityEnterView != null) {
            postCreateActivityEnterView.hideEmojiPopup();
            String text = postCreateActivityEnterView.getFieldText();
            if (text != null) {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                //TODO save text before pause.
//                editor.putString("dialog_" + dialog_id, text);
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

    public void createMenu(View v, boolean single) {
        if (actionBar.isActionModeShowed()) {
            return;
        }

        MessageObject message = null;
        if (v instanceof ChatBaseCell) {
            message = ((ChatBaseCell) v).getMessageObject();
        } else if (v instanceof ChatActionCell) {
            message = ((ChatActionCell) v).getMessageObject();
        }
        if (message == null) {
            return;
        }
        final int type = getMessageType(message);

        selectedObject = null;
        actionBar.hideActionMode();

        if (single || type < 2 || type == 20) {
            if (type >= 0) {
                selectedObject = message;
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
            }
            return;
        }
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
        if (option == 0) {
            if (SendMessagesHelper.getInstance().retrySendMessage(selectedObject, false)) {
                moveScrollToLastMessage();
            }
        } else if (option == 1) {
            final MessageObject finalSelectedObject = selectedObject;
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setMessage(LocaleController.formatString("AreYouSureDeleteMessages", R.string.AreYouSureDeleteMessages, LocaleController.formatPluralString("messages", 1)));
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ArrayList<Integer> ids = new ArrayList<>();
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
            if (view instanceof ChatBaseCell) {
                ChatBaseCell cell = (ChatBaseCell) view;

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

                cell.setMessageObject(cell.getMessageObject());
                cell.setCheckPressed(!disableSelection, disableSelection && selected);
                cell.setHighlighted(highlightMessageId != Integer.MAX_VALUE && cell.getMessageObject() != null && cell.getMessageObject().getId() == highlightMessageId);
            }
        }
    }

    private void alertUserOpenError(MessageObject message) {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        if (message.type == 3) {
            builder.setMessage(LocaleController.getString("NoPlayerInstalled", R.string.NoPlayerInstalled));
        } else {
            builder.setMessage(LocaleController.formatString("NoHandleAppInstalled", R.string.NoHandleAppInstalled, message.messageOwner.media.document.mime_type));
        }
        showDialog(builder.create());
    }

    @Override
    public void updatePhotoAtIndex(int index) {

    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (messageObject == null) {
            return null;
        }
        int count = postListView.getChildCount();

        for (int a = 0; a < count; a++) {
            MessageObject messageToOpen = null;
            ImageReceiver imageReceiver = null;
            View view = postListView.getChildAt(a);
            if (view instanceof ChatMediaCell) {
                ChatMediaCell cell = (ChatMediaCell) view;
                MessageObject message = cell.getMessageObject();
                if (message != null && message.getId() == messageObject.getId()) {
                    messageToOpen = message;
                    imageReceiver = cell.getPhotoImage();
                }
            } else if (view instanceof ChatActionCell) {
                ChatActionCell cell = (ChatActionCell) view;
                MessageObject message = cell.getMessageObject();
                if (message != null && message.getId() == messageObject.getId()) {
                    messageToOpen = message;
                    imageReceiver = cell.getPhotoImage();
                }
            }

            if (messageToOpen != null) {
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
    public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
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

    public class ChatActivityAdapter extends RecyclerView.Adapter {

        private Context mContext;

        public ChatActivityAdapter(Context context) {
            mContext = context;
        }

        private class Holder extends RecyclerView.ViewHolder {

            public Holder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public int getItemCount() {
            int count = messages.size();
            if (count != 0) {
                if (!endReached) {
                    count++;
                }
                if (!forward_end_reached) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public long getItemId(int i) {
            return RecyclerListView.NO_ID;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == 1) {
                if (!chatMediaCellsCache.isEmpty()) {
                    view = chatMediaCellsCache.get(0);
                    chatMediaCellsCache.remove(0);
                } else {
                    view = new ChatMediaCell(mContext);
                }
            }  else if (viewType == 5) {
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.chat_loading_layout, parent, false);
                view.findViewById(R.id.progressLayout).setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_loader2 : R.drawable.system_loader1);
            } else if (viewType == 6) {
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.chat_unread_layout, parent, false);
            }

            if (view instanceof ChatBaseCell) {
                ((ChatBaseCell) view).setDelegate(new ChatBaseCell.ChatBaseCellDelegate() {
                    @Override
                    public void didPressedUserAvatar(ChatBaseCell cell, TLRPC.User user) {

                    }

                    @Override
                    public void didPressedCancelSendButton(ChatBaseCell cell) {

                    }

                    @Override
                    public void didLongPressed(ChatBaseCell cell) {
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
                    public void didPressReplyMessage(ChatBaseCell cell, int id) {
                        scrollToMessageId(id, cell.getMessageObject().getId(), true);
                    }
                });
                if (view instanceof ChatMediaCell) {
                    ((ChatMediaCell) view).setAllowedToSetPhoto(openAnimationEnded);
                    ((ChatMediaCell) view).setMediaDelegate(new ChatMediaCell.ChatMediaCellDelegate() {
                        @Override
                        public void didClickedImage(ChatMediaCell cell) {
                            MessageObject message = cell.getMessageObject();
                            if (message.isSendError()) {
                                createMenu(cell, false);
                                return;
                            } else if (message.isSending()) {
                                return;
                            }
                            if (message.type == 1) {
                                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                                PhotoViewer.getInstance().openPhoto(message, PostCreateActivity.this);
                            } else if (message.type == 4) {
                                if (!isGoogleMapsInstalled()) {
                                    return;
                                }
                                LocationActivity fragment = new LocationActivity();
                                fragment.setMessageObject(message);
                                presentFragment(fragment);
                            }
                        }

                        @Override
                        public void didPressedOther(ChatMediaCell cell) {
                            createMenu(cell, true);
                        }
                    });
                }
            }

            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = holder.getItemViewType();
            if (viewType == 5) {
                holder.itemView.findViewById(R.id.progressLayout).setVisibility(loadsCount > 1 ? View.VISIBLE : View.INVISIBLE);
                return;
            }

            MessageObject message = messages.get(messages.size() - position - (!endReached ? 0 : 1));
            View view = holder.itemView;

            int type = message.contentType;

            boolean selected = false;
            boolean disableSelection = false;
            if (actionBar.isActionModeShowed()) {
                view.setBackgroundColor(0);
                disableSelection = true;
            } else {
                view.setBackgroundColor(0);
            }

            if (view instanceof ChatBaseCell) {
                ChatBaseCell baseCell = (ChatBaseCell) view;
                baseCell.isChat = currentChat != null;
                baseCell.setMessageObject(message);
                baseCell.setCheckPressed(!disableSelection, disableSelection && selected);
                if (view instanceof ChatAudioCell && MediaController.getInstance().canDownloadMedia(MediaController.AUTODOWNLOAD_MASK_AUDIO)) {
                    ((ChatAudioCell) view).downloadAudioIfNeed();
                }
                baseCell.setHighlighted(highlightMessageId != Integer.MAX_VALUE && message.getId() == highlightMessageId);
            } else if (view instanceof ChatActionCell) {
                ChatActionCell actionCell = (ChatActionCell) view;
                actionCell.setMessageObject(message);
            } else if (type == 6) {
                TextView messageTextView = (TextView) view.findViewById(R.id.chat_message_text);
                messageTextView.setText(LocaleController.formatPluralString("NewMessages", unread_to_load));
            }
        }

        @Override
        public int getItemViewType(int position) {
            int offset = 1;
            if (!endReached && messages.size() != 0) {
                offset = 0;
                if (position == 0) {
                    return 5;
                }
            }
            if (!forward_end_reached && position == (messages.size() + 1 - offset)) {
                return 5;
            }
            MessageObject message = messages.get(messages.size() - position - offset);
            return message.contentType;
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (holder.itemView instanceof ChatBaseCell) {
                ChatBaseCell baseCell = (ChatBaseCell) holder.itemView;
                baseCell.setHighlighted(highlightMessageId != Integer.MAX_VALUE && baseCell.getMessageObject().getId() == highlightMessageId);
            }
            if (holder.itemView instanceof ChatMessageCell) {
                final ChatMessageCell messageCell = (ChatMessageCell) holder.itemView;
                messageCell.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        messageCell.getViewTreeObserver().removeOnPreDrawListener(this);
                        messageCell.getLocalVisibleRect(scrollRect);
                        messageCell.setVisiblePart(scrollRect.top, scrollRect.bottom - scrollRect.top);
                        return true;
                    }
                });
            }
        }

        public void updateRowWithMessageObject(MessageObject messageObject) {
            int index = messages.indexOf(messageObject);
            if (index == -1) {
                return;
            }
            notifyItemChanged(messages.size() - (!endReached ? 0 : 1) - index);
        }

        public void removeMessageObject(MessageObject messageObject) {
            int index = messages.indexOf(messageObject);
            if (index == -1) {
                return;
            }
            messages.remove(index);
            notifyItemRemoved(messages.size() - (!endReached ? 0 : 1) - index);
        }
    }
}
