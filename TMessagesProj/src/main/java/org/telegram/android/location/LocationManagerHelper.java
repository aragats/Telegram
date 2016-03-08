package org.telegram.android.location;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.telegram.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.aragats.wgo.ApplicationLoader;

/**
 * Created by aragats on 15/09/15.
 */
public class LocationManagerHelper {

    private static volatile LocationManagerHelper Instance = null;

    private LocationListener locationListener;

    private Location lastSavedLocation;

    private Location customLocation;

    private Geocoder geocoder;

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
        LocationManager locationManager = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    public void stopLocationListener() {
        LocationManager locationManager = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);

    }


    // cache
    //TODO-aragats method to get location
    // TODO It does not work all time. It return only the last location, but not search current location ((( .
    public Location getLastLocation() {
        LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l = null;
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
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
}
