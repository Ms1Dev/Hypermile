package com.example.hypermile.bluetooth;


import android.view.View;

public interface DiscoveredDeviceListElement {
    public int getResource();
    public View setViewContent(View view);
    public boolean cmp(DiscoveredDeviceListElement comparison);
}
