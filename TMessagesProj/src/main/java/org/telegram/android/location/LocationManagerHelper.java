package org.telegram.android.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import ru.aragats.wgo.ApplicationLoader;

import java.util.List;

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
}
