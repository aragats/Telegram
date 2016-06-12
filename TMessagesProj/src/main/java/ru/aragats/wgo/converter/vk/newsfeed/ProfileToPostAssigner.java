package ru.aragats.wgo.converter.vk.newsfeed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.Venue;
import ru.aragats.wgo.dto.vk.newsfeed.Profile;

/**
 * Created by aragats on 12/06/16.
 */
public class ProfileToPostAssigner {

    public void assign(List<Post> posts, List<Profile> profiles) {
        Map<Integer, Profile> profilesMap = new HashMap<>();
        for (Profile profile : profiles) {
            profilesMap.put(profile.getId(), profile);
        }
        for (Post post : posts) {
            Integer ownerId = post.getOwnerId();
            Profile profile = profilesMap.get(ownerId);
            Venue venue = post.getVenue();
            if (profile != null && venue != null) {
                venue.setName(profile.getFirstName() + " " + profile.getLastName());
                Image image = new Image();
                image.setUrl(profile.getPhoto100());
                venue.setIcon(image);
                venue.setUrl("https://vk.com/" + profile.getScreenName());
            }
        }
    }
}
