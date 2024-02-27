package com.example.hypermile.reports;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class acts as a data structure for storing journey data
 * When data is retrieved from Firestore it is cast to this class to make handling the data easier
 */
public class JourneyData implements Serializable {
    private ArrayList<Map<String,Object>> vehicleData = new ArrayList<>();
    private ArrayList<Map<String,Double>> route = new ArrayList<>();
    private Double totalDistanceMetres = 0.0;
    private Double totalGpsDistanceMetres = 0.0;
    private Double avgSpeed = 0.0;
    private Double avgSpeedIncStops = 0.0;
    private Double avgMpg = 0.0;
    private Double fuelUsed = 0.0;


    private Double fuelUsedExcStops = 0.0;
    private transient Timestamp createdWhen;

    public JourneyData(){}

    public void addVehicleData(Map<String,Object> row) {
        vehicleData.add(row);
    }

    public void addRouteCoordinate(Map<String,Double> coordinate) {
        route.add(coordinate);
    }

    public Double getTotalDistanceMetres() {
        return totalDistanceMetres;
    }

    public void addToTotalGpsDistanceMetres(Double distanceMetres) {
        this.totalGpsDistanceMetres += distanceMetres;
    }

    public Double getTotalGpsDistanceMetres() {
        return totalGpsDistanceMetres;
    }

    public void setTotalDistanceMetres(Double totalDistanceMetres) {
        this.totalDistanceMetres = totalDistanceMetres;
    }

    public Double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(Double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public Double getAvgSpeedIncStops() {
        return avgSpeedIncStops;
    }

    public void setAvgSpeedIncStops(Double avgSpeedIncStops) {
        this.avgSpeedIncStops = avgSpeedIncStops;
    }

    public Timestamp getCreatedWhen() {
        return createdWhen;
    }

    public void setCreatedWhen(Timestamp createdWhen) {
        this.createdWhen = createdWhen;
    }

    public Double getFuelUsed() {
        return fuelUsed;
    }
    public void addFuelUsed(double fuelUsed) {
        this.fuelUsed += fuelUsed;
    }
    public Double getFuelUsedExcStops() {
        return fuelUsedExcStops;
    }
    public void addFuelUsedExcStops(Double fuelUsed) {
        fuelUsedExcStops += fuelUsed;
    }
    public Double getAvgMpg() {
        return avgMpg;
    }

    public void setAvgMpg(Double avgMpg) {
        this.avgMpg = avgMpg;
    }

    public ArrayList<Map<String, Object>> getVehicleData() {
        return vehicleData;
    }

    public ArrayList<Map<String, Double>> getRoute() {
        return route;
    }
}
