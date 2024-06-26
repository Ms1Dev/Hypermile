package com.example.hypermile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.hypermile.reports.Report;

/**
 * Displays details about the report that was selected
 * If there is GPS data it will show a map fragment with the route as well
 */
public class ReportActivity extends AppCompatActivity {
    private static final int LOW_MPG_BOUNDARY = 25;
    private static final int MID_MPG_BOUNDARY = 40;
    public static final int LOW_MPG_COLOUR = Color.RED;
    public static final int MID_MPG_COLOUR = Color.YELLOW;
    public static final int HIGH_MPG_COLOUR = Color.GREEN;

    private MapsFragment mapsFragment;
    private LinearLayout statisticsLayout;
    private TableLayout statisticsTable;


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
        actionBar.setTitle(Report.DATE_FORMAT.format(Double.valueOf(report.getDateOfReport())));

        ImageView lowMpgKey = findViewById(R.id.lowMpgKey);
        ImageView midMpgKey = findViewById(R.id.midMpgKey);
        ImageView highMpgKey = findViewById(R.id.highMpgKey);

        TextView lowMpgDetails = findViewById(R.id.lowMpgDetails);
        TextView midMpgDetails = findViewById(R.id.midMpgDetails);
        TextView highMpgDetails = findViewById(R.id.highMpgDetails);

        String lowMpgKeyText = "< " + LOW_MPG_BOUNDARY + " MPG";
        String midMpgKeyText = LOW_MPG_BOUNDARY + " - " + MID_MPG_BOUNDARY + " MPG";
        String highMpgKeyText = " > " + MID_MPG_BOUNDARY + " MPG";

        lowMpgKey.setColorFilter(LOW_MPG_COLOUR);
        lowMpgDetails.setText(lowMpgKeyText);

        midMpgKey.setColorFilter(MID_MPG_COLOUR);
        midMpgDetails.setText(midMpgKeyText);

        highMpgKey.setColorFilter(HIGH_MPG_COLOUR);
        highMpgDetails.setText(highMpgKeyText);

        statisticsLayout = findViewById(R.id.statisticsLayout);
        statisticsTable = findViewById(R.id.statisticsTable);

        addStatistics(report);

        // if permissions for GPS/internet are granted AND gps coordinates were recorded then show a map of route
        if (
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                report.getTotalGpsDistance() > 0.0
        ) {
            mapsFragment = new MapsFragment(report.getRoute(), LOW_MPG_BOUNDARY, MID_MPG_BOUNDARY);
            getSupportFragmentManager().beginTransaction().add(R.id.mapLayout, mapsFragment).commit();
        }
        else {
            findViewById(R.id.mapCard).setVisibility(View.GONE);
            findViewById(R.id.noMapInfo).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Adds rows to the statistics table using data from the Report object
     */
    private void addStatistics(Report report) {
        addTableRow("Average MPG",String.valueOf(report.getAvgMpg()));
        addTableRow("Average Speed",String.valueOf(report.getAvgSpeed() + " MPH"));
        addTableRow("Average Speed (inc stationary)",String.valueOf(report.getAvgSpeedIncStationary() + " MPH"));
        addTableRow("Fuel used",String.valueOf(report.getFuelUsed() + " litres" ));
        addTableRow("Fuel used (inc stationary)",String.valueOf(report.getFuelUsedIncStops() + " litres"));
        addTableRow("Total distance",String.valueOf(report.getTotalDistance() + " miles"));
    }

    /**
     * Used by addStatistics to add rows to table
     */
    private void addTableRow(String label, String value) {
        TableRow tableRow = new TableRow(this);
        TextView labelView = new TextView(this);
        TextView valueView = new TextView(this);

        labelView.setText(label);
        valueView.setText(value);

        tableRow.addView(labelView);
        tableRow.addView(new Space(this));
        tableRow.addView(valueView);

        tableRow.setMinimumHeight(80);
        tableRow.setVerticalGravity(Gravity.CENTER_VERTICAL);

        statisticsTable.addView(tableRow);
    }

    /**
     * This is needed or the back button will not work
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