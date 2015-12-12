package ru.aragats.wgo.rest.task;

import android.os.AsyncTask;

import ru.aragats.wgo.rest.client.WGOClient;

/**
 * Created by aragats on 05/12/15.
 */
public class RestTask extends AsyncTask<String, Void, String> {

    private Exception exception;

    protected String doInBackground(String... urls) {
        try {
            WGOClient wgoClient = new WGOClient();
            return wgoClient.method();
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

    protected void onPostExecute(String feed) {
        System.out.println("Post Execute");
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}