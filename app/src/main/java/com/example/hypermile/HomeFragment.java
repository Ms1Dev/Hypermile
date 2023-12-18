package com.example.hypermile;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.hypermile.bluetoothDevices.Connection;
import com.example.hypermile.bluetoothDevices.ConnectionEventListener;
import com.example.hypermile.bluetoothDevices.ConnectionState;
import com.example.hypermile.bluetoothDevices.DiscoveredDevice;

import java.util.zip.Inflater;


public class HomeFragment extends Fragment {
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

        return view;
    }
}