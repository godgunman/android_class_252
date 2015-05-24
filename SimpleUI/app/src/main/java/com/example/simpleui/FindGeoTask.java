package com.example.simpleui;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by ggm on 5/25/15.
 */
class FindGeoTask extends AsyncTask<Object, Void, JSONObject> {

    private TaskCallback callback;

    // 0: (string)address, 1: (callback)callback
    @Override
    protected JSONObject doInBackground(Object... params) {
        String address = (String) params[0];
        callback = (TaskCallback) params[1];

        JSONObject jsonObject = Utils.addressToLocation(address);
        return jsonObject;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        callback.done(jsonObject);
    }

    protected interface TaskCallback {
        void done(JSONObject jsonObject);
    }
}
