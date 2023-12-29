package com.example.hypermile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TableRow;

import com.example.hypermile.dataGathering.DataManager;
import com.example.hypermile.visual.LiveDataGauge;
import com.example.hypermile.visual.LiveDataLineChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

public class LiveDataFragment extends Fragment {

    private DataManager dataManager;
    private TableRow row_1;
    private TableRow row_2;
    private TableRow row_3;

    LiveDataGauge speedGauge;
    LiveDataGauge engineSpeedGauge;
    LiveDataGauge fuelRateGauge;
    LiveDataLineChart liveDataLineChart;

    long startTimeOffset;
    View view;

    public LiveDataFragment() {
        // Required empty public constructor
    }

    public static LiveDataFragment newInstance(String param1, String param2) {
        LiveDataFragment fragment = new LiveDataFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTimeOffset = System.currentTimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_live_data, container, false);

        row_1 = view.findViewById(R.id.tablerow_1);
        row_2 = view.findViewById(R.id.tablerow_2);
        row_3 = view.findViewById(R.id.tablerow_3);

        speedGauge = new LiveDataGauge(getContext());
        row_1.addView(speedGauge);

        engineSpeedGauge = new LiveDataGauge(getContext());
        row_1.addView(engineSpeedGauge);

        liveDataLineChart = new LiveDataLineChart(getContext());
        row_2.addView(liveDataLineChart);

        fuelRateGauge = new LiveDataGauge(getContext());
        row_3.addView(fuelRateGauge);

        row_3.addView(getSpacer(getContext()));

        return view;
    }

    private Space getSpacer(Context context) {
        Space space = new Space(context);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.span = 1;
        layoutParams.weight = 1;
        space.setLayoutParams(layoutParams);
        return space;
    }

    public void connectDataToGauges() {
        dataManager = DataManager.getInstance();

        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speedGauge.setDataSource(dataManager.getSpeed());
                engineSpeedGauge.setDataSource(dataManager.getEngineSpeed());
                fuelRateGauge.setDataSource(dataManager.getFuelRate());
                liveDataLineChart.setAxisSources(dataManager.getCurrentTimestamp(), dataManager.getCalculatedMpg());
            }
        });

        startTimeOffset = System.currentTimeMillis();
    }
}