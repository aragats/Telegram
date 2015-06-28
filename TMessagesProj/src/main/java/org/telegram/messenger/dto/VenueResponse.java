package org.telegram.messenger.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aragats on 10/05/15.
 */
public class VenueResponse {
    private List<Venue> venues = new ArrayList<>();


    public List<Venue> getVenues() {
        return venues;
    }

    public void setVenues(List<Venue> venues) {
        this.venues = venues;
    }
}
