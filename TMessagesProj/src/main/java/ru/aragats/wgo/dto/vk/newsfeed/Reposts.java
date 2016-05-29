
package ru.aragats.wgo.dto.vk.newsfeed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Reposts {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("user_reposted")
    @Expose
    private int userReposted;

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
     *     The userReposted
     */
    public int getUserReposted() {
        return userReposted;
    }

    /**
     * 
     * @param userReposted
     *     The user_reposted
     */
    public void setUserReposted(int userReposted) {
        this.userReposted = userReposted;
    }

}
