package ru.aragats.wgo.dto;


import org.telegram.utils.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aragats on 27/12/14.
 */
public class Post implements Serializable {

    private String id;
    private String text;
    private Venue venue;
    private Coordinates coordinates;
    //wall, preview, original (almost). increase resolution. ????
    private List<Image> images;
    private long createdDate;

    //Anonymized userId
    private String userId;

    //  transient
    private transient float distance;
    private transient boolean local;
    private transient int likes;
    private transient int ownerId;


    public Post() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    //TODO rethink about using such methods.
    public Image getPreviewImage() {
        if (!CollectionUtils.isEmpty(images)) {
            return images.get(0);
        }
        return null;
    }

    public Image getImage() {
        if (!CollectionUtils.isEmpty(images)) {
            if (images.size() == 2) {
                return images.get(1);
            } else {
                return images.get(0);
            }
        }
        return null;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }


    public String getVenuePreviewImageUrl() {
        if (this.getVenue() == null || this.getVenue().getIcon() == null) {
            return null;
        }
        return this.getVenue().getIcon().getUrl();
    }


    public String getPreviewImageUrl() {
        return this.getPreviewImage().getUrl();
    }


    //TODO delete ???
    public Coordinates getPostCoordinates() {
        if (venue != null && venue.getCoordinates() != null) {
            return venue.getCoordinates();
        } else {
            return coordinates;
        }
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }


    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
