package ru.aragats.wgo.converter.vk.photos;

import org.telegram.utils.Constants;
import org.telegram.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.aragats.wgo.converter.AbstractConverter;
import ru.aragats.wgo.dto.Coordinates;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.Venue;
import ru.aragats.wgo.dto.vk.photos.PhotoItem;

/**
 * Created by aragats on 06/02/16.
 */
public class PhotoItemToPostConverter extends AbstractConverter<PhotoItem, Post> {
    @Override
    public Post convertIntern(PhotoItem source) {
        //TODO validate height width and url if we can not build Post then return null and exclude it from the post list.
        if (source.getHeight() == null || source.getWidth() == null || StringUtils.isEmpty(source.getPhoto604())
                || StringUtils.isEmpty(source.getPhoto807()) || StringUtils.isEmpty(source.getPhoto1280())) {
            return null;
        }
        Post result = new Post();
        result.setId("" + source.getId());
        result.setOwnerId(source.getOwnerId());
        result.setCreatedDate(((long) source.getDate() * 1000));
        Coordinates coordinates = new Coordinates();
        coordinates.setType(Constants.POINT);
        coordinates.setCoordinates(Arrays.asList(source.getLong(), source.getLat()));
        result.setCoordinates(coordinates);
        result.setText(source.getText());
        Venue venue = new Venue();
        venue.setCoordinates(coordinates);
        venue.setName("VK");
        venue.setAddress("");
        venue.setUrl("https://vk.com/id" + source.getOwnerId());
        result.setVenue(venue);

        List<Image> images = new ArrayList<>();
        Image previewImage = new Image();
        previewImage.setHeight(source.getHeight()); // TODO wrong
        previewImage.setWidth(source.getWidth()); // TODO wrong
        previewImage.setUrl(source.getPhoto604());
        images.add(previewImage);


        Image image = new Image();
        image.setHeight(source.getHeight()); // TODO wrong
        image.setWidth(source.getWidth()); // TODO wrong
        image.setUrl(source.getPhoto1280());
        images.add(image);


        result.setImages(images);


        return result;
    }
}
