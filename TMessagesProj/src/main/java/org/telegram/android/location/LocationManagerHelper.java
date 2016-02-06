package org.telegram.android.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.telegram.utils.StringUtils;

import ru.aragats.wgo.ApplicationLoader;
import ru.aragats.wgo.dto.Coordinates;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by aragats on 15/09/15.
 */
public class LocationManagerHelper {

    private static volatile LocationManagerHelper Instance = null;

    private LocationListener locationListener;

    private Location lastSavedLocation;

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
        // Define a listener that responds to location updates

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //TODO is often ??
                // Called when a new location is found by the network location provider.
//                makeUseOfNewLocation(location);
                Location loc = getLastLocation();
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

    public Location getLastSavedLocation() {
        return lastSavedLocation;
    }


    public String getAddress(Context context, double longitude, double latitude, String defaultVal) {
        Geocoder geocoder;
        List<Address> addresses;
        String address;
//        geocoder = new Geocoder(context, Locale.getDefault()); //TODO de ?? should be english ? or ?? in real time. Save in ENG, but display in locale in real-time
        geocoder = new Geocoder(context, Locale.ENGLISH); //TODO de ?? should be english ? or ?? in real time. Save in ENG, but display in locale in real-time
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
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

    public String getAddress(Context context, List<Double> coordinates, String defaultVal) {
        Geocoder geocoder;
        List<Address> addresses;
        String address;
//        geocoder = new Geocoder(context, Locale.getDefault()); //TODO de ?? should be english ? or ?? in real time. Save in ENG, but display in locale in real-time
        geocoder = new Geocoder(context, Locale.ENGLISH); //TODO de ?? should be english ? or ?? in real time. Save in ENG, but display in locale in real-time
        try {
            addresses = geocoder.getFromLocation(coordinates.get(1), coordinates.get(0), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
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

    public String getAddress(Geocoder geocoder, double longitude, double latitude) {
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
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
}
