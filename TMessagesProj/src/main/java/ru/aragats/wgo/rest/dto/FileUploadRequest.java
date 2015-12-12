package ru.aragats.wgo.rest.dto;

/**
 * Created by aragats on 12/12/15.
 */
public class FileUploadRequest {

    private String filePath;
    private String contentType;

    public FileUploadRequest() {
    }

    public FileUploadRequest(String filePath, String contentType) {
        this.filePath = filePath;
        this.contentType = contentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
