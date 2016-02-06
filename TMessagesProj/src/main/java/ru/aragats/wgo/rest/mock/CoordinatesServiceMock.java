package ru.aragats.wgo.rest.mock;

import ru.aragats.wgo.dto.Coordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * Created by aragats on 10/05/15.
 */
public class CoordinatesServiceMock {

    public static List<Coordinates> coordinatesList = new ArrayList<>();

    static {
        coordinatesList.addAll(generateCoordinatesList(5));

    }

    public static Coordinates generateCoordinates() {
        Coordinates result = new Coordinates();
        result.setType("Point");
        Random random = new Random();
        result.setCoordinates(Arrays.asList(180 * random.nextDouble(), 90 * random.nextDouble()));
        return result;
    }

    public static List<Coordinates> generateCoordinatesList(int count) {
        List<Coordinates> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(generateCoordinates());
        }
        return result;

    }

    public static Coordinates getRandomCoordinates() {
        return coordinatesList.get(new Random().nextInt(coordinatesList.size()));
    }

}
