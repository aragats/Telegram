package org.telegram.messenger.service.mock;

import org.telegram.messenger.dto.Image;
import org.telegram.messenger.dto.Post;
import org.telegram.messenger.dto.PostResponse;
import org.telegram.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * Created by aragats on 09/05/15.
 */
public class PostServiceMock {

    public static final String SEED_1 = "abcdefgrtyuiophjk";
    public static final String SEED_2 = "ab c de fg rty  ui o ph jk";

    private static List<Post> posts = new ArrayList<>();

    static {
        posts.addAll(generatePosts(10));
    }

    public static void addPost(Post post) {
        posts.add(post);
    }

    public static PostResponse getPosts(String location, String query, final int offset, final int count) {
        PostResponse response = new PostResponse();
        int end = offset + count;
        if (!StringUtils.isEmpty(query)) {
            List<Post> posts = searchPosts(query);
            if (posts.isEmpty() || offset > posts.size()) {
                response.setPosts(new ArrayList<Post>());
                return response;
            }
            response.setPosts(posts.subList(offset, end > posts.size() ? posts.size() : end));
        } else {
            if (offset > posts.size()) {
                response.setPosts(new ArrayList<Post>());
                return response;
            }
            response.setPosts(posts.subList(offset, end > posts.size() ? posts.size() : end));
        }
        return response;
    }

    private static List<Post> searchPosts(String query) {
        List<Post> result = new ArrayList<>();
        for (Post post : posts) {
            if (post.getMessage().contains(query)) {
                result.add(post);
            }
        }
        return result;

    }


    public static List<Post> getPosts() {
        return posts;
    }


    public static Post generatePost(int i) {
        Post post = new Post();
        post.setId("" + i);
        post.setCreatedDate(new Date().getTime());
        post.setMessage(generateString("ab c de fg rty  ui o ph jk", 150));
        Image image = ImageServiceMock.getRandomImage();
        post.setPreviewImage(image);
        post.setImage(image);
        post.setVenue(VenueServiceMock.getRandomVenue());
        return post;
    }

    public static List<Post> generatePosts(int count) {
        List<Post> postObjects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            postObjects.add(generatePost(i));
        }
        return postObjects;

    }


    //    post.id + generateString(new Random(), "abcdef", 5);
    public static String generateString(String characters, int length) {
        Random rng = new Random();
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }


//    public static List<PostObject> convertPost(List<Post> posts) {
//        List<PostObject> result = new ArrayList<>();
//        for (Post post : posts) {
//            result.add(new PostObject(post));
//        }
//        return result;
//    }


}
