package kz.mobdev.mapview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.onlylemi.mapview.R;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import kz.mobdev.mapview.library.MapView;
import kz.mobdev.mapview.library.MapViewListener;
import kz.mobdev.mapview.library.layers.LocationLayer;
import kz.mobdev.mapview.library.layers.MapLayer;

public class LocationLayerTestActivity extends AppCompatActivity implements SensorEventListener, MapView.OnRotationChanged {

    private MapView mapView;

    private LocationLayer locationLayer;

    private boolean openSensor = false;

    private SensorManager sensorManager;

    Sensor accelerometer;
    Sensor magnetometer;
    Sensor rotationSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_layer_test);

        mapView = (MapView) findViewById(R.id.mapview);
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
            }

            @Override
            public void onMapLoadFail() {

            }
        });
        mapView.loadMap(bitmap);

        mapView.setOnRotationChanged(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        assert sensorManager != null;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_location_layer_test, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mapView.isMapLoadFinish()) {
            switch (item.getItemId()) {
                case R.id.location_layer_set_mode:
                    if (locationLayer.isOpenCompass()) {
                        item.setTitle("Open Compass");
                    } else {
                        item.setTitle("Close Compass");
                    }
                    locationLayer.setOpenCompass(!locationLayer.isOpenCompass());
                    mapView.refresh();
                    break;
                case R.id.location_layer_set_compass_circle_rotate:
                    float rotate = 90;
                    locationLayer.setCompassIndicatorCircleRotateDegree(rotate);
                    mapView.refresh();
                    Toast.makeText(this, "circle rotate: " + rotate, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.location_layer_set_compass_arrow_rotate:
                    rotate = 30;
                    locationLayer.setCompassIndicatorArrowRotateDegree(rotate);
                    mapView.refresh();
                    Toast.makeText(this, "arrow rotate: " + rotate, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.location_layer_set_auto_sensor:
                    if (openSensor) {
                        item.setTitle("Open Sensor");
                        sensorManager.unregisterListener(this);
                    } else {
                        item.setTitle("Close Sensor");
                        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
                        //sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    }
                    openSensor = !openSensor;
                    break;
                default:
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    float[] mGravity = new float[3];
    float[] mGeomagnetic = new float[3];
    long count = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        //if (mapView.isMapLoadFinish()/* && openSensor*/) {
        float mapDegree = 0; // the rotate between reality map to northern
        final float[] degree = {0f};
        float alpha = 0.97f;

//        count++;

        //Log.d("suka", "onSensorChanged: before run");
        Observable.just(event)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<SensorEvent>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(SensorEvent sensorEvent) {
                        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                            mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                            mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                            mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
                        }
                        //mGravity = sensorEvent.values.clone();
                        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                            mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                            mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                            mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
                        }

                        if (mGravity != null && mGeomagnetic != null) {
                            float R[] = new float[9];
                            float I[] = new float[9];

                            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                            if (success) {
                                float orientation[] = new float[3];
                                SensorManager.getOrientation(R, orientation);

                                degree[0] = (float) Math.toDegrees(orientation[0]);
                                degree[0] = (degree[0] + 360) % 360;
                            }

                            //Log.d("suka", "onSensorChanged: on run");

                            locationLayer.setCompassIndicatorArrowRotateDegree(mapDegree + mapView
                                    .getCurrentRotateDegrees() + degree[0]);
                            //mapView.refreshLocationLayer();
                            mapView.refresh();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onRotateBegin() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onRotateEnd() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }
}
