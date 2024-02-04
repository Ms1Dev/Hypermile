package com.example.hypermile.reports;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * Data is stored in FireStore using this class
 */
public class JourneyData implements Serializable {
    private ArrayList<Map<String,Object>> vehicleData = new ArrayList<>();
    private ArrayList<Map<String,Double>> route = new ArrayList<>();
    private Double totalDistanceMetres = 0.0;
    private Double avgSpeed = 0.0;
    private Double avgSpeedIncStops = 0.0;
    private Double avgMpg = 0.0;
    private Double fuelUsed = 0.0;
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

    public void addToTotalDistanceMetres(Double distanceMetres) {
        this.totalDistanceMetres += distanceMetres;
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
