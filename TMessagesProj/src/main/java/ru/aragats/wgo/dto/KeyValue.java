package ru.aragats.wgo.dto;

/**
 * Created by aragats on 07/05/16.
 */
public class KeyValue {
    String key;

    public KeyValue() {
    }

    public KeyValue(String value) {
        this.key = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}