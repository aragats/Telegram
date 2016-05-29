
package ru.aragats.wgo.dto.vk.photos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VKPhotoResponse {

    @SerializedName("response")
    @Expose
    private PhotoResponse response;

    /**
     * 
     * @return
     *     The response
     */
    public PhotoResponse getResponse() {
        return response;
    }

    /**
     * 
     * @param response
     *     The response
     */
    public void setResponse(PhotoResponse response) {
        this.response = response;
    }

}
