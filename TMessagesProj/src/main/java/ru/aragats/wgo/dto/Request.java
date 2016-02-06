package ru.aragats.wgo.dto;

/**
 * Created by aragats on 05/12/15.
 */
public class Request {

    private int offset;
    private String idOffset;
    private int count;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getIdOffset() {
        return idOffset;
    }

    public void setIdOffset(String idOffset) {
        this.idOffset = idOffset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
