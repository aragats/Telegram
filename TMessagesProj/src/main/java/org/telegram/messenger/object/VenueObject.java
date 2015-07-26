package org.telegram.messenger.object;


import org.telegram.messenger.dto.Venue;

/**
 * Created by aragats on 27/12/14.
 */
public class VenueObject {

    private Venue venue;

    public VenueObject() {

    }


    public VenueObject(Venue venue) {
        this.venue = venue;
    }


    public String getId() {
        return this.venue.getId();
    }


    public String getVenuePreviewImageUrl() {
        return this.venue.getPreviewImage().getUrl();
    }

    public String getAddress() {
        return this.venue.getAddress();
    }

    public String getDistanceStr() {
        return this.venue.getDistance() + " km";
    }

    public String getName() {
        return this.venue.getName();
    }

    public Venue getVenue() {
        return venue;
    }

    //
//
//
//    public long id;
//    private String name;
//    private String distanceStr;
//    private String address;
//    private float distance;
//
//    private String image;
//
//
//
//    //TODO should be enum.
//    private int type;
//
//
//
//
//
//    public long getId() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }
//
//
//    public String getDistanceStr() {
//        return distanceStr;
//    }
//
//    public void setDistanceStr(String distanceStr) {
//        this.distanceStr = distanceStr;
//    }
//
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public int getType() {
//        return type;
//    }
//
//    public void setType(int type) {
//        this.type = type;
//    }
//
//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }
//
//    public float getDistance() {
//        return distance;
//    }
//
//    public void setDistance(float distance) {
//        this.distance = distance;
//    }
//
//    public String getImage() {
//        return image;
//    }
//
//    public void setImage(String image) {
//        this.image = image;
//    }
}
