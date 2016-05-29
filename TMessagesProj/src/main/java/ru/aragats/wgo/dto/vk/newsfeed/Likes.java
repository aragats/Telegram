
package ru.aragats.wgo.dto.vk.newsfeed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Likes {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("user_likes")
    @Expose
    private int userLikes;
    @SerializedName("can_like")
    @Expose
    private int canLike;
    @SerializedName("can_publish")
    @Expose
    private int canPublish;

    /**
     * 
     * @return
     *     The count
     */
    public int getCount() {
        return count;
    }

    /**
     * 
     * @param count
     *     The count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 
     * @return
     *     The userLikes
     */
    public int getUserLikes() {
        return userLikes;
    }

    /**
     * 
     * @param userLikes
     *     The user_likes
     */
    public void setUserLikes(int userLikes) {
        this.userLikes = userLikes;
    }

    /**
     * 
     * @return
     *     The canLike
     */
    public int getCanLike() {
        return canLike;
    }

    /**
     * 
     * @param canLike
     *     The can_like
     */
    public void setCanLike(int canLike) {
        this.canLike = canLike;
    }

    /**
     * 
     * @return
     *     The canPublish
     */
    public int getCanPublish() {
        return canPublish;
    }

    /**
     * 
     * @param canPublish
     *     The can_publish
     */
    public void setCanPublish(int canPublish) {
        this.canPublish = canPublish;
    }

}
