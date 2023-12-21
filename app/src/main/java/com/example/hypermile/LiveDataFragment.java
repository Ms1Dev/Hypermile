package com.example.hypermile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hypermile.visual.GaugeView;
import com.example.hypermile.visual.LiveDataGauge;

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

        return view;
    }

}