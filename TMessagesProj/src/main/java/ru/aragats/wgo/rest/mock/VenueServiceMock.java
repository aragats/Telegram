package ru.aragats.wgo.rest.mock;

import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.Venue;
import ru.aragats.wgo.dto.VenueResponse;
import org.telegram.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by aragats on 10/05/15.
 */
public class VenueServiceMock {

    private static List<Venue> venues = new ArrayList<>();

    static {
        venues.addAll(generateVenues(60));

    }

    public static VenueResponse getVenues(String location, String query, final int offset, final int count) {
        int end = offset + count;

        VenueResponse result = new VenueResponse();
        if (!StringUtils.isEmpty(query)) {
            List<Venue> venues = searchVenues(query);
            if (venues.isEmpty() || offset > venues.size()) {
                result.setVenues(new ArrayList<Venue>());
                return result;
            }
            result.setVenues(venues.subList(offset, end > venues.size() ? venues.size() : end));
        } else {
            if (offset > venues.size()) {
                result.setVenues(new ArrayList<Venue>());
                return result;
            }
            result.setVenues(venues.subList(offset, end > venues.size() ? venues.size() : end));
        }
        return result;
    }


    private static List<Venue> searchVenues(String query) {
        List<Venue> result = new ArrayList<>();
        for (Venue venue : venues) {
            if (venue.getName().contains(query)) {
                result.add(venue);
            }
        }
        return result;

    }


    public static Venue generateVenue(int i) {
        Venue result = new Venue();
        result.setId("" + i);
        Image image = ImageServiceMock.getRandomImage();
        result.setIcon(image);
        result.setAddress(PostServiceMock.generateString("ab c de fg rty  ui o ph jk", 60));
        result.setName(PostServiceMock.generateString("ab c de fg rty  ui o ph jk", 60));
        result.setCoordinates(CoordinatesServiceMock.getRandomCoordinates());
        return result;
    }


    public static List<Venue> generateVenues(int count) {
        List<Venue> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(generateVenue(i));
        }
        return result;
    }


    public static Venue getRandomVenue() {
        return venues.get(new Random().nextInt(venues.size()));
    }
}
