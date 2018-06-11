package kz.mobdev.mapview.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.MainThreadDisposable;

public class SensorEventObservableFactory {

    public static Observable<SensorEvent> createSensorEventObservable(@NonNull List<Sensor> sensors,
                                                                      @NonNull SensorManager sensorManager) {
        return Observable.create(subscriber -> {
            MainThreadDisposable.verifyMainThread();

            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (subscriber.isDisposed()) {
                        return;
                    }

                    subscriber.onNext(event);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // NO-OP
                }
            };

            for (Sensor sensor : sensors) {
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
            }

            // unregister listener in main thread when being unsubscribed
            subscriber.setDisposable(new MainThreadDisposable() {
                @Override
                protected void onDispose() {
                    for (Sensor sensor : sensors) {
                        sensorManager.unregisterListener(listener, sensor);
                    }
                }
            });
        });
    }
}