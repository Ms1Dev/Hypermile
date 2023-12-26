package com.example.hypermile.dataGathering;

public interface DataInputObserver<T> {
    public void incomingData(T data);
    public void setUnits(String units);
}
