package com.example.hypermile.dataGathering;

import java.util.ArrayList;

abstract public class DataSource<T> {
    protected String units;
    protected int minValue;
    protected int maxValue;
    protected int decimalPoints = 0;
    protected T data;
    ArrayList<DataInputObserver<T>> dataInputObservers = new ArrayList<>();


    public abstract String getName();

    public void addDataInputListener(DataInputObserver<T> dataInputObserver) {
        dataInputObservers.add(dataInputObserver);
//        dataInputObserver.setUnits(units);
    }

    public void removeDataInputListener(DataInputObserver<T> dataInputObserver) {
        dataInputObservers.remove(dataInputObserver);
    }

    protected void notifyObservers(T data) {
        this.data = data;
        for (DataInputObserver<T> observer : dataInputObservers) {
            observer.incomingData(data);
        }
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getDecimalPoints() {
        return decimalPoints;
    }

    public void setDecimalPoints(int decimalPoints) {
        this.decimalPoints = decimalPoints;
    }

    public T getData() {
        return data;
    }

}
