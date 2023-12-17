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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
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
import android.widget.ProgressBar;

import com.example.hypermile.bluetoothDevices.DiscoveredDevice;
import com.example.hypermile.bluetoothDevices.DiscoveredDeviceAdapter;
import com.example.hypermile.bluetoothDevices.DiscoveredDeviceListElement;
import com.example.hypermile.bluetoothDevices.DiscoveredDeviceSectionHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SelectBluetoothDeviceActivity extends AppCompatActivity {

    List<DiscoveredDeviceListElement> discoveredDevices = new ArrayList<>();

    DiscoveredDeviceAdapter discoveredDeviceAdapter;

    BluetoothAdapter bluetoothAdapter;

//    SOURCE: https://developer.android.com/develop/connectivity/bluetooth/setup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bluetooth_device);

        Toolbar toolbar = (Toolbar) findViewById(R.id.selectBluetoothToolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Bluetooth Devices");

        ListView discoveredDeviceList = findViewById(R.id.discoveredDeviceList);

        discoveredDeviceAdapter = new DiscoveredDeviceAdapter(this, discoveredDevices);

        discoveredDeviceList.setAdapter(discoveredDeviceAdapter);



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
        }


        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        discoveredDeviceAdapter.add(new DiscoveredDeviceSectionHeader("Paired Devices"));

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                discoveredDeviceAdapter.add(new DiscoveredDevice(device,deviceName));
            }
        }

        IntentFilter found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, found_filter);
        IntentFilter finished_filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, finished_filter);

        discoveredDeviceAdapter.add(new DiscoveredDeviceSectionHeader("Discovered Devices"));

        bluetoothAdapter.startDiscovery();
    }

    private ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            Log.d("debug", "onActivityResult: " + result);
        }
    });

    private void discoveryFinishedCallback() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    // Create a BroadcastReceiver
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                }
                String deviceName = device.getName();
                discoveredDeviceAdapter.add(new DiscoveredDevice(device,deviceName));
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                discoveryFinishedCallback();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();

        if (item_id == android.R.id.home) {
            this.finish();
            return true;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }



}