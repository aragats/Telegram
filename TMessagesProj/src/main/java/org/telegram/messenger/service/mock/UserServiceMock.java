package org.telegram.messenger.service.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.aragats.whats.dto.Image;
import ru.aragats.whats.dto.User;

import static ru.aragats.whats.service.PostServiceMock.*;

/**
 * Created by aragats on 09/05/15.
 */
public class UserServiceMock {


    private static List<User> users = new ArrayList<>();

    static {
        users.addAll(generateUsers(5));
    }


    public static User generateUser() {
        User result = new User();
        result.setId("" + (long) (Math.random() * 100));
        result.setEmail(generateString(SEED_1, 5) + "@" + generateString(SEED_1, 5));
        result.setFirstName(generateString(SEED_1, 5));
        result.setLastName(generateString(SEED_1, 5));
        result.setUsername(generateString(SEED_1, 5));
        Image image = ImageServiceMock.getRandomImage();
        result.setImage(image);
        result.setPreviewImage(image);
        return result;
    }


    public static List<User> generateUsers(int count) {
        List<User> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(generateUser());
        }
        return result;
    }

    public static User getRandomUser() {
        return users.get(new Random().nextInt(users.size()));
    }


}
