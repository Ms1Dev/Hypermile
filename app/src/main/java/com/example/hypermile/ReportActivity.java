package com.example.hypermile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hypermile.reports.Report;

public class ReportActivity extends AppCompatActivity {
    private static final int LOW_MPG_BOUNDARY = 25;
    private static final int MID_MPG_BOUNDARY = 40;
    public static final int LOW_MPG_COLOUR = Color.RED;
    public static final int MID_MPG_COLOUR = Color.YELLOW;
    public static final int HIGH_MPG_COLOUR = Color.GREEN;

    MapsFragment mapsFragment;

    LinearLayout statisticsLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Intent intent = getIntent();
        Report report = (Report) intent.getSerializableExtra("Report");

        Toolbar toolbar = findViewById(R.id.reportActivityToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Report " + report.getDateOfReport());

        TextView lowMpgKey = findViewById(R.id.low_mpg_val);
        TextView midMpgKey = findViewById(R.id.mid_mpg_val);
        TextView highMpgKey = findViewById(R.id.high_mpg_val);

        String lowMpgKeyText = " <" + LOW_MPG_BOUNDARY + " MPG";
        String midMpgKeyText = LOW_MPG_BOUNDARY + " - " + MID_MPG_BOUNDARY + " MPG";
        String highMpgKeyText = " > " + MID_MPG_BOUNDARY + " MPG";

        lowMpgKey.setBackgroundColor(LOW_MPG_COLOUR);
        lowMpgKey.setText(lowMpgKeyText);

        midMpgKey.setBackgroundColor(MID_MPG_COLOUR);
        midMpgKey.setText(midMpgKeyText);

        highMpgKey.setBackgroundColor(HIGH_MPG_COLOUR);
        highMpgKey.setText(highMpgKeyText);

        statisticsLayout = findViewById(R.id.statisticsLayout);

        report.processData();
        addStatistics(report);

        mapsFragment = new MapsFragment(report.getRoute(), LOW_MPG_BOUNDARY, MID_MPG_BOUNDARY);

        getSupportFragmentManager().beginTransaction().add(R.id.mapLayout, mapsFragment).commit();
    }

    private void addStatistics(Report report) {
        TextView averageMpg = new TextView(this);
        TextView averageSpeed = new TextView(this);
        TextView averageSpeedIncStationary = new TextView(this);
        TextView fuelUsed = new TextView(this);
        TextView totalDistance = new TextView(this);

        String avgMpgStr = "Average MPG: " + report.getAvgMpg();
        String avgSpeedStr = "Average Speed: " + report.getAvgSpeed() + " KPH";
        String avgSpeedIncStationaryStr = "Average Speed (inc stationary): " + report.getAvgSpeedIncStationary() + " KPH";
        String fuelUsedStr = "Fuel used: " + report.getFuelUsed() + " litres";
        String totalDistanceStr = "Total distance: " + report.getTotalDistance() + " miles";

        averageMpg.setText(avgMpgStr);
        averageSpeed.setText(avgSpeedStr);
        averageSpeedIncStationary.setText(avgSpeedIncStationaryStr);
        fuelUsed.setText(fuelUsedStr);
        totalDistance.setText(totalDistanceStr);

        statisticsLayout.addView(averageMpg);
        statisticsLayout.addView(averageSpeed);
        statisticsLayout.addView(averageSpeedIncStationary);
        statisticsLayout.addView(fuelUsed);
        statisticsLayout.addView(totalDistance);
    }

    /**
     * @brief handles the back button on click
     * @param item
     * @return true
     *
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();

        if (item_id == android.R.id.home) {
            this.finish();
            return true;
        }
        return true;
    }
}