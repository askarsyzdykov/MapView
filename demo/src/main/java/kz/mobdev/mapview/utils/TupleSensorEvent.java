package kz.mobdev.mapview.utils;

import android.hardware.SensorEvent;

/**
 * Created by Askar Syzdykov on 6/11/18.
 */
public class TupleSensorEvent {

    private SensorEvent first;
    private SensorEvent second;

    public TupleSensorEvent(SensorEvent first, SensorEvent second) {
        this.first = first;
        this.second = second;
    }

    public SensorEvent getFirst() {
        return first;
    }

    public SensorEvent getSecond() {
        return second;
    }
}
