
package ru.aragats.wgo.dto.vk.newsfeed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Profile {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("sex")
    @Expose
    private Integer sex;
    @SerializedName("screen_name")
    @Expose
    private String screenName;
    @SerializedName("photo_50")
    @Expose
    private String photo50;
    @SerializedName("photo_100")
    @Expose
    private String photo100;
    @SerializedName("online")
    @Expose
    private Integer online;
    @SerializedName("online_app")
    @Expose
    private String onlineApp;
    @SerializedName("online_mobile")
    @Expose
    private Integer onlineMobile;

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
     *     The firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * 
     * @param firstName
     *     The first_name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * 
     * @return
     *     The lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * 
     * @param lastName
     *     The last_name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 
     * @return
     *     The sex
     */
    public Integer getSex() {
        return sex;
    }

    /**
     * 
     * @param sex
     *     The sex
     */
    public void setSex(Integer sex) {
        this.sex = sex;
    }

    /**
     * 
     * @return
     *     The screenName
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * 
     * @param screenName
     *     The screen_name
     */
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    /**
     * 
     * @return
     *     The photo50
     */
    public String getPhoto50() {
        return photo50;
    }

    /**
     * 
     * @param photo50
     *     The photo_50
     */
    public void setPhoto50(String photo50) {
        this.photo50 = photo50;
    }

    /**
     * 
     * @return
     *     The photo100
     */
    public String getPhoto100() {
        return photo100;
    }

    /**
     * 
     * @param photo100
     *     The photo_100
     */
    public void setPhoto100(String photo100) {
        this.photo100 = photo100;
    }

    /**
     * 
     * @return
     *     The online
     */
    public Integer getOnline() {
        return online;
    }

    /**
     * 
     * @param online
     *     The online
     */
    public void setOnline(Integer online) {
        this.online = online;
    }

    /**
     * 
     * @return
     *     The onlineApp
     */
    public String getOnlineApp() {
        return onlineApp;
    }

    /**
     * 
     * @param onlineApp
     *     The online_app
     */
    public void setOnlineApp(String onlineApp) {
        this.onlineApp = onlineApp;
    }

    /**
     * 
     * @return
     *     The onlineMobile
     */
    public Integer getOnlineMobile() {
        return onlineMobile;
    }

    /**
     * 
     * @param onlineMobile
     *     The online_mobile
     */
    public void setOnlineMobile(Integer onlineMobile) {
        this.onlineMobile = onlineMobile;
    }

}
