package com.example.hypermile.dataGathering;

import java.util.ArrayList;

abstract public class DataSource<T> {
    protected String units;
    protected T data;
    ArrayList<DataInputObserver<T>> dataInputObservers = new ArrayList<>();

    public void addDataInputListener(DataInputObserver<T> dataInputObserver) {
        dataInputObservers.add(dataInputObserver);
        dataInputObserver.setUnits(units);
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

    public T getData() {
        return data;
    }

}
