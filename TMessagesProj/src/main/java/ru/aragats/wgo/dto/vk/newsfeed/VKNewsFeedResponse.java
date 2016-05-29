
package ru.aragats.wgo.dto.vk.newsfeed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VKNewsFeedResponse {

    @SerializedName("response")
    @Expose
    private NewsFeedResponse response;

    /**
     * 
     * @return
     *     The response
     */
    public NewsFeedResponse getResponse() {
        return response;
    }

    /**
     * 
     * @param response
     *     The response
     */
    public void setResponse(NewsFeedResponse response) {
        this.response = response;
    }

}
