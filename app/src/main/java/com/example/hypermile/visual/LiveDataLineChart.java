package com.example.hypermile.visual;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.hypermile.R;
import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.util.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Line chart used for showing MPG over time
 * Uses the MPAndroidChart library: https://github.com/PhilJay/MPAndroidChart
 * Documentation: https://weeklycoding.com/mpandroidchart-documentation/
 */
public class LiveDataLineChart extends RelativeLayout  {
    DataSource<Timestamp> xAxis;
    DataSource<Double> yAxis;
    LineChart lineChart;
    private LineDataSet dataSet;
    private LineData lineData;
    Double currentY;
    long startTimeOffset;
    View view;

    public LiveDataLineChart(Context context) {
        super(context);
        initialise(context, null);
    }

    public LiveDataLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
    }

    public LiveDataLineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs);
    }

    public void setAxisSources(DataSource<Timestamp> xAxis, DataSource<Double> yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;

        xAxis.addDataInputListener(new DataInputObserver<Timestamp>() {
            @Override
            public void incomingData(Timestamp data) {
                updateGraph(data);
            }
        });

        yAxis.addDataInputListener(new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                setY(data);
            }
        });

    }

    /**
     *
     * @param context
     * @param attrs
     */
    private void initialise(Context context, AttributeSet attrs) {
        view = LayoutInflater.from(context).inflate(R.layout.live_data_line_chart, this);
        lineChart = view.findViewById(R.id.line_chart);

        startTimeOffset = System.currentTimeMillis();

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.span = 2;
        layoutParams.weight = 2;
        setLayoutParams(layoutParams);

        Utils.unclip(this);

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

    private void setY(Double y) {
        currentY = y;
    }

    /**
     * Called when new data is available
     * @param x
     */
    private void updateGraph(Timestamp x) {
        if (currentY == null || currentY.isNaN()) return;
        addEntry((float)(x.getTime() - startTimeOffset), currentY.floatValue());
        currentY = null;
    }

    /**
     * Adds a new datapoint to the graph and scrolls to keep new data in view
     * Dynamically adding data is not officially supported in the documentation but it can be done
     * See: https://github.com/PhilJay/MPAndroidChart/wiki/Dynamic-&-Realtime-Data
     */
    private void addEntry(float x, float y) {
        Entry entry = new Entry(x, y);
        dataSet.addEntry(entry);
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.setVisibleXRangeMinimum(20000); // this is such a large number because the X axis is in milliseconds
        lineChart.setVisibleXRangeMaximum(20000);
        lineChart.moveViewToX(dataSet.getXMax());
    }

}