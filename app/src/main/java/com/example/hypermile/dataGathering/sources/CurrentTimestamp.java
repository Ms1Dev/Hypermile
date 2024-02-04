package com.example.hypermile.dataGathering.sources;

import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.dataGathering.PollCompleteListener;

import java.sql.Timestamp;

/**
 * Publishes the timestamp every time a round of sensor polling is complete
 */
public class CurrentTimestamp extends DataSource<Timestamp> implements PollCompleteListener {
    @Override
    public void pollingComplete() {
        data = new Timestamp(System.currentTimeMillis());
        notifyObservers(data);
    }

    @Override
    public String getName() {
        return "Timestamp";
    }
}
