package ru.aragats.wgo.comparator;

import java.util.Comparator;

import ru.aragats.wgo.dto.Post;

/**
 * Created by aragats on 05/03/16.
 */
public class PostDistanceComparator implements Comparator<Post> {
    @Override
    public int compare(Post lhs, Post rhs) {
        float leftDistance = lhs.getDistance();
        float rightDistance = rhs.getDistance();
        if (leftDistance > rightDistance) {
            return 1;
        } else if (leftDistance < rightDistance) {
            return -1;
        } else {
            return 0;
        }
//                    return (int) (rhs.getCreatedDate() - lhs.getCreatedDate());
    }
}
