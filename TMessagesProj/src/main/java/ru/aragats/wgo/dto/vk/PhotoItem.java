
package ru.aragats.wgo.dto.vk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoItem {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("album_id")
    @Expose
    private Integer albumId;
    @SerializedName("owner_id")
    @Expose
    private Integer ownerId;
    @SerializedName("photo_75")
    @Expose
    private String photo75;
    @SerializedName("photo_130")
    @Expose
    private String photo130;
    @SerializedName("photo_604")
    @Expose
    private String photo604;
    @SerializedName("photo_807")
    @Expose
    private String photo807;
    @SerializedName("photo_1280")
    @Expose
    private String photo1280;
    @SerializedName("width")
    @Expose
    private Integer width;
    @SerializedName("height")
    @Expose
    private Integer height;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("date")
    @Expose
    private Integer date;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("long")
    @Expose
    private Double _long;
    @SerializedName("post_id")
    @Expose
    private Integer postId;
    @SerializedName("photo_2560")
    @Expose
    private String photo2560;

    /**
     * 
     * @return
     *     The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The albumId
     */
    public Integer getAlbumId() {
        return albumId;
    }

    /**
     * 
     * @param albumId
     *     The album_id
     */
    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }

    /**
     * 
     * @return
     *     The ownerId
     */
    public Integer getOwnerId() {
        return ownerId;
    }

    /**
     * 
     * @param ownerId
     *     The owner_id
     */
    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * 
     * @return
     *     The photo75
     */
    public String getPhoto75() {
        return photo75;
    }

    /**
     * 
     * @param photo75
     *     The photo_75
     */
    public void setPhoto75(String photo75) {
        this.photo75 = photo75;
    }

    /**
     * 
     * @return
     *     The photo130
     */
    public String getPhoto130() {
        return photo130;
    }

    /**
     * 
     * @param photo130
     *     The photo_130
     */
    public void setPhoto130(String photo130) {
        this.photo130 = photo130;
    }

    /**
     * 
     * @return
     *     The photo604
     */
    public String getPhoto604() {
        return photo604;
    }

    /**
     * 
     * @param photo604
     *     The photo_604
     */
    public void setPhoto604(String photo604) {
        this.photo604 = photo604;
    }

    /**
     * 
     * @return
     *     The photo807
     */
    public String getPhoto807() {
        return photo807;
    }

    /**
     * 
     * @param photo807
     *     The photo_807
     */
    public void setPhoto807(String photo807) {
        this.photo807 = photo807;
    }

    /**
     * 
     * @return
     *     The photo1280
     */
    public String getPhoto1280() {
        return photo1280;
    }

    /**
     * 
     * @param photo1280
     *     The photo_1280
     */
    public void setPhoto1280(String photo1280) {
        this.photo1280 = photo1280;
    }

    /**
     * 
     * @return
     *     The width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * 
     * @param width
     *     The width
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * 
     * @return
     *     The height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * 
     * @param height
     *     The height
     */
    public void setHeight(Integer height) {
        this.height = height;
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
     *     The date
     */
    public Integer getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The date
     */
    public void setDate(Integer date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The lat
     */
    public Double getLat() {
        return lat;
    }

    /**
     * 
     * @param lat
     *     The lat
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     * 
     * @return
     *     The _long
     */
    public Double getLong() {
        return _long;
    }

    /**
     * 
     * @param _long
     *     The long
     */
    public void setLong(Double _long) {
        this._long = _long;
    }

    /**
     * 
     * @return
     *     The postId
     */
    public Integer getPostId() {
        return postId;
    }

    /**
     * 
     * @param postId
     *     The post_id
     */
    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    /**
     * 
     * @return
     *     The photo2560
     */
    public String getPhoto2560() {
        return photo2560;
    }

    /**
     * 
     * @param photo2560
     *     The photo_2560
     */
    public void setPhoto2560(String photo2560) {
        this.photo2560 = photo2560;
    }

}
