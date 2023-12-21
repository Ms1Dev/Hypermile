package com.example.hypermile.visual;

import android.content.Context;
import android.util.AttributeSet;

import com.example.hypermile.data.DataInputObserver;

public class LiveDataGauge implements DataInputObserver {
    GaugeView gaugeView;

    public LiveDataGauge(GaugeView gaugeView) {
        this.gaugeView = gaugeView;
    }

    @Override
    public void incomingData(double data) {
        gaugeView.updateValue((int) data);
    }

    @Override
    public void setUnits(String units) {
        gaugeView.setUnit(units);
    }

}
