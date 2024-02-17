package com.example.hypermile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Map;

public class MapsFragment extends Fragment {
    double startLat;
    double startLong;
    int lowMpgBoundary;
    int midMpgBoundary;
    ArrayList<Map<String,Double>> route;
    public MapsFragment(ArrayList<Map<String,Double>> route, int lowMpgBoundary, int midMpgBoundary) {
        this.route = route;
        this.lowMpgBoundary = lowMpgBoundary;
        this.midMpgBoundary = midMpgBoundary;
        if (route.size() > 0) {
            startLat = route.get(0).get("latitude");
            startLong = route.get(0).get("longitude");
        }
    }

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng startLoc = new LatLng(startLat, startLong);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLoc,12));
            drawRoute(googleMap);
        }
    };

    private int getLineColour(Double mpgValue) {
        if (mpgValue < lowMpgBoundary) {
            return ReportActivity.LOW_MPG_COLOUR;
        }
        else if (mpgValue < midMpgBoundary) {
            return ReportActivity.MID_MPG_COLOUR;
        }
        else {
            return ReportActivity.HIGH_MPG_COLOUR;
        }
    }

    /**
     * Draws a line representing the route on the map
     * The line is built piece by piece using gps coordinates that were stored from the journey
     * Each segment of the line is coloured depending on the MPG at that given moment.
     * @param googleMap
     */
    private void drawRoute(GoogleMap googleMap) {
        // running this on a new thread so it doesn't block the main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,Double> prevCoordinate = null;

                // iterate through all coodinates that were stored from the journey
                for (Map<String,Double> coordinate : route) {
                    PolylineOptions polyline = new PolylineOptions();
                    if (prevCoordinate != null) {
                        LatLng posTo = new LatLng(coordinate.get("latitude"), coordinate.get("longitude"));
                        LatLng posFrom = new LatLng(prevCoordinate.get("latitude"), prevCoordinate.get("longitude"));

                        // set the colour of the poly line
                        Double mpg = prevCoordinate.get("mpg");;
                        if (mpg == null) {
                            mpg = 0.0;
                        }
                        polyline.add(posFrom,posTo).color(getLineColour(mpg));
                        polyline.endCap(new RoundCap()); // rounding the ends of each segment creates a smooth line
                    }
                    prevCoordinate = coordinate;

                    // ui changes still need to be done on the main thread
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleMap.addPolyline(polyline);
                        }
                    });
                }
            }
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}