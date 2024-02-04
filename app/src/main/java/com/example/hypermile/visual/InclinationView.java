package com.example.hypermile.visual;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.hypermile.R;
import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.util.Utils;

import java.util.Locale;

/**
 * A view that contains an image of a car and shows inclination value
 * Rotates an image of a car so it matches the inclination of the vehicle
 */
public class InclinationView extends RelativeLayout implements DataInputObserver<Double> {
    private final static int DIAL_RANGE = 75;
    private final static int NORMAL_TEXT_SIZE = 25;
    private final static int NORMAL_LABEL_SIZE = 11;
    private final static int NORMAL_DIAL_SIZE = 250;
    private final static int DECIMAL_POINTS = 2;
    private final static int NORMAL_SIZE = 400;
    private ImageView carImage;
    private TextView label;
    int value = 0;
    View view;
    DataSource<Double> dataSource;
    GaugeView gaugeView;

    public InclinationView(Context context) {
        super(context);
        initialise(context, null);
    }

    public InclinationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
    }

    public InclinationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs);
    }


    public void setDataSource(DataSource<Double> dataSource) {
        this.dataSource = dataSource;
        dataSource.addDataInputListener(this);
        TextView title = view.findViewById(R.id.gauge_title);
        title.setText(dataSource.getName());
    }

    private void initialise(Context context, AttributeSet attrs) {
        view = LayoutInflater.from(context).inflate(R.layout.inclination_view, this);

        carImage = view.findViewById(R.id.car_image);
        label = view.findViewById(R.id.label);

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.span = 1;
        layoutParams.weight = 1;

        Utils.unclip(this);

        setLayoutParams(layoutParams);
    }

    public void updateValue(Double value) {
        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String labelValue = String.format(Locale.getDefault(),"%."+ DECIMAL_POINTS +"f°", value);
                label.setText(labelValue);
                carImage.setRotation((-1 * value.floatValue()));
            }
        });
    }

    @Override
    public void incomingData(Double data) {
        updateValue(data);
    }

    public void setUnits(String units) {
        gaugeView.setUnit(units);
    }

}
