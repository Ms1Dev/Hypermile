package com.example.hypermile.dataGathering;

/**
 * Interface for observing new data from datasources
 * @param <T>
 */
public interface DataInputObserver<T> {
    public void incomingData(T data);
}
