package com.example.hypermile.visual;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.example.hypermile.R;
import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;

/**
 * UI component of the ConnectionStatusBar
 * Provides the progress bar for one of the connections
 */
public class ProgressStatusBar extends RelativeLayout implements ConnectionEventListener {
    View view;
    ProgressBar progressStatusBar;
    FrameLayout tickImage;
    FrameLayout crossImage;
    FrameLayout errorImage;

    public ProgressStatusBar(Context context) {
        super(context);
        initialise(context, null);
    }

    public ProgressStatusBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
    }

    public ProgressStatusBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs);
    }

    private void initialise(Context context, AttributeSet attrs) {
        view = LayoutInflater.from(context).inflate(R.layout.progress_status_bar, this);
        progressStatusBar = view.findViewById(R.id.statusProgressBar);
        tickImage = view.findViewById(R.id.tickImage);
        crossImage = view.findViewById(R.id.crossImage);
        errorImage = view.findViewById(R.id.errorImage);

        stateDisconnected();
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        switch (connectionState) {
            case DISCONNECTED:
                stateDisconnected();
                break;
            case CONNECTING:
                stateWorking();
                break;
            case CONNECTED:
                stateConnected();
                break;
            case ERROR:
                stateError();
                break;
        }
    }

    public void stateDisconnected() {
        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressStatusBar.setIndeterminate(false);
                progressStatusBar.setScaleY((float)0.6);
                progressStatusBar.setProgress(0);
                tickImage.setVisibility(View.INVISIBLE);
                crossImage.setVisibility(View.VISIBLE);
                errorImage.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void stateConnected() {
        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressStatusBar.setIndeterminate(false);
                progressStatusBar.setScaleY((float)0.6);
                progressStatusBar.setProgress(100);
                tickImage.setVisibility(View.VISIBLE);
                crossImage.setVisibility(View.INVISIBLE);
                errorImage.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void stateWorking() {
        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressStatusBar.setIndeterminate(true);
                progressStatusBar.setScaleY((float)0.75);
                tickImage.setVisibility(View.INVISIBLE);
                crossImage.setVisibility(View.INVISIBLE);
                errorImage.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void stateError() {
        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressStatusBar.setIndeterminate(false);
                progressStatusBar.setScaleY((float)0.6);
                progressStatusBar.setProgress(0);
                tickImage.setVisibility(View.INVISIBLE);
                crossImage.setVisibility(View.INVISIBLE);
                errorImage.setVisibility(View.VISIBLE);
            }
        });
    }
}
