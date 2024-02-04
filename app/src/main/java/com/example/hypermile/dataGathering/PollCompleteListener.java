package com.example.hypermile.dataGathering;

/**
 * Interface for classes that need to be notified when a round of sensor polling has completed
 */
public interface PollCompleteListener {
    public void pollingComplete();
}
