package com.example.hypermile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hypermile.visual.GaugeView;

import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LiveDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LiveDataFragment extends Fragment {

    GaugeView gaugeView;

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

        gaugeView = view.findViewById(R.id.gauge);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                while (true) {
                    try {
                        Thread.sleep(1000);
                        gaugeView.updateValue(random.nextInt(100));
                    } catch (InterruptedException e) {

                    }
                }
            }
        }).start();

        return view;
    }
}