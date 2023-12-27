package com.example.hypermile.visual;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hypermile.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

public class GaugeView extends RelativeLayout {
    private final static int DIAL_RANGE = 75;
    private final static int NORMAL_TEXT_SIZE = 25;
    private final static int NORMAL_LABEL_SIZE = 11;
    private final static int NORMAL_DIAL_SIZE = 250;
    private final static int NORMAL_SIZE = 400;
    private CircularProgressIndicator dial;
    private CircularProgressIndicator dialTrack;
    private TextView label;
    private TextView unitLabel;
    int value = 0;
    int min = 0;
    int max = 0;
    int decimalPoints = 0;
    View view;

    public GaugeView(Context context) {
        super(context);
        initialise(context, null);
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs);
    }

    private void initialise(Context context, AttributeSet attrs) {
        view = LayoutInflater.from(context).inflate(R.layout.gauge_view, this);
        dial = view.findViewById(R.id.dial);
        dialTrack = view.findViewById(R.id.dial_track);
        label = view.findViewById(R.id.label);
        unitLabel = view.findViewById(R.id.unit_label);

        if (attrs != null) {
            TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.GaugeView);
            int width = attributes.getDimensionPixelSize(R.styleable.GaugeView_width, NORMAL_SIZE);
            int height = attributes.getDimensionPixelSize(R.styleable.GaugeView_height, NORMAL_SIZE);
            int scale = Math.min(width,height);

            ConstraintLayout rootLayout = view.findViewById(R.id.gauge_root_layout);
            LayoutParams rootLayoutParams = (LayoutParams) rootLayout.getLayoutParams();
            rootLayoutParams.height = height;
            rootLayoutParams.width = width;
            rootLayout.setLayoutParams(rootLayoutParams);

            double scaleMultiplier = (scale * 1.0) / NORMAL_SIZE;
            dial.setIndicatorSize((int) (NORMAL_DIAL_SIZE * scaleMultiplier));
            dialTrack.setIndicatorSize((int) (NORMAL_DIAL_SIZE * scaleMultiplier));
            label.setTextSize((float) (NORMAL_TEXT_SIZE * scaleMultiplier));
            unitLabel.setTextSize((float) (NORMAL_LABEL_SIZE * scaleMultiplier));

            Resources resources = view.getResources();

            int dialColor = attributes.getColor(R.styleable.GaugeView_dialColour, resources.getColor(R.color.primary));
            int trackColor = attributes.getColor(R.styleable.GaugeView_trackColour, resources.getColor(R.color.secondary_container));
            int backgroundColor = attributes.getColor(R.styleable.GaugeView_backgroundColour, resources.getColor(R.color.white));
            int labelColor = attributes.getColor(R.styleable.GaugeView_labelColour, resources.getColor(R.color.black));

            setDialColour(dialColor);
            setTrackColour(trackColor);
            setBackgroundColour(backgroundColor);
            setLabelColour(labelColor);

            String unitLabel = attributes.getString(R.styleable.GaugeView_unitLabel);
            decimalPoints = attributes.getInt(R.styleable.GaugeView_decimalPlaces, 0);
            setUnit(unitLabel);

            dial.setVisibility(View.GONE);
            dialTrack.setVisibility(View.GONE);

            if (attributes.getBoolean(R.styleable.GaugeView_showDial, true)) {
                 showDial();
            }
        }
    }

    private int convertToDialValue(int value) {
        if (min == max) return 0;
        int convertedValue = ((value - min) * DIAL_RANGE) / (max - min);
        if (convertedValue > DIAL_RANGE) convertedValue = DIAL_RANGE;
        else if (convertedValue < 0) convertedValue = 0;
        return convertedValue;
    }

    public void updateValue(Double value) {
        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String labelValue = String.format(Locale.getDefault(),"%."+ decimalPoints +"f", value);
                label.setText(labelValue);
                dial.setProgress(convertToDialValue(value.intValue()), true);
            }
        });
    }

    public void setRange(int min, int max) {
        this.min = min;
        this.max = max;
        if (max > min) {
            showDial();
        }
    }

    public void setUnit(String unit) {
        if (unit != null) {
            ((Activity) view.getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    unitLabel.setText(unit);
                }
            });
        }
    }

    public void setDecimalPoints(int decimalPoints) {
        this.decimalPoints = decimalPoints;
    }

    public void setDialColour(int colour) {
        dial.setIndicatorColor(colour);
    }

    public void setTrackColour(int colour) {
        dialTrack.setIndicatorColor(colour);
    }

    public void setBackgroundColour(int colour) {
        view.findViewById(R.id.gauge_root_layout).setBackgroundColor(colour);
    }

    public void setLabelColour(int colour) {
        label.setTextColor(colour);
        unitLabel.setTextColor(colour);
    }

    public void showDial() {
        dial.setVisibility(View.VISIBLE);
        dialTrack.setVisibility(View.VISIBLE);
    }

}
