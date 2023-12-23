package com.example.hypermile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hypermile.data.DataInputObserver;
import com.example.hypermile.data.DerivedFuelRate;
import com.example.hypermile.data.DerivedMpg;
import com.example.hypermile.visual.GaugeView;
import com.example.hypermile.visual.LiveDataGauge;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LiveDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LiveDataFragment extends Fragment implements DataInputObserver {

    private LineChart lineChart;
    private LineDataSet dataSet;
    private LineData lineData;
    private DerivedMpg derivedMpg;
    private DerivedFuelRate derivedFuelRate;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_data, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        derivedFuelRate = new DerivedFuelRate(mainActivity.massAirFlow);
        derivedMpg = new DerivedMpg(mainActivity.speed, derivedFuelRate);
        addGauges(view);
        addLineChart(view);
        derivedMpg.addDataInputListener(this);
        return view;
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

    private void addGauges(View view) {
        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity == null) return;

        GaugeView speedGauge = view.findViewById(R.id.speed_gauge);
        speedGauge.setRange(0,110);
        LiveDataGauge speedLiveData = new LiveDataGauge(speedGauge);
        mainActivity.speed.addDataInputListener( speedLiveData );

        GaugeView engineSpeedGauge = view.findViewById(R.id.engineSpeed_gauge);
        engineSpeedGauge.setRange(0,8000);
        LiveDataGauge engineSpeedLiveData = new LiveDataGauge(engineSpeedGauge);
        mainActivity.engineSpeed.addDataInputListener( engineSpeedLiveData );

        GaugeView massAirFlowGauge = view.findViewById(R.id.maf_gauge);
        massAirFlowGauge.hideDial();
        LiveDataGauge massAirFlowLiveData = new LiveDataGauge(massAirFlowGauge);
        mainActivity.massAirFlow.addDataInputListener( massAirFlowLiveData );

        GaugeView fuelRateGauge = view.findViewById(R.id.fuelRate_gauge);
        fuelRateGauge.hideDial();
        LiveDataGauge fuelRateLiveData = new LiveDataGauge(fuelRateGauge);
        derivedFuelRate.addDataInputListener( fuelRateLiveData );
//
//        GaugeView mpgGauge = view.findViewById(R.id.mpg_gauge);
//        mpgGauge.hideDial();
//        LiveDataGauge mpgLiveData = new LiveDataGauge(mpgGauge);
//        derivedMpg.addDataInputListener( mpgLiveData );
    }

    @Override
    public void incomingData(double data) {
        Entry entry = new Entry(dataSet.getEntryCount(),(float) data);
        dataSet.addEntry(entry);
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.setVisibleXRangeMinimum(20);
        lineChart.setVisibleXRangeMaximum(20);
        lineChart.moveViewToX(dataSet.getEntryCount());
    }

    @Override
    public void setUnits(String units) {

    }
}