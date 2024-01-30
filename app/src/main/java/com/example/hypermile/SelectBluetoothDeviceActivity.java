package com.example.hypermile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;
import com.example.hypermile.bluetooth.DeviceSelectedCallback;
import com.example.hypermile.bluetooth.DiscoveredDevice;
import com.example.hypermile.bluetooth.DiscoveredDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectBluetoothDeviceActivity extends AppCompatActivity implements DeviceSelectedCallback, ConnectionEventListener {
    private DiscoveredDevice selectedDevice;
    List<DiscoveredDevice> discoveredDevices = new ArrayList<>();
    DiscoveredDeviceAdapter discoveredDeviceAdapter;
    BluetoothAdapter bluetoothAdapter;
    boolean permissionsGranted = false;

    /**
     * This listens out for bluetooth adapter actions. Adds a device to the discovered device list when found.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // device is found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                }
                String deviceName = device.getName();
                if (deviceName != null) {
                    discoveredDeviceAdapter.add(new DiscoveredDevice(device, deviceName, SelectBluetoothDeviceActivity.this));
                }

            }

            // discovery has finished
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                discoveryFinishedCallback();
            }
        }
    };

//    SOURCE: https://developer.android.com/develop/connectivity/bluetooth/setup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bluetooth_device);

        // set the toolbar - title and back btn
        Toolbar toolbar = (Toolbar) findViewById(R.id.selectBluetoothToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Bluetooth Devices");

        if (
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        ) {
            populateDeviceList();
        }
        else {
            Log.d("TAG", "onCreate: " + "no permiss");
            setResult(999);
            finish();
        }
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTING) {
            try {
                bluetoothAdapter.cancelDiscovery();
            } catch (SecurityException e) {
                Log.e("Err", "onStateChange: Failed to cancel discovery", e);
            }
        }
    }

    /**
     * Adds bluetooth devices to the list view
     */
    private void populateDeviceList() {
//        // request permissions
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//            permissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
//        }

        if (
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsGranted = false;
            setResult(999);
            finish();
            return;
        } else {
            permissionsGranted = true;
        }

        // get the bluetooth adapter
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // ensure bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        // create array adapter for bluetooth device list
        ListView discoveredDeviceList = findViewById(R.id.discoveredDeviceList);
        discoveredDeviceAdapter = new DiscoveredDeviceAdapter(this, discoveredDevices);
        discoveredDeviceList.setAdapter(discoveredDeviceAdapter);

        // create action filters for the broadcast object - listen for device found and discovery finishing
        IntentFilter found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, found_filter);
        IntentFilter finished_filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, finished_filter);

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        bluetoothAdapter.startDiscovery();
    }


    // TODO: do something on activity result
    private ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            Log.d("debug", "onActivityResult: " + result);
        }
    });


    /**
     *  Called when bluetooth adapter finishes looking for devices
     */
    private void discoveryFinishedCallback() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }


    /**
     * @brief handles the back button on click
     * @param item
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();

        if (item_id == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            this.finish();
            return true;
        }
        return true;
    }

    @Override
    public void finish() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.cancelDiscovery();
        }
        super.finish();
    }

    /**
     * Unregisters the broadcast receiver on destroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e) {

        }
    }

    @Override
    public void deviceSelected(DiscoveredDevice discoveredDevice) {
        selectedDevice = discoveredDevice;
        Intent intent = new Intent();
        intent.putExtra("device", selectedDevice.getBluetoothDevice());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}