package ru.aragats.wgo.rest.dto;

/**
 * Created by aragats on 05/12/15.
 */
public class PostRequest extends Request {

    private double lng;
    private double lat;
    private int distance;

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}



