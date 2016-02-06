package ru.aragats.wgo.converter.vk;

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
        Post result = new Post();
        result.setId("" + source.getId());
        result.setCreatedDate(( (long)source.getDate() * 1000));
        Coordinates coordinates = new Coordinates();
        coordinates.setType("Point");
        coordinates.setCoordinates(Arrays.asList(source.getLong(), source.getLat()));
        result.setCoordinates(coordinates);
        result.setText(source.getText());
        Venue venue = new Venue();
        venue.setCoordinates(coordinates);
        venue.setName("");
        venue.setAddress("");
        result.setVenue(venue);

        List<Image> images = new ArrayList<>();
        Image previewImage = new Image();
        previewImage.setHeight(source.getHeight());
        previewImage.setWidth(source.getWidth());
        previewImage.setUrl(source.getPhoto604());
        images.add(previewImage);


        Image image = new Image();
        image.setHeight(source.getHeight());
        image.setWidth(source.getWidth());
        image.setUrl(source.getPhoto1280());
        images.add(image);


        result.setImages(images);


        return result;
    }
}
