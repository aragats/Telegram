
package ru.aragats.wgo.dto.vk.newsfeed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Comments {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("can_post")
    @Expose
    private int canPost;

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
     *     The canPost
     */
    public int getCanPost() {
        return canPost;
    }

    /**
     * 
     * @param canPost
     *     The can_post
     */
    public void setCanPost(int canPost) {
        this.canPost = canPost;
    }

}
