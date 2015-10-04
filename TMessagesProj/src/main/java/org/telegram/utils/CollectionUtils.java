package org.telegram.utils;

import java.util.List;

/**
 * Created by aragats on 04/10/15.
 */
public class CollectionUtils {

    public static boolean isEmpty(List list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        return false;

    }
}
