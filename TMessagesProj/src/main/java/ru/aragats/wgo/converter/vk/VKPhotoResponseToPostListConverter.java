package ru.aragats.wgo.converter.vk;

import java.util.ArrayList;
import java.util.List;

import ru.aragats.wgo.converter.AbstractConverter;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.vk.PhotoItem;
import ru.aragats.wgo.dto.vk.PhotoResponse;

/**
 * Created by aragats on 06/02/16.
 */
public class VKPhotoResponseToPostListConverter extends AbstractConverter<PhotoResponse, List<Post>> {

    private PhotoItemToPostConverter photoItemToPostConverter = new PhotoItemToPostConverter();

    @Override
    public List<Post> convertIntern(PhotoResponse source) {
        List<Post> result = new ArrayList<>();
        if (source.getCount() == 0) {
            return result;
        }
        result.addAll(photoItemToPostConverter.convert(source.getItems()));
        return result;

    }
}
