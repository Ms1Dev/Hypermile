package com.example.hypermile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hypermile.bluetooth.Connection;
import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;
import com.example.hypermile.dataGathering.DataManager;
import com.example.hypermile.dataGathering.sources.VehicleDataLogger;
import com.example.hypermile.obd.Obd;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ConnectionEventListener {
    private static final String PREFERENCE_FILENAME = "Hypermile_preferences";
    private static final String PREFERENCE_DEVICE_MAC = "ConnectedDeviceMAC";
    LiveDataFragment liveDataFragment;
    HomeFragment homeFragment;
    ReportsFragment reportsFragment;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    TextView deviceStatus;
    ImageView statusConnected;
    ImageView statusDisconnected;
    ProgressBar statusConnecting;
    protected VehicleDataLogger engineSpeed;
    protected VehicleDataLogger massAirFlow;
    protected VehicleDataLogger speed;

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

        deviceStatus = findViewById(R.id.deviceStatusText);
        statusConnected = findViewById(R.id.statusBar_connected);
        statusDisconnected = findViewById(R.id.statusBar_disconnected);
        statusConnecting = findViewById(R.id.statusBar_connecting);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Connection connection = Connection.getInstance();
        onStateChange(connection.getConnectionState());
        connection.addConnectionEventListener(this);
        connection.connectToExisting(this);

//        DataManager.getInstance().initialise();
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
        DataManager.getInstance().initialise();
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
            Intent selectBluetoothIntent = new Intent(MainActivity.this, SelectBluetoothDeviceActivity.class);
            startActivity(selectBluetoothIntent);
            return true;
        }

        return false;
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

    private void persistBluetoothDeviceMacAddress() {
        String macAddress = Connection.getInstance().getBluetoothDevice().getAddress();
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(PREFERENCE_DEVICE_MAC, macAddress).apply();
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusConnected.setVisibility(View.INVISIBLE);
                statusDisconnected.setVisibility(View.INVISIBLE);
                statusConnecting.setVisibility(View.INVISIBLE);
                LinearLayout statusBar = findViewById(R.id.deviceStatusBar);
                switch (connectionState) {
                    case CONNECTED:
                        deviceStatus.setText("Connected");
                        statusConnected.setVisibility(View.VISIBLE);
                        statusBar.setBackgroundColor(getResources().getColor(R.color.secondary_container));
                        persistBluetoothDeviceMacAddress();
                        break;
                    case BLUETOOTH_CONNECTING:
                        deviceStatus.setText("Bluetooth Connecting");
                        statusConnecting.setVisibility(View.VISIBLE);
                        statusBar.setBackgroundColor(getResources().getColor(R.color.tertiary_container));
                        break;
                    case OBD_CONNECTING:
                        deviceStatus.setText("OBD Connecting");
                        statusConnecting.setVisibility(View.VISIBLE);
                        statusBar.setBackgroundColor(getResources().getColor(R.color.tertiary_container));
                        break;
                    case DISCONNECTED:
                        deviceStatus.setText("Disconnected");
                        statusDisconnected.setVisibility(View.VISIBLE);
                        statusBar.setBackgroundColor(getResources().getColor(R.color.error_container));
                        break;
                    case OBD_FAIL:
                        deviceStatus.setText("OBD Fail");
                        statusDisconnected.setVisibility(View.VISIBLE);
                        statusBar.setBackgroundColor(getResources().getColor(R.color.error_container));
                        break;
                    case BLUETOOTH_FAIL:
                        deviceStatus.setText("Bluetooth Fail");
                        statusDisconnected.setVisibility(View.VISIBLE);
                        statusBar.setBackgroundColor(getResources().getColor(R.color.error_container));
                        break;
                }
            }
        });
    }
}