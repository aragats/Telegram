package org.telegram.messenger.service.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.aragats.whats.dto.Image;

/**
 * Created by aragats on 09/05/15.
 */
public class ImageServiceMock {
    private static List<String> imageURLs = new ArrayList<>();
    private static List<Image> images = new ArrayList<>();

    static {
        images.addAll(generateImages());
    }


    public static List<Image> generateImages() {
        List<Image> images = new ArrayList<>();
        images.add(generateImage1());
        images.add(generateImage2());
        return images;

    }

    public static Image generateImage1() {
        Image result = new Image();
        result.setSize(0);
        result.setHeight(434);
        result.setWidth(550);
        result.setUrl("http://cs623223.vk.me/v623223105/11514/9LDXX9MyXyY.jpg");
        return result;
    }

    public static Image generateImage2() {
        Image result = new Image();
        result.setSize(0);
        result.setHeight(1224);
        result.setWidth(1632);
        result.setUrl("http://cs622031.vk.me/v622031634/2af54/Rba5Mi6iPNw.jpg");
        return result;
    }

    public static Image getRandomImage() {
        return images.get(new Random().nextInt(images.size()));
    }
}
