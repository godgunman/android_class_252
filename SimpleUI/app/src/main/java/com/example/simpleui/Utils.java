package com.example.simpleui;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.os.StrictMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by ggm on 4/28/15.
 */
public class Utils {

    public static void writeFile(Context context, String fileName, String fileContent) {
        try {
            FileOutputStream fos
                    = context.openFileOutput(fileName, Context.MODE_APPEND);
            fos.write(fileContent.getBytes());
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String readFile(Context context, String fileName) {

        try {
            FileInputStream fis = context.openFileInput(fileName);
            byte[] buffer = new byte[1024];
            fis.read(buffer);
            fis.close();

            return new String(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String fetch(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder builder = new StringBuilder();
            String line;
            while( (line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String GOOGLE_GEO_API_URL = "http://maps.googleapis.com/maps/api/geocode/json";

    public static JSONObject addressToLocation(String address) {

        try {
            address = URLEncoder.encode(address, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String queryUrl = GOOGLE_GEO_API_URL + "?address=" + address + "&sensor=false";
        String jsonString = Utils.fetch(queryUrl);
        try {
            JSONObject object = new JSONObject(jsonString);

            JSONObject location =
                    object.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");

            return location;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void disableStrictMode() {

        StrictMode.ThreadPolicy.Builder builder =
                new StrictMode.ThreadPolicy.Builder();

        StrictMode.ThreadPolicy threadPolicy =
                builder.permitAll().build();

        StrictMode.setThreadPolicy(threadPolicy);
    }
}
