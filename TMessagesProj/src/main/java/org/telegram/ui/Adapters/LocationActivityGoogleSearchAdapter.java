package org.telegram.ui.Adapters;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.android.location.LocationManagerHelper;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.Cells.LocationCell;
import org.telegram.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aragats on 08/03/16.
 */
//
public class LocationActivityGoogleSearchAdapter extends LocationActivitySearchAdapter {


    private Context mContext;

    public LocationActivityGoogleSearchAdapter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = new LocationCell(mContext);
        }
        ((LocationCell) view).setLocation(places.get(i), null, i != places.size() - 1);
        return view;
    }


    public void searchGooglePlacesWithQuery(final String query, final Location coordinate) {
        if (lastSearchLocation != null && coordinate.distanceTo(lastSearchLocation) < 200) {
            return;
        }
        lastSearchLocation = coordinate;
        if (searching) {
            searching = false;
            requestQueue.cancelAll("search");
        }
        searching = true;
        //TODO run in the thread
        List<Address> addresses = LocationManagerHelper.getInstance().getAddressesFromLocationName(query, Constants.MAX_RESULTS);
        places = (ArrayList<TLRPC.TL_messageMediaVenue>) convertAddresses(addresses);
        searching = false;
        notifyDataSetChanged();
        if (delegate != null) {
            delegate.didLoadedSearchResult(places);
        }

        notifyDataSetChanged();
    }


    private TLRPC.TL_messageMediaVenue convertAddress(Address address) {
        TLRPC.TL_messageMediaVenue place = new TLRPC.TL_messageMediaVenue();
        place.title = address.getAddressLine(0);
        place.address = address.getAddressLine(1);
        place.geo = new TLRPC.GeoPoint();
        place.geo.lat = address.getLatitude();
        place.geo._long = address.getLongitude();
        return place;
    }

    private List<TLRPC.TL_messageMediaVenue> convertAddresses(List<Address> addresses) {
        List<TLRPC.TL_messageMediaVenue> result = new ArrayList<>();
        for (Address address : addresses) {
            result.add(convertAddress(address));
        }
        return result;
    }
}
