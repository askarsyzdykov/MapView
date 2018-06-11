package kz.mobdev.mapview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.onlylemi.mapview.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import kz.mobdev.mapview.library.MapView;
import kz.mobdev.mapview.library.MapViewListener;
import kz.mobdev.mapview.library.layers.LocationLayer;
import kz.mobdev.mapview.utils.SensorEventObservableFactory;
import kz.mobdev.mapview.utils.TupleSensorEvent;

public class LocationLayerTestActivity extends AppCompatActivity {

    private MapView mapView;

    private LocationLayer locationLayer;

    float[] mGravity = new float[3];
    float[] mGeomagnetic = new float[3];

    Observable<Integer> sensorEventObservable;
    Disposable sensorEventDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_layer_test);

        Observable<SensorEvent> o1 = createAccelerationObservable(this, Sensor.TYPE_ACCELEROMETER);
        Observable<SensorEvent> o2 = createAccelerationObservable(this, Sensor.TYPE_MAGNETIC_FIELD);

        sensorEventObservable = Observable.zip(o1, o2, TupleSensorEvent::new)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::getDegree)
                .filter(degree -> degree != 0f)
                .map(Float::intValue)
                .distinctUntilChanged();

        mapView = findViewById(R.id.mapview);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open("map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onMapLoadSuccess() {
                locationLayer = new LocationLayer(mapView, new PointF(400, 400));
                locationLayer.setOpenCompass(true);
                locationLayer.setCompassIndicatorCircleRotateDegree(60);
                locationLayer.setCompassIndicatorArrowRotateDegree(-30);
                mapView.addLayer(locationLayer);

                mapView.refresh();

                sensorEventDisposable = sensorEventObservable.subscribe(
                        sensorEvent -> updateCompass(sensorEvent)
                );
            }

            @Override
            public void onMapLoadFail() {

            }
        });
        mapView.loadMap(bitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorEventDisposable != null && sensorEventDisposable.isDisposed()) {
            sensorEventDisposable = sensorEventObservable.subscribe(this::updateCompass);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorEventDisposable != null) {
            sensorEventDisposable.dispose();
        }
    }

    private void updateCompass(int degree) {
        Log.d("degree2", String.valueOf(degree));

        locationLayer.setCompassIndicatorArrowRotateDegree(mapView.getCurrentRotateDegrees() + degree);
        mapView.refresh();
    }

    private float getDegree(TupleSensorEvent tuple) {
        SensorEvent accelerometerEvent = tuple.getFirst();
        SensorEvent magneticEvent = tuple.getSecond();

        Log.d("LocationLayerTestActivity", accelerometerEvent.sensor.getName() + " " + Arrays.toString(accelerometerEvent.values));
        Log.d("LocationLayerTestActivity", magneticEvent.sensor.getName() + " " + Arrays.toString(magneticEvent.values));

        final float[] degree = {0f};
        float alpha = 0.97f;

        mGravity[0] = alpha * mGravity[0] + (1 - alpha) * accelerometerEvent.values[0];
        mGravity[1] = alpha * mGravity[1] + (1 - alpha) * accelerometerEvent.values[1];
        mGravity[2] = alpha * mGravity[2] + (1 - alpha) * accelerometerEvent.values[2];

        mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * magneticEvent.values[0];
        mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * magneticEvent.values[1];
        mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * magneticEvent.values[2];

        float R[] = new float[9];
        float I[] = new float[9];

        boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
        if (success) {
            float orientation[] = new float[3];
            SensorManager.getOrientation(R, orientation);

            degree[0] = (float) Math.toDegrees(orientation[0]);
            degree[0] = (degree[0] + 360) % 360;
        }

        Log.d("raw degree", String.valueOf(degree[0]));
        return degree[0];
    }

    @NonNull
    private static Observable<SensorEvent> createAccelerationObservable(@NonNull Context context, int sensorType) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(sensorType);
        if (sensors.isEmpty()) {
            throw new IllegalStateException("Device has no linear acceleration sensor");
        }

        return SensorEventObservableFactory.createSensorEventObservable(sensors, sensorManager);
    }
}
