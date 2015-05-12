package com.example.simpleui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_CODE_ORDER_ACTIVITY = 0;

    private Button button;
    private EditText editText;
    private CheckBox checkBox;
    private ListView listView;
    private Spinner spinner;
    private ProgressDialog progressDialog;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private JSONArray orderInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sp.edit();

        progressDialog = new ProgressDialog(this);

        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        listView = (ListView) findViewById(R.id.listView);
        spinner = (Spinner) findViewById(R.id.spinner);

        button.setText("SUBMIT");
        editText.setText(sp.getString("text", ""));
        checkBox.setChecked(sp.getBoolean("checkbox", false));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                String text = editText.getText().toString();
                editor.putString("text", text);
                editor.commit();

                Log.d("debug", "keyCode = " + keyCode);
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    send();
                    return true;
                }
                return false;
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                editor.putBoolean("checkbox", isChecked);
                editor.commit();
            }
        });

        updateHistory();
        setStoreName();

        Utils.disableStrictMode();
        String content1 = Utils.fetch("http://maps.googleapis.com/maps/api/geocode/json?address=%E8%87%BA%E5%8C%97%E5%B8%82%E7%BE%85%E6%96%AF%E7%A6%8F%E8%B7%AF%E5%9B%9B%E6%AE%B5%E4%B8%80%E8%99%9F&sensor=false");
        JSONObject location = Utils.addressToLocation("臺北市羅斯福路四段一號");

        Log.d("debug", content1);
        Log.d("debug", location.toString());
    }

    public void setStoreName() {
        ParseQuery<ParseObject> query = new ParseQuery<>("StoreInfo");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    String[] storeNames = new String[list.size()];

                    for (int i = 0; i < list.size(); i++) {
                        String name = list.get(i).getString("name");
                        String address = list.get(i).getString("address");

                        storeNames[i] = name + "," + address;
                    }
                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, storeNames);

                    spinner.setAdapter(adapter);
                }
            }
        });
    }

    public void goToOrderActivity(View view) {

        Intent intent = new Intent();
        intent.setClass(this, OrderActivity.class);
        intent.putExtra("storeName", (String) spinner.getSelectedItem());
        startActivityForResult(intent, REQUEST_CODE_ORDER_ACTIVITY);
    }

    private int getDrinkNumber(JSONArray array) {
        return new Random().nextInt();
    }

    private void updateHistory() {

        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Order");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {

                    List<Map<String, String>> data = new ArrayList<>();

                    for (ParseObject object : list) {

                        String storeName = object.getString("storeName");
                        String note = object.getString("note");
                        JSONArray order = object.getJSONArray("order");

                        Map<String, String> item = new HashMap<>();
                        item.put("storeName", storeName);
                        item.put("note", note);
                        item.put("drinkNumber", String.valueOf(getDrinkNumber(order)));

                        data.add(item);
                    }

                    String[] from = {"storeName", "note", "drinkNumber"};
                    int[] to = {R.id.storeName, R.id.note, R.id.number};
                    SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, data, R.layout.listview_item, from, to);
                    listView.setAdapter(adapter);

                    progressDialog.dismiss();
                }
            }
        });
    }

    private void send() {

        String text = editText.getText().toString();

        if (checkBox.isChecked()) {
            text = "*****";
        }

        try {
            JSONObject all = new JSONObject();
            all.put("note", text);
            all.put("order", orderInfo);
            all.put("storeName", (String) spinner.getSelectedItem());

            Utils.writeFile(this, "history", all.toString() + "\n");


            ParseObject testObject = new ParseObject("Order");
            testObject.put("note", text);
            testObject.put("order", orderInfo);
            testObject.put("storeName", (String) spinner.getSelectedItem());
            testObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(MainActivity.this, "saved", Toast.LENGTH_LONG).show();
                    } else {
                        e.printStackTrace();
                    }
                    Log.d("debug", "line200");
                }
            });

            Log.d("debug", "line204");

            editText.setText("");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        updateHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ORDER_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                String jsonArrayString = data.getStringExtra("order");

                try {
                    orderInfo = new JSONArray(jsonArrayString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast.makeText(this, jsonArrayString, Toast.LENGTH_LONG).show();
            }
        }

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            send();
        }
    };

    class MyOnClickListner implements View.OnClickListener {

        @Override
        public void onClick(View v) {

        }
    }
}
