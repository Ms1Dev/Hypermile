package com.example.hypermile.visual;

import com.example.hypermile.dataGathering.DataInputObserver;

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
