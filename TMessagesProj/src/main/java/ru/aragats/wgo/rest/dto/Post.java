package ru.aragats.wgo.rest.dto;

/**
 * Created by aragats on 05/12/15.
 */
public class Post {
    private int id;
    private String name;

    public Post() {
    }

    public Post(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
