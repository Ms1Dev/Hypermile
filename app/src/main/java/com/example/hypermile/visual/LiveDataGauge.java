package com.example.hypermile.visual;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.hypermile.R;
import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.util.Utils;

/**
 * Extension of the custom gauge view
 * Shows a label with the data along with the gauge
 */
public class LiveDataGauge extends RelativeLayout implements DataInputObserver<Double> {
    DataSource<Double> dataSource;
    GaugeView gaugeView;
    View view;

    public LiveDataGauge(Context context) {
        super(context);
        initialise(context, null);
    }

    public LiveDataGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
    }

    public LiveDataGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs);
    }

    public void setDataSource(DataSource<Double> dataSource) {
        this.dataSource = dataSource;
        dataSource.addDataInputListener(this);
        TextView title = view.findViewById(R.id.gauge_title);
        title.setText(dataSource.getName());
        gaugeView.setUnit(dataSource.getUnits());
        gaugeView.setRange(dataSource.getMinValue(), dataSource.getMaxValue());
        gaugeView.setDecimalPoints(dataSource.getDecimalPoints());
    }

    private void initialise(Context context, AttributeSet attrs) {
        view = LayoutInflater.from(context).inflate(R.layout.live_data_gauge, this);
        gaugeView = view.findViewById(R.id.live_data_dial);

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.span = 1;
        layoutParams.weight = 1;

        Utils.unclip(this);

        setLayoutParams(layoutParams);
    }

    @Override
    public void incomingData(Double data) {
        gaugeView.updateValue(data);
    }

    public void setUnits(String units) {
        gaugeView.setUnit(units);
    }

}
