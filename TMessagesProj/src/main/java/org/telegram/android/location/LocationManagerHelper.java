package org.telegram.android.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import org.telegram.messenger.TLRPC;
import org.telegram.utils.CollectionUtils;
import org.telegram.utils.Constants;
import org.telegram.utils.Permissions;
import org.telegram.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ru.aragats.aracle.ApplicationLoader;
import ru.aragats.wgo.dto.Coordinates;

/**
 * Created by aragats on 15/09/15.
 */
public class LocationManagerHelper {

    private static volatile LocationManagerHelper Instance = null;

    private LocationListener locationListener;

    private Location lastSavedLocation;

    private Location customLocation;

    private Geocoder geocoder;

//    private GoogleApiClient mGoogleApiClient;

    public static LocationManagerHelper getInstance() {
        LocationManagerHelper localInstance = Instance;
        if (localInstance == null) {
            synchronized (LocationManagerHelper.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new LocationManagerHelper();
                }
            }
        }
        return localInstance;
    }

    private LocationManagerHelper() {
//        mGoogleApiClient = new GoogleApiClient
//                .Builder(ApplicationLoader.applicationContext)
////                .enableAutoManage((FragmentActivity) ApplicationLoader.applicationContext, 0, null)
//                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
////                .addConnectionCallbacks(null)
////                .addOnConnectionFailedListener(null)
//                .build();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.connect();
//        }

        geocoder = new Geocoder(ApplicationLoader.applicationContext, Locale.ENGLISH);
        // Define a listener that responds to location updates

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //TODO is often ??
                // Called when a new location is found by the network location provider.
//                makeUseOfNewLocation(location);
//                Location loc = getLastLocation();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

    }

    //TODO move the method to other place.
    public void runLocationListener() {
        Activity activity = ApplicationLoader.parentActivity;
        //TODO check both permissions
        if (!Permissions.locationPermitted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            activity.requestPermissions(Permissions.LOCATION_PERMISSION_GROUP, Permissions.LOCATION_REQUEST_CODE);
            return;
        }

        LocationManager locationManager = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    public void stopLocationListener() {
        Activity activity = ApplicationLoader.parentActivity;
        //TODO check both permissions
        if (!Permissions.locationPermitted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            activity.requestPermissions(Permissions.LOCATION_PERMISSION_GROUP, Permissions.LOCATION_REQUEST_CODE);
            return;
        }
        LocationManager locationManager = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);

    }


    // cache
    //TODO-aragats method to get location
    // TODO It does not work all time. It return only the last location, but not search current location ((( .
    public Location getLastLocation() {
        if(!Permissions.checkLocationPermission(ApplicationLoader.parentActivity)){
            return null;
        }
        LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l = null;
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i)); // TODO potential error.
            if (l != null) {
                break;
            }
        }
        lastSavedLocation = l;
        return l;
    }

    public Location getLocation4TimeLine() {
        if (customLocation != null) {
            return customLocation;
        }
        return getLastLocation();
    }

    public boolean isLocationServiceEnabled() {
        if(!Permissions.checkLocationPermission(ApplicationLoader.parentActivity)){
            return false;
        }
        LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
        return (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

//    lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
//            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//    lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

    public Location getLastSavedLocation() {
        return lastSavedLocation;
    }

    public Location getLastSavedOrLastLocation() {
        if (lastSavedLocation != null) {
            return lastSavedLocation;
        }
        return getLastLocation();
    }


//    private ArrayList<PlaceAutocomplete> getPredictions(CharSequence constraint) {
//        if (mGoogleApiClient != null) {
//            Log.i(TAG, "Executing autocomplete query for: " + constraint);
//            PendingResult<AutocompletePredictionBuffer> results =
//                    Places.GeoDataApi
//                            .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
//                                    mBounds, mPlaceFilter);
//            // Wait for predictions, set the timeout.
//            AutocompletePredictionBuffer autocompletePredictions = results
//                    .await(60, TimeUnit.SECONDS);
//            final Status status = autocompletePredictions.getStatus();
//            if (!status.isSuccess()) {
//                Toast.makeText(getContext(), "Error: " + status.toString(),
//                        Toast.LENGTH_SHORT).show();
//                Log.e(TAG, "Error getting place predictions: " + status
//                        .toString());
//                autocompletePredictions.release();
//                return null;
//            }
//
//            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
//                    + " predictions.");
//            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
//            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
//            while (iterator.hasNext()) {
//                AutocompletePrediction prediction = iterator.next();
//                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
//                        prediction.getDescription()));
//            }
//            // Buffer release
//            autocompletePredictions.release();
//            return resultList;
//        }
//        Log.e(TAG, "Google API client is not connected.");
//        return null;
//    }


//    public void predictPlaceByName(String query) {
//
//        if (!mGoogleApiClient.isConnected()) {
//            return;
//        }
//
//        LatLngBounds latLngBounds = new LatLngBounds(
//                new LatLng(52.222485, 12.755897),
//                new LatLng(52.893462, 13.739561));
////        PendingResult<AutocompletePredictionBuffer> result =
////                Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query,
////                        latLngBounds, null);
//
//        Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query, latLngBounds, null)
//                .setResultCallback(
//                        new ResultCallback<AutocompletePredictionBuffer>() {
//                            @Override
//                            public void onResult(AutocompletePredictionBuffer buffer) {
//
//                                if (buffer == null)
//                                    return;
//
//                                if (buffer.getStatus().isSuccess()) {
//                                    for (AutocompletePrediction prediction : buffer) {
//                                        prediction.getPlaceId();
//                                        prediction.getDescription();
//                                        //Add as a new item to avoid IllegalArgumentsException when buffer is released
////                                        add( new AutoCompletePlace( prediction.getPlaceId(), prediction.getDescription() ) );
//                                    }
//                                }
//
//                                //Prevent memory leak by releasing buffer
//                                buffer.release();
//                            }
//                        }, 60, TimeUnit.SECONDS);
////
//        return;
//    }

    //TODO run in the thread
    //
    public List<Address> getAddressesFromLocationName(String locationName, int maxResults) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, maxResults);
            if (addresses == null) {
                addresses = new ArrayList<>();
            }
            return addresses;
        } catch (IOException ex) {
//            e.printStackTrace();
            //TODO logger
        }
        return new ArrayList<>();
    }

    //TODO run in the thread
    public String getAddress(Context context, double longitude, double latitude, String defaultVal) {
//        Geocoder geocoder;
        String address;
//        geocoder = new Geocoder(context, Locale.getDefault()); //TODO de ?? should be english ? or ?? in real time. Save in ENG, but display in locale in real-time
//        geocoder = new Geocoder(context, Locale.ENGLISH); //TODO de ?? should be english ? or ?? in real time. Save in ENG, but display in locale in real-time
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            //TODO what if addresses is empty NPE ?
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            if (StringUtils.isEmpty(address)) {
                address = defaultVal;
            }
//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
        } catch (IOException e) {
//            e.printStackTrace();
            //TODO logger
            address = defaultVal;
        }
        return address;
    }

    //TODO run in the thread
    public String getAddress(Context context, List<Double> coordinates, String defaultVal) {
//        Geocoder geocoder;
        String address;
//        geocoder = new Geocoder(context, Locale.getDefault()); //TODO de ?? should be english ? or ?? in real time. Save in ENG, but display in locale in real-time
//        geocoder = new Geocoder(context, Locale.ENGLISH); //TODO de ?? should be english ? or ?? in real time. Save in ENG, but display in locale in real-time
        try {
            List<Address> addresses = geocoder.getFromLocation(coordinates.get(1), coordinates.get(0), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            //TODO what if addresses is empty NPE ?
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            if (StringUtils.isEmpty(address)) {
                address = defaultVal;
            }
//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
        } catch (IOException e) {
//            e.printStackTrace();
            //TODO logger
            address = defaultVal;
        }
        return address;
    }

    //TODO run in the thread
    public String getAddress(Geocoder geocoder, double longitude, double latitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            //TODO what if addresses is empty NPE ?
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            return address;
        } catch (IOException e) {
//            e.printStackTrace();
            //TODO logger
            return "";
        }
    }


    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = ApplicationLoader.applicationContext.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public Location getCustomLocation() {
        return customLocation;
    }

    public void setCustomLocation(Location customLocation) {
        this.customLocation = customLocation;
    }


    public static Coordinates convertLocationToCoordinates(Location location) {
        if (location == null) {
            return null;
        }
        Coordinates coordinates = new Coordinates();
        coordinates.setCoordinates(Arrays.asList(location.getLongitude(), location.getLatitude()));
        coordinates.setType(Constants.POINT);
        return coordinates;
    }

    public static Location convertCoordinatesToLocation(Coordinates coordinates) {
        if (coordinates == null || CollectionUtils.isEmpty(coordinates.getCoordinates())) {
            return null;
        }
        List<Double> coords = coordinates.getCoordinates();
        Location location = new Location("network");
        location.setLatitude(coords.get(1));
        location.setLongitude(coords.get(0));
        return location;
    }

    public static Location convertGeoPointToLocation(TLRPC.GeoPoint geoPoint) {
        if (geoPoint == null) {
            return null;
        }
        Location location = new Location("network");
        location.setLatitude(geoPoint.lat);
        location.setLongitude(geoPoint._long);
        return location;
    }


    public static TLRPC.TL_messageMediaGeo convertCoordinatesToGeoPoint(Coordinates coordinates, boolean custom) {
        if (coordinates == null || CollectionUtils.isEmpty(coordinates.getCoordinates())) {
            return null;
        }
        List<Double> coords = coordinates.getCoordinates();
        TLRPC.TL_messageMediaGeo location = new TLRPC.TL_messageMediaGeo();
        location.geo = new TLRPC.TL_geoPoint();
        location.geo.lat = coords.get(1);
        location.geo._long = coords.get(0);
        location.isCustomLocation = custom;
        return location;
    }


}
