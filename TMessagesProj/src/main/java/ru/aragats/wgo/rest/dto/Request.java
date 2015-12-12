package ru.aragats.wgo.rest.dto;

/**
 * Created by aragats on 05/12/15.
 */
public class Request {

    private String offset;
    private int count;

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
