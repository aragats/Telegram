package ru.aragats.wgo.rest.dto;

/**
 * Created by aragats on 12/12/15.
 */
public class VenuePostsRequest extends Request {

    private String venueId;


    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }
}
