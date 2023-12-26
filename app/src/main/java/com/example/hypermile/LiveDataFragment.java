package com.example.hypermile;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataManager;
import com.example.hypermile.visual.GaugeView;
import com.example.hypermile.visual.LiveDataGauge;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.sql.Timestamp;
import java.util.ArrayList;

public class LiveDataFragment extends Fragment {

    private LineChart lineChart;
    private LineDataSet dataSet;
    private LineData lineData;
    private DataManager dataManager;
    Double currentMpg;
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
//        dataManager = DataManager.getInstance();
//
//        dataManager.getDerivedMpg().addDataInputListener(new DataInputObserver<Double>() {
//            @Override
//            public void incomingData(Double data) {
//                currentMpg = data;
//            }
//            @Override
//            public void setUnits(String units) {}
//        });
//
//        dataManager.getCurrentTimestamp().addDataInputListener(new DataInputObserver<Timestamp>() {
//            @Override
//            public void incomingData(Timestamp data) {
//                updateGraph(data);
//            }
//            @Override
//            public void setUnits(String units) {}
//        });
//
        startTimeOffset = System.currentTimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_live_data, container, false);
//        addGauges(view);
        addLineChart(view);
        return view;
    }


    public void connectDataToGauges() {
        dataManager = DataManager.getInstance();

        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addGauges(view);
                addLineChart(view);
            }
        });

        dataManager.getCalculatedMpg().addDataInputListener(new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                currentMpg = data;
            }
            @Override
            public void setUnits(String units) {}
        });

        dataManager.getCurrentTimestamp().addDataInputListener(new DataInputObserver<Timestamp>() {
            @Override
            public void incomingData(Timestamp data) {
                updateGraph(data);
            }
            @Override
            public void setUnits(String units) {}
        });

        startTimeOffset = System.currentTimeMillis();
    }


    public void addLineChart(View view) {
        lineChart = view.findViewById(R.id.chart);
        ArrayList<Entry> entries = new ArrayList<Entry>();
        dataSet = new LineDataSet(entries, "");
        lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getAxisLeft().setAxisMinimum(0);
        lineChart.getAxisLeft().setAxisMaximum(100);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDescription(null);

        lineChart.invalidate();
    }

    public void addGauges(View view) {
        if (!dataManager.isInitialised()) return;

        GaugeView speedGauge = view.findViewById(R.id.speed_gauge);
        speedGauge.setRange(0,110);
        LiveDataGauge speedLiveData = new LiveDataGauge(speedGauge);
        dataManager.getSpeed().addDataInputListener( speedLiveData );

        GaugeView engineSpeedGauge = view.findViewById(R.id.engineSpeed_gauge);
        engineSpeedGauge.setRange(0,8000);
        LiveDataGauge engineSpeedLiveData = new LiveDataGauge(engineSpeedGauge);
        dataManager.getEngineSpeed().addDataInputListener( engineSpeedLiveData );

        GaugeView massAirFlowGauge = view.findViewById(R.id.maf_gauge);
        massAirFlowGauge.hideDial();
        LiveDataGauge massAirFlowLiveData = new LiveDataGauge(massAirFlowGauge);
        dataManager.getMassAirFlow().addDataInputListener( massAirFlowLiveData );

        GaugeView fuelRateGauge = view.findViewById(R.id.fuelRate_gauge);
        fuelRateGauge.hideDial();
        LiveDataGauge fuelRateLiveData = new LiveDataGauge(fuelRateGauge);
        dataManager.getFuelRate().addDataInputListener( fuelRateLiveData );

//        GaugeView mpgGauge = view.findViewById(R.id.mpg_gauge);
//        mpgGauge.hideDial();
//        LiveDataGauge mpgLiveData = new LiveDataGauge(mpgGauge);
//        derivedMpg.addDataInputListener( mpgLiveData );
    }

    public void updateGraph(Timestamp timestamp) {

        if (currentMpg == null) return;

        Entry entry = new Entry((float)( timestamp.getTime() - startTimeOffset), currentMpg.floatValue());
        dataSet.addEntry(entry);
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.setVisibleXRangeMinimum(20000);
        lineChart.setVisibleXRangeMaximum(20000);
        lineChart.moveViewToX(dataSet.getXMax());

        currentMpg = null;
    }
}