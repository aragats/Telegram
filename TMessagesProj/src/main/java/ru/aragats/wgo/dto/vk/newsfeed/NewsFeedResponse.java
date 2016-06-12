
package ru.aragats.wgo.dto.vk.newsfeed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedResponse {

    @SerializedName("items")
    @Expose
    private List<NewsFeedItem> items = new ArrayList<NewsFeedItem>();
    @SerializedName("profiles")
    @Expose
    private List<Profile> profiles = new ArrayList<Profile>();
    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("total_count")
    @Expose
    private int totalCount;
    @SerializedName("next_from")
    @Expose
    private String nextFrom;

    /**
     * 
     * @return
     *     The items
     */
    public List<NewsFeedItem> getItems() {
        return items;
    }

    /**
     * 
     * @param items
     *     The items
     */
    public void setItems(List<NewsFeedItem> items) {
        this.items = items;
    }

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
     *     The totalCount
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * 
     * @param totalCount
     *     The total_count
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * 
     * @return
     *     The nextFrom
     */
    public String getNextFrom() {
        return nextFrom;
    }

    /**
     * 
     * @param nextFrom
     *     The next_from
     */
    public void setNextFrom(String nextFrom) {
        this.nextFrom = nextFrom;
    }


    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }
}
