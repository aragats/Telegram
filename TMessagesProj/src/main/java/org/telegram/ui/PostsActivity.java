/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.android.AnimationCompat.ViewProxy;
import org.telegram.android.ContactsController;
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.MediaController;
import org.telegram.android.NotificationCenter;
import org.telegram.android.PostsController;
import org.telegram.android.location.LocationManagerHelper;
import org.telegram.android.support.widget.LinearLayoutManager;
import org.telegram.android.support.widget.RecyclerView;

import ru.aragats.wgo.ApplicationLoader;
import ru.aragats.wgo.R;

import org.telegram.messenger.FileLog;

import ru.aragats.wgo.dto.Coordinates;
import ru.aragats.wgo.dto.Post;

import org.telegram.messenger.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.Adapters.PostsAdapter;
import org.telegram.ui.Adapters.PostsSearchAdapter;
import org.telegram.ui.Cells.PostCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.utils.Constants;
import org.telegram.utils.StringUtils;

import java.util.Arrays;

//TODO delte it or reuse.

//TODO refresh list https://www.bignerdranch.com/blog/implementing-swipe-to-refresh/
/*
TODO-aragats
 */
public class PostsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {

    private int mLastFirstVisibleItem;
    private boolean mIsScrollingUp;


    private RecyclerListView postListView;
    private LinearLayoutManager layoutManager;
    private PostsAdapter postsAdapter;
    private PostsSearchAdapter postsSearchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private ProgressBar progressView;
    private LinearLayout emptyView;
    private ImageView floatingButton;
    private TextView textViewForEmptyView;


    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private static boolean postsLoaded;
    private boolean searching;
    private boolean searchWas;
    private String selectedPost;
    private String searchString;
    private String openedPostId;

    // Swipe Refresh Layout
    private SwipeRefreshLayout swipeRefreshLayout;
    //handler
    private Handler handler = new Handler();

    private Runnable refreshProgressRun = new Runnable() {
        @Override
        public void run() {
            if (PostsController.getInstance().isLoadingPosts() && swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }
    };

    private LocationActivityAragats.LocationActivityDelegate locationActivityDelegate;


    private ActionBarMenuItem locationItem;
    private static int itemId = 1;

    private final static int list_menu_synchronize = itemId++;
    private final static int list_menu_map = itemId++;
    private final static int action_bar_menu_search = itemId++;
    private final static int action_bar_menu_location = itemId++;
    private final static int action_bar_menu_other = itemId++;

    private boolean offlineMode;

    //TODO-legacy. update according to new version.
    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(Object post, int index) {
        if (post == null || !(post instanceof Post)) {
            return null;
        }
        int count = this.postListView.getChildCount();

        for (int a = 0; a < count; a++) {
            Post postToOpen = null;
            ImageReceiver imageReceiver = null;
            View view = this.postListView.getChildAt(a);
            if (view instanceof PostCell) {
                PostCell cell = (PostCell) view;
                Post cellPost = cell.getPost();
                if (cellPost != null && cellPost.getId() != null && cellPost.getId().equals(((Post) post).getId())) {
                    postToOpen = cellPost;
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

    @Override
    public void updatePhotoAtIndex(int index) {

    }


    public PostsActivity(Bundle args) {
        super(args);
        if (args != null) {
            offlineMode = args.getBoolean("offlineMode", false);
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        if (getArguments() != null) {
            //example of retrieving arguments.
        }

        if (searchString == null) {
            //TODO learn NotificationCenter class especiallu case when post notification. There is different situations when notify when animation or not.
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.switchToOfflineMode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.switchToOnlineMode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.stopRefreshingView);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.offlinePostsLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.postsRefresh);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.undefinedLocation);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.locationServiceDisabled);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.postRequestFinished);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.postsNeedReload);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        }

        if (offlineMode) {
            MediaController.loadGeoTaggedGalleryPhotos(classGuid, false);
        }
        LocationManagerHelper.getInstance().runLocationListener();

        if (!offlineMode && !postsLoaded) {
            PostsController.getInstance().loadPosts(null, 0, Constants.POST_COUNT, true, offlineMode);
            ContactsController.getInstance().checkInviteText();
            postsLoaded = true;
        }
        if (!LocationManagerHelper.getInstance().isLocationServiceEnabled()) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.locationServiceDisabled);
        }

        locationActivityDelegate = new LocationActivityAragats.LocationActivityDelegate() {

            @Override
            public void didSelectLocation(TLRPC.MessageMedia location) {
                //TODO I think i do not need last saved location. I can just put "network" provider into Location isntance
                Location lastSavedLocation = LocationManagerHelper.getInstance().getLastSavedOrLastLocation();
                if (!location.isCustomLocation || lastSavedLocation == null) {
                    locationItem.setIcon(R.drawable.ic_attach_location_grey);
                    LocationManagerHelper.getInstance().setCustomLocation(null);
                    // TODO save additional request if the same coordinates are chosen ?? But man can move. so current location could change
                    refreshPosts(true);
                    return;
                }
                Location customLocation = new Location(lastSavedLocation.getProvider());
                double lat = location.geo.lat;
                double lng = location.geo._long;
                if (location.geoPlace != null) {
                    lat = location.geoPlace.lat;
                    lng = location.geoPlace._long;
                }
                customLocation.setLatitude(lat);
                customLocation.setLongitude(lng);
                LocationManagerHelper.getInstance().setCustomLocation(customLocation);
                locationItem.setIcon(R.drawable.ic_attach_location_white);
                //                R.drawable.location_b; R.drawable.location_g
                refreshPosts(true);
            }
        };


        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (searchString == null) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.switchToOfflineMode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.switchToOnlineMode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.stopRefreshingView);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.offlinePostsLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.postsRefresh);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.undefinedLocation);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.locationServiceDisabled);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.postRequestFinished);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.postsNeedReload);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        }
        LocationManagerHelper.getInstance().stopLocationListener();

    }

    @Override
    public View createView(final Context context, LayoutInflater inflater) {
        searching = false;
        searchWas = false;

        ResourceLoader.loadRecources(context);

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem item = menu.addItem(action_bar_menu_search, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                searching = true;
                if (postListView != null) {
                    if (searchString != null) {
                        postListView.setEmptyView(searchEmptyView);
                        progressView.setVisibility(View.INVISIBLE);
                        emptyView.setVisibility(View.INVISIBLE);
                    }
                    floatingButton.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean onSearchCollapse() {
                //TODO search posts.
                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                searching = false;
                searchWas = false;
                if (postListView != null) {
                    searchEmptyView.setVisibility(View.INVISIBLE);
                    if (PostsController.getInstance().isLoadingPosts() && PostsController.getInstance().getPosts().isEmpty()) {
                        emptyView.setVisibility(View.INVISIBLE);
                        postListView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.INVISIBLE);
                        postListView.setEmptyView(emptyView);
                    }
                    floatingButton.setVisibility(View.VISIBLE);
                    floatingHidden = true;
                    ViewProxy.setTranslationY(floatingButton, AndroidUtilities.dp(100));
                    hideFloatingButton(false);
                    if (postListView.getAdapter() != postsAdapter) {
                        postListView.setAdapter(postsAdapter);
                        postsAdapter.notifyDataSetChanged();
                    }
                }
                if (postsSearchAdapter != null) {
                    postsSearchAdapter.searchPosts(null);
                }
                return true;
            }

            @Override
            public void onTextChanged(EditText editText) {
                //TODO search posts.
                String text = editText.getText().toString();
                if (text.length() != 0) {
                    searchWas = true;
                    if (postsSearchAdapter != null) {
                        postListView.setAdapter(postsSearchAdapter);
                        postsSearchAdapter.notifyDataSetChanged();
                    }
                    if (searchEmptyView != null && postListView.getEmptyView() != searchEmptyView) {
                        emptyView.setVisibility(View.INVISIBLE);
                        progressView.setVisibility(View.INVISIBLE);
                        searchEmptyView.showTextView();
                        postListView.setEmptyView(searchEmptyView);
                    }
                }
                if (postsSearchAdapter != null) {
                    postsSearchAdapter.searchPosts(text);
                }
            }
        });
        item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        //TODO-TEMP search invisible
        item.setVisibility(View.INVISIBLE);

        locationItem = menu.addItem(action_bar_menu_location, R.drawable.ic_attach_location_grey);

        ActionBarMenuItem otherItem = menu.addItem(action_bar_menu_other, R.drawable.ic_ab_other);
        otherItem.addSubItem(list_menu_synchronize, LocaleController.getString("Synchronize", R.string.Synchronize), 0);
        otherItem.addSubItem(list_menu_map, LocaleController.getString("Map", R.string.Map), 0);

        if (searchString != null) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        } else {
            actionBar.setBackButtonDrawable(new MenuDrawable());
        }
//            actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
        actionBar.setTitle(LocaleController.getString("AppFullName", R.string.AppFullName));
        //TODO in LaunchActivity if setAllowOverlayText is TRUE you can change the text.
//        actionBar.setTitleOverlayText("Verbinde...");
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    //TODO block drawer Menu here.
//                    if (onlySelect) {
//                        finishFragment();
//                    } else
                    if (parentLayout != null) {
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == list_menu_synchronize) {
                    if (offlineMode) {
                        showProgressView();
                        MediaController.loadGeoTaggedGalleryPhotos(0, true);
                    } else {
                        refreshPosts(true);
                    }
                } else if (id == list_menu_map) {
                    openLocationChooser();
                } else if (id == action_bar_menu_location) {
                    openLocationChooser();
                } else if (id == 1) {
                    System.out.println();
//                    UserConfig.appLocked = !UserConfig.appLocked;
//                    UserConfig.saveConfig(false);
//                    updatePasscodeButton();
                }
            }
        });


        FrameLayout frameLayout = new FrameLayout(context);
        fragmentView = frameLayout;

        //
        swipeRefreshLayout = new SwipeRefreshLayout(context);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO temp test
//                new RestTask().execute("param");
                PostsController.getInstance().loadPosts(null, 0, Constants.POST_COUNT, true, offlineMode);

//                RestManager.getInstance().uploadTest(new PostRequest(), new Callback<PostResponse>() {
//                    @Override
//                    public void onResponse(Response<PostResponse> response, Retrofit retrofit) {
//                        System.out.println(response);
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        System.out.println(t);
//                    }
//                });


//                refreshContent();
//                Toast.makeText(((Context) getParentActivity()), "REFRESH BUTTON is CLICKED", Toast.LENGTH_SHORT).show();
                // Probably refresh icon disappear when we update the adapter the content. Because I should not use this method. OR NOT . I think it is ok to use this method. according to tutorial
//                stopRefreshingProgressView();
//                postsAdapter.notifyDataSetChanged();
            }
        });

        //


        postListView = new RecyclerListView(context);
        postListView.setVerticalScrollBarEnabled(true);
        postListView.setItemAnimator(null);
        postListView.setInstantClick(true);
        postListView.setLayoutAnimation(null);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        postListView.setLayoutManager(layoutManager);
        if (Build.VERSION.SDK_INT >= 11) {
            postListView.setVerticalScrollbarPosition(LocaleController.isRTL ? ListView.SCROLLBAR_POSITION_LEFT : ListView.SCROLLBAR_POSITION_RIGHT);
        }
        //
        swipeRefreshLayout.addView(postListView);
        //
        frameLayout.addView(swipeRefreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        postListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (postListView == null || postListView.getAdapter() == null) {
                    return;
                }
                String post_id = "";
                RecyclerView.Adapter adapter = postListView.getAdapter();
                if (adapter == postsAdapter) {
                    Post postObject = postsAdapter.getItem(position);
                    if (postObject == null) {
                        return;
                    }
                    post_id = postObject.getId();
                } else if (adapter == postsSearchAdapter) {
                    Object obj = postsSearchAdapter.getItem(position);
                    if (obj instanceof Post) {
                        Post post = (Post) obj;
                        post_id = post.getId();
                        postsSearchAdapter.addHashtagsFromMessage(postsSearchAdapter.getLastSearchString());
                    } else if (obj instanceof String) {
                        actionBar.openSearchField((String) obj);
                    }
                }

                if (StringUtils.isEmpty(post_id)) {
                    return;
                }


                if (actionBar != null) {
                    actionBar.closeSearchField();
                }

                // TODO here action on click to post. Open new Activity for example

//                    if (AndroidUtilities.isTablet()) {
//                        if (!StringUtils.isEmpty(openedPostId) && openedPostId.equals(post_id)) {
//                            return;
//                        }
//                        if (postsAdapter != null) {
//                            postsAdapter.setOpenedPostId(openedPostId = post_id);
//                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
//                        }
//                    }
//                    if (searchString != null) {
//                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
//                        presentFragment(new ChatActivity(args));
//                    } else {
//                        presentFragment(new ChatActivity(args));
//                    }

            }
        });
        postListView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                onItemLongClickHandle(view, position);

            }
        });

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setVisibility(View.INVISIBLE);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        frameLayout.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new LinearLayout(context);
        emptyView.setOrientation(LinearLayout.VERTICAL);
        emptyView.setVisibility(View.INVISIBLE);
        emptyView.setGravity(Gravity.CENTER);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        emptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

//        TextView textView = new TextView(context);
//        textView.setText(LocaleController.getString("NothingHappens", R.string.NothingHappens));
//        textView.setTextColor(0xff959595);
//        textView.setGravity(Gravity.CENTER);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                refreshPosts(false);
//            }
//        });
//        emptyView.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        textViewForEmptyView = new TextView(context);
//        String help = LocaleController.getString("NoChatsHelp", R.string.NoChatsHelp);
//        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
//            help = help.replace("\n", " ");
//        }
//        textView.setText(LocaleController.getString("NothingHappens", R.string.NothingHappens));
        textViewForEmptyView.setText(LocaleController.getString("LoadDataFailed", R.string.LoadDataFailed));
        textViewForEmptyView.setTextColor(0xff959595);
        textViewForEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textViewForEmptyView.setGravity(Gravity.CENTER);
        textViewForEmptyView.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(6), AndroidUtilities.dp(8), AndroidUtilities.dp(6));
        textViewForEmptyView.setLineSpacing(AndroidUtilities.dp(2), 1);
        emptyView.addView(textViewForEmptyView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        Button tryAgainButton = new Button(context);
        tryAgainButton.setTransformationMethod(null);
        tryAgainButton.setText(LocaleController.getString("TryAgain", R.string.TryAgain));
        tryAgainButton.setTextColor(0xff377aae);
//        tryAgainButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // ??
        tryAgainButton.setBackgroundColor(0xffd7e8f7); // background is font color of the text in address subtitle
        tryAgainButton.setGravity(Gravity.CENTER);
        tryAgainButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPosts(false);
            }
        });


        emptyView.addView(tryAgainButton, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
//        emptyView.addView(tryAgainButton);


        progressView = new ProgressBar(context);
        progressView.setVisibility(View.INVISIBLE);
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        floatingButton = new ImageView(context);
        floatingButton.setVisibility(View.VISIBLE);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        floatingButton.setBackgroundResource(R.drawable.floating_states);
        floatingButton.setImageResource(R.drawable.floating_pencil);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        frameLayout.addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
//                args.putBoolean("destroyAfterSelect", true);
                presentFragment(new PostCreateActivity(args));
            }
        });

        postListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }

                final int currentFirstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                    mIsScrollingUp = false;
                } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                    mIsScrollingUp = true;
                }

                mLastFirstVisibleItem = currentFirstVisibleItem;

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();

                if (searching && searchWas) {
                    if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !postsSearchAdapter.isPostsSearchEndReached()) {
                        postsSearchAdapter.loadMoreSearchPosts();
                    }
                    return;
                }
                //TODO fix it. to often run load posts.
                if (visibleItemCount > 0) {
                    if (layoutManager.findLastVisibleItemPosition() == PostsController.getInstance().getPosts().size() - 1 && !mIsScrollingUp && !PostsController.getInstance().getPosts().isEmpty()) {
                        String offset = PostsController.getInstance().getPosts().get(PostsController.getInstance().getPosts().size() - 1).getId(); // TODO When empty list. java.lang.ArrayIndexOutOfBoundsException: length=12; index=-1
//                        startRefreshingProgressView();
                        PostsController.getInstance().loadPosts(offset, PostsController.getInstance().getOffset(), Constants.POST_COUNT, false, offlineMode);
                    }
                }

                if (floatingButton.getVisibility() != View.GONE) {
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated) {
                        hideFloatingButton(goingDown);
                    }
                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }
            }
        });

        if (searchString == null) {
            //TODO find way not to pass PostActivity
            postsAdapter = new PostsAdapter(context, PostsActivity.this);
            if (AndroidUtilities.isTablet() && StringUtils.isEmpty(openedPostId)) {
                postsAdapter.setOpenedPostId(openedPostId);
            }
            postListView.setAdapter(postsAdapter);
        }
        int type = 0;
        if (searchString != null) {
            type = 2;
        } else {
            type = 1;
        }
        postsSearchAdapter = new PostsSearchAdapter(context, type);
        postsSearchAdapter.setDelegate(new PostsSearchAdapter.PostsActivitySearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && searchEmptyView != null) {
                    if (search) {
                        searchEmptyView.showProgress();
                    } else {
                        searchEmptyView.showTextView();
                    }
                }
            }
        });

        if (PostsController.getInstance().isLoadingPosts() && PostsController.getInstance().getPosts().isEmpty()) {
            searchEmptyView.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.INVISIBLE);
            postListView.setEmptyView(progressView);
        } else {
            searchEmptyView.setVisibility(View.INVISIBLE);
            progressView.setVisibility(View.INVISIBLE);
            postListView.setEmptyView(emptyView);
        }
        if (searchString != null) {
            actionBar.openSearchField(searchString);
        }

        return fragmentView;
    }

    @Override
    public void onPause() {
        LocationManagerHelper.getInstance().stopLocationListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationManagerHelper.getInstance().runLocationListener();
        if (postsAdapter != null) {
            postsAdapter.notifyDataSetChanged();
        }
        if (postsSearchAdapter != null) {
            postsSearchAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (floatingButton != null) {
            floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewProxy.setTranslationY(floatingButton, floatingHidden ? AndroidUtilities.dp(100) : 0);
                    floatingButton.setClickable(!floatingHidden);
                    if (floatingButton != null) {
                        if (Build.VERSION.SDK_INT < 16) {
                            floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
//        int guid = 0;
//        if(args !=null && args.length != 0) {
//            guid  = (int)args[0];
//        }
        if (id == NotificationCenter.stopRefreshingView) {
            stopRefreshingProgressView();
        } else if (id == NotificationCenter.locationServiceDisabled || id == NotificationCenter.undefinedLocation) {
//            Toast.makeText(((Context) getParentActivity()), "Please, enable gps on your phone", Toast.LENGTH_SHORT).show();
            stopRefreshingProgressView();
            Activity context = getParentActivity();
            if (context != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.getString("EnableGPS", R.string.EnableGPS));
//            builder.setPositiveButton("OK", null);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //TODO example of invoking application context
                        Context applicationContext = ApplicationLoader.applicationContext;
                        PostsActivity.this.getParentActivity().startActivity(myIntent);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        } else if (id == NotificationCenter.postRequestFinished) {
            boolean withError = false;
            if (args != null && args.length != 0) {
                withError = (boolean) args[0];
            }
            notifyDateSetChanged();
            updateViewLayers(withError);
            stopRefreshingProgressView();
            if (withError) {
                Toast.makeText(((Context) getParentActivity()), "Load posts error", Toast.LENGTH_SHORT).show();
            }
        } else if (id == NotificationCenter.postsNeedReload) {
//            hideProgressView();
            boolean scrollToTop = false;
            if (args != null && args.length != 0) {
                scrollToTop = (boolean) args[0];
            }

            if (scrollToTop) {
                layoutManager.scrollToPosition(0);
            }
            notifyDateSetChanged();
            updateViewLayers(false);
            stopRefreshingProgressView();
        } else if (id == NotificationCenter.emojiDidLoaded) {
            if (postListView != null) {
                updateVisibleRows(0);
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            updateVisibleRows((Integer) args[0]);
        } else if (id == NotificationCenter.postsRefresh) {
            boolean force = false;
            if (args != null && args.length != 0) {
                force = args[0] == null ? false : (Boolean) args[0];
            }
            refreshPosts(force);
        } else if (id == NotificationCenter.offlinePostsLoaded) {
//            layoutManager.scrollToPosition(0);
//            startRefreshingProgressView();
            PostsController.getInstance().loadPosts(null, 0, Constants.POST_COUNT, true, offlineMode); // TODO why offlineMode is false /// aaa becaue different instances !!!
        } else if (id == NotificationCenter.switchToOfflineMode) {
            boolean force = false;
            if (!this.offlineMode) {
                force = true;
                if (!MediaController.getInstance().isRTreeloaded()) {
                    showProgressView();
                }
                PostsController.getInstance().cancelAllCalls();
                PostsController.getInstance().getPosts().clear();
                MediaController.loadGeoTaggedGalleryPhotos(0, false);
            }


            this.offlineMode = true;
//            refreshPosts(force);
        } else if (id == NotificationCenter.switchToOnlineMode) {
            boolean force = false;
            if (this.offlineMode) {
                force = true;
                PostsController.getInstance().cancelAllCalls();
                PostsController.getInstance().getPosts().clear();
            }
            this.offlineMode = false;
//            layoutManager.scrollToPosition(0);
            refreshPosts(force);
        }
    }

    private void refreshPosts(boolean force) {
        if (PostsController.getInstance().getPosts().isEmpty() || force) {
//            startRefreshingProgressView();
            showProgressView();
            PostsController.getInstance().loadPosts(null, 0, Constants.POST_COUNT, true, offlineMode);
        }
    }

    private void showProgressView() {
        if (progressView != null && postListView != null) {
            progressView.setVisibility(View.VISIBLE);
            postListView.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.INVISIBLE);
        }
    }

    private void hideProgressView() {
        if (progressView != null && postListView != null) {
            progressView.setVisibility(View.INVISIBLE);
            postListView.setVisibility(View.VISIBLE);
        }
    }


    private void hideFloatingButton(boolean hide) {
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        ObjectAnimatorProxy animator = ObjectAnimatorProxy.ofFloatProxy(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(100) : 0).setDuration(300);
        animator.setInterpolator(floatingInterpolator);
        floatingButton.setClickable(!hide);
        animator.start();
    }

    private void updateVisibleRows(int mask) {
        if (postListView == null) {
            return;
        }
        int count = postListView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = postListView.getChildAt(a);
            if (child instanceof PostCell) {
                PostCell cell = (PostCell) child;
                if ((mask & PostsController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                    cell.checkCurrentPostIndex();
                    if (AndroidUtilities.isTablet()) {
                        cell.setPostSelected(cell.getPostId().equals(openedPostId));
                    }
                } else if ((mask & PostsController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                    if (AndroidUtilities.isTablet()) {
                        cell.setPostSelected(cell.getPostId().equals(openedPostId));
                    }
                } else {
                    cell.update(mask);
                }
            }
//            else if (child instanceof UserCell) {
//                ((UserCell) child).update(mask);
//            }
        }
    }

    public void setSearchString(String string) {
        searchString = string;
    }


    private void didSelectResult(final String dialog_id, boolean useAlert, final boolean param) {
        //TODO here onlySelect Posts (Dialog)
    }


    //TODO it is needed for PhotoViewer.

    public ActionBar getActionBar() {
        return actionBar;
    }


    @Deprecated //temporally
    private void onItemLongClickHandle(View view, int position) {
        //TODO-mock mock
        if (true) {
            return;
        }
        if (searching && searchWas || getParentActivity() == null) {
            if (searchWas && searching) {
                RecyclerView.Adapter adapter = postListView.getAdapter();
                if (adapter == postsSearchAdapter) {
                    Object item = postsSearchAdapter.getItem(position);
                    if (item instanceof String) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                        builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                postsSearchAdapter.clearRecentHashtags();
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                        return;
                    }
                }
            }
            return;
        }
        Post post = PostsController.getInstance().getPosts().get(position);

        selectedPost = post.getId();

                /*AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));


                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());*/

        BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
//                int lower_id = (int) selectedPost;
//                int high_id = (int) (selectedPost >> 32);

//                final boolean isChat = lower_id < 0 && high_id != 1;
        builder.setItems(new CharSequence[]{LocaleController.getString("ClearHistory", R.string.ClearHistory),
                LocaleController.getString("Delete", R.string.Delete)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                if (which == 0) {
                    builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                } else {

                    builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));

                }
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (which != 0) {

                            PostsController.getInstance().deletePost(selectedPost, 0, false);

                            if (AndroidUtilities.isTablet()) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedPost);
                            }
                        } else {
                            PostsController.getInstance().deletePost(selectedPost, 0, true);
                        }
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });
        showDialog(builder.create());

    }

    private void startRefreshingProgressView() {
        handler.postDelayed(refreshProgressRun, Constants.PROGRESS_DIALOG_TIMEOUT);
    }

    private void stopRefreshingProgressView() {
        handler.removeCallbacks(refreshProgressRun);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (progressView != null) {
            progressView.setVisibility(View.INVISIBLE);
        }
    }

    //http://stackoverflow.com/questions/5375654/how-to-implement-google-maps-search-by-address-in-android  search Google Maps
    private void openLocationChooser() {
        if (!isGoogleMapsInstalled()) {
            return;
        }
        Bundle args = new Bundle();
//        args.putBoolean(Constants.RESTRICTED_AREA, true);
//        args.putInt(Constants.RADIUS_ARG, Constants.RADIUS);
        args.putBoolean(Constants.SEARCH_PLACES_ENABLE_ARG, false);
        LocationActivityAragats fragment = new LocationActivityAragats(args);
        Location customLocation = LocationManagerHelper.getInstance().getCustomLocation();
        if (customLocation != null) {
            fragment.setCustomLocation(new Location(customLocation));
        }
        fragment.setDelegate(locationActivityDelegate);
        presentFragment(fragment);
    }


    private void updateViewLayers(boolean withError) {
        if (postListView != null) {
            try {
                if (PostsController.getInstance().isLoadingPosts() && PostsController.getInstance().getPosts().isEmpty()) {
                    searchEmptyView.setVisibility(View.INVISIBLE);
                    emptyView.setVisibility(View.INVISIBLE);
                    postListView.setEmptyView(progressView);
                } else {
//                        postListView.setVisibility(View.INVISIBLE);
                    progressView.setVisibility(View.INVISIBLE);
                    if (searching && searchWas) {
                        emptyView.setVisibility(View.INVISIBLE);
                        postListView.setEmptyView(searchEmptyView);
                    } else {
                        searchEmptyView.setVisibility(View.INVISIBLE);
                        postListView.setEmptyView(emptyView);
                    }
                    if (PostsController.getInstance().getPosts().isEmpty()) {
                        // if with or without errors.
                        if (withError) {
                            textViewForEmptyView.setText(LocaleController.getString("LoadDataFailed", R.string.LoadDataFailed));
                        } else {
                            textViewForEmptyView.setText(LocaleController.getString("NoPosts", R.string.NoPosts));

                        }

                    }
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e); //TODO fix it in other way?
            }
        }
    }


    private void notifyDateSetChanged() {
        if (postsAdapter != null) {
//                if (postsAdapter.isDataSetChanged()) {
            postsAdapter.notifyDataSetChanged();
//                } else {
//                    updateVisibleRows(PostsController.UPDATE_MASK_NEW_MESSAGE);
//                }
        }
        if (postsSearchAdapter != null) {
            postsSearchAdapter.notifyDataSetChanged();
        }
    }


    public LocationActivityAragats.LocationActivityDelegate getLocationActivityDelegate() {
        return locationActivityDelegate;
    }
}
