package ru.aragats.wgo.converter.vk.newsfeed;

import java.util.ArrayList;
import java.util.List;

import ru.aragats.wgo.converter.AbstractConverter;
import ru.aragats.wgo.converter.vk.photos.PhotoItemToPostConverter;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.vk.newsfeed.NewsFeedItem;
import ru.aragats.wgo.dto.vk.newsfeed.NewsFeedResponse;
import ru.aragats.wgo.dto.vk.photos.PhotoResponse;

/**
 * Created by aragats on 06/02/16.
 */
public class VKNewsFeedResponseToPostListConverter extends AbstractConverter<NewsFeedResponse, List<Post>> {

    private NewsFeedItemToPostConverter newsFeedItemToPostConverter = new NewsFeedItemToPostConverter();

    @Override
    public List<Post> convertIntern(NewsFeedResponse source) {
        List<Post> result = new ArrayList<>();
        if (source.getCount() == 0) {
            return result;
        }
        result.addAll(newsFeedItemToPostConverter.convert(source.getItems()));
        return result;

    }
}
