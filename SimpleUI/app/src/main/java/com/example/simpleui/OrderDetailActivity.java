package com.example.simpleui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OrderDetailActivity extends ActionBarActivity {

    private TextView storeName;
    private ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (GoogleApiAvailability
                .getInstance()
                .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            setContentView(R.layout.activity_order_detail_gmap);
            initGoogleMap();

        } else {
            setContentView(R.layout.activity_order_detail_webview);
            initWebView();
        }

        initViewsInstance();
        setViewsValue();

    }

    private void initViewsInstance() {
        storeName = (TextView) findViewById(R.id.textView_storeName);
        photo = (ImageView) findViewById(R.id.imageView_photo);
    }

    private void setViewsValue() {

        Intent intent = getIntent();
        storeName.setText(intent.getStringExtra("storeName")
                + ","
                + intent.getStringExtra("storeAddress"));

        if (intent.hasExtra("file")) {
            byte[] file = intent.getByteArrayExtra("file");
            photo.setImageBitmap(BitmapFactory.decodeByteArray(file, 0, file.length));
        }
    }

    private void initGoogleMap() {

        String address = getIntent().getStringExtra("storeAddress");

        FindGeoTask task = new FindGeoTask();
        task.execute(address, new FindGeoTask.TaskCallback() {
            public void done(JSONObject jsonObject) {
                try {

                    double lat = jsonObject.getDouble("lat");
                    double lng = jsonObject.getDouble("lng");
                    setGMapValue(new LatLng(lat, lng));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void setGMapValue(LatLng latLng) {
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

    private void initWebView() {
        String storeAddress = getIntent().getStringExtra("storeAddress");

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(Utils.getStaticMapURL(storeAddress));
        webView.setWebViewClient(new WebViewClient());
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
