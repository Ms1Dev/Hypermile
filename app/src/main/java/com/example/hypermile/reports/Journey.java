package com.example.hypermile.reports;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;
import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.dataGathering.PollCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Journey implements DataInputObserver<Timestamp>, ConnectionEventListener {
    private final ArrayList<DataSource<Double>> dataSources = new ArrayList<>();
    private static DateFormat dateFormat = new SimpleDateFormat("hh:mm ddd MM yyyy", Locale.getDefault());

    private DataSource<Timestamp> timestampSource;
    Map<String, Map<String,Object> > table = new HashMap<>();

    public Journey() {}

    public void start(DataSource<Timestamp> timestampSource) {
        this.timestampSource = timestampSource;
        timestampSource.addDataInputListener(this);
    }

    public void addDataSource (DataSource<Double> dataSource) {
        dataSources.add(dataSource);
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
        if (timestamp == null) return;

        Map<String, Object> row = new HashMap<>();

        for (DataSource<Double> dataSource : dataSources) {
            row.put(dataSource.getName(), dataSource.getData());
        }

        table.put(String.valueOf(timestamp.getTime()), row);
    }


    private void complete() {
        timestampSource.removeDataInputListener(this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Date date = new Date(timestampSource.getData().getTime());
        String timestamp = dateFormat.format(date);

        db.collection("journeys").document(timestamp)
            .set(table)
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
