package org.telegram.messenger.object;

import android.text.StaticLayout;

import org.telegram.messenger.dto.Image;
import org.telegram.messenger.dto.Post;


/**
 * Created by aragats on 27/12/14.
 */
public class PostObject {

    public static class TextLayoutBlock {
        public StaticLayout textLayout;
        public float textXOffset = 0;
        public float textYOffset = 0;
        public int charactersOffset = 0;
    }

    private Post post;


    public PostObject() {

    }

    public PostObject(Post post) {
        this.post = post;
    }


    public String getId() {
        return this.post.getId();
    }

    public long getCreatedDate() {
        return this.post.getCreatedDate();
    }

    public String getMessage() {
        return this.post.getMessage();
    }

    public String getAuthor() {
        return this.post.getUser().getFirstName() + " " + post.getUser().getLastName();
    }

    public String getVenueName() {
        return this.post.getVenue().getName();
    }

    public String getPreviewImageUrl() {
        return this.post.getPreviewImage().getUrl();
    }

    public String getVenuePreviewImageUrl() {
        return this.post.getVenue().getImage().getUrl();
    }

    public Image getPreviewImage() {
        return this.post.getPreviewImage();
    }

    public Image getImage() {
        return this.post.getImage();
    }

//    public PostObject(TLRPC.TL_dialog dialog) {
//        peer = dialog.peer;
//        top_message = dialog.top_message;
//        unread_count = dialog.unread_count;
//        notify_settings = dialog.notify_settings;
//        last_message_date = dialog.last_message_date;
//        id = dialog.id;
//        last_read = dialog.last_read;
//
//    }
//    //TODO change to private
//    public TLRPC.Peer peer;
//
//    public int top_message;
//    public int unread_count;
//    public TLRPC.PeerNotifySettings notify_settings;
//
//    public int last_message_date;
//    public int last_read;
//
//    public long id;
//    private String message;
//    private int date;
//    private String user;
//    private String venue;
//    private String firstName;
//    private String lastName;
//    private String image;
//    private int width;
//    private int height;
//
//    //TODO should be enum.
//    private int type;
//
//    private String attachPath;
//
//    private int size;
//
//    private String info;
//
//
//    private String text;
//
//    private String fullImage;
//
//
//
//    public void setPostFromDialog(TLRPC.TL_dialog dialog) {
//        peer = dialog.peer;
//        top_message = dialog.top_message;
//        unread_count = dialog.unread_count;
//        notify_settings = dialog.notify_settings;
//        last_message_date = dialog.last_message_date;
//        id = dialog.id;
//        last_read = dialog.last_read;
//
//    }
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
//    public int getDate() {
//        return date;
//    }
//
//    public void setDate(int date) {
//        this.date = date;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public String getUser() {
//        return user;
//    }
//
//    public void setUser(String user) {
//        this.user = user;
//    }
//
//    public String getVenue() {
//        return venue;
//    }
//
//    public void setVenue(String venue) {
//        this.venue = venue;
//    }
//
//    public String getFirstName() {
//        return firstName;
//    }
//
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }
//
//    public String getImage() {
//        return image;
//    }
//
//    public void setImage(String image) {
//        this.image = image;
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
//    public String getAttachPath() {
//        return attachPath;
//    }
//
//    public void setAttachPath(String attachPath) {
//        this.attachPath = attachPath;
//    }
//
//    public int getSize() {
//        return size;
//    }
//
//    public void setSize(int size) {
//        this.size = size;
//    }
//
//    public String getInfo() {
//        return info;
//    }
//
//    public void setInfo(String info) {
//        this.info = info;
//    }
//
//    public int getWidth() {
//        return width;
//    }
//
//    public void setWidth(int width) {
//        this.width = width;
//    }
//
//    public int getHeight() {
//        return height;
//    }
//
//    public void setHeight(int height) {
//        this.height = height;
//    }
//
//    public String getText() {
//        return text;
//    }
//
//    public void setText(String text) {
//        this.text = text;
//    }
//
//    public String getFullImage() {
//        return fullImage;
//    }
//
//    public void setFullImage(String fullImage) {
//        this.fullImage = fullImage;
//    }
}
