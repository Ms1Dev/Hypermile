package com.example.hypermile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.example.hypermile.bluetooth.Connection;
import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;
import com.example.hypermile.dataGathering.DataManager;
import com.example.hypermile.dataGathering.sources.CurrentLocation;
import com.example.hypermile.obd.Obd;
import com.example.hypermile.reports.Journey;
import com.example.hypermile.visual.ConnectionStatusBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.security.Permission;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ConnectionEventListener {
    private static final String PREFERENCE_FILENAME = "Hypermile_preferences";
    private static final String PREFERENCE_DEVICE_MAC = "ConnectedDeviceMAC";
    private LiveDataFragment liveDataFragment;
    private HomeFragment homeFragment;
    private ReportsFragment reportsFragment;
    private ConnectionStatusBar connectionStatusBar;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private Obd obd;
    private Connection connection;
    private DataManager dataManager;

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

        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.INTERNET
            }, 0);
        }
        else {
            begin();
        }

        // TODO: if no device in preferences then select from manage bluetooth
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        ) {
            alertUser(UserAlert.BLUETOOTH_PERMISSION_NOT_GRANTED);
        }
        else if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: warn user of GPS and internet needed for maps
        }
        else {
            begin();
        }
    }

    private void begin() {
        connection = new Connection();
        connectionStatusBar = findViewById(R.id.connectionStatusBar);
        connection.addConnectionEventListener( connectionStatusBar.getBlueToothConnectionListener() );
        obd = new Obd();
        connection.addConnectionEventListener(obd);
        obd.addConnectionEventListener( connectionStatusBar.getObdConnectionListener() );
        connection.addConnectionEventListener(this);

        connection.connectToExisting(this);

        dataManager = new DataManager(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                obd.initialise(connection);
                while (!obd.isReady());
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataManager.initialise(obd);
                liveDataFragment.connectDataToGauges(dataManager);
                Journey journey = new Journey();
                obd.addConnectionEventListener(journey);
                journey.addDataSource(dataManager.getEngineSpeed());
                journey.addDataSource(dataManager.getSpeed());
                journey.addDataSource(dataManager.getCalculatedMpg());
                journey.addDataSource(dataManager.getCalculatedInclination());
                journey.addDataSource(dataManager.getFuelRate());
                journey.start(dataManager.getCurrentTimestamp());
                journey.addLocationDataSource(dataManager.getCurrentLocation());
            }
        });
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

        return false;
    }


    ActivityResultLauncher<Void> selectBluetoothActivityLauncher = registerForActivityResult(new ActivityResultContract<Void, BluetoothDevice>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void unused) {
            return new Intent(MainActivity.this, SelectBluetoothDeviceActivity.class);
        }
        @Override
        public BluetoothDevice parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                Log.d("TAG", "onActivityResult() returned: " + intent);
                return intent.getParcelableExtra("device");
            }
            else if (resultCode == 999) {
                alertUser(UserAlert.BLUETOOTH_PERMISSION_NOT_GRANTED);
            }
            return null;
        }
    }, new ActivityResultCallback<BluetoothDevice>() {
        @Override
        public void onActivityResult(BluetoothDevice result) {
            if (result != null) {
                Log.d("TAG", "onActivityResult() returned: " + result);
                connection.manuallySelectedConnection(result);
                connectionStatusBar.getBlueToothConnectionListener().onStateChange(connection.getConnectionState());
            }
        }
    });


    public void alertUser(UserAlert userAlert) {
        Snackbar snackbar = null;
        switch (userAlert) {
            case VEHICLE_SPEC_UNKNOWN:
                snackbar = Snackbar.make(this, getWindow().getDecorView(), "Vehicle details need to be manually set", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchSettings();
                    }
                });
                break;
            case BLUETOOTH_PERMISSION_NOT_GRANTED:
                snackbar = Snackbar.make(this, getWindow().getDecorView(), "Bluetooth permissions required", Snackbar.LENGTH_INDEFINITE);
                Snackbar finalSnackbar = snackbar;
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finalSnackbar.dismiss();
                    }
                });
                break;
        }
        snackbar.setAnchorView(findViewById(R.id.connectionStatusBar));
        snackbar.show();
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
        connection.disconnect();
        Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            String macAddress = connection.getBluetoothDevice().getAddress();
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(PREFERENCE_DEVICE_MAC, macAddress).apply();
        }
    }
}