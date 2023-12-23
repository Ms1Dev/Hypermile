package com.example.hypermile.visual;

import android.content.Context;
import android.util.AttributeSet;

import com.example.hypermile.data.DataInputObserver;

public class LiveDataGauge implements DataInputObserver<Double> {
    GaugeView gaugeView;

    public LiveDataGauge(GaugeView gaugeView) {
        this.gaugeView = gaugeView;
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
