package ru.aragats.wgo.comparator;

import java.util.Comparator;

import ru.aragats.wgo.dto.Post;

/**
 * Created by aragats on 05/03/16.
 */
public class PostDateComparator implements Comparator<Post> {
    @Override
    public int compare(Post lhs, Post rhs) {
        return (int) (rhs.getCreatedDate() - lhs.getCreatedDate());
    }
}
