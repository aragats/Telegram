package ru.aragats.wgo.converter.vk.newsfeed;

import org.telegram.utils.CollectionUtils;
import org.telegram.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.aragats.wgo.converter.AbstractConverter;
import ru.aragats.wgo.dto.Coordinates;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.Venue;
import ru.aragats.wgo.dto.vk.newsfeed.Attachment;
import ru.aragats.wgo.dto.vk.newsfeed.Geo;
import ru.aragats.wgo.dto.vk.newsfeed.NewsFeedItem;
import ru.aragats.wgo.dto.vk.newsfeed.NewsFeedResponse;
import ru.aragats.wgo.dto.vk.newsfeed.Photo;
import ru.aragats.wgo.dto.vk.newsfeed.Place;
import ru.aragats.wgo.dto.vk.photos.PhotoItem;

/**
 * Created by aragats on 06/02/16.
 */
public class NewsFeedItemToPostConverter extends AbstractConverter<NewsFeedItem, Post> {
    private GeoToVenueConverter geoToVenueConverter = new GeoToVenueConverter();
    private PhotoToImagesConverter photoToImagesConverter = new PhotoToImagesConverter();


    @Override
    public Post convertIntern(NewsFeedItem source) {
        List<Attachment> attachments = source.getAttachments();
        if (CollectionUtils.isEmpty(attachments)) {
            return null;
        }

        List<Image> images = photoToImagesConverter.convert(attachments.get(0).getPhoto());
        if (CollectionUtils.isEmpty(images)) {
            return null;
        }
        Venue venue = geoToVenueConverter.convert(source.getGeo());
        if (venue == null || venue.getCoordinates() == null) {
            return null;
        }
        Post result = new Post();
        result.setId("" + source.getId());
        result.setCreatedDate(((long) source.getDate() * 1000));
        result.setCoordinates(venue.getCoordinates());
        result.setText(source.getText());
        result.setVenue(venue);
        result.setImages(images);

        if (source.getLikes() != null) {
            result.setLikes(source.getLikes().getCount());
        }
        result.setOwnerId(source.getOwnerId());

        //TODO mock
//        result.setText("");

        return result;
    }


}
