package org.telegram.messenger.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aragats on 09/05/15.
 */
public class Coordinates {
    //TODO enum. probably.
    private String type;
    private List<Double> coordinates = new ArrayList<Double>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
