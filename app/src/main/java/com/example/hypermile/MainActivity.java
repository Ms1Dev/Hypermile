package com.example.hypermile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int item_id = item.getItemId();

        if (item_id == R.id.home) {
            HomeFragment homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, homeFragment).commit();
            return true;
        }
        else if (item_id == R.id.live_data) {
            LiveDataFragment liveDataFragment = new LiveDataFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, liveDataFragment).commit();
            return true;
        }
        else if (item_id == R.id.reports) {
            ReportsFragment reportsFragment = new ReportsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, reportsFragment).commit();
            return true;
        }

        return false;
    }
}