package com.example.hypermile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.hypermile.reports.JourneyData;
import com.example.hypermile.reports.Report;
import com.example.hypermile.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This fragment is the default home screen fragment.
 * Shows info about the latest report and some statistics on the home screen.
 */
public class HomeFragment extends Fragment {
    private final static String TAG = "HomeFragment";
    private final static long DAY_SECONDS = 86400;

    Button viewLatestReportBtn;
    Button goToReportsBtn;
    TextView latestReportInfo;
    private ProgressBar statisticsLoadProgressBar;
    private TableLayout statisticsTable;
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
        latestReportInfo = view.findViewById(R.id.latestReportInfo);
        viewLatestReportBtn.setEnabled(false);
        statisticsLoadProgressBar= view.findViewById(R.id.statisticsLoadProgressBar);
        statisticsTable = view.findViewById(R.id.statisticsTable);

        TabLayout statisticsTabs = view.findViewById(R.id.statisticsSelectRange);

        // set onclick listener for button that will take user to the reports list fragment
        goToReportsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.selectFragmentProgrammatically(R.id.reports);
            }
        });

        statisticsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getStatistics(7);
                        break;
                    case 1:
                        getStatistics(28);
                        break;
                    case 2:
                        getStatistics(365);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


        // call methods for updating the UI with firestore data
        getStatistics(7);
        getLatestReport();

        return view;
    }

    /**
     * Attempts to retrieve the most recent document from firestore
     */
    private void getLatestReport() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // get the journey collection
        CollectionReference journeyCollection = db.collection(userId);

        // order by the createdWhen attribute and limit to 1 to get the latest
        Query latestJourney = journeyCollection.orderBy("createdWhen", Query.Direction.DESCENDING).limit(1);

        latestJourney.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    if (documents.size() == 1) {
                        // get the first document (there should only be one anyway)
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        // convert the document to a JourneyData class object
                        JourneyData journeyData = document.toObject(JourneyData.class);
                        // create an Report instance using the JourneyData object and pass to method for updating UI
                        latestReportRetrieved( new Report(document.getId(), journeyData) );
                    }
                }
            }
        });
    }

    /**
     * Called when the latest document is retrieved from firestore.
     * Updates the homepage to show info and a link to this report.
     */
    private void latestReportRetrieved(Report report) {
        // enable the button and add onclick listener that will launch the report activity
        viewLatestReportBtn.setEnabled(true);
        viewLatestReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reportIntent = new Intent(getContext(), ReportActivity.class);
                reportIntent.putExtra("Report", report);
                startActivity(reportIntent);
            }
        });

        // update the text to show info about report
        String reportText = getResources().getString(R.string.latest_report, report.getAvgMpg(), report.getFuelUsedIncStops());
        latestReportInfo.setText(reportText);
    }

    /**
     * Retrieves documents that have been created between now and the number of days prior.
     * Iterates over those documents and creates totals/averages for certain attributes.
     */
    private void getStatistics(int daysPrior) {
        statisticsTable.setVisibility(View.GONE);
        statisticsLoadProgressBar.setVisibility(View.VISIBLE);

        long secondsPrior = DAY_SECONDS * daysPrior;
        Timestamp now = Timestamp.now();
        long fromSeconds = now.getSeconds() - secondsPrior;
        Timestamp filterDate = new Timestamp(fromSeconds, now.getNanoseconds());

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference journeyCollection = db.collection(userId);
        Query filteredJourneys = journeyCollection.whereGreaterThan("createdWhen", filterDate);

        filteredJourneys.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Double totalDistance = 0.0;
                    Double avgMpg = 0.0;
                    Double fuelUsed = 0.0;
                    int avgCounter = 0;

                    // iterate over the documents
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            // accumulate mpg and increment counter if greater than 0 and not NaN
                            Double _avgMpg = (Double) document.get("avgMpg");
                            try {
                                if (!_avgMpg.isNaN() && _avgMpg > 0) {
                                    avgMpg += _avgMpg;
                                    avgCounter++;
                                }
                            }
                            catch (NullPointerException e){}

                            // accumulate fuel usage and distance
                            fuelUsed += (Double) document.get("fuelUsed");
                            totalDistance += (Double) document.get("totalDistanceMetres");
                        }
                        catch (NullPointerException e) {
                            Log.d(TAG, "Error: " + e);
                        }
                    }

                    // format the data
                    double distanceMiles = Utils.metresToMiles(totalDistance);
                    double litresUsed = Utils.round2dp(fuelUsed);
                    double mpg = Utils.round2dp(avgMpg / avgCounter);

                    // add data to a map to pass to the ui method
                    Map<String, Double> statistics = new HashMap<>();
                    statistics.put("Total Distance", distanceMiles);
                    statistics.put("Fuel Used", litresUsed);
                    statistics.put("Carbon Footprint", Utils.kgCO2e(litresUsed, 1));
                    statistics.put("Average MPG", mpg);

                    // populate ui elements with data
                    setStatistics(statistics);

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * Populates the statistics fields on the home screen.
     * This is called by the getStatistics method
     * @param statistics
     */
    private void setStatistics(Map<String, Double> statistics) {
        // to make changes to the ui it needs to run on the ui thread
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

                statisticsTable.setVisibility(View.VISIBLE);
                statisticsLoadProgressBar.setVisibility(View.GONE);
            }
        });
    }
}