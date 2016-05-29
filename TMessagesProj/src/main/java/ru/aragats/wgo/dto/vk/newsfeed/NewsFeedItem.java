
package ru.aragats.wgo.dto.vk.newsfeed;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NewsFeedItem {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("date")
    @Expose
    private int date;
    @SerializedName("owner_id")
    @Expose
    private int ownerId;
    @SerializedName("from_id")
    @Expose
    private int fromId;
    @SerializedName("post_type")
    @Expose
    private String postType;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("attachments")
    @Expose
    private List<Attachment> attachments = new ArrayList<Attachment>();
    @SerializedName("geo")
    @Expose
    private Geo geo;
    @SerializedName("post_source")
    @Expose
    private PostSource postSource;
    @SerializedName("comments")
    @Expose
    private Comments comments;
    @SerializedName("likes")
    @Expose
    private Likes likes;
    @SerializedName("reposts")
    @Expose
    private Reposts reposts;

    /**
     * 
     * @return
     *     The id
     */
    public int getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The date
     */
    public int getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The date
     */
    public void setDate(int date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The ownerId
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * 
     * @param ownerId
     *     The owner_id
     */
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * 
     * @return
     *     The fromId
     */
    public int getFromId() {
        return fromId;
    }

    /**
     * 
     * @param fromId
     *     The from_id
     */
    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    /**
     * 
     * @return
     *     The postType
     */
    public String getPostType() {
        return postType;
    }

    /**
     * 
     * @param postType
     *     The post_type
     */
    public void setPostType(String postType) {
        this.postType = postType;
    }

    /**
     * 
     * @return
     *     The text
     */
    public String getText() {
        return text;
    }

    /**
     * 
     * @param text
     *     The text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 
     * @return
     *     The attachments
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * 
     * @param attachments
     *     The attachments
     */
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * 
     * @return
     *     The geo
     */
    public Geo getGeo() {
        return geo;
    }

    /**
     * 
     * @param geo
     *     The geo
     */
    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    /**
     * 
     * @return
     *     The postSource
     */
    public PostSource getPostSource() {
        return postSource;
    }

    /**
     * 
     * @param postSource
     *     The post_source
     */
    public void setPostSource(PostSource postSource) {
        this.postSource = postSource;
    }

    /**
     * 
     * @return
     *     The comments
     */
    public Comments getComments() {
        return comments;
    }

    /**
     * 
     * @param comments
     *     The comments
     */
    public void setComments(Comments comments) {
        this.comments = comments;
    }

    /**
     * 
     * @return
     *     The likes
     */
    public Likes getLikes() {
        return likes;
    }

    /**
     * 
     * @param likes
     *     The likes
     */
    public void setLikes(Likes likes) {
        this.likes = likes;
    }

    /**
     * 
     * @return
     *     The reposts
     */
    public Reposts getReposts() {
        return reposts;
    }

    /**
     * 
     * @param reposts
     *     The reposts
     */
    public void setReposts(Reposts reposts) {
        this.reposts = reposts;
    }

}
