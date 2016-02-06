package ru.aragats.wgo.rest.mock;

import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import static ru.aragats.wgo.rest.mock.PostServiceMock.*;

/**
 * Created by aragats on 09/05/15.
 */
public class UserServiceMock {


    private static List<User> users = new ArrayList<>();
    private static User defaultUser;

    static {
        users.addAll(generateUsers(5));

        defaultUser = new User();
        defaultUser.setId(Long.MAX_VALUE + "");
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
        result.setPhone("4912131414124");
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

    public static User getDefaultUser() {
        return defaultUser;
    }


}
