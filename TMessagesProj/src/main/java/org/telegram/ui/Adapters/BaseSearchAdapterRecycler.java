/*
 * This is the source code of Telegram for Android v. 2.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.telegram.ui.Adapters;

import org.telegram.android.support.widget.RecyclerView;
import org.telegram.messenger.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseSearchAdapterRecycler extends RecyclerView.Adapter {

    protected static class HashtagObject {
        String hashtag;
        int date;
    }

    private long reqId = 0;
    private int lastReqId;
    protected String lastFoundUsername = null;

    protected ArrayList<HashtagObject> hashtags;
    protected HashMap<String, HashtagObject> hashtagsByText;
    protected boolean hashtagsLoadedFromDb = false;

    public void queryServerSearch(final String query) {
//        if (reqId != 0) {
//            ConnectionsManager.getInstance().cancelRpc(reqId, true);
//            reqId = 0;
//        }
    }



    public void addHashtagsFromMessage(String message) {
        if (message == null) {
            return;
        }
        boolean changed = false;
        Pattern pattern = Pattern.compile("(^|\\s)#[\\w@\\.]+");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (message.charAt(start) != '@' && message.charAt(start) != '#') {
                start++;
            }
            String hashtag = message.substring(start, end);
            if (hashtagsByText == null) {
                hashtagsByText = new HashMap<>();
                hashtags = new ArrayList<>();
            }
            HashtagObject hashtagObject = hashtagsByText.get(hashtag);
            if (hashtagObject == null) {
                hashtagObject = new HashtagObject();
                hashtagObject.hashtag = hashtag;
                hashtagsByText.put(hashtagObject.hashtag, hashtagObject);
            } else {
                hashtags.remove(hashtagObject);
            }
            hashtagObject.date = (int) (System.currentTimeMillis() / 1000);
            hashtags.add(0, hashtagObject);
            changed = true;
        }
//        if (changed) {
//            putRecentHashtags(hashtags);
//        }
    }


    public void clearRecentHashtags() {
        hashtags = new ArrayList<>();
        hashtagsByText = new HashMap<>();
    }

    protected void setHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
        hashtags = arrayList;
        hashtagsByText = hashMap;
        hashtagsLoadedFromDb = true;
    }
}
