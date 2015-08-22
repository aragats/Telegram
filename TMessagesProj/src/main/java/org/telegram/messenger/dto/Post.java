package org.telegram.messenger.dto;

import android.text.StaticLayout;


/**
 * Created by aragats on 27/12/14.
 */
public class Post {

    private String id;
    private String message;
    private Venue venue;
    private Coordinates coordinates;
    private Image previewImage;
    private Image image;
    private long createdDate;


    public Post() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Image getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(Image previewImage) {
        this.previewImage = previewImage;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }


    public String getVenuePreviewImageUrl() {
        if(this.getVenue() == null || this.getVenue().getImage() == null) {
            return null;
        }
        return this.getVenue().getImage().getUrl();
    }


    public String getPreviewImageUrl() {
        return this.getPreviewImage().getUrl();
    }

}
