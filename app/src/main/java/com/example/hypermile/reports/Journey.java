package com.example.hypermile.reports;


import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;
import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.util.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects journey data and stores it in FireStore when a journey is complete
 */
public class Journey implements DataInputObserver<Timestamp>, ConnectionEventListener {
    private final ArrayList<DataSource<Double>> dataSources = new ArrayList<>();
    private DataSource<Location> locationDataSource;
    private DataSource<Timestamp> timestampSource;
    private Timestamp prevTimestamp;
    private long journeyStart;
    private long journeyEnd;
    private Double totalSpeed = 0.0;
    private Double totalMpg = 0.0;
    private Double currentMpg = 0.0;
    private Double currentFuelRate = 0.0;
    private int rowCount;
    private int rowCountExcStops;
    private final JourneyData journeyData;
    private Location prevLocation;

    public Journey() {
        journeyData = new JourneyData();
    }

    public void start(DataSource<Timestamp> timestampSource) {
        this.timestampSource = timestampSource;
        timestampSource.addDataInputListener(this);
        journeyStart = System.currentTimeMillis();
    }

    public void addDataSource (DataSource<Double> dataSource) {
        dataSources.add(dataSource);
    }

    public void addLocationDataSource (DataSource<Location> dataSource) {
        locationDataSource = dataSource;
    }

    @Override
    public void incomingData(Timestamp timestamp) {
        addTableRow(timestamp);
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState.equals(ConnectionState.DISCONNECTED)) {
            complete();
        }
    }

    private void addTableRow(Timestamp timestamp) {
        addDataRow(timestamp);
        addRouteCoordinate();
        rowCount++;

        if (prevTimestamp != null) {
            long millisecondsElapsed = Utils.millisDiff(prevTimestamp, timestamp);
            journeyData.addFuelUsed(fuelUsed(millisecondsElapsed));
        }
        prevTimestamp = timestamp;
    }

    private double fuelUsed(long milliseconds) {
        double hours = milliseconds / 3600000.0;
        return currentFuelRate * hours;
    }

    private void addDataRow(Timestamp timestamp) {
        if (timestamp == null) return;

        Map<String, Object> vehicleDataRow = new HashMap<>();

        for (DataSource<Double> dataSource : dataSources) {
            cumulativeData(dataSource);
            vehicleDataRow.put(dataSource.getName(), dataSource.getData());
        }
        vehicleDataRow.put("Timestamp", (Long) timestamp.getTime());

        journeyData.addVehicleData(vehicleDataRow);
    }

    private void addRouteCoordinate() {
        if (locationDataSource.getData() != null) {
            Map<String, Double> routeCoordinate = new HashMap<>();
            Location location = locationDataSource.getData();
            routeCoordinate.put("latitude", location.getLatitude());
            routeCoordinate.put("longitude", location.getLongitude());
            routeCoordinate.put("altitude", location.getAltitude());
            routeCoordinate.put("mpg", currentMpg);
            journeyData.addRouteCoordinate(routeCoordinate);

            if (prevLocation != null) {
                journeyData.addToTotalGpsDistanceMetres((double) prevLocation.distanceTo(location));
            }

            prevLocation = location;
        }
    }

    /**
     * Collects data that is used for averages and totals
     * @param dataSource
     */
    private void cumulativeData(DataSource<Double> dataSource) {
        if (dataSource.getData() == null) return;
        switch (dataSource.getName()) {
            case "Speed":
                double speed = dataSource.getData();
                if (speed > 0) {
                    rowCountExcStops++;
                }
                totalSpeed += dataSource.getData();
                break;
            case "MPG":
                currentMpg = dataSource.getData();
                if (currentMpg.isNaN()) {
                    currentMpg = 0.0;
                }
                totalMpg += currentMpg;
                break;
            case "Fuel Rate":
                currentFuelRate = dataSource.getData();
                break;
        }
    }


    private void calcAverages() {
        journeyData.setAvgMpg( totalMpg / rowCountExcStops );
        journeyData.setAvgSpeed( totalSpeed / rowCountExcStops );

        double avgSpeedIncStops = totalSpeed / rowCount;
        journeyData.setAvgSpeedIncStops( avgSpeedIncStops );

        double speedMetresPerHour = avgSpeedIncStops * 1000;
        double timeDiffHours = (journeyEnd - journeyStart) / 3600000.0;
        journeyData.setTotalDistanceMetres(speedMetresPerHour * timeDiffHours);
    }

    /**
     * Called when the journey ends.
     * Stores all data to FireStore
     */
    public void complete() {
        timestampSource.removeDataInputListener(this);
        journeyEnd = System.currentTimeMillis();
        com.google.firebase.Timestamp createdWhen = new com.google.firebase.Timestamp(timestampSource.getData());
        journeyData.setCreatedWhen(createdWhen);
        calcAverages();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("journeys").document(String.valueOf(timestampSource.getData().getTime()))
            .set(journeyData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Journey", "DocumentSnapshot successfully written!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("Journey", "Error adding document", e);
                }
            });
    }
}
