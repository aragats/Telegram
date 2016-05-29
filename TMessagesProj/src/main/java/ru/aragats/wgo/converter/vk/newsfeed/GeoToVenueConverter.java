package ru.aragats.wgo.converter.vk.newsfeed;

import org.telegram.utils.Constants;
import org.telegram.utils.StringUtils;

import java.util.Arrays;

import ru.aragats.wgo.converter.AbstractConverter;
import ru.aragats.wgo.converter.BiAbstractConverter;
import ru.aragats.wgo.dto.Coordinates;
import ru.aragats.wgo.dto.Venue;
import ru.aragats.wgo.dto.vk.newsfeed.Geo;
import ru.aragats.wgo.dto.vk.newsfeed.Place;

/**
 * Created by aragats on 29/05/16.
 */
public class GeoToVenueConverter extends AbstractConverter<Geo, Venue> {
    @Override
    public Venue convertIntern(Geo source) {
        String type = source.getType(); // point
        if (StringUtils.isEmpty(type) || !type.equalsIgnoreCase(Constants.POINT)) {
            return null;
        }
        Venue target = new Venue();
        Coordinates coordinates = new Coordinates();
        target.setCoordinates(coordinates);
        target.setName("");
        target.setAddress("");
        coordinates.setType("Point");
        String coordinatesStr = source.getCoordinates();
        if (!StringUtils.isEmpty(coordinatesStr)) {
            String[] coordinatesStrArr = coordinatesStr.trim().split("\\s+");
            if (coordinatesStrArr.length == 2) {
                coordinates.setCoordinates(Arrays.asList(Double.parseDouble(coordinatesStrArr[0]),
                        Double.parseDouble(coordinatesStrArr[1])));
            }
            Place place = source.getPlace();
            if (place != null && !StringUtils.isEmpty(place.getTitle())) {
                target.setAddress(place.getTitle());
            }
            if (StringUtils.isEmpty(target.getAddress())) {
                target.setName("VK");
            }

        }
        return target;
    }
}
