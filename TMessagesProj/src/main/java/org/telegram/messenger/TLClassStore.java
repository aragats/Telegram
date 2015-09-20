/*
 * This is the source code of Telegram for Android v. 2.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.telegram.messenger;

import java.util.HashMap;

public class TLClassStore {
    private HashMap<Integer, Class> classStore;

    public TLClassStore() {
        classStore = new HashMap<>();

        classStore.put(TLRPC.TL_photoSize.constructor, TLRPC.TL_photoSize.class);
    }

    static TLClassStore store = null;

    public static TLClassStore Instance() {
        if (store == null) {
            store = new TLClassStore();
        }
        return store;
    }

    public TLObject TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
        Class objClass = classStore.get(constructor);
        if (objClass != null) {
            TLObject response;
            try {
                response = (TLObject) objClass.newInstance();
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
                return null;
            }
            response.readParams(stream, exception);
            return response;
        }
        return null;
    }
}
