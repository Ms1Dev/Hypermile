package com.example.hypermile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.Image;
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
import com.example.hypermile.reports.JourneyMonitor;
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
    private JourneyMonitor journeyMonitor;
    private Snackbar alertSnackBar;

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
    }

    /**
     *  This is called when the user selects an option for permissions
     */
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
        // create a bluetooth connection instance
        connection = new Connection();
        // create an obd connection instance (connection of device to car ECU)
        obd = new Obd();
        // obd can only start once a bluetooth connection is established so listen for connection
        connection.addConnectionEventListener(obd);
        // pass the connection to the OBD object to allow it to communicate with the device
        obd.initialise(connection);

        // status bar for showing status of bluetooth and obd
        connectionStatusBar = findViewById(R.id.connectionStatusBar);
        // connection status bar listen to bluetooth connection status
        connection.addConnectionEventListener( connectionStatusBar.getBlueToothConnectionListener() );
        // status bar must also listen for changes in obd connection
        obd.addConnectionEventListener( connectionStatusBar.getObdConnectionListener() );
        // the main activity will store the MAC address on successful connection so it must listen to connection changes as well
        connection.addConnectionEventListener(this);
        // attempt to automatically connect to a bluetooth device if MAC address is stored
        connection.connectToExisting(this);

        // create a data manager for all data sources
        dataManager = new DataManager(this, obd);
        // data manager requires an OBD connection
        obd.addConnectionEventListener(dataManager);

        // live data fragment uses the data manager to access data sources
        liveDataFragment.setDataManager(dataManager);
        // it can only begin when the data manager is ready so it listens for ready state
        dataManager.addDataManagerReadyListener(liveDataFragment);

        // journey monitor will create instances of journeys when it detects start/stop conditions
        JourneyMonitor journeyMonitor = new JourneyMonitor(dataManager);
        // the journey monitor needs RPM and connection status to recognise start and end of journeys
        obd.addConnectionEventListener(journeyMonitor);
        dataManager.addDataManagerReadyListener(journeyMonitor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow, menu);
        return true;
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

    /**
     * Launches the select bluetooth device activity
     */
    ActivityResultLauncher<Void> selectBluetoothActivityLauncher = registerForActivityResult(new ActivityResultContract<Void, BluetoothDevice>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void unused) {
            return new Intent(MainActivity.this, SelectBluetoothDeviceActivity.class);
        }
        @Override
        public BluetoothDevice parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
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
                // pass the selected device to the connection object to attempt a connection
                connection.manuallySelectedConnection(result);
                // change the state of the connection status bar
                connectionStatusBar.getBlueToothConnectionListener().onStateChange(connection.getConnectionState());
            }
        }
    });

    /**
     * Shows a snack bar with alert message for the user
     * @param userAlert
     */
    public void alertUser(UserAlert userAlert) {
        Snackbar snackbar = null;
        switch (userAlert) {
            case VEHICLE_SPEC_UNKNOWN:
                alertSnackBar = Snackbar.make(this, getWindow().getDecorView(), "Vehicle details need to be manually set", Snackbar.LENGTH_INDEFINITE);
                alertSnackBar.setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchSettings();
                    }
                });
                break;
            case BLUETOOTH_PERMISSION_NOT_GRANTED:
                alertSnackBar = Snackbar.make(this, getWindow().getDecorView(), "Bluetooth permissions required", Snackbar.LENGTH_INDEFINITE);
                alertSnackBar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertSnackBar.dismiss();
                    }
                });
                break;
            case FIREBASE_ERROR:
                alertSnackBar = Snackbar.make(this, getWindow().getDecorView(), "Database error. Please contact support", Snackbar.LENGTH_INDEFINITE);
                alertSnackBar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertSnackBar.dismiss();
                    }
                });
                break;
        }
        alertSnackBar.setAnchorView(findViewById(R.id.connectionStatusBar));
        alertSnackBar.show();
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
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(homeFragment).hide(reportsFragment).hide(liveDataFragment);

        if (item_id == R.id.home) {
            fragmentTransaction.show(homeFragment).commit();
            return true;
        }
        else if (item_id == R.id.live_data) {
            fragmentTransaction.show(liveDataFragment).commit();
            return true;
        }
        else if (item_id == R.id.reports) {
            fragmentTransaction.show(reportsFragment).commit();
            return true;
        }

        return false;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        if (connection != null) {
            connection.disconnect();
        }
        Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Called when the bluetooth connection changes.
     * If connection is successful then the MAC address of the device is stored in shared preferences
     * @param connectionState
     */
    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            String macAddress = connection.getBluetoothDevice().getAddress();
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(PREFERENCE_DEVICE_MAC, macAddress).apply();
        }
    }
}