package com.example.hypermile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.hypermile.dataGathering.DataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class CarDetailsActivity extends AppCompatActivity {
    LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

        content = findViewById(R.id.carDetailsContent);

        JSONObject carDetailsJSON = DataManager.getVehicleDetails();

        if (carDetailsJSON != null) {
            populateContent(carDetailsJSON, content, 1);
        }
    }

    private int populateContent(JSONObject jsonObject, View view, int level) {
        final int padding = 15;

        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = null;

            try {
                value = jsonObject.get(key);
            } catch (JSONException e) {
                continue;
            }

            if (value == null) continue;

            if (value instanceof JSONObject) {
                TextView textView = new TextView(view.getContext());
                textView.setText(key);
                textView.setPadding(padding * level, padding, padding, padding);
                ((LinearLayout) view).addView(textView);
                level = populateContent((JSONObject) value, view, ++level);
            }
            else if (value instanceof String) {
                TextView textView = new TextView(view.getContext());
                String text = key + ": " + (String) value;
                textView.setText(text);
                textView.setPadding(padding * level * 2, padding, padding, padding);
                ((LinearLayout) view).addView(textView);
            }
            else if (value instanceof Integer) {
                TextView textView = new TextView(view.getContext());
                String text = key + ": " + (Integer) value;
                textView.setText(text);
                textView.setPadding(padding * level * 2, padding, padding, padding);
                ((LinearLayout) view).addView(textView);
            }
            else if (value instanceof Double) {
                TextView textView = new TextView(view.getContext());
                String text = key + ": " + (Double) value;
                textView.setText(text);
                textView.setPadding(padding * level * 2, padding, padding, padding);
                ((LinearLayout) view).addView(textView);
            }
            else if (value instanceof Boolean) {
                TextView textView = new TextView(view.getContext());
                String text = key + ": " + ((Boolean) value? "true" : "false");
                textView.setText(text);
                textView.setPadding(padding * level * 2, padding, padding, padding);
                ((LinearLayout) view).addView(textView);
            }
            else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jobj = null;

                    try {
                        jobj = array.getJSONObject(i);
                    } catch (JSONException e) {
                        continue;
                    }
                    if (jobj != null) {
                        level = populateContent(jobj, view, ++level);
                    }

                }

            }
        }
        return --level;
    }
}