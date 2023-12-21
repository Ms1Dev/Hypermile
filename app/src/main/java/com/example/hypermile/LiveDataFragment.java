package com.example.hypermile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hypermile.data.DataPoint;
import com.example.hypermile.data.DerivedFuelRate;
import com.example.hypermile.data.DerivedMpg;
import com.example.hypermile.visual.GaugeView;
import com.example.hypermile.visual.LiveDataGauge;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LiveDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LiveDataFragment extends Fragment {

    LiveDataGauge engineSpeedGauge;

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

        GaugeView gaugeView = view.findViewById(R.id.engineSpeed_gauge);
        gaugeView.setRange(0,8000);
        engineSpeedGauge = new LiveDataGauge(gaugeView);
        ((MainActivity) getActivity()).engineSpeed.addDataInputListener( engineSpeedGauge);

        GaugeView gaugeView2 = view.findViewById(R.id.maf_gauge);
        gaugeView2.setRange(0,200);
        LiveDataGauge mafGauge = new LiveDataGauge(gaugeView2);
        ((MainActivity) getActivity()).massAirFlow.addDataInputListener( mafGauge);

        GaugeView gaugeView3 = view.findViewById(R.id.speed_gauge);
        gaugeView3.setRange(0,110);
        LiveDataGauge speedGauge = new LiveDataGauge(gaugeView3);
        ((MainActivity) getActivity()).speed.addDataInputListener( speedGauge);

        GaugeView gaugeView4 = view.findViewById(R.id.fuelRate_gauge);
        gaugeView4.hideDial();
        LiveDataGauge fuelrateGauge = new LiveDataGauge(gaugeView4);
        DerivedFuelRate fuelrate = new DerivedFuelRate(((MainActivity) getActivity()).massAirFlow);
        fuelrate.addDataInputListener( fuelrateGauge);

        GaugeView gaugeView5 = view.findViewById(R.id.mpg_gauge);
        gaugeView5.hideDial();
        LiveDataGauge mpgGauge = new LiveDataGauge(gaugeView5);
        DataPoint mpg = new DerivedMpg(((MainActivity) getActivity()).speed, fuelrate);
        mpg.addDataInputListener( mpgGauge);


        LineChart lineChart = view.findViewById(R.id.chart);

        ArrayList<Entry> entries = new ArrayList<Entry>();

        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            entries.add(new Entry( i, random.nextInt(50)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Random");

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.invalidate();

        return view;
    }

}