package ru.aragats.wgo.dto;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by aragats on 09/05/15.
 */
public class PostResponse {
    private List<Post> posts = new ArrayList<>();
    private String error;
    //TODO make it enum !!
    private String source;


    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
