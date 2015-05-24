package com.example.simpleui;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by ggm on 5/25/15.
 */
class FindGeoFromAddressTask extends AsyncTask<Object, Void, JSONObject> {

    int index;

    private List<JSONObject> locationList;

    public FindGeoFromAddressTask(List<JSONObject> locationList) {
        this.locationList = locationList;
    }

    // 0: (string)address, 1: (int)index
    @Override
    protected JSONObject doInBackground(Object... params) {
        index = (int) params[1];

        JSONObject jsonObject = Utils.addressToLocation((String) params[0]);
        return jsonObject;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        Log.d("debug", jsonObject.toString());
        locationList.add(index, jsonObject);
    }
}
