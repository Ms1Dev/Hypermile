package com.example.hypermile.visual;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hypermile.R;
import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;

public class LiveDataGauge extends RelativeLayout implements DataInputObserver<Double> {
    DataSource<Double> dataSource;
    GaugeView gaugeView;
    View view;

    public LiveDataGauge(Context context) {
        super(context);
        initialise(context, null);
    }

    public LiveDataGauge(Context context, DataSource<Double> dataSource) {
        super(context);
        setDataSource(dataSource);
        initialise(context, null);
    }

    public LiveDataGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
    }

    public LiveDataGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs);
    }

    public void setDataSource(DataSource<Double> dataSource) {
        this.dataSource = dataSource;
        dataSource.addDataInputListener(this);
        TextView title = view.findViewById(R.id.gauge_title);
        title.setText(dataSource.getName());
        gaugeView.setUnit(dataSource.getUnits());
        gaugeView.setRange(dataSource.getMinValue(), dataSource.getMaxValue());
    }

    private void initialise(Context context, AttributeSet attrs) {
        view = LayoutInflater.from(context).inflate(R.layout.live_data_gauge, this);
        gaugeView = view.findViewById(R.id.live_data_dial);
    }

    @Override
    public void incomingData(Double data) {
        gaugeView.updateValue(data);
    }

    @Override
    public void setUnits(String units) {
        gaugeView.setUnit(units);
    }

}
