package ru.aragats.wgo.converter.vk;

import org.telegram.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.aragats.wgo.converter.AbstractConverter;
import ru.aragats.wgo.dto.Coordinates;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.Venue;
import ru.aragats.wgo.dto.vk.PhotoItem;

/**
 * Created by aragats on 06/02/16.
 */
public class PhotoItemToPostConverter extends AbstractConverter<PhotoItem, Post> {
    @Override
    public Post convertIntern(PhotoItem source) {
        //TODO validate height width and url if we can not build Post then return null and exlude it from the post list.
        Post result = new Post();
        result.setId("" + source.getId());
        result.setCreatedDate(((long) source.getDate() * 1000));
        Coordinates coordinates = new Coordinates();
        coordinates.setType("Point");
        coordinates.setCoordinates(Arrays.asList(source.getLong(), source.getLat()));
        result.setCoordinates(coordinates);
        result.setText(source.getText());
        Venue venue = new Venue();
        venue.setCoordinates(coordinates);
        venue.setName("VK");
        venue.setAddress("");
        result.setVenue(venue);

        List<Image> images = new ArrayList<>();
        Image previewImage = new Image();
        //TODO if height or width  == 0 then throw this item
        previewImage.setHeight(source.getHeight());
        previewImage.setWidth(source.getWidth());
        previewImage.setUrl(source.getPhoto604());
        images.add(previewImage);


        Image image = new Image();
        image.setHeight(source.getHeight());
        image.setWidth(source.getWidth());
        if(!StringUtils.isEmpty(source.getPhoto1280())) {
            image.setUrl(source.getPhoto1280());
        } else {
            image.setUrl(source.getPhoto604());
        }
        images.add(image);


        result.setImages(images);


        return result;
    }
}
