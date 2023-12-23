package com.example.hypermile.data;

public interface DataInputObserver<T> {
    public void incomingData(T data);
    public void setUnits(String units);
}
