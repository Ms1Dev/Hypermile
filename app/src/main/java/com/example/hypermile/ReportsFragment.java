package com.example.hypermile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.hypermile.bluetooth.DiscoveredDeviceAdapter;
import com.example.hypermile.reports.Report;
import com.example.hypermile.reports.ReportAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReportsFragment extends Fragment {


    public ReportsFragment() {
        // Required empty public constructor
    }

    public static ReportsFragment newInstance(String param1, String param2) {
        ReportsFragment fragment = new ReportsFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        ListView reportList = view.findViewById(R.id.report_list);

        List<Report> reports = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("journeys")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<QuerySnapshot> task)
                       {
                           if (task.isSuccessful()) {
                               for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("TAG", "onComplete: " + document.getId());
                                    reports.add(new Report(document.getId(), document.getData()));
                               }
                           }
                           else {
                               Log.w("Report list", "Error getting documents.", task.getException());
                           }
                       }
                   });

        Map<String,Object> testmap = new HashMap<>();
        testmap.put("test", "asd");

        reports.add(new Report("test", testmap));

        ReportAdapter reportAdapter = new ReportAdapter(this.getContext(), reports);

        reportList.setAdapter(reportAdapter);

        return view;
    }
}