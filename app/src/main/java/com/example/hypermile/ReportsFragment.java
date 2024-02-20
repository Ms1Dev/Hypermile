package com.example.hypermile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.hypermile.bluetooth.DiscoveredDeviceAdapter;
import com.example.hypermile.reports.JourneyData;
import com.example.hypermile.reports.Report;
import com.example.hypermile.reports.ReportAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.Document;

import java.util.ArrayList;
import java.util.Collection;
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

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference journeyCollection = db.collection(userId);

        ReportAdapter reportAdapter = new ReportAdapter(this.getContext(), reports);
        reportList.setAdapter(reportAdapter);

        reportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Report report = (Report) adapterView.getItemAtPosition(pos);

                Intent reportIntent = new Intent(ReportsFragment.this.getContext(), ReportActivity.class);
                reportIntent.putExtra("Report", report);

                startActivity(reportIntent);

            }
        });

        //https://firebase.google.com/docs/firestore/query-data/listen

        journeyCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    ((MainActivity) getContext()).alertUser(UserAlert.FIREBASE_ERROR);
                    return;
                }

                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        QueryDocumentSnapshot document = dc.getDocument();
                        JourneyData journeyData = document.toObject(JourneyData.class);
                        reportAdapter.add(new Report(document.getId(), journeyData));
                    }
                }
            }
        });

        return view;
    }
}