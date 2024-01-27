package com.example.hypermile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class MapsFragment extends Fragment {
    double startLat;
    double startLong;
    int lowMpgBoundary;
    int midMpgBoundary;
    ArrayList<Location> route;
    public MapsFragment(ArrayList<Location> route, int lowMpgBoundary, int midMpgBoundary) {
        this.route = route;
        this.lowMpgBoundary = lowMpgBoundary;
        this.midMpgBoundary = midMpgBoundary;
        if (route.size() > 0) {
            startLat = route.get(0).getLatitude();
            startLong = route.get(0).getLongitude();
        }
    }
//https://www.digitalocean.com/community/tutorials/android-google-map-drawing-route-two-points
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
    private void drawRoute(GoogleMap googleMap) {
//        ArrayList<LatLng> points = new ArrayList<>();
//        PolylineOptions lineOptions = new PolylineOptions();

        Location prevLocation = null;
        for (Location location : route) {
//            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
//            points.add(position);
            if (prevLocation != null) {

                LatLng posTo = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng posFrom = new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude());
                PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.add(posFrom,posTo).color(getLineColour(Double.valueOf(prevLocation.getProvider())));
                googleMap.addPolyline(lineOptions);
            }
            prevLocation = location;
        }

//        lineOptions.addAll(points);
//        lineOptions.width(12);
//        lineOptions.color(Color.RED);
//        lineOptions.geodesic(true);
//        googleMap.addPolyline(lineOptions);

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