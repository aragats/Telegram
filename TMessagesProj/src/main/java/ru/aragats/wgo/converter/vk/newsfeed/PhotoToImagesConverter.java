package ru.aragats.wgo.converter.vk.newsfeed;

import org.telegram.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import ru.aragats.wgo.converter.AbstractConverter;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.vk.newsfeed.Attachment;
import ru.aragats.wgo.dto.vk.newsfeed.Photo;

/**
 * Created by aragats on 29/05/16.
 */
public class PhotoToImagesConverter extends AbstractConverter<Photo, List<Image>> {
    @Override
    public List<Image> convertIntern(Photo source) {
        if (StringUtils.isEmpty(source.getPhoto604())
                || StringUtils.isEmpty(source.getPhoto807())
                || StringUtils.isEmpty(source.getPhoto1280())) {
            return null;
        }

        List<Image> target = new ArrayList<>();
        Image previewImage = new Image();
        previewImage.setUrl(source.getPhoto604());
        previewImage.setWidth(source.getWidth());   // TODO wrong
        previewImage.setHeight(source.getHeight()); // TODO wrong
        target.add(previewImage);

        Image image = new Image();
        image.setUrl(source.getPhoto1280());
        image.setWidth(source.getWidth());   // TODO wrong
        image.setHeight(source.getHeight()); // TODO wrong
        target.add(image);
        return target;
    }
}
