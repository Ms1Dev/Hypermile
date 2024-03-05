package com.example.hypermile.dataGathering;

/**
 * Implemented by the DataManager class to restrict access to relevant methods
 */
public interface EngineSpec {
    int getEngineCapacity();
    int getFuelType();
}
