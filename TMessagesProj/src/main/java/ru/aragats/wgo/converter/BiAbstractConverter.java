package ru.aragats.wgo.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aragats on 06/02/16.
 */
public abstract class BiAbstractConverter<S, T> extends AbstractConverter<S, T> {


    public S inverseConvert(T target) {
        if (target == null) {
            return null;
        }
        return inverseConvertIntern(target);
    }

    public abstract S inverseConvertIntern(T target);


    public List<S> inverseConvert(List<T> targets) {
        List<S> result = new ArrayList<>();
        for (T target: targets) {
            if(target != null) {
                result.add(inverseConvert(target));
            }
        }
        return result;
//        return targets.stream().
//                filter(source -> source != null).
//                map(this::inverseConvert).
//                collect(Collectors.toList());
    }
}
