package com.example.hypermile.data;

import java.util.ArrayList;

abstract public class DataPoint {
    double data;
    ArrayList<DataInputObserver> dataInputObservers = new ArrayList<>();

    public void addDataInputListener(DataInputObserver dataInputObserver) {
        dataInputObservers.add(dataInputObserver);
    }

    public void removeDataInputListener(DataInputObserver dataInputObserver) {
        dataInputObservers.remove(dataInputObserver);
    }

    protected void notifyObservers(double data) {
        this.data = data;
        for (DataInputObserver observer : dataInputObservers) {
            observer.incomingData(data);
        }
    }

    public double getData() {
        return data;
    }

}
