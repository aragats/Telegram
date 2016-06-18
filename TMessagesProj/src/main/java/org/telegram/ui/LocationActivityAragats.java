/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.android.NotificationCenter;
import org.telegram.android.location.LocationManagerHelper;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseLocationAdapter;
import org.telegram.ui.Adapters.LocationActivityAdapter;
import org.telegram.ui.Adapters.LocationActivitySearchAdapter;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MapPlaceholderDrawable;
import org.telegram.utils.CollectionUtils;
import org.telegram.utils.Constants;
import org.telegram.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.aragats.aracle.R;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.Venue;

//TODO-aragats
public class LocationActivityAragats extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private GoogleMap googleMap;
    private TextView distanceTextView;
    private BackupImageView avatarImageView;
    //TODO leave it. name of user under the map
    private TextView nameTextView;
    private MapView mapView;
    private FrameLayout mapViewClip;
    private LocationActivityAdapter adapter;
    private ListView listView;
    private ListView searchListView;
    private LocationActivitySearchAdapter searchAdapter;
    private LinearLayout emptyTextLayout;
    private ImageView markerImageView;
    private ImageView markerXImageView;
    private ImageView locationButton;

    private AnimatorSet animatorSet;

    private boolean searching;
    private boolean searchWas;

    private boolean wasResults;
    private boolean onLocationButtonClicked;

    private Location myLocation;
    private Location userLocation;
    private Location customLocation;
    private Location venueChosenLocation;
    private int markerTop;

    private Post post;
    private boolean userLocationMoved = false;
    private boolean firstWas = false;
    private CircleOptions circleOptions;
    private LocationActivityDelegate delegate;

    private int overScrollHeight = AndroidUtilities.displaySize.x - AndroidUtilities.getCurrentActionBarHeight() - AndroidUtilities.dp(66);
    private int halfHeight;

    private final static int share = 1;
    private final static int map_list_menu_map = 2;
    private final static int map_list_menu_satellite = 3;
    private final static int map_list_menu_hybrid = 4;


    private boolean searchPlacesEnable;
    private boolean restrictedArea;
    private int radius;


    public interface LocationActivityDelegate {
        void didSelectLocation(TLRPC.MessageMedia location);
    }

    public LocationActivityAragats(Bundle args) {
        super(args);
        searchPlacesEnable = getArguments().getBoolean(Constants.SEARCH_PLACES_ENABLE_ARG, false);
        // Circle area restriction.
        restrictedArea = getArguments().getBoolean(Constants.RESTRICTED_AREA, false);
        radius = getArguments().getInt(Constants.RADIUS_ARG, Constants.RADIUS_4SQUARE);
    }

    @Override
    public boolean needAddActionBar() {
        return post != null;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        swipeBackEnabled = false;
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        if (post != null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        if (mapView != null) {
            mapView.onDestroy();
        }
        if (adapter != null) {
            adapter.destroy();
        }
        if (searchAdapter != null) {
            searchAdapter.destroy();
        }
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == map_list_menu_map) {
                    if (googleMap != null) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }
                } else if (id == map_list_menu_satellite) {
                    if (googleMap != null) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    }
                } else if (id == map_list_menu_hybrid) {
                    if (googleMap != null) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    }
                } else if (id == share) {
                    try {
                        List<Double> coordinates = post.getPostCoordinates().getCoordinates();
                        double lat = coordinates.get(1);
                        double lon = coordinates.get(0);
                        getParentActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lon + "?q=" + lat + "," + lon)));
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        if (post != null) {
            Venue venue = post.getVenue();
            if (venue != null && !StringUtils.isEmpty(venue.getName())) {
                actionBar.setTitle(venue.getName());
                if (!StringUtils.isEmpty(venue.getAddress())) {
                    actionBar.setSubtitle(venue.getAddress());
                } else {
                    List<Double> coordinates = venue.getCoordinates().getCoordinates();
                    actionBar.setSubtitle(String.format(Locale.US, "(%f,%f)", coordinates.get(1), coordinates.get(0)));
                }
            } else {
//                actionBar.setTitle(LocaleController.getString("ChatLocation", R.string.ChatLocation));
                if (post.getVenue() != null && !StringUtils.isEmpty(post.getVenue().getAddress())) {
                    actionBar.setTitle(post.getVenue().getAddress()); // TODO TextUtils.ellipsize END  ...  and replace tags and emoji and so on
                } else {
                    actionBar.setTitle(LocaleController.getString("ChatLocation", R.string.Location));
                }
                if (venue != null && venue.getCoordinates() != null && !CollectionUtils.isEmpty(venue.getCoordinates().getCoordinates(), 2)) {
                    List<Double> coordinates = venue.getCoordinates().getCoordinates();
                    actionBar.setSubtitle(String.format(Locale.US, "(%f,%f)", coordinates.get(1), coordinates.get(0)));
                }
            }
            menu.addItem(share, R.drawable.share);
        } else {
            actionBar.setTitle(LocaleController.getString("ShareLocation", R.string.Location));

            ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
                @Override
                public void onSearchExpand() {
                    searching = true;
                    listView.setVisibility(View.GONE);
                    mapViewClip.setVisibility(View.GONE);
                    searchListView.setVisibility(View.VISIBLE);
                    searchListView.setEmptyView(emptyTextLayout);
                }

                @Override
                public boolean onSearchCollapse() {
                    searching = false;
                    searchWas = false;
                    searchListView.setEmptyView(null);
                    listView.setVisibility(View.VISIBLE);
                    mapViewClip.setVisibility(View.VISIBLE);
                    searchListView.setVisibility(View.GONE);
                    emptyTextLayout.setVisibility(View.GONE);
                    searchAdapter.searchDelayed(null, null, radius);
                    return true;
                }

                @Override
                public void onTextChanged(EditText editText) {
                    if (searchAdapter == null) {
                        return;
                    }
                    String text = editText.getText().toString();
                    if (text.length() != 0) {
                        searchWas = true;
                    }
                    searchAdapter.searchDelayed(text, userLocation, radius); //TODO It can be the case. getCenterLocation or leave it as a userLocation (custom moved location)
                }
            });
            item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        }

        ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_other);
        item.addSubItem(map_list_menu_map, LocaleController.getString("Map", R.string.Map), 0);
        item.addSubItem(map_list_menu_satellite, LocaleController.getString("Satellite", R.string.Satellite), 0);
        item.addSubItem(map_list_menu_hybrid, LocaleController.getString("Hybrid", R.string.Hybrid), 0);
        fragmentView = new FrameLayout(context) {
            private boolean first = true;

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);

                if (changed) {
                    fixLayoutInternal(first);
                    first = false;
                }
            }
        };
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        locationButton = new ImageView(context);
        locationButton.setBackgroundResource(R.drawable.floating_user_states);
        locationButton.setImageResource(R.drawable.myloc_on);
        locationButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(locationButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(locationButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            locationButton.setStateListAnimator(animator);
            locationButton.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }

        if (post != null) {
            mapView = new MapView(context);
            frameLayout.setBackgroundDrawable(new MapPlaceholderDrawable());
            mapView.onCreate(null);
            try {
                MapsInitializer.initialize(context);
                googleMap = mapView.getMap();
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }

            FrameLayout bottomView = new FrameLayout(context);
            bottomView.setBackgroundResource(R.drawable.location_panel);
            frameLayout.addView(bottomView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 60, Gravity.LEFT | Gravity.BOTTOM));
            bottomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userLocation != null) {
                        LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                        if (googleMap != null) {
                            CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 8);
                            googleMap.animateCamera(position);
                        }
                    }
                }
            });

            avatarImageView = new BackupImageView(context);
            avatarImageView.setRoundRadius(AndroidUtilities.dp(20));
            bottomView.addView(avatarImageView, LayoutHelper.createFrame(40, 40, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 0 : 12, 12, LocaleController.isRTL ? 12 : 0, 0));

            nameTextView = new TextView(context);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            nameTextView.setTextColor(0xff212121);
            nameTextView.setMaxLines(1);
            nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            nameTextView.setEllipsize(TextUtils.TruncateAt.END);
            nameTextView.setSingleLine(true);
            nameTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
//            bottomView.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 12 : 72, 10, LocaleController.isRTL ? 72 : 12, 0));
            // change the top margin to change the position
            bottomView.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 12 : 72, 33, LocaleController.isRTL ? 72 : 12, 0));
//            bottomView.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 12 : 72, 10, LocaleController.isRTL ? 72 : 172, 0));

            distanceTextView = new TextView(context);
            distanceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            distanceTextView.setTextColor(0xff2f8cc9);
            distanceTextView.setMaxLines(1);
            distanceTextView.setEllipsize(TextUtils.TruncateAt.END);
            distanceTextView.setSingleLine(true);
            distanceTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
//            bottomView.addView(distanceTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 12 : 72, 33, LocaleController.isRTL ? 72 : 12, 0));
            // change the top margin to change the position
            bottomView.addView(distanceTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 12 : 72, 10, LocaleController.isRTL ? 72 : 12, 0));

            List<Double> coordinates = post.getPostCoordinates().getCoordinates();
            userLocation = new Location("network");
            userLocation.setLatitude(coordinates.get(1));
            userLocation.setLongitude(coordinates.get(0));
            if (googleMap != null) {
                LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                try {
                    googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
                CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 8);
                googleMap.moveCamera(position);
            }

            ImageView routeButton = new ImageView(context);
            routeButton.setBackgroundResource(R.drawable.floating_states);
            routeButton.setImageResource(R.drawable.navigate);
            routeButton.setScaleType(ImageView.ScaleType.CENTER);
            if (Build.VERSION.SDK_INT >= 21) {
                StateListAnimator animator = new StateListAnimator();
                animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(routeButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
                animator.addState(new int[]{}, ObjectAnimator.ofFloat(routeButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
                routeButton.setStateListAnimator(animator);
                routeButton.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                    }
                });
            }
            frameLayout.addView(routeButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 28));
            routeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myLocation != null) {
                        try {
                            List<Double> coordinates = post.getPostCoordinates().getCoordinates();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", myLocation.getLatitude(), myLocation.getLongitude(), coordinates.get(1), coordinates.get(0))));
                            getParentActivity().startActivity(intent);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                }
            });


            ImageView choosePostLocationButton = new ImageView(context);
            choosePostLocationButton.setBackgroundResource(R.drawable.floating_states);
            choosePostLocationButton.setImageResource(R.drawable.ic_attach_location_white);
            choosePostLocationButton.setScaleType(ImageView.ScaleType.CENTER);
            if (Build.VERSION.SDK_INT >= 21) {
                StateListAnimator animator = new StateListAnimator();
                animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(choosePostLocationButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
                animator.addState(new int[]{}, ObjectAnimator.ofFloat(choosePostLocationButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
                choosePostLocationButton.setStateListAnimator(animator);
                choosePostLocationButton.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                    }
                });
            }
            frameLayout.addView(choosePostLocationButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? (14 + (56 + 16)) : 0, 0, LocaleController.isRTL ? 0 : (14 + (56 + 16)), 28));
            choosePostLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (post != null && post.getCoordinates() != null && delegate != null) {
                        delegate.didSelectLocation(LocationManagerHelper.convertCoordinatesToGeoPoint(post.getCoordinates(), true));
                        finishFragment();
                    }
                }
            });


            frameLayout.addView(locationButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 100));
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myLocation != null && googleMap != null) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), googleMap.getMaxZoomLevel() - 8));
                    }
                }
            });
        } else {
            searchWas = false;
            searching = false;
            mapViewClip = new FrameLayout(context);
            mapViewClip.setBackgroundDrawable(new MapPlaceholderDrawable());
            if (adapter != null) {
                adapter.destroy();
            }
            if (searchAdapter != null) {
                searchAdapter.destroy();
            }

            listView = new ListView(context);
            listView.setAdapter(adapter = new LocationActivityAdapter(context));
            adapter.setSearchPlacesEnabled(searchPlacesEnable);
            listView.setVerticalScrollBarEnabled(false);
            listView.setDividerHeight(0);
            listView.setDivider(null);
            frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (totalItemCount == 0) {
                        return;
                    }
                    updateClipView(firstVisibleItem);
                }
            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (myLocation == null) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.undefinedLocation);
                        return;
                    }
                    if (position == 1) {
                        if (delegate != null && userLocation != null) {
                            TLRPC.TL_messageMediaGeo location = new TLRPC.TL_messageMediaGeo();
                            location.geo = new TLRPC.TL_geoPoint();
                            location.geo.lat = userLocation.getLatitude();
                            location.geo._long = userLocation.getLongitude();
                            location.isCustomLocation = adapter.isCustomLocation();
                            // check whether is inside the radius only when there is the radius.
                            boolean valid = validChosenLocation(LocationManagerHelper.convertGeoPointToLocation(location.geo));
                            if (valid) {
                                delegate.didSelectLocation(location);
                                finishFragment();
                            } else {
                                Toast.makeText(getParentActivity(), "Invalid location. Choose the location inside the circle", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        TLRPC.TL_messageMediaVenue object = adapter.getItem(position);
                        if (object != null && delegate != null) {
                            object.geo = convertLocationToGeoPoint(userLocation);
                            object.isCustomLocation = true;
                            delegate.didSelectLocation(object);
                        }
                        finishFragment();
                    }
                }
            });
            adapter.setDelegate(new BaseLocationAdapter.BaseLocationAdapterDelegate() {
                @Override
                public void didLoadedSearchResult(ArrayList<TLRPC.TL_messageMediaVenue> places) {
                    if (!wasResults && !places.isEmpty()) {
                        wasResults = true;
                    }
                }
            });
            adapter.setOverScrollHeight(overScrollHeight);

            frameLayout.addView(mapViewClip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));

            mapView = new MapView(context) {
                @Override
                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    if (Build.VERSION.SDK_INT >= 11) {
                        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                            if (animatorSet != null) {
                                animatorSet.cancel();
                            }
                            animatorSet = new AnimatorSet();
                            animatorSet.setDuration(200);
                            animatorSet.playTogether(
                                    ObjectAnimator.ofFloat(markerImageView, "translationY", markerTop + -AndroidUtilities.dp(10)),
                                    ObjectAnimator.ofFloat(markerXImageView, "alpha", 1.0f));
                            animatorSet.start();
                        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                            if (animatorSet != null) {
                                animatorSet.cancel();
                            }
                            animatorSet = new AnimatorSet();
                            animatorSet.setDuration(200);
                            animatorSet.playTogether(
                                    ObjectAnimator.ofFloat(markerImageView, "translationY", markerTop),
                                    ObjectAnimator.ofFloat(markerXImageView, "alpha", 0.0f));
                            animatorSet.start();
                        }
                    }
                    if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                        if (!userLocationMoved) {
                            if (Build.VERSION.SDK_INT >= 11) {
                                AnimatorSet animatorSet = new AnimatorSet();
                                animatorSet.setDuration(200);
                                animatorSet.play(ObjectAnimator.ofFloat(locationButton, "alpha", 1.0f));
                                animatorSet.start();
                            } else {
                                locationButton.setVisibility(VISIBLE);
                            }
                            userLocationMoved = true;
                        }
                        if (googleMap != null && userLocation != null) {
                            userLocation.setLatitude(googleMap.getCameraPosition().target.latitude);
                            userLocation.setLongitude(googleMap.getCameraPosition().target.longitude);
                        }
                        adapter.setCustomLocation(userLocation);
                    }
                    return super.onInterceptTouchEvent(ev);
                }
            };
            mapView.onCreate(null);
            try {
                MapsInitializer.initialize(context);
                googleMap = mapView.getMap();
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }

            View shadow = new View(context);
            shadow.setBackgroundResource(R.drawable.header_shadow_reverse);
            mapViewClip.addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(3), Gravity.LEFT | Gravity.BOTTOM));

            markerImageView = new ImageView(context);
            markerImageView.setImageResource(R.drawable.map_pin);
            mapViewClip.addView(markerImageView, LayoutHelper.createFrame(24, 42, Gravity.TOP | Gravity.CENTER_HORIZONTAL));

            if (Build.VERSION.SDK_INT >= 11) {
                markerXImageView = new ImageView(context);
                markerXImageView.setAlpha(0.0f);
                markerXImageView.setImageResource(R.drawable.place_x);
                mapViewClip.addView(markerXImageView, LayoutHelper.createFrame(14, 14, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
            }

            mapViewClip.addView(locationButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myLocation != null && googleMap != null) {
                        if (Build.VERSION.SDK_INT >= 11) {
                            AnimatorSet animatorSet = new AnimatorSet();
                            animatorSet.setDuration(200);
                            animatorSet.play(ObjectAnimator.ofFloat(locationButton, "alpha", 0.0f));
                            animatorSet.start();
                        } else {
                            locationButton.setVisibility(View.INVISIBLE);
                        }
                        onLocationButtonClicked = true;
                        adapter.setCustomLocation(null);
                        userLocationMoved = false;
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
                        // TODO back to location of the center ????? of the custom location. do I need it ?
                        if (restrictedArea) {
                            Location centerLocation = getCenterLocation();
                            if (centerLocation != null) {
                                userLocation = new Location(centerLocation);
                                adapter.setCustomLocation(centerLocation);
                                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude())));
                            }
                        }
                    }
                }
            });
            if (Build.VERSION.SDK_INT >= 11) {
                locationButton.setAlpha(0.0f);
            } else {
                locationButton.setVisibility(View.INVISIBLE);
            }

            emptyTextLayout = new LinearLayout(context);
            emptyTextLayout.setVisibility(View.GONE);
            emptyTextLayout.setOrientation(LinearLayout.VERTICAL);
            frameLayout.addView(emptyTextLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
            emptyTextLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            TextView emptyTextView = new TextView(context);
            emptyTextView.setTextColor(0xff808080);
            emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            emptyTextView.setGravity(Gravity.CENTER);
            emptyTextView.setText(LocaleController.getString("NoResult", R.string.NoResult));
            emptyTextLayout.addView(emptyTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0.5f));

            FrameLayout frameLayoutEmpty = new FrameLayout(context);
            emptyTextLayout.addView(frameLayoutEmpty, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0.5f));

            searchListView = new ListView(context);
            searchListView.setVisibility(View.GONE);
            searchListView.setDividerHeight(0);
            searchListView.setDivider(null);
//            searchListView.setAdapter(searchAdapter = new LocationActivityGoogleSearchAdapter(context));
            searchListView.setAdapter(searchAdapter = new LocationActivitySearchAdapter(context));
            frameLayout.addView(searchListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
            searchListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_TOUCH_SCROLL && searching && searchWas) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });
            searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TLRPC.TL_messageMediaVenue object = searchAdapter.getItem(position);
                    if (object != null && delegate != null) {
                        object.geo = convertLocationToGeoPoint(userLocation);
                        object.isCustomLocation = true;
                        delegate.didSelectLocation(object);
                    }
                    finishFragment();
                }
            });

            if (googleMap != null) {
                userLocation = new Location("network");
                userLocation.setLatitude(20.659322);
                userLocation.setLongitude(-11.406250);
                if (customLocation != null) {
                    userLocation = new Location(customLocation);
                    adapter.setCustomLocation(userLocation);
                }
            }

            frameLayout.addView(actionBar);
        }

        if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);
            googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    positionMarker(location);
                }
            });
            positionMarker(myLocation = LocationManagerHelper.getInstance().getLastLocation());
            if (myLocation == null) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.undefinedLocation);
            }


            //TODO in case of myLocation is null. it does not work !!!! Consider it !!!!
            if (restrictedArea) {
                googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        //TODO reuse validChosenLocation;
                        if (userLocation != null && myLocation != null) {
                            Location centerLocation = myLocation;
                            if (customLocation != null) {
                                centerLocation = customLocation;
                            }
                            if (centerLocation.distanceTo(userLocation) > radius) {
                                userLocation = new Location(centerLocation);
                                adapter.setCustomLocation(userLocation); // ?
                                CameraUpdate position = CameraUpdateFactory.newLatLng(new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude()));
                                googleMap.animateCamera(position);
                            }
                        }
                    }
                });

                Location centerLocation = getCenterLocation();
                if (centerLocation != null) {
//                googleMap.addCircle(new CircleOptions()
//                        .center(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
//                        .radius(Constants.RADIUS)
//                        .strokeColor(Color.BLUE)
//                        .fillColor(Color.TRANSPARENT)
//                        .strokeWidth(2));

                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude()))
                            .radius(radius)
                            .strokeWidth(1)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.parseColor("#300084d3")); //#500084d3  #506bc8ff - blue  #501cff5c - green
//                        .fillColor(Color.parseColor("#506bc8ff"));// blue original //#500084d3  #506bc8ff - blue  #501cff5c - green
//                        .fillColor(Color.parseColor("#501cff5c")); //#500084d3  #506bc8ff  #501cff5c - green
                    ////#500084d3 - original;  #500084d3 - original with other transparency;   #506bc8ff - blue (my);  #501cff5c - green (my)
                    // Supported formats are: #RRGGBB #AARRGGBB
                    //   #AA is the alpha, or amount of transparency
                    googleMap.addCircle(circleOptions);
                }
            }

        }

        return fragmentView;
    }

    @Override
    public void onOpenAnimationEnd() {
        if (mapViewClip != null) {
            mapViewClip.addView(mapView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, overScrollHeight + AndroidUtilities.dp(10), Gravity.TOP | Gravity.LEFT));
            updateClipView(listView.getFirstVisiblePosition());
        } else {
            ((FrameLayout) fragmentView).addView(mapView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        }
    }

    private void updateClipView(int firstVisibleItem) {
        int height = 0;
        int top = 0;
        View child = listView.getChildAt(0);
        if (child != null) {
            if (firstVisibleItem == 0) {
                top = child.getTop();
                height = overScrollHeight + (top < 0 ? top : 0);
                halfHeight = (top < 0 ? top : 0) / 2;
            }
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mapViewClip.getLayoutParams();
            if (layoutParams != null) {
                if (height <= 0) {
                    if (mapView.getVisibility() == View.VISIBLE) {
                        mapView.setVisibility(View.INVISIBLE);
                        mapViewClip.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mapView.getVisibility() == View.INVISIBLE) {
                        mapView.setVisibility(View.VISIBLE);
                        mapViewClip.setVisibility(View.VISIBLE);
                    }
                }
                if (Build.VERSION.SDK_INT >= 11) {
                    mapViewClip.setTranslationY(Math.min(0, top));
                    mapView.setTranslationY(Math.max(0, -top / 2));
                    markerImageView.setTranslationY(markerTop = -top - AndroidUtilities.dp(42) + height / 2);
                    markerXImageView.setTranslationY(-top - AndroidUtilities.dp(7) + height / 2);

                    if (googleMap != null) {
                        layoutParams = (FrameLayout.LayoutParams) mapView.getLayoutParams();
                        if (layoutParams != null && layoutParams.height != overScrollHeight + AndroidUtilities.dp(10)) {
                            layoutParams.height = overScrollHeight + AndroidUtilities.dp(10);
                            googleMap.setPadding(0, 0, 0, AndroidUtilities.dp(10));
                            mapView.setLayoutParams(layoutParams);
                        }
                    }
                } else {
                    markerTop = 0;
                    layoutParams.height = height;
                    mapViewClip.setLayoutParams(layoutParams);

                    layoutParams = (FrameLayout.LayoutParams) markerImageView.getLayoutParams();
                    layoutParams.topMargin = height / 2 - AndroidUtilities.dp(42);
                    markerImageView.setLayoutParams(layoutParams);

                    if (googleMap != null) {
                        layoutParams = (FrameLayout.LayoutParams) mapView.getLayoutParams();
                        if (layoutParams != null) {
                            layoutParams.topMargin = halfHeight;
                            layoutParams.height = overScrollHeight + AndroidUtilities.dp(10);
                            googleMap.setPadding(0, 0, 0, AndroidUtilities.dp(10));
                            mapView.setLayoutParams(layoutParams);
                        }
                    }
                }
            }
        }
    }

    private void fixLayoutInternal(final boolean resume) {
        if (listView != null) {
            int height = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.getCurrentActionBarHeight();
            int viewHeight = fragmentView.getMeasuredHeight();
            if (viewHeight == 0) {
                return;
            }
            overScrollHeight = viewHeight - AndroidUtilities.dp(66) - height;

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.topMargin = height;
            listView.setLayoutParams(layoutParams);
            layoutParams = (FrameLayout.LayoutParams) mapViewClip.getLayoutParams();
            layoutParams.topMargin = height;
            layoutParams.height = overScrollHeight;
            mapViewClip.setLayoutParams(layoutParams);
            layoutParams = (FrameLayout.LayoutParams) searchListView.getLayoutParams();
            layoutParams.topMargin = height;
            searchListView.setLayoutParams(layoutParams);

            adapter.setOverScrollHeight(overScrollHeight);
            layoutParams = (FrameLayout.LayoutParams) mapView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.height = overScrollHeight + AndroidUtilities.dp(10);
                if (googleMap != null) {
                    googleMap.setPadding(0, 0, 0, AndroidUtilities.dp(10));
                }
                mapView.setLayoutParams(layoutParams);
            }
            adapter.notifyDataSetChanged();

            if (resume) {
                listView.setSelectionFromTop(0, -(int) (AndroidUtilities.dp(56) * 2.5f + AndroidUtilities.dp(36 + 66)));
                updateClipView(listView.getFirstVisiblePosition());
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelectionFromTop(0, -(int) (AndroidUtilities.dp(56) * 2.5f + AndroidUtilities.dp(36 + 66)));
                        updateClipView(listView.getFirstVisiblePosition());
                    }
                });
            } else {
                updateClipView(listView.getFirstVisiblePosition());
            }
        }
    }

    private void updateUserData() {
        if (post != null && avatarImageView != null) {
            //TODO location is from user. below photo and name.
            if (post.getPreviewImage() != null) {
//                TLRPC.FileLocation photo = null;
//                if (user.photo != null) {
//                    photo = user.photo.photo_small;
//                }
//                avatarImageView.setImage(post.getImage().getUrl(), "50_50", avatarDrawable);
                avatarImageView.setImage(post.getPreviewImage().getUrl(), "50_50", new AvatarDrawable());
                nameTextView.setText(post.getText());
            } else {
                avatarImageView.setImageDrawable(null);
            }
//            Object user = null;
//            if (user != null) {
////                TLRPC.FileLocation photo = null;
////                if (user.photo != null) {
////                    photo = user.photo.photo_small;
////                }
////                avatarImageView.setImage(photo, null, new AvatarDrawable(user));
////                nameTextView.setText(ContactsController.formatName(user.first_name, user.last_name));
//            } else {
//                avatarImageView.setImageDrawable(null);
//            }
        }
    }

    private void positionMarker(Location location) {
        if (location == null) {
            return;
        }
        myLocation = new Location(location);
        if (post != null) {
            if (userLocation != null && distanceTextView != null) {
                float distance = location.distanceTo(userLocation);
                if (distance < 1000) {
                    distanceTextView.setText(String.format("%d %s", (int) (distance), LocaleController.getString("MetersAway", R.string.MetersAway)));
                } else {
                    distanceTextView.setText(String.format("%.2f %s", distance / 1000.0f, LocaleController.getString("KMetersAway", R.string.KMetersAway)));
                }
            }
        } else if (googleMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (adapter != null) {
                if (searchPlacesEnable) {
                    adapter.searchGooglePlacesWithQuery(null, getCenterLocation(), radius, Constants.FOURSQUARE_BROWSER);
                }
                adapter.setGpsLocation(myLocation);
            }
            if (!userLocationMoved) {
                //
                if (venueChosenLocation != null) {
                    if (!onLocationButtonClicked) {
                        userLocation = new Location(venueChosenLocation);
                        latLng = new LatLng(venueChosenLocation.getLatitude(), venueChosenLocation.getLongitude());
                        adapter.setCustomLocation(userLocation);
                    } else {
                        Location centerLocation = getCenterLocation(); // TODO check on null.
                        if (centerLocation != null) {
                            userLocation = new Location(centerLocation); // again assign but already done earlier
                            latLng = new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude());
                            adapter.setCustomLocation(userLocation);
                        }
                    }
                } else if (customLocation != null && !onLocationButtonClicked) {
                    userLocation = new Location(customLocation); // again assign but already done earlier.
                    latLng = new LatLng(customLocation.getLatitude(), customLocation.getLongitude());
                } else {
                    userLocation = new Location(location);
                }
                //
                if (firstWas) {
                    CameraUpdate position = CameraUpdateFactory.newLatLng(latLng);
                    googleMap.animateCamera(position);
                } else {
                    firstWas = true;
                    CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 8);
                    googleMap.moveCamera(position);
                }
            }
        }
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
//            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
//                updateUserData();
//            }
        } else if (id == NotificationCenter.closeChats) {
            removeSelfFromStack();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            try {
                mapView.onPause();
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!AndroidUtilities.isTablet()) {
            getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
        if (mapView != null) {
            mapView.onResume();
        }
        updateUserData();
        fixLayoutInternal(true);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    public void setDelegate(LocationActivityDelegate delegate) {
        this.delegate = delegate;
    }

    private void updateSearchInterface() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


    private TLRPC.GeoPoint convertLocationToGeoPoint(Location userLocation) {
        TLRPC.GeoPoint geo = new TLRPC.TL_geoPoint();
        geo.lat = userLocation.getLatitude();
        geo._long = userLocation.getLongitude();
        return geo;
    }


    public void setCustomLocation(Location customLocation) {
        this.customLocation = customLocation;
    }

    public void setVenueChosenLocation(Location venueChosenLocation) {
        this.venueChosenLocation = venueChosenLocation;
    }

    public Location getCustomLocation() {
        return customLocation;
    }


    private boolean validChosenLocation(Location location) {
        if (restrictedArea) {
            if (location != null && myLocation != null) {
                Location centerLocation = myLocation;
                if (customLocation != null) {
                    centerLocation = customLocation;
                }
                if (centerLocation.distanceTo(location) <= radius) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }


    private Location getCenterLocation() {
        Location centerLocation = myLocation;
        if (customLocation != null) {
            centerLocation = customLocation;
        }
        return centerLocation;
    }
}
