package ru.aragats.wgo.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aragats on 06/02/16.
 */
public abstract class AbstractConverter<S, T> {
    public AbstractConverter() {

    }


    public T convert(S source) {
        if (source == null) {
            return null;
        }
        return convertIntern(source);
    }

    public abstract T convertIntern(S source);

    public List<T> convert(List<S> sources) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>();
        for (S src : sources) {
            if (src != null) {
                result.add(convert(src));
            }
        }
        return result;

//        return sources.stream().
//                filter(source -> source != null).
//                map(this::convert).
//                collect(Collectors.toList());
    }

    public List<T> convert(S[] sources) {
        if (sources == null || sources.length == 0) {
            return new ArrayList<>();
        }
        return convert(Arrays.asList(sources));
    }
}
