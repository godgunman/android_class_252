package com.example.simpleui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
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
    private static final int REQUEST_CODE_TAKE_PHOTO = 1;

    private Button button;
    private EditText editText;
    private CheckBox checkBox;
    private ListView listView;
    private Spinner spinner;
    private ImageView imageView;

    private ProgressDialog progressDialog;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private JSONArray orderInfo;
    private Bitmap bm;
    private List<ParseObject> orderObjects;

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
        imageView = (ImageView) findViewById(R.id.imageView);

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ParseObject order = orderObjects.get(position);

                String note = order.getString("note");
                String storeName = order.getString("storeName");
                ParseFile file = order.getParseFile("photo");
                JSONArray array = order.getJSONArray("order");

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, OrderDetailActivity.class);

                try {
                    intent.putExtra("note", note);
                    intent.putExtra("storeName", storeName);
                    if (file != null) {
                        intent.putExtra("file", file.getData());
                    }
                    intent.putExtra("array",array.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                startActivity(intent);
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
    }

    private List<JSONObject> locationList;

    private AsyncTask<Object, Void, JSONObject> findAddressTask = new AsyncTask<Object, Void, JSONObject>() {
        int index;

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
    };

    class FindAddressTask extends AsyncTask<Object, Void, JSONObject> {

        int index;

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

    public void setStoreName() {
        ParseQuery<ParseObject> query = new ParseQuery<>("StoreInfo");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    String[] storeNames = new String[list.size()];

                    locationList = new ArrayList<JSONObject>(list.size());

                    for (int i = 0; i < list.size(); i++) {
                        String name = list.get(i).getString("name");
                        String address = list.get(i).getString("address");

//                        findAddressTask.execute(address, i);

                        FindAddressTask fat = new FindAddressTask();
                        fat.execute(address, i);

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

                    orderObjects = list;

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

        ParseObject orderObject = new ParseObject("Order");
        orderObject.put("note", text);
        orderObject.put("order", orderInfo);
        orderObject.put("storeName", (String) spinner.getSelectedItem());

        if (bm != null) {
            ParseFile file = new ParseFile("photo.png", Utils.bitmapToBytes(bm));
            orderObject.put("photo", file);
        }

        orderObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(MainActivity.this, "saved", Toast.LENGTH_LONG).show();
                } else {
                    e.printStackTrace();
                }
                updateHistory();
            }
        });

        editText.setText("");

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
        } else if (id == R.id.action_take_photo) {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);

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
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                bm = data.getParcelableExtra("data");
                imageView.setImageBitmap(bm);
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
