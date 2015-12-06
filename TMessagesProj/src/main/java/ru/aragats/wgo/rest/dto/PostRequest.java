package ru.aragats.wgo.rest.dto;

/**
 * Created by aragats on 05/12/15.
 */
public class PostRequest {

    private String filePath;

    public PostRequest() {
    }

    public PostRequest(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
