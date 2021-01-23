package com.serrano.cryptoview;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.input.RotaryEncoder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import static android.view.ViewConfiguration.get;
import static androidx.core.view.ViewConfigurationCompat.getScaledVerticalScrollFactor;
import static java.security.AccessController.getContext;

public class MainActivity extends WearableActivity implements AdapterView.OnItemSelectedListener  {

    private TextView mTextView;
    private TextView mAmount;
    private String amount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View view = findViewById(R.id.main_screen);

        //Scrolling UI
        view.requestFocus();
        final ViewConfiguration config = get(this);
        view.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_SCROLL && RotaryEncoder.isFromRotaryEncoder(ev)) {
                    // Don't forget the negation here
                    float delta = -RotaryEncoder.getRotaryAxisValue(ev) *  getScaledVerticalScrollFactor (config, MainActivity.this);

                    // Swap these axes if you want to do horizontal scrolling instead
                    v.scrollBy(0, Math.round(delta));
                    Log.d("Scroll", String.valueOf(delta));
                    return true;
                }

                return false;
            }
        });


        view.setBackgroundColor(Color.BLACK);
        mAmount = findViewById(R.id.price);

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cryptos_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);



        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        series.setColor(Color.rgb(18,218,90));
        graph.addSeries(series);


        fetch();
        // Enables Always-on
        setAmbientEnabled();
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            fetch();
            }
        });

    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {


        // An item was selected. You can retrieve the selected item using
            Log.d("tch", String.valueOf(parent.getSelectedItemId()));
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    private void fetch() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    URL url = null;
                    try {
                        url = new URL(" https://api.coinbase.com/v2/prices/spot?currency=USD");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    try {
                        Log.d("CURL", url.toString());

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                            for (String line; (line = reader.readLine()) != null;) {
                                Log.d("CURL", line);
                                parse(line);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void parse (String data){
        String jsonString = data ; //assign your JSON String here
        JSONObject obj = null;
        try {
            obj = new JSONObject(jsonString);
            amount = obj.getJSONObject("data").getString("amount");
            Log.d("CURL", amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                mAmount.setText("$" +amount);
            }
        });


    }

}