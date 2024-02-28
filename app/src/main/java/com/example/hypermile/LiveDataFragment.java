package com.example.hypermile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TableRow;

import com.example.hypermile.dataGathering.DataManager;
import com.example.hypermile.dataGathering.DataManagerReadyListener;
import com.example.hypermile.visual.LiveDataGauge;
import com.example.hypermile.visual.LiveDataLineChart;
import com.example.hypermile.visual.InclinationView;

/**
 * Presents live data to the user in the form of gauges and tables.
 * Individual elements update every time new data is received from one of the many data sources.
 */
public class LiveDataFragment extends Fragment implements DataManagerReadyListener {
    private LiveDataGauge speedGauge;
    private LiveDataGauge engineSpeedGauge;
    private LiveDataGauge fuelRateGauge;
    private LiveDataLineChart liveDataLineChart;
    private InclinationView inclinationView;

    private DataManager dataManager;

    private long startTimeOffset;
    private View view;

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

    /**
     * Table rows and elements are added programmatically here to give the option to add more at a later date.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_live_data, container, false);

        TableRow row_1 = view.findViewById(R.id.tablerow_1);
        TableRow row_2 = view.findViewById(R.id.tablerow_2);
        TableRow row_3 = view.findViewById(R.id.tablerow_3);

        speedGauge = new LiveDataGauge(getContext());
        row_1.addView(speedGauge);

        engineSpeedGauge = new LiveDataGauge(getContext());
        row_1.addView(engineSpeedGauge);

        liveDataLineChart = new LiveDataLineChart(getContext());
        row_2.addView(liveDataLineChart);

        fuelRateGauge = new LiveDataGauge(getContext());
        row_3.addView(fuelRateGauge);

        inclinationView = new InclinationView(getContext());
        row_3.addView(inclinationView);

        return view;
    }

    /**
     * Connects the data sources to their respective gauges.
     * Can only be done when the DataManager is ready so is called by the dataManagerReady function.
     */
    public void connectDataToGauges() {
        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speedGauge.setDataSource(dataManager.getSpeed());
                engineSpeedGauge.setDataSource(dataManager.getEngineSpeed());
                fuelRateGauge.setDataSource(dataManager.getFuelRate());
                liveDataLineChart.setAxisSources(dataManager.getCurrentTimestamp(), dataManager.getCalculatedMpg());
                inclinationView.setDataSource(dataManager.getCalculatedInclination());
            }
        });

        startTimeOffset = System.currentTimeMillis();
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Gauges can only connect to data sources once DataManager is ready
     */
    @Override
    public void dataManagerReady() {
        if (dataManager != null) {
            connectDataToGauges();
        }
    }


    /**
     * Creates a space equal to the size of one gauge for when the gauge number is uneven.
     */
    private Space getSpacer(Context context) {
        Space space = new Space(context);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.span = 1;
        layoutParams.weight = 1;
        space.setLayoutParams(layoutParams);
        return space;
    }

}