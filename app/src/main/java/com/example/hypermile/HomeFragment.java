package com.example.hypermile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.hypermile.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {
    private final static String TAG = "HoneFragment";
    private final static long DAY_SECONDS = 86400;

    Button viewLatestReportBtn;
    Button goToReportsBtn;
    View view;


    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        goToReportsBtn = (Button) view.findViewById(R.id.goToReportsBtn);
        viewLatestReportBtn = (Button) view.findViewById(R.id.viewLatestReportBtn);

        viewLatestReportBtn.setEnabled(false);

        goToReportsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.selectFragmentProgrammatically(R.id.reports);
            }
        });

        view.findViewById(R.id.weeklyStatBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStatistics(7);
            }
        });

        view.findViewById(R.id.monthlyStatBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getStatistics(28);
            }
        });

        view.findViewById(R.id.yearlyStatBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStatistics(365);
            }
        });

        getStatistics(7);

        return view;
    }



    private void setStatistics(Map<String, Double> statistics) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView totalDistance = view.findViewById(R.id.totalDistance);
                TextView fuelUsed = view.findViewById(R.id.fuelUsage);
                TextView carbonFootprint = view.findViewById(R.id.carbonFootprint);
                TextView averageMpg = view.findViewById(R.id.averageMpg);

                String totalDistanceStr = String.valueOf(statistics.get("Total Distance") + " miles");
                String fuelUsedStr = String.valueOf(statistics.get("Fuel Used") + " litres");
                String carbonFootprintStr = String.valueOf(statistics.get("Carbon Footprint") + " kgCO2e");
                String averageMpgStr = String.valueOf(statistics.get("Average MPG") + " mpg");

                totalDistance.setText(totalDistanceStr);
                fuelUsed.setText(fuelUsedStr);
                carbonFootprint.setText(carbonFootprintStr);
                averageMpg.setText(averageMpgStr);
            }
        });
    }

    private void getStatistics(int daysPrior) {
        long secondsPrior = DAY_SECONDS * daysPrior;
        Timestamp now = Timestamp.now();
        long fromSeconds = now.getSeconds() - secondsPrior;
        Timestamp filterDate = new Timestamp(fromSeconds, now.getNanoseconds());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference journeyCollection = db.collection("journeys");
        Query filteredJourneys = journeyCollection.whereGreaterThan("createdWhen", filterDate);

        filteredJourneys.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Double totalDistance = 0.0;
                    Double avgMpg = 0.0;
                    Double fuelUsed = 0.0;
                    int avgCounter = 0;

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            fuelUsed += (Double) document.get("fuelUsed");

                            Double _avgMpg = (Double) document.get("avgMpg");
                            try {
                                if (!_avgMpg.isNaN() && _avgMpg > 0) {
                                    avgMpg += _avgMpg;
                                    avgCounter++;
                                }
                            }
                            catch (NullPointerException e){}

                            totalDistance += (Double) document.get("totalDistanceMetres");
                        }
                        catch (NullPointerException e) {
                            Log.d(TAG, "Error: " + e);
                        }
                    }

                    double distanceMiles = Utils.metresToMiles(totalDistance);
                    double litresUsed = Utils.round2dp(fuelUsed);

                    Map<String, Double> statistics = new HashMap<>();
                    statistics.put("Total Distance", distanceMiles);
                    statistics.put("Fuel Used", litresUsed);
                    statistics.put("Carbon Footprint", Utils.kgCO2e(litresUsed, 1));
                    statistics.put("Average MPG", Utils.round2dp(avgMpg / avgCounter));

                    Log.d(TAG, "onComplete: " + avgMpg);

                    setStatistics(statistics);

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
}