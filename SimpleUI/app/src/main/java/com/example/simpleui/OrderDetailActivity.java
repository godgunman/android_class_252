package com.example.simpleui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class OrderDetailActivity extends ActionBarActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        webView = (WebView) findViewById(R.id.webView);

        Intent intent = getIntent();

        String note = intent.getStringExtra("note");
        String storeName = intent.getStringExtra("storeName");
        if (intent.hasExtra("file")) {
            byte[] file = intent.getByteArrayExtra("file");
        }
        try {
            JSONArray array = new JSONArray(intent.getStringExtra("array"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("debug", "note:" + note + ", storeName" + storeName );
        webView.loadUrl(getStaticMapURL(storeName));
        webView.setWebViewClient(new WebViewClient());

        initGoogleMaps();
    }

    public void initGoogleMaps() {

        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);

        LatLng latLng = new LatLng(lat, lng);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        GoogleMap googleMap = mapFragment.getMap();
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(address));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

        MarkerOptions markerOptions =
                new MarkerOptions()
                        .position(latLng)
                        .title("here");

        googleMap.addMarker(markerOptions);
    }

    public String getStaticMapURL(String storeName) {
        String address = storeName.split(",")[1];
        try {
            String encodedAddress = URLEncoder.encode(address, "utf-8");
            return "https://maps.googleapis.com/maps/api/staticmap?center="
                    + encodedAddress
                    + "&zoom=17&size=600x300";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
