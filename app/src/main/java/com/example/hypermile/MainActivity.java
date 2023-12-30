package com.example.hypermile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hypermile.api.ApiRequest;
import com.example.hypermile.bluetooth.Connection;
import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;
import com.example.hypermile.dataGathering.DataManager;
import com.example.hypermile.dataGathering.sources.VehicleDataLogger;
import com.example.hypermile.obd.Obd;
import com.example.hypermile.visual.ConnectionStatusBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ConnectionEventListener {
    private static final String PREFERENCE_FILENAME = "Hypermile_preferences";
    private static final String PREFERENCE_DEVICE_MAC = "ConnectedDeviceMAC";
    public final static String CONNECTION_STATE = "com.example.hypermile.CONNECTION_STATE";
    LiveDataFragment liveDataFragment;
    HomeFragment homeFragment;
    ReportsFragment reportsFragment;
    ConnectionStatusBar connectionStatusBar;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeFragment = new HomeFragment();
        liveDataFragment = new LiveDataFragment();
        reportsFragment = new ReportsFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.mainContent, homeFragment)
                .add(R.id.mainContent, reportsFragment)
                .add(R.id.mainContent, liveDataFragment)
                .commit();

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Connection connection = Connection.getInstance();
        connection.connectToExisting(this);

        connectionStatusBar = findViewById(R.id.connectionStatusBar);
        connection.addConnectionEventListener( connectionStatusBar.getBlueToothConnectionListener() );
        Obd.addConnectionEventListener( connectionStatusBar.getObdConnectionListener() );

        connection.addConnectionEventListener(this);

        ApiRequest.setContext(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Obd.isReady());
                obdReady();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow, menu);
        return true;
    }

    public void obdReady() {
        DataManager.getInstance().initialise(this);
        liveDataFragment.connectDataToGauges();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();

        if (item_id == R.id.signout) {
            signOut();
            return true;
        }
        else if (item_id == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item_id == R.id.bluetooth) {
            selectBluetoothActivityLauncher.launch(null);
            return true;
        }
        else if (item_id == R.id.car_details) {
            Intent carDetailsIntent = new Intent(MainActivity.this, CarDetailsActivity.class);
            startActivity(carDetailsIntent);
            return true;
        }

        return false;
    }


    ActivityResultLauncher<Void> selectBluetoothActivityLauncher = registerForActivityResult(new ActivityResultContract<Void, ConnectionState>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void unused) {
            return new Intent(MainActivity.this, SelectBluetoothDeviceActivity.class);
        }
        @Override
        public ConnectionState parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                return (ConnectionState) intent.getSerializableExtra(CONNECTION_STATE);
            }
            return null;
        }
    }, new ActivityResultCallback<ConnectionState>() {
        @Override
        public void onActivityResult(ConnectionState result) {
            if (result != null) {
                connectionStatusBar.getBlueToothConnectionListener().onStateChange(result);
            }
        }
    });


    public void alertUser(UserAlert userAlert) {

        switch (userAlert) {
            case VEHICLE_SPEC_UNKNOWN:
                Snackbar snackbar = Snackbar.make(this, getWindow().getDecorView(), "Vehicle details need to be manually set", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAnchorView(findViewById(R.id.connectionStatusBar));
                snackbar.setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchSettings();
                    }
                });
                snackbar.show();
                break;
        }
    }

    private void launchSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();
        return loadFragment(item_id);
    }

    public void selectFragmentProgrammatically(int item_id) {
        loadFragment(item_id);
        bottomNavigationView.setSelectedItemId(item_id);
    }

    private boolean loadFragment(int item_id) {
        if (item_id == R.id.home) {
            getSupportFragmentManager().beginTransaction()
                    .show(homeFragment)
                    .hide(reportsFragment)
                    .hide(liveDataFragment)
                    .commit();
            return true;
        }
        else if (item_id == R.id.live_data) {
            getSupportFragmentManager().beginTransaction()
                    .show(liveDataFragment)
                    .hide(reportsFragment)
                    .hide(homeFragment)
                    .commit();
            return true;
        }
        else if (item_id == R.id.reports) {
            getSupportFragmentManager().beginTransaction()
                    .show(reportsFragment)
                    .hide(liveDataFragment)
                    .hide(homeFragment)
                    .commit();
            return true;
        }

        return false;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            String macAddress = Connection.getInstance().getBluetoothDevice().getAddress();
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(PREFERENCE_DEVICE_MAC, macAddress).apply();
        }
    }
}