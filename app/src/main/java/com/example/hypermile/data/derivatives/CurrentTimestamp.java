package com.example.hypermile.data.derivatives;

import com.example.hypermile.data.DataSource;
import com.example.hypermile.data.PollCompleteListener;
import com.example.hypermile.data.PollingElement;

import java.sql.Timestamp;

public class CurrentTimestamp extends DataSource<Timestamp> implements PollCompleteListener {
    @Override
    public void pollingComplete() {
        data = new Timestamp(System.currentTimeMillis());
        notifyObservers(data);
    }
}
