package com.example.hypermile.visual;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.hypermile.R;
import com.example.hypermile.bluetooth.ConnectionEventListener;

public class ConnectionStatusBar extends RelativeLayout {
    View view;
    ProgressStatusBar blueToothStatusBar;
    ProgressStatusBar obdStatusBar;

    public ConnectionStatusBar(Context context) {
        super(context);
        initialise(context, null);
    }

    public ConnectionStatusBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
    }

    public ConnectionStatusBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs);
    }

    private void initialise(Context context, AttributeSet attrs) {
        view = LayoutInflater.from(context).inflate(R.layout.connection_status_bar, this);
        blueToothStatusBar = view.findViewById(R.id.bluetoothStatusBar);
        obdStatusBar = view.findViewById(R.id.odbStatusBar);
    }

    public ConnectionEventListener getBlueToothConnectionListener() {
        return blueToothStatusBar;
    }

    public ConnectionEventListener getObdConnectionListener() {
        return obdStatusBar;
    }
}
