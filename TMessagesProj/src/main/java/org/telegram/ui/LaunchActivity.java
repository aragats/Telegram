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
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.Adapters.DrawerLayoutAdapter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.utils.CollectionUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import ru.aragats.wgo.ApplicationLoader;
import ru.aragats.wgo.R;

public class LaunchActivity extends Activity implements ActionBarLayout.ActionBarLayoutDelegate, NotificationCenter.NotificationCenterDelegate {
    private boolean finished;
    private String sendingText;
    private ArrayList<Uri> photoPathsArray; //TODO for share images.
    private int currentConnectionState;
    private static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList<>();
    private static ArrayList<BaseFragment> layerFragmentsStack = new ArrayList<>();
    private static ArrayList<BaseFragment> rightFragmentsStack = new ArrayList<>();

    private ActionBarLayout actionBarLayout;
    private ActionBarLayout layersActionBarLayout;
    private ActionBarLayout rightActionBarLayout;
    private FrameLayout shadowTablet;
    private FrameLayout shadowTabletSide;
    private ImageView backgroundTablet;
    protected DrawerLayoutContainer drawerLayoutContainer;
    private DrawerLayoutAdapter drawerLayoutAdapter;
    private AlertDialog visibleDialog;


    private boolean tabletFullSize;

    private Runnable lockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApplicationLoader.postInitApplication();

        if (!UserConfig.isClientActivated()) {
            Intent intent = getIntent();
            if (intent != null && intent.getAction() != null && (Intent.ACTION_SEND.equals(intent.getAction()) || intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE))) {
                super.onCreate(savedInstanceState);
                finish();
                return;
            }
            if (intent != null && !intent.getBooleanExtra("fromIntro", false)) {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("logininfo", MODE_PRIVATE);
                Map<String, ?> state = preferences.getAll();
                if (state.isEmpty()) {
                    Intent intent2 = new Intent(this, IntroActivity.class);
                    startActivity(intent2);
                    super.onCreate(savedInstanceState);
                    finish();
                    return;
                }
            }
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.Theme_TMessages);
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);


        super.onCreate(savedInstanceState);

//        if (UserConfig.appLocked) {
//            UserConfig.lastPauseTime = ConnectionsManager.getInstance().getCurrentTime();
//        }

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            AndroidUtilities.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        actionBarLayout = new ActionBarLayout(this);

        drawerLayoutContainer = new DrawerLayoutContainer(this);
        setContentView(drawerLayoutContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (AndroidUtilities.isTablet()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            RelativeLayout launchLayout = new RelativeLayout(this);
            drawerLayoutContainer.addView(launchLayout);
            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) launchLayout.getLayoutParams();
            layoutParams1.width = LayoutHelper.MATCH_PARENT;
            layoutParams1.height = LayoutHelper.MATCH_PARENT;
            launchLayout.setLayoutParams(layoutParams1);

            backgroundTablet = new ImageView(this);
            backgroundTablet.setScaleType(ImageView.ScaleType.CENTER_CROP);
            backgroundTablet.setImageResource(R.drawable.cats);
            launchLayout.addView(backgroundTablet);
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) backgroundTablet.getLayoutParams();
            relativeLayoutParams.width = LayoutHelper.MATCH_PARENT;
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            backgroundTablet.setLayoutParams(relativeLayoutParams);

            launchLayout.addView(actionBarLayout);
            relativeLayoutParams = (RelativeLayout.LayoutParams) actionBarLayout.getLayoutParams();
            relativeLayoutParams.width = LayoutHelper.MATCH_PARENT;
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            actionBarLayout.setLayoutParams(relativeLayoutParams);

            rightActionBarLayout = new ActionBarLayout(this);
            launchLayout.addView(rightActionBarLayout);
            relativeLayoutParams = (RelativeLayout.LayoutParams) rightActionBarLayout.getLayoutParams();
            relativeLayoutParams.width = AndroidUtilities.dp(320);
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            rightActionBarLayout.setLayoutParams(relativeLayoutParams);
            rightActionBarLayout.init(rightFragmentsStack);
            rightActionBarLayout.setDelegate(this);

            shadowTabletSide = new FrameLayout(this);
            shadowTabletSide.setBackgroundColor(0x40295274);
            launchLayout.addView(shadowTabletSide);
            relativeLayoutParams = (RelativeLayout.LayoutParams) shadowTabletSide.getLayoutParams();
            relativeLayoutParams.width = AndroidUtilities.dp(1);
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            shadowTabletSide.setLayoutParams(relativeLayoutParams);

            shadowTablet = new FrameLayout(this);
            shadowTablet.setVisibility(View.GONE);
            shadowTablet.setBackgroundColor(0x7F000000);
            launchLayout.addView(shadowTablet);
            relativeLayoutParams = (RelativeLayout.LayoutParams) shadowTablet.getLayoutParams();
            relativeLayoutParams.width = LayoutHelper.MATCH_PARENT;
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            shadowTablet.setLayoutParams(relativeLayoutParams);
            shadowTablet.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (!actionBarLayout.fragmentsStack.isEmpty() && event.getAction() == MotionEvent.ACTION_UP) {
                        float x = event.getX();
                        float y = event.getY();
                        int location[] = new int[2];
                        layersActionBarLayout.getLocationOnScreen(location);
                        int viewX = location[0];
                        int viewY = location[1];

                        if (layersActionBarLayout.checkTransitionAnimation() || x > viewX && x < viewX + layersActionBarLayout.getWidth() && y > viewY && y < viewY + layersActionBarLayout.getHeight()) {
                            return false;
                        } else {
                            if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                                for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                                    layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                                    a--;
                                }
                                layersActionBarLayout.closeLastFragment(true);
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });

            shadowTablet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            layersActionBarLayout = new ActionBarLayout(this);
            layersActionBarLayout.setRemoveActionBarExtraHeight(true);
            layersActionBarLayout.setBackgroundView(shadowTablet);
            layersActionBarLayout.setUseAlphaAnimations(true);
            layersActionBarLayout.setBackgroundResource(R.drawable.boxshadow);
            launchLayout.addView(layersActionBarLayout);
            relativeLayoutParams = (RelativeLayout.LayoutParams) layersActionBarLayout.getLayoutParams();
            relativeLayoutParams.width = AndroidUtilities.dp(530);
            relativeLayoutParams.height = AndroidUtilities.dp(528);
            layersActionBarLayout.setLayoutParams(relativeLayoutParams);
            layersActionBarLayout.init(layerFragmentsStack);
            layersActionBarLayout.setDelegate(this);
            layersActionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);
            layersActionBarLayout.setVisibility(View.GONE);
        } else {
            drawerLayoutContainer.addView(actionBarLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        ListView listView = new ListView(this);
        listView.setAdapter(drawerLayoutAdapter = new DrawerLayoutAdapter(this));
        drawerLayoutContainer.setDrawerLayout(listView);
        listView.setBackgroundColor(0xffffffff);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
        Point screenSize = AndroidUtilities.getRealScreenSize();
        layoutParams.width = AndroidUtilities.isTablet() ? AndroidUtilities.dp(320) : Math.min(screenSize.x, screenSize.y) - AndroidUtilities.dp(56);
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        listView.setPadding(0, 0, 0, 0);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setLayoutParams(layoutParams);
        listView.setVerticalScrollBarEnabled(false);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 2) {
                    //TODO-aragats
                    //TODO Update opened PostsActivity and close drawer.
//                    Bundle args = new Bundle();
//                    presentFragment(new PostsActivity(args));
                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 3) {

                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, ContactsController.getInstance().getInviteText());
                        startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteFriends", R.string.InviteFriends)), 500);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                    drawerLayoutContainer.closeDrawer(false);

                } else if (position == 5) {
                    try {
                        Intent pickIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(LocaleController.getString("TelegramFaqUrl", R.string.TelegramFaqUrl)));
                        startActivityForResult(pickIntent, 500);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                    drawerLayoutContainer.closeDrawer(false);
                }
            }
        });

        drawerLayoutContainer.setParentActionBarLayout(actionBarLayout);
        actionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);
        actionBarLayout.init(mainFragmentsStack);
        actionBarLayout.setDelegate(this);

        ApplicationLoader.loadWallpaper();

        NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeOtherAppActivities, this);
        currentConnectionState = ConnectionsManager.getInstance().getConnectionState();

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeOtherAppActivities);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didUpdatedConnectionState);
        if (Build.VERSION.SDK_INT < 14) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.screenStateChanged);
        }

        if (actionBarLayout.fragmentsStack.isEmpty()) {
            if (!UserConfig.isClientActivated()) {
//                actionBarLayout.addFragmentToStack(new LoginActivity());
//                drawerLayoutContainer.setAllowOpenDrawer(false, false);
            } else {
                actionBarLayout.addFragmentToStack(new PostsActivity(null));
                drawerLayoutContainer.setAllowOpenDrawer(true, false);
            }

            try {
                if (savedInstanceState != null) {
                    String fragmentName = savedInstanceState.getString("fragment");
                    if (fragmentName != null) {
                        Bundle args = savedInstanceState.getBundle("args");
                        switch (fragmentName) {
//                            case "chat":
//                                if (args != null) {
//                                    ChatActivity chat = new ChatActivity(args);
//                                    if (actionBarLayout.addFragmentToStack(chat)) {
//                                        chat.restoreSelfArgs(savedInstanceState);
//                                    }
//                                }
//                                break;
                        }
                    }
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        } else {
            boolean allowOpen = true;
//            if (AndroidUtilities.isTablet()) {
//                allowOpen = actionBarLayout.fragmentsStack.size() <= 1 && layersActionBarLayout.fragmentsStack.isEmpty();
//                if (layersActionBarLayout.fragmentsStack.size() == 1 && layersActionBarLayout.fragmentsStack.get(0) instanceof LoginActivity) {
//                    allowOpen = false;
//                }
//            }
//            if (actionBarLayout.fragmentsStack.size() == 1 && actionBarLayout.fragmentsStack.get(0) instanceof LoginActivity) {
//                allowOpen = false;
//            }
            drawerLayoutContainer.setAllowOpenDrawer(allowOpen, false);
        }

        handleIntent(getIntent(), false, savedInstanceState != null);
        needLayout();
    }


    private boolean handleIntent(Intent intent, boolean isNew, boolean restore) {
        int flags = intent.getFlags();
        boolean pushOpened = false;

        boolean showDialogsList = false;

        photoPathsArray = null;
        sendingText = null;

        if (UserConfig.isClientActivated() && (flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            if (intent != null && intent.getAction() != null && !restore) {
                if (Intent.ACTION_SEND.equals(intent.getAction())) {
                    boolean error = false;
                    String type = intent.getType();
                    if (type != null && (type.equals("text/plain") || type.equals("message/rfc822")) && (intent.getStringExtra(Intent.EXTRA_TEXT) != null || intent.getCharSequenceExtra(Intent.EXTRA_TEXT) != null)) {
                        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                        if (text == null) {
                            text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT).toString(); // TODO possible NPE ??
                        }
                        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

                        if (text != null && text.length() != 0) {
                            if ((text.startsWith("http://") || text.startsWith("https://")) && subject != null && subject.length() != 0) {
                                text = subject + "\n" + text;
                            }
                            sendingText = text;
                        } else {
                            error = true;
                        }
                    }
                    Parcelable parcelable = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (parcelable != null) {
                        String path;
                        if (!(parcelable instanceof Uri)) {
                            parcelable = Uri.parse(parcelable.toString());
                        }
                        Uri uri = (Uri) parcelable;
                        if (uri != null && (type != null && type.startsWith("image/") || uri.toString().toLowerCase().endsWith(".jpg"))) {
                            String tempPath = AndroidUtilities.getPath(uri);
                            if (photoPathsArray == null) {
                                photoPathsArray = new ArrayList<>();
                            }
                            photoPathsArray.add(uri); //for image
                        }
                    } else if (sendingText == null) {
                        error = true;
                    }

                    if (error) {
                        Toast.makeText(this, "Unsupported content", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        if (showDialogsList) { // false
            if (!AndroidUtilities.isTablet()) {
                actionBarLayout.removeAllFragments();
            } else {
                if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                    for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                        layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                        a--;
                    }
                    layersActionBarLayout.closeLastFragment(false);
                }
            }
            pushOpened = false;
            isNew = false;
        } else if (photoPathsArray != null || sendingText != null) {
            if (!AndroidUtilities.isTablet()) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
            }
            Bundle args = new Bundle();
            args.putString("text", sendingText);
            args.putString("image", CollectionUtils.isEmpty(photoPathsArray) ? null : AndroidUtilities.getPath(photoPathsArray.get(0)));
            PostCreateActivity fragment = new PostCreateActivity(args);
//            fragment.setDelegate(this);
            boolean removeLast = false;
            if (AndroidUtilities.isTablet()) {
                removeLast = layersActionBarLayout.fragmentsStack.size() > 0 && layersActionBarLayout.fragmentsStack.get(layersActionBarLayout.fragmentsStack.size() - 1) instanceof PostCreateActivity;
            } else {
                removeLast = actionBarLayout.fragmentsStack.size() > 1 && actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1) instanceof PostCreateActivity;
            }
            actionBarLayout.presentFragment(fragment, removeLast, true, true);
            pushOpened = true;
            if (PhotoViewer.getInstance().isVisible()) {
                PhotoViewer.getInstance().closePhoto(false, true);
            }

            drawerLayoutContainer.setAllowOpenDrawer(false, false);
            if (AndroidUtilities.isTablet()) {
                actionBarLayout.showLastFragment();
                rightActionBarLayout.showLastFragment();
            } else {
                drawerLayoutContainer.setAllowOpenDrawer(true, false);
            }
        }
        if (!pushOpened && !isNew) {
            if (AndroidUtilities.isTablet()) {
                if (!UserConfig.isClientActivated()) {
                    if (layersActionBarLayout.fragmentsStack.isEmpty()) {
//                            layersActionBarLayout.addFragmentToStack(new LoginActivity());
//                            drawerLayoutContainer.setAllowOpenDrawer(false, false);
                    }
                } else {
                    if (actionBarLayout.fragmentsStack.isEmpty()) {
                        actionBarLayout.addFragmentToStack(new PostsActivity(null));
                        drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    }
                }
            } else {
                if (actionBarLayout.fragmentsStack.isEmpty()) {
                    if (!UserConfig.isClientActivated()) {
//                            actionBarLayout.addFragmentToStack(new LoginActivity());
//                            drawerLayoutContainer.setAllowOpenDrawer(false, false);
                    } else {
                        actionBarLayout.addFragmentToStack(new PostsActivity(null));
                        drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    }
                }
            }
            actionBarLayout.showLastFragment();
            if (AndroidUtilities.isTablet()) {
                layersActionBarLayout.showLastFragment();
                rightActionBarLayout.showLastFragment();
            }
        }

        intent.setAction(null);
        return pushOpened;
    }


    public AlertDialog showAlertDialog(AlertDialog.Builder builder) {
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        try {
            visibleDialog = builder.show();
            visibleDialog.setCanceledOnTouchOutside(true);
            visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    visibleDialog = null;
                }
            });
            return visibleDialog;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, true, false);
    }

//    @Override
//    public void didSelectDialog(MessagesActivity messageFragment, long dialog_id, boolean param) {
//
//    }

    private void onFinish() {
        if (finished) {
            return;
        }
        finished = true;
        if (lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(lockRunnable);
            lockRunnable = null;
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeOtherAppActivities);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didUpdatedConnectionState);
        if (Build.VERSION.SDK_INT < 14) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.screenStateChanged);
        }
    }

    public void presentFragment(BaseFragment fragment) {
        actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(final BaseFragment fragment, final boolean removeLast, boolean forceWithoutAnimation) {
        return actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true);
    }

    public void needLayout() {
        if (AndroidUtilities.isTablet()) {

            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) layersActionBarLayout.getLayoutParams();
            relativeLayoutParams.leftMargin = (AndroidUtilities.displaySize.x - relativeLayoutParams.width) / 2;
            int y = (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
            relativeLayoutParams.topMargin = y + (AndroidUtilities.displaySize.y - relativeLayoutParams.height - y) / 2;
            layersActionBarLayout.setLayoutParams(relativeLayoutParams);

            if (!AndroidUtilities.isSmallTablet() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                tabletFullSize = false;
                int leftWidth = AndroidUtilities.displaySize.x / 100 * 35;
                if (leftWidth < AndroidUtilities.dp(320)) {
                    leftWidth = AndroidUtilities.dp(320);
                }

                relativeLayoutParams = (RelativeLayout.LayoutParams) actionBarLayout.getLayoutParams();
                relativeLayoutParams.width = leftWidth;
                relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
                actionBarLayout.setLayoutParams(relativeLayoutParams);

                relativeLayoutParams = (RelativeLayout.LayoutParams) shadowTabletSide.getLayoutParams();
                relativeLayoutParams.leftMargin = leftWidth;
                shadowTabletSide.setLayoutParams(relativeLayoutParams);

                relativeLayoutParams = (RelativeLayout.LayoutParams) rightActionBarLayout.getLayoutParams();
                relativeLayoutParams.width = AndroidUtilities.displaySize.x - leftWidth;
                relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
                relativeLayoutParams.leftMargin = leftWidth;
                rightActionBarLayout.setLayoutParams(relativeLayoutParams);

                if (AndroidUtilities.isSmallTablet() && actionBarLayout.fragmentsStack.size() == 2) {
                    BaseFragment chatFragment = actionBarLayout.fragmentsStack.get(1);
                    chatFragment.onPause();
                    actionBarLayout.fragmentsStack.remove(1);
                    rightActionBarLayout.fragmentsStack.add(chatFragment);
                }

                rightActionBarLayout.setVisibility(rightActionBarLayout.fragmentsStack.isEmpty() ? View.GONE : View.VISIBLE);
                backgroundTablet.setVisibility(rightActionBarLayout.fragmentsStack.isEmpty() ? View.VISIBLE : View.GONE);
                shadowTabletSide.setVisibility(!actionBarLayout.fragmentsStack.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                tabletFullSize = true;

                relativeLayoutParams = (RelativeLayout.LayoutParams) actionBarLayout.getLayoutParams();
                relativeLayoutParams.width = LayoutHelper.MATCH_PARENT;
                relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
                actionBarLayout.setLayoutParams(relativeLayoutParams);

                shadowTabletSide.setVisibility(View.GONE);
                rightActionBarLayout.setVisibility(View.GONE);
                backgroundTablet.setVisibility(!actionBarLayout.fragmentsStack.isEmpty() ? View.GONE : View.VISIBLE);

                if (rightActionBarLayout.fragmentsStack.size() == 1) {
                    BaseFragment chatFragment = rightActionBarLayout.fragmentsStack.get(0);
                    chatFragment.onPause();
                    rightActionBarLayout.fragmentsStack.remove(0);
                    actionBarLayout.addFragmentToStack(chatFragment);
                }
            }
        }
    }

    public void fixLayout() {
        if (AndroidUtilities.isTablet()) {
            if (actionBarLayout == null) {
                return;
            }
            actionBarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    needLayout();
                    if (actionBarLayout != null) {
                        if (Build.VERSION.SDK_INT < 16) {
                            actionBarLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            actionBarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (actionBarLayout.fragmentsStack.size() != 0) {
            BaseFragment fragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
            fragment.onActivityResultFragment(requestCode, resultCode, data);
        }
        if (AndroidUtilities.isTablet()) {
            if (rightActionBarLayout.fragmentsStack.size() != 0) {
                BaseFragment fragment = rightActionBarLayout.fragmentsStack.get(rightActionBarLayout.fragmentsStack.size() - 1);
                fragment.onActivityResultFragment(requestCode, resultCode, data);
            }
            if (layersActionBarLayout.fragmentsStack.size() != 0) {
                BaseFragment fragment = layersActionBarLayout.fragmentsStack.get(layersActionBarLayout.fragmentsStack.size() - 1);
                fragment.onActivityResultFragment(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        actionBarLayout.onPause();
        if (AndroidUtilities.isTablet()) {
            rightActionBarLayout.onPause();
            layersActionBarLayout.onPause();
        }
        ApplicationLoader.mainInterfacePaused = true;
        ConnectionsManager.getInstance().setAppPaused(true, false);
        AndroidUtilities.unregisterUpdates();
    }

    @Override
    protected void onDestroy() {
        PhotoViewer.getInstance().destroyPhotoViewer();
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        super.onDestroy();
        onFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBarLayout.onResume();
        if (AndroidUtilities.isTablet()) {
            rightActionBarLayout.onResume();
            layersActionBarLayout.onResume();
        }
        //TODO-CONFIG HOCKEY_APP_HASH
        AndroidUtilities.checkForCrashes(this);
        AndroidUtilities.checkForUpdates(this);
        ApplicationLoader.mainInterfacePaused = false;
        ConnectionsManager.getInstance().setAppPaused(false, false);
        updateCurrentConnectionState();
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onResume();
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        AndroidUtilities.checkDisplaySize();
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.closeOtherAppActivities) {
            if (args[0] != this) {
                onFinish();
                finish();
            }
        } else if (id == NotificationCenter.didUpdatedConnectionState) {
            int state = (Integer) args[0];
            if (currentConnectionState != state) {
                FileLog.e("tmessages", "switch to state " + state);
                currentConnectionState = state;
                updateCurrentConnectionState();
            }
        } else if (id == NotificationCenter.screenStateChanged) {
            if (!ApplicationLoader.mainInterfacePaused) {
                if (!ApplicationLoader.isScreenOn) {
//                    onPasscodePause();
                } else {
//                    onPasscodeResume();
                }
            }
        }
    }


    private void updateCurrentConnectionState() {
        String text = null;
        if (currentConnectionState == 1) {
            text = LocaleController.getString("WaitingForNetwork", R.string.WaitingForNetwork);
        } else if (currentConnectionState == 2) {
            text = LocaleController.getString("Connecting", R.string.Connecting);
        } else if (currentConnectionState == 3) {
            text = LocaleController.getString("Updating", R.string.Updating);
        }
        actionBarLayout.setTitleOverlayText(text);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            BaseFragment lastFragment = null;
            if (AndroidUtilities.isTablet()) {
                if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = layersActionBarLayout.fragmentsStack.get(layersActionBarLayout.fragmentsStack.size() - 1);
                } else if (!rightActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = rightActionBarLayout.fragmentsStack.get(rightActionBarLayout.fragmentsStack.size() - 1);
                } else if (!actionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
                }
            } else {
                if (!actionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
                }
            }

            if (lastFragment != null) {
                Bundle args = lastFragment.getArguments();
//                if (lastFragment instanceof ChatActivity && args != null) {
//                    outState.putBundle("args", args);
//                    outState.putString("fragment", "chat");
//                }
//                lastFragment.saveSelfArgs(outState);
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    @Override
    public void onBackPressed() {
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
        } else if (drawerLayoutContainer.isDrawerOpened()) {
            drawerLayoutContainer.closeDrawer(false);
        } else if (AndroidUtilities.isTablet()) {
            if (layersActionBarLayout.getVisibility() == View.VISIBLE) {
                layersActionBarLayout.onBackPressed();
            } else {
                boolean cancel = false;
                if (rightActionBarLayout.getVisibility() == View.VISIBLE && !rightActionBarLayout.fragmentsStack.isEmpty()) {
                    BaseFragment lastFragment = rightActionBarLayout.fragmentsStack.get(rightActionBarLayout.fragmentsStack.size() - 1);
                    cancel = !lastFragment.onBackPressed();
                }
                if (!cancel) {
                    actionBarLayout.onBackPressed();
                }
            }
        } else {
            actionBarLayout.onBackPressed();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        actionBarLayout.onLowMemory();
        if (AndroidUtilities.isTablet()) {
            rightActionBarLayout.onLowMemory();
            layersActionBarLayout.onLowMemory();
        }
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        actionBarLayout.onActionModeStarted(mode);
        if (AndroidUtilities.isTablet()) {
            rightActionBarLayout.onActionModeStarted(mode);
            layersActionBarLayout.onActionModeStarted(mode);
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        actionBarLayout.onActionModeFinished(mode);
        if (AndroidUtilities.isTablet()) {
            rightActionBarLayout.onActionModeFinished(mode);
            layersActionBarLayout.onActionModeFinished(mode);
        }
    }

    @Override
    public boolean onPreIme() {
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (AndroidUtilities.isTablet()) {
                if (layersActionBarLayout.getVisibility() == View.VISIBLE && !layersActionBarLayout.fragmentsStack.isEmpty()) {
                    layersActionBarLayout.onKeyUp(keyCode, event);
                } else if (rightActionBarLayout.getVisibility() == View.VISIBLE && !rightActionBarLayout.fragmentsStack.isEmpty()) {
                    rightActionBarLayout.onKeyUp(keyCode, event);
                } else {
                    actionBarLayout.onKeyUp(keyCode, event);
                }
            } else {
                if (actionBarLayout.fragmentsStack.size() == 1) {
                    if (!drawerLayoutContainer.isDrawerOpened()) {
                        if (getCurrentFocus() != null) {
                            AndroidUtilities.hideKeyboard(getCurrentFocus());
                        }
                        drawerLayoutContainer.openDrawer(false);
                    } else {
                        drawerLayoutContainer.closeDrawer(false);
                    }
                } else {
                    actionBarLayout.onKeyUp(keyCode, event);
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean needPresentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation, ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
//            drawerLayoutContainer.setAllowOpenDrawer(!(fragment instanceof LoginActivity) && layersActionBarLayout.getVisibility() != View.VISIBLE, true);
            drawerLayoutContainer.setAllowOpenDrawer(true, true);
            if (fragment instanceof PostsActivity) {
                PostsActivity postsActivity = (PostsActivity) fragment;
//                if (postsActivity.isMainDialogList() && layout != actionBarLayout) {
                if (layout != actionBarLayout) {
                    actionBarLayout.removeAllFragments();
                    actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, false);
                    layersActionBarLayout.removeAllFragments();
                    layersActionBarLayout.setVisibility(View.GONE);
                    drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    if (!tabletFullSize) {
                        shadowTabletSide.setVisibility(View.VISIBLE);
                        if (rightActionBarLayout.fragmentsStack.isEmpty()) {
                            backgroundTablet.setVisibility(View.VISIBLE);
                        }
                    }
                    return false;
                }
            }
            return true;
        } else {
//            drawerLayoutContainer.setAllowOpenDrawer(!(fragment instanceof LoginActivity), false);
            drawerLayoutContainer.setAllowOpenDrawer(true, false);
            return true;
        }
    }

    @Override
    public boolean needAddFragmentToStack(BaseFragment fragment, ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
//            drawerLayoutContainer.setAllowOpenDrawer(!(fragment instanceof LoginActivity) && layersActionBarLayout.getVisibility() != View.VISIBLE, true);
            drawerLayoutContainer.setAllowOpenDrawer(true, true);
            if (fragment instanceof PostsActivity) {
                PostsActivity postsActivity = (PostsActivity) fragment;
//                if (postsActivity.isMainDialogList() && layout != actionBarLayout) {
                if (layout != actionBarLayout) {
                    actionBarLayout.removeAllFragments();
                    actionBarLayout.addFragmentToStack(fragment);
                    layersActionBarLayout.removeAllFragments();
                    layersActionBarLayout.setVisibility(View.GONE);
                    drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    if (!tabletFullSize) {
                        shadowTabletSide.setVisibility(View.VISIBLE);
                        if (rightActionBarLayout.fragmentsStack.isEmpty()) {
                            backgroundTablet.setVisibility(View.VISIBLE);
                        }
                    }
                    return false;
                }
            }
            return true;
        } else {
//            drawerLayoutContainer.setAllowOpenDrawer(!(fragment instanceof LoginActivity), false);
            drawerLayoutContainer.setAllowOpenDrawer(true, false);
            return true;
        }
    }

    @Override
    public boolean needCloseLastFragment(ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (layout == actionBarLayout && layout.fragmentsStack.size() <= 1) {
                onFinish();
                finish();
                return false;
            } else if (layout == rightActionBarLayout) {
                if (!tabletFullSize) {
                    backgroundTablet.setVisibility(View.VISIBLE);
                }
            } else if (layout == layersActionBarLayout && actionBarLayout.fragmentsStack.isEmpty() && layersActionBarLayout.fragmentsStack.size() == 1) {
                onFinish();
                finish();
                return false;
            }
        } else {
            if (layout.fragmentsStack.size() <= 1) {
                onFinish();
                finish();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRebuildAllFragments(ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (layout == layersActionBarLayout) {
                rightActionBarLayout.rebuildAllFragmentViews(true);
                rightActionBarLayout.showLastFragment();
                actionBarLayout.rebuildAllFragmentViews(true);
                actionBarLayout.showLastFragment();
            }
        }
        drawerLayoutAdapter.notifyDataSetChanged();
    }
}
