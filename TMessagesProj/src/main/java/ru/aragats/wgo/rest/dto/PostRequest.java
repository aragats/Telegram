package ru.aragats.wgo.rest.dto;

/**
 * Created by aragats on 05/12/15.
 */
public class PostRequest extends Request {

    private double longitude;
    private double latitude;
    private int distance;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}



